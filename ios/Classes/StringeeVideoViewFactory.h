//
// Created by HoangDuoc on 1/6/21.
//

#import <Flutter/Flutter.h>

@interface StringeeVideoViewFactory : NSObject <FlutterPlatformViewFactory>
- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;
@end
