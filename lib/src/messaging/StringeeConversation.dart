import 'dart:async';
import 'dart:convert';

import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

class StringeeConversation {
  late StringeeClient _client;
  String? _id;
  String? _localId;
  String? _name;
  bool? _isGroup;
  String? _creator;
  int? _createdAt;
  int? _updatedAt;
  int? _totalUnread;
  @deprecated
  Map<dynamic, dynamic>? _text;
  StringeeMessage? _lastMsg;
  String? _pinnedMsgId;
  List<StringeeUser>? _participants;
  String? _oaId;
  String? _customData;
  int? _lastTimeNewMsg;
  int? _lastMsgSeqReceived;
  ChannelType? _channelType = ChannelType.normal;
  bool? _ended = false;
  int? _lastSequence;

  String? get id => _id;

  String? get name => _name;

  bool? get isGroup => _isGroup;

  String? get creator => _creator;

  int? get totalUnread => _totalUnread;

  int? get updatedAt => _updatedAt;

  int? get createdAt => _createdAt;

  @deprecated
  Map<dynamic, dynamic>? get text => _text;

  StringeeMessage? get lastMsg => _lastMsg;

  String? get pinnedMsgId => _pinnedMsgId;

  List<StringeeUser>? get participants => _participants;

  String? get oaId => _oaId;

  String? get customData => _customData;

  int? get lastTimeNewMsg => _lastTimeNewMsg;

  int? get lastMsgSeqReceived => _lastMsgSeqReceived;

  String? get localId => _localId;

  ChannelType? get channelType => _channelType;

  bool? get ended => _ended;

  int? get lastSequence => _lastSequence;

  @override
  String toString() {
    return {
      'userId': _client.userId,
      'uuid': _client.uuid,
      'id': _id,
      'name': _name,
      'isGroup': _isGroup,
      'creator': _creator,
      'createdAt': _createdAt,
      'totalUnread': _totalUnread,
      'lastMsg': _lastMsg!.toString(),
      'pinnedMsgId': _pinnedMsgId,
      'participants': _participants!.toString(),
      'oaId': _oaId,
      'customData': _customData,
      'localId': _localId,
      'lastTimeNewMsg': _lastTimeNewMsg,
      'lastMsgSeqReceived': _lastMsgSeqReceived,
      'channelType': _channelType!.index,
      'ended': _ended!,
      'lastSequence': _lastSequence!,
    }.toString();
  }

  StringeeConversation.fromJson(
      Map<dynamic, dynamic>? convInfor, StringeeClient client) {
    if (convInfor == null) {
      return;
    }
    _client = client;

    this._id = convInfor['id'];
    this._name = convInfor['name'];
    this._isGroup = convInfor['isGroup'];
    this._creator = convInfor['creator'];
    this._createdAt = convInfor['createdAt'];
    this._updatedAt = convInfor['updatedAt'];
    this._totalUnread = convInfor['totalUnread'];
    this._text = convInfor['text'];
    this._lastMsg =
        new StringeeMessage.fromJson(convInfor['lastMsg'], this._client);
    this._pinnedMsgId = convInfor['pinnedMsgId'];
    this._lastTimeNewMsg = convInfor['lastTimeNewMsg'];
    this._lastMsgSeqReceived = convInfor['lastMsgSeqReceived'];
    this._localId = convInfor['localId'];
    this._channelType = ChannelType.values[convInfor['channelType']];
    this._ended = convInfor['ended'];
    this._lastSequence = convInfor['lastSequence'];

    List<StringeeUser> participants = [];
    List<dynamic> participantArray = convInfor['participants'];
    for (int i = 0; i < participantArray.length; i++) {
      StringeeUser user = StringeeUser.fromJson(participantArray[i]);
      participants.add(user);
    }
    this._participants = participants;
    this._oaId = convInfor['oaId'];
    this._customData = convInfor['customData'];
  }

  /// Send [StringeeMessage]
  Future<Map<dynamic, dynamic>> sendMessage(StringeeMessage message) async {
    message.convId = this._id;

    Map<String, dynamic> params = message.toJson();
    params['uuid'] = _client.uuid;

    return await _client.methodChannel
        .invokeMethod('sendMessage', params);
    // return await _client.methodChannel.invokeMethod('sendMessage', json.encode(message));
  }

  /// Get [List] of [StringeeMessage] of [StringeeConversation] by [msgIds]
  Future<Map<dynamic, dynamic>> getMessages(List<String> msgIds) async {
    if (msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {'convId': this._id, 'msgIds': msgIds, 'uuid': _client.uuid};
    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('getMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of local [StringeeMessage] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLocalMessages(int count) async {
    if (count <= 0) return await reportInvalidValue('count');
    final params = {'convId': this._id, 'count': count, 'uuid': _client.uuid};
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getLocalMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of lastest [StringeeMessage] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getLastMessages(
    int count, {
    bool? loadDeletedMsg,
    bool? loadDeletedMsgContent,
    bool? loadAll,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');
    final params = {
      'convId': this._id,
      'count': count,
      'uuid': _client.uuid,
      if (loadDeletedMsg != null) 'loadDeletedMsg': loadDeletedMsg,
      if (loadDeletedMsgContent != null)
        'loadDeletedMsgContent': loadDeletedMsgContent,
      if (loadAll != null) 'loadAll': loadAll,
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getLastMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [StringeeMessage] after [StringeeMessage.sequence] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getMessagesAfter(
    int count,
    int sequence, {
    bool? loadDeletedMsg,
    bool? loadDeletedMsgContent,
    bool? loadAll,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');
    if (sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
      'uuid': _client.uuid,
      if (loadDeletedMsg != null) 'loadDeletedMsg': loadDeletedMsg,
      if (loadDeletedMsgContent != null)
        'loadDeletedMsgContent': loadDeletedMsgContent,
      if (loadAll != null) 'loadAll': loadAll,
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getMessagesAfter', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [StringeeMessage] before [StringeeMessage.sequence] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getMessagesBefore(
    int count,
    int sequence, {
    bool? loadDeletedMsg,
    bool? loadDeletedMsgContent,
    bool? loadAll,
  }) async {
    if (count <= 0) return await reportInvalidValue('count');
    if (sequence < 0) return await reportInvalidValue('sequence');
    final params = {
      'convId': this._id,
      'count': count,
      'seq': sequence,
      'uuid': _client.uuid,
      if (loadDeletedMsg != null) 'loadDeletedMsg': loadDeletedMsg,
      if (loadDeletedMsgContent != null)
        'loadDeletedMsgContent': loadDeletedMsgContent,
      if (loadAll != null) 'loadAll': loadAll,
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getMessagesBefore', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Get [count] of [StringeeMessage] after [start] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> getAttachmentMessages(
      int count, int start, MsgType msgType) async {
    if (count <= 0) return await reportInvalidValue('count');
    if (start < 0) return await reportInvalidValue('start');
    final params = {
      'convId': this._id,
      'count': count,
      'start': start,
      'uuid': _client.uuid,
      'type': msgType.index,
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('getAttachmentMessages', params);
    if (result['status']) {
      List<StringeeMessage> messages = [];
      List<dynamic> msgArray = result['body'];
      for (int i = 0; i < msgArray.length; i++) {
        StringeeMessage msg = StringeeMessage.fromJson(msgArray[i], _client);
        messages.add(msg);
      }
      result['body'] = messages;
    }
    return result;
  }

  /// Update [StringeeConversation.name]
  Future<Map<dynamic, dynamic>> updateConversation(String name,
      {String? avatar}) async {
    if (name.trim().isEmpty) return await reportInvalidValue('name');
    final params = {
      'convId': this._id,
      'name': name.trim(),
      'uuid': _client.uuid,
      if (avatar != null) 'avatar': avatar,
    };
    return await _client.methodChannel
        .invokeMethod('updateConversation', params);
  }

  /// Change [UserRole]
  Future<Map<dynamic, dynamic>> setRole(String userId, UserRole role) async {
    if (userId.trim().isEmpty) return await reportInvalidValue('userId');
    final params = {
      'convId': this._id,
      'userId': userId.trim(),
      'role': role.index,
      'uuid': _client.uuid
    };
    return await _client.methodChannel.invokeMethod('setRole', params);
  }

  /// Delete [List] of [StringeeMessage]
  Future<Map<dynamic, dynamic>> deleteMessages(List<String> msgIds) async {
    if (msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {'convId': this._id, 'msgIds': msgIds, 'uuid': _client.uuid};
    return await _client.methodChannel
        .invokeMethod('deleteMessages', params);
  }

  /// Revoke [List] of [StringeeMessage] include deleted [StringeeMessage] or not
  Future<Map<dynamic, dynamic>> revokeMessages(
      List<String> msgIds, bool isDeleted) async {
    if (msgIds.length == 0) return await reportInvalidValue('msgIds');
    final params = {
      'convId': this._id,
      'msgIds': msgIds,
      'isDeleted': isDeleted,
      'uuid': _client.uuid
    };
    return await _client.methodChannel
        .invokeMethod('revokeMessages', params);
  }

  /// Mark [StringeeConversation] as read
  Future<Map<dynamic, dynamic>> markAsRead() async {
    final params = {'convId': this._id, 'uuid': _client.uuid};

    return await _client.methodChannel
        .invokeMethod('markAsRead', params);
  }

  /// Customer rate the live chat
  Future<Map<dynamic, dynamic>> rateChat(Rating rating,
      {String? comment}) async {
    final params = {
      'convId': this._id,
      'uuid': _client.uuid,
      'rating': rating.index,
      if (comment != null) 'comment': comment,
    };

    return await _client.methodChannel.invokeMethod('rateChat', params);
  }

  /// Transfer support chat to other agent
  Future<Map<dynamic, dynamic>> transferTo(
      String userId, String customerId, String customerName) async {
    final params = {
      'convId': this._id,
      'uuid': _client.uuid,
      'userId': userId,
      'customerId': customerId,
      'customerName': customerName,
    };

    return await _client.methodChannel
        .invokeMethod('transferTo', params);
  }

  /// Continue support chat
  Future<Map<dynamic, dynamic>> continueChatting() async {
    final params = {'convId': this._id, 'uuid': _client.uuid};

    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('continueChatting', params);
    if (result['status'])
      result['body'] = StringeeConversation.fromJson(result['body'], _client);

    return result;
  }

  /// Send chat transcript
  Future<Map<dynamic, dynamic>> sendChatTranscript(
      String email, String domain) async {
    if (email.trim().isEmpty) return await reportInvalidValue('email');
    if (domain.trim().isEmpty) return await reportInvalidValue('domain');
    if (_id == null || _id!.isEmpty) return await reportInvalidValue('convId');

    final params = {
      'email': email.trim(),
      'domain': domain.trim(),
      'convId': _id,
      'uuid': _client.uuid
    };

    return await _client.methodChannel
        .invokeMethod('sendChatTranscript', params);
  }

  /// End live-chat [StringeeConversation]
  Future<Map<dynamic, dynamic>> endChat() async {
    if (_id == null || _id!.isEmpty) return await reportInvalidValue('convId');

    final params = {'convId': _id, 'uuid': _client.uuid};

    return await _client.methodChannel.invokeMethod('endChat', params);
  }

  /// Send begin typing
  Future<Map<dynamic, dynamic>> beginTyping() async {
    if (_id == null || _id!.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _id, 'uuid': _client.uuid};
    return await _client.methodChannel
        .invokeMethod('beginTyping', params);
  }

  /// Send end typing
  Future<Map<dynamic, dynamic>> endTyping() async {
    if (_id == null || _id!.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _id, 'uuid': _client.uuid};
    return await _client.methodChannel.invokeMethod('endTyping', params);
  }

  /// Delete [StringeeConversation]
  Future<Map<dynamic, dynamic>> delete() async {
    final params = {'convId': this._id, 'uuid': _client.uuid};

    return await _client.methodChannel.invokeMethod('delete', params);
  }

  /// Add [List] of [participants] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> addParticipants(
      List<StringeeUser> participants) async {
    if (participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'convId': this._id,
      'participants': json.encode(participants),
      'uuid': _client.uuid
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('addParticipants', params);
    if (result['status']) {
      List<StringeeUser> addedParticipants = [];
      List<dynamic> participantArray = result['body'];
      for (int i = 0; i < participantArray.length; i++) {
        StringeeUser user = StringeeUser.fromJson(participantArray[i]);
        addedParticipants.add(user);
      }
      result['body'] = addedParticipants;
    }
    return result;
  }

  /// Remove [List] of [participants] of [StringeeConversation]
  Future<Map<dynamic, dynamic>> removeParticipants(
      List<StringeeUser> participants) async {
    if (participants.length == 0)
      return await reportInvalidValue('participants');
    final params = {
      'convId': this._id,
      'participants': json.encode(participants),
      'uuid': _client.uuid
    };
    Map<dynamic, dynamic> result = await _client.methodChannel
        .invokeMethod('removeParticipants', params);
    if (result['status']) {
      List<StringeeUser> removedParticipants = [];
      List<dynamic> participantArray = result['body'];
      for (int i = 0; i < participantArray.length; i++) {
        StringeeUser user = StringeeUser.fromJson(participantArray[i]);
        removedParticipants.add(user);
      }
      result['body'] = removedParticipants;
    }
    return result;
  }
}
