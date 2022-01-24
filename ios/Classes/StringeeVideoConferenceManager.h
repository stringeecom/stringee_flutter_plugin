//
//  StringeeVideoConferenceManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/15/22.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeVideoConferenceManager : NSObject<StringeeVideoTrackDelegate, StringeeVideoRoomDelegate>

- (instancetype)initWithIdentifier:(NSString *)identifier;

+ (StringeeVideoTrack *)getTrack:(NSString *)trackId;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)setClient:(StringeeClient *)client;

// MARK: - StringeeVideo

- (void)joinRoom:(NSDictionary *)data result:(FlutterResult)result;

- (void)createLocalVideoTrack:(NSDictionary *)data result:(FlutterResult)result;

// MARK: - StringeeVideoRoom

- (void)publish:(NSDictionary *)data result:(FlutterResult)result;

- (void)unpublish:(NSDictionary *)data result:(FlutterResult)result;

- (void)subscribe:(NSDictionary *)data result:(FlutterResult)result;

- (void)unsubscribe:(NSDictionary *)data result:(FlutterResult)result;

- (void)leave:(NSDictionary *)data result:(FlutterResult)result;

- (void)sendMessage:(NSDictionary *)data result:(FlutterResult)result;

// MARK: - StringeeVideoTrack

- (void)mute:(NSDictionary *)data result:(FlutterResult)result;

- (void)enableVideo:(NSDictionary *)data result:(FlutterResult)result;

- (void)switchCamera:(NSDictionary *)data result:(FlutterResult)result;

- (void)close:(NSDictionary *)data result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END
