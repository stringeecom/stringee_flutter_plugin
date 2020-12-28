import '../StringeeConstants.dart';
class StringeeChange {
  Type _type;
  ObjectType _objectType;
  StringeeObject _object;

  StringeeChange(Type type, StringeeObject _object) {
    this._type = type;
    this._object = _object;
    _objectType = _object.type;
  }

  StringeeObject get object => _object;

  ObjectType get objectType => _objectType;

  Type get changeType => _type;
}
