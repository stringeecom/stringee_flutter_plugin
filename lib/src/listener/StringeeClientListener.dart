import '../../stringee_flutter_plugin.dart';

class StringeeClientListener {
  void Function(
    StringeeClient stringeeClient,
    String userId,
  ) onConnect;
  void Function(
    StringeeClient stringeeClient,
  ) onDisconnect;
  void Function(
    StringeeClient stringeeClient,
    int code,
    String message,
  ) onFailWithError;
  void Function(
    StringeeClient stringeeClient,
  ) onRequestAccessToken;
  void Function(
    StringeeClient stringeeClient,
    StringeeCall stringeeCall,
  )? onIncomingCall;
  void Function(
    StringeeClient stringeeClient,
    StringeeCall2 stringeeCall2,
  )? onIncomingCall2;
  void Function(
    StringeeClient stringeeClient,
    String from,
    Map<dynamic, dynamic> message,
  )? onReceiveCustomMessage;
  void Function(
    StringeeClient stringeeClient,
    StringeeObjectChange objectChange,
  )? onChangeEvent;
  void Function(
    StringeeClient stringeeClient,
    StringeeChatRequest chatRequest,
  )? onReceiveChatRequest;
  void Function(
    StringeeClient stringeeClient,
    StringeeChatRequest chatRequest,
  )? onReceiveTransferChatRequest;
  void Function(
    StringeeClient stringeeClient,
    StringeeChatRequest chatRequest,
    ChatRequestState state,
  )? onChatRequestHandleOnAnotherDevice;
  void Function(
    StringeeClient stringeeClient,
    StringeeChatRequest chatRequest,
  )? onTimeoutAnswerChat;
  void Function(
    StringeeClient stringeeClient,
    String conversationId,
    String customerId,
    String customerName,
  )? onTimeoutInQueue;
  void Function(
    StringeeClient stringeeClient,
    String conversationId,
    String endedBy,
  )? onConversationEnded;
  void Function(
    StringeeClient stringeeClient,
    String conversationId,
    String userId,
    String displayName,
  )? onUserBeginTyping;
  void Function(
    StringeeClient stringeeClient,
    String conversationId,
    String userId,
    String displayName,
  )? onUserEndTyping;

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
    this.onChatRequestHandleOnAnotherDevice,
    this.onTimeoutAnswerChat,
    this.onTimeoutInQueue,
    this.onConversationEnded,
    this.onUserBeginTyping,
    this.onUserEndTyping,
  });
}
