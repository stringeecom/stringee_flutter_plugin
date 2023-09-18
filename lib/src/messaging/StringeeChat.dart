import 'dart:async';
import 'dart:convert';

import '../../stringee_flutter_plugin.dart';

@Deprecated('Using StringeeClient instead')
class StringeeChat {
  late StringeeClient _client;
  late StreamSubscription<dynamic> _subscriber;
  @deprecated
  StreamController<dynamic> _eventStreamController =
      StreamController.broadcast();

  @deprecated
  StreamController<dynamic> get eventStreamController => _eventStreamController;

  @deprecated
  StringeeChat(StringeeClient client) {
    _client = client;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeObjectEventType.chat.index &&
        map['uuid'] == _client.uuid) {
      switch (map['event']) {
        case 'didReceiveChangeEvent':
          _handleReceiveChangeEvent(map['body']);
          break;
      }
    }
  }

  /// ====================== BEGIN LIVE CHAT =======================

  /// Get chat profile which contain portal info and list of queues
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getChatProfile(String key) async {
    if (key.trim().isEmpty) return await reportInvalidValue('key');

    final params = {'key': key.trim(), 'uuid': _client.uuid};

    return await _client.methodChannel
        .invokeMethod('getChatProfile', params);
  }

  /// Get live-chat token
  @Deprecated('Using StringeeClient instead')
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
      'uuid': _client.uuid,
    };

    return await _client.methodChannel
        .invokeMethod('getLiveChatToken', params);
  }

  /// Update user info
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> updateUserInfo(
      {String? name, String? email, String? avatar, String? phone}) async {
    final params = {
      if (name != null) 'name': name.trim(),
      if (email != null) 'email': email.trim(),
      if (avatar != null) 'avatar': avatar.trim(),
      if (phone != null) 'phone': phone.trim(),
      'uuid': _client.uuid
    };

    return await _client.methodChannel
        .invokeMethod('updateUserInfo', params);
  }

  /// Create live-chat [StringeeConversation]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> createLiveChatConversation(
      String queueId) async {
    if (queueId.trim().isEmpty) return await reportInvalidValue('queueId');

    final params = {
      'queueId': queueId,
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('createLiveChatConversation', params);
    if (result['status']) {
      result['body'] = StringeeConversation.fromJson(result['body'], _client);
    }
    return result;
  }

  /// Create live-chat ticket
  @Deprecated('Using StringeeClient instead')
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
      'uuid': _client.uuid
    };

    return await _client.methodChannel
        .invokeMethod('createLiveChatTicket', params);
  }

  /// ====================== END LIVE CHAT =======================

  /// Create new [StringeeConversation] with [options] and [participants]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> createConversation(
      StringeeConversationOption options,
      List<StringeeUser> participants) async {
    if (participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'participants': json.encode(participants),
      'option': json.encode(options),
      'uuid': _client.uuid
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('createConversation', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], _client);
    return result;
  }

  /// Get [StringeeConversation] with [StringeeConversation.id] = [convId]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getConversationById(String convId) async {
    if (convId.trim().isEmpty) return await reportInvalidValue('convId');

    final params = {'convId': convId.trim(), 'uuid': _client.uuid};

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getConversationById', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], _client);
    return result;
  }

  /// Get [StringeeConversation] by [userId] from Stringee server
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getConversationByUserId(String userId) async {
    if (userId.trim().isEmpty) return await reportInvalidValue('convId');

    final params = {'userId': userId.trim(), 'uuid': _client.uuid};

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getConversationByUserId', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], _client);
    return result;
  }

  /// Get local [StringeeConversation]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getLocalConversations({String? oaId}) async {
    final params = {
      if (oaId != null) 'oaId': oaId,
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getLocalConversations', params);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], _client));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of lastest [StringeeConversation] from Stringee server
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getLastConversation(
    int count, {
    String? oaId,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');

    final param = {
      'count': count,
      if (oaId != null) 'oaId': oaId,
      'uuid': _client.uuid
    };

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getLastConversation', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], _client));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] before [datetime] from Stringee server
  @Deprecated('Using StringeeClient instead')
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
      'uuid': _client.uuid
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getConversationsBefore', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], _client));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Get [count] of [StringeeConversation] after [datetime] from Stringee server
  @Deprecated('Using StringeeClient instead')
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
      'uuid': _client.uuid
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getConversationsAfter', param);
    if (result['status']) {
      List<dynamic> list = result['body'];
      List<StringeeConversation> conversations = [];
      for (int i = 0; i < list.length; i++) {
        conversations.add(StringeeConversation.fromJson(list[i], _client));
      }
      result['body'] = conversations;
    }
    return result;
  }

  /// Clear local database
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> clearDb() async {
    final param = {'uuid': _client.uuid};

    return await _client.methodChannel.invokeMethod('clearDb', param);
  }

  /// Get total of unread [StringeeConversation]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> getTotalUnread() async {
    final param = {'uuid': _client.uuid};

    return await _client.methodChannel
        .invokeMethod('getTotalUnread', param);
  }

  /// Join Oa [StringeeConversation]
  @Deprecated('Using StringeeClient instead')
  Future<Map<dynamic, dynamic>> joinOaConversation(String convId) async {
    final params = {
      'convId': convId,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel
        .invokeMethod('joinOaConversation', params);
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
              new StringeeConversation.fromJson(objectDatas[i], _client);
          objects.add(conv);
        }
        break;
      case ObjectType.message:
        for (int i = 0; i < objectDatas!.length; i++) {
          StringeeMessage msg =
              new StringeeMessage.fromJson(objectDatas[i], _client);
          objects.add(msg);
        }
        break;
    }
    StringeeObjectChange stringeeChange =
        new StringeeObjectChange(changeType, objectType, objects);
    _eventStreamController.add({
      "eventType": StringeeChatEvents.didReceiveObjectChange,
      "body": stringeeChange
    });
  }

  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
