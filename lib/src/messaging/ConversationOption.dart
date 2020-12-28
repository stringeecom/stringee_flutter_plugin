class ConversationOption {
  String _name;
  bool _isGroup;
  bool _isDistinct;

  ConversationOption(
    this._name,
    this._isGroup,
    this._isDistinct,
  );

  Map<String, dynamic> toJson() {
    return {
      'name': _name,
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
    };
  }
}
