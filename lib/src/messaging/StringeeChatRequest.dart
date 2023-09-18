import '../StringeeClient.dart';
import '../StringeeConstants.dart';

class StringeeChatRequest {
  late StringeeClient _client;
  String? _convId;
  String? _customerId;
  String? _customerName;
  ChannelType? _channelType = ChannelType.live_chat;
  StringeeChatRequestType? _type = StringeeChatRequestType.normal;

  String? get convId => _convId;

  String? get customerId => _customerId;

  String? get customerName => _customerName;

  ChannelType? get channelType => _channelType;

  StringeeChatRequestType? get type => _type;

  @override
  String toString() {
    return {
      'userId': _client.userId,
      'uuid': _client.uuid,
      'convId': _convId,
      'customerId': _customerId,
      'customerName': _customerName,
      'channelType': _channelType!.index,
      'type': _type!.index,
    }.toString();
  }

  StringeeChatRequest.fromJson(
    Map<dynamic, dynamic> data,
    StringeeClient client,
  ) {
    _convId = data["convId"];
    _customerId = data["customerId"];
    _customerName = data["customerName"];
    _channelType = ChannelType.values[data["channelType"]];
    _type = StringeeChatRequestType.values[data["type"]];
    _client = client;
  }

  /// Accept [StringeeChatRequest]
  Future<Map<dynamic, dynamic>> accept() async {
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await _client.methodChannel
        .invokeMethod('acceptChatRequest', params);
  }

  /// Reject [StringeeChatRequest]
  Future<Map<dynamic, dynamic>> reject() async {
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await _client.methodChannel
        .invokeMethod('rejectChatRequest', params);
  }
}
