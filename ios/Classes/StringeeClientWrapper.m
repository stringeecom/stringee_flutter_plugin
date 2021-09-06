//
//  StringeeClientWrapper.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 6/19/21.
//

#import "StringeeClientWrapper.h"
#import "StringeeHelper.h"
#import "StringeeManager.h"

static NSMutableDictionary<NSString *, StringeeClientWrapper *> *clients;

@implementation StringeeClientWrapper {
    FlutterEventSink _eventSink;
    BOOL isConnecting;
    BOOL _firstConnectTime;
}

+ (void)initialize {
    if (clients == nil) {
        clients = [[NSMutableDictionary alloc] init];
    }
}

- (instancetype)initWithIdentifier:(NSString *)identifier eventSink:(FlutterEventSink)eventSink
{
    self = [super init];
    if (self) {
        [StringeeClientWrapper initialize];
        self.identifier = identifier;
        [clients setValue:self forKey:identifier];
        
        _callManager = [[StringeeCallManager alloc] initWithIdentifier:identifier];
        _call2Manager = [[StringeeCall2Manager alloc] initWithIdentifier:identifier];
        _convManager = [[StringeeConversationManager alloc] initWithIdentifier:identifier];
        _msgManager = [[StringeeMessageManager alloc] initWithIdentifier:identifier];
        _chatManager = [[StringeeChatManager alloc] initWithIdentifier:identifier];

        _eventSink = eventSink;
        [_callManager setEventSink:_eventSink];
        [_call2Manager setEventSink:_eventSink];
        [_convManager setEventSink:_eventSink];
        [_msgManager setEventSink:_eventSink];
        [_chatManager setEventSink:_eventSink];
        
        // Fix cho phan live-chat
        _firstConnectTime = true;
        _client = [[StringeeClient alloc] init];
        [_chatManager setClient:_client];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setEventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
    [_callManager setEventSink:_eventSink];
    [_call2Manager setEventSink:_eventSink];
    [_convManager setEventSink:_eventSink];
    [_msgManager setEventSink:_eventSink];
    [_chatManager setEventSink:_eventSink];
}

+ (void)setEventSinkForAllInstances:(FlutterEventSink)eventSink {
    for (StringeeClientWrapper *wrapper in clients.allValues) {
        [wrapper setEventSink:eventSink];
    }
}

- (void)setBaseAPIUrl:(NSString *)baseAPIUrl {
    // Set base url o day

}

+ (instancetype)getByUuid:(NSString *)identifier {
    if (identifier == nil || identifier.length == 0) {
        return nil;
    }
    
    return [clients objectForKey: identifier];
}

#pragma mark - Flutter Method

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
    
    if (!_client || _firstConnectTime) {
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
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleUserTypingNotification:) name:StringeeChatUserTypingNotification object:_client];

        [_callManager setClient:_client];
        [_call2Manager setClient:_client];
        [_convManager setClient:_client];
        [_msgManager setClient:_client];
        [_chatManager setClient:_client];
        
        _firstConnectTime = false;
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
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *deviceToken = data[@"deviceToken"];
        
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

#pragma mark - Client Delegate

- (void)didConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
}

- (void)didDisConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidDisConnect, STEBody : @{@"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting)}});
}

- (void)didFailWithError:(StringeeClient *)stringeeClient code:(int)code message:(NSString *)message {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidFailWithError, STEBody : @{ @"userId" : stringeeClient.userId, @"code" : @(code), @"message" : message }});
}

- (void)requestAccessToken:(StringeeClient *)stringeeClient {
    isConnecting = NO;
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STERequestAccessToken, STEBody : @{ @"userId" : stringeeClient.userId }});
}

- (void)didReceiveCustomMessage:(StringeeClient *)stringeeClient message:(NSDictionary *)message fromUserId:(NSString *)userId {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveCustomMessage, STEBody : @{ @"fromUserId" : userId, @"message" : message }});
}

- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall:(StringeeCall *)stringeeCall {
    stringeeCall.delegate = _callManager;
    [[StringeeManager instance].calls setObject:stringeeCall forKey:stringeeCall.callId];
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall, STEBody : [StringeeHelper StringeeCall:stringeeCall] });
}

- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall2:(StringeeCall2 *)stringeeCall2 {
    stringeeCall2.delegate = _call2Manager;
    [[StringeeManager instance].call2s setObject:stringeeCall2 forKey:stringeeCall2.callId];
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEIncomingCall2, STEBody : [StringeeHelper StringeeCall2:stringeeCall2] });
}

- (void)didReceiveChatRequest:(StringeeClient *)stringeeClient request:(StringeeChatRequest *)request {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveChatRequest, STEBody : [StringeeHelper StringeeChatRequest:request] });
}

- (void)didReceiveTransferChatRequest:(StringeeClient *)stringeeClient request:(StringeeChatRequest *)request {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEDidReceiveTransferChatRequest, STEBody : [StringeeHelper StringeeChatRequest:request] });
}

- (void)timeoutAnswerChat:(StringeeClient *)stringeeClient request:(StringeeChatRequest *)request {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STETimeoutAnswerChat, STEBody : [StringeeHelper StringeeChatRequest:request] });
}

- (void)timeoutInQueue:(StringeeClient *)stringeeClient info:(NSDictionary *)info {
    NSLog(@"timeoutInQueue %@", info);
    id rInfo = info != nil ? info : [NSNull null];
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STETimeoutInQueue, STEBody : rInfo });
}

- (void)conversationEnded:(StringeeClient *)stringeeClient info:(NSDictionary *)info {
    id rInfo = info != nil ? info : [NSNull null];
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEConversationEnded, STEBody : rInfo });
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

    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeChat), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(objectType), @"objects" : returnObjects, @"changeType" : @(firstObjectChange.type) }});
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
        
        self->_eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeChat), STEEvent : STEDidReceiveChangeEvent, STEBody : @{ @"objectType" : @(0), @"objects" : @[[StringeeHelper Conversation:conversation]], @"changeType" : @(StringeeObjectChangeTypeUpdate) }});
    }];
}

- (void)handleUserTypingNotification:(NSNotification *)notification {
    NSDictionary *userInfo = [notification userInfo];
    if (!userInfo) return;
    
    NSString *convId = [userInfo objectForKey:@"convId"] != nil ? [userInfo objectForKey:@"convId"] : @"";
    NSString *userId = [userInfo objectForKey:@"userId"] != nil ? [userInfo objectForKey:@"userId"] : @"";
    NSString *displayName = [userInfo objectForKey:@"displayName"] != nil ? [userInfo objectForKey:@"displayName"] : @"";
    BOOL begin = [[userInfo objectForKey:@"begin"] boolValue];

    NSDictionary *infos = @{
                            @"convId" : convId,
                            @"userId" : userId,
                            @"displayName" : displayName
                            };
    if (begin) {
        // begin
        _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEUserBeginTyping, STEBody : infos });
    } else {
        // end
        _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeClient), STEEvent : STEUserEndTyping, STEBody : infos });
    }
}

@end
