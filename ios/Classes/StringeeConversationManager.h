//
//  StringeeConversationManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeConversationManager : NSObject

- (instancetype)initWithClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

- (void)createConversation:(id)arguments result:(FlutterResult)result;

@end

NS_ASSUME_NONNULL_END
