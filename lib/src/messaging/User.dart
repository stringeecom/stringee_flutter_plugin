import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

class User {
  String _userId;
  String _name;
  String _avatarUrl;
  UserRole _role;

  User({@required String userId, String name, String avatarUrl}) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
  }

  String get userId => _userId;

  String get name => _name;

  String get avatarUrl => _avatarUrl;

  UserRole get role => _role;

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId;
    if (_name != null) params['name'] = _name;
    if (_avatarUrl != null) params['avatarUrl'] = _avatarUrl;
    if (_role != null) params['role'] = _role.index;
    return params;
  }

  User.fromJson(Map<dynamic, dynamic> json) {
    this._userId = json['userId'];
    this._name = json['name'];
    this._avatarUrl = json['avatarUrl'];
    if (json.containsKey('role')) {
      String role = json['role'];
      switch (role) {
        case 'member':
          this._role = UserRole.MEMBER;
          break;
        case 'admin':
          this._role = UserRole.ADMIN;
          break;
      }
    }
  }

  User.fromJsonNotify(Map<dynamic, dynamic> json) {
    this._userId = json['user'];
    this._name = json['displayName'];
    this._avatarUrl = json['avatarUrl'];
    if (json.containsKey('role')) {
      String role = json['role'];
      switch (role) {
        case 'member':
          this._role = UserRole.MEMBER;
          break;
        case 'admin':
          this._role = UserRole.ADMIN;
          break;
      }
    }
  }
}
