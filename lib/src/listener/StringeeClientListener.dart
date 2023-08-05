import '../../stringee_flutter_plugin.dart';

class StringeeClientListener {
  void Function(String userId) onConnect;
  void Function() onDisconnect;
  void Function(int code, String message) onFailWithError;
  void Function() onRequestAccessToken;
  void Function(StringeeCall stringeeCall)? onIncomingCall;
  void Function(StringeeCall2 stringeeCall2)? onIncomingCall2;
  void Function(String from, Map<dynamic, dynamic> message)?
      onReceiveCustomMessage;
  void Function(StringeeObjectChange objectChange)? onChangeEvent;
  void Function(StringeeChatRequest chatRequest)? onReceiveChatRequest;
  void Function(StringeeChatRequest chatRequest)? onReceiveTransferChatRequest;
  void Function(StringeeChatRequest chatRequest)? onTimeoutAnswerChat;
  void Function(String conversationId, String customerId, String customerName)?
      onTimeoutInQueue;
  void Function(String conversationId, String endedBy)? onConversationEnded;
  void Function(String conversationId, String userId, String displayName)?
      onUserBeginTyping;
  void Function(String conversationId, String userId, String displayName)?
      onUserEndTyping;

  StringeeClientListener({
    required this.onConnect,
    required this.onDisconnect,
    required this.onFailWithError,
    required this.onRequestAccessToken,
    this.onIncomingCall,
    this.onIncomingCall2,
    this.onReceiveCustomMessage,
    this.onChangeEvent,
    this.onReceiveChatRequest,
    this.onReceiveTransferChatRequest,
    this.onTimeoutAnswerChat,
    this.onTimeoutInQueue,
    this.onConversationEnded,
    this.onUserBeginTyping,
    this.onUserEndTyping,
  });
}
