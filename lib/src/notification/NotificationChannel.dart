import 'dart:typed_data';

enum NotificationImportance {
  None,
  Min,
  Low,
  Default,
  High,
  Max,
}

enum NotificationRingtoneType {
  Notification,
  Ringtone,
  Alarm,
}

class NotificationChannel {
  late String channelId;
  late String channelName;
  late String description;
  NotificationImportance importance = NotificationImportance.Default;
  bool? enableLights;
  bool? enableVibration;
  Int64List? vibrationPattern;
  bool? lockscreenVisibility;
  bool? playSound;
  String? soundSource;
  NotificationRingtoneType? defaultRingtoneType;

  NotificationChannel(
    String channelId,
    String channelName,
    String description, {
    NotificationImportance? importance,
    bool? enableLights,
    bool? enableVibration,
    Int64List? vibrationPattern,
    bool? lockscreenVisibility,
    bool? playSound,
    String? soundSource,
    NotificationRingtoneType? defaultRingtoneType,
  }) {
    this.channelId = channelId;
    this.channelName = channelName;
    this.description = description;
    if (importance != null) this.importance = importance;
    if (enableLights != null) this.enableLights = enableLights;
    if (enableVibration != null) this.enableVibration = enableVibration;
    if (vibrationPattern != null) this.vibrationPattern = vibrationPattern;
    if (lockscreenVisibility != null)
      this.lockscreenVisibility = lockscreenVisibility;
    if (playSound != null) this.playSound = playSound;
    if (soundSource != null) this.soundSource = soundSource;
    if (defaultRingtoneType != null)
      this.defaultRingtoneType = defaultRingtoneType;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['channelId'] = channelId.trim();
    params['channelName'] = channelName.trim();
    params['description'] = description.trim();
    params['importance'] = importance.index;
    if (enableLights != null) params['enableLights'] = enableLights;
    if (enableVibration != null) params['enableVibration'] = enableVibration;
    if (vibrationPattern != null) params['vibrationPattern'] = vibrationPattern;
    if (lockscreenVisibility != null)
      params['lockscreenVisibility'] = lockscreenVisibility;
    if (playSound != null) params['playSound'] = playSound;
    if (soundSource != null) params['enableLights'] = soundSource;
    if (defaultRingtoneType != null)
      params['enableLights'] = defaultRingtoneType!.index;
    return params;
  }
}
