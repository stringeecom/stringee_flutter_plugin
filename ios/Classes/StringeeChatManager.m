//
//  StringeeChatManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 9/1/21.
//

#import "StringeeChatManager.h"

@implementation StringeeChatManager {
    StringeeClient *_client;
    FlutterEventSink _eventSink;
    NSString *_identifier;
}

- (instancetype)initWithIdentifier:(NSString *)identifier
{
    self = [super init];
    if (self) {
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

@end
