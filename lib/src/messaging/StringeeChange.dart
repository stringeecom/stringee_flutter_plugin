import '../StringeeConstants.dart';
import 'Conversation.dart';
import 'Message.dart';

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
    if (object is Conversation) {
      return ObjectType.CONVERSATION;
    } else if (object is Message) {
      return ObjectType.MESSAGE;
    } else {
      throw new ArgumentError("Invalid object type: " + object.toString());
    }
  }
}
