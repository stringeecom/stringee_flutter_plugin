import '../helper/value_parser.dart';

/// A participant in a [StringeeVideoRoom].
class StringeeRoomUser {
  late String _id;

  /// Creates a participant from native room [info].
  StringeeRoomUser(Map<dynamic, dynamic> info) {
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
  }

  /// The participant's Stringee user ID.
  String get id => _id;

  @override
  String toString() {
    return '{id: $_id}';
  }
}
