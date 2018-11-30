import 'dart:async';

import 'StringeeClient.dart';

enum StringeeCallEventType {
  DidChangeSignalingState,

  DidChangeMediaState,

  DidReceiveDtmfDigit,

  DidReceiveCallInfo,

  DidHandleOnAnotherDevice
}

enum StringeeCallType {
  AppToAppOutgoing,

  AppToAppIncoming,

  AppToPhone,

  PhoneToApp
}

enum StringeeSignalingState {
  Calling,
  Ringing,
  Answered,
  Busy,
  Ended
}

enum StringeeMediaState {
  Connected,
  Disconnected
}

class StringeeCall {
  String _id;
  String _from;
  String _to;
  String _fromAlias;
  String _toAlias;
  StringeeCallType _callType;
  String _customDataFromYourServer;
  StreamController<dynamic> _eventStreamController = StreamController();
  StreamSubscription<dynamic> _subscriber;

  String get id => _id;
  String get from => _from;
  String get to => _to;
  String get fromAlias => _fromAlias;
  String get toAlias => _toAlias;
  StringeeCallType get callType => _callType;
  String get customDataFromYourServer => _customDataFromYourServer;
  StreamController<dynamic> get eventStreamController => _eventStreamController;

  StringeeCall() {
    _subscriber = StringeeClient().eventStreamController.stream.listen(this._listener);
  }

  StringeeCall.fromCallInfo(Map<dynamic, dynamic> info) {
    this.initFromInfo(info);
    _subscriber = StringeeClient().eventStreamController.stream.listen(this._listener);
  }

  void initFromInfo(Map<dynamic, dynamic> callInfo) {
    this._id = callInfo['callId'];
    this._from = callInfo['from'];
    this._to = callInfo['to'];
    this._fromAlias = callInfo['fromAlias'];
    this._toAlias = callInfo['toAlias'];
    this._customDataFromYourServer = callInfo['customDataFromYourServer'];
    this._callType = StringeeCallType.values[callInfo['callType']];
  }

  void _listener(dynamic event) {
    assert(event != null);

    final Map<dynamic, dynamic> map = event;

    switch (map['event']) {
      case 'didChangeSignalingState':
        handleSignalingStateChange(map['body']);
        break;
      case 'didChangeMediaState':
        handleMediaStateChange(map['body']);
        break;
      case 'didReceiveDtmfDigit':
        handleDtmfDidReceive(map['body']);
        break;
      case 'didReceiveCallInfo':
        handleCallInfoDidReceive(map['body']);
        break;
      case 'didHandleOnAnotherDevice':
        handleAnotherDeviceHadHandle(map['body']);
        break;
      default:
        break;
    }
  }

  void handleSignalingStateChange(Map<dynamic, dynamic> map) {
    String callId = map['callId'];
    if (callId != this._id) return;

    StringeeSignalingState signalingState = StringeeSignalingState.values[map['code']];
    _eventStreamController.add({"eventType" : StringeeCallEventType.DidChangeSignalingState, "body" : signalingState});
  }

  void handleMediaStateChange(Map<dynamic, dynamic> map) {
    String callId = map['callId'];
    if (callId != this._id) return;

    StringeeMediaState mediaState = StringeeMediaState.values[map['code']];
    _eventStreamController.add({"eventType" : StringeeCallEventType.DidChangeMediaState, "body" : mediaState});
  }

  void handleDtmfDidReceive(Map<dynamic, dynamic> map) {
    String callId = map['callId'];
    if (callId != this._id) return;

    String dtmf = map['dtmf'];
    _eventStreamController.add({"eventType" : StringeeCallEventType.DidReceiveDtmfDigit, "body" : dtmf});
  }

  void handleCallInfoDidReceive(Map<dynamic, dynamic> map) {
    String callId = map['callId'];
    if (callId != this._id) return;

    Map<dynamic, dynamic> data = map['data'];
    _eventStreamController.add({"eventType" : StringeeCallEventType.DidReceiveDtmfDigit, "body" : data});
  }

  void handleAnotherDeviceHadHandle(Map<dynamic, dynamic> map) {
    String callId = map['callId'];
    if (callId != this._id) return;

    StringeeSignalingState signalingState = StringeeSignalingState.values[map['code']];
    String reason = map['reason'];
    _eventStreamController.add({"eventType" : StringeeCallEventType.DidReceiveDtmfDigit, "body" : signalingState});
  }

  //region Actions
  Future<Map<dynamic, dynamic>> makeCall(Map<dynamic, dynamic> parameters) async {
    Map<dynamic, dynamic> results = await StringeeClient.methodChannel.invokeMethod('makeCall', parameters);
    Map<dynamic, dynamic> callInfo = results['callInfo'];

    this.initFromInfo(callInfo);

    final Map<String, dynamic> resultDatas = {
      'status' : results['status'],
      'code' : results['code'],
      'message' : results['message']
    };

    return resultDatas;
  }

  Future<Map<dynamic, dynamic>> initAnswer() async {
    return await StringeeClient.methodChannel.invokeMethod('initAnswer', this._id);
  }

  Future<Map<dynamic, dynamic>> answer() async {
    return await StringeeClient.methodChannel.invokeMethod('answer', this._id);
  }

  Future<Map<dynamic, dynamic>> hangup() async {
    return await StringeeClient.methodChannel.invokeMethod('hangup', this._id);
  }

  Future<Map<dynamic, dynamic>> reject() async {
    return await StringeeClient.methodChannel.invokeMethod('reject', this._id);
  }

  Future<Map<dynamic, dynamic>> sendDtmf(String dtmf) async {
    final pram = {
      'callId' : this._id,
      'dtmf' : dtmf,
    };
    return await StringeeClient.methodChannel.invokeMethod('sendDtmf', pram);
  }

  Future<Map<dynamic, dynamic>> sendCallInfo(Map<dynamic, dynamic> callInfo) async {
    final pram = {
      'callId' : this._id,
      'callInfo' : callInfo,
    };
    return await StringeeClient.methodChannel.invokeMethod('sendCallInfo', pram);
  }

  Future<Map<dynamic, dynamic>> getCallStats() async {
    return await StringeeClient.methodChannel.invokeMethod('getCallStats');
  }

  Future<Map<dynamic, dynamic>> mute(bool mute) async {
    final pram = {
      'callId' : this._id,
      'mute' : mute,
    };
    return await StringeeClient.methodChannel.invokeMethod('mute', pram);
  }

  Future<Map<dynamic, dynamic>> setSpeakerphoneOn(bool on) async {
    final pram = {
      'callId' : this._id,
      'speaker' : on,
    };
    return await StringeeClient.methodChannel.invokeMethod('setSpeakerphoneOn', pram);
  }

  void destroy() {
    if (_subscriber != null) {
      _subscriber.cancel();
      _eventStreamController.close();
    }
  }

//endregion




}