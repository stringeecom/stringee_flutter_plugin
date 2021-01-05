import 'package:flutter/cupertino.dart';

/// Type of Call
enum StringeeCallType { AppToAppOutgoing, AppToAppIncoming, AppToPhone, PhoneToApp }

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

class MakeCallParams {
  String _from;
  String _to;
  bool _isVideoCall;
  String _customData;
  VideoQuality _videoQuality;

  MakeCallParams({
    @required String from,
    @required String to,
    bool isVideoCall,
    String customData,
    VideoQuality videoQuality,
  })  : assert(from != null || from.trim().isNotEmpty),
        assert(to != null || to.trim().isNotEmpty) {
    this._from = from.trim();
    this._to = to.trim();
    this._isVideoCall = (isVideoCall != null) ? isVideoCall : false;
    if (customData != null) this._customData = customData.trim();
    if (this._isVideoCall) this._videoQuality = (videoQuality != null) ? videoQuality : VideoQuality.NORMAL;
  }

  VideoQuality get videoQuality => _videoQuality;

  String get customData => _customData;

  bool get isVideoCall => _isVideoCall;

  String get to => _to;

  String get from => _from;
}
