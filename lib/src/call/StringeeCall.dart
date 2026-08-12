import 'dart:async';
import 'dart:convert';
import 'dart:io';

import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// A first-generation Stringee voice or video call.
///
/// Create an instance for an outgoing call, or use [StringeeCall.fromCallInfo]
/// with data received from [StringeeClient]. Call [destroy] when the object is
/// no longer needed to release its event subscription.
class StringeeCall {
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

  /// The server-assigned call ID.
  String? get id => _id;

  /// The call serial number supplied by the native SDK.
  int? get serial => _serial;

  /// The caller identifier.
  String? get from => _from;

  /// The callee identifier.
  String? get to => _to;

  /// The display alias of the caller.
  String? get fromAlias => _fromAlias;

  /// The display alias of the callee.
  String? get toAlias => _toAlias;

  /// Whether this call negotiates video.
  bool get isVideoCall => _isVideoCall;

  /// The direction and endpoint type of the call.
  StringeeCallType? get callType => _callType;

  /// Custom call data returned by the application server.
  String? get customDataFromYourServer => _customDataFromYourServer;

  /// Emits [StringeeCallEvents] for this call.
  StreamController<dynamic> get eventStreamController => _eventStreamController;

  /// Creates an empty outgoing call associated with [client].
  StringeeCall(StringeeClient client) {
    _client = client;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  /// Creates a call from native [info], typically for an incoming call.
  StringeeCall.fromCallInfo(
      Map<dynamic, dynamic>? info, StringeeClient client) {
    this.initCallInfo(info);
    _client = client;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  /// Updates this object's metadata from a native call-info payload.
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
    if (map['nativeEventType'] == StringeeObjectEventType.call.index &&
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
      }
    }
  }

  /// Converts a native signaling-state payload into a Dart call event.
  void handleDidChangeSignalingState(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    StringeeSignalingState signalingState = StringeeValueParser.enumValue(
      StringeeSignalingState.values,
      map['code'],
      StringeeSignalingState.ended,
    );
    _eventStreamController.add({
      "eventType": StringeeCallEvents.didChangeSignalingState,
      "body": signalingState
    });
  }

  /// Converts a native media-state payload into a Dart call event.
  void handleDidChangeMediaState(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    StringeeMediaState mediaState = StringeeValueParser.enumValue(
      StringeeMediaState.values,
      map['code'],
      StringeeMediaState.disconnected,
    );
    _eventStreamController.add({
      "eventType": StringeeCallEvents.didChangeMediaState,
      "body": mediaState
    });
  }

  /// Forwards a native custom call-info payload to listeners.
  void handleDidReceiveCallInfo(Map<dynamic, dynamic> map) {
    String? callId = StringeeValueParser.toStringValue(map['callId']);
    if (callId != this._id) return;

    Map<dynamic, dynamic>? data = StringeeValueParser.toMap(map['info']);
    _eventStreamController.add(
        {"eventType": StringeeCallEvents.didReceiveCallInfo, "body": data});
  }

  /// Reports that this call was handled by another signed-in device.
  void handleDidHandleOnAnotherDevice(Map<dynamic, dynamic> map) {
    StringeeSignalingState signalingState = StringeeValueParser.enumValue(
      StringeeSignalingState.values,
      map['code'],
      StringeeSignalingState.ended,
    );
    _eventStreamController.add({
      "eventType": StringeeCallEvents.didHandleOnAnotherDevice,
      "body": signalingState
    });
  }

  /// Reports that the local video stream is ready to render.
  void handleDidReceiveLocalStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCallEvents.didReceiveLocalStream,
      "body": StringeeValueParser.toStringValue(map['callId'])
    });
  }

  /// Reports that the remote video stream is ready to render.
  void handleDidReceiveRemoteStream(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "eventType": StringeeCallEvents.didReceiveRemoteStream,
      "body": StringeeValueParser.toStringValue(map['callId'])
    });
  }

  /// Makes an outgoing call with custom [parameters].
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
        await StringeeClient.methodChannel.invokeMethod('makeCall', params);
    Map<dynamic, dynamic>? callInfo = results['callInfo'];
    this.initCallInfo(callInfo);

    final Map<String, dynamic> resultDatas = {
      'status': results['status'],
      'code': results['code'],
      'message': results['message']
    };
    return resultDatas;
  }

  /// Makes an outgoing call with typed [MakeCallParams].
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

  /// Sends ringing state before answering an incoming call.
  Future<Map<dynamic, dynamic>> initAnswer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('initAnswer', param);
  }

  /// Answers the current incoming call.
  Future<Map<dynamic, dynamic>> answer() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('answer', param);
  }

  /// Hangs up the current call.
  Future<Map<dynamic, dynamic>> hangup() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('hangup', param);
  }

  /// Rejects the current incoming call.
  Future<Map<dynamic, dynamic>> reject() async {
    final param = {'uuid': _client.uuid, 'callId': this._id};

    return await StringeeClient.methodChannel.invokeMethod('reject', param);
  }

  /// Sends DTMF digits during the current call.
  Future<Map<dynamic, dynamic>> sendDtmf(String dtmf) async {
    if (dtmf.trim().isEmpty) return await reportInvalidValue('dtmf');
    final params = {
      'callId': this._id,
      'dtmf': dtmf.trim(),
      'uuid': _client.uuid
    };
    return await StringeeClient.methodChannel.invokeMethod('sendDtmf', params);
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
        .invokeMethod('sendCallInfo', params);
  }

  /// Gets media statistics for the current call.
  Future<Map<dynamic, dynamic>> getCallStats() async {
    final params = {
      'callId': this._id,
      'uuid': _client.uuid,
    };

    return await StringeeClient.methodChannel
        .invokeMethod('getCallStats', params);
  }

  /// Mutes or unmutes the local audio stream.
  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final params = {
      'callId': this._id,
      'mute': mute,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel.invokeMethod('mute', params);
  }

  /// Enables or disables the local video stream.
  Future<Map<dynamic, dynamic>> enableVideo(bool enableVideo) async {
    final params = {
      'callId': this._id,
      'enableVideo': enableVideo,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('enableVideo', params);
  }

  /// Switches to another camera.
  Future<Map<dynamic, dynamic>> switchCamera({String? cameraId}) async {
    Map params = {
      'callId': this._id,
      'uuid': _client.uuid,
      if (cameraId != null) 'cameraId': cameraId,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('switchCamera', params);
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
          .invokeMethod('resumeVideo', params);
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
          .invokeMethod('setMirror', params);
    }
  }

  /// Cancels native event subscription and closes the call event stream.
  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
