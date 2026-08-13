import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// An incoming live-chat request that an agent can accept or reject.
class StringeeChatRequest {
  late String _convId;
  late String _customerId;
  late String _customerName;
  StringeeChannelType _channelType = StringeeChannelType.livechat;
  StringeeChatRequestType _type = StringeeChatRequestType.normal;

  /// Conversation ID associated with the request.
  String get convId => _convId;

  /// Customer's Stringee identifier.
  String get customerId => _customerId;

  /// Customer's display name.
  String get customerName => _customerName;

  /// Channel through which the customer contacted the agent.
  StringeeChannelType get channelType => _channelType;

  /// Whether this is a normal or transferred request.
  StringeeChatRequestType get type => _type;

  late StringeeClient _client;

  /// Creates a request from native [data] and binds it to [client].
  StringeeChatRequest(Map<dynamic, dynamic> data, StringeeClient client) {
    _convId = StringeeValueParser.toStringValue(data["convId"]) ?? '';
    _customerId = StringeeValueParser.toStringValue(data["customerId"]) ?? '';
    _customerName =
        StringeeValueParser.toStringValue(data["customerName"]) ?? '';
    _channelType = StringeeValueParser.enumValue(
      StringeeChannelType.values,
      data["channelType"],
      StringeeChannelType.livechat,
    );
    _type = StringeeValueParser.enumValue(
      StringeeChatRequestType.values,
      data["type"],
      StringeeChatRequestType.normal,
    );
    _client = client;
  }

  /// Accepts this chat request.
  Future<Map<dynamic, dynamic>> accept() async {
    if (_convId.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel
        .invokeMethod('acceptChatRequest', params);
  }

  /// Rejects this chat request.
  Future<Map<dynamic, dynamic>> reject() async {
    if (_convId.isEmpty) return await reportInvalidValue('convId');
    final params = {'convId': _convId, 'uuid': _client.uuid};
    return await StringeeClient.methodChannel
        .invokeMethod('rejectChatRequest', params);
  }
}
