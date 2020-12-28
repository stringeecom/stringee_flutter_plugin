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
