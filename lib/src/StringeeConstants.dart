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
