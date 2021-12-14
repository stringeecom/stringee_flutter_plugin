//
//  Utils.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import "StringeeHelper.h"

@implementation StringeeHelper

+ (id)StringeeCall:(StringeeCall *)call {
    if (!call) {
        return [NSNull null];
    }
    
    id callId = call.callId ? call.callId :[NSNull null];
    id from = call.from ? call.from : [NSNull null];
    id to = call.to ? call.to : [NSNull null];
    id fromAlias = call.fromAlias ? call.fromAlias : [NSNull null];
    id toAlias = call.toAlias ? call.toAlias : [NSNull null];
    id customDataFromYourServer = call.customDataFromYourServer ? call.customDataFromYourServer : [NSNull null];
    
    int calltype = 0;
    if (call.callType == CallTypeCallIn) {
        // Phone-to-app
        calltype = 3;
    } else if (call.callType == CallTypeCallOut) {
        // App-to-phone
        calltype = 2;
    } else if (call.callType == CallTypeInternalIncomingCall) {
        // App-to-app-incoming-call
        calltype = 1;
    } else {
        // App-to-app-outgoing-call
        calltype = 0;
    }
    
    NSMutableDictionary *data = [NSMutableDictionary new];
    [data setObject:callId forKey:@"callId"];
    [data setObject:@(call.serial) forKey:@"serial"];
    [data setObject:from forKey:@"from"];
    [data setObject:to forKey:@"to"];
    [data setObject:fromAlias forKey:@"fromAlias"];
    [data setObject:toAlias forKey:@"toAlias"];
    [data setObject:@(calltype) forKey:@"callType"];
    [data setObject:@(call.isVideoCall) forKey:@"isVideoCall"];
    [data setObject:customDataFromYourServer forKey:@"customDataFromYourServer"];
    
    return data;
}

+ (id)StringeeCall2:(StringeeCall2 *)call {
    if (!call) {
        return [NSNull null];
    }
    
    id callId = call.callId ? call.callId :[NSNull null];
    id from = call.from ? call.from : [NSNull null];
    id to = call.to ? call.to : [NSNull null];
    id fromAlias = call.fromAlias ? call.fromAlias : [NSNull null];
    id toAlias = call.toAlias ? call.toAlias : [NSNull null];
    id customDataFromYourServer = call.customDataFromYourServer ? call.customDataFromYourServer : [NSNull null];
    
    int calltype = 0;
    if (call.callType == CallTypeCallIn) {
        // Phone-to-app
        calltype = 3;
    } else if (call.callType == CallTypeCallOut) {
        // App-to-phone
        calltype = 2;
    } else if (call.callType == CallTypeInternalIncomingCall) {
        // App-to-app-incoming-call
        calltype = 1;
    } else {
        // App-to-app-outgoing-call
        calltype = 0;
    }
    
    NSMutableDictionary *data = [NSMutableDictionary new];
    [data setObject:callId forKey:@"callId"];
    [data setObject:@(call.serial) forKey:@"serial"];
    [data setObject:from forKey:@"from"];
    [data setObject:to forKey:@"to"];
    [data setObject:fromAlias forKey:@"fromAlias"];
    [data setObject:toAlias forKey:@"toAlias"];
    [data setObject:@(calltype) forKey:@"callType"];
    [data setObject:@(call.isVideoCall) forKey:@"isVideoCall"];
    [data setObject:customDataFromYourServer forKey:@"customDataFromYourServer"];
    
    return data;
}

+ (id)Identity:(StringeeIdentity *)identity {
    if (!identity) return [NSNull null];
    
    NSString *userId = identity.userId.length ? identity.userId : @"";
    NSString *name = identity.displayName.length ? identity.displayName : @"";
    NSString *avatar = identity.avatarUrl.length ? identity.avatarUrl : @"";
    NSString *role = identity.role == StringeeRoleAdmin ? @"admin" : @"member";

    return @{
             @"userId": userId,
             @"name": name,
             @"avatarUrl": avatar,
             @"role": role
             };
}

+ (id)Identities:(NSArray<StringeeIdentity *> *)identities {
    if (!identities) {
        return @[];
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeIdentity *identity in identities) {
        [response addObject:[self Identity:identity]];
    }
    return response;
}

+ (id)Conversation:(StringeeConversation *)conversation {
    if (!conversation) return [NSNull null];

    NSString *identifier = conversation.identifier ? conversation.identifier : @"";
    NSString *name = conversation.name ? conversation.name : @"";
    NSString *lastMsgId = conversation.lastMsg.identifier ? conversation.lastMsg.identifier : @"";
    NSString *pinMsgId = conversation.pinMsgId ? conversation.pinMsgId : @"";

    NSMutableArray *participants = [[NSMutableArray alloc] init];
    for (StringeeIdentity *identity in conversation.participants) {
        [participants addObject:[self Identity:identity]];
    }
    NSString *lastMsgSender = conversation.lastMsg.sender ? conversation.lastMsg.sender : @"";
    NSString *text = conversation.lastMsg.content ? conversation.lastMsg.content : @"";
    id lastMsgContent = [self StringToDictionary:text];
    NSString *creator = conversation.creator ? conversation.creator : @"";
    StringeeMessageStatus lastMsgState = conversation.lastMsgSeqReceived > conversation.lastMsgSeqSeen ? StringeeMessageStatusDelivered : StringeeMessageStatusRead;
    NSString *oaId = conversation.oaId ? conversation.oaId : @"";
    NSString *customData = conversation.customData ? conversation.customData : @"";

    return @{
             @"id": identifier,
             @"name": name,
             @"participants": participants,
             @"isGroup": @(conversation.isGroup),
             @"updatedAt" : @(conversation.lastUpdate),
             @"lastMsgSender" : lastMsgSender,
             @"text": lastMsgContent,
             @"lastMsgType": @(conversation.lastMsg.type),
             @"totalUnread": @(conversation.unread),
             @"lastMsgId": lastMsgId,
             @"creator": creator,
             @"createdAt" : @(conversation.created),
             @"lastMsgSeqReceived": @(conversation.lastMsgSeqReceived),
             @"lastTimeNewMsg": @(conversation.lastTimeNewMsg),
             @"lastMsgState": @(lastMsgState),
             @"pinnedMsgId" : pinMsgId,
             @"oaId": oaId,
             @"customData": customData
             };
}

+ (NSArray *)Conversations:(NSArray<StringeeConversation *> *)conversations {
    if (!conversations) {
        return @[];
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeConversation *conversation in conversations) {
        [response addObject:[self Conversation:conversation]];
    }
    return response;
}

+ (id)Message:(StringeeMessage *)message {
    if (!message) return [NSNull null];
    
    NSString *localId = message.localIdentifier.length ? message.localIdentifier : @"";
    NSString *identifier = message.identifier.length ? message.identifier : @"";
    NSString *conversationId = message.convId.length ? message.convId : @"";
    NSString *sender = message.sender.length ? message.sender : @"";
    id customData;
    if (message.metadata != nil) {
        if ([message.metadata isKindOfClass:[NSDictionary class]]) {
            customData = message.metadata;
        } else if ([message.metadata isKindOfClass:[NSString class]]) {
            customData = [self StringToDictionary:(NSString *)message.metadata];
        } else {
            customData = [NSNull null];
        }
    } else {
        customData = [NSNull null];
    }
    
    NSString *thumbnailPath = @"";
    NSString *thumbnailUrl = @"";
    NSString *filePath = @"";
    NSString *fileUrl = @"";
    double longitude = 0;
    double latitude = 0;
    double duration = 0;
    double ratio = 0;
    NSUInteger fileLength = 0;
    NSString *fileName = @"";
    NSString *contact = @"";
    
    NSDictionary *content;
    
    switch (message.type) {
        case StringeeMessageTypeText:
            content = @{@"content": message.content};
            break;
        case StringeeMessageTypeLink:
            content = @{@"content": message.content};
            break;
        case StringeeMessageTypeCreateGroup:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypeRenameGroup:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypeNotify:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypePhoto:
        {
            StringeePhotoMessage *photoMsg = (StringeePhotoMessage *)message;
            thumbnailPath = photoMsg.thumbnailPath.length ? photoMsg.thumbnailPath : @"";
            thumbnailUrl = photoMsg.thumbnailUrl.length ? photoMsg.thumbnailUrl : @"";
            filePath = photoMsg.filePath.length ? photoMsg.filePath : @"";
            fileUrl = photoMsg.fileUrl.length ? photoMsg.fileUrl : @"";
            ratio = photoMsg.ratio;
            
            content = @{
                        @"photo": @{
                                    @"filePath": fileUrl,
                                    @"thumbnail": thumbnailUrl,
                                    @"ratio": @(ratio)
                                }
                        };
        }
            break;
        case StringeeMessageTypeVideo:
        {
            StringeeVideoMessage *videoMsg = (StringeeVideoMessage *)message;
            thumbnailPath = videoMsg.thumbnailPath.length ? videoMsg.thumbnailPath : @"";
            thumbnailUrl = videoMsg.thumbnailUrl.length ? videoMsg.thumbnailUrl : @"";
            filePath = videoMsg.filePath.length ? videoMsg.filePath : @"";
            fileUrl = videoMsg.fileUrl.length ? videoMsg.fileUrl : @"";
            ratio = videoMsg.ratio;
            duration = videoMsg.duration;
            
            content = @{
                        @"video": @{
                                    @"filePath": fileUrl,
                                    @"thumbnail": thumbnailUrl,
                                    @"ratio": @(ratio),
                                    @"duration": @(duration)
                                }
                        };
        }
            break;
        case StringeeMessageTypeAudio:
        {
            StringeeAudioMessage *audioMsg = (StringeeAudioMessage *)message;
            filePath = audioMsg.filePath.length ? audioMsg.filePath : @"";
            fileUrl = audioMsg.fileUrl.length ? audioMsg.fileUrl : @"";
            duration = audioMsg.duration;
            
            content = @{
                        @"audio": @{
                                @"filePath": fileUrl,
                                @"duration": @(duration)
                                }
                        };
        }
            break;
        case StringeeMessageTypeFile:
        {
            StringeeFileMessage *fileMsg = (StringeeFileMessage *)message;
            filePath = fileMsg.filePath.length ? fileMsg.filePath : @"";
            fileUrl = fileMsg.fileUrl.length ? fileMsg.fileUrl : @"";
            fileName = fileMsg.filename.length ? fileMsg.filename : @"";
            fileLength = fileMsg.length;
            
            content = @{
                        @"file": @{
                                @"filePath": fileUrl,
                                @"fileName": fileName,
                                @"fileLength": @(fileLength),
                                }
                        };
        }
            break;
        case StringeeMessageTypeLocation:
        {
            StringeeLocationMessage *locationMsg = (StringeeLocationMessage *)message;
            latitude = locationMsg.latitude;
            longitude = locationMsg.longitude;
            
            content = @{
                        @"location": @{
                                @"lat": @(latitude),
                                @"lon": @(longitude)
                                }
                        };
        }
            break;
        case StringeeMessageTypeContact:
        {
            StringeeContactMessage *contactMsg = (StringeeContactMessage *)message;
            NSString *vcard = contactMsg.vcard.length ? contactMsg.vcard : @"";
            
            content = @{
                        @"contact": @{
                                @"vcard": vcard
                                }
                        };
        }
            break;
            
        default:
            content = @{};
            break;
    }
    
    
    return @{
             @"localId": localId,
             @"id": identifier,
             @"convId": conversationId,
             @"senderId": sender,
             @"createdAt": @(message.created),
             @"state": @(message.status),
             @"sequence": @(message.seq),
             @"customData": customData,
             @"type": @(message.type),
             @"content": content,
             @"thumbnailPath": thumbnailPath,
             @"thumbnailUrl": thumbnailUrl,
             @"filePath": filePath,
             @"fileUrl": fileUrl,
             @"latitude": @(latitude),
             @"longitude": @(longitude),
             @"duration": @(duration),
             @"ratio": @(ratio),
             @"fileName": fileName,
             @"fileLength": @(fileLength),
             @"contact": contact
             };
}

+ (NSArray *)Messages:(NSArray<StringeeMessage *> *)messages {
    if (!messages) {
        return @[];
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeMessage *message in messages) {
        [response addObject:[self Message:message]];
    }
    return response;
}

// MARK: - Utils

+ (id)StringToDictionary:(NSString *)str {
    if (!str || !str.length) {
        return [NSNull null];
    }
    
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    
    if (jsonError) {
        return [NSNull null];
    } else {
        return json;
    }
}

+ (id)StringToArray:(NSString *)str {
    if (!str || !str.length) {
        return [NSNull null];
    }
    
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSArray *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    
    if (jsonError) {
        return [NSNull null];
    } else {
        return json;
    }
}

+ (BOOL)isValid:(NSString *)value {
    if (value == nil || ![value isKindOfClass:[NSString class]] || value.length == 0) {
        return false;
    }
    
    return true;
}

+ (StringeeConversationOption *)parseOptionWithData:(NSDictionary *)data {
    StringeeConversationOption *option = [[StringeeConversationOption alloc] init];
    option.isGroup = [[data objectForKey:@"isGroup"] boolValue];
    option.distinctByParticipants = [[data objectForKey:@"isDistinct"] boolValue];
    option.oaId = (NSString *)[data objectForKey:@"oaId"];
    option.customData = (NSString *)[data objectForKey:@"customData"];
    option.creatorId = (NSString *)[data objectForKey:@"creatorId"];

    return option;
}

+ (NSSet<StringeeIdentity *> *)parsePartsWithData:(NSArray *)data {
    NSMutableSet *parts = [[NSMutableSet alloc] init];
    for (NSDictionary *userData in data) {
        NSString *userId = [userData objectForKey:@"userId"];
        StringeeIdentity *part = [[StringeeIdentity alloc] init];
        part.userId = userId;
        [parts addObject:part];
    }
    
    return parts;
}

+ (NSArray<StringeeServerAddress *> *)parseServerAddressesWithData:(NSArray *)data {
    NSMutableArray *serverAddresses = [[NSMutableArray alloc] init];
    for (NSDictionary *serverAddrData in data) {
        NSString *host = [serverAddrData objectForKey:@"host"];
        int port = [[serverAddrData objectForKey:@"port"] intValue];
        StringeeServerAddress *serverAddr = [[StringeeServerAddress alloc] initWithHost:host port:port];
        [serverAddresses addObject:serverAddr];
    }
    
    return serverAddresses;
}

+ (id)ChatProfile:(StringeeChatProfile *)profile {
    if (!profile) return [NSNull null];
    
    NSString *identifier = profile.identifier != nil ? profile.identifier : @"";
    NSString *background = profile.background != nil ? profile.background : @"";
    NSString *hour = profile.hour != nil ? profile.hour : @"";
    NSString *language = profile.language != nil ? profile.language : @"";
    NSString *logo_url = profile.logo_url != nil ? profile.logo_url : @"";
    NSString *popup_answer_url = profile.popup_answer_url != nil ? profile.popup_answer_url : @"";
    NSString *portal = profile.portal != nil ? profile.portal : @"";
    NSArray *queues = [StringeeHelper Queues:profile.queues];
    
    return @{
             @"id": identifier,
             @"background": background,
             @"hour": hour,
             @"language": language,
             @"logo_url": logo_url,
             @"popup_answer_url": popup_answer_url,
             @"portal": portal,
             @"queues": queues,
             @"auto_create_ticket": @(profile.auto_create_ticket),
             @"enabled": @(profile.enabled),
             @"facebook_as_livechat": @(profile.facebook_as_livechat),
             @"project_id": @(profile.project_id),
             @"zalo_as_livechat": @(profile.zalo_as_livechat)
             };
}

+ (id)StringeeQueue:(StringeeQueue *)queue {
    if (!queue) return [NSNull null];
    
    NSString *identifier = queue.identifier != nil ? queue.identifier : @"";
    NSString *name = queue.name != nil ? queue.name : @"";

    return @{
             @"id": identifier,
             @"name": name
             };
}

+ (NSArray *)Queues:(NSArray<StringeeQueue *> *)queues {
    if (!queues) {
        return @[];
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeQueue *queue in queues) {
        [response addObject:[self StringeeQueue:queue]];
    }
    return response;
}

+ (id)StringeeChatRequest:(StringeeChatRequest *)request {
    if (!request) return [NSNull null];
    
    NSString *convId = request.convId != nil ? request.convId : @"";
    NSString *customerId = request.customerId != nil ? request.customerId : @"";
    NSString *customerName = request.customerName != nil ? request.customerName : @"";
    
    return @{
             @"convId": convId,
             @"customerId": customerId,
             @"customerName": customerName,
             @"channelType": @(request.channelType),
             @"type": @(request.type),
             };
}

@end
