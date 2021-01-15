//
//  Utils.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/14/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeHelper : NSObject

+ (id)StringeeCall:(StringeeCall *)call;

+ (id)StringeeCall2:(StringeeCall2 *)call;

+ (StringeeConversationOption *)parseOptionWithData:(NSDictionary *)data;

+ (NSSet<StringeeIdentity *> *)parsePartsWithData:(NSArray *)data;

@end

NS_ASSUME_NONNULL_END
