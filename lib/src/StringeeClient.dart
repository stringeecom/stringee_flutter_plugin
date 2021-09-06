import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

import 'StringeeConstants.dart';
import 'call/StringeeCall.dart';
import 'call/StringeeCall2.dart';
import 'messaging/StringeeChatRequest.dart';

class StringeeClient {
  // Native
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.methodchannel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.eventchannel');

  // Flutter
  StreamController<dynamic> _eventStreamController =
      StreamController.broadcast();

  String? _userId;
  String? _projectId;
  bool _hasConnected = false;
  bool _isReconnecting = true;

  // Multi Client
  List<StringeeServerAddress>? _serverAddresses;
  final String _uuid = GUIDGen.generate();

  String? get userId => _userId;

  String? get projectId => _projectId;

  bool get hasConnected => _hasConnected;

  bool get isReconnecting => _isReconnecting;

  StreamController<dynamic> get eventStreamController => _eventStreamController;

  String get uuid => _uuid;

  StringeeClient(
      {List<StringeeServerAddress>? serverAddresses, String? baseAPIUrl}) {
    _serverAddresses = serverAddresses;

    final params = {'uuid': _uuid, 'baseAPIUrl': baseAPIUrl};

    // Config client
    methodChannel.invokeMapMethod('setupClient', params);

    // Xu ly su kien nhan duoc tu native
    eventChannel.receiveBroadcastStream().listen(this._listener);
  }

  ///send StringeeClient event
  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeObjectEventType.client.index &&
        map['uuid'] == _uuid) {
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
        case 'incomingCall2':
          _handleIncomingCall2Event(map['body']);
          break;

        case 'didReceiveChatRequest':
          _handleDidReceiveChatRequestEvent(map['body']);
          break;
        case 'didReceiveTransferChatRequest':
          _handleDidReceiveTransferChatRequestEvent(map['body']);
          break;
        case 'timeoutAnswerChat':
          _handleTimeoutAnswerChatEvent(map['body']);
          break;
        case 'timeoutInQueue':
          _handleTimeoutInQueueEvent(map['body']);
          break;
        case 'conversationEnded':
          _handleConversationEndedEvent(map['body']);
          break;
        case 'userBeginTyping':
          _handleUserBeginTypingEvent(map['body']);
          break;
        case 'userEndTyping':
          _handleUserEndTypingEvent(map['body']);
          break;
      }
    } else {
      _eventStreamController.add(event);
    }
  }

  /// Connect to [StringeeClient] by [token]
  Future<Map<dynamic, dynamic>> connect(String token) async {
    if (token.trim().isEmpty) return await reportInvalidValue('token');

    var params = {'token': token.trim(), 'uuid': _uuid};
    if (_serverAddresses != null && _serverAddresses!.length > 0) {
      params['serverAddresses'] = json.encode(_serverAddresses);
    }

    await methodChannel.invokeMethod('connect', params);
    Map<String, dynamic> rData = {
      'status': true,
      'code': 0,
      'message': 'Success',
    };
    return rData;
  }

  /// Disconnect from [StringeeCLient]
  Future<Map<dynamic, dynamic>> disconnect() async {
    final params = {'uuid': _uuid};

    return await methodChannel.invokeMethod('disconnect', params);
  }

  /// Register push from Stringee by [deviceToken]
  Future<Map<dynamic, dynamic>> registerPush(
    String deviceToken, {
    bool? isProduction,
    bool? isVoip,
  }) async {
    if (deviceToken.trim().isEmpty)
      return await reportInvalidValue('deviceToken');
    Map<dynamic, dynamic> params = {
      'deviceToken': deviceToken.trim(),
      'uuid': _uuid
    };
    if (Platform.isIOS) {
      bool paramIsProduction = isProduction != null ? isProduction : false;
      bool paramsIsVoip = isVoip != null ? isVoip : true;
      params['isProduction'] = paramIsProduction;
      params['isVoip'] = paramsIsVoip;
    }
    return await methodChannel.invokeMethod('registerPush', params);
  }

  /// Unregister push from Stringee by [deviceToken[
  Future<Map<dynamic, dynamic>> unregisterPush(String deviceToken) async {
    if (deviceToken.trim().isEmpty)
      return await reportInvalidValue('deviceToken');

    final params = {
      'deviceToken': deviceToken.trim(),
      'uuid': _uuid,
    };

    return await methodChannel.invokeMethod('unregisterPush', params);
  }

  /// Send a [customData] to [userId]
  Future<Map<dynamic, dynamic>> sendCustomMessage(
      String userId, Map<dynamic, dynamic> customData) async {
    if (userId.trim().isEmpty) return await reportInvalidValue('userId');
    final params = {'userId': userId.trim(), 'msg': customData, 'uuid': _uuid};
    return await methodChannel.invokeMethod('sendCustomMessage', params);
  }

  void _handleDidConnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = true;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController
        .add({"eventType": StringeeClientEvents.didConnect, "body": null});
  }

  void _handleDidDisconnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = false;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController
        .add({"eventType": StringeeClientEvents.didDisconnect, "body": null});
  }

  void _handleDidFailWithErrorEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    Map<dynamic, dynamic> bodyMap = {
      'code': map['code'],
      'message': map['message'],
    };
    _eventStreamController.add({
      "eventType": StringeeClientEvents.didFailWithError,
      "body": bodyMap,
    });
  }

  void _handleRequestAccessTokenEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController.add(
        {"eventType": StringeeClientEvents.requestAccessToken, "body": null});
  }

  void _handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic>? map) {
    _eventStreamController.add({
      "eventType": StringeeClientEvents.didReceiveCustomMessage,
      "body": map
    });
  }

  void _handleIncomingCallEvent(Map<dynamic, dynamic>? map) {
    StringeeCall call = StringeeCall.fromCallInfo(map, this);
    _eventStreamController
        .add({"eventType": StringeeClientEvents.incomingCall, "body": call});
  }

  void _handleIncomingCall2Event(Map<dynamic, dynamic>? map) {
    StringeeCall2 call = StringeeCall2.fromCallInfo(map, this);
    _eventStreamController
        .add({"eventType": StringeeClientEvents.incomingCall2, "body": call});
  }

  void _handleDidReceiveChatRequestEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    _eventStreamController.add({
      "eventType": StringeeClientEvents.didReceiveChatRequest,
      "body": request
    });
  }

  void _handleDidReceiveTransferChatRequestEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    _eventStreamController.add({
      "eventType": StringeeClientEvents.didReceiveTransferChatRequest,
      "body": request
    });
  }

  void _handleTimeoutAnswerChatEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    _eventStreamController.add(
        {"eventType": StringeeClientEvents.timeoutAnswerChat, "body": request});
  }

  void _handleTimeoutInQueueEvent(Map<dynamic, dynamic> map) {
    _eventStreamController
        .add({"eventType": StringeeClientEvents.timeoutInQueue, "body": map});
  }

  void _handleConversationEndedEvent(Map<dynamic, dynamic> map) {
    _eventStreamController.add(
        {"eventType": StringeeClientEvents.conversationEnded, "body": map});
  }

  void _handleUserBeginTypingEvent(Map<dynamic, dynamic> map) {
    _eventStreamController
        .add({"eventType": StringeeClientEvents.userBeginTyping, "body": map});
  }

  void _handleUserEndTypingEvent(Map<dynamic, dynamic> map) {
    _eventStreamController
        .add({"eventType": StringeeClientEvents.userEndTyping, "body": map});
  }
}
