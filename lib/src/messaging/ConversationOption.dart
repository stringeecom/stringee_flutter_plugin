import 'package:flutter/cupertino.dart';

class ConversationOption {
  String _name;
  bool _isGroup;
  bool _isDistinct;

  ConversationOption({String name, @required bool isGroup, @required bool isDistinct})
      : assert(isGroup != null),
        assert(isDistinct != null) {
    this._name = name;
    this._isGroup = isGroup;
    this._isDistinct = isDistinct;
  }

  Map<String, dynamic> toJson() {
    return {
      if (_name != null) 'name': _name.trim(),
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
    };
  }
}
