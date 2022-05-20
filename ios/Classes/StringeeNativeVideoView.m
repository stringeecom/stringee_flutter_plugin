
#import "StringeeNativeVideoView.h"
#import <Stringee/Stringee.h>
#import "StringeeManager.h"
#import "StringeeHelper.h"
#import "StringeeVideoConferenceManager.h"

@implementation StringeeNativeVideoView {
    UIView *_view;
}

- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
              binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    if (self = [super init]) {
        _view = [[UIView alloc] init];
        NSLog(@"KHOI TAO StringeeNativeVideoView: %@ - (%f, %f, %f %f)", args, frame.origin.x, frame.origin.y, frame.size.width, frame.size.height);
        BOOL forCall = [[args objectForKey:@"forCall"] boolValue];

        if (forCall) {
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
                StringeeVideoTrack *track = [[StringeeManager instance].call2VideoTracks objectForKey:callId];
                if (track != nil) {
                    StringeeVideoView *videoView = [track attachWithVideoContentMode:contentMode];
                    if (videoView != nil) {
                        videoView.frame = CGRectMake(0, 0, width, height);
                        [_view addSubview:videoView];
                    }

                    [[StringeeManager instance].call2VideoTracks removeObjectForKey:callId];
                } else {
                    call.remoteVideoView.contentMode = contentMode;
                    CGRect oldFrame = call.remoteVideoView.frame;
                    call.remoteVideoView.frame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y, width, height);
                    [_view addSubview:call.remoteVideoView];
                }
            }
        } else {
            // Video conference
            NSString *trackId = [args objectForKey:@"trackId"];

            NSString *scalingType = [args objectForKey:@"scalingType"];
            if ([args objectForKey:@"width"] == [NSNull null] || [args objectForKey:@"width"] == nil || [args objectForKey:@"height"] == [NSNull null] || [args objectForKey:@"height"] == nil) {
                NSLog(@"StringeeVideoView's size is invalid");
                return self;
            }

            CGFloat width = [[args objectForKey:@"width"] floatValue];
            CGFloat height = [[args objectForKey:@"height"] floatValue];

            if (![StringeeHelper validString:trackId]) {
                NSLog(@"TrackId invalid, trackId: %@", trackId);
                return self;
            }

            StringeeVideoContentMode contentMode = StringeeVideoContentModeScaleAspectFill;
            if (scalingType != nil && [scalingType isEqualToString:@"FIT"]) {
                contentMode = StringeeVideoContentModeScaleAspectFit;
            }

            StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:trackId];
            if (track == nil) {
                NSLog(@"Track not found");
                return self;
            }

            StringeeVideoView *videoView = [track attachWithVideoContentMode:contentMode];
            if (videoView != nil) {
                videoView.frame = CGRectMake(0, 0, width, height);
                [_view addSubview:videoView];
            }
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

