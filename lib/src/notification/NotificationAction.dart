import 'package:stringee_flutter_plugin/src/notification/NotificationIcon.dart';

import '../../stringee_flutter_plugin.dart';

typedef CallBack = void Function();

class NotificationAction {
  late int id;
  NotificationIcon? icon;
  CallBack onPressed;
  String? title;
  bool? isOpenApp = true;

  NotificationAction(
    this.title,
    this.onPressed, {
    NotificationIcon? icon,
    bool? isOpenApp,
  }) {
    id = new DateTime.now().millisecond;
    StringeeNotification().actionMap[id] = this;
    if (icon != null) this.icon = icon;
    if (isOpenApp != null) this.isOpenApp = isOpenApp;
  }

  void press() {
    onPressed();
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['id'] = id;
    if (icon != null) params['icon'] = icon!.toJson();
    params['title'] = title;
    params['isOpenApp'] = isOpenApp;
    return params;
  }
}
