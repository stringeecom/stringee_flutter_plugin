import 'dart:async';
import 'dart:convert';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:stringee_flutter_plugin/src/StringeeParameter.dart';
import 'package:stringee_flutter_plugin/src/messaging/Conversation.dart';
import 'package:stringee_flutter_plugin/src/messaging/Message.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeChange.dart';
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
    if (map['typeEvent'] == StringeeType.StringeeClient.index) {
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
        case 'didReceiveTopicMessage':
          _handleDidReceiveTopicMessageEvent(map['body']);
          break;
        case 'didReceiveChangeEvent':
          _handleReceiveChangeEvent(map['body']);
          break;
      }
    } else {
      eventStreamController.add(event);
    }
  }

  ///connect to StringeeClient by [token]
  Future<void> connect({@required String token}) async {
    assert(token != null);
    return await methodChannel.invokeMethod('connect', token);
  }

  ///disconnect from StringeeCLient
  Future<void> disconnect() async {
    return await methodChannel.invokeMethod('disconnect');
  }

  ///register push from Stringee by [deviceToken[
  Future<Map<dynamic, dynamic>> registerPush({@required String deviceToken}) async {
    assert(deviceToken != null);
    return await methodChannel.invokeMethod('registerPush', deviceToken);
  }

  ///unregister push from Stringee by [deviceToken[
  Future<Map<dynamic, dynamic>> unregisterPush({@required String deviceToken}) async {
    assert(deviceToken != null);
    return await methodChannel.invokeMethod('unregisterPush', deviceToken);
  }

  ///send a [CustomData]
  Future<Map<dynamic, dynamic>> sendCustomMessage({@required CustomData customData}) async {
    assert(customData != null);
    return await methodChannel.invokeMethod('sendCustomMessage', json.encode(customData));
  }

  ///create new [Conversation] with [CreateConvParam]
  Future<Map<dynamic, dynamic>> createConversation({@required CreateConvParam param}) async {
    assert(param != null);
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('createConversation', json.encode(param));
    if (result['status']) result['body'] = Conversation.fromJson(json.decode(result['body']));
    return result;
  }

******  ///get [Conversation] by [Conversation.id]
  Future<Map<dynamic, dynamic>> getConversationById(String convId) async {
    assert(convId != null);
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationById', convId);
    if (result['status']) result['body'] = Conversation.fromJson(result['body']);
    return result;
  }

  ///get conversation by participant id
  Future<Map<dynamic, dynamic>> getConversationByUserId(String userId) async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationByUserId', userId);
    result['body'] = Conversation.fromJson(result['body']);
    return result;
  }

  ///get conversation from server
  Future<Map<dynamic, dynamic>> getConversationFromServer(String convId) async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationFromServer', convId);
    result['body'] = Conversation.fromJson(result['body']);
    return result;
  }

  ///get local conversation
  Future<Map<dynamic, dynamic>> getLocalConversations() async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getLocalConversations');
    List<dynamic> list = result['body'];
    List<Conversation> conversations = [];
    for (int i = 0; i < list.length; i++) {
      conversations.add(Conversation.fromJson(list[i]));
    }
    result['body'] = conversations;
    return result;
  }

  ///get (count) last conversation
  Future<Map<dynamic, dynamic>> getLastConversation(int count) async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getLastConversation', count);
    List<dynamic> list = result['body'];
    List<Conversation> conversations = [];
    for (int i = 0; i < list.length; i++) {
      conversations.add(Conversation.fromJson(list[i]));
    }
    result['body'] = conversations;
    return result;
  }

  ///get (count) conversations before (dateTime)
  ///@param (int) count
  ///@param (int) dateTime
  Future<Map<dynamic, dynamic>> getConversationsBefore(Map<dynamic, dynamic> parameters) async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationsBefore', parameters);
    List<dynamic> list = result['body'];
    List<Conversation> conversations = [];
    for (int i = 0; i < list.length; i++) {
      conversations.add(Conversation.fromJson(list[i]));
    }
    result['body'] = conversations;
    return result;
  }

  ///get (count) conversations before (dateTime)
  ///@param (int) count
  ///@param (int) dateTime
  Future<Map<dynamic, dynamic>> getConversationsAfter(Map<dynamic, dynamic> parameters) async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('getConversationsAfter', parameters);
    List<dynamic> list = result['body'];
    List<Conversation> conversations = [];
    for (int i = 0; i < list.length; i++) {
      conversations.add(Conversation.fromJson(list[i]));
    }
    result['body'] = conversations;
    return result;
  }

  ///clear local database
  Future<Map<dynamic, dynamic>> clearDb() async {
    return await methodChannel.invokeMethod('clearDb');
  }

  ///block user with userId
  Future<Map<dynamic, dynamic>> blockUser(String userId) async {
    return await methodChannel.invokeMethod('blockUser', userId);
  }

  ///get count of unread conversation
  Future<Map<dynamic, dynamic>> getTotalUnread() async {
    return await methodChannel.invokeMethod('getTotalUnread');
  }

  void _handleDidConnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = true;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController
        .add({"typeEvent": StringeeClientEvents, "eventType": StringeeClientEvents.DidConnect, "body": null});
  }

  void _handleDidDisconnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = false;
    _isReconnecting = map['isReconnecting'];
    _eventStreamController
        .add({"typeEvent": StringeeClientEvents, "eventType": StringeeClientEvents.DidDisconnect, "body": null});
  }

  void _handleDidFailWithErrorEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidFailWithError,
      "message": map['message'],
      "code": map['code']
    });
  }

  void _handleRequestAccessTokenEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _eventStreamController
        .add({"typeEvent": StringeeClientEvents, "eventType": StringeeClientEvents.RequestAccessToken, "body": null});
  }

  void _handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic> map) {
    CustomData customData = CustomData(userId: map['from'], msg: json.decode(map['msg']));
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidReceiveCustomMessage,
      "body": customData
    });
  }

  void _handleDidReceiveTopicMessageEvent(Map<dynamic, dynamic> map) {
    TopicMessage topicMsg = TopicMessage(userId: map['from'], msg: json.decode(map['msg']));
    _eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidReceiveTopicMessage,
      "body": topicMsg
    });
  }

  void _handleIncomingCallEvent(Map<dynamic, dynamic> map) {
    StringeeCall call = StringeeCall.fromCallInfo(map);
    _eventStreamController
        .add({"typeEvent": StringeeClientEvents, "eventType": StringeeClientEvents.IncomingCall, "body": call});
  }

  void _handleIncomingCall2Event(Map<dynamic, dynamic> map) {
    StringeeCall2 call = StringeeCall2.fromCallInfo(map);
    _eventStreamController
        .add({"typeEvent": StringeeClientEvents, "eventType": StringeeClientEvents.IncomingCall2, "body": call});
  }

  void _handleReceiveChangeEvent(Map<dynamic, dynamic> map) {
    map['changeType'] = ChangeType.values[map['changeType']];
    map['objectType'] = ObjectType.values[map['objectType']];
    switch (map['objectType']) {
      case ObjectType.CONVERSATION:
        map['objects'] = new Conversation.fromJson((json.decode(map['objects']))[0]);
        break;
      case ObjectType.MESSAGE:
        map['objects'] = new Message.fromJson((json.decode(map['objects']))[0]);
        break;
    }
    StringeeChange stringeeChange = new StringeeChange(map['changeType'], map['objects']);
    eventStreamController.add({
      "typeEvent": StringeeClientEvents,
      "eventType": StringeeClientEvents.DidReceiveChange,
      "body": stringeeChange
    });
  }
}
