import '../../stringee_plugin.dart';

@Deprecated('This class will be removed in the next major release.')
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
