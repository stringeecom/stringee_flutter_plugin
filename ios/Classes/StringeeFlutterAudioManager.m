#import "StringeeFlutterAudioManager.h"
#import "StringeeHelper.h"
#import <AVFoundation/AVFoundation.h>

@interface CustomPortDescription : NSObject
@property (nonatomic, copy) NSString *portType;
@property (nonatomic, copy) NSString *portName;
@property (nonatomic, copy) NSString *UID;
@end

@implementation CustomPortDescription
@end

@interface StringeeFlutterAudioManager ()

// Audio manager state
@property (nonatomic, strong, nullable) AVAudioSessionPortDescription *selectedAudioDevice;
@property (nonatomic, strong, nullable) NSArray<AVAudioSessionPortDescription *> *availableAudioDevices;

// Event sink for broadcasting events
@property (nonatomic, copy) FlutterEventSink eventSink;

@end

@implementation StringeeFlutterAudioManager

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    self = [super init];
    if (self) {
        _availableAudioDevices = @[];
        _selectedAudioDevice = nil; // Default: nil

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
        NSNumber *deviceCode = args[@"device"];
        [self selectAudioDevice:[deviceCode integerValue] result:result];
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
    
    // Get the current audio route
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    AVAudioSessionRouteDescription *currentRoute = audioSession.currentRoute;
    self.selectedAudioDevice = currentRoute.outputs.firstObject;
    
    // Initialize list of available audio devices
    self.availableAudioDevices = [NSMutableArray arrayWithArray:audioSession.availableInputs];

    // Notify success
    #if DEBUG
    NSLog(@"Audio manager started selected audio device = %@", self.selectedAudioDevice);
    #endif
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager started"});
    }

    // send event to Flutter
    [self sendAudioStateUpdate];
}

- (void)stopAudioManagerWithResult:(FlutterResult)result {
    // Mock: Stop the audio manager
    // self.selectedAudioDevice = -1;
    // [self.availableAudioDevices removeAllObjects];

    // Notify success
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager stopped"});
    }
}

- (void)selectAudioDevice:(NSInteger)deviceCode result:(FlutterResult)result {
    // if (![self.availableAudioDevices containsObject:@(deviceCode)]) {
    //     result(@{@"status": @NO, @"code": @-3, @"message": @"Audio device not available to select"});
    //     return;
    // }

    // self.selectedAudioDevice = deviceCode;

    // Notify success
    result(@{@"status": @YES, @"code": @0, @"message": @"Audio device selected"});
}

#pragma mark - Audio Route Change Handling
- (void)handleAudioRouteChange:(NSNotification *)notification {
    #if DEBUG
    NSLog(@"Audio route changed");
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

        #if DEBUG
        NSLog(@"Sending audio state update selectedAudiodevic port type = %@", self.selectedAudioDevice.portType);
        #endif
        // Prepare selected device information
        NSDictionary *selectedDevice = self.selectedAudioDevice ? @{
            @"uuid": self.selectedAudioDevice.UID ?: [NSNull null],
            @"name": self.selectedAudioDevice.portName ?: [NSNull null],
            @"type": @([self audioTypeFromPortType:self.selectedAudioDevice.portType])
        } : [NSNull null];

        #if DEBUG
        NSLog(@"selectedDevice = %@", selectedDevice);
        NSLog(@"availableAudioDevices = %@", self.availableAudioDevices);
        #endif

        // Prepare available devices list
        NSMutableArray *deviceList = [NSMutableArray array];
        for (AVAudioSessionPortDescription *port in self.availableAudioDevices) {
            [deviceList addObject:@{
                @"uuid": port.UID ?: [NSNull null],
                @"name": port.portName ?: [NSNull null],
                @"type": @([self audioTypeFromPortType:port.portType])
            }];
        }

        #if DEBUG
        NSLog(@"deviceList = %@", deviceList);
        #endif

        // Prepare event data
        NSDictionary *event = @{
            @"device": selectedDevice,
            @"devices": deviceList
        };

        // Send event to Flutter
        self.eventSink(event);
    }
}

#pragma mark - Private helper functions

- (NSArray<AVAudioSessionPortDescription *> *)mergeInputOutputDevices {
    NSMutableDictionary<NSString *, AVAudioSessionPortDescription *> *mergedDevices = [NSMutableDictionary dictionary];

    // Combine input devices
    // for (AVAudioSessionPortDescription *input in [AVAudioSession sharedInstance].availableInputs) {
    //     mergedDevices[input.UID] = input;
    // }

    #if DEBUG
    // NSLog(@"availableInputs = %@", [AVAudioSession sharedInstance].availableInputs);
    NSLog(@"current route outputs = %@", [AVAudioSession sharedInstance].currentRoute.outputs);
    #endif

    // Combine output devices
    AVAudioSessionRouteDescription *currentRoute = [AVAudioSession sharedInstance].currentRoute;
    for (AVAudioSessionPortDescription *output in currentRoute.outputs) {
        if (mergedDevices[output.UID]) {
            // If the UID already exists, update the entry with the output
            mergedDevices[output.UID] = output;
        } else {
            mergedDevices[output.UID] = output;
        }
    }

    // Ensure built-in speaker and receiver are included
    BOOL hasBuiltInSpeaker = NO;
    BOOL hasBuiltInReceiver = NO;

    for (AVAudioSessionPortDescription *device in mergedDevices.allValues) {
        if ([device.portType isEqualToString:AVAudioSessionPortBuiltInSpeaker]) {
            hasBuiltInSpeaker = YES;
        } else if ([device.portType isEqualToString:AVAudioSessionPortBuiltInReceiver]) {
            hasBuiltInReceiver = YES;
        }
    }

    if (!hasBuiltInSpeaker) {
        CustomPortDescription *builtInSpeaker = [CustomPortDescription new];
        builtInSpeaker.portType = AVAudioSessionPortBuiltInSpeaker;
        builtInSpeaker.portName = @"Built-In Speaker";
        builtInSpeaker.UID = @"Built-In Speaker";
        [mergedDevices setObject:(AVAudioSessionPortDescription *)builtInSpeaker forKey:builtInSpeaker.UID];
    }

    if (!hasBuiltInReceiver) {
        CustomPortDescription *builtInReceiver = [CustomPortDescription new];
        builtInReceiver.portType = AVAudioSessionPortBuiltInReceiver;
        builtInReceiver.portName = @"Receiver";
        builtInReceiver.UID = @"Built-In Receiver";
        [mergedDevices setObject:(AVAudioSessionPortDescription *)builtInReceiver forKey:builtInReceiver.UID];
    }

    #if DEBUG
    NSLog(@"mergedDevices = %@", mergedDevices.allValues);
    #endif

    // Return the merged devices as an array
    return [mergedDevices allValues];
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
