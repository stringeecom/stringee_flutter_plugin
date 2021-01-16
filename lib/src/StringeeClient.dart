import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeConversation.dart';
import 'package:stringee_flutter_plugin/src/messaging/ConversationOption.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeMessage.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeChange.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeUser.dart';
import 'call/StringeeCall.dart';
import 'call/StringeeCall2.dart';
import 'StringeeConstants.dart';

class StringeeClient {
  static final StringeeClient _instance = StringeeClient._internal();

  static const MethodChannel methodChannel = MethodChannel('com.stringee.flutter.methodchannel');
  static const EventChannel eventChannel = EventChannel('com.stringee.flutter.eventchannel');
  StreamController<dynamic> _eventStreamController = StreamController.broadcast();

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

  ///send StringeeClient event
  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeType.StringeeClient.index) {
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
        case 'didReceiveChangeEvent':
          _handleReceiveChangeEvent(map['body']);
          break;
      }
    } else {
      eventStreamController.add(event);
    }
  }

  /// Connect to [StringeeClient] by [token]
  Future<Map<dynamic, dynamic>> connect(String token) async {
    if (token == null || token.trim().isEmpty) return await reportInvalidValue('token');
    return await methodChannel.invokeMethod('connect', token.trim());
  }

  /// Disconnect from [StringeeCLient]
  Future<Map<dynamic, dynamic>> disconnect() async {
    return await methodChannel.invokeMethod('disconnect');
  }

  /// Register push from Stringee by [deviceToken]
  Future<Map<dynamic, dynamic>> registerPush(String deviceToken) async {
    if (deviceToken == null || deviceToken.trim().isEmpty)
      return await reportInvalidValue('deviceToken');
    return await methodChannel.invokeMethod('registerPush', deviceToken.trim());
  }

  /// Unregister push from Stringee by [deviceToken[
  Future<Map<dynamic, dynamic>> unregisterPush(String deviceToken) async {
    if (deviceToken == null || deviceToken.trim().isEmpty)
      return await reportInvalidValue('deviceToken');
    return await methodChannel.invokeMethod('unregisterPush', deviceToken.trim());
  }

  /// Send a [customData] to [userId]
  Future<Map<dynamic, dynamic>> sendCustomMessage(
      String userId, Map<dynamic, dynamic> customData) async {
    if (userId == null || userId.trim().isEmpty) return await reportInvalidValue('userId');
    if (customData == null) return await reportInvalidValue('customData');
    final params = {
      'userId': userId.trim(),
      'msg': customData,
    };
    return await methodChannel.invokeMethod('sendCustomMessage', params);
  }

  /// Create new [StringeeConversation] with [options] and [participants]
  Future<Map<dynamic, dynamic>> createConversation(
      ConversationOption options, List<User> participants) async {
    if (participants == null || participants.length == 0)
      return await reportInvalidValue('participants');
    if (options == null) return await reportInvalidValue('options');
    final params = {
      'participants': json.encode(participants),
      'option': json.encode(options),
    };
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('createConversation', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(json.decode(result['body']));
    return result;
  }

  /// Get [StringeeConversation] with [StringeeConversation.id] = [convId]
  Future<Map<dynamic, dynamic>> getConversationById(String convId) async {
    if (convId == null || convId.trim().isEmpty) return await reportInvalidValue('convId');
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('getConversationById', convId.trim());
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(json.decode(result['body']));
    return result;
  }

  /// Get [StringeeConversation] by [userId] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationByUserId(String userId) async {
    if (userId == null || userId.trim().isEmpty) return await reportInvalidValue('convId');
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('getConversationByUserId', userId.trim());
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(json.decode(result['body']));
    return result;
  }

  /// Get local [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLocalConversations() async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getLocalConversations');
    if (result['status']) {
      List<dynamic> list = json.decode(result['body']);
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i]));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of lastest [StringeeConversation] from Stringee server
  Future<Map<dynamic, dynamic>> getLastConversation(int count) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getLastConversation', count);
    if (result['status']) {
      List<dynamic> list = json.decode(result['body']);
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i]));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] before [datetime] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationsBefore(int count, int datetime) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (datetime == null || datetime <= 0) return await reportInvalidValue('datetime');
    final param = {
      'count': count,
      'datetime': datetime,
    };
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('getConversationsBefore', param);
    if (result['status']) {
      List<dynamic> list = json.decode(result['body']);
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i]));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] after [milliseconds] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationsAfter(int count, int datetime) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (datetime == null || datetime <= 0) return await reportInvalidValue('datetime');
    final param = {
      'count': count,
      'datetime': datetime,
    };
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationsAfter', param);
    if (result['status']) {
      List<dynamic> list = json.decode(result['body']);
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i]));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Clear local database
  Future<Map<dynamic, dynamic>> clearDb() async {
    return await methodChannel.invokeMethod('clearDb');
  }

  /// Get total of unread [StringeeConversation]
  Future<Map<dynamic, dynamic>> getTotalUnread() async {
    return await methodChannel.invokeMethod('getTotalUnread');
  }

  void _handleDidConnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = true;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidConnect,
      "body": null
    });
  }

  void _handleDidDisconnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = false;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidDisconnect,
      "body": null
    });
  }

  void _handleDidFailWithErrorEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    Map<dynamic, dynamic> bodyMap = {
      'code': map['code'],
      'message': map['message'],
    };
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidFailWithError,
      "body": bodyMap,
    });
  }

  void _handleRequestAccessTokenEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.RequestAccessToken,
      "body": null
    });
  }

  void _handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic> map) {
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidReceiveCustomMessage,
      "body": map
    });
  }

  void _handleIncomingCallEvent(Map<dynamic, dynamic> map) {
    StringeeCall call = StringeeCall.fromCallInfo(map);
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.IncomingCall,
      "body": call
    });
  }

  void _handleIncomingCall2Event(Map<dynamic, dynamic> map) {
    StringeeCall2 call = StringeeCall2.fromCallInfo(map);
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.IncomingCall2,
      "body": call
    });
  }

  void _handleReceiveChangeEvent(Map<dynamic, dynamic> map) {
    map['changeType'] = ChangeType.values[map['changeType']];
    map['objectType'] = ObjectType.values[map['objectType']];
    switch (map['objectType']) {
      case ObjectType.CONVERSATION:
        map['objects'] = new StringeeConversation.fromJson((json.decode(map['objects']))[0]);
        break;
      case ObjectType.MESSAGE:
        map['objects'] = new StringeeMessage.fromJson((json.decode(map['objects']))[0]);
        break;
    }
    StringeeChange stringeeChange = new StringeeChange(map['changeType'], map['objects']);
    eventStreamController
        .add({"eventType": StringeeClientEvents.DidReceiveChange, "body": stringeeChange});
  }
}
