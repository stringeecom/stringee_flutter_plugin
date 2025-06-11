import 'package:equatable/equatable.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class IncomingCallNotificationInfo extends Equatable {
  // Channel info
  final String channelId;
  final String channelName;
  final String channelDescription;
  final bool enableLights;
  final bool enableVibration;
  final List<int> vibrationPattern;
  final LockScreenVisibility lockscreenVisibility;
  final NotificationSound? notificationSound;

  // Notification info
  final int id;
  final String fromName;
  final String fromNumber;
  final String? fromAvatarUrl;
  final NotificationIcon? icon;
  final String? contentTitle;
  final String? contentText;

  // Other information
  final String clientId;
  final String callId;

  IncomingCallNotificationInfo({
    required this.channelId,
    required this.channelName,
    required this.channelDescription,
    this.enableLights = false,
    this.enableVibration = true,
    this.vibrationPattern = const [0, 350, 500],
    this.lockscreenVisibility = LockScreenVisibility.Private,
    this.notificationSound,
    required this.id,
    required this.fromName,
    required this.fromNumber,
    this.fromAvatarUrl,
    this.icon,
    this.contentTitle,
    this.contentText,
    required this.clientId,
    required this.callId,
  });

  Map<String, dynamic> toJson() => {
        'channelId': channelId.trim(),
        'channelName': channelName.trim(),
        'channelDescription': channelDescription.trim(),
        'enableLights': enableLights,
        'enableVibration': enableVibration,
        'vibrationPattern': vibrationPattern,
        'lockscreenVisibility': lockscreenVisibility.value,
        'notificationSound': notificationSound != null
            ? notificationSound?.toJson()
            : NotificationSound(sourceType: SourceType.System).toJson(),
        'id': id,
        'fromName': fromName.trim(),
        'fromNumber': fromNumber.trim(),
        if (fromAvatarUrl != null) 'fromAvatarUrl': fromAvatarUrl!.trim(),
        if (icon != null) 'icon': icon!.toJson(),
        if (contentTitle != null) 'contentTitle': contentTitle!.trim(),
        if (contentText != null) 'contentText': contentText!.trim(),
        'uuid': clientId.trim(),
        'callId': callId.trim(),
      };

  @override
  List<Object?> get props => [
        channelId,
        channelName,
        channelDescription,
        enableLights,
        enableVibration,
        vibrationPattern,
        lockscreenVisibility,
        notificationSound,
        id,
        fromName,
        fromNumber,
        fromAvatarUrl,
        icon,
        contentTitle,
        contentText,
        clientId,
        callId,
      ];
}
