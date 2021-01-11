
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

// Channel
static NSString *STEMethodChannelName = @"com.stringee.flutter.methodchannel";
static NSString *STEEventChannelName = @"com.stringee.flutter.eventchannel";

// Common
static NSString *STEEvent              = @"event";
static NSString *STEEventType          = @"nativeEventType";
static NSString *STEBody               = @"body";
static NSString *STEStatus             = @"status";
static NSString *STECode               = @"code";
static NSString *STEMessage            = @"message";

// Client
static NSString *STEDidConnect               = @"didConnect";
static NSString *STEDidDisConnect            = @"didDisconnect";
static NSString *STEDidFailWithError         = @"didFailWithError";
static NSString *STERequestAccessToken       = @"requestAccessToken";
static NSString *STEIncomingCall               = @"incomingCall";
static NSString *STEDidReceiveCustomMessage    = @"didReceiveCustomMessage";

// Call
static NSString *STEDidChangeSignalingState     = @"didChangeSignalingState";
static NSString *STEDidChangeMediaState         = @"didChangeMediaState";
static NSString *STEDidReceiveLocalStream       = @"didReceiveLocalStream";
static NSString *STEDidReceiveRemoteStream      = @"didReceiveRemoteStream";
static NSString *STEDidReceiveDtmfDigit         = @"didReceiveDtmfDigit";
static NSString *STEDidReceiveCallInfo          = @"didReceiveCallInfo";
static NSString *STEDidHandleOnAnotherDevice    = @"didHandleOnAnotherDevice";

typedef NS_ENUM(NSInteger, StringeeNativeEventType) {
    StringeeNativeEventTypeClient  = 0,
    StringeeNativeEventTypeCall    = 1,
    StringeeNativeEventTypeCall2   = 2,
};

@interface StringeeFlutterPlugin : NSObject<FlutterPlugin, StringeeIncomingCallDelegate, StringeeConnectionDelegate, FlutterStreamHandler>

@property (nonatomic) StringeeClient *client;
//@property (nonatomic) NSMutableDictionary *calls;

@end
