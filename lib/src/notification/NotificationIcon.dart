import '../../stringee_flutter_plugin.dart';

class NotificationIcon {
  String source;
  IconSourceFrom sourceFrom;

  NotificationIcon(this.source, this.sourceFrom);

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['source'] = source.trim();
    params['sourceFrom'] = sourceFrom.index;
    return params;
  }
}
