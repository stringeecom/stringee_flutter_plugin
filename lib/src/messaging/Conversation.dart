import 'dart:convert';

import 'package:stringee_flutter_plugin/src/StringeeClient.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

import 'StringeeChange.dart';
import 'User.dart';

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

  Conversation.initFromEvent(Map<dynamic, dynamic> convInfor) {
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

  Future<Map<dynamic, dynamic>> delete(String clientId) async {
    return await StringeeClient.methodChannel.invokeMethod('delete', clientId);
  }
}

class LastMsg {
  int _messageType;
  String _text;
  String _content;

  LastMsg(Map<dynamic, dynamic> lastMsgInfor) {
    this._messageType = lastMsgInfor['messageType'];
    this._text = lastMsgInfor['text'];
    this._content = lastMsgInfor['content'];
  }
}
