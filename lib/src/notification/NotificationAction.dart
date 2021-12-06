import 'package:stringee_flutter_plugin/src/notification/NotificationIcon.dart';

import '../../stringee_flutter_plugin.dart';

class NotificationAction {
  late String id;
  NotificationIcon? icon;
  String title;
  bool? recreateTask = false;

  NotificationAction(
      {required this.id,
      required this.title,
      NotificationIcon? icon,
      bool? recreateTask = true}) {
    if (icon != null) this.icon = icon;
    if (recreateTask != null) this.recreateTask = recreateTask;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['id'] = id;
    if (icon != null) params['icon'] = icon!.toJson();
    params['title'] = title;
    params['recreateTask'] = recreateTask;
    return params;
  }
}
