//
//  StringeeManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeManager : NSObject

@property (nonatomic) NSMutableDictionary *calls;
@property (nonatomic) NSMutableDictionary *call2s;
@property (nonatomic) NSMutableDictionary *call2VideoTracks;

+ (StringeeManager *)instance;

- (void)addTrackForCall2:(StringeeVideoTrack *)track callId:(NSString *)callId;

- (void)removeTrackForCall2:(NSString *)callId;

- (StringeeVideoTrack *)getVideoTrackForCall2:(NSString *)callId trackId:(NSString*)trackId;

- (void)removeTrackForCall2:(NSString *)callId trackId:(NSString *)trackId;

- (StringeeVideoTrack *)getVideoTrackForCall2ByTracKId:(NSString*)trackId;

@end

NS_ASSUME_NONNULL_END

