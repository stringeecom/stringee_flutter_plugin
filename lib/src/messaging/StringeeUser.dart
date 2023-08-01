import 'package:stringee_flutter_plugin/src/StringeeConstants.dart';

class StringeeUser {
  String? _userId;
  String? _name;
  String? _avatarUrl;
  UserRole _role = UserRole.member;

  StringeeUser({required String userId, String? name, String? avatarUrl}) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
  }

  String? get userId => _userId;

  String? get name => _name;

  String? get avatarUrl => _avatarUrl;

  UserRole get role => _role;

  @override
  String toString() {
    return '{userId: $_userId, name: $name, avatarUrl: $avatarUrl, role: $role}';
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId!.trim();
    if (_name != null) params['name'] = _name!.trim();
    if (_avatarUrl != null) params['avatarUrl'] = _avatarUrl!.trim();
    params['role'] = _role.name;
    return params;
  }

  StringeeUser.fromJson(Map<dynamic, dynamic> json) {
    this._userId = json['user'];
    this._name = json['displayName'];
    this._avatarUrl = json['avatarUrl'];
    if (json.containsKey('role')) {
      String? role = json['role'];
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
