
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

@interface StringeeFlutterPlugin : NSObject<FlutterPlugin, StringeeIncomingCallDelegate, StringeeConnectionDelegate, FlutterStreamHandler, StringeeCallDelegate>

@property (nonatomic) StringeeClient *client;
@property (nonatomic) NSMutableDictionary *calls;

@end
