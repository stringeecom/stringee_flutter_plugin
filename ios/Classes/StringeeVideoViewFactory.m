
#import "StringeeVideoViewFactory.h"
#import "StringeeNativeVideoView.h"

@implementation StringeeVideoViewFactory {
    NSObject<FlutterBinaryMessenger>* _messenger;
}

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        _messenger = messenger;
    }
    return self;
}

- (NSObject<FlutterPlatformView>*)createWithFrame:(CGRect)frame
                                   viewIdentifier:(int64_t)viewId
                                        arguments:(id _Nullable)args {
    return [[StringeeNativeVideoView alloc] initWithFrame:frame
                                           viewIdentifier:viewId
                                                arguments:args
                                          binaryMessenger:_messenger];
}

- (NSObject<FlutterMessageCodec> *)createArgsCodec {
    return [FlutterStandardMessageCodec sharedInstance];
}

@end
