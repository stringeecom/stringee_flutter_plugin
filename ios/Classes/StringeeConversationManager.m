//
//  StringeeConversationManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import "StringeeConversationManager.h"
#import "StringeeFlutterPlugin.h"
#import "StringeeManager.h"
#import "StringeeHelper.h"

@implementation StringeeConversationManager {
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

- (void)createConversation:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSDictionary *optionData = [StringeeHelper StringToDictionary:[data objectForKey:@"option"]];
    NSArray *partsData = [StringeeHelper StringToArray:[data objectForKey:@"participants"]];
    if (optionData == nil || partsData == nil) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Option or Participants is invalid"});
        return;
    }

    StringeeConversationOption *option = [StringeeHelper parseOptionWithData:optionData];
    NSSet<StringeeIdentity *> *parts = [StringeeHelper parsePartsWithData:partsData];
    NSString *name = [optionData objectForKey:@"name"];

    [_client createConversationWithName:name participants:parts options:option completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversation:conversation]});
    }];
}

- (void)getConversationById:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = data[@"convId"];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"ConvId is invalid."});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversation:conversation]});
    }];
}

- (void)getConversationByUserId:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *userId = data[@"userId"];

    if (userId == nil || userId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"UserId is invalid."});
        return;
    }

    NSMutableSet *users = [[NSMutableSet alloc] init];
    StringeeIdentity *iden = [StringeeIdentity new];
    iden.userId = userId;
    [users addObject:iden];

    StringeeIdentity *meUser = [StringeeIdentity new];
    meUser.userId = _client.userId;
    [users addObject:meUser];

    [_client getConversationForUsers:users completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        if (!conversations || conversations.count == 0) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        for (StringeeConversation *conversation in conversations) {
            if (conversation.isGroup == false) {
                result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversation:conversation]});
                return;
            }
        }
    }];
}

- (void)getLocalConversations:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *oaId = data[@"oaId"];
    if (oaId != nil || oaId.length > 0) {
        [_client getLocalConversationsWithCount:500 userId:_client.userId oaId:oaId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    } else {
        [_client getLocalConversationsWithCount:500 userId:_client.userId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    }
}

- (void)getLastConversation:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    int count = [[data objectForKey:@"count"] intValue];
    NSString *oaId = data[@"oaId"];

    if (oaId != nil || oaId.length > 0) {
        [_client getLastConversationsWithCount:count oaId:oaId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    } else {
        [_client getLastConversationsWithCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    }

}

- (void)getConversationsBefore:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    int count = [[data objectForKey:@"count"] intValue];
    long long datetime = [[data objectForKey:@"datetime"] longLongValue];
    NSString *oaId = data[@"oaId"];

    if (oaId != nil || oaId.length > 0) {
        [_client getConversationsBefore:datetime withCount:count oaId:oaId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    } else {
        [_client getConversationsBefore:datetime withCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    }
}

- (void)getConversationsAfter:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    int count = [[data objectForKey:@"count"] intValue];
    long long datetime = [[data objectForKey:@"datetime"] longLongValue];
    NSString *oaId = data[@"oaId"];

    if (oaId != nil || oaId.length > 0) {
        [_client getConversationsAfter:datetime withCount:count oaId:oaId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    } else {
        [_client getConversationsAfter:datetime withCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Conversations:conversations]});
        }];
    }
}

- (void)joinOAConversation:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Conversation's id is invalid"});
        return;
    }

    [_client joinOAConversationWithConvId:convId completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)clearDb:(id)arguments result:(FlutterResult)result {
    if (!_client) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied"});
        return;
    }

    [_client clearData];
}

- (void)getTotalUnread:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    [_client getUnreadConversationCountWithCompletionHandler:^(BOOL status, int code, NSString *message, int count) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: @(count)});
    }];
}

- (void)deleteConv:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Conversation's id is invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation deleteWithCompletionHandler:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
        }];
    }];
}

- (void)addParticipants:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSArray *partDatas = [StringeeHelper StringToArray:[data objectForKey:@"participants"]];
    NSSet<StringeeIdentity *> *parts = [StringeeHelper parsePartsWithData:partDatas];

    if (convId == nil || convId.length == 0 || parts == nil || parts.count == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation addParticipants:parts completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeIdentity *> *addedUsers) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Identities:addedUsers]});
        }];
    }];
}

- (void)removeParticipants:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSArray *partDatas = [StringeeHelper StringToArray:[data objectForKey:@"participants"]];
    NSSet<StringeeIdentity *> *parts = [StringeeHelper parsePartsWithData:partDatas];

    if (convId == nil || convId.length == 0 || parts == nil || parts.count == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation removeParticipants:parts completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeIdentity *> *removedUsers) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Identities:removedUsers]});
        }];
    }];
}

- (void)setRole:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSString *userId = [data objectForKey:@"userId"];
    int intRole = [[data objectForKey:@"role"] intValue];
    StringeeRole role = intRole == 0 ? StringeeRoleAdmin : StringeeRoleMember;

    if (convId == nil || convId.length == 0 || userId == nil || userId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        StringeeIdentity *part = [[StringeeIdentity alloc] init];
        part.userId = userId;

        [conversation setRole:role forPart:part completion:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
        }];
    }];
}

- (void)updateConversation:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSString *name = [data objectForKey:@"name"];

    if (convId == nil || convId.length == 0 || name == nil || name.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation updateWithName:name strAvatarUrl:@"" completionHandler:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
        }];
    }];
}

- (void)markAsRead:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }

    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }

        [conversation markAllMessagesAsSeenWithCompletionHandler:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
        }];
    }];
}

@end

