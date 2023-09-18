class StringeeConversationOption {
  String? _name;
  bool _isGroup = false;
  bool _isDistinct = false;
  String? _oaId;
  String? _customData;
  String? _creatorId;

  StringeeConversationOption(
      {required bool isGroup,
      required bool isDistinct,
      String? name,
      String? oaId,
      String? customData,
      String? creatorId}) {
    if (name != null) this._name = name;
    this._isGroup = isGroup;
    this._isDistinct = isDistinct;
    if (oaId != null) this._oaId = oaId;
    if (customData != null) this._customData = customData;
    if (creatorId != null) this._creatorId = creatorId;
  }

  @override
  String toString() {
    return {
      if (_name != null) 'name': _name!.trim(),
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
      if (_oaId != null) 'oaId': _oaId!.trim(),
      if (_customData != null) 'customData': _customData!.trim(),
      if (_creatorId != null) 'creatorId': _creatorId!.trim(),
    }.toString();
  }
}
