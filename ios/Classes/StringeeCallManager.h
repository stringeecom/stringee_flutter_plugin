//
//  StringeeCallManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeCallManager : NSObject<StringeeCallDelegate>

- (instancetype)initWithIdentifier:(NSString *)identifier;

- (void)setClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)makeCall:(id)arguments result:(FlutterResult)result;

- (void)initAnswer:(id)arguments result:(FlutterResult)result;

- (void)answer:(id)arguments result:(FlutterResult)result;

- (void)hangup:(id)arguments result:(FlutterResult)result;

- (void)reject:(id)arguments result:(FlutterResult)result;

- (void)sendDtmf:(id)arguments result:(FlutterResult)result;

- (void)sendCallInfo:(id)arguments result:(FlutterResult)result;

- (void)getCallStats:(id)arguments result:(FlutterResult)result;

- (void)mute:(id)arguments result:(FlutterResult)result;

- (void)setSpeakerphoneOn:(id)arguments result:(FlutterResult)result;

- (void)switchCamera:(id)arguments result:(FlutterResult)result;

- (void)enableVideo:(id)arguments result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END
