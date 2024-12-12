#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

@interface StringeeFlutterAudioManager : NSObject <FlutterStreamHandler>

// Public initializer
- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;

// Handle method calls for the audio manager
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result;

@end