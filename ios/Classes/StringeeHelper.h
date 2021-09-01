//
//  Utils.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

// Channel
static NSString *STEMethodChannelName = @"com.stringee.flutter.methodchannel";
static NSString *STEEventChannelName = @"com.stringee.flutter.eventchannel";

// Common
static NSString *STEUuid               = @"uuid";
static NSString *STEEvent              = @"event";
static NSString *STEEventType          = @"nativeEventType";
static NSString *STEBody               = @"body";
static NSString *STEStatus             = @"status";
static NSString *STECode               = @"code";
static NSString *STEMessage            = @"message";

// Client
static NSString *STEDidConnect                  = @"didConnect";
static NSString *STEDidDisConnect               = @"didDisconnect";
static NSString *STEDidFailWithError            = @"didFailWithError";
static NSString *STERequestAccessToken          = @"requestAccessToken";
static NSString *STEIncomingCall                = @"incomingCall";
static NSString *STEIncomingCall2               = @"incomingCall2";
static NSString *STEDidReceiveCustomMessage     = @"didReceiveCustomMessage";
static NSString *STEDidReceiveChangeEvent       = @"didReceiveChangeEvent";

// Call
static NSString *STEDidChangeSignalingState     = @"didChangeSignalingState";
static NSString *STEDidChangeMediaState         = @"didChangeMediaState";
static NSString *STEDidReceiveLocalStream       = @"didReceiveLocalStream";
static NSString *STEDidReceiveRemoteStream      = @"didReceiveRemoteStream";
static NSString *STEDidReceiveDtmfDigit         = @"didReceiveDtmfDigit";
static NSString *STEDidReceiveCallInfo          = @"didReceiveCallInfo";
static NSString *STEDidHandleOnAnotherDevice    = @"didHandleOnAnotherDevice";

// Live-chat
static NSString *STEDidReceiveChatRequest            = @"STEDidReceiveChatRequest";
static NSString *STEDidReceiveTransferChatRequest    = @"STEDidReceiveTransferChatRequest";
static NSString *STETimeoutAnswerChat                = @"STETimeoutAnswerChat";
static NSString *STETimeoutInQueue                   = @"STETimeoutInQueue";
static NSString *STEConversationEnded                = @"STEConversationEnded";
static NSString *STEUserBeginTyping                  = @"STEUserBeginTyping";
static NSString *STEUserEndTyping                    = @"STEUserEndTyping";


typedef NS_ENUM(NSInteger, StringeeNativeEventType) {
    StringeeNativeEventTypeClient  = 0,
    StringeeNativeEventTypeCall    = 1,
    StringeeNativeEventTypeCall2   = 2,
    StringeeNativeEventTypeChat    = 3,
};

NS_ASSUME_NONNULL_BEGIN

@interface StringeeHelper : NSObject

+ (id)StringeeCall:(StringeeCall *)call;

+ (id)StringeeCall2:(StringeeCall2 *)call;

+ (id)Identity:(StringeeIdentity *)identity;

+ (id)Identities:(NSArray<StringeeIdentity *> *)identities;

+ (id)Conversation:(StringeeConversation *)conversation;

+ (NSArray *)Conversations:(NSArray<StringeeConversation *> *)conversations;

+ (id)Message:(StringeeMessage *)message;

+ (NSArray *)Messages:(NSArray<StringeeMessage *> *)messages;


+ (id)StringToDictionary:(NSString *)str;

+ (id)StringToArray:(NSString *)str;

+ (StringeeConversationOption *)parseOptionWithData:(NSDictionary *)data;

+ (NSSet<StringeeIdentity *> *)parsePartsWithData:(NSArray *)data;

+ (NSArray<StringeeServerAddress *> *)parseServerAddressesWithData:(NSArray *)data;

+ (id)ChatProfile:(StringeeChatProfile *)profile;

+ (id)StringeeChatRequest:(StringeeChatRequest *)request;

@end

NS_ASSUME_NONNULL_END
