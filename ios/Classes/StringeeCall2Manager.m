//
//  StringeeCall2Manager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import "StringeeCall2Manager.h"
#import "StringeeFlutterPlugin.h"
#import "StringeeManager.h"
#import "StringeeHelper.h"

@implementation StringeeCall2Manager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
    NSString *_identifier;
}

- (instancetype)initWithIdentifier:(NSString *)identifier
{
    self = [super init];
    if (self) {
        _identifier = identifier;
    }
    return self;
}

- (void)setClient:(StringeeClient *)client {
    _client = client;
}

- (void)setEventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
}

#pragma mark - Call Actions

- (void)makeCall:(id)arguments result:(FlutterResult)result {
    if (![arguments isKindOfClass:[NSDictionary class]]) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"The parameters format is invalid.", @"callInfo" : [NSNull null]});
        return;
    }
    NSDictionary *data = (NSDictionary *)arguments;
    
    NSString *from = data[@"from"];
    NSString *to = data[@"to"];
    NSNumber *isVideoCall = data[@"isVideoCall"];
    NSString *customData = data[@"customData"];
    NSString *videoQuality = data[@"videoQuality"];
    
    StringeeCall2 *outgoingCall = [[StringeeCall2 alloc] initWithStringeeClient:_client from:from to:to];
    outgoingCall.delegate = self;
    outgoingCall.isVideoCall = [isVideoCall boolValue];
    
    if (customData && ![customData isKindOfClass:[NSNull class]]) {
        if ([customData isKindOfClass:[NSString class]] && customData.length) {
            outgoingCall.customData = customData;
        }
    }
    
    if (videoQuality && ![videoQuality isKindOfClass:[NSNull class]]) {
        if ([videoQuality isEqualToString:@"NORMAL"]) {
            outgoingCall.videoResolution = VideoResolution_Normal;
        } else if ([videoQuality isEqualToString:@"HD"]) {
            outgoingCall.videoResolution = VideoResolution_HD;
        } else {
            outgoingCall.videoResolution = VideoResolution_HD;
        }
    }
    
    
    [outgoingCall makeCallWithCompletionHandler:^(BOOL status, int code, NSString *message, NSString *data) {
        if (status) {
            [[StringeeManager instance].call2s setObject:outgoingCall forKey:outgoingCall.callId];
        }
        
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, @"callInfo" : [StringeeHelper StringeeCall2:outgoingCall]});
    }];
}

- (void)initAnswer:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = data[@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Init answer call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Init answer call failed. The call is not found."});
        return;
    }
    
    [call initAnswerCall];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Init answer call successfully."});
}

- (void)answer:(id)arguments result:(FlutterResult)result {
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = data[@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Answer call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Answer call failed. The call is not found."});
        return;
    }
    
    [call answerCallWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)hangup:(id)arguments result:(FlutterResult)result {
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = data[@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Hangup call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Hangup call failed. The call is not found."});
        return;
    }
    
    [call hangupWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)reject:(id)arguments result:(FlutterResult)result {
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = data[@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Reject call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Reject call failed. The call is not found."});
        return;
    }
    
    [call rejectWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)mute:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    BOOL mute = [[data objectForKey:@"mute"] boolValue];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call mute:mute];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
}

- (void)setSpeakerphoneOn:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    BOOL speaker = [[data objectForKey:@"speaker"] boolValue];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [[StringeeAudioManager instance] setLoudspeaker:speaker];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
}

- (void)switchCamera:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call switchCamera];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
}

- (void)enableVideo:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    BOOL enableVideo = [[data objectForKey:@"enableVideo"] boolValue];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call enableLocalVideo:enableVideo];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
}

- (void)getCallStats:(id)arguments result:(FlutterResult)result {
    if (!_client) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = data[@"callId"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall2 *call = [[StringeeManager instance].call2s objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call stats:false completionHandler:^(NSDictionary<NSString *,NSString *> *values) {
        NSMutableDictionary *dic;
        if (values != nil) {
            dic = [[NSMutableDictionary alloc] initWithDictionary:values];
            long long milliseconds = (long long)([[NSDate date] timeIntervalSince1970] * 1000);
            [dic setValue:@(milliseconds) forKey:@"timeStamp"];
        }
        result(@{STEStatus : @(true), STECode : @(0), STEMessage: @"Success", @"stats" : dic});
    }];
}

#pragma mark - Call Delegate

- (void)didChangeMediaState2:(StringeeCall2 *)stringeeCall2 mediaState:(MediaState)mediaState {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeCall2), STEEvent : STEDidChangeMediaState, STEBody : @{ @"callId" : stringeeCall2.callId, @"code" : @(mediaState) }});
}

- (void)didChangeSignalingState2:(StringeeCall2 *)stringeeCall2 signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeCall2), STEEvent : STEDidChangeSignalingState, STEBody : @{ @"callId" : stringeeCall2.callId, @"code" : @(signalingState) }});
    if (signalingState == SignalingStateBusy || signalingState == SignalingStateEnded) {
        [[StringeeManager instance].call2s removeObjectForKey:stringeeCall2.callId];
    }
}

- (void)didReceiveLocalStream2:(StringeeCall2 *)stringeeCall2 {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeCall2), STEEvent : STEDidReceiveLocalStream, STEBody : @{ @"callId" : stringeeCall2.callId }});
}

- (void)didReceiveRemoteStream2:(StringeeCall2 *)stringeeCall2 {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeCall2), STEEvent : STEDidReceiveRemoteStream, STEBody : @{ @"callId" : stringeeCall2.callId }});
}

- (void)didHandleOnAnotherDevice2:(StringeeCall2 *)stringeeCall2 signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeCall2), STEEvent : STEDidHandleOnAnotherDevice, STEBody : @{ @"callId" : stringeeCall2.callId, @"code" : @(signalingState), @"description" : reason }});
}

@end
