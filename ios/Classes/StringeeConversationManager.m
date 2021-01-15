//
//  StringeeConversationManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import "StringeeConversationManager.h"
#import "StringeeFlutterPlugin.h"
#import "StringeeManager.h"
#import "StringeeHelper.h"

@implementation StringeeConversationManager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
}

- (instancetype)initWithClient:(StringeeClient *)client
{
    self = [super init];
    if (self) {
        _client = client;
    }
    return self;
}

- (void)setEventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
}

- (void)createConversation:(id)arguments result:(FlutterResult)result {
    if (!_client || !_client.hasConnected) {
        result(@{STEStatus : @(NO), STECode : @(-1), STEMessage: @"StringeeClient is not initialzied or connected."});
        return;
    }
    
    NSLog(@"==== createConversation: %@", arguments);
    NSDictionary *data = (NSDictionary *)arguments;
    NSDictionary *optionData = [data objectForKey:@"option"];
    NSArray *partsData = [data objectForKey:@"participants"];
    if (optionData == nil || partsData == nil) {
        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Option or Participants is invalid"});
    }
    
    StringeeConversationOption *option = [StringeeHelper parseOptionWithData:optionData];
    NSSet<StringeeIdentity *> *parts = [StringeeHelper parsePartsWithData:partsData];
    NSString *name = [optionData objectForKey:@"name"];
    
    [_client createConversationWithName:name participants:parts options:option completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        result(@{STEStatus : @(status), STECode : @(code), STEMessage: message, STEBody: @""});
    }];
    
//    if (!callId || [callId isKindOfClass:[NSNull class]] || !callId.length) {
//        result(@{STEStatus : @(NO), STECode : @(-2), STEMessage: @"Init answer call failed. The callId is invalid."});
//        return;
//    }
//
//    StringeeCall *call = [[StringeeManager instance].calls objectForKey:callId];
//
//    if (!call) {
//        result(@{STEStatus : @(NO), STECode : @(-3), STEMessage: @"Init answer call failed. The call is not found."});
//        return;
//    }
//
//    [call initAnswerCall];
    result(@{STEStatus : @(YES), STECode : @(0), STEMessage: @"Init answer call successfully."});
}

@end
