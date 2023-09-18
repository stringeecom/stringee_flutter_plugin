import '../../stringee_flutter_plugin.dart';

class StringeeConversationFilter {
  ChatSupportStatus? _chatSupportStatus;
  bool? _isDeleted;
  bool? _isUnread;
  String? _oaId;
  List<ChannelType>? _channelTypes;

  StringeeConversationFilter(
      {ChatSupportStatus? chatSupportStatus,
      bool? isDeleted,
      bool? isUnread,
      String? oaId,
      List<ChannelType>? channelTypes}) {
    if (chatSupportStatus != null) this._chatSupportStatus = chatSupportStatus;
    this._isDeleted = isDeleted != null ? isDeleted : false;
    this._isUnread = isUnread != null ? isUnread : false;
    if (oaId != null) this._oaId = oaId;
    if (channelTypes != null) {
      this._channelTypes = channelTypes;
    } else {
      this._channelTypes = [ChannelType.normal];
    }
  }

  @override
  String toString() {
    return {
      if (_chatSupportStatus != null)
        'chatSupportStatus': _chatSupportStatus!.index,
      if (_isDeleted != null) 'isDeleted': _isUnread,
      if (_isUnread != null) 'isUnread': _isUnread,
      if (_oaId != null) 'oaId': _oaId!.trim(),
      'channelTypes': _channelTypes.getListTypes,
    }.toString();
  }
}
