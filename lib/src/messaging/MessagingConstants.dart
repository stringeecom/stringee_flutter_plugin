///Chat object type
enum ObjectType {
  CONVERSATION,
  MESSAGE,
}

///Chat change type
enum ChangeType {
  INSERT,
  UPDATE,
  DELETE,
}

///Conversation state
enum ConvState {
  STATE_DEFAULT,
  STATE_LEFT,
}

enum MsgState {
  INITIALIZE,
  SENDING,
  SENT,
  DELIVERED,
  READ,
}

enum MsgStateType {
  TYPE_SEND,
  TYPE_RECEIVE,
}

enum MsgType {
  TYPE_TEXT,
  TYPE_PHOTO,
  TYPE_VIDEO,
  TYPE_AUDIO,
  TYPE_FILE,
  TYPE_LINK,
  TYPE_CREATE_CONVERSATION,
  TYPE_RENAME_CONVERSATION,
  TYPE_LOCATION,
  TYPE_CONTACT,
  TYPE_STICKER,
  TYPE_NOTIFICATION,
  TYPE_TEMP_DATE,
}

extension MsgTypeValueExtension on MsgType {
  // ignore: missing_return
  int get value {
    switch (this) {
      case MsgType.TYPE_TEXT:
        return 1;
        break;
      case MsgType.TYPE_PHOTO:
        return 2;
        break;
      case MsgType.TYPE_VIDEO:
        return 3;
        break;
      case MsgType.TYPE_AUDIO:
        return 4;
        break;
      case MsgType.TYPE_FILE:
        return 5;
        break;
      case MsgType.TYPE_LINK:
        return 6;
        break;
      case MsgType.TYPE_CREATE_CONVERSATION:
        return 7;
        break;
      case MsgType.TYPE_RENAME_CONVERSATION:
        return 8;
        break;
      case MsgType.TYPE_LOCATION:
        return 9;
        break;
      case MsgType.TYPE_CONTACT:
        return 10;
        break;
      case MsgType.TYPE_STICKER:
        return 11;
        break;
      case MsgType.TYPE_NOTIFICATION:
        return 100;
        break;
      case MsgType.TYPE_TEMP_DATE:
        return 1000;
        break;
    }
  }
}

extension MsgTypeExtension on int {
  // ignore: missing_return
  MsgType get msgType {
    switch (this) {
      case 1:
        return MsgType.TYPE_TEXT;
        break;
      case 2:
        return MsgType.TYPE_PHOTO;
        break;
      case 3:
        return MsgType.TYPE_VIDEO;
        break;
      case 4:
        return MsgType.TYPE_AUDIO;
        break;
      case 5:
        return MsgType.TYPE_FILE;
        break;
      case 6:
        return MsgType.TYPE_LINK;
        break;
      case 7:
        return MsgType.TYPE_CREATE_CONVERSATION;
        break;
      case 6:
        return MsgType.TYPE_RENAME_CONVERSATION;
        break;
      case 7:
        return MsgType.TYPE_LOCATION;
        break;
      case 10:
        return MsgType.TYPE_CONTACT;
        break;
      case 11:
        return MsgType.TYPE_STICKER;
        break;
      case 100:
        return MsgType.TYPE_NOTIFICATION;
        break;
      case 1000:
        return MsgType.TYPE_TEMP_DATE;
        break;
    }
  }
}
