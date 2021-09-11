import '../StringeeClient.dart';
import '../StringeeConstants.dart';

class StringeeChatRequest {
  late String _convId;
  late String _customerId;
  late String _customerName;
  StringeeChannelType _channelType = StringeeChannelType.livechat;
  StringeeChatRequestType _type = StringeeChatRequestType.normal;

  String get convId => _convId;

  String get customerId => _customerId;

  String get customerName => _customerName;

  StringeeChannelType get channelType => _channelType;

  StringeeChatRequestType get type => _type;

  late StringeeClient _client;

  StringeeChatRequest(Map<dynamic, dynamic> data, StringeeClient client) {
    _convId = data["convId"];
    _customerId = data["customerId"];
    _customerName = data["customerName"];
    _channelType = StringeeChannelType.values[data["channelType"]];
    _type = StringeeChatRequestType.values[data["type"]];
    _client = client;
  }

  /// Accept [StringeeChatRequest]
  Future<Map<dynamic, dynamic>> accept() async {
    if (_convId.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel
        .invokeMethod('acceptChatRequest', params);
  }

  /// Reject [StringeeChatRequest]
  Future<Map<dynamic, dynamic>> reject() async {
    if (_convId.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel
        .invokeMethod('rejectChatRequest', params);
  }
}
