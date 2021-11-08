import 'package:stringee_flutter_plugin/src/notification/NotificationIcon.dart';

import '../../stringee_flutter_plugin.dart';

class NotificationAction {
  late String id;
  NotificationIcon? icon;
  String title;

  NotificationAction({
    required this.id,
    required this.title,
    NotificationIcon? icon,
  }) {
    if (icon != null) this.icon = icon;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['id'] = id;
    if (icon != null) params['icon'] = icon!.toJson();
    params['title'] = title;
    return params;
  }
}
