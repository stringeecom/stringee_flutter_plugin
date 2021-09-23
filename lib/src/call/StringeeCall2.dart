import 'dart:async';
import 'dart:convert';
import 'dart:io';

import '../StringeeClient.dart';
import '../StringeeConstants.dart';

class StringeeCall2 {
  String? _id;
  int? _serial;
  String? _from;
  String? _to;
  String? _fromAlias;
  String? _toAlias;
  StringeeCallType? _callType;
  String? _customDataFromYourServer;
  bool _isVideoCall = false;
  StreamController<dynamic> _eventStreamController = StreamController();
  late StreamSubscription<dynamic> _subscriber;
  late StringeeClient _client;

  String? get id => _id;

  int? get serial => _serial;

  String? get from => _from;

  String? get to => _to;

  String? get fromAlias => _fromAlias;

  String? get toAlias => _toAlias;

  bool get isVideoCall => _isVideoCall;

  StringeeCallType? get callType => _callType;

  String? get customDataFromYourServer => _customDataFromYourServer;

  StreamController<dynamic> get eventStreamController => _eventStreamController;

  StringeeCall2(StringeeClient client) {
    _client = client;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  StringeeCall2.fromCallInfo(
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
    this._isVideoCall = callInfo['isVideoCall'];
    this._customDataFromYourServer = callInfo['customDataFromYourServer'];
    this._callType = StringeeCallType.values[callInfo['callType']];
  }

  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeObjectEventType.call2.index &&
        map['uuid'] == _client.uuid) {
      switch (map['event']) {
        case 'didChangeSignalingState':
          handleDidChangeSignalingState(map['body']);
          break;
        case 'didChangeMediaState':
          handleDidChangeMediaState(map['body']);
          break;
        case 'didReceiveCallInfo':
          handleDidReceiveCallInfo(map['body']);
          break;
        case 'didHandleOnAnotherDevice':
          handleDidHandleOnAnotherDevice(map['body']);
          break;
        case 'didReceiveLocalStream':
          handleDidReceiveLocalStream(map['body']);
          break;
        case 'didReceiveRemoteStream':
          handleDidReceiveRemoteStream(map['body']);
          break;
        case 'didChangeAudioDevice':
          handleDidChangeAudioDevice(map['body']);
          break;
      }
    }
  }

  void handleDidChangeSignalingState(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    StringeeSignalingState signalingState =
        StringeeSignalingState.values[map['code']];
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didChangeSignalingState,
      "body": signalingState
    });
  }

  void handleDidChangeMediaState(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    StringeeMediaState mediaState = StringeeMediaState.values[map['code']];
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didChangeMediaState,
      "body": mediaState
    });
  }

  void handleDidReceiveCallInfo(Map<dynamic, dynamic> map) {
    String? callId = map['callId'];
    if (callId != this._id) return;

    Map<dynamic, dynamic>? data = map['info'];
    _eventStreamController.add(
        {"eventType": StringeeCall2Events.didReceiveCallInfo, "body": data});
  }

  void handleDidHandleOnAnotherDevice(Map<dynamic, dynamic> map) {
    StringeeSignalingState signalingState =
        StringeeSignalingState.values[map['code']];
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didHandleOnAnotherDevice,
      "body": signalingState
    });
  }

  void handleDidReceiveLocalStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didReceiveLocalStream,
      "body": map['callId']
    });
  }

  void handleDidReceiveRemoteStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didReceiveRemoteStream,
      "body": map['callId']
    });
  }

  void handleDidChangeAudioDevice(Map<dynamic, dynamic> map) {
    AudioDevice selectedAudioDevice = AudioDevice.values[map['code']];
    List<dynamic> codeList = [];
    codeList.addAll(map['codeList']);
    List<AudioDevice> availableAudioDevices = [];
    for (int i = 0; i < codeList.length; i++) {
      AudioDevice audioDevice = AudioDevice.values[codeList[i]];
      availableAudioDevices.add(audioDevice);
    }
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didChangeAudioDevice,
      "selectedAudioDevice": selectedAudioDevice,
      "availableAudioDevices": availableAudioDevices
    });
  }

  /// Make a new coll with custom [parameters]
  Future<Map<dynamic, dynamic>> makeCall(
      Map<dynamic, dynamic> parameters) async {
    if (!parameters.containsKey('from') ||
        (parameters['from'] as String).trim().isEmpty ||
        !parameters.containsKey('to') ||
        (parameters['to'] as String).trim().isEmpty)
      return await reportInvalidValue('MakeCallParams');

    var params = {};

    params['from'] = (parameters['from'] as String).trim();
    params['to'] = (parameters['to'] as String).trim();
    if (parameters.containsKey('customData')) if (parameters['customData'] !=
        null) {
      if (parameters['customData'] is Map) {
        params['customData'] = json.encode(parameters['customData']);
      } else {
        params['customData'] = (parameters['customData'].toString()).trim();
      }
    }
    if (parameters.containsKey('isVideoCall')) {
      params['isVideoCall'] = (parameters['isVideoCall'] != null)
          ? parameters['isVideoCall']
          : false;
      if (params['isVideoCall']) {
        if (parameters['videoQuality'] != null) {
          switch (parameters['videoQuality']) {
            case VideoQuality.normal:
              params['videoQuality'] = "NORMAL";
              break;
            case VideoQuality.hd:
              params['videoQuality'] = "HD";
              break;
            case VideoQuality.fullHd:
              params['videoQuality'] = "FULLHD";
              break;
            default:
              params['videoQuality'] = "NORMAL";
              break;
          }
        } else {
          params['videoQuality'] = "NORMAL";
        }
      }
    }

    params['uuid'] = _client.uuid;

    Map<dynamic, dynamic> results =
        await StringeeClient.methodChannel.invokeMethod('makeCall2', params);
    Map<dynamic, dynamic>? callInfo = results['callInfo'];
    this.initCallInfo(callInfo);

    final Map<String, dynamic> resultDatas = {
      'status': results['status'],
      'code': results['code'],
      'message': results['message']
    };
    return resultDatas;
  }

  /// Make a new coll with [MakeCallParams]
  Future<Map<dynamic, dynamic>> makeCallFromParams(
      MakeCallParams params) async {
    Map<dynamic, dynamic> parameters = {
      'from': params.from!.trim(),
      'to': params.to!.trim(),
      if (params.customData != null) 'customData': params.customData,
      'isVideoCall': params.isVideoCall,
      if (params.isVideoCall!) 'videoQuality': params.videoQuality,
    };
    return await makeCall(parameters);
  }

  /// Init an answer from incoming call
  Future<Map<dynamic, dynamic>> initAnswer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel
        .invokeMethod('initAnswer2', param);
  }

  /// Answer a call
  Future<Map<dynamic, dynamic>> answer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('answer2', param);
  }

  /// Hang up a call
  Future<Map<dynamic, dynamic>> hangup() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('hangup2', param);
  }

  /// Reject a call
  Future<Map<dynamic, dynamic>> reject() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('reject2', param);
  }

  /// Send a [dtmf]
  Future<Map<dynamic, dynamic>> sendDtmf(String dtmf) async {
    if (dtmf.trim().isEmpty) return await reportInvalidValue('dtmf');
    final params = {
      'callId': this._id,
      'dtmf': dtmf.trim(),
      'uuid': _client.uuid
    };
    return await StringeeClient.methodChannel.invokeMethod('sendDtmf2', params);
  }

  /// Send a call info
  Future<Map<dynamic, dynamic>> sendCallInfo(
      Map<dynamic, dynamic> callInfo) async {
    final params = {
      'callId': this._id,
      'callInfo': callInfo,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('sendCallInfo2', params);
  }

  /// Get call stats
  Future<Map<dynamic, dynamic>> getCallStats() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };

    return await StringeeClient.methodChannel
        .invokeMethod('getCallStats2', params);
  }

  /// Mute/Unmute
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {'callId': this._id, 'mute': mute, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel.invokeMethod('mute2', params);
  }

  /// Enable/ Disable video
  Future<Map<dynamic, dynamic>> enableVideo(bool enableVideo) async {
    final params = {
      'callId': this._id,
      'enableVideo': enableVideo,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('enableVideo2', params);
  }

  /// Set speaker phone on/off
  Future<Map<dynamic, dynamic>> setSpeakerphoneOn(bool speakerPhoneOn) async {
    final params = {
      'callId': this._id,
      'speaker': speakerPhoneOn,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('setSpeakerphoneOn2', params);
  }

  /// Switch camera
  Future<Map<dynamic, dynamic>> switchCamera({int? cameraId}) async {
    Map params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };
    if (Platform.isAndroid) {
      if (cameraId != null) {
        params['cameraId'] = cameraId;
      }
    }
    return await StringeeClient.methodChannel
        .invokeMethod('switchCamera2', params);
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
      return await StringeeClient.methodChannel
          .invokeMethod('resumeVideo2', params);
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
      return await StringeeClient.methodChannel
          .invokeMethod('setMirror2', params);
    }
  }

  /// close event stream
  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
