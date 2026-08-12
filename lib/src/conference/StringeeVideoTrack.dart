import 'package:flutter/material.dart';

import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// A local or remote media track in a [StringeeVideoRoom].
class StringeeVideoTrack {
  late String _id;
  late String _localId;
  late StringeeRoomUser _publisher;
  late bool _audioEnable;
  late bool _videoEnable;
  late bool _isScreenCapture;
  late bool _isLocal;
  late final StringeeClient _client;

  /// Server-assigned track ID.
  String get id => _id;

  /// Local track ID used before or while publishing.
  String get localId => _localId;

  /// User who publishes this track.
  StringeeRoomUser get publisher => _publisher;

  /// Whether audio is enabled for the track.
  bool get audioEnable => _audioEnable;

  /// Whether video is enabled for the track.
  bool get videoEnable => _videoEnable;

  /// Whether this track captures a screen instead of a camera.
  bool get isScreenCapture => _isScreenCapture;

  /// Whether this track originates on the current device.
  bool get isLocal => _isLocal;

  @override
  String toString() {
    return '{id: $_id, publisher: $_publisher, audioEnable: $_audioEnable, videoEnable: $_videoEnable, isScreenCapture: $_isScreenCapture, isLocal: $_isLocal}';
  }

  /// Creates a track from native [info].
  StringeeVideoTrack(
    StringeeClient client,
    Map<dynamic, dynamic> info,
  ) {
    this._client = client;
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
    this._localId = StringeeValueParser.toStringValue(info['localId']) ?? '';
    this._audioEnable = StringeeValueParser.toBool(info['audio']) ?? false;
    this._videoEnable = StringeeValueParser.toBool(info['video']) ?? false;
    this._isScreenCapture = StringeeValueParser.toBool(info['screen']) ?? false;
    this._isLocal = StringeeValueParser.toBool(info['isLocal']) ?? false;
    this._publisher = StringeeRoomUser(
      StringeeValueParser.toMap(info['publisher']) ?? {},
    );
  }

  /// Returns immutable metadata suitable for subscribe/unsubscribe APIs.
  StringeeVideoTrackInfo getInfo() {
    StringeeVideoTrackInfo info = StringeeVideoTrackInfo.fromTrack(this);
    return info;
  }

  /// Mutes or unmutes this track's audio stream.
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'mute': mute,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('track.mute', params);
  }

  /// Enables or disables this track's video stream.
  Future<Map<dynamic, dynamic>> enableVideo(bool enable) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'enable': enable,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('track.enableVideo', params);
  }

  /// Switches this local track to another camera.
  Future<Map<dynamic, dynamic>> switchCamera({String? cameraId}) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      if (cameraId != null) 'cameraId': cameraId,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('track.switchCamera', params);
  }

  /// Creates a Flutter view attached to this video track.
  StringeeVideoView attach({
    Key? key,
    bool? isMirror,
    double? height,
    double? width,
    EdgeInsetsGeometry? margin,
    AlignmentGeometry? alignment,
    EdgeInsetsGeometry? padding,
    Widget? child,
    ScalingType? scalingType,
    BorderRadius? borderRadius,
  }) {
    StringeeVideoView videoView = StringeeVideoView.forTrack(
      _isLocal ? _localId : _id,
      height: height,
      isMirror: isMirror,
      width: width,
      margin: margin,
      padding: padding,
      alignment: alignment,
      child: child,
      scalingType: scalingType,
      borderRadius: borderRadius,
    );
    return videoView;
  }
}
