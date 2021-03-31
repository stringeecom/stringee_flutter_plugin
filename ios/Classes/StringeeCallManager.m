//
//  StringeeCallManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import "StringeeCallManager.h"
#import "StringeeFlutterPlugin.h"
#import "StringeeManager.h"
#import "StringeeHelper.h"

@implementation StringeeCallManager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
    NSArray *DTMF;
}

- (instancetype)initWithClient:(StringeeClient *)client
{
    self = [super init];
    if (self) {
        _client = client;
        DTMF = @[@"0", @"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @"*", @"#"];
    }
    return self;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        DTMF = @[@"0", @"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @"*", @"#"];
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
    
    StringeeCall *outgoingCall = [[StringeeCall alloc] initWithStringeeClient:_client from:from to:to];
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
            [[StringeeManager instance].calls setObject:outgoingCall forKey:outgoingCall.callId];
        }
        
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, @"callInfo" : [StringeeHelper StringeeCall:outgoingCall]});
    }];
}

- (void)initAnswer:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSString *callId = (NSString *)arguments;
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Init answer call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Init answer call failed. The call is not found."});
        return;
    }
    
    [call initAnswerCall];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Init answer call successfully."});
}

- (void)answer:(id)arguments result:(FlutterResult)result {
    
    NSString *callId = (NSString *)arguments;
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Answer call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Answer call failed. The call is not found."});
        return;
    }
    
    [call answerCallWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)hangup:(id)arguments result:(FlutterResult)result {
    NSString *callId = (NSString *)arguments;
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Hangup call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Hangup call failed. The call is not found."});
        return;
    }
    
    [call hangupWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)reject:(id)arguments result:(FlutterResult)result {
    NSString *callId = (NSString *)arguments;
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Reject call failed. The callId is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Reject call failed. The call is not found."});
        return;
    }
    
    [call rejectWithCompletionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)sendDtmf:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    NSString *dtmf = [data objectForKey:@"dtmf"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Failed to send. The callId is invalid."});
        return;
    }
    
    if (!dtmf || [dtmf isKindOfClass:[NSNull class]] || !dtmf.length || ![DTMF containsObject:dtmf]) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Failed to send. The dtmf is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Failed to send. The call is not found."});
        return;
    }

    CallDTMF dtmfParam;
    
    if ([dtmf isEqualToString:@"0"]) {
        dtmfParam = CallDTMFZero;
    }
    else if ([dtmf isEqualToString:@"1"]) {
        dtmfParam = CallDTMFOne;
    }
    else if ([dtmf isEqualToString:@"2"]) {
        dtmfParam = CallDTMFTwo;
    }
    else if ([dtmf isEqualToString:@"3"]) {
        dtmfParam = CallDTMFThree;
    }
    else if ([dtmf isEqualToString:@"4"]) {
        dtmfParam = CallDTMFFour;
    }
    else if ([dtmf isEqualToString:@"5"]) {
        dtmfParam = CallDTMFFive;
    }
    else if ([dtmf isEqualToString:@"6"]) {
        dtmfParam = CallDTMFSix;
    }
    else if ([dtmf isEqualToString:@"7"]) {
        dtmfParam = CallDTMFSeven;
    }
    else if ([dtmf isEqualToString:@"8"]) {
        dtmfParam = CallDTMFEight;
    }
    else if ([dtmf isEqualToString:@"9"]) {
        dtmfParam = CallDTMFNine;
    }
    else if ([dtmf isEqualToString:@"*"]) {
        dtmfParam = CallDTMFStar;
    }
    else {
        dtmfParam = CallDTMFPound;
    }
    
    [call sendDTMF:dtmfParam completionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)sendCallInfo:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *callId = [data objectForKey:@"callId"];
    NSDictionary *callInfo = [data objectForKey:@"callInfo"];
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Failed to send. The callId is invalid."});
        return;
    }
    
    if (!callInfo || ![callInfo isKindOfClass:[NSDictionary class]]) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"The call info is invalid."});
        return;
    }
    
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Failed to send. The call is not found."});
        return;
    }
    
    [call sendCallInfo:data completionHandler:^(BOOL status, int code, NSString *message) {
        if (status) {
            result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Sends successfully"});
        } else {
            result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"Failed to send. The client is not connected to Stringee Server."});
        }
    }];
}

- (void)getCallStats:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSString *callId = (NSString *)arguments;
    
    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
        return;
    }
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call statsWithCompletionHandler:^(NSDictionary<NSString *,NSString *> *values) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found.", @"stats" : values});
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
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
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
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
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
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
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
    
    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
    
    if (!call) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
        return;
    }
    
    [call enableLocalVideo:enableVideo];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
}

#pragma mark - Call Delegate

- (void)didChangeMediaState:(StringeeCall *)stringeeCall mediaState:(MediaState)mediaState {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidChangeMediaState, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(mediaState) }});
}

- (void)didChangeSignalingState:(StringeeCall *)stringeeCall signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidChangeSignalingState, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(signalingState) }});
    if (signalingState == SignalingStateBusy || signalingState == SignalingStateEnded) {
        [[StringeeManager instance].calls removeObjectForKey:stringeeCall.callId];
    }
}

- (void)didReceiveLocalStream:(StringeeCall *)stringeeCall {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveLocalStream, STEBody : @{ @"callId" : stringeeCall.callId }});
}

- (void)didReceiveRemoteStream:(StringeeCall *)stringeeCall {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveRemoteStream, STEBody : @{ @"callId" : stringeeCall.callId }});
}

- (void)didReceiveDtmfDigit:(StringeeCall *)stringeeCall callDTMF:(CallDTMF)callDTMF {
    NSString * digit = @"";
    if ((long)callDTMF <= 9) {
        digit = [NSString stringWithFormat:@"%ld", (long)callDTMF];
    } else if (callDTMF == 10) {
        digit = @"*";
    } else if (callDTMF == 11) {
        digit = @"#";
    }
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveDtmfDigit, STEBody : @{ @"callId" : stringeeCall.callId, @"dtmf" : digit }});
}

- (void)didReceiveCallInfo:(StringeeCall *)stringeeCall info:(NSDictionary *)info {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveCallInfo, STEBody : @{ @"callId" : stringeeCall.callId, @"data" : info }});
}

- (void)didHandleOnAnotherDevice:(StringeeCall *)stringeeCall signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidHandleOnAnotherDevice, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(signalingState), @"description" : reason }});
}


@end
