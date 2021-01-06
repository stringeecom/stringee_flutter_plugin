export 'call/CallConstants.dart';
export 'messaging/MessagingConstants.dart';

/// Events for StringeeClient
enum StringeeClientEvents {
  DidConnect,
  DidDisconnect,
  DidFailWithError,
  RequestAccessToken,
  DidReceiveCustomMessage,
  DidReceiveTopicMessage,
  IncomingCall,
  IncomingCall2,
  DidReceiveChange,
}

/// Events for StringeeCall
enum StringeeCallEvents {
  DidChangeSignalingState,
  DidChangeMediaState,
  DidReceiveCallInfo,
  DidHandleOnAnotherDevice,
  DidReceiveLocalStream,
  DidReceiveRemoteStream,
  DidChangeAudioDevice
}

/// Events for StringeeCall2
enum StringeeCall2Events {
  DidChangeSignalingState,
  DidChangeMediaState,
  DidReceiveCallInfo,
  DidHandleOnAnotherDevice,
  DidReceiveLocalStream,
  DidReceiveRemoteStream,
  DidChangeAudioDevice
}

/// Type of event
enum StringeeType {
  StringeeClient,
  StringeeCall,
  StringeeCall2,
}

/// Error code and message:
/// -1 : StringeeClient is not initialized or disconnected
/// -2 : value is invalid
/// -3 : No conversation found
/// -4 : StringeeCall/ StringeeCall2 is not initialized
Future<Map<String, dynamic>> reportInvalidValue(String value) async {
  Map<String, dynamic> params = {
    'status': false,
    'code': -2,
    'message': value + ' value is invalid',
  };
  return params;
}
