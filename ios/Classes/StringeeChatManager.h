//
//  StringeeChatManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 9/1/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeChatManager : NSObject

- (instancetype)initWithIdentifier:(NSString *)identifier;

- (void)setClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)getChatProfile:(NSDictionary *)data result:(FlutterResult)result;

- (void)getLiveChatToken:(NSDictionary *)data result:(FlutterResult)result;

- (void)updateUserInfo:(NSDictionary *)data result:(FlutterResult)result;

- (void)createLiveChatConversation:(NSDictionary *)data result:(FlutterResult)result;

- (void)createLiveChatTicket:(NSDictionary *)data result:(FlutterResult)result;

- (void)sendChatTranscript:(NSDictionary *)data result:(FlutterResult)result;

- (void)endChat:(NSDictionary *)data result:(FlutterResult)result;

- (void)beginTyping:(NSDictionary *)data result:(FlutterResult)result;

- (void)endTyping:(NSDictionary *)data result:(FlutterResult)result;

- (void)acceptChatRequest:(NSDictionary *)data result:(FlutterResult)result;

- (void)rejectChatRequest:(NSDictionary *)data result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END

