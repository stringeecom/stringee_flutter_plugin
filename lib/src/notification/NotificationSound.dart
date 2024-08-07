import '../../stringee_plugin.dart';

@Deprecated('This class will be removed in the next major release.')
class NotificationSound {
  String? source;
  SourceType sourceType = SourceType.System;
  NotificationRingtoneType ringtoneType = NotificationRingtoneType.Notification;

  NotificationSound.raw(this.source) {
    sourceType = SourceType.Raw;
  }

  NotificationSound.uri(this.source) {
    sourceType = SourceType.Uri;
  }

  NotificationSound.system({NotificationRingtoneType? ringtoneType}) {
    if (ringtoneType != null) this.ringtoneType = ringtoneType;
    sourceType = SourceType.System;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    if (source != null) params['source'] = source!.trim();
    params['sourceType'] = sourceType.index;
    params['ringtoneType'] = ringtoneType.value;
    return params;
  }
}
