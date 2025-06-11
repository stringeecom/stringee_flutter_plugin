import 'package:equatable/equatable.dart';

enum LockScreenVisibility {
  Secret,
  Private,
  Public,
}

extension LockScreenVisibilityX on LockScreenVisibility? {
  int get value {
    switch (this) {
      case LockScreenVisibility.Secret:
        return -1;
      case LockScreenVisibility.Private:
        return 0;
      case LockScreenVisibility.Public:
        return 1;
      default:
        return 0;
    }
  }
}

enum SourceType {
  Raw,
  Uri,
  System,
}

enum IconSourceFrom {
  Drawable,
  Mipmap,
}

class NotificationIcon extends Equatable {
  final String source;
  final IconSourceFrom sourceFrom;

  NotificationIcon({required this.source, required this.sourceFrom});

  Map<String, dynamic> toJson() => {
        'source': source.trim(),
        'sourceFrom': sourceFrom.index,
      };

  @override
  List<Object?> get props => [source, sourceFrom];
}

class NotificationSound extends Equatable {
  final String? source;
  final SourceType sourceType;

  NotificationSound({this.source, this.sourceType = SourceType.System});

  Map<String, dynamic> toJson() => {
        if (source != null) 'source': source!.trim(),
        'sourceType': sourceType.index
      };

  @override
  List<Object?> get props => [source, sourceType];
}
