//
//  StringeeClientWrapper.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 6/19/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>
#import <Flutter/Flutter.h>
#import "StringeeCallManager.h"
#import "StringeeCall2Manager.h"
#import "StringeeConversationManager.h"
#import "StringeeMessageManager.h"
#import "StringeeChatManager.h"

@interface StringeeClientWrapper : NSObject<StringeeIncomingCallDelegate, StringeeConnectionDelegate>

// Class func
+ (instancetype)getByUuid:(NSString *)identifier;

+ (void)setEventSinkForAllInstances:(FlutterEventSink)eventSink;

// Instance info
@property (nonatomic) StringeeClient *client;
@property (nonatomic) NSString *identifier;
@property (nonatomic) NSString *baseAPIUrl;

@property (nonatomic) StringeeCallManager *callManager;
@property (nonatomic) StringeeCall2Manager *call2Manager;
@property (nonatomic) StringeeConversationManager *convManager;
@property (nonatomic) StringeeMessageManager *msgManager;
@property (nonatomic) StringeeChatManager *chatManager;

- (instancetype)initWithIdentifier:(NSString *)identifier eventSink:(FlutterEventSink)eventSink;

- (void)setEventSink:(FlutterEventSink)eventSink;

// Flutter action
- (void)connect:(id)arguments result:(FlutterResult)result;

- (void)disconnect:(id)arguments result:(FlutterResult)result;

- (void)registerPush:(id)arguments result:(FlutterResult)result;

- (void)unregisterPush:(id)arguments result:(FlutterResult)result;

- (void)sendCustomMessage:(id)arguments result:(FlutterResult)result;

@end

