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

- (instancetype)initWithClient:(StringeeClient *)client;

- (void)setEventSink:(FlutterEventSink)eventSink;

@end

NS_ASSUME_NONNULL_END
