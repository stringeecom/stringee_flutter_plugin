//
//  StringeeManager.h
//  stringee_flutter_plugin
//
//  Created by HoangDuoc on 1/8/21.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

NS_ASSUME_NONNULL_BEGIN

@interface StringeeManager : NSObject

@property (nonatomic) NSMutableDictionary *calls;
@property (nonatomic) NSMutableDictionary *call2s;

+ (StringeeManager *)instance;

@end

NS_ASSUME_NONNULL_END
