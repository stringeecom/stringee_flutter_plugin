import 'dart:convert';

import 'package:stringee_flutter_plugin/src/StringeeClient.dart';
import 'package:stringee_flutter_plugin/src/messaging/ConversationOption.dart';
import 'package:stringee_flutter_plugin/src/messaging/Message.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

import 'StringeeChange.dart';
import 'User.dart';

class LastMsg {
  int _messageType;
  String _text;
  String _content;

  int get messageType => _messageType;

  String get text => _text;

  String get content => _content;

  LastMsg(Map<dynamic, dynamic> lastMsgInfor) {
    this._messageType = lastMsgInfor['messageType'];
    this._text = lastMsgInfor['text'];
    this._content = lastMsgInfor['content'];
  }
}

class Conversation implements StringeeObject {
  String _id;
  String _localId;
  String _name;
  bool _isDistinct;
  bool _isGroup;
  bool _isEnded;
  String _clientId;
  String _creator;
  int _createAt;
  int _updateAt;
  int _totalUnread;
  String _text;
  ConvState _state;
  String _lastMsgSender;
  MsgType _lastMsgType;
  String _lastMsgId;
  int _lastMsgSeqReceived;
  int _lastTimeNewMsg;
  MsgState _lastMsgState;
  LastMsg _lastMsg;
  String _pinnedMsgId;
  List<User> _participants;

  Conversation();

  String get id => _id;

  String get localId => _localId;

  String get name => _name;

  bool get isDistinct => _isDistinct;

  bool get isGroup => _isGroup;

  bool get isEnded => _isEnded;

  String get clientId => _clientId;

  String get creator => _creator;

  int get createAt => _createAt;

  int get updateAt => _updateAt;

  int get totalUnread => _totalUnread;

  String get text => _text;

  ConvState get state => _state;

  String get lastMsgSender => _lastMsgSender;

  MsgType get lastMsgType => _lastMsgType;

  String get lastMsgId => _lastMsgId;

  int get lastMsgSeqReceived => _lastMsgSeqReceived;

  int get lastTimeNewMsg => _lastTimeNewMsg;

  MsgState get lastMsgState => _lastMsgState;

  LastMsg get lastMsg => _lastMsg;

  String get pinnedMsgId => _pinnedMsgId;

  List<User> get participants => _participants;

  Conversation.fromJson(Map<dynamic, dynamic> convInfor) {
    if (convInfor == null) {
      return;
    }

    this._id = convInfor['id'];
    this._localId = convInfor['localId'];
    this._name = convInfor['name'];
    this._isDistinct = convInfor['isDistinct'];
    this._isGroup = convInfor['isGroup'];
    this._isEnded = convInfor['isEnded'];
    this._clientId = convInfor['clientId'];
    this._creator = convInfor['creator'];
    this._createAt = convInfor['createAt'];
    this._updateAt = convInfor['updateAt'];
    this._totalUnread = convInfor['totalUnread'];
    this._text = convInfor['text'];
    this._state = ConvState.values[convInfor['state']];
    this._lastMsgSender = convInfor['lastMsgSender'];
    this._lastMsgType = (convInfor['lastMsgType'] as int).msgType;
    this._lastMsgId = convInfor['lastMsgId'];
    this._lastMsgSeqReceived = convInfor['lastMsgSeqReceived'];
    this._lastTimeNewMsg = convInfor['lastTimeNewMsg'];
    this._lastMsgState = MsgState.values[convInfor['lastMsgState']];
    this._lastMsg = new LastMsg(convInfor['lastMsg']);
    this._pinnedMsgId = convInfor['pinnedMsgId'];

    List<User> participants = [];
    List<dynamic> participantArray = json.decode(convInfor['participants']);
    for (int i = 0; i < participantArray.length; i++) {
      User user = User.fromJson(participantArray[i]);
      participants.add(user);
    }
    this._participants = participants;
  }

  /// Delete [Conversation] has [Conversation.id] = [convId]
  Future<Map<dynamic, dynamic>> delete(String convId) async {
    assert(convId != null);
    return await StringeeClient.methodChannel.invokeMethod('delete', convId);
  }

  /// Add [Conversation.participants] with [ParticipantParams]
  Future<Map<dynamic, dynamic>> addParticipants(String convId, List<User> participants) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(participants.length > 0);
    final params = {
      'convId': convId,
      'participants': json.encode(participants),
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('addParticipants', params);
    if (result['status']) {
      List<User> addedParticipants = [];
      List<dynamic> participantArray = json.decode(result['body']);
      for (int i = 0; i < participantArray.length; i++) {
        User user = User.fromJson(participantArray[i]);
        addedParticipants.add(user);
      }
      result['body'] = addedParticipants;
    }
    return result;
  }

  /// Remove [Conversation.participants] with [ParticipantParams]
  Future<Map<dynamic, dynamic>> removeParticipants(String convId, List<User> participants) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(participants.length > 0);
    final params = {
      'convId': convId,
      'participants': json.encode(participants),
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('removeParticipants', params);
    if (result['status']) {
      List<User> removedParticipants = [];
      List<dynamic> participantArray = json.decode(result['body']);
      for (int i = 0; i < participantArray.length; i++) {
        User user = User.fromJson(participantArray[i]);
        removedParticipants.add(user);
      }
      result['body'] = removedParticipants;
    }
    return result;
  }

  /// Send [Message]
  Future<Map<dynamic, dynamic>> sendMessage(Message message) async {
    assert(message != null);
    return await StringeeClient.methodChannel.invokeMethod('sendMessage', json.encode(message));
  }

  /// Get [List] of [Message] of [Conversation] by [msgIds]
  Future<Map<dynamic, dynamic>> getMessages(String convId, List<String> msgIds) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(msgIds.length > 0);
    final params = {
      'convId': convId,
      'msgIds': msgIds,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('getMessages', params);
    if (result['status']) {
      List<Message> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        Message msg = Message.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of local [Message] of [Conversation]
  Future<Map<dynamic, dynamic>> getLocalMessages(String convId, int count) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(count > 0);
    final params = {
      'convId': convId,
      'count': count,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<Message> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        Message msg = Message.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of lastest [Message] of [Conversation]
  Future<Map<dynamic, dynamic>> getLastMessages(String convId, int count) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(count > 0);
    final params = {
      'convId': convId,
      'count': count,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<Message> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        Message msg = Message.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [Message] after [Message.sequence] of [Conversation]
  Future<Map<dynamic, dynamic>> getMessagesAfter(String convId, int count, int sequence) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(count > 0);
    assert(sequence >= 0);
    final params = {
      'convId': convId,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<Message> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        Message msg = Message.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [Message] before [Message.sequence] of [Conversation]
  Future<Map<dynamic, dynamic>> getMessagesBefore(String convId, int count, int sequence) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(count > 0);
    assert(sequence >= 0);
    final params = {
      'convId': convId,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<Message> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        Message msg = Message.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Update [Conversation.name]
  Future<Map<dynamic, dynamic>> updateConversation(String convId, String name) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(name != null || name.trim().isNotEmpty);
    final params = {
      'convId': convId,
      'name': name,
    };
    return await StringeeClient.methodChannel.invokeMethod('updateConversation', params);
  }

  /// Change [UserRole]
  Future<Map<dynamic, dynamic>> setRole(String convId, String userId, UserRole role) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(userId != null || userId.trim().isNotEmpty);
    assert(role != null);
    final params = {
      'convId': convId,
      'userId': userId,
      'role': role.index,
    };
    return await StringeeClient.methodChannel.invokeMethod('setRole', params);
  }

  /// Delete [List] of [Message]
  Future<Map<dynamic, dynamic>> deleteMessages(String convId, List<String> msgIds) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(msgIds.length > 0);
    final params = {
      'convId': convId,
      'msgIds': msgIds,
    };
    return await StringeeClient.methodChannel.invokeMethod('deleteMessages', params);
  }

  /// Revoke [List] of [Message] include deleted [Message] or not
  Future<Map<dynamic, dynamic>> revokeMessages(String convId, List<String> msgIds, bool isDeleted) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(msgIds.length > 0);
    final params = {
      'convId': convId,
      'msgIds': msgIds,
      'isDeleted': (isDeleted != null) ? isDeleted : false,
    };
    return await StringeeClient.methodChannel.invokeMethod('revokeMessages', params);
  }

  /// Mark [Conversation] as readed
  Future<Map<dynamic, dynamic>> markAsRead(String convId) async {
    assert(convId != null || convId.trim().isNotEmpty);
    return await StringeeClient.methodChannel.invokeMethod('markAsRead', convId);
  }
}
