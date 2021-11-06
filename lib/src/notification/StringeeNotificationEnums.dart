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

enum NotificationImportance {
  None,
  Min,
  Low,
  Default,
  High,
  Max,
}

enum NotificationLockScreenVisibility {
  Secret,
  Private,
  Public,
}

extension NotificationLockScreenVisibilityValueExtension
on NotificationLockScreenVisibility? {
  int get value {
    switch (this) {
      case NotificationLockScreenVisibility.Secret:
        return -1;
      case NotificationLockScreenVisibility.Private:
        return 0;
      case NotificationLockScreenVisibility.Public:
        return 1;
      default:
        return 0;
    }
  }
}

enum SourceType {
  Raw,
  Uri,
  System,
}

enum NotificationRingtoneType {
  Notification,
  Ringtone,
  Alarm,
}

extension NotificationRingtoneTypeValueExtension
on NotificationRingtoneType? {
  int get value {
    switch (this) {
      case NotificationRingtoneType.Notification:
        return 2;
      case NotificationRingtoneType.Ringtone:
        return 1;
      case NotificationRingtoneType.Alarm:
        return 4;
      default:
        return 2;
    }
  }
}

enum IconSourceFrom{
  Drawable,
  Mipmap,
}