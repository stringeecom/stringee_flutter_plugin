//events
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

/// Type of Call
enum StringeeCallType {
  AppToAppOutgoing,
  AppToAppIncoming,
  AppToPhone,
  PhoneToApp
}

/// Type of Audio Device
enum AudioDevice {
  SPEAKER_PHONE,
  WIRED_HEADSET,
  EARPIECE,
  BLUETOOTH,
  NONE,
}

/// Type of Signaling State
enum StringeeSignalingState {
  Calling,
  Ringing,
  Answered,
  Busy,
  Ended,
}

/// Type of Media State
enum StringeeMediaState {
  Connected,
  Disconnected,
}

/// Type of Video Quality
enum VideoQuality {
  NORMAL,
  HD,
  FULLHD,
}

///Type of Scaling type
enum ScalingType {
  SCALE_ASPECT_FIT,
  SCALE_ASPECT_FILL,
  SCALE_ASPECT_BALANCED,
}
