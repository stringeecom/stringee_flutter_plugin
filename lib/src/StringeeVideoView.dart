import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

import '../stringee_plugin.dart';

class StringeeVideoView extends StatelessWidget {
  late final String? callId;
  late final String? trackId;
  final bool isLocal;
  final bool? isMirror;
  final EdgeInsetsGeometry? margin;
  final AlignmentGeometry? alignment;
  final EdgeInsetsGeometry? padding;
  final ScalingType? scalingType;
  final double? height;
  final double? width;
  final Widget? child;
  late final bool forCall;
  final BorderRadius? borderRadius;

  StringeeVideoView(
    this.callId,
    this.isLocal, {
    Key? key,
    this.isMirror = false,
    this.height,
    this.width,
    this.margin,
    this.alignment,
    this.padding,
    this.child,
    this.scalingType = ScalingType.fill,
    this.borderRadius,
  })  : assert(margin == null || margin.isNonNegative),
        assert(padding == null || padding.isNonNegative),
        super(key: key) {
    forCall = true;
  }

  StringeeVideoView.forTrack(
    this.trackId, {
    Key? key,
    this.isMirror = false,
    this.height,
    this.width,
    this.margin,
    this.alignment,
    this.padding,
    this.child,
    this.scalingType = ScalingType.fill,
    this.borderRadius,
  })  : isLocal = true,
        assert(margin == null || margin.isNonNegative),
        assert(padding == null || padding.isNonNegative),
        super(key: key) {
    forCall = false;
  }

  Widget createVideoView(BuildContext context) {
    // This is used in the platform side to register the view.
    const viewType = 'stringeeVideoView';
    // Pass parameters to the platform side.
    Map<dynamic, dynamic> creationParams = <dynamic, dynamic>{};
    if (forCall) {
      creationParams = {
        'callId': callId,
        'isLocal': isLocal,
        'width': width,
        'height': height,
        'forCall': forCall,
      };
    } else {
      creationParams = {
        'trackId': trackId,
        'width': width,
        'height': height,
        'forCall': forCall,
      };
    }

    switch (scalingType) {
      case ScalingType.fill:
        creationParams['scalingType'] = "FILL";
        break;
      case ScalingType.fit:
        creationParams['scalingType'] = "FIT";
        break;
      default:
        creationParams['scalingType'] = "FILL";
        break;
    }

    if (Platform.isAndroid) {
      creationParams['isMirror'] = isMirror == null ? false : isMirror;
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return PlatformViewLink(
          viewType: viewType,
          surfaceFactory:
              (BuildContext context, PlatformViewController controller) {
            return AndroidViewSurface(
              controller: controller as AndroidViewController,
              gestureRecognizers: const <Factory<
                  OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
            );
          },
          onCreatePlatformView: (PlatformViewCreationParams params) {
            return PlatformViewsService.initSurfaceAndroidView(
              id: params.id,
              viewType: viewType,
              layoutDirection: TextDirection.rtl,
              creationParams: creationParams,
              creationParamsCodec: StandardMessageCodec(),
            )
              ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
              ..create();
          },
        );
      case TargetPlatform.iOS:
        // Work around the current iOS FlutterPlatformView sizing issue.
        if (width == null) {
          creationParams['width'] = MediaQuery.of(context).size.width;
        }

        if (height == null) {
          creationParams['height'] = MediaQuery.of(context).size.height;
        }

        return UiKitView(
          key: forCall ? Key(callId!) : Key(trackId!),
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        );
      default:
        throw UnsupportedError("Unsupported platform view");
    }
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> childrenWidget = <Widget>[createVideoView(context)];

    // Create a container with the configured size.
    Widget current = Container(
      height: height,
      width: width,
      child: Stack(
        children: childrenWidget,
      ),
    );

    // Overlay the optional child widget on top of the video view.
    if (child != null) {
      Widget? child = this.child;

      // Apply child padding.
      if (padding != null) {
        child = Padding(padding: padding!, child: child);
      }

      childrenWidget.add(child!);
    }

    // Apply border radius.
    if (borderRadius != null) {
      current = new ClipRRect(
        clipBehavior: Clip.hardEdge,
        borderRadius: borderRadius!,
        child: current,
      );
    }

    // Apply margin.
    if (margin != null) {
      current = Padding(padding: margin!, child: current);
    }

    // Apply alignment.
    if (alignment != null) {
      current = Align(alignment: alignment!, child: current);
    }

    return current;
  }
}

// class StringeeVideoView extends StatefulWidget {
//   late final String? callId;
//   late final String? trackId;
//   bool isLocal = true;
//   final bool? isMirror;
//   final EdgeInsetsGeometry? margin;
//   final AlignmentGeometry? alignment;
//   final EdgeInsetsGeometry? padding;
//   final ScalingType? scalingType;
//   final double? height;
//   final double? width;
//   final Widget? child;
//   late final bool forCall;
//   final BorderRadius? borderRadius;

//   StringeeVideoView(
//     this.callId,
//     this.isLocal, {
//     Key? key,
//     this.isMirror = false,
//     this.height,
//     this.width,
//     this.margin,
//     this.alignment,
//     this.padding,
//     this.child,
//     this.scalingType = ScalingType.fill,
//     this.borderRadius,
//   })  : assert(margin == null || margin.isNonNegative),
//         assert(padding == null || padding.isNonNegative),
//         super(key: key) {
//     forCall = true;
//   }

//   StringeeVideoView.forTrack(
//     this.trackId, {
//     Key? key,
//     this.isMirror = false,
//     this.height,
//     this.width,
//     this.margin,
//     this.alignment,
//     this.padding,
//     this.child,
//     this.scalingType = ScalingType.fill,
//     this.borderRadius,
//   })  : assert(margin == null || margin.isNonNegative),
//         assert(padding == null || padding.isNonNegative),
//         super(key: key) {
//     forCall = false;
//   }

//   @override
//   StringeeVideoViewState createState() => StringeeVideoViewState();
// }

// class StringeeVideoViewState extends State<StringeeVideoView> {
//   @override
//   void initState() {
//     super.initState();
//   }

//   Widget createVideoView(BuildContext context) {
//     // This is used in the platform side to register the view.
//     final String viewType = 'stringeeVideoView';
//     // Pass parameters to the platform side.
//     Map<dynamic, dynamic> creationParams = <dynamic, dynamic>{};
//     if (widget.forCall) {
//       creationParams = {
//         'callId': widget.callId,
//         'isLocal': widget.isLocal,
//         'width': widget.width,
//         'height': widget.height,
//         'forCall': widget.forCall,
//       };
//     } else {
//       creationParams = {
//         'trackId': widget.trackId,
//         'width': widget.width,
//         'height': widget.height,
//         'forCall': widget.forCall,
//       };
//     }

//     switch (widget.scalingType) {
//       case ScalingType.fill:
//         creationParams['scalingType'] = "FILL";
//         break;
//       case ScalingType.fit:
//         creationParams['scalingType'] = "FIT";
//         break;
//       default:
//         creationParams['scalingType'] = "FILL";
//         break;
//     }

//     if (Platform.isAndroid) {
//       creationParams['isMirror'] =
//           widget.isMirror == null ? false : widget.isMirror;
//     }

//     switch (defaultTargetPlatform) {
//       case TargetPlatform.android:
//         return PlatformViewLink(
//           viewType: viewType,
//           surfaceFactory:
//               (BuildContext context, PlatformViewController controller) {
//             return AndroidViewSurface(
//               controller: controller as AndroidViewController,
//               gestureRecognizers: const <Factory<
//                   OneSequenceGestureRecognizer>>{},
//               hitTestBehavior: PlatformViewHitTestBehavior.opaque,
//             );
//           },
//           onCreatePlatformView: (PlatformViewCreationParams params) {
//             return PlatformViewsService.initSurfaceAndroidView(
//               id: params.id,
//               viewType: viewType,
//               layoutDirection: TextDirection.rtl,
//               creationParams: creationParams,
//               creationParamsCodec: StandardMessageCodec(),
//             )
//               ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
//               ..create();
//           },
//         );
//       case TargetPlatform.iOS:
//         // Co loi FlutterPlatformView chua duoc fix => dung tam cach nay
//         if (widget.width == null) {
//           creationParams['width'] = MediaQuery.of(context).size.width;
//         }

//         if (widget.height == null) {
//           creationParams['height'] = MediaQuery.of(context).size.height;
//         }

//         return UiKitView(
//           viewType: viewType,
//           layoutDirection: TextDirection.ltr,
//           creationParams: creationParams,
//           creationParamsCodec: const StandardMessageCodec(),
//         );
//       default:
//         throw UnsupportedError("Unsupported platform view");
//     }
//   }

//   @override
//   Widget build(BuildContext context) {
//     List<Widget> childrenWidget = <Widget>[createVideoView(context)];

//     /// Create Container with height and width
//     Widget current = Container(
//       height: widget.height,
//       width: widget.width,
//       child: Stack(
//         children: childrenWidget,
//       ),
//     );

//     /// Add child widget
//     if (widget.child != null) {
//       Widget? child = widget.child;

//       /// Set child widget padding
//       if (widget.padding != null) {
//         child = Padding(padding: widget.padding!, child: child);
//       }

//       childrenWidget.add(child!);
//     }

//     /// Set border
//     if (widget.borderRadius != null) {
//       current = new ClipRRect(
//         clipBehavior: Clip.hardEdge,
//         borderRadius: widget.borderRadius!,
//         child: current,
//       );
//     }

//     /// Set margin
//     if (widget.margin != null) {
//       current = Padding(padding: widget.margin!, child: current);
//     }

//     /// Set alignment
//     if (widget.alignment != null) {
//       current = Align(alignment: widget.alignment!, child: current);
//     }

//     return current;
//   }
// }
