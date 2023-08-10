import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

import '../stringee_flutter_plugin.dart';

class StringeeClient {
  // Native
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.methodchannel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.eventchannel');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  // Flutter
  @Deprecated('')
  StreamController<dynamic> _eventStreamController =
      StreamController.broadcast();

  String? _userId;
  String? _projectId;
  bool _hasConnected = false;
  bool _isReconnecting = true;
  StringeeClientListener? _clientListener;
  ChatEvent? _chatEvent;

  // Multi Client
  List<StringeeServerAddress>? _serverAddresses;
  final String _uuid = GUIDGen.generate();

  String? get userId => _userId;

  String? get projectId => _projectId;

  bool get hasConnected => _hasConnected;

  bool get isReconnecting => _isReconnecting;

  @Deprecated('')
  StreamController<dynamic> get eventStreamController => _eventStreamController;

  String get uuid => _uuid;

  StringeeClient(
      {List<StringeeServerAddress>? serverAddresses, String? baseAPIUrl}) {
    _serverAddresses = serverAddresses;

    final params = {'uuid': _uuid, 'baseAPIUrl': baseAPIUrl};

    // Config client
    methodChannel.invokeMapMethod('setupClient', params);

    // Xu ly su kien nhan duoc tu native
    broadcastStream.listen(this._listener);
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
    } else if (map['nativeEventType'] == StringeeObjectEventType.chat.index &&
        map['uuid'] == _uuid) {
      switch (map['event']) {
        case 'didReceiveChangeEvent':
          _handleReceiveChangeEvent(map['body']);
          break;
      }
    } else {
      _eventStreamController.add(event);
    }
  }

  void registerEvent(StringeeClientListener clientListener) {
    _clientListener = clientListener;
  }

  void getChatEvent(ChatEvent chatEvent) {
    _chatEvent = chatEvent;
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

  /// Register push from Stringee by [deviceToken] and delete another [deviceToken] of other package by [packageNames]
  Future<Map<dynamic, dynamic>> registerPushAndDeleteOthers(
    String deviceToken,
    List<String> packageNames, {
    bool? isProduction,
    bool? isVoip,
  }) async {
    if (deviceToken.trim().isEmpty)
      return await reportInvalidValue('deviceToken');
    if (packageNames.length == 0)
      return await reportInvalidValue('packageNames');
    Map<dynamic, dynamic> params = {
      'deviceToken': deviceToken.trim(),
      'packageNames': packageNames,
      'uuid': _uuid
    };
    if (Platform.isIOS) {
      bool paramIsProduction = isProduction != null ? isProduction : false;
      bool paramsIsVoip = isVoip != null ? isVoip : true;
      params['isProduction'] = paramIsProduction;
      params['isVoip'] = paramsIsVoip;
    }
    return await methodChannel.invokeMethod(
        'registerPushAndDeleteOthers', params);
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

  /// ====================== BEGIN LIVE CHAT =======================

  /// Get chat profile which contain portal info and list of queues
  Future<Map<dynamic, dynamic>> getChatProfile(String key) async {
    if (key.trim().isEmpty) return await reportInvalidValue('key');

    final params = {'key': key.trim(), 'uuid': _uuid};

    return await StringeeClient.methodChannel
        .invokeMethod('getChatProfile', params);
  }

  /// Get live-chat token
  Future<Map<dynamic, dynamic>> getLiveChatToken(
    String key,
    String name,
    String email,
  ) async {
    if (key.trim().isEmpty) return await reportInvalidValue('key');
    if (name.trim().isEmpty) return await reportInvalidValue('name');
    if (email.trim().isEmpty) return await reportInvalidValue('email');

    final params = {
      'key': key.trim(),
      'name': name.trim(),
      'email': email.trim(),
      'uuid': _uuid,
    };

    return await StringeeClient.methodChannel
        .invokeMethod('getLiveChatToken', params);
  }

  /// Update user info
  Future<Map<dynamic, dynamic>> updateUserInfo(
      {String? name, String? email, String? avatar, String? phone}) async {
    final params = {
      if (name != null) 'name': name.trim(),
      if (email != null) 'email': email.trim(),
      if (avatar != null) 'avatar': avatar.trim(),
      if (phone != null) 'phone': phone.trim(),
      'uuid': _uuid
    };

    return await StringeeClient.methodChannel
        .invokeMethod('updateUserInfo', params);
  }

  /// Create live-chat [StringeeConversation]
  Future<Map<dynamic, dynamic>> createLiveChatConversation(
      String queueId) async {
    if (queueId.trim().isEmpty) return await reportInvalidValue('queueId');

    final params = {
      'queueId': queueId,
      'uuid': _uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('createLiveChatConversation', params);
    if (result['status']) {
      result['body'] = StringeeConversation.fromJson(result['body'], this);
    }
    return result;
  }

  /// Create live-chat ticket
  Future<Map<dynamic, dynamic>> createLiveChatTicket(
      String key, String name, String email, String description) async {
    if (key.trim().isEmpty) return await reportInvalidValue('key');
    if (name.trim().isEmpty) return await reportInvalidValue('name');
    if (email.trim().isEmpty) return await reportInvalidValue('email');

    final params = {
      'key': key.trim(),
      'name': name.trim(),
      'email': email.trim(),
      'description': description.trim(),
      'uuid': _uuid
    };

    return await StringeeClient.methodChannel
        .invokeMethod('createLiveChatTicket', params);
  }

  /// ====================== END LIVE CHAT =======================

  /// Create new [StringeeConversation] with [options] and [participants]
  Future<Map<dynamic, dynamic>> createConversation(
      StringeeConversationOption options,
      List<StringeeUser> participants) async {
    if (participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'participants': json.encode(participants),
      'option': json.encode(options),
      'uuid': _uuid
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('createConversation', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], this);
    return result;
  }

  /// Get [StringeeConversation] with [StringeeConversation.id] = [convId]
  Future<Map<dynamic, dynamic>> getConversationById(String convId) async {
    if (convId.trim().isEmpty) return await reportInvalidValue('convId');

    final params = {'convId': convId.trim(), 'uuid': _uuid};

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getConversationById', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], this);
    return result;
  }

  /// Get [StringeeConversation] by [userId] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationByUserId(String userId) async {
    if (userId.trim().isEmpty) return await reportInvalidValue('convId');

    final params = {'userId': userId.trim(), 'uuid': _uuid};

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getConversationByUserId', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], this);
    return result;
  }

  /// Get local [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLocalConversations({String? oaId}) async {
    final params = {
      if (oaId != null) 'oaId': oaId,
      'uuid': _uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getLocalConversations', params);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], this));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of lastest [StringeeConversation] from Stringee server
  Future<Map<dynamic, dynamic>> getLastConversation(
    int count, {
    String? oaId,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');

    final param = {
      'count': count,
      if (oaId != null) 'oaId': oaId,
      'uuid': _uuid
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getLastConversation', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], this));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] before [datetime] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationsBefore(
    int count,
    int datetime, {
    String? oaId,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');
    if (datetime <= 0) return await reportInvalidValue('datetime');
    final param = {
      'count': count,
      'datetime': datetime,
      if (oaId != null) 'oaId': oaId,
      'uuid': _uuid
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getConversationsBefore', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], this));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] after [datetime] from Stringee server
  Future<Map<dynamic, dynamic>> getConversationsAfter(
    int count,
    int datetime, {
    String? oaId,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');
    if (datetime <= 0) return await reportInvalidValue('datetime');
    final param = {
      'count': count,
      'datetime': datetime,
      if (oaId != null) 'oaId': oaId,
      'uuid': _uuid
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('getConversationsAfter', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], this));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Clear local database
  Future<Map<dynamic, dynamic>> clearDb() async {
    final param = {'uuid': _uuid};

    return await StringeeClient.methodChannel.invokeMethod('clearDb', param);
  }

  /// Get total of unread [StringeeConversation]
  Future<Map<dynamic, dynamic>> getTotalUnread() async {
    final param = {'uuid': _uuid};

    return await StringeeClient.methodChannel
        .invokeMethod('getTotalUnread', param);
  }

  /// Join Oa [StringeeConversation]
  Future<Map<dynamic, dynamic>> joinOaConversation(String convId) async {
    final params = {
      'convId': convId,
      'uuid': _uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('joinOaConversation', params);
  }

  /// Begin handle events

  void _handleDidConnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = true;
    _isReconnecting = map['isReconnecting'];
    if (_clientListener != null) {
      _clientListener!.onConnect(_userId!);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.didConnect, "body": null});
    }
  }

  void _handleDidDisconnectEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    _projectId = map['projectId'];
    _hasConnected = false;
    _isReconnecting = map['isReconnecting'];
    if (_clientListener != null) {
      _clientListener!.onDisconnect();
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.didDisconnect, "body": null});
    }
  }

  void _handleDidFailWithErrorEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    Map<dynamic, dynamic> bodyMap = {
      'code': map['code'],
      'message': map['message'],
    };
    if (_clientListener != null) {
      _clientListener!.onFailWithError(map['code'], map['message']);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeClientEvents.didFailWithError,
        "body": bodyMap,
      });
    }
  }

  void _handleRequestAccessTokenEvent(Map<dynamic, dynamic> map) {
    _userId = map['userId'];
    if (_clientListener != null) {
      _clientListener!.onRequestAccessToken();
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeClientEvents.requestAccessToken, "body": null});
    }
  }

  void _handleIncomingCallEvent(Map<dynamic, dynamic>? map) {
    StringeeCall call = StringeeCall.fromCallInfo(map, this);
    if (_clientListener != null) {
      if (_clientListener!.onIncomingCall != null) {
        _clientListener!.onIncomingCall!(call);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.incomingCall, "body": call});
    }
  }

  void _handleIncomingCall2Event(Map<dynamic, dynamic>? map) {
    StringeeCall2 call = StringeeCall2.fromCallInfo(map, this);
    if (_clientListener != null) {
      if (_clientListener!.onIncomingCall2 != null) {
        _clientListener!.onIncomingCall2!(call);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.incomingCall2, "body": call});
    }
  }

  void _handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic>? map) {
    String from = map!['fromUserId'];
    Map<dynamic, dynamic> message = map['message'];
    if (_clientListener != null) {
      if (_clientListener!.onReceiveCustomMessage != null) {
        _clientListener!.onReceiveCustomMessage!(from, message);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeClientEvents.didReceiveCustomMessage,
        "body": map
      });
    }
  }

  void _handleDidReceiveChatRequestEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    if (_clientListener != null) {
      if (_clientListener!.onReceiveChatRequest != null) {
        _clientListener!.onReceiveChatRequest!(request);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeClientEvents.didReceiveChatRequest,
        "body": request
      });
    }
  }

  void _handleDidReceiveTransferChatRequestEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    if (_clientListener != null) {
      if (_clientListener!.onReceiveTransferChatRequest != null) {
        _clientListener!.onReceiveTransferChatRequest!(request);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeClientEvents.didReceiveTransferChatRequest,
        "body": request
      });
    }
  }

  void _handleTimeoutAnswerChatEvent(Map<dynamic, dynamic> map) {
    StringeeChatRequest request = StringeeChatRequest(map, this);
    if (_clientListener != null) {
      if (_clientListener!.onTimeoutAnswerChat != null) {
        _clientListener!.onTimeoutAnswerChat!(request);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeClientEvents.timeoutAnswerChat, "body": request});
    }
  }

  void _handleTimeoutInQueueEvent(Map<dynamic, dynamic> map) {
    String conversationId = map['convId'];
    String customerId = map['customerId'];
    String customerName = map['customerName'];
    if (_clientListener != null) {
      if (_clientListener!.onTimeoutInQueue != null) {
        _clientListener!.onTimeoutInQueue!(
            conversationId, customerId, customerName);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.timeoutInQueue, "body": map});
    }
  }

  void _handleConversationEndedEvent(Map<dynamic, dynamic> map) {
    String conversationId = map['convId'];
    String endedBy = map['endedby'];
    if (_clientListener != null) {
      if (_clientListener!.onConversationEnded != null) {
        _clientListener!.onConversationEnded!(conversationId, endedBy);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeClientEvents.conversationEnded, "body": map});
    }
  }

  void _handleUserBeginTypingEvent(Map<dynamic, dynamic> map) {
    String conversationId = map['convId'];
    String userId = map['userId'];
    String displayName = map['displayName'];
    if (_clientListener != null) {
      if (_clientListener!.onUserBeginTyping != null) {
        _clientListener!.onUserBeginTyping!(
            conversationId, userId, displayName);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.userBeginTyping, "body": map});
    }
  }

  void _handleUserEndTypingEvent(Map<dynamic, dynamic> map) {
    String conversationId = map['convId'];
    String userId = map['userId'];
    String displayName = map['displayName'];
    if (_clientListener != null) {
      if (_clientListener!.onUserEndTyping != null) {
        _clientListener!.onUserEndTyping!(conversationId, userId, displayName);
      }
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeClientEvents.userEndTyping, "body": map});
    }
  }

  void _handleReceiveChangeEvent(Map<dynamic, dynamic> map) {
    ChangeType changeType = ChangeType.values[map['changeType']];
    ObjectType objectType = ObjectType.values[map['objectType']];
    List<dynamic>? objectDatas = map['objects'];
    List<dynamic> objects = [];

    switch (objectType) {
      case ObjectType.conversation:
        for (int i = 0; i < objectDatas!.length; i++) {
          StringeeConversation conv =
              new StringeeConversation.fromJson(objectDatas[i], this);
          objects.add(conv);
        }
        break;
      case ObjectType.message:
        for (int i = 0; i < objectDatas!.length; i++) {
          StringeeMessage msg =
              new StringeeMessage.fromJson(objectDatas[i], this);
          objects.add(msg);
        }
        break;
    }
    StringeeObjectChange stringeeChange =
        new StringeeObjectChange(changeType, objectType, objects);
    if (_clientListener != null) {
      if (_clientListener!.onChangeEvent != null) {
        _clientListener!.onChangeEvent!(stringeeChange);
      }
    }
    if (_chatEvent != null) {
      _chatEvent!.onChangeEvent(stringeeChange);
    }
  }

  /// End handle events
}
