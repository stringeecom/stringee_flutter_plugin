#import "StringeeFlutterPlugin.h"
#import "StringeeVideoViewFactory.h"
#import "StringeeManager.h"
#import "StringeeCallManager.h"
#import "StringeeCall2Manager.h"
#import "StringeeConversationManager.h"
#import "StringeeMessageManager.h"
#import "StringeeHelper.h"
#import "StringeeClientWrapper.h"

@implementation StringeeFlutterPlugin {
//    NSArray *DTMF;
//    BOOL isConnecting;
    
    FlutterEventSink _eventSink;
//    StringeeCallManager *_callManager;
//    StringeeCall2Manager *_call2Manager;
//    StringeeConversationManager *_convManager;
//    StringeeMessageManager *_msgManager;
}

#pragma mark - Init

- (instancetype)init {
    self = [super init];
    if (self) {
//        DTMF = @[@"0", @"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @"*", @"#"];
        
//        _callManager = [[StringeeCallManager alloc] init];
//        _call2Manager = [[StringeeCall2Manager alloc] init];
//        _convManager = [[StringeeConversationManager alloc] init];
//        _msgManager = [[StringeeMessageManager alloc] init];
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
    if ([call.method isEqualToString:@"setupClient"]) {
        [self setupClient:call.arguments result:result];
        return;
    }
    
    NSDictionary *data = (NSDictionary *)call.arguments;
    NSString *uuid = [data objectForKey:@"uuid"];
    StringeeClientWrapper *wrapper = [StringeeClientWrapper getByUuid:uuid];
    if (wrapper == nil) {
        result(@{STEStatus : @(false), STECode : @(-100), STEMessage: @"Wrapper is not found"});
        return;
    }
    
    // Client
    if ([call.method isEqualToString:@"connect"]) {
        [wrapper connect:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"disconnect"]) {
        [wrapper disconnect:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"registerPush"]) {
        [wrapper registerPush:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"unregisterPush"]) {
        [wrapper unregisterPush:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendCustomMessage"]) {
        [wrapper sendCustomMessage:call.arguments result:result];
    }
    
    // Call
    else if ([call.method isEqualToString:@"makeCall"]) {
        [wrapper.callManager makeCall:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"initAnswer"]) {
        [wrapper.callManager initAnswer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"answer"]) {
        [wrapper.callManager answer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"hangup"]) {
        [wrapper.callManager hangup:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"reject"]) {
        [wrapper.callManager reject:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendDtmf"]) {
        [wrapper.callManager sendDtmf:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"sendCallInfo"]) {
        [wrapper.callManager sendCallInfo:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getCallStats"]) {
        [wrapper.callManager getCallStats:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"mute"]) {
        [wrapper.callManager mute:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setSpeakerphoneOn"]) {
        [wrapper.callManager setSpeakerphoneOn:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"switchCamera"]) {
        [wrapper.callManager switchCamera:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"enableVideo"]) {
        [wrapper.callManager enableVideo:call.arguments result:result];
    }
    
    // Call2
    else if ([call.method isEqualToString:@"makeCall2"]) {
        [wrapper.call2Manager makeCall:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"initAnswer2"]) {
        [wrapper.call2Manager initAnswer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"answer2"]) {
        [wrapper.call2Manager answer:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"hangup2"]) {
        [wrapper.call2Manager hangup:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"reject2"]) {
        [wrapper.call2Manager reject:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"mute2"]) {
        [wrapper.call2Manager mute:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setSpeakerphoneOn2"]) {
        [wrapper.call2Manager setSpeakerphoneOn:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"switchCamera2"]) {
        [wrapper.call2Manager switchCamera:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"enableVideo2"]) {
        [wrapper.call2Manager enableVideo:call.arguments result:result];
    }
    
    // Conversation
    else if ([call.method isEqualToString:@"createConversation"]) {
        [wrapper.convManager createConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationById"]) {
        [wrapper.convManager getConversationById:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationByUserId"]) {
        [wrapper.convManager getConversationByUserId:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLocalConversations"]) {
        [wrapper.convManager getLocalConversations:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLastConversation"]) {
        [wrapper.convManager getLastConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationsBefore"]) {
        [wrapper.convManager getConversationsBefore:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getConversationsAfter"]) {
        [wrapper.convManager getConversationsAfter:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"clearDb"]) {
        [wrapper.convManager clearDb:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getTotalUnread"]) {
        [wrapper.convManager getTotalUnread:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"delete"]) {
        [wrapper.convManager deleteConv:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"addParticipants"]) {
        [wrapper.convManager addParticipants:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"removeParticipants"]) {
        [wrapper.convManager removeParticipants:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"updateConversation"]) {
        [wrapper.convManager updateConversation:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"setRole"]) {
        [wrapper.convManager setRole:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"markAsRead"]) {
        [wrapper.convManager markAsRead:call.arguments result:result];
    }
    
    // Message
    else if ([call.method isEqualToString:@"sendMessage"]) {
        [wrapper.msgManager sendMessage:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessages"]) {
        [wrapper.msgManager getMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLocalMessages"]) {
        [wrapper.msgManager getLocalMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getLastMessages"]) {
        [wrapper.msgManager getLastMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessagesAfter"]) {
        [wrapper.msgManager getMessagesAfter:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"getMessagesBefore"]) {
        [wrapper.msgManager getMessagesBefore:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"deleteMessages"]) {
        [wrapper.msgManager deleteMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"revokeMessages"]) {
        [wrapper.msgManager revokeMessages:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"editMsg"]) {
        [wrapper.msgManager editMsg:call.arguments result:result];
    }
    else if ([call.method isEqualToString:@"pinOrUnPin"]) {
        [wrapper.msgManager pinOrUnPin:call.arguments result:result];
    }
    
    else {
        result(FlutterMethodNotImplemented);
    }
}

- (FlutterError *)onListenWithArguments:(id)arguments eventSink:(FlutterEventSink)events {
    _eventSink = events;
//    [_callManager setEventSink:_eventSink];
//    [_call2Manager setEventSink:_eventSink];
//    [_convManager setEventSink:_eventSink];
//    [_msgManager setEventSink:_eventSink];
    
    [StringeeClientWrapper setEventSinkForAllInstances:events];
    return nil;
}

- (FlutterError *)onCancelWithArguments:(id)arguments {
    _eventSink = nil;
//    [_callManager setEventSink:_eventSink];
//    [_call2Manager setEventSink:_eventSink];
//    [_convManager setEventSink:_eventSink];
//    [_msgManager setEventSink:_eventSink];
    
    [StringeeClientWrapper setEventSinkForAllInstances:nil];
    return nil;
}

#pragma mark - Client Actions

- (void)setupClient:(id)arguments result:(FlutterResult)result {
    NSDictionary *data = (NSDictionary *)arguments;
    
    if (![data isKindOfClass:[NSDictionary class]]) {
        result(nil);
        return;
    }
    
    NSString *uuid = [data objectForKey:@"uuid"];
    NSString *baseAPIUrl = [data objectForKey:@"baseAPIUrl"];
    
    StringeeClientWrapper *wrapper = [StringeeClientWrapper getByUuid:uuid];
    if (wrapper == nil) {
        wrapper = [[StringeeClientWrapper alloc] initWithIdentifier:uuid eventSink:_eventSink];
    }
    wrapper.baseAPIUrl = baseAPIUrl;
    
    result(nil);
}

//- (void)connect:(id)arguments result:(FlutterResult)result {
//    NSDictionary *data = (NSDictionary *)arguments;
//    
//    if (![data isKindOfClass:[NSDictionary class]]) {
//        result(nil);
//        return;
//    }
//    
//    if (isConnecting) {
//        result(nil);
//        return;
//    }
//    isConnecting = YES;
//    
//    NSString *token = [data objectForKey:@"token"];
//    NSString *uuid = [data objectForKey:@"uuid"];
//    
//    StringeeClientWrapper *wrapper = [StringeeClientWrapper getByUuid:uuid];
//    if (wrapper == nil) {
//        NSLog(@"Wrapper is not found");
//        return;
//    }
//
//    if (!_client) {
//        id strServerAddressesData = [data objectForKey:@"serverAddresses"];
//        if (strServerAddressesData != nil && strServerAddressesData != [NSNull null]) {
//            NSArray *serverAddressesData = [StringeeHelper StringToArray:strServerAddressesData];
//            NSArray *serverAddresses = [StringeeHelper parseServerAddressesWithData:serverAddressesData];
//            if (serverAddresses.count > 0) {
//                _client = [[StringeeClient alloc] initWithConnectionDelegate:self serverAddress:serverAddresses];
//            } else {
//                _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
//            }
//        } else {
//            _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
//        }
//        _client.incomingCallDelegate = self;
//        
//        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleObjectChangeNotification:) name:StringeeClientObjectsDidChangeNotification object:_client];
//        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNewMessageNotification:) name:StringeeClientNewMessageNotification object:_client];
//        
//        [_callManager setClient:_client];
//        [_call2Manager setClient:_client];
//        [_convManager setClient:_client];
//        [_msgManager setClient:_client];
//    }
//    
//    [_client connectWithAccessToken:token];
//    result(nil);
//}
//
//- (void)disconnect:(id)arguments result:(FlutterResult)result {
//    if (_client) {
//        [_client disconnect];
//    }
//    isConnecting = NO;
//    result(nil);
//}
//
//- (void)registerPush:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//    
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *deviceToken = data[@"deviceToken"];
//    BOOL isProduction = [data[@"isProduction"] boolValue];
//    BOOL isVoip = [data[@"isVoip"] boolValue];
//    
//    if (!deviceToken || [deviceToken isKindOfClass:[NSNull class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Info is invalid."});
//    }
//    
//    [_client registerPushForDeviceToken:deviceToken isProduction:isProduction isVoip:isVoip completionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)unregisterPush:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//    
//    NSString *deviceToken = (NSString *)arguments;
//    
//    if (!deviceToken || [deviceToken isKindOfClass:[NSNull class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Info is invalid."});
//    }
//    
//    [_client unregisterPushForDeviceToken:deviceToken completionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}
//
//- (void)sendCustomMessage:(id)arguments result:(FlutterResult)result {
//    if (!_client || !_client.hasConnected) {
//        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
//        return;
//    }
//    
//    NSDictionary *data = (NSDictionary *)arguments;
//    NSString *userId = data[@"toUserId"];
//    NSDictionary *message = data[@"message"];
//    
//    if (!userId || [userId isKindOfClass:[NSNull class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"UserId is invalid."});
//    }
//    
//    if (!message || [message isKindOfClass:[NSNull class]] || ![message isKindOfClass:[NSDictionary class]]) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Message is invalid."});
//    }
//    
//    [_client sendCustomMessage:message toUserId:userId completionHandler:^(BOOL status, int code, NSString *message) {
//        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
//    }];
//}

//#pragma mark - Chat Event
//
//- (void)handleObjectChangeNotification:(NSNotification *)notification {
//    NSArray *objectChanges = [notification.userInfo objectForKey:StringeeClientObjectChangesUserInfoKey];
//    if (!objectChanges.count) {
//        return;
//    }
//
//    NSMutableArray *objects = [[NSMutableArray alloc] init];
//
//    for (StringeeObjectChange *objectChange in objectChanges) {
//        [objects addObject:objectChange.object];
//    }
//
//    StringeeObjectChange *firstObjectChange = [objectChanges firstObject];
//    id firstObject = [objects firstObject];
//
//    int objectType;
//    NSArray *jsObjectDatas;
//    if ([firstObject isKindOfClass:[StringeeConversation class]]) {
//        objectType = 0;
//        jsObjectDatas = [StringeeHelper Conversations:objects];
//    } else if ([firstObject isKindOfClass:[StringeeMessage class]]) {
//        objectType = 1;
//        jsObjectDatas = [StringeeHelper Messages:objects];
//
//        // Xoá đối tượng message đã lưu
//        for (NSDictionary *message in jsObjectDatas) {
//            NSNumber *state = message[@"state"];
//            if (state.intValue == StringeeMessageStatusRead) {
//                NSString *localId = message[@"localId"];
//                if (localId) {
//                    [_msgManager.trackedMessages removeObjectForKey:localId];
//                }
//            }
//        }
//    } else {
//        objectType = 2;
//    }
//
//    id returnObjects = jsObjectDatas ? jsObjectDatas : [NSNull null];
//
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(objectType), @"objects" : returnObjects, @"changeType" : @(firstObjectChange.type) }});
//}
//
//- (void)handleNewMessageNotification:(NSNotification *)notification {
//    NSDictionary *userInfo = [notification userInfo];
//    if (!userInfo) return;
//
//    NSString *convId = [userInfo objectForKey:StringeeClientNewMessageConversationIDKey];
//    if (convId == nil || convId.length == 0) {
//        return;
//    }
//
//    // Lấy về conversation
//    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
//        if (!conversation) {
//            return;
//        }
//
//        self->_eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(0), @"objects" : @[[StringeeHelper Conversation:conversation]], @"changeType" : @(StringeeObjectChangeTypeUpdate) }});
//    }];
//}

//#pragma mark - Client Delegate
//
//- (void)didConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
//}
//
//- (void)didDisConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidDisConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
//}
//
//- (void)didFailWithError:(StringeeClient *)stringeeClient code:(int)code message:(NSString *)message {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidFailWithError, STEBody : @{ @"userId" : stringeeClient.userId, @"code" : @(code), @"message" : message }});
//}
//
//- (void)requestAccessToken:(StringeeClient *)stringeeClient {
//    isConnecting = NO;
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STERequestAccessToken, STEBody : @{ @"userId" : stringeeClient.userId }});
//}
//
//- (void)didReceiveCustomMessage:(StringeeClient *)stringeeClient message:(NSDictionary *)message fromUserId:(NSString *)userId {
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveCustomMessage, STEBody : @{ @"fromUserId" : userId, @"message" : message }});
//}
//
//- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall:(StringeeCall *)stringeeCall {
//    stringeeCall.delegate = _callManager;
//    [[StringeeManager instance].calls setObject:stringeeCall forKey:stringeeCall.callId];
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall, STEBody : [StringeeHelper StringeeCall:stringeeCall] });
//}
//
//- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall2:(StringeeCall2 *)stringeeCall2 {
//    stringeeCall2.delegate = _call2Manager;
//    [[StringeeManager instance].call2s setObject:stringeeCall2 forKey:stringeeCall2.callId];
//    _eventSink(@{STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall2, STEBody : [StringeeHelper StringeeCall2:stringeeCall2] });
//}


@end
