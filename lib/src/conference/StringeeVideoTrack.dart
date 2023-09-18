import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../../stringee_flutter_plugin.dart';

class StringeeVideoTrack {
  late final StringeeClient _client;
  late String _id;
  late String _localId;
  late StringeeRoomUser _publisher;
  late bool _audioEnable;
  late bool _videoEnable;
  late bool _isScreenCapture;
  late bool _isLocal;

  String get id => _id;

  String get localId => _localId;

  StringeeRoomUser get publisher => _publisher;

  bool get audioEnable => _audioEnable;

  bool get videoEnable => _videoEnable;

  bool get isScreenCapture => _isScreenCapture;

  bool get isLocal => _isLocal;

  @override
  String toString() {
    return {
      'id': _id,
      'localId': _localId,
      'publisher': _publisher.toString(),
      'audioEnable': _audioEnable,
      'videoEnable': _videoEnable,
      'isScreenCapture': _isScreenCapture,
      'isLocal': _isLocal,
    }.toString();
  }

  StringeeVideoTrack(
    StringeeClient client,
    Map<dynamic, dynamic> info,
  ) {
    this._client = client;
    this._id = info['id'];
    this._localId = info['localId'];
    this._audioEnable = info['audio'];
    this._videoEnable = info['video'];
    this._isScreenCapture = info['screen'];
    this._isLocal = info['isLocal'];
    this._publisher = StringeeRoomUser(info['publisher']);
  }

  /// Mute
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'mute': mute,
    };
    return await _client.methodChannel.invokeMethod('track.mute', params);
  }

  /// Enable video
  Future<Map<dynamic, dynamic>> enableVideo(bool enable) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'enable': enable,
    };
    return await _client.methodChannel
        .invokeMethod('track.enableVideo', params);
  }

  /// Switch camera
  Future<Map<dynamic, dynamic>> switchCamera({String? cameraId}) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      if (cameraId != null) 'cameraId': cameraId,
    };
    return await _client.methodChannel
        .invokeMethod('track.switchCamera', params);
  }

  /// Release track
  Future<Map<dynamic, dynamic>> release() async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('track.release', params);
  }

  /// Snap shot local
  Future<Map<dynamic, dynamic>> snapShot() async {
    if (!(_isLocal || _isScreenCapture)) {
      final params = {
        'status': false,
        "code": '-5',
        "message": "This video track is not your local track",
      };
      return params;
    }
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
    };
    var result =
        await _client.methodChannel.invokeMethod('track.snapShot', params);
    if (result['status']) {
      result['image'] = MemoryImage(Uint8List.fromList(result['image']));
    }
    return result;
  }

  /// Send audio enable notification
  Future<Map<dynamic, dynamic>> sendAudioEnableNotification(bool enable) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'enable': enable,
    };
    return await _client.methodChannel
        .invokeMethod('track.sendAudioEnableNotification', params);
  }

  /// Send video enable notification
  Future<Map<dynamic, dynamic>> sendVideoEnableNotification(bool enable) async {
    final params = {
      'localId': _localId,
      'uuid': _client.uuid,
      'enable': enable,
    };
    return await _client.methodChannel
        .invokeMethod('track.sendVideoEnableNotification', params);
  }

  /// Set speaker phone on/off
  Future<Map<dynamic, dynamic>> setSpeakerphoneOn(bool speakerPhoneOn) async {
    final params = {
      'speaker': speakerPhoneOn,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel
        .invokeMethod('track.setSpeakerphoneOn', params);
  }

  /// Set audio to bluetooth
  Future<Map<dynamic, dynamic>> setBluetoothScoOn(bool bluetoothScoOn) async {
    final params = {
      'bluetooth': bluetoothScoOn,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel
        .invokeMethod('track.setBluetoothScoOn', params);
  }

  /// Set stream like a mirror
  Future<Map<dynamic, dynamic>> setMirror(bool isLocal, bool isMirror) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      final params = {
        'id': isLocal ? _localId : id,
        'isMirror': isMirror,
        'uuid': _client.uuid,
      };
      return await _client.methodChannel
          .invokeMethod('track.setMirror', params);
    }
  }

  /// Attach view
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
    );
    return videoView;
  }
}
