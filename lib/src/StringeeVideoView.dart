import 'dart:io' show Platform;
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

class StringeeVideoView extends StatefulWidget {
  final String callId;
  bool isLocal = true;
  bool isOverlay = false;
  bool isMirror = false;
  final EdgeInsetsGeometry margin;
  final AlignmentGeometry alignment;
  final double height;
  final double width;
  final Color color;

  StringeeVideoView({
    Key key,
    this.callId,
    this.isLocal,
    this.isOverlay,
    this.isMirror,
    this.height,
    this.width,
    this.margin,
    this.alignment,
    this.color,
  });

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
    // TODO: implement initState
    super.initState();
    creationParams = {
      'callId': widget.callId,
      'isLocal': widget.isLocal,
      'isOverlay': widget.isOverlay,
    };
    if (Platform.isAndroid) {
      creationParams['isMirror'] = widget.isMirror;
    }
  }

  Widget createVideoView() {
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return PlatformViewLink(
          viewType: viewType,
          surfaceFactory:
              (BuildContext context, PlatformViewController controller) {
            return AndroidViewSurface(
              controller: controller,
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
        break;
      case TargetPlatform.iOS:
        return UiKitView(
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
        );
        break;
      default:
        throw UnsupportedError("Unsupported platform view");
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    Widget current = Container(
      color: widget.color,
      height: widget.height,
      width: widget.width,
      margin: widget.margin,
      child: createVideoView(),
    );

    if (widget.alignment != null)
      current = Align(alignment: widget.alignment, child: current);

    return current;
  }
}
