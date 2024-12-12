enum AudioType {
  speakerPhone,
  wiredHeadset,
  earpiece,
  bluetooth,
  other,
  none,
}

extension AudioTypeX on AudioType {
  static AudioType fromValue(int? value) {
    switch (value) {
      case 0:
        return AudioType.speakerPhone;
      case 1:
        return AudioType.wiredHeadset;
      case 2:
        return AudioType.earpiece;
      case 3:
        return AudioType.bluetooth;
      case 4:
        return AudioType.other;
      default:
        return AudioType.none;
    }
  }
}
