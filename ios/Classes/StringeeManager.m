//
//  StringeeManager.m
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import "StringeeManager.h"

@implementation StringeeManager

static StringeeManager *stringeeManager = nil;

+ (StringeeManager *)instance {
    @synchronized(self) {
        if (stringeeManager == nil) {
            stringeeManager = [[self alloc] init];
        }
    }
    return stringeeManager;
}

- (id)init {
    self = [super init];
    if (self) {
        self.calls = [[NSMutableDictionary alloc] init];
        self.call2s = [[NSMutableDictionary alloc] init];
    }
    return self;
}

@end
