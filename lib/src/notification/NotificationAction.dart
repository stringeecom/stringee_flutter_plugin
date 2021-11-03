import '../../stringee_flutter_plugin.dart';

typedef CallBack = void Function();

class NotificationAction {
  late int id;
  String icon;
  CallBack onPressed;
  String? title;

  NotificationAction(this.icon, this.onPressed, {this.title}) {
    id = new DateTime.now().millisecond;
    StringeeNotification().actionMap[id] = this;
  }

  void press(){
    onPressed();
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['icon'] = icon.trim();
    if (title != null) params['title'] = title;
    return params;
  }
}
