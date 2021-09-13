//
//  StringeeMessageManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import "StringeeMessageManager.h"
#import "StringeeFlutterPlugin.h"
#import "StringeeManager.h"
#import "StringeeHelper.h"

@implementation StringeeMessageManager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
    NSString *_identifier;
}

- (instancetype)initWithIdentifier:(NSString *)identifier
{
    self = [super init];
    if (self) {
        _identifier = identifier;
        self.trackedMessages = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.trackedMessages = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)setClient:(StringeeClient *)client {
    _client = client;
}

- (void)setEventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
}

- (void)sendMessage:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected.", STEBody: [NSNull null]});
        return;
    }
    
//    NSLog(@"==== sendMessage: %@", arguments);
//    NSDictionary *data = [StringeeHelper StringToDictionary:arguments];
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSDictionary *customData = [data objectForKey:@"customData"];
    int type = [[data objectForKey:@"type"] intValue];
    
    if (convId == nil || convId.length == 0 || data == nil) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
        return;
    }
    
    __weak StringeeMessageManager *weakSelf = self;
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        StringeeMessageManager *strongSelf = weakSelf;
        
        if (!conversation || strongSelf == nil) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Conversation is not found", STEBody: [NSNull null]});
            return;
        }
        
        StringeeMessage *msgToSend;
        switch (type) {
            case StringeeMessageTypeText:
            {
                NSString *text = data[@"text"];
                if (![text isKindOfClass:[NSString class]] || !text.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeTextMessage alloc] initWithText:text metadata:customData];
            }
                break;
                
            case StringeeMessageTypePhoto:
            {
                NSString *filePath = data[@"filePath"];
                NSString *thumbnail = data[@"thumbnail"] != nil ? data[@"thumbnail"] : @"";
                NSNumber *ratio = data[@"ratio"] != nil ? data[@"ratio"] : @(1);
                
                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                
                msgToSend = [[StringeePhotoMessage alloc] initWithFileUrl:filePath thumbnailUrl:thumbnail ratio:ratio.floatValue metadata:customData];
            }
                break;
                
            case StringeeMessageTypeVideo:
            {
                NSString *filePath = data[@"filePath"];
                NSString *thumbnail = data[@"thumbnail"] != nil ? data[@"thumbnail"] : @"";
                NSNumber *ratio = data[@"ratio"] != nil ? data[@"ratio"] : @(1);
                NSNumber *duration = data[@"duration"] != nil ? data[@"duration"] : @(0);
                
                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeVideoMessage alloc] initWithFileUrl:filePath thumbnailUrl:thumbnail ratio:ratio.floatValue duration:duration.doubleValue metadata:customData];
            }
                break;
                
            case StringeeMessageTypeAudio:
            {
                NSString *filePath = data[@"filePath"];
                NSNumber *duration = data[@"duration"] != nil ? data[@"duration"] : @(0);
                
                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeAudioMessage alloc] initWithFileUrl:filePath duration:duration.doubleValue metadata:customData];
            }
                break;
                
            case StringeeMessageTypeFile:
            {
                NSString *filePath = data[@"filePath"];
                NSString *filename = data[@"filename"] != nil ? data[@"filename"] : @"";
                NSNumber *length = data[@"length"] != nil ? data[@"length"] : @(0);
                
                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeFileMessage alloc] initWithFileUrl:filePath fileName:filename length:length.longLongValue metadata:customData];
            }
                break;
                
            case StringeeMessageTypeLink:
            {
                NSString *text = data[@"text"];
                if (![text isKindOfClass:[NSString class]] || !text.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeTextMessage alloc] initWithLink:text metadata:customData];
            }
                break;
                
            case StringeeMessageTypeLocation:
            {
                NSNumber *lat = data[@"lat"];
                NSNumber *lon = data[@"lon"];
                
                if (!lat || !lon) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeLocationMessage alloc] initWithlatitude:lat.doubleValue longitude:lon.doubleValue metadata:customData];
            }
                break;
                
            case StringeeMessageTypeContact:
            {
                NSString *vcard = data[@"vcard"];
                if (![vcard isKindOfClass:[NSString class]] || !vcard.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeContactMessage alloc] initWithVcard:vcard metadata:customData];
            }
                break;
                
            case StringeeMessageTypeSticker:
            {
                NSString *category = data[@"stickerCategory"];
                NSString *name = data[@"stickerName"];
                
                if (![category isKindOfClass:[NSString class]] || !category.length || ![name isKindOfClass:[NSString class]] || !name.length) {
                    result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                    return;
                }
                msgToSend = [[StringeeStickerMessage alloc] initWithCategory:category name:name metadata:customData];
            }
                break;
                
            default:
                result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid", STEBody: [NSNull null]});
                return;
        }
        
        // Luu lại các tin nhắn gửi đi đến khi trạng thái được cập nhật read
        [strongSelf.trackedMessages setObject:msgToSend forKey:msgToSend.localIdentifier];
        
        NSError *error;
        [conversation sendMessageWithoutPretreatment:msgToSend error:&error];
        if (error) {
            result(@{STEStatus : @(false), STECode : @(1), STEMessage: @"Fail", STEBody: [NSNull null]});
        } else {
            result(@{STEStatus : @(true), STECode : @(0), STEMessage: @"Success", STEBody: [NSNull null]});
        }
    }];
}

- (void)getMessages:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== getMessages: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSArray *msgIds = [data objectForKey:@"msgIds"];

    if (convId == nil || convId.length == 0 || msgIds == nil || msgIds.count == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getMessageWithIds:msgIds completion:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *msgs) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Messages:msgs]});
        }];
    }];
}

- (void)getLocalMessages:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== getLocalMessages: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    int count = [[data objectForKey:@"count"] intValue];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getLocalMessagesWithCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Messages:messages]});
        }];
    }];
}

- (void)getLastMessages:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== getLastMessages: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    int count = [[data objectForKey:@"count"] intValue];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getLastMessagesWithCount:count loadDeletedMessage:false loadDeletedMessageContent:false completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Messages:messages]});
        }];
    }];
}

- (void)getMessagesAfter:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== getMessagesAfter: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    int count = [[data objectForKey:@"count"] intValue];
    int seq = [[data objectForKey:@"seq"] intValue];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getMessagesAfter:seq withCount:count loadDeletedMessage:false loadDeletedMessageContent:false completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Messages:messages]});
        }];
    }];
}

- (void)getMessagesBefore:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== getMessagesBefore: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    int count = [[data objectForKey:@"count"] intValue];
    int seq = [[data objectForKey:@"seq"] intValue];

    if (convId == nil || convId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getMessagesBefore:seq withCount:count loadDeletedMessage:false loadDeletedMessageContent:false completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper Messages:messages]});
        }];
    }];
}

- (void)deleteMessages:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== deleteMessages: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSArray *msgIds = [data objectForKey:@"msgIds"];

    if (convId == nil || convId.length == 0 || msgIds == nil || msgIds.count == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Object is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation deleteMessageWithMessageIds:msgIds withCompletionHandler:^(BOOL status, int code, NSString *message) {
            result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
        }];
    }];
}

- (void)revokeMessages:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== revokeMessages: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSArray *msgIds = [data objectForKey:@"msgIds"];
    BOOL isDeleted = [[data objectForKey:@"isDeleted"] boolValue];

    if (convId == nil || convId.length == 0 || msgIds == nil || msgIds.count == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Conversation is not found", STEBody: [NSNull null]});
            return;
        }
        
        dispatch_group_t group = dispatch_group_create();
        __block BOOL returnStatus = true;
        __block int returnCode = 0;
        __block NSString *returnMessage = @"Success";
        
        for (NSString *msgId in msgIds) {
            if (msgId.length == 0) {
                continue;
            }

            [conversation getMessageWithId:msgId completion:^(BOOL status, int code, NSString *message, StringeeMessage *msg) {
                if (msg == nil) {
                    return;
                }
                
                dispatch_group_async(group,dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^ {
                    [conversation revokeMessage:msg deleted:isDeleted completion:^(BOOL status, int code, NSString *message) {
                        if (!status) {
                            returnStatus = status;
                            returnMessage = message;
                            returnCode = code;
                        }
                    }];
                });
            }];
        }
        
        dispatch_group_notify(group,dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^ {
            result(@{STEStatus : @(returnStatus), STECode : @(returnCode), STEMessage: returnMessage, STEBody: [NSNull null]});
        });
    }];
}

- (void)editMsg:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSString *msgId = [data objectForKey:@"msgId"];
    NSString *content = [data objectForKey:@"content"];

    if (convId == nil || convId.length == 0 || msgId == nil || msgId.length == 0 || content == nil || content.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Conversation is not found", STEBody: [NSNull null]});
            return;
        }
        
        [conversation getMessageWithId:msgId completion:^(BOOL status, int code, NSString *message, StringeeMessage *msg) {
            if (!msg) {
                result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Message is not found", STEBody: [NSNull null]});
                return;
            }

            [conversation editMessage:msg newContent:content completion:^(BOOL status, int code, NSString *message) {
                result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
            }];
        }];
    }];
}

- (void)pinOrUnPin:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
//    NSLog(@"==== pinOrUnPin: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSString *convId = [data objectForKey:@"convId"];
    NSString *msgId = [data objectForKey:@"msgId"];
    BOOL pinOrUnPin = [[data objectForKey:@"pinOrUnPin"] boolValue];

    if (convId == nil || convId.length == 0 || msgId == nil || msgId.length == 0) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameters are invalid"});
        return;
    }
        
    [_client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Conversation is not found", STEBody: [NSNull null]});
            return;
        }
                
        [conversation getMessageWithId:msgId completion:^(BOOL status, int code, NSString *message, StringeeMessage *msg) {
            if (!msg) {
                result(@{STEStatus : @(false), STECode : @(-3), STEMessage: @"Message is not found", STEBody: [NSNull null]});
                return;
            }
            
            [conversation pinMessage:msg isPin:pinOrUnPin completion:^(BOOL status, int code, NSString *message) {
                result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [NSNull null]});
            }];
        }];
    }];
}

@end
