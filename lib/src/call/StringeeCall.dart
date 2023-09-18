import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

class StringeeCall {
  late StringeeClient _client;
  String? _id;
  int? _serial;
  String? _from;
  String? _to;
  String? _fromAlias;
  String? _toAlias;
  StringeeCallType? _callType;
  String? customDataFromYourServer;
  bool isVideoCall = false;
  bool _isP2P = false;
  VideoQuality videoQuality = VideoQuality.normal;

  StringeeCallListener? _callListener;

  StreamController<dynamic> _eventStreamController = StreamController();
  late StreamSubscription<dynamic> _subscriber;

  String? get id => _id;

  int? get serial => _serial;

  String? get from => _from;

  String? get to => _to;

  String? get fromAlias => _fromAlias;

  String? get toAlias => _toAlias;

  StringeeCallType? get callType => _callType;

  bool get isP2P => _isP2P;

  @deprecated
  StreamController<dynamic> get eventStreamController => _eventStreamController;

  @override
  String toString() {
    return {
      'userId': _client.userId,
      'uuid': _client.uuid,
      'serial': _serial,
      'from': _from,
      'fromAlias': _fromAlias,
      'to': _to,
      'toAlias': _toAlias,
      'callType': _callType,
      'customDataFromYourServer': customDataFromYourServer,
      'isVideoCall': isVideoCall,
      'isP2P': _isP2P,
      'videoQuality': videoQuality.index,
    }.toString();
  }

  StringeeCall(StringeeClient client, String from, String to) {
    this._client = client;
    this._from = from;
    this._to = to;
    this._subscriber =
        client.eventStreamController.stream.listen(this._listener);
  }

  StringeeCall.fromCallInfo(
      Map<dynamic, dynamic>? info, StringeeClient client) {
    this.initCallInfo(info);
    _client = client;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  void initCallInfo(Map<dynamic, dynamic>? callInfo) {
    if (callInfo == null) {
      return;
    }

    this._id = callInfo['callId'];
    this._serial = callInfo['serial'];
    this._from = callInfo['from'];
    this._to = callInfo['to'];
    this._fromAlias = callInfo['fromAlias'];
    this._toAlias = callInfo['toAlias'];
    this.isVideoCall = callInfo['isVideoCall'];
    this.customDataFromYourServer = callInfo['customDataFromYourServer'];
    this._callType = StringeeCallType.values[callInfo['callType']];
    this.videoQuality = VideoQuality.values[callInfo['videoQuality']];
    this._isP2P = callInfo['isP2P'];
  }

  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeObjectEventType.call.index &&
        map['uuid'] == _client.uuid) {
      switch (map['event']) {
        case 'didChangeSignalingState':
          _handleDidChangeSignalingState(map['body']);
          break;
        case 'didChangeMediaState':
          _handleDidChangeMediaState(map['body']);
          break;
        case 'didReceiveCallInfo':
          _handleDidReceiveCallInfo(map['body']);
          break;
        case 'didHandleOnAnotherDevice':
          _handleDidHandleOnAnotherDevice(map['body']);
          break;
        case 'didReceiveLocalStream':
          _handleDidReceiveLocalStream(map['body']);
          break;
        case 'didReceiveRemoteStream':
          _handleDidReceiveRemoteStream(map['body']);
          break;
        case 'didChangeAudioDevice':
          _handleDidChangeAudioDevice(map['body']);
          break;
      }
    }
  }

  void registerEvent(StringeeCallListener callListener) {
    _callListener = callListener;
  }

  void _handleDidChangeSignalingState(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    StringeeSignalingState signalingState =
        StringeeSignalingState.values[map['code']];
    this._id = map['callId'];
    if (_callListener != null) {
      _callListener!.onChangeSignalingState(this, signalingState);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didChangeSignalingState,
        "body": signalingState
      });
    }
    if (signalingState == StringeeSignalingState.ended ||
        signalingState == StringeeSignalingState.busy) {
      destroy();
    }
  }

  void _handleDidChangeMediaState(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    StringeeMediaState mediaState = StringeeMediaState.values[map['code']];
    this._id = map['callId'];
    if (_callListener != null) {
      _callListener!.onChangeMediaState(this, mediaState);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didChangeMediaState,
        "body": mediaState
      });
    }
  }

  void _handleDidReceiveCallInfo(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    this._id = map['callId'];
    Map<dynamic, dynamic> data = map['info'];
    if (_callListener != null) {
      _callListener!.onReceiveCallInfo(this, data);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeCallEvents.didReceiveCallInfo, "body": data});
    }
  }

  void _handleDidHandleOnAnotherDevice(Map<dynamic, dynamic> map) {
    StringeeSignalingState signalingState =
        StringeeSignalingState.values[map['code']];
    this._id = map['callId'];
    if (_callListener != null) {
      _callListener!.onHandleOnAnotherDevice(this, signalingState);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didHandleOnAnotherDevice,
        "body": signalingState
      });
    }
    if (signalingState != StringeeSignalingState.ringing) {
      destroy();
    }
  }

  void _handleDidReceiveLocalStream(Map<dynamic, dynamic> map) {
    this._id = map['callId'];
    if (_callListener != null) {
      _callListener!.onReceiveLocalStream(this);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didReceiveLocalStream,
        "body": map['callId']
      });
    }
  }

  void _handleDidReceiveRemoteStream(Map<dynamic, dynamic> map) {
    this._id = map['callId'];
    if (_callListener != null) {
      _callListener!.onReceiveRemoteStream(this);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didReceiveRemoteStream,
        "body": map['callId']
      });
    }
  }

  void _handleDidChangeAudioDevice(Map<dynamic, dynamic> map) {
    AudioDevice selectedAudioDevice = AudioDevice.values[map['code']];
    List<dynamic> codeList = [];
    codeList.addAll(map['codeList']);
    List<AudioDevice> availableAudioDevices = [];
    for (int i = 0; i < codeList.length; i++) {
      AudioDevice audioDevice = AudioDevice.values[codeList[i]];
      availableAudioDevices.add(audioDevice);
    }
    if (_callListener != null) {
      if (_callListener!.onChangeAudioDevice != null) {
        _callListener!.onChangeAudioDevice!(
            this, selectedAudioDevice, availableAudioDevices);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeCallEvents.didChangeAudioDevice,
        "selectedAudioDevice": selectedAudioDevice,
        "availableAudioDevices": availableAudioDevices
      });
    }
  }

  /// Make a new coll with custom [parameters]
  Future<Map<dynamic, dynamic>> makeCall() async {
    var params = {
      'uuid': _client.uuid,
      'from': this._from,
      'to': this._to,
      if (this.customDataFromYourServer != null)
        'customData': this.customDataFromYourServer,
      'isVideoCall': this.isVideoCall,
    };
    if (this.isVideoCall) {
      switch (this.videoQuality) {
        case VideoQuality.normal:
          params['videoQuality'] = "NORMAL";
          break;
        case VideoQuality.hd:
          params['videoQuality'] = "HD";
          break;
        case VideoQuality.fullHd:
          params['videoQuality'] = "FULLHD";
          break;
      }
    }

    Map<dynamic, dynamic> results =
        await _client.methodChannel.invokeMethod('makeCall', params);
    Map<dynamic, dynamic>? callInfo = results['callInfo'];
    this.initCallInfo(callInfo);

    final Map<String, dynamic> resultDatas = {
      'status': results['status'],
      'code': results['code'],
      'message': results['message']
    };
    return resultDatas;
  }

  /// Init an answer from incoming call
  Future<Map<dynamic, dynamic>> initAnswer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await _client.methodChannel.invokeMethod('initAnswer', param);
  }

  /// Answer a call
  Future<Map<dynamic, dynamic>> answer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await _client.methodChannel.invokeMethod('answer', param);
  }

  /// Hang up a call
  Future<Map<dynamic, dynamic>> hangup() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};
    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('hangup', param);
    if (result['status']) {
      destroy();
    }
    return result;
  }

  /// Reject a call
  Future<Map<dynamic, dynamic>> reject() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('reject', param);
    if (result['status']) {
      destroy();
    }
    return result;
  }

  /// Send a [dtmf]
  Future<Map<dynamic, dynamic>> sendDtmf(String dtmf) async {
    if (dtmf.trim().isEmpty) return await reportInvalidValue('dtmf');
    final params = {
      'callId': this._id,
      'dtmf': dtmf.trim(),
      'uuid': _client.uuid
    };
    return await _client.methodChannel.invokeMethod('sendDtmf', params);
  }

  /// Send a call info
  Future<Map<dynamic, dynamic>> sendCallInfo(
      Map<dynamic, dynamic> callInfo) async {
    final params = {
      'callId': this._id,
      'callInfo': callInfo,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('sendCallInfo', params);
  }

  /// Get call stats
  Future<Map<dynamic, dynamic>> getCallStats() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };
    var result =
        await _client.methodChannel.invokeMethod('getCallStats', params);
    if (result['status']) {
      result['stats'] = new StringeeCallStats(result['stats']);
    }
    return result;
  }

  /// Mute/Unmute
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {
      'callId': this._id,
      'mute': mute,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('mute', params);
  }

  /// Enable/ Disable video
  Future<Map<dynamic, dynamic>> enableVideo(bool enableVideo) async {
    final params = {
      'callId': this._id,
      'enableVideo': enableVideo,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('enableVideo', params);
  }

  /// Set speaker phone on/off
  Future<Map<dynamic, dynamic>> setSpeakerphoneOn(bool speakerPhoneOn) async {
    final params = {
      'callId': this._id,
      'speaker': speakerPhoneOn,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel
        .invokeMethod('setSpeakerphoneOn', params);
  }

  /// Switch camera
  Future<Map<dynamic, dynamic>> switchCamera({String? cameraId}) async {
    Map params = {
      'callId': this._id,
      'uuid': _client.uuid,
      if (cameraId != null) 'cameraId': cameraId,
    };
    return await _client.methodChannel.invokeMethod('switchCamera', params);
  }

  /// Resume local video
  Future<Map<dynamic, dynamic>> resumeVideo() async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      final params = {
        'callId': this._id,
        'uuid': _client.uuid,
      };
      return await _client.methodChannel.invokeMethod('resumeVideo', params);
    }
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
        'callId': this._id,
        'isLocal': isLocal,
        'isMirror': isMirror,
        'uuid': _client.uuid,
      };
      return await _client.methodChannel.invokeMethod('setMirror', params);
    }
  }

  /// Hold the call
  Future<Map<dynamic, dynamic>> hold() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('hold', params);
  }

  /// Un hold the call
  Future<Map<dynamic, dynamic>> unHold() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('unHold', params);
  }

  /// Transfer call to other user with userId
  Future<Map<dynamic, dynamic>> transferToUserId(String userId) async {
    if (userId.trim().isEmpty) return await reportInvalidValue('userId');
    final params = {
      'callId': this._id,
      'userId': userId,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('transferToUserId', params);
  }

  /// Transfer call to phone
  Future<Map<dynamic, dynamic>> transferToPhone(String from, String to) async {
    if (from.trim().isEmpty) return await reportInvalidValue('from');
    if (to.trim().isEmpty) return await reportInvalidValue('to');
    final params = {
      'callId': this._id,
      'from': from,
      'to': to,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('transferToPhone', params);
  }

  /// Set audio to bluetooth
  Future<Map<dynamic, dynamic>> setBluetoothScoOn(bool bluetoothScoOn) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      final params = {
        'callId': this._id,
        'bluetooth': bluetoothScoOn,
        'uuid': _client.uuid,
      };
      return await _client.methodChannel
          .invokeMethod('setBluetoothScoOn', params);
    }
  }

  /// Snap shot local
  Future<Map<dynamic, dynamic>> snapShot() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };
    var result = await _client.methodChannel.invokeMethod('snapShot', params);
    if (result['status']) {
      result['image'] = MemoryImage(Uint8List.fromList(result['image']));
    }
    return result;
  }


  /// Close event stream
  @deprecated
  void destroy() {
    _subscriber.cancel();
    if (!_eventStreamController.isClosed) _eventStreamController.close();
  }
}
