
#import "StringeeNativeVideoView.h"
#import <Stringee/Stringee.h>
#import "StringeeManager.h"

@implementation StringeeNativeVideoView {
    UIView *_view;
}

- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
              binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    if (self = [super init]) {
        _view = [[UIView alloc] init];
//        NSLog(@"KHOI TAO StringeeNativeVideoView: %@ - (%f, %f, %f %f)", args, frame.origin.x, frame.origin.y, frame.size.width, frame.size.height);

        NSString *callId = [args objectForKey:@"callId"];
        BOOL isLocal = [[args objectForKey:@"isLocal"] boolValue];
        NSString *scalingType = [args objectForKey:@"scalingType"];
        if ([args objectForKey:@"width"] == [NSNull null] || [args objectForKey:@"width"] == nil || [args objectForKey:@"height"] == [NSNull null] || [args objectForKey:@"height"] == nil) {
            NSLog(@"StringeeVideoView's size is invalid");
            return self;
        }
        
        CGFloat width = [[args objectForKey:@"width"] floatValue];
        CGFloat height = [[args objectForKey:@"height"] floatValue];

        if (callId == nil || callId.length == 0) {
            NSLog(@"CallId is invalid, callId: %@", callId);
            return self;
        }
        
        StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
        if (call == nil) {
            call = [[StringeeManager instance].call2s objectForKey:callId];
        }
        
        if (call == nil) {
            return self;
        }
        
        StringeeVideoContentMode contentMode = StringeeVideoContentModeScaleAspectFill;
        if (scalingType != nil && [scalingType isEqualToString:@"FIT"]) {
            contentMode = StringeeVideoContentModeScaleAspectFit;
        }
        
        if (isLocal) {
            call.localVideoView.contentMode = contentMode;
            CGRect oldFrame = call.localVideoView.frame;
            call.localVideoView.frame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y, width, height);
            [_view addSubview:call.localVideoView];
        } else {
            call.remoteVideoView.contentMode = contentMode;
            CGRect oldFrame = call.remoteVideoView.frame;
            call.remoteVideoView.frame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y, width, height);
            [_view addSubview:call.remoteVideoView];
        }
        
    }
    return self;
}

- (UIView*)view {
    return _view;
}

- (void)dealloc
{
//    NSLog(@"Dealloc method is called");
    for (UIView *subview in _view.subviews) {
        [subview removeFromSuperview];
    }
}



@end
