class StringeeRoomUser {
  late String _id;

  StringeeRoomUser(Map<dynamic, dynamic> info) {
    this._id = info['id'];
  }

  String get id => _id;

  @override
  String toString() {
    return '{id: $_id}';
  }
}

