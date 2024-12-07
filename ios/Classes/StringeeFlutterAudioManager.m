#import "StringeeFlutterAudioManager.h"
#import "StringeeHelper.h"

@interface StringeeFlutterAudioManager ()

// Audio manager state
@property (nonatomic, assign) NSInteger selectedAudioDevice;
@property (nonatomic, strong) NSMutableArray<NSNumber *> *availableAudioDevices;

// Event sink for broadcasting events
@property (nonatomic, copy) FlutterEventSink eventSink;

@end

@implementation StringeeFlutterAudioManager

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    self = [super init];
    if (self) {
        _availableAudioDevices = [NSMutableArray array];
        _selectedAudioDevice = -2; // Default: earpiece

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

    // Send initial state to the event sink
    [self sendAudioStateUpdate];
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    self.eventSink = nil;
    return nil;
}

#pragma mark - Audio Manager Methods

- (void)startAudioManagerWithResult:(FlutterResult)result {
    // Mock: Initialize and start the audio manager
    self.selectedAudioDevice = 0; // Example device
    [self.availableAudioDevices addObjectsFromArray:@[@0, @1, @2]]; // Mock device codes

    // Notify success
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager started"});
    }
}

- (void)stopAudioManagerWithResult:(FlutterResult)result {
    // Mock: Stop the audio manager
    self.selectedAudioDevice = -1;
    [self.availableAudioDevices removeAllObjects];

    // Notify success
    if (result) {
        result(@{@"status": @YES, @"code": @0, @"message": @"Audio manager stopped"});
    }
}

- (void)selectAudioDevice:(NSInteger)deviceCode result:(FlutterResult)result {
    if (![self.availableAudioDevices containsObject:@(deviceCode)]) {
        result(@{@"status": @NO, @"code": @-3, @"message": @"Audio device not available to select"});
        return;
    }

    self.selectedAudioDevice = deviceCode;

    // Notify success
    result(@{@"status": @YES, @"code": @0, @"message": @"Audio device selected"});
}

#pragma mark - Event Broadcast

- (void)sendAudioStateUpdate {
    if (self.eventSink) {
        NSDictionary *event = @{
            @"code": @(self.selectedAudioDevice),
            @"codeList": self.availableAudioDevices
        };
        self.eventSink(event);
    }
}

@end