class User {
  String _userId;
  String _name;
  String _avatarUrl;

  User(this._userId,
      this._name,
      this._avatarUrl,);

  String get userId => _userId;

  String get name => _name;

  String get avatarUrl => _avatarUrl;

  Map<String, dynamic> toJson() {
    return {
      'userId': _userId,
      'name': _name,
      'avatarUrl': _avatarUrl,
    };
  }

  User.fromJson(Map<dynamic, dynamic> json){
    this._userId = json['userId'];
    this._name = json['name'];
    this._avatarUrl = json['avatarUrl'];
  }
}
