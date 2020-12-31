import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/ConversationOption.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'messaging/User.dart';

class TopicMessage {
  String _userId;
  Map<dynamic, dynamic> _msg;

  Map<dynamic, dynamic> get msg => _msg;

  String get userId => _userId;

  TopicMessage({@required String userId, @required Map<dynamic, dynamic> msg}) {
    this._userId = userId;
    this._msg = msg;
  }
}

class CustomData {
  String _userId;
  Map<dynamic, dynamic> _msg;

  Map<dynamic, dynamic> get msg => _msg;

  String get userId => _userId;

  CustomData({@required String userId, @required Map<dynamic, dynamic> msg}) {
    this._userId = userId;
    this._msg = msg;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId;
    params['msg'] = _msg;
    return params;
  }
}

class Parameter {}

class CreateConvParam extends Parameter {
  List<User> _participants;
  ConversationOption _option;

  ConversationOption get option => _option;

  List<User> get participants => _participants;

  CreateConvParam({@required List<User> participants, @required ConversationOption option}) {
    this._participants = participants;
    this._option = option;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['participants'] = _participants;
    params['option'] = _option;
    return params;
  }
}
