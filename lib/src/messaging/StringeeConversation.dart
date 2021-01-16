import 'dart:convert';

import 'package:stringee_flutter_plugin/src/StringeeClient.dart';
import 'package:stringee_flutter_plugin/src/messaging/StringeeMessage.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

import '../StringeeConstants.dart';
import 'StringeeChange.dart';
import 'StringeeUser.dart';

class StringeeConversation implements StringeeObject {
  String _id;
  String _name;
  bool _isGroup;
  String _creator;
  int _createdAt;
  int _updatedAt;
  int _totalUnread;
  String _text;

  StringeeMessage _lastMsg;
  String _pinnedMsgId;
  List<User> _participants;

  StringeeConversation();

  String get id => _id;

  String get name => _name;

  bool get isGroup => _isGroup;

  String get creator => _creator;

  int get totalUnread => _totalUnread;

  int get updatedAt => _updatedAt;

  int get createdAt => _createdAt;

  String get text => _text;

  StringeeMessage get lastMsg => _lastMsg;

  String get pinnedMsgId => _pinnedMsgId;

  List<User> get participants => _participants;

  StringeeConversation.fromJson(Map<dynamic, dynamic> convInfor) {
    if (convInfor == null) {
      return;
    }

    this._id = convInfor['id'];
    this._name = convInfor['name'];
    this._isGroup = convInfor['isGroup'];
    this._creator = convInfor['creator'];
    this._createdAt = convInfor['createdAt'];
    this._updatedAt = convInfor['updatedAt'];
    this._totalUnread = convInfor['totalUnread'];
    this._text = convInfor['text'];
    this._lastMsg = new StringeeMessage.lstMsg(
        convInfor['lastMsgId'],
        this._id,
        (convInfor['lastMsgType'] as int).msgType,
        convInfor['lastMsgSender'],
        convInfor['lastMsgSeqReceived'],
        MsgState.values[convInfor['lastMsgState']],
        convInfor['lastTimeNewMsg'],
        jsonDecode(this._text));
    this._pinnedMsgId = convInfor['pinnedMsgId'];

    List<User> participants = [];
    List<dynamic> participantArray = json.decode(convInfor['participants']);
    for (int i = 0; i < participantArray.length; i++) {
      User user = User.fromJson(participantArray[i]);
      participants.add(user);
    }
    this._participants = participants;
  }

  /// Delete [StringeeConversation]
  Future<Map<dynamic, dynamic>> delete() async {
    return await StringeeClient.methodChannel.invokeMethod('delete', this._id);
  }

  /// Add [List] of [participants] of [StringeeConversation]
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

  /// Remove [List] of [participants] of [StringeeConversation]
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

  /// Send [StringeeMessage]
  Future<Map<dynamic, dynamic>> sendMessage(StringeeMessage message) async {
    if (message == null) return await reportInvalidValue('message');
    message.convId = this._id;
    return await StringeeClient.methodChannel.invokeMethod('sendMessage', json.encode(message));
  }

  /// Get [List] of [StringeeMessage] of [StringeeConversation] by [msgIds]
  Future<Map<dynamic, dynamic>> getMessages(List<String> msgIds) async {
    if (msgIds == null || msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of local [StringeeMessage] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLocalMessages(int count) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    final params = {
      'convId': this._id,
      'count': count,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of lastest [StringeeMessage] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLastMessages(int count) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    final params = {
      'convId': this._id,
      'count': count,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getLastMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [StringeeMessage] after [StringeeMessage.sequence] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getMessagesAfter(int count, int sequence) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (sequence == null || sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getMessagesAfter', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [StringeeMessage] before [StringeeMessage.sequence] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getMessagesBefore(int count, int sequence) async {
    if (count == null || count <= 0) return await reportInvalidValue('count');
    if (sequence == null || sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('getMessagesBefore', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = json.decode(result['body']);
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i]);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Update [StringeeConversation.name]
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

  /// Delete [List] of [StringeeMessage]
  Future<Map<dynamic, dynamic>> deleteMessages(List<String> msgIds) async {
    if (msgIds == null || msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
    };
    return await StringeeClient.methodChannel.invokeMethod('deleteMessages', params);
  }

  /// Revoke [List] of [StringeeMessage] include deleted [StringeeMessage] or not
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

  /// Mark [StringeeConversation] as readed
  Future<Map<dynamic, dynamic>> markAsRead() async {
    return await StringeeClient.methodChannel.invokeMethod('markAsRead', this._id);
  }
}
