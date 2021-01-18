//
//  StringeeConversationManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeConversationManager : NSObject

- (instancetype)initWithClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)createConversation:(id)arguments result:(FlutterResult)result;

- (void)getConversationById:(id)arguments result:(FlutterResult)result;

- (void)getConversationByUserId:(id)arguments result:(FlutterResult)result;

- (void)getLocalConversations:(id)arguments result:(FlutterResult)result;

- (void)getLastConversation:(id)arguments result:(FlutterResult)result;

- (void)getConversationsBefore:(id)arguments result:(FlutterResult)result;

- (void)getConversationsAfter:(id)arguments result:(FlutterResult)result;

- (void)clearDb:(id)arguments result:(FlutterResult)result;

- (void)deleteConv:(id)arguments result:(FlutterResult)result;

- (void)addParticipants:(id)arguments result:(FlutterResult)result;

- (void)removeParticipants:(id)arguments result:(FlutterResult)result;

- (void)setRole:(id)arguments result:(FlutterResult)result;

- (void)getTotalUnread:(id)arguments result:(FlutterResult)result;

- (void)updateConversation:(id)arguments result:(FlutterResult)result;

- (void)markAsRead:(id)arguments result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END
