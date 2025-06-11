import 'package:equatable/equatable.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class ScreenCaptureServiceInfo extends Equatable {
  // Channel info
  final String channelId;
  final String channelName;
  final String channelDescription;
  final LockScreenVisibility lockscreenVisibility;

  // Notification info
  final int id;
  final NotificationIcon? icon;
  final String? contentTitle;
  final String? contentText;

  // Other information
  final String clientId;
  final String callId;

  ScreenCaptureServiceInfo({
    required this.channelId,
    required this.channelName,
    required this.channelDescription,
    this.lockscreenVisibility = LockScreenVisibility.Private,
    required this.id,
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
        'lockscreenVisibility': lockscreenVisibility.value,
        'id': id,
        'uuid': clientId.trim(),
        'callId': callId.trim(),
        if (icon != null) 'icon': icon?.toJson(),
        if (contentTitle != null) 'contentTitle': contentTitle?.trim(),
        if (contentText != null) 'contentText': contentText?.trim(),
      };

  @override
  List<Object?> get props => [
        channelId,
        channelName,
        channelDescription,
        lockscreenVisibility,
        id,
        icon,
        contentTitle,
        contentText,
        clientId,
        callId,
      ];
}
