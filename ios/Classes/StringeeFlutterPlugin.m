#import "StringeeFlutterPlugin.h"
#import "StringeeVideoViewFactory.h"
#import "StringeeManager.h"
#import "StringeeCallManager.h"
#import "StringeeCall2Manager.h"
#import "StringeeConversationManager.h"
#import "StringeeMessageManager.h"
#import "StringeeHelper.h"

@implementation StringeeFlutterPlugin {
    FlutterEventSink _eventSink;
    NSArray *DTMF;
    BOOL isConnecting;
    
    StringeeCallManager *_callManager;
    StringeeCall2Manager *_call2Manager;
    StringeeConversationManager *_convManager;
    StringeeMessageManager *_msgManager;
}

#pragma mark - Init

- (instancetype)init {
    self = [super init];
    if (self) {
        DTMF = @[@"0", @"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @"*", @"#"];
        
//        if (!_client) {
//            _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
//            _client.incomingCallDelegate = self;
//
//            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleObjectChangeNotification:) name:StringeeClientObjectsDidChangeNotification object:_client];
//            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNewMessageNotification:) name:StringeeClientNewMessageNotification object:_client];
//        }
//
        _callManager = [[StringeeCallManager alloc] init];
        _call2Manager = [[StringeeCall2Manager alloc] init];
        _convManager = [[StringeeConversationManager alloc] init];
        _msgManager = [[StringeeMessageManager alloc] init];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - Channel

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    // Method channel
    FlutterMethodChannel *methodChannel = [FlutterMethodChannel
                                           methodChannelWithName:STEMethodChannelName
                                           binaryMessenger:[registrar messenger]];
    StringeeFlutterPlugin* instance = [[StringeeFlutterPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:methodChannel];
    
    // Event channel
    FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:STEEventChannelName binaryMessenger:[registrar messenger]];
    [eventChannel setStreamHandler:instance];

    StringeeVideoViewFactory* factory = [[StringeeVideoViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory:factory withId:@"stringeeVideoView"];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    // Client
    if ([call.method isEqualToString:@"connect"]) {
        [self connect:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"disconnect"]) {
        [self disconnect:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"registerPush"]) {
        [self registerPush:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"unregisterPush"]) {
        [self unregisterPush:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendCustomMessage"]) {
        [self sendCustomMessage:call.arguments result:result];
    }
    
    // Call
    else if ([call.method isEqualToString:@"makeCall"]) {
        [_callManager makeCall:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"initAnswer"]) {
        [_callManager initAnswer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"answer"]) {
        [_callManager answer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"hangup"]) {
        [_callManager hangup:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"reject"]) {
        [_callManager reject:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendDtmf"]) {
        [_callManager sendDtmf:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendCallInfo"]) {
        [_callManager sendCallInfo:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getCallStats"]) {
        [_callManager getCallStats:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"mute"]) {
        [_callManager mute:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setSpeakerphoneOn"]) {
        [_callManager setSpeakerphoneOn:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"switchCamera"]) {
        [_callManager switchCamera:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"enableVideo"]) {
        [_callManager enableVideo:call.arguments result:result];
    }
    
    // Call2
    else if ([call.method isEqualToString:@"makeCall2"]) {
        [_call2Manager makeCall:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"initAnswer2"]) {
        [_call2Manager initAnswer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"answer2"]) {
        [_call2Manager answer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"hangup2"]) {
        [_call2Manager hangup:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"reject2"]) {
        [_call2Manager reject:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"mute2"]) {
        [_call2Manager mute:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setSpeakerphoneOn2"]) {
        [_call2Manager setSpeakerphoneOn:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"switchCamera2"]) {
        [_call2Manager switchCamera:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"enableVideo2"]) {
        [_call2Manager enableVideo:call.arguments result:result];
    }
    
    // Conversation
    else if ([call.method isEqualToString:@"createConversation"]) {
        [_convManager createConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationById"]) {
        [_convManager getConversationById:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationByUserId"]) {
        [_convManager getConversationByUserId:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLocalConversations"]) {
        [_convManager getLocalConversations:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLastConversation"]) {
        [_convManager getLastConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationsBefore"]) {
        [_convManager getConversationsBefore:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationsAfter"]) {
        [_convManager getConversationsAfter:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"clearDb"]) {
        [_convManager clearDb:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getTotalUnread"]) {
        [_convManager getTotalUnread:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"delete"]) {
        [_convManager deleteConv:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"addParticipants"]) {
        [_convManager addParticipants:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"removeParticipants"]) {
        [_convManager removeParticipants:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"updateConversation"]) {
        [_convManager updateConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setRole"]) {
        [_convManager setRole:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"markAsRead"]) {
        [_convManager markAsRead:call.arguments result:result];
    }
    
    // Message
    else if ([call.method isEqualToString:@"sendMessage"]) {
        [_msgManager sendMessage:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessages"]) {
        [_msgManager getMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLocalMessages"]) {
        [_msgManager getLocalMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLastMessages"]) {
        [_msgManager getLastMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessagesAfter"]) {
        [_msgManager getMessagesAfter:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessagesBefore"]) {
        [_msgManager getMessagesBefore:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"deleteMessages"]) {
        [_msgManager deleteMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"revokeMessages"]) {
        [_msgManager revokeMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"editMsg"]) {
        [_msgManager editMsg:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"pinOrUnPin"]) {
        [_msgManager pinOrUnPin:call.arguments result:result];
    }
    
    else {
        result(FlutterMethodNotImplemented);
    }
}

- (FlutterError *)onListenWithArguments:(id)arguments eventSink:(FlutterEventSink)events {
    _eventSink = events;
    [_callManager setEventSink:_eventSink];
    [_call2Manager setEventSink:_eventSink];
    [_convManager setEventSink:_eventSink];
    [_msgManager setEventSink:_eventSink];
    return nil;
}

- (FlutterError *)onCancelWithArguments:(id)arguments {
    _eventSink = nil;
    [_callManager setEventSink:_eventSink];
    [_call2Manager setEventSink:_eventSink];
    [_convManager setEventSink:_eventSink];
    [_msgManager setEventSink:_eventSink];
    return nil;
}

#pragma mark - Client Actions

- (void)connect:(id)arguments result:(FlutterResult)result {
    NSDictionary *data = (NSDictionary *)arguments;
    
    if (![data isKindOfClass:[NSDictionary class]]) {
        result(nil);
        return;
    }
    
    if (isConnecting) {
        result(nil);
        return;
    }
    isConnecting = YES;
    
    NSString *token = [data objectForKey:@"token"];
    
    if (!_client) {
        id strServerAddressesData = [data objectForKey:@"serverAddresses"];
        if (strServerAddressesData != nil && strServerAddressesData != [NSNull null]) {
            NSArray *serverAddressesData = [StringeeHelper StringToArray:strServerAddressesData];
            NSArray *serverAddresses = [StringeeHelper parseServerAddressesWithData:serverAddressesData];
            if (serverAddresses.count > 0) {
                _client = [[StringeeClient alloc] initWithConnectionDelegate:self serverAddress:serverAddresses];
            } else {
                _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
            }
        } else {
            _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
        }
        _client.incomingCallDelegate = self;
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleObjectChangeNotification:) name:StringeeClientObjectsDidChangeNotification object:_client];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNewMessageNotification:) name:StringeeClientNewMessageNotification object:_client];
        
        [_callManager setClient:_client];
        [_call2Manager setClient:_client];
        [_convManager setClient:_client];
        [_msgManager setClient:_client];
    }
    
    [_client connectWithAccessToken:token];
    result(nil);
}

- (void)disconnect:(id)arguments result:(FlutterResult)result {
    if (_client) {
        [_client disconnect];
    }
    isConnecting = NO;
    result(nil);
}

- (void)registerPush:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *deviceToken = data[@"deviceToken"];
    BOOL isProduction = [data[@"isProduction"] boolValue];
    BOOL isVoip = [data[@"isVoip"] boolValue];
    
    if (!deviceToken || [deviceToken isKindOfClass:[NSNull class]]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Info is invalid."});
    }
    
    [_client registerPushForDeviceToken:deviceToken isProduction:isProduction isVoip:isVoip completionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)unregisterPush:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSString *deviceToken = (NSString *)arguments;
    
    if (!deviceToken || [deviceToken isKindOfClass:[NSNull class]]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Info is invalid."});
    }
    
    [_client unregisterPushForDeviceToken:deviceToken completionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)sendCustomMessage:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *userId = data[@"toUserId"];
    NSDictionary *message = data[@"message"];
    
    if (!userId || [userId isKindOfClass:[NSNull class]]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"UserId is invalid."});
    }
    
    if (!message || [message isKindOfClass:[NSNull class]] || ![message isKindOfClass:[NSDictionary class]]) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Message is invalid."});
    }
    
    [_client sendCustomMessage:message toUserId:userId completionHandler:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

#pragma mark - Chat Event

- (void)handleObjectChangeNotification:(NSNotification *)notification {
    NSArray *objectChanges = [notification.userInfo objectForKey:StringeeClientObjectChangesUserInfoKey];
    if (!objectChanges.count) {
        return;
    }

    NSMutableArray *objects = [[NSMutableArray alloc] init];

    for (StringeeObjectChange *objectChange in objectChanges) {
        [objects addObject:objectChange.object];
    }

    StringeeObjectChange *firstObjectChange = [objectChanges firstObject];
    id firstObject = [objects firstObject];

    int objectType;
    NSArray *jsObjectDatas;
    if ([firstObject isKindOfClass:[StringeeConversation class]]) {
        objectType = 0;
        jsObjectDatas = [StringeeHelper Conversations:objects];
    } else if ([firstObject isKindOfClass:[StringeeMessage class]]) {
        objectType = 1;
        jsObjectDatas = [StringeeHelper Messages:objects];

        // Xoá đối tượng message đã lưu
        for (NSDictionary *message in jsObjectDatas) {
            NSNumber *state = message[@"state"];
            if (state.intValue == StringeeMessageStatusRead) {
                NSString *localId = message[@"localId"];
                if (localId) {
                    [_msgManager.trackedMessages removeObjectForKey:localId];
                }
            }
        }
    } else {
        objectType = 2;
    }

    id returnObjects = jsObjectDatas ? jsObjectDatas : [NSNull null];

    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(objectType), @"objects" : returnObjects, @"changeType" : @(firstObjectChange.type) }});
}

- (void)handleNewMessageNotification:(NSNotification *)notification {
    NSDictionary *userInfo = [notification userInfo];
    if (!userInfo) return;
    
    NSString *convId = [userInfo objectForKey:StringeeClientNewMessageConversationIDKey];
    if (convId == nil || convId.length == 0) {
        return;
    }
    
    // Lấy về conversation
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            return;
        }
        
        self->_eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(0), @"objects" : @[[StringeeHelper Conversation:conversation]], @"changeType" : @(StringeeObjectChangeTypeUpdate) }});
    }];
}

#pragma mark - Call Actions

//- (void)makeCall:(id)arguments result:(FlutterResult)result {
//    if (![arguments isKindOfClass:[NSDictionary class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"The parameters format is invalid.", @"callInfo" : [NSNull null]});
//        return;
//    }
//    NSDictionary *data = (NSDictionary *)arguments;
//
//    NSString *from = data[@"from"];
//    NSString *to = data[@"to"];
//    NSNumber *isVideoCall = data[@"isVideoCall"];
//    NSString *customData = data[@"customData"];
//    NSString *videoResolution = data[@"videoResolution"];
//
//    StringeeCall *outgoingCall = [[StringeeCall alloc] initWithStringeeClient:_client from:from to:to];
//    outgoingCall.delegate = self;
//    outgoingCall.isVideoCall = [isVideoCall boolValue];
//
//    if (customData && ![customData isKindOfClass:[NSNull class]]) {
//        if ([customData isKindOfClass:[NSString class]] && customData.length) {
//            outgoingCall.customData = customData;
//        }
//    }
//
//    if (videoResolution && ![videoResolution isKindOfClass:[NSNull class]]) {
//        if ([videoResolution isEqualToString:@"NORMAL"]) {
//            outgoingCall.videoResolution = VideoResolution_Normal;
//        } else if ([videoResolution isEqualToString:@"HD"]) {
//            outgoingCall.videoResolution = VideoResolution_HD;
//        }
//    }
//
//
//    [outgoingCall makeCallWithCompletionHandler:^(BOOL status, int code, NSString *message, NSString *data) {
//        if (status) {
//            [[StringeeManager instance].calls setObject:outgoingCall forKey:outgoingCall.callId];
//        }
//
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, @"callInfo" : [self StringeeCall:outgoingCall]});
//    }];
//}
//
//- (void)initAnswer:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Init answer call failed. The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Init answer call failed. The call is not found."});
//        return;
//    }
//
//    [call initAnswerCall];
//    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Init answer call successfully."});
//}
//
//- (void)answer:(id)arguments result:(FlutterResult)result {
//
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Answer call failed. The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Answer call failed. The call is not found."});
//        return;
//    }
//
//    [call answerCallWithCompletionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)hangup:(id)arguments result:(FlutterResult)result {
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Hangup call failed. The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Hangup call failed. The call is not found."});
//        return;
//    }
//
//    [call hangupWithCompletionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)reject:(id)arguments result:(FlutterResult)result {
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Reject call failed. The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Reject call failed. The call is not found."});
//        return;
//    }
//
//    [call rejectWithCompletionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)sendDtmf:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *callId = [data objectForKey:@"callId"];
//    NSString *dtmf = [data objectForKey:@"dtmf"];
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Failed to send. The callId is invalid."});
//    }
//
//    if (!dtmf || [dtmf isKindOfClass:[NSNull class]] || !dtmf.length || ![DTMF containsObject:dtmf]) {
//        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Failed to send. The dtmf is invalid."});
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Failed to send. The call is not found."});
//        return;
//    }
//
//    CallDTMF dtmfParam;
//
//    if ([dtmf isEqualToString:@"0"]) {
//        dtmfParam = CallDTMFZero;
//    }
//    else if ([dtmf isEqualToString:@"1"]) {
//        dtmfParam = CallDTMFOne;
//    }
//    else if ([dtmf isEqualToString:@"2"]) {
//        dtmfParam = CallDTMFTwo;
//    }
//    else if ([dtmf isEqualToString:@"3"]) {
//        dtmfParam = CallDTMFThree;
//    }
//    else if ([dtmf isEqualToString:@"4"]) {
//        dtmfParam = CallDTMFFour;
//    }
//    else if ([dtmf isEqualToString:@"5"]) {
//        dtmfParam = CallDTMFFive;
//    }
//    else if ([dtmf isEqualToString:@"6"]) {
//        dtmfParam = CallDTMFSix;
//    }
//    else if ([dtmf isEqualToString:@"7"]) {
//        dtmfParam = CallDTMFSeven;
//    }
//    else if ([dtmf isEqualToString:@"8"]) {
//        dtmfParam = CallDTMFEight;
//    }
//    else if ([dtmf isEqualToString:@"9"]) {
//        dtmfParam = CallDTMFNine;
//    }
//    else if ([dtmf isEqualToString:@"*"]) {
//        dtmfParam = CallDTMFStar;
//    }
//    else {
//        dtmfParam = CallDTMFPound;
//    }
//
//    [call sendDTMF:dtmfParam completionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)sendCallInfo:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *callId = [data objectForKey:@"callId"];
//    NSDictionary *callInfo = [data objectForKey:@"callInfo"];
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Failed to send. The callId is invalid."});
//        return;
//    }
//
//    if (!callInfo || ![callInfo isKindOfClass:[NSDictionary class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"The call info is invalid."});
//        return;
//    }
//
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Failed to send. The call is not found."});
//        return;
//    }
//
//    [call sendCallInfo:data completionHandler:^(BOOL status, int code, NSString *message) {
//        if (status) {
//            result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Sends successfully"});
//        } else {
//            result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"Failed to send. The client is not connected to Stringee Server."});
//        }
//    }];
//}
//
//- (void)getCallStats:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
//        return;
//    }
//
//    [call statsWithCompletionHandler:^(NSDictionary<NSString *,NSString *> *values) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found.", @"stats" : values});
//    }];
//}
//
//- (void)mute:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *callId = [data objectForKey:@"callId"];
//    BOOL mute = [[data objectForKey:@"mute"] boolValue];
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
//        return;
//    }
//
//    [call mute:mute];
//    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
//}
//
//- (void)setSpeakerphoneOn:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *callId = [data objectForKey:@"callId"];
//    BOOL speaker = [[data objectForKey:@"speaker"] boolValue];
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
//        return;
//    }
//
//    [[StringeeAudioManager instance] setLoudspeaker:speaker];
//    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
//}
//
//- (void)switchCamera:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSString *callId = (NSString *)arguments;
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
//        return;
//    }
//
//    [call switchCamera];
//    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
//}
//
//- (void)enableVideo:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *callId = [data objectForKey:@"callId"];
//    BOOL enableVideo = [[data objectForKey:@"enabled"] boolValue];
//
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"The call is not found."});
//        return;
//    }
//
//    [call enableLocalVideo:enableVideo];
//    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success."});
//}

#pragma mark - Client Delegate

- (void)didConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
}

- (void)didDisConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidDisConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
}

- (void)didFailWithError:(StringeeClient *)stringeeClient code:(int)code message:(NSString *)message {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidFailWithError, STEBody : @{ @"userId" : stringeeClient.userId, @"code" : @(code), @"message" : message }});
}

- (void)requestAccessToken:(StringeeClient *)stringeeClient {
    isConnecting = NO;
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STERequestAccessToken, STEBody : @{ @"userId" : stringeeClient.userId }});
}

- (void)didReceiveCustomMessage:(StringeeClient *)stringeeClient message:(NSDictionary *)message fromUserId:(NSString *)userId {
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveCustomMessage, STEBody : @{ @"fromUserId" : userId, @"message" : message }});
}

- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall:(StringeeCall *)stringeeCall {
    stringeeCall.delegate = _callManager;
    [[StringeeManager instance].calls setObject:stringeeCall forKey:stringeeCall.callId];
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall, STEBody : [StringeeHelper StringeeCall:stringeeCall] });
}

- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall2:(StringeeCall2 *)stringeeCall2 {
    stringeeCall2.delegate = _call2Manager;
    [[StringeeManager instance].call2s setObject:stringeeCall2 forKey:stringeeCall2.callId];
    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall2, STEBody : [StringeeHelper StringeeCall2:stringeeCall2] });
}

#pragma mark - Call Delegate

//- (void)didChangeMediaState:(StringeeCall *)stringeeCall mediaState:(MediaState)mediaState {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidChangeMediaState, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(mediaState) }});
//}
//
//- (void)didChangeSignalingState:(StringeeCall *)stringeeCall signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidChangeSignalingState, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(signalingState) }});
//    if (signalingState == SignalingStateBusy || signalingState == SignalingStateEnded) {
//        [[StringeeManager instance].calls removeObjectForKey:stringeeCall.callId];
//    }
//}
//
//- (void)didReceiveLocalStream:(StringeeCall *)stringeeCall {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveLocalStream, STEBody : @{ @"callId" : stringeeCall.callId }});
//}
//
//- (void)didReceiveRemoteStream:(StringeeCall *)stringeeCall {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveRemoteStream, STEBody : @{ @"callId" : stringeeCall.callId }});
//}
//
//- (void)didReceiveDtmfDigit:(StringeeCall *)stringeeCall callDTMF:(CallDTMF)callDTMF {
//    NSString * digit = @"";
//    if ((long)callDTMF <= 9) {
//        digit = [NSString stringWithFormat:@"%ld", (long)callDTMF];
//    } else if (callDTMF == 10) {
//        digit = @"*";
//    } else if (callDTMF == 11) {
//        digit = @"#";
//    }
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveDtmfDigit, STEBody : @{ @"callId" : stringeeCall.callId, @"dtmf" : digit }});
//}
//
//- (void)didReceiveCallInfo:(StringeeCall *)stringeeCall info:(NSDictionary *)info {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidReceiveCallInfo, STEBody : @{ @"callId" : stringeeCall.callId, @"data" : info }});
//}
//
//- (void)didHandleOnAnotherDevice:(StringeeCall *)stringeeCall signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeCall), STEEvent : STEDidHandleOnAnotherDevice, STEBody : @{ @"callId" : stringeeCall.callId, @"code" : @(signalingState), @"description" : reason }});
//}

#pragma mark - Utils

//- (id)StringeeCall:(StringeeCall *)call {
//    if (!call) {
//        return [NSNull null];
//    }
//    
//    id callId = call.callId ? call.callId :[NSNull null];
//    id from = call.from ? call.from : [NSNull null];
//    id to = call.to ? call.to : [NSNull null];
//    id fromAlias = call.fromAlias ? call.fromAlias : [NSNull null];
//    id toAlias = call.toAlias ? call.toAlias : [NSNull null];
//    id customDataFromYourServer = call.customDataFromYourServer ? call.customDataFromYourServer : [NSNull null];
//    
//    int calltype = 0;
//    if (call.callType == CallTypeCallIn) {
//        // Phone-to-app
//        calltype = 3;
//    } else if (call.callType == CallTypeCallOut) {
//        // App-to-phone
//        calltype = 2;
//    } else if (call.callType == CallTypeInternalIncomingCall) {
//        // App-to-app-incoming-call
//        calltype = 1;
//    } else {
//        // App-to-app-outgoing-call
//        calltype = 0;
//    }
//    
//    NSMutableDictionary *data = [NSMutableDictionary new];
//    [data setObject:callId forKey:@"callId"];
//    [data setObject:from forKey:@"from"];
//    [data setObject:to forKey:@"to"];
//    [data setObject:fromAlias forKey:@"fromAlias"];
//    [data setObject:toAlias forKey:@"toAlias"];
//    [data setObject:@(calltype) forKey:@"callType"];
//    [data setObject:@(call.isVideoCall) forKey:@"isVideoCall"];
//    [data setObject:customDataFromYourServer forKey:@"customDataFromYourServer"];
//    
//    return data;
//}

@end
