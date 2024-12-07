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

static NSString *STEAudioMethodChannelName = @"com.stringee.flutter.audio.method_channel";
static NSString *STEAudioEventChannelName = @"com.stringee.flutter.audio.event_channel";

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
static NSString *STEDidReceiveChatRequest            = @"didReceiveChatRequest";
static NSString *STEDidReceiveTransferChatRequest    = @"didReceiveTransferChatRequest";
static NSString *STETimeoutAnswerChat                = @"timeoutAnswerChat";
static NSString *STETimeoutInQueue                   = @"timeoutInQueue";
static NSString *STEConversationEnded                = @"conversationEnded";
static NSString *STEUserBeginTyping                  = @"userBeginTyping";
static NSString *STEUserEndTyping                    = @"userEndTyping";

// Room (Video Conference)
static NSString *STEDidJoinRoom            = @"didJoinRoom";
static NSString *STEDidLeaveRoom           = @"didLeaveRoom";
static NSString *STEDidAddVideoTrack       = @"didAddVideoTrack";
static NSString *STEDidRemoveVideoTrack    = @"didRemoveVideoTrack";
static NSString *STEDidReceiveRoomMessage  = @"didReceiveRoomMessage";
static NSString *STETrackReadyToPlay       = @"trackReadyToPlay";


typedef NS_ENUM(NSInteger, StringeeNativeEventType) {
    StringeeNativeEventTypeClient  = 0,
    StringeeNativeEventTypeCall    = 1,
    StringeeNativeEventTypeCall2   = 2,
    StringeeNativeEventTypeChat    = 3,
    StringeeNativeEventTypeRoom    = 4
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

+ (BOOL)validString:(NSString *)value;

// MARK: - Stringee Video Conference

+ (id)StringeeVideoRoom:(StringeeVideoRoom *)room;

+ (id)StringeeVideoTrackInfo:(StringeeVideoTrackInfo *)trackInfo;

+ (NSArray *)StringeeVideoTrackInfos:(NSArray<StringeeVideoTrackInfo *> *)trackInfos;

+ (id)StringeeRoomUserInfo:(StringeeRoomUserInfo *)userInfo;

+ (NSArray *)StringeeRoomUserInfos:(NSArray<StringeeRoomUserInfo *> *)userInfos;

+ (StringeeVideoTrackOption *)parseVideoTrackOptionWithData:(NSDictionary *)data;

+ (id)StringeeVideoTrack:(StringeeVideoTrack *)track;

@end

NS_ASSUME_NONNULL_END

