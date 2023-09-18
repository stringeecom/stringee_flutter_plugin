class StringeeQueue {
  String? _id;
  String? _name;

  String? get id => _id;

  String? get name => _name;

  @override
  String toString() {
    return {
      if (_id != null) 'id': _id!.trim(),
      if (_name != null) 'name': _name!.trim(),
    }.toString();
  }

  StringeeQueue.fromJson(Map<dynamic, dynamic> json) {
    this._id = json['id'];
    this._name = json['name'];
  }
}
