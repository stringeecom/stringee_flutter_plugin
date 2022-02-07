//
//  StringeeVideoConferenceManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/15/22.
//

#import "StringeeVideoConferenceManager.h"
#import "StringeeHelper.h"

static NSMutableDictionary<NSString *, StringeeVideoRoom *> *_rooms;
static NSMutableDictionary<NSString *, StringeeVideoTrack *> *_localTracks; // key la localId
static NSMutableDictionary<NSString *, StringeeVideoTrack *> *_remoteTracks; // key la serverId

@implementation StringeeVideoConferenceManager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
    NSString *_identifier;
}

+ (void)initialize {
    if (_rooms == nil) {
        _rooms = [NSMutableDictionary new];
    }

    if (_localTracks == nil) {
        _localTracks = [NSMutableDictionary new];
    }

    if (_remoteTracks == nil) {
        _remoteTracks = [NSMutableDictionary new];
    }
}

- (instancetype)initWithIdentifier:(NSString *)identifier
{
    self = [super init];
    if (self) {
        [StringeeVideoConferenceManager initialize];
        _identifier = identifier;
    }
    return self;
}

- (void)setClient:(StringeeClient *)client {
    _client = client;
}

- (void)setEventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
}

// MARK: - StringeeVideo

- (void)joinRoom:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomToken = [data objectForKey:@"roomToken"];

    if (![StringeeHelper validString:roomToken]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    [StringeeVideo joinRoom:_client roomToken:roomToken completion:^(BOOL status, int code, NSString * _Nonnull message, StringeeVideoRoom * _Nonnull room, NSArray<StringeeVideoTrackInfo *> * _Nonnull tracks, NSArray<StringeeRoomUserInfo *> * _Nonnull users) {
        // Cache room
        if (room != nil) {
            room.delegate = self;
            [StringeeVideoConferenceManager addRoom:room];
        }

        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: @{@"room": [StringeeHelper StringeeVideoRoom:room], @"videoTrackInfos": [StringeeHelper StringeeVideoTrackInfos:tracks], @"users": [StringeeHelper StringeeRoomUserInfos:users]}});
    }];
}

- (void)createLocalVideoTrack:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied"});
        return;
    }

    NSDictionary *options = [data objectForKey:@"options"];

    if (options == nil) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoTrackOption *parsedOption = [StringeeHelper parseVideoTrackOptionWithData:options];

    StringeeVideoTrack *localTrack = [StringeeVideo createLocalVideoTrack:_client options:parsedOption delegate:self];
    [StringeeVideoConferenceManager addTrack:localTrack];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success", STEBody: [StringeeHelper StringeeVideoTrack:localTrack]});
}

// MARK: - StringeeVideoRoom

- (void)publish:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    NSString *localTrackId = [data objectForKey:@"localId"];

    if (![StringeeHelper validString:roomId] || ![StringeeHelper validString:localTrackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:localTrackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    if (!track.isLocal) {
        result(@{STEStatus : @(NO), STECode : @(-5), STEMessage: @"Can not publish remote track"});
        return;
    }

    [room publish:track completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper StringeeVideoTrack:track]});
    }];
}

- (void)unpublish:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    NSString *trackId = [data objectForKey:@"localId"];

    if (![StringeeHelper validString:roomId] || ![StringeeHelper validString:trackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:trackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    if (!track.isLocal) {
        result(@{STEStatus : @(NO), STECode : @(-5), STEMessage: @"Can not unpublish remote track"});
        return;
    }

    [room unpublish:track completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)subscribe:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    NSString *trackId = [data objectForKey:@"trackId"];
    NSDictionary *options = [data objectForKey:@"options"];

    if (![StringeeHelper validString:roomId] || ![StringeeHelper validString:trackId] || options == nil) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }
    StringeeVideoTrackOption *parsedOption = [StringeeHelper parseVideoTrackOptionWithData:options];

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }
    StringeeVideoTrackInfo *trackInfo = [[StringeeVideoTrackInfo alloc] init];
    trackInfo.serverId = trackId;
    [room subscribe:trackInfo options:parsedOption delegate:self completion:^(BOOL status, int code, NSString *message, StringeeVideoTrack *videoTrack) {
        [StringeeVideoConferenceManager addTrack:videoTrack];
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: [StringeeHelper StringeeVideoTrack:videoTrack]});
    }];
}

- (void)unsubscribe:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    NSString *trackId = [data objectForKey:@"trackId"];

    if (![StringeeHelper validString:roomId] || ![StringeeHelper validString:trackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:trackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    [room unsubscribe:track completion:^(BOOL status, int code, NSString *message) {
        if (status) {
            [StringeeVideoConferenceManager removeTrack:track.serverId];
        }
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)leave:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    BOOL allClient = [[data objectForKey:@"allClient"] boolValue];

    if (![StringeeHelper validString:roomId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }

    [room leave:allClient completion:^(BOOL status, int code, NSString *message) {
        // Sau khi leave thi xoa het data da cache
        [StringeeVideoConferenceManager removeRoom:room.roomId];
        [StringeeVideoConferenceManager removeAllTracksForRoom:room.roomId];

        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

- (void)sendMessage:(NSDictionary *)data result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }

    NSString *roomId = [data objectForKey:@"roomId"];
    NSDictionary *msg = [data objectForKey:@"msg"];

    if (![StringeeHelper validString:roomId] || msg == nil || ![msg isKindOfClass:[NSDictionary class]]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoRoom *room = [StringeeVideoConferenceManager getRoom:roomId];
    if (room == nil) {
        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Room not found"});
        return;
    }

    [room sendMessage:msg completion:^(BOOL status, int code, NSString *message) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
    }];
}

// MARK: - StringeeVideoTrack

- (void)mute:(NSDictionary *)data result:(FlutterResult)result {
    NSString *localTrackId = [data objectForKey:@"localId"];
    BOOL mute = [[data objectForKey:@"mute"] boolValue];

    if (![StringeeHelper validString:localTrackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:localTrackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    BOOL status = [track mute:mute];
    int code = status ? 1 : 0;
    NSString *message = status ? @"Success" : @"Fail";
    result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
}

- (void)enableVideo:(NSDictionary *)data result:(FlutterResult)result {
    NSString *localTrackId = [data objectForKey:@"localId"];
    BOOL enable = [[data objectForKey:@"enable"] boolValue];

    if (![StringeeHelper validString:localTrackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:localTrackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    BOOL status = [track enableLocalVideo:enable];
    int code = status ? 1 : 0;
    NSString *message = status ? @"Success" : @"Fail";
    result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
}

- (void)switchCamera:(NSDictionary *)data result:(FlutterResult)result {
    NSString *localTrackId = [data objectForKey:@"localId"];

    if (![StringeeHelper validString:localTrackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:localTrackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    BOOL status = [track switchCamera];
    int code = status ? 1 : 0;
    NSString *message = status ? @"Success" : @"Fail";
    result(@{STEStatus : @(status), STECode : @(code), STEMessage: message});
}

- (void)close:(NSDictionary *)data result:(FlutterResult)result {
    NSString *localTrackId = [data objectForKey:@"localId"];

    if (![StringeeHelper validString:localTrackId]) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Parameter invalid"});
        return;
    }

    StringeeVideoTrack *track = [StringeeVideoConferenceManager getTrack:localTrackId];
    if (track == nil) {
        result(@{STEStatus : @(NO), STECode : @(-4), STEMessage: @"Track not found"});
        return;
    }

    [track close];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Success"});
}

// MARK: - Stringee Video Room Delegate

- (void)joinRoom:(StringeeVideoRoom *)room userInfo:(StringeeRoomUserInfo *)userInfo {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STEDidJoinRoom, STEBody : @{ @"roomId" : room.roomId, @"user": [StringeeHelper StringeeRoomUserInfo:userInfo] }});
}

- (void)leaveRoom:(StringeeVideoRoom *)room userInfo:(StringeeRoomUserInfo *)userInfo {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STEDidLeaveRoom, STEBody : @{ @"roomId" : room.roomId, @"user": [StringeeHelper StringeeRoomUserInfo:userInfo] }});
}

- (void)addTrack:(StringeeVideoRoom *)room trackInfo:(StringeeVideoTrackInfo *)trackInfo {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STEDidAddVideoTrack, STEBody : @{ @"roomId" : room.roomId, @"videoTrackInfo": [StringeeHelper StringeeVideoTrackInfo:trackInfo] }});
}

- (void)removeTrack:(StringeeVideoRoom *)room trackInfo:(StringeeVideoTrackInfo *)trackInfo {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STEDidRemoveVideoTrack, STEBody : @{ @"roomId" : room.roomId, @"videoTrackInfo": [StringeeHelper StringeeVideoTrackInfo:trackInfo] }});
}

- (void)newMessage:(StringeeVideoRoom *)room msg:(NSDictionary *)msg fromUser:(StringeeRoomUserInfo *)fromUser {
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STEDidReceiveRoomMessage, STEBody : @{ @"roomId" : room.roomId, @"from": [StringeeHelper StringeeRoomUserInfo:fromUser], @"msg": msg }});
}

// MARK: - Stringee Video Track Delegate

- (void)ready:(StringeeVideoTrack *)track {
    NSLog(@"====== Track ready, localId: %@, serverId: %@", track.localId, track.serverId);
    _eventSink(@{STEUuid : _identifier, STEEventType : @(StringeeNativeEventTypeRoom), STEEvent : STETrackReadyToPlay, STEBody : @{ @"roomId" : track.room.roomId, @"track": [StringeeHelper StringeeVideoTrack:track] }});
}

// MARK: - Utils

+ (void)addRoom:(StringeeVideoRoom *)room {
    if (room == nil || room.roomId == nil || room.roomId.length == 0) {
        return;
    }

    [_rooms setObject:room forKey:room.roomId];
}

+ (StringeeVideoRoom *)getRoom:(NSString *)roomId {
    if (![StringeeHelper validString:roomId]) {
        return nil;
    }

    return [_rooms objectForKey:roomId];
}

+ (void)removeRoom:(NSString *)roomId {
    if (![StringeeHelper validString:roomId]) {
        return;
    }

    [_rooms removeObjectForKey:roomId];
}

+ (void)addTrack:(StringeeVideoTrack *)track {
    if (track == nil) {
        return;
    }

    if ([StringeeHelper validString:track.serverId]) {
        [_remoteTracks setObject:track forKey:track.serverId];
    } else if ([StringeeHelper validString:track.localId]) {
        [_localTracks setObject:track forKey:track.localId];
    }
}

+ (StringeeVideoTrack *)getTrack:(NSString *)trackId {
    // trackId co the la localId hoac serverId
    if (![StringeeHelper validString:trackId]) {
        return nil;
    }

    StringeeVideoTrack *track = [_remoteTracks objectForKey:trackId];
    if (track != nil) {
        return track;
    }

    return [_localTracks objectForKey:trackId];
}

+ (void)removeTrack:(NSString *)trackId {
    if (![StringeeHelper validString:trackId]) {
        return;
    }

    [_localTracks removeObjectForKey:trackId];
    [_remoteTracks removeObjectForKey:trackId];
}

+ (void)removeAllTracksForRoom:(NSString *)roomId {
    if (![StringeeHelper validString:roomId]) {
        return;
    }

    NSMutableArray *localKeys = [NSMutableArray new];
    for (StringeeVideoTrack *track in _localTracks.allValues) {
        if ([track.room.roomId isEqualToString:roomId]) {
            NSString *key = [[_localTracks allKeysForObject:track] firstObject];
            if ([StringeeHelper validString:key]) {
                [localKeys addObject:key];
            }
        }
    }
    [_localTracks removeObjectsForKeys:localKeys];

    NSMutableArray *remoteKeys = [NSMutableArray new];
    for (StringeeVideoTrack *track in _remoteTracks.allValues) {
        if ([track.room.roomId isEqualToString:roomId]) {
            NSString *key = [[_remoteTracks allKeysForObject:track] firstObject];
            if ([StringeeHelper validString:key]) {
                [remoteKeys addObject:key];
            }
        }
    }
    [_remoteTracks removeObjectsForKeys:remoteKeys];
}

@end
