//
//  StringeeManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import "StringeeManager.h"

@implementation StringeeManager

static StringeeManager *stringeeManager = nil;



+ (StringeeManager *)instance {
    @synchronized(self) {
        if (stringeeManager == nil) {
            stringeeManager = [[self alloc] init];
        }
    }
    return stringeeManager;
}

- (id)init {
    self = [super init];
    if (self) {
        self.calls = [[NSMutableDictionary alloc] init];
        self.call2s = [[NSMutableDictionary alloc] init];
        self.call2VideoTracks = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)addTrackForCall2:(StringeeVideoTrack *)track callId:(NSString *)callId {
    NSMutableDictionary *nestedDic = [self.call2VideoTracks objectForKey:callId];
    if (nestedDic == nil) {
        nestedDic = [[NSMutableDictionary alloc] init];
        [self.call2VideoTracks setObject:nestedDic forKey:callId];
    }
    NSString *trackId = track.isLocal ? track.localId : track.serverId;
    [nestedDic setObject:track forKey:trackId];
}

- (void)removeTrackForCall2:(NSString *)callId {
    [self.call2VideoTracks removeObjectForKey:callId];
}

- (void)removeTrackForCall2:(NSString *)callId trackId:(NSString *)trackId {
    NSMutableDictionary *nestedDic = [self.call2VideoTracks objectForKey:callId];
    if (nestedDic != nil) {
        [nestedDic removeObjectForKey:trackId];
    }
}

- (StringeeVideoTrack *)getVideoTrackForCall2:(NSString *)callId trackId:(NSString*)trackId {
    NSMutableDictionary *nestedDic = [self.call2VideoTracks objectForKey:callId];
    if (nestedDic == nil) {
        return nil;
    }
    
    if ((trackId == nil || trackId.length == 0) && nestedDic.allValues.count > 0) {
        return nestedDic.allValues.lastObject;
    }
    
    return [nestedDic objectForKey:trackId];
}

- (StringeeVideoTrack *)getVideoTrackForCall2ByTracKId:(NSString*)trackId {
    for (NSDictionary *tracks in self.call2VideoTracks.allValues) {
        for (StringeeVideoTrack *track in tracks.allValues) {
            if ([track.localId isEqualToString:trackId] || [track.serverId isEqualToString:trackId]) {
                return track;
            }
        }
    }
    
    return nil;
}

@end

