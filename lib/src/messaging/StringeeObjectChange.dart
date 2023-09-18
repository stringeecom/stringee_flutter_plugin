import '../../stringee_flutter_plugin.dart';

class StringeeObjectChange {
  ChangeType? _type;
  ObjectType? _objectType;
  List<dynamic>? _objects;

  ChangeType? get type => _type;

  ObjectType? get objectType => _objectType;

  List<dynamic>? get objects => _objects;

  StringeeObjectChange(
      ChangeType type, ObjectType objectType, List<dynamic> objects) {
    this._type = type;
    this._objects = objects;
    this._objectType = objectType;
  }

  @override
  String toString() {
    return {
      if (_type != null) 'type': _type!.index,
      if (_objectType != null) 'objectType': _objectType!.index,
      if (_objects != null) 'objects': _objects!.toString(),
    }.toString();
  }
}
