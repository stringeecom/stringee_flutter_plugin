//
//  Utils.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeHelper : NSObject

+ (id)StringeeCall:(StringeeCall *)call;

+ (id)StringeeCall2:(StringeeCall2 *)call;

+ (id)Identity:(StringeeIdentity *)identity;

+ (id)Identities:(NSArray<StringeeIdentity *> *)identities;

+ (StringeeConversationOption *)parseOptionWithData:(NSDictionary *)data;

+ (NSSet<StringeeIdentity *> *)parsePartsWithData:(NSArray *)data;

+ (id)StringeeIdentity:(StringeeIdentity *)identity;

+ (id)Conversation:(StringeeConversation *)conversation;

+ (NSArray *)Conversations:(NSArray<StringeeConversation *> *)conversations;

+ (id)Message:(StringeeMessage *)message;

+ (NSArray *)Messages:(NSArray<StringeeMessage *> *)messages;

@end

NS_ASSUME_NONNULL_END
