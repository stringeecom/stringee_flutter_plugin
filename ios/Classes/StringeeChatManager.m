//
//  StringeeChatManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 9/1/21.
//

#import "StringeeChatManager.h"
#import "StringeeHelper.h"

@implementation StringeeChatManager {
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

- (void)getChatProfile:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== getChatProfile: %@", data);
    NSString *key = [data objectForKey:@"key"];

    if (key == nil || key.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    [_client getChatProfileWithKey:key completion:^(BOOL status, int code, NSString *message, StringeeChatProfile *chatProfile) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper ChatProfile:chatProfile]});
    }];
}

- (void)getLiveChatToken:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== getLiveChatToken: %@", data);
    NSString *key = [data objectForKey:@"key"];
    NSString *name = [data objectForKey:@"name"];
    NSString *email = [data objectForKey:@"email"];

    if (key == nil || key.length == 0 || name == nil || name.length == 0 || email == nil || email.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    [_client generateTokenForCustomerWithKey:key username:name email:email completion:^(BOOL status, int code, NSString *message, NSString *token) {
        id rToken = token != nil ? token : [NSNull null];
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: rToken});
    }];
}

- (void)updateUserInfo:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== updateUserInfo: %@", data);
    NSString *name = [data objectForKey:@"name"] != nil ? [data objectForKey:@"name"] : @"";
    NSString *email = [data objectForKey:@"email"] != nil ? [data objectForKey:@"email"] : @"";
    NSString *avatar = [data objectForKey:@"avatar"] != nil ? [data objectForKey:@"avatar"] : @"";
    NSString *phone = [data objectForKey:@"phone"] != nil ? [data objectForKey:@"phone"] : @"";

    [_client updateUserInfoWithUsername:name email:email avatar:avatar phone:phone completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)createLiveChatConversation:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== createLiveChatConversation: %@", data);
    NSString *queueId = [data objectForKey:@"queueId"];

    if (queueId == nil || queueId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    [_client createLiveChatConversationWithQueueId:queueId completion:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversation:conversation]});
    }];
}

- (void)createLiveChatTicket:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== createLiveChatTicket: %@", data);
    NSString *key = [data objectForKey:@"key"] != nil ? [data objectForKey:@"key"] : @"";
    NSString *name = [data objectForKey:@"name"] != nil ? [data objectForKey:@"name"] : @"";
    NSString *email = [data objectForKey:@"email"] != nil ? [data objectForKey:@"email"] : @"";
    NSString *description = [data objectForKey:@"description"] != nil ? [data objectForKey:@"description"] : @"";

    if (key == nil || key.length == 0 || name == nil || name.length == 0 || email == nil || email.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    [_client createTicketForMissChatWithKey:key username:name email:email note:description completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)sendChatTranscript:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== sendChatTranscript: %@", data);
    NSString *convId = [data objectForKey:@"convId"];
    NSString *domain = [data objectForKey:@"domain"];
    NSString *email = [data objectForKey:@"email"];

    [_client sendChatTranscriptTo:email convId:convId domain:domain completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)endChat:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== endChat: %@", data);
    NSString *convId = [data objectForKey:@"convId"];

    [_client endChatSupportWithConvId:convId completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)beginTyping:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== beginTyping: %@", data);
    NSString *convId = [data objectForKey:@"convId"];

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation beginTypingWithCompletion:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
        }];
    }];
}

- (void)endTyping:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== endTyping: %@", data);
    NSString *convId = [data objectForKey:@"convId"];

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation endTypingWithCompletion:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
        }];
    }];
}

- (void)acceptChatRequest:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== acceptChatRequest: %@", data);
    NSString *convId = [data objectForKey:@"convId"];

    StringeeChatRequest *request = [_client getChatRequestWithConvId:convId];
    if (request == nil) {
        result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
        return;
    }

    [request acceptWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)rejectChatRequest:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

//    NSLog(@"==== rejectChatRequest: %@", data);
    NSString *convId = [data objectForKey:@"convId"];

    StringeeChatRequest *request = [_client getChatRequestWithConvId:convId];
    if (request == nil) {
        result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
        return;
    }

    [request rejectWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}


@end

