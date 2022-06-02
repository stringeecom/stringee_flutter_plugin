//
//  StringeeMessageManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeMessageManager : NSObject

@property (nonatomic) NSMutableDictionary *trackedMessages;

- (instancetype)initWithIdentifier:(NSString *)identifier;

- (void)setClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)sendMessage:(id)arguments result:(FlutterResult)result;

- (void)getMessages:(id)arguments result:(FlutterResult)result;

- (void)getLocalMessages:(id)arguments result:(FlutterResult)result;

- (void)getLastMessages:(id)arguments result:(FlutterResult)result;

- (void)getMessagesAfter:(id)arguments result:(FlutterResult)result;

- (void)getMessagesBefore:(id)arguments result:(FlutterResult)result;

- (void)deleteMessages:(id)arguments result:(FlutterResult)result;

- (void)revokeMessages:(id)arguments result:(FlutterResult)result;

- (void)editMsg:(id)arguments result:(FlutterResult)result;

- (void)pinOrUnPin:(id)arguments result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END

