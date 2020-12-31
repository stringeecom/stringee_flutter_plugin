import 'package:flutter/cupertino.dart';

class ConversationOption {
  String _name;
  bool _isGroup;
  bool _isDistinct;

  ConversationOption({@required String name, bool isGroup, bool isDistinct}) {
    this._isGroup = isGroup;
    this._name = name;
    this._isDistinct = isDistinct;
  }

  Map<String, dynamic> toJson() {
    return {
      if (_name != null) 'name': _name,
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
    };
  }
}
