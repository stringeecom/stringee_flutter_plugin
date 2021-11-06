import 'package:stringee_flutter_plugin/src/notification/NotificationIcon.dart';

import '../../stringee_flutter_plugin.dart';

class NotificationAndroid {
  int id;
  String channelId;
  String? contentTitle;
  String? contentText;
  String? subText;
  String? contentInfo;
  int? number = 0;
  bool? autoCancel = true;
  int? showWhen = 0;
  NotificationIcon? icon;
  bool? playSound = true;
  NotificationSound? notificationSound = NotificationSound.system();
  NotificationCategory? category;
  bool? fullScreenIntent = false;
  List<int>? vibrationPattern = [];
  bool? onGoing = false;
  bool? onlyAlertOnce = false;
  int? timeoutAfter = 0;
  NotificationPriority? priority = NotificationPriority.Default;
  List<NotificationAction>? actions = [];

  NotificationAndroid(
    this.id,
    this.channelId, {
    String? contentTitle,
    String? contentText,
    String? subText,
    String? contentInfo,
    int? number,
    bool? autoCancel,
    int? showWhen,
    NotificationIcon? icon,
    bool? playSound,
    NotificationSound? notificationSound,
    NotificationCategory? category,
    bool? fullScreenIntent,
    List<int>? vibrationPattern,
    bool? onGoing,
    bool? onlyAlertOnce,
    int? timeoutAfter,
    NotificationPriority? priority,
    List<NotificationAction>? actions,
  }) {
    if (contentTitle != null) this.contentTitle = contentTitle;
    if (contentText != null) this.contentText = contentText;
    if (subText != null) this.subText = subText;
    if (contentInfo != null) this.contentInfo = contentInfo;
    if (number != null) this.number = number;
    if (autoCancel != null) this.autoCancel = autoCancel;
    if (showWhen != null) this.showWhen = showWhen;
    if (icon != null) this.icon = icon;
    if (playSound != null) this.playSound = playSound;
    if (notificationSound != null) this.notificationSound = notificationSound;
    if (category != null) this.category = category;
    if (fullScreenIntent != null) this.fullScreenIntent = fullScreenIntent;
    if (vibrationPattern != null) this.vibrationPattern = vibrationPattern;
    if (onGoing != null) this.onGoing = onGoing;
    if (onlyAlertOnce != null) this.onlyAlertOnce = onlyAlertOnce;
    if (timeoutAfter != null) this.timeoutAfter = timeoutAfter;
    if (priority != null) this.priority = priority;
    if (actions != null) this.actions = actions;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['id'] = id;
    params['channelId'] = channelId.trim();
    params['number'] = number;
    params['autoCancel'] = autoCancel;
    params['showWhen'] = showWhen;
    params['playSound'] = playSound;
    params['fullScreenIntent'] = fullScreenIntent;
    params['vibrationPattern'] = vibrationPattern;
    params['onGoing'] = onGoing;
    params['onlyAlertOnce'] = onlyAlertOnce;
    params['timeoutAfter'] = timeoutAfter;
    params['priority'] = priority.value;
    params['actions'] = (actions!.length > 0)
        ? actions!.map((action) => action.toJson()).toList()
        : actions;
    params['notificationSound'] = notificationSound!.toJson();
    if (contentTitle != null) params['contentTitle'] = contentTitle!.trim();
    if (contentText != null) params['contentText'] = contentText!.trim();
    if (subText != null) params['subText'] = subText!.trim();
    if (contentInfo != null) params['contentInfo'] = contentInfo!.trim();
    if (icon != null) params['icon'] = icon!.toJson();
    if (category != null) params['category'] = category.value;
    return params;
  }
}
