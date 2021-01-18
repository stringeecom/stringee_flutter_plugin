import '../StringeeConstants.dart';
import 'StringeeConversation.dart';
import 'StringeeMessage.dart';

class StringeeObject {}

class StringeeChange {
  ChangeType _type;
  ObjectType _objectType;
  StringeeObject _object;

  StringeeChange(ChangeType type, StringeeObject _object) {
    this._type = type;
    this._object = _object;
    _objectType = getType(_object);
  }

  StringeeObject get object => _object;

  ObjectType get objectType => _objectType;

  ChangeType get changeType => _type;

  ObjectType getType(StringeeObject object) {
    if (object is StringeeConversation) {
      return ObjectType.Conversation;
    } else if (object is StringeeMessage) {
      return ObjectType.Message;
    } else {
      throw new ArgumentError("Invalid object type: " + object.toString());
    }
  }
}
