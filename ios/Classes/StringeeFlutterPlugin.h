
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

typedef NS_ENUM(NSInteger, StringeeNativeEventType) {
    StringeeNativeEventTypeClient  = 0,
    StringeeNativeEventTypeCall    = 1,
    StringeeNativeEventTypeCall2   = 2,
};

@interface StringeeFlutterPlugin : NSObject<FlutterPlugin, StringeeIncomingCallDelegate, StringeeConnectionDelegate, FlutterStreamHandler, StringeeCallDelegate>

@property (nonatomic) StringeeClient *client;
@property (nonatomic) NSMutableDictionary *calls;

@end
