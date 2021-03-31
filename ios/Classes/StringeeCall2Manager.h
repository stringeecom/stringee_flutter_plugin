//
//  StringeeCall2Manager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeCall2Manager : NSObject<StringeeCall2Delegate>

//- (instancetype)initWithClient:(StringeeClient *)client;

- (void)setClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)makeCall:(id)arguments result:(FlutterResult)result;

- (void)initAnswer:(id)arguments result:(FlutterResult)result;

- (void)answer:(id)arguments result:(FlutterResult)result;

- (void)hangup:(id)arguments result:(FlutterResult)result;

- (void)reject:(id)arguments result:(FlutterResult)result;

- (void)mute:(id)arguments result:(FlutterResult)result;

- (void)setSpeakerphoneOn:(id)arguments result:(FlutterResult)result;

- (void)switchCamera:(id)arguments result:(FlutterResult)result;

- (void)enableVideo:(id)arguments result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END
