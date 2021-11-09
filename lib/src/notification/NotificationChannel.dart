import '../../stringee_flutter_plugin.dart';

class NotificationChannel {
  late String channelId;
  late String channelName;
  late String description;
  NotificationImportance importance = NotificationImportance.Default;
  bool? enableLights = true;
  bool? enableVibration = true;
  List<int>? vibrationPattern = [];
  NotificationLockScreenVisibility lockscreenVisibility =
      NotificationLockScreenVisibility.Private;
  bool? playSound = true;
  NotificationSound? notificationSound = NotificationSound.system();
  bool bypassDnd = false;

  NotificationChannel(
    this.channelId,
    this.channelName,
    this.description, {
    NotificationImportance? importance,
    bool? enableLights,
    bool? enableVibration,
    List<int>? vibrationPattern,
    NotificationLockScreenVisibility? lockscreenVisibility,
    bool? playSound,
    NotificationSound? notificationSound,
    bool? autoReset,
    bool? bypassDnd,
  }) {
    if (importance != null) this.importance = importance;
    if (enableLights != null) this.enableLights = enableLights;
    if (enableVibration != null) this.enableVibration = enableVibration;
    if (vibrationPattern != null) this.vibrationPattern = vibrationPattern;
    if (lockscreenVisibility != null)
      this.lockscreenVisibility = lockscreenVisibility;
    if (playSound != null) this.playSound = playSound;
    if (notificationSound != null) this.notificationSound = notificationSound;
    if (bypassDnd != null) this.bypassDnd = bypassDnd;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['channelId'] = channelId.trim();
    params['channelName'] = channelName.trim();
    params['description'] = description.trim();
    params['importance'] = importance.index;
    params['lockscreenVisibility'] = lockscreenVisibility.value;
    params['bypassDnd'] = bypassDnd;
    params['notificationSound'] = notificationSound!.toJson();
    if (enableLights != null) params['enableLights'] = enableLights;
    if (enableVibration != null) params['enableVibration'] = enableVibration;
    if (vibrationPattern != null) params['vibrationPattern'] = vibrationPattern;
    if (playSound != null) params['playSound'] = playSound;
    return params;
  }
}
