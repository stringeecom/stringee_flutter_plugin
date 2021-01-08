import 'dart:convert';

import 'package:stringee_flutter_plugin/src/StringeeClient.dart';
import 'package:stringee_flutter_plugin/src/messaging/Message.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

import '../StringeeConstants.dart';
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

  /// Delete [Conversation]
  Future<Map<dynamic, dynamic>> delete() async {
    return await StringeeClient.methodChannel.invokeMethod('delete', this._id);
  }

  /// Add [List] of [participants] of [Conversation]
  Future<Map<dynamic, dynamic>> addParticipants(List<User> participants) async {
    if (participants == null || participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'convId': this._id,
      'participants': json.encode(participants),
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('addParticipants', params);
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

  /// Remove [List] of [participants] of [Conversation]
  Future<Map<dynamic, dynamic>> removeParticipants(List<User> participants) async {
    if (participants == null || participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'convId': this._id,
      'participants': json.encode(participants),
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('removeParticipants', params);
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
    if (message == null) return await reportInvalidValue('message');
    return await StringeeClient.methodChannel.invokeMethod('sendMessage', json.encode(message));
  }

  /// Get [List] of [Message] of [Conversation] by [msgIds]
  Future<Map<dynamic, dynamic>> getMessages(List<String> msgIds) async {
    if (msgIds == null || msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getMessages', params);
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
  Future<Map<dynamic, dynamic>> getLocalMessages(int count) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    final params = {
      'convId': this._id,
      'count': count,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
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
  Future<Map<dynamic, dynamic>> getLastMessages(int count) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    final params = {
      'convId': this._id,
      'count': count,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
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
  Future<Map<dynamic, dynamic>> getMessagesAfter(int count, int sequence) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (sequence == null || sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
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
  Future<Map<dynamic, dynamic>> getMessagesBefore(int count, int sequence) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (sequence == null || sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
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
  Future<Map<dynamic, dynamic>> updateConversation(String name) async {
    if (name == null || name.trim().isEmpty) return await reportInvalidValue('name');
    final params = {
      'convId': this._id,
      'name': name.trim(),
    };
    return await StringeeClient.methodChannel.invokeMethod('updateConversation', params);
  }

  /// Change [UserRole]
  Future<Map<dynamic, dynamic>> setRole(String userId, UserRole role) async {
    if (userId == null || userId.trim().isEmpty) return await reportInvalidValue('userId');
    if (role == null) return await reportInvalidValue('role');
    final params = {
      'convId': this._id,
      'userId': userId.trim(),
      'role': role.index,
    };
    return await StringeeClient.methodChannel.invokeMethod('setRole', params);
  }

  /// Delete [List] of [Message]
  Future<Map<dynamic, dynamic>> deleteMessages(List<String> msgIds) async {
    if (msgIds == null || msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
    };
    return await StringeeClient.methodChannel.invokeMethod('deleteMessages', params);
  }

  /// Revoke [List] of [Message] include deleted [Message] or not
  Future<Map<dynamic, dynamic>> revokeMessages(List<String> msgIds, bool isDeleted) async {
    if (msgIds == null || msgIds.length == 0) return await reportInvalidValue('msgIds');
    if (isDeleted == null) return await reportInvalidValue('isDeleted');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
      'isDeleted': isDeleted,
    };
    return await StringeeClient.methodChannel.invokeMethod('revokeMessages', params);
  }

  /// Mark [Conversation] as readed
  Future<Map<dynamic, dynamic>> markAsRead() async {
    return await StringeeClient.methodChannel.invokeMethod('markAsRead', this._id);
  }
}
