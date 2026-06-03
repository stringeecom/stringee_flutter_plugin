import 'dart:async';
import 'dart:convert';
import 'dart:io';

import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

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

    this._id = StringeeValueParser.toStringValue(callInfo['callId']);
    this._serial = StringeeValueParser.toInt(callInfo['serial']);
    this._from = StringeeValueParser.toStringValue(callInfo['from']);
    this._to = StringeeValueParser.toStringValue(callInfo['to']);
    this._fromAlias = StringeeValueParser.toStringValue(callInfo['fromAlias']);
    this._toAlias = StringeeValueParser.toStringValue(callInfo['toAlias']);
    this._isVideoCall =
        StringeeValueParser.toBool(callInfo['isVideoCall']) ?? false;
    this._customDataFromYourServer = StringeeValueParser.toStringValue(
      callInfo['customDataFromYourServer'],
    );
    this._callType = StringeeValueParser.enumValue(
      StringeeCallType.values,
      callInfo['callType'],
      StringeeCallType.appToAppOutgoing,
    );
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
        case 'didAddVideoTrack':
          handleDidAddVideoTrack(map['body']);
          break;
        case 'didRemoveVideoTrack':
          handleDidRemoveVideoTrack(map['body']);
          break;
      }
    }
  }

  void handleDidChangeSignalingState(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    StringeeSignalingState signalingState = StringeeValueParser.enumValue(
      StringeeSignalingState.values,
      map['code'],
      StringeeSignalingState.ended,
    );
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didChangeSignalingState,
      "body": signalingState
    });
  }

  void handleDidChangeMediaState(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    StringeeMediaState mediaState = StringeeValueParser.enumValue(
      StringeeMediaState.values,
      map['code'],
      StringeeMediaState.disconnected,
    );
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didChangeMediaState,
      "body": mediaState
    });
  }

  void handleDidReceiveCallInfo(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    Map<dynamic, dynamic>? data = StringeeValueParser.toMap(map['info']);
    _eventStreamController.add(
        {"eventType": StringeeCall2Events.didReceiveCallInfo, "body": data});
  }

  void handleDidHandleOnAnotherDevice(Map<dynamic, dynamic> map) {
    StringeeSignalingState signalingState = StringeeValueParser.enumValue(
      StringeeSignalingState.values,
      map['code'],
      StringeeSignalingState.ended,
    );
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didHandleOnAnotherDevice,
      "body": signalingState
    });
  }

  void handleDidReceiveLocalStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didReceiveLocalStream,
      "body": StringeeValueParser.toStringValue(map['callId'])
    });
  }

  void handleDidReceiveRemoteStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didReceiveRemoteStream,
      "body": StringeeValueParser.toStringValue(map['callId'])
    });
  }

  void handleDidAddVideoTrack(Map<dynamic, dynamic> map) {
    StringeeVideoTrack videoTrack = StringeeVideoTrack(
      _client,
      StringeeValueParser.toMap(map['videoTrack']) ?? {},
    );
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didAddVideoTrack,
      "body": videoTrack
    });
  }

  void handleDidRemoveVideoTrack(Map<dynamic, dynamic> map) {
    StringeeVideoTrack videoTrack = StringeeVideoTrack(
      _client,
      StringeeValueParser.toMap(map['videoTrack']) ?? {},
    );
    _eventStreamController.add({
      "eventType": StringeeCall2Events.didRemoveVideoTrack,
      "body": videoTrack
    });
  }

  /// Makes an outgoing Call2 call with custom [parameters].
  Future<Map<dynamic, dynamic>> makeCall(
      Map<dynamic, dynamic> parameters) async {
    final from = StringeeValueParser.toStringValue(parameters['from'])?.trim();
    final to = StringeeValueParser.toStringValue(parameters['to'])?.trim();
    if (from == null || from.isEmpty || to == null || to.isEmpty)
      return await reportInvalidValue('MakeCallParams');

    var params = {};

    params['from'] = from;
    params['to'] = to;
    if (parameters.containsKey('customData')) if (parameters['customData'] !=
        null) {
      if (parameters['customData'] is Map) {
        params['customData'] = json.encode(parameters['customData']);
      } else {
        params['customData'] = (parameters['customData'].toString()).trim();
      }
    }
    if (parameters.containsKey('isVideoCall')) {
      params['isVideoCall'] =
          StringeeValueParser.toBool(parameters['isVideoCall']) ?? false;
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

  /// Makes an outgoing Call2 call with typed [MakeCallParams].
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

  /// Sends ringing state before answering an incoming Call2 call.
  Future<Map<dynamic, dynamic>> initAnswer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel
        .invokeMethod('initAnswer2', param);
  }

  /// Answers the current incoming Call2 call.
  Future<Map<dynamic, dynamic>> answer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('answer2', param);
  }

  /// Hangs up the current Call2 call.
  Future<Map<dynamic, dynamic>> hangup() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('hangup2', param);
  }

  /// Rejects the current incoming Call2 call.
  Future<Map<dynamic, dynamic>> reject() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('reject2', param);
  }

  /// Sends DTMF digits during the current Call2 call.
  Future<Map<dynamic, dynamic>> sendDtmf(String dtmf) async {
    if (dtmf.trim().isEmpty) return await reportInvalidValue('dtmf');
    final params = {
      'callId': this._id,
      'dtmf': dtmf.trim(),
      'uuid': _client.uuid
    };
    return await StringeeClient.methodChannel.invokeMethod('sendDtmf2', params);
  }

  /// Sends custom [callInfo] to the remote participant.
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

  /// Gets media statistics for the current Call2 call.
  Future<Map<dynamic, dynamic>> getCallStats() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };

    return await StringeeClient.methodChannel
        .invokeMethod('getCallStats2', params);
  }

  /// Mutes or unmutes the local audio stream.
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {'callId': this._id, 'mute': mute, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel.invokeMethod('mute2', params);
  }

  /// Enables or disables the local video stream.
  Future<Map<dynamic, dynamic>> enableVideo(bool enableVideo) async {
    final params = {
      'callId': this._id,
      'enableVideo': enableVideo,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('enableVideo2', params);
  }

  /// Switches to another camera.
  Future<Map<dynamic, dynamic>> switchCamera({String? cameraId}) async {
    Map params = {
      'callId': this._id,
      'uuid': _client.uuid,
      if (cameraId != null) 'cameraId': cameraId,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('switchCamera2', params);
  }

  /// Resumes the local video stream on Android.
  Future<Map<dynamic, dynamic>> resumeVideo() async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function works only for Android",
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

  /// Sets mirror mode for the local or remote video renderer on Android.
  Future<Map<dynamic, dynamic>> setMirror(bool isLocal, bool isMirror) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function works only for Android",
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

  /// Cancels native event subscription and closes the call event stream.
  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
