import '../StringeeConstants.dart';

class StringeeObjectChange {
  ChangeType _type;
  ObjectType _objectType;
  List<dynamic> _objects;

  ChangeType get type => _type;

  ObjectType get objectType => _objectType;

  List<dynamic> get objects => _objects;

  StringeeObjectChange(ChangeType type, ObjectType objectType, List<dynamic> objects) {
    this._type = type;
    this._objects = objects;
    this._objectType = objectType;
  }
}
