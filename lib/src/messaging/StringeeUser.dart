import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// A user participating in a Stringee conversation.
class StringeeUser {
  String? _userId;
  String? _name;
  String? _avatarUrl;
  UserRole _role = UserRole.member;

  /// Creates a user with [userId] and optional profile information.
  StringeeUser({required String userId, String? name, String? avatarUrl}) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
  }

  /// Stringee user ID.
  String? get userId => _userId;

  /// Display name.
  String? get name => _name;

  /// Profile image URL.
  String? get avatarUrl => _avatarUrl;

  /// Role in the containing conversation.
  UserRole get role => _role;

  @override
  String toString() {
    return '{userId: $_userId, name: $name, avatarUrl: $avatarUrl, role: $role}';
  }

  /// Converts this user to a platform-channel payload.
  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId!.trim();
    if (_name != null) params['name'] = _name!.trim();
    if (_avatarUrl != null) params['avatarUrl'] = _avatarUrl!.trim();
    params['role'] = _role.index;
    return params;
  }

  /// Creates a user from native [json].
  StringeeUser.fromJson(Map<dynamic, dynamic> json) {
    this._userId = StringeeValueParser.toStringValue(json['user']);
    this._name = StringeeValueParser.toStringValue(json['displayName']);
    this._avatarUrl = StringeeValueParser.toStringValue(json['avatarUrl']);
    if (json.containsKey('role')) {
      String? role = StringeeValueParser.toStringValue(json['role']);
      switch (role) {
        case 'member':
          this._role = UserRole.member;
          break;
        case 'admin':
          this._role = UserRole.admin;
          break;
        default:
          this._role = UserRole.member;
          break;
      }
    }
  }
}
