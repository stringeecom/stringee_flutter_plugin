import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/ConversationOption.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'messaging/User.dart';

class StringeeParams {
  Map<String, dynamic> toJson() {}
}

class ParticipantParams implements StringeeParams {
  String _convId;
  List<User> _participants;

  ParticipantParams({@required String convId, @required List<User> participants})
      : assert(convId != null || convId.trim().isNotEmpty),
        assert(participants != null || participants.length > 0) {
    this._convId = convId;
    this._participants = participants;
  }

  @override
  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['convId'] = _convId;
    params['participants'] = _participants;
    return params;
  }
}
