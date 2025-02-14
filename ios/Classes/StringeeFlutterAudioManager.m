#import "StringeeFlutterAudioManager.h"
#import "StringeeHelper.h"
#import <AVFoundation/AVFoundation.h>

@interface CustomPortDescription : NSObject
@property (nonatomic, copy) NSString *portType;
@property (nonatomic, copy) NSString *portName;
@property (nonatomic, copy) NSString *UID;

- (instancetype)initWithPortDescription:(AVAudioSessionPortDescription *)portDescription;

@end

@implementation CustomPortDescription

- (instancetype)initWithPortDescription:(AVAudioSessionPortDescription *)portDescription {
    self = [super init];
    if (self) {
        _UID = portDescription.UID;
        _portType = portDescription.portType;
        _portName = portDescription.portName;
    }
    return self;
}

- (instancetype)initWithPortType:(NSString *)portType portName:(NSString *)portName UID:(NSString *)UID {
    self = [super init];
    if (self) {
        _UID = UID;
        _portType = portType;
        _portName = portName;
    }
    return self;
}

- (void)updateWithOutputPort:(AVAudioSessionPortDescription *)output {
    // Optional: Add logic to update device details based on output information if needed
}

- (NSString *)description {
    return [NSString stringWithFormat:@"<CustomPortDescription: portType=%@, portName=%@, UID=%@>",
            self.portType, self.portName, self.UID];
}

@end

@interface StringeeFlutterAudioManager ()

// Audio manager state
@property (nonatomic, strong, nullable) AVAudioSessionPortDescription *selectedAudioDevice;
@property (nonatomic, strong, nullable) NSArray<AVAudioSessionPortDescription *> *inputDevices;
@property (nonatomic, strong, nullable) NSArray<AVAudioSessionPortDescription *> *outputDevices;
@property (nonatomic, strong, nullable) NSArray<CustomPortDescription *> *availableAudioDevices;

// Event sink for broadcasting events
@property (nonatomic, copy) FlutterEventSink eventSink;

@end

@implementation StringeeFlutterAudioManager

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    self = [super init];
    if (self) {
        _availableAudioDevices = @[];
        _selectedAudioDevice = nil; // Default: nil
        _inputDevices = @[];
        _outputDevices = @[];

        // Set up the method channel
        FlutterMethodChannel *methodChannel = [FlutterMethodChannel methodChannelWithName:STEAudioMethodChannelName binaryMessenger:[registrar messenger]];
        [registrar addMethodCallDelegate:self channel:methodChannel];

        // Set up the event channel
        FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:STEAudioEventChannelName binaryMessenger:[registrar messenger]];
        [eventChannel setStreamHandler:self];
    }
    return self;
}

#pragma mark - Method Channel Handling

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([call.method isEqualToString:@"start"]) {
        [self startAudioManagerWithResult:result];
    } else if ([call.method isEqualToString:@"stop"]) {
        [self stopAudioManagerWithResult:result];
    } else if ([call.method isEqualToString:@"selectDevice"]) {
        NSDictionary *args = call.arguments;

        if (![args isKindOfClass:[NSDictionary class]]) {
            result([FlutterError errorWithCode:@"INVALID_ARGUMENT"
                                   message:@"Expected a dictionary for 'device'"
                                   details:nil]);
            return;
        }
        NSDictionary *deviceDic = args[@"device"];
        NSNumber *portTypeCode = deviceDic[@"type"];
        NSString *portType = [self portTypeFromCode:[portTypeCode integerValue]];
        // Parse dictionary to CustomPortDescription
        CustomPortDescription *device = [[CustomPortDescription alloc] init];
        device.portType = portType;
        device.portName = deviceDic[@"name"];
        device.UID = deviceDic[@"uuid"];

        [self selectAudioDevice:device result:result];
    } else {
        result(FlutterMethodNotImplemented);
    }
}

#pragma mark - Event Channel Handling

- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                      eventSink:(FlutterEventSink)events {
    self.eventSink = events;
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    self.eventSink = nil;
    return nil;
}

#pragma mark - Audio Manager Methods

- (void)startAudioManagerWithResult:(FlutterResult)result {
    // Add notification observer for route changes
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleAudioRouteChange:)
                                                 name:AVAudioSessionRouteChangeNotification
                                               object:nil];

    #if DEBUG
    NSLog(@"[Stringee] Audio manager started");
    #endif

    // Notify success
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager started"});
    }

    // send event to Flutter
    [self sendAudioStateUpdate];
}

- (void)stopAudioManagerWithResult:(FlutterResult)result {
    // Remove notification observer for route changes
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:AVAudioSessionRouteChangeNotification
                                                  object:nil];
    
    // Optionally, reset selected audio device and available devices (if needed)
    self.selectedAudioDevice = nil;
    self.inputDevices = nil;
    self.outputDevices = nil;
    self.availableAudioDevices = nil;

    #if DEBUG
    NSLog(@"[Stringee] Audio manager stopped");
    #endif

    // Notify success
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager stopped"});
    }
}

- (void)selectAudioDevice:(CustomPortDescription *)device result:(FlutterResult)result {
    #if DEBUG
    NSLog(@"[Stringee] current audio device = %@", self.selectedAudioDevice);
    NSLog(@"[Stringee] Selecting audio device: %@", device);
    #endif

    // check if the device is already selected
    if (self.selectedAudioDevice && [self.selectedAudioDevice.UID isEqualToString:device.UID]) {
        // Notify success
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio device already selected"});
        return;
    }

    // check if the device is built-in speaker
    if ([device.portType isEqualToString:AVAudioSessionPortBuiltInSpeaker]) {
        // set speaker as the audio output
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        [self handleSpeakerSelectionWithAudioSession:audioSession result:result];
        return;
    }

    // get available inputs
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    NSArray<AVAudioSessionPortDescription *> *availableInputs = audioSession.availableInputs;

    // check if the device is available
    AVAudioSessionPortDescription *deviceInput = nil;
    for (AVAudioSessionPortDescription *input in availableInputs) {
        if ([input.UID isEqualToString:device.UID]) {
            deviceInput = input;
            break;
        }
    }
    if (deviceInput) {
        #if DEBUG
        NSLog(@"[Stringee] Device found in available inputs: device = %@", deviceInput);
        #endif
        // set preferred input
        NSError *error = nil;
        [audioSession setPreferredInput:deviceInput error:&error];
        if (error) {
            result([FlutterError errorWithCode:@"SELECT_DEVICE_ERROR"
                                       message:@"Failed to select the audio device"
                                       details:error.localizedDescription]);
            return;
        } else {
            // Notify success
            result(@{@"status": @YES, @"code": @0, @"message": @"Audio device selected"});
        }
        return;
    }

    // Notify success
    result(@{@"status": @YES, @"code": @0, @"message": @"Audio device selected"});
}

#pragma mark - Audio Route Change Handling
- (void)handleAudioRouteChange:(NSNotification *)notification {
    #if DEBUG
    NSLog(@"[Stringee] Audio route changed");
    #endif
    // Send updated state to Flutter
    [self sendAudioStateUpdate];
}

#pragma mark - Event Broadcast

- (void)sendAudioStateUpdate {
    // Update the current audio route
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    AVAudioSessionRouteDescription *currentRoute = audioSession.currentRoute;

    // Update selectedAudioDevice from current route's outputs
    NSArray<AVAudioSessionPortDescription *> *outputs = currentRoute.outputs;
    if (outputs.count > 0) {
        self.selectedAudioDevice = outputs.firstObject;
    } else {
        self.selectedAudioDevice = nil; // No active output
    }

    // Update the list of available audio devices
    self.availableAudioDevices = [self mergeInputOutputDevices];

    if (self.eventSink) {
        // Prepare available devices list
        NSMutableArray *deviceList = [NSMutableArray array];
        for (AVAudioSessionPortDescription *port in self.availableAudioDevices) {
            [deviceList addObject:@{
                @"uuid": port.UID ?: [NSNull null],
                @"name": port.portName ?: [NSNull null],
                @"type": @([self audioTypeFromPortType:port.portType])
            }];
        }

        // Prepare selected device information
        NSDictionary * selectedDevice = nil;

        // check if type of selectedDevice in deviceList
        for (NSDictionary *device in deviceList) {
            if ([device[@"type"] isEqual:@([self audioTypeFromPortType:self.selectedAudioDevice.portType])]) {
                selectedDevice = device;
                break;
            }
        }

        #if DEBUG
        NSLog(@"[Stringee] current audio device = %@", selectedDevice);
        NSLog(@"[Stringee] available audio devices = %@", self.availableAudioDevices);
        #endif

        // Prepare event data
        if (selectedDevice && deviceList) {
            NSMutableDictionary *event = [NSMutableDictionary dictionary];
            event[@"device"] = selectedDevice;
            event[@"devices"] = deviceList;
            #if DEBUG
            NSLog(@"[Stringee] Audio state update: %@", event);
            #endif

            // Send event to Flutter
            self.eventSink(event);
        } else {
            #if DEBUG
            NSLog(@"[Stringee] Audio state update: No audio device selected");
            #endif
        }
    }
}

#pragma mark - Private helper functions

- (void)handleSpeakerSelectionWithAudioSession:(AVAudioSession *)audioSession result:(FlutterResult)result {
    NSError *error = nil;
    [audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error];

    if (error) {
        result([FlutterError errorWithCode:@"SPEAKER_SELECTION_ERROR"
                                   message:@"Failed to set speaker as the audio output"
                                   details:error.localizedDescription]);
        return;
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        result(@{@"status": @YES, @"code": @0, @"message": @"Speaker selected"});
    });
}

- (NSString *)portTypeFromCode:(NSInteger)code {
    switch (code) {
        case 0:
            return AVAudioSessionPortBuiltInSpeaker;
        case 1:
            return AVAudioSessionPortHeadphones;
        case 2:
            return AVAudioSessionPortBuiltInReceiver;
        case 3:
            return AVAudioSessionPortBluetoothHFP;
        default:
            return nil; // Unknown port type
    }
}

- (NSArray<CustomPortDescription *> *)mergeInputOutputDevices {

    // Get the current audio route
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    AVAudioSessionRouteDescription *currentRoute = audioSession.currentRoute;

    // Update selected audio device based on the current route
    self.selectedAudioDevice = currentRoute.outputs.firstObject;
    // Update the list of available devices (inputs and outputs merged)
    self.inputDevices = audioSession.availableInputs; // Direct input from AVAudioSession
    self.outputDevices = currentRoute.outputs;        // Current outputs from the route

    // Merge input and output devices into a single list
    NSMutableDictionary<NSString *, CustomPortDescription *> *mergedDevices = [NSMutableDictionary dictionary];
    
    // Add output devices to the merged list
    for (AVAudioSessionPortDescription *outputDevice in self.outputDevices) {
        CustomPortDescription *customDevice = [[CustomPortDescription alloc] initWithPortDescription:outputDevice];
        mergedDevices[outputDevice.UID] = customDevice;
    }
    
    // Add input devices with replacements
    for (AVAudioSessionPortDescription *inputDevice in self.inputDevices) {
        NSString *replacementPortType = nil;
        if ([inputDevice.portType isEqualToString:AVAudioSessionPortBuiltInMic]) {
            replacementPortType = AVAudioSessionPortBuiltInReceiver; // Replace Built-In Mic with Built-In Receiver
        } else if ([inputDevice.portType isEqualToString:AVAudioSessionPortHeadsetMic]) {
            replacementPortType = AVAudioSessionPortHeadphones; // Replace Headset Mic with Headphones
        }
        
        if (replacementPortType) {
            // Create a custom device for the replacement
            CustomPortDescription *customDevice = [[CustomPortDescription alloc] init];
            customDevice.portType = replacementPortType;
            customDevice.portName = inputDevice.portName; // Use the same name
            customDevice.UID = inputDevice.UID; // Use the same UID
            mergedDevices[inputDevice.UID] = customDevice;
        } else {
            // Add the input device as is
            CustomPortDescription *customDevice = [[CustomPortDescription alloc] initWithPortDescription:inputDevice];
            mergedDevices[inputDevice.UID] = customDevice;
        }
    }

    // If we have both Built-In Mic and Built-In Receiver, remove the Built-In Receiver entry
    if ([mergedDevices objectForKey:@"Built-In Microphone"] && [mergedDevices objectForKey:@"Built-In Receiver"]) {
        [mergedDevices removeObjectForKey:@"Built-In Receiver"];
    }

    // Ensure built-in speaker is present in the list
    BOOL builtInSpeakerFound = NO;
    for (AVAudioSessionPortDescription *outputDevice in self.outputDevices) {
        if ([outputDevice.portType isEqualToString:AVAudioSessionPortBuiltInSpeaker]) {
            builtInSpeakerFound = YES;
            break;
        }
    }

    if (!builtInSpeakerFound) {
        // Add built-in speaker if not found
        CustomPortDescription *customBuiltInSpeaker = [[CustomPortDescription alloc] initWithPortType:AVAudioSessionPortBuiltInSpeaker portName:@"Built-In Speaker" UID:@"Built-In Speaker"];
        mergedDevices[customBuiltInSpeaker.UID] = customBuiltInSpeaker;
    }

    // #if DEBUG
    // NSLog(@"Input devices = %@", self.inputDevices);
    // NSLog(@"Output devices = %@", self.outputDevices);
    // NSLog(@"Merged devices = %@", mergedDevices);
    // #endif
    
    // Return the merged list of devices as an array
    return mergedDevices.allValues;
}

- (NSInteger)audioTypeFromPortType:(NSString *)portType {
    if ([portType isEqualToString:AVAudioSessionPortBuiltInSpeaker]) {
        return 0; // AudioType.speakerPhone
    } else if ([portType isEqualToString:AVAudioSessionPortHeadphones] ||
               [portType isEqualToString:AVAudioSessionPortLineOut] ||
               [portType isEqualToString:AVAudioSessionPortHeadsetMic]) {
        return 1; // AudioType.wiredHeadset
    } else if ([portType isEqualToString:AVAudioSessionPortBuiltInReceiver] ||
               [portType isEqualToString:AVAudioSessionPortBuiltInMic]) {
        return 2; // AudioType.earpiece
    } else if ([portType isEqualToString:AVAudioSessionPortBluetoothA2DP] ||
               [portType isEqualToString:AVAudioSessionPortBluetoothHFP] ||
               [portType isEqualToString:AVAudioSessionPortBluetoothLE]) {
        return 3; // AudioType.bluetooth
    } else {
        return 4; // AudioType.other
    }
}

@end
