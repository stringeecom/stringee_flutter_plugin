import 'package:flutter/cupertino.dart';

class User {
  String _userId;
  String _name;
  String _avatarUrl;

  User({@required String userId, String name, String avatarUrl}) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
  }

  String get userId => _userId;

  String get name => _name;

  String get avatarUrl => _avatarUrl;

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId;
    if (_name != null) params['name'] = _name;
    if (_avatarUrl != null) params['avatarUrl'] = _avatarUrl;
    return params;
  }

  User.fromJson(Map<dynamic, dynamic> json) {
    this._userId = json['userId'];
    this._name = json['name'];
    this._avatarUrl = json['avatarUrl'];
  }

  User.fromJsonNotify(Map<dynamic, dynamic> json) {
    this._userId = json['user'];
    this._name = json['displayName'];
    this._avatarUrl = json['avatarUrl'];
  }
}
