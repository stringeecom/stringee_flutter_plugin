import '../helper/value_parser.dart';

class StringeeRoomUser {
  late String _id;

  StringeeRoomUser(Map<dynamic, dynamic> info) {
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
  }

  String get id => _id;

  @override
  String toString() {
    return '{id: $_id}';
  }
}
