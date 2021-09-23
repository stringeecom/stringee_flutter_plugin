import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'StringeeConstants.dart';

class StringeeVideoView extends StatefulWidget {
  late final String callId;
  late final String trackId;
  bool isLocal = true;
  bool? isOverlay = false;
  bool? isMirror = false;
  final EdgeInsetsGeometry? margin;
  final AlignmentGeometry? alignment;
  final EdgeInsetsGeometry? padding;
  ScalingType? scalingType = ScalingType.fill;
  final double? height;
  final double? width;
  final Color? color;
  final Widget? child;

  StringeeVideoView(
    this.callId,
    this.isLocal, {
    Key? key,
    this.isOverlay,
    this.isMirror,
    this.color,
    this.height,
    this.width,
    this.margin,
    this.alignment,
    this.padding,
    this.child,
    this.scalingType,
  })  : assert(margin == null || margin.isNonNegative),
        assert(padding == null || padding.isNonNegative),
        super(key: key);

  StringeeVideoView.forTrack(
    this.trackId, {
    Key? key,
    this.color,
    this.height,
    this.width,
    this.margin,
    this.alignment,
    this.padding,
    this.child,
    this.scalingType,
  })  : assert(margin == null || margin.isNonNegative),
        assert(padding == null || padding.isNonNegative),
        super(key: key);

  @override
  StringeeVideoViewState createState() => StringeeVideoViewState();
}

class StringeeVideoViewState extends State<StringeeVideoView> {
  // This is used in the platform side to register the view.
  final String viewType = 'stringeeVideoView';

  // Pass parameters to the platform side.
  Map<dynamic, dynamic> creationParams = <dynamic, dynamic>{};

  @override
  void initState() {
    super.initState();

    creationParams = {
      'trackId': widget.trackId,
      'callId': widget.callId,
      'isLocal': widget.isLocal,
      'isOverlay': widget.isOverlay,
      'width': widget.width,
      'height': widget.height
    };

    switch (widget.scalingType) {
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
      creationParams['isMirror'] =
          widget.isMirror == null ? false : widget.isMirror;
    }
  }

  Widget createVideoView(BuildContext context) {
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return PlatformViewLink(
          viewType: viewType,
          surfaceFactory:
              (BuildContext context, PlatformViewController controller) {
            return AndroidViewSurface(
              controller: controller as AndroidViewController,
              gestureRecognizers: const <
                  Factory<OneSequenceGestureRecognizer>>{},
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
        // Co loi FlutterPlatformView chua duoc fix => dung tam cach nay
        if (widget.width == null) {
          creationParams['width'] = MediaQuery.of(context).size.width;
        }

        if (widget.height == null) {
          creationParams['height'] = MediaQuery.of(context).size.height;
        }

        return UiKitView(
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
    List<Widget> childrenWidget = <Widget>[];
    childrenWidget.add(createVideoView(context));

    Widget current = Container(
      height: widget.height,
      width: widget.width,
      color: widget.color,
      margin: widget.margin,
      child: Stack(
        children: childrenWidget,
      ),
    );

    if (widget.child != null) {
      Widget? child = widget.child;

      if (widget.padding != null) {
        child = Padding(padding: widget.padding!, child: child);
      }

      childrenWidget.add(child!);
    }

    if (widget.alignment != null) {
      current = Align(alignment: widget.alignment!, child: current);
    }

    return current;
  }
}
