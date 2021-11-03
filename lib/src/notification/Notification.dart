import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:stringee_flutter_plugin/src/notification/NotificationAction.dart';

enum NotificationCategory {
  Call,
  Navigation,
  Message,
  Email,
  Event,
  Promo,
  Alarm,
  Progress,
  Social,
  Error,
  Transport,
  System,
  Service,
  Reminder,
  Recommendation,
  Status,
  Workout,
  LocationSharing,
  Stopwatch,
  MissedCall,
}

extension NotificationCategoryValueExtension on NotificationCategory? {
  String? get value {
    switch (this) {
      case NotificationCategory.Call:
        return 'call';
      case NotificationCategory.Navigation:
        return 'navigation';
      case NotificationCategory.Message:
        return 'msg';
      case NotificationCategory.Email:
        return 'email';
      case NotificationCategory.Event:
        return 'event';
      case NotificationCategory.Promo:
        return 'promo';
      case NotificationCategory.Alarm:
        return 'alarm';
      case NotificationCategory.Progress:
        return 'progress';
      case NotificationCategory.Social:
        return 'social';
      case NotificationCategory.Error:
        return 'err';
      case NotificationCategory.Transport:
        return 'transport';
      case NotificationCategory.System:
        return 'sys';
      case NotificationCategory.Service:
        return 'reminder';
      case NotificationCategory.Reminder:
        return 'event';
      case NotificationCategory.Recommendation:
        return 'recommendation';
      case NotificationCategory.Status:
        return 'status';
      case NotificationCategory.Workout:
        return 'workout';
      case NotificationCategory.LocationSharing:
        return 'location_sharing';
      case NotificationCategory.Stopwatch:
        return 'stopwatch';
      case NotificationCategory.MissedCall:
        return 'missed_call';
      default:
        return null;
    }
  }
}

enum NotificationPriority {
  Min,
  Low,
  Default,
  High,
  Max,
}

extension NotificationPriorityValueExtension on NotificationPriority? {
  int get value {
    switch (this) {
      case NotificationPriority.Min:
        return -2;
      case NotificationPriority.Low:
        return -1;
      case NotificationPriority.Default:
        return 0;
      case NotificationPriority.High:
        return 1;
      case NotificationPriority.Max:
        return 2;
      default:
        return 0;
    }
  }
}

class NotificationSound {
  String sound;
  AudioStreamType? streamType;

  NotificationSound(this.sound, {this.streamType});

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['sound'] = sound.trim();
    if (streamType != null) params['streamType'] = streamType;
    return params;
  }
}

enum AudioStreamType {
  VoiceCall,
  System,
  Ring,
  Music,
  Alarm,
  Notification,
  Dtmf,
  Accessibility,
}

class Notification {
  int id;
  String channelId;
  String? contentTitle;
  String? contentText;
  String? subText;
  String? contentInfo;
  int? number;
  bool? autoCancel;
  bool? showWhen;
  int? when;
  String? icon;
  NotificationSound? sound;
  bool? silent;
  NotificationCategory? category;
  bool? fullScreenIntent;
  bool? highPriority;
  Int64List? vibrationPattern;
  bool? onGoing;
  bool? onlyAlertOnce;
  int? timeoutAfter;
  NotificationPriority? priority;
  List<NotificationAction>? actions;

  Notification(this.id, this.channelId,
      {this.contentTitle,
      this.contentText,
      this.subText,
      this.contentInfo,
      this.number,
      this.autoCancel,
      this.showWhen,
      this.when,
      this.icon,
      this.sound,
      this.silent,
      this.category,
      this.fullScreenIntent,
      this.highPriority,
      this.vibrationPattern,
      this.onGoing,
      this.onlyAlertOnce,
      this.timeoutAfter,
      this.priority,
      this.actions});

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['id'] = id;
    params['channelId'] = channelId.trim();
    if (contentTitle != null) params['contentTitle'] = contentTitle!.trim();
    if (subText != null) params['subText'] = subText!.trim();
    if (contentInfo != null) params['contentInfo'] = contentInfo!.trim();
    if (number != null) params['number'] = number;
    if (autoCancel != null) params['autoCancel'] = autoCancel;
    if (showWhen != null) params['showWhen'] = showWhen;
    if (when != null) params['when'] = when;
    if (icon != null) params['icon'] = icon!.trim();
    if (sound != null) params['sound'] = json.encode(sound);
    if (silent != null) params['silent'] = silent;
    if (category != null) params['category'] = category.value;
    if (fullScreenIntent != null) params['fullScreenIntent'] = fullScreenIntent;
    if (highPriority != null) params['highPriority'] = highPriority;
    if (vibrationPattern != null) params['vibrationPattern'] = vibrationPattern;
    if (onGoing != null) params['onGoing'] = onGoing;
    if (onlyAlertOnce != null) params['onlyAlertOnce'] = onlyAlertOnce;
    if (timeoutAfter != null) params['timeoutAfter'] = timeoutAfter;
    if (priority != null) params['priority'] = priority.value;
    if (actions != null) params['actions'] = json.encode(actions);
    return params;
  }
}
