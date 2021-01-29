/// Type of Call
enum StringeeCallType {
  appToAppOutgoing,
  appToAppIncoming,
  appToPhone,
  phoneToApp,
}

/// Type of Audio Device
enum AudioDevice {
  speakerPhone,
  wiredHeadset,
  earpiece,
  bluetooth,
  none,
}

/// Type of Signaling State
enum StringeeSignalingState {
  calling,
  ringing,
  answered,
  busy,
  ended,
}

/// Type of Media State
enum StringeeMediaState {
  connected,
  disconnected,
}

/// Type of Video Quality
enum VideoQuality {
  normal,
  hd,
  fullHd,
}

///Type of Scaling type
enum ScalingType {
  fit,
  fill,
}

class MakeCallParams {
  String _from;
  String _to;
  bool _isVideoCall;
  Map<dynamic, dynamic> _customData;
  VideoQuality _videoQuality;

  MakeCallParams(
    String from,
    String to, {
    bool isVideoCall,
    Map<dynamic, dynamic> customData,
    VideoQuality videoQuality,
  })  : assert(from != null || from.trim().isNotEmpty),
        assert(to != null || to.trim().isNotEmpty) {
    this._from = from.trim();
    this._to = to.trim();
    this._isVideoCall = (isVideoCall != null) ? isVideoCall : false;
    if (customData != null) this._customData = customData;
    if (this._isVideoCall)
      this._videoQuality = (videoQuality != null) ? videoQuality : VideoQuality.normal;
  }

  VideoQuality get videoQuality => _videoQuality;

  Map<dynamic, dynamic> get customData => _customData;

  bool get isVideoCall => _isVideoCall;

  String get to => _to;

  String get from => _from;

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['from'] = this._from.trim();
    params['to'] = this._to.trim();
    if (this._customData != null) params['customData'] = this._customData;
    params['isVideoCall'] = this._isVideoCall;
    if (this._isVideoCall) {
      switch (this._videoQuality) {
        case VideoQuality.normal:
          params['videoResolution'] = "NORMAL";
          break;
        case VideoQuality.hd:
          params['videoResolution'] = "HD";
          break;
        case VideoQuality.fullHd:
          params['videoResolution'] = "FULLHD";
          break;
      }
    }
    return params;
  }
}
