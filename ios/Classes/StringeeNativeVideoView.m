
#import "StringeeNativeVideoView.h"

@implementation StringeeNativeVideoView {
    UIView *_view;
}

- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
              binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    if (self = [super init]) {
        _view = [[UIView alloc] init];


    }
    return self;
}

- (UIView*)view {
    return _view;
}

@end
