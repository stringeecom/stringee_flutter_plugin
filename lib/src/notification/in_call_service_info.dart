import 'package:equatable/equatable.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class InCallServiceInfo extends Equatable {
  // Channel info
  final String channelId;
  final String channelName;
  final String channelDescription;
  final LockScreenVisibility lockscreenVisibility;

  // Notification info
  final int id;
  final String name;
  final String number;
  final String? avatarUrl;
  final String? contentTitle;
  final String? contentText;
  final NotificationIcon? icon;

  // Other information
  final String clientId;
  final String callId;

  InCallServiceInfo({
    required this.channelId,
    required this.channelName,
    required this.channelDescription,
    this.lockscreenVisibility = LockScreenVisibility.Private,
    required this.id,
    required this.name,
    required this.number,
    this.avatarUrl,
    this.contentTitle,
    this.contentText,
    this.icon,
    required this.clientId,
    required this.callId,
  });

  Map<String, dynamic> toJson() => {
        'channelId': channelId.trim(),
        'channelName': channelName.trim(),
        'channelDescription': channelDescription.trim(),
        'lockscreenVisibility': lockscreenVisibility.value,
        'id': id,
        'name': name,
        'number': number,
        if (avatarUrl != null) 'avatarUrl': avatarUrl,
        if (contentTitle != null) 'contentTitle': contentTitle?.trim(),
        if (contentText != null) 'contentText': contentText?.trim(),
        if (icon != null) 'icon': icon?.toJson(),
        'uuid': clientId,
        'callId': callId,
      };

  @override
  List<Object?> get props => [
        channelId,
        channelName,
        channelDescription,
        lockscreenVisibility,
        id,
        name,
        number,
        avatarUrl,
        contentTitle,
        contentText,
        icon,
        clientId,
        callId,
      ];
}
