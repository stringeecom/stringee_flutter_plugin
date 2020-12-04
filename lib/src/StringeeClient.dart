import 'dart:async';
import 'package:flutter/services.dart';
import 'StringeeCall.dart';

enum StringeeClientEventType {
  DidConnect,
  DidDisconnect,
  DidFailWithError,
  RequestAccessToken,
  DidReceiveCustomMessage,
  DidReceiveTopicMessage,
  IncomingCall
}

class StringeeClient {
  static final StringeeClient _instance = StringeeClient._internal();

  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.methodchannel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.eventchannel');
  StreamController<dynamic> _eventStreamController =
      StreamController.broadcast();

  String _userId;
  String _projectId;
  bool _hasConnected = false;
  bool _isReconnecting = false;

  String get userId => _userId;

  String get projectId => _projectId;

  bool get hasConnected => _hasConnected;

  bool get isReconnecting => _isReconnecting;

  StreamController<dynamic> get eventStreamController => _eventStreamController;

  factory StringeeClient() {
    return _instance;
  }

  StringeeClient._internal() {
    eventChannel.receiveBroadcastStream().listen(this._listener);
  }

  Future<void> connect(String token) async {
    assert(token != null);
    return await methodChannel.invokeMethod('connect', token);
  }

  Future<void> disconnect() async {
    return await methodChannel.invokeMethod('disconnect');
  }

  Future<Map<dynamic, dynamic>> registerPush(
      Map<dynamic, dynamic> parameters) async {
    return await methodChannel.invokeMethod('registerPush', parameters);
  }

  Future<Map<dynamic, dynamic>> unregisterPush(String deviceToken) async {
    return await methodChannel.invokeMethod('unregisterPush', deviceToken);
  }

  Future<Map<dynamic, dynamic>> sendCustomMessage(
      Map<dynamic, dynamic> parameters) async {
    return await methodChannel.invokeMethod('sendCustomMessage', parameters);
  }

  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    // if (map.containsKey('eventCode')) {
    //   StringeeEvent _stringeeEvent = StringeeEvent.values[map['eventCode']];
    //   switch (_stringeeEvent) {
    //     case StringeeEvent.StringeeClientEventType:
    switch (map['event']) {
      case 'didConnect':
        _handleDidConnectEvent(map['body']);
        break;
      case 'didDisconnect':
        _handleDidDisconnectEvent(map['body']);
        break;
      case 'didFailWithError':
        _handleDidFailWithErrorEvent(map['body']);
        break;
      case 'requestAccessToken':
        _handleRequestAccessTokenEvent(map['body']);
        break;
      case 'didReceiveCustomMessage':
        _handleDidReceiveCustomMessageEvent(map['body']);
        break;
      case 'incomingCall':
        _handleIncomingCallEvent(map['body']);
        break;
      case 'didReceiveTopicMessage':
        _handleDidReceiveTopicMessageEvent(map['body']);
        break;
      default:
        _eventStreamController.add(event);
        break;
    }
    //     break;
    //   case StringeeEvent.StringeeCallEventType:
    //     _eventStreamController.add(event);
    //     break;
    //   case StringeeEvent.StringeeCall2EventType:
    //     _eventStreamController.add(event);
    //     break;
    // }
    // }
  }

  void _handleDidConnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = true;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController
        .add({"eventType": StringeeClientEventType.DidConnect, "body": null});
  }

  void _handleDidDisconnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = false;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController.add(
        {"eventType": StringeeClientEventType.DidDisconnect, "body": null});
  }

  void _handleDidFailWithErrorEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController.add({
      "eventType": StringeeClientEventType.DidFailWithError,
      "message": map['message'],
      "code": map['code']
    });
  }

  void _handleRequestAccessTokenEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController.add({
      "eventType": StringeeClientEventType.RequestAccessToken,
      "body": null
    });
  }

  void _handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic> map) {
    Map<dynamic, dynamic> body = {
      "from": map['fromUserId'],
      "message": map['infor']
    };
    _eventStreamController.add({
      "eventType": StringeeClientEventType.DidReceiveCustomMessage,
      "body": body
    });
  }

  void _handleDidReceiveTopicMessageEvent(Map<dynamic, dynamic> map) {
    Map<dynamic, dynamic> body = {
      "from": map['fromUserId'],
      "message": map['infor']
    };
    _eventStreamController.add({
      "eventType": StringeeClientEventType.DidReceiveTopicMessage,
      "body": body
    });
  }

  void _handleIncomingCallEvent(Map<dynamic, dynamic> map) {
    StringeeCall call = StringeeCall.fromCallInfo(map);
    _eventStreamController
        .add({"eventType": StringeeClientEventType.IncomingCall, "body": call});
  }
}
