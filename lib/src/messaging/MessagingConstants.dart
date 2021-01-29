/// Chat object type
enum ObjectType {
  conversation,
  message,
}

/// Chat change type
enum ChangeType {
  insert,
  update,
  delete,
}

/// Role of user
enum UserRole {
  admin,
  member,
}

/// [message]'s State
enum MsgState {
  initialize,
  sending,
  sent,
  delivered,
  read,
}

/// Type of [message]
enum MsgType {
  text,
  photo,
  video,
  audio,
  file,
  link,
  createConversation,
  renameConversation,
  location,
  contact,
  sticker,
  notification,
}

extension MsgTypeValueExtension on MsgType {
  // ignore: missing_return
  int get value {
    switch (this) {
      case MsgType.text:
        return 1;
        break;
      case MsgType.photo:
        return 2;
        break;
      case MsgType.video:
        return 3;
        break;
      case MsgType.audio:
        return 4;
        break;
      case MsgType.file:
        return 5;
        break;
      case MsgType.link:
        return 6;
        break;
      case MsgType.createConversation:
        return 7;
        break;
      case MsgType.renameConversation:
        return 8;
        break;
      case MsgType.location:
        return 9;
        break;
      case MsgType.contact:
        return 10;
        break;
      case MsgType.sticker:
        return 11;
        break;
      case MsgType.notification:
        return 100;
        break;
    }
  }
}

extension MsgTypeExtension on int {
  // ignore: missing_return
  MsgType get msgType {
    switch (this) {
      case 1:
        return MsgType.text;
        break;
      case 2:
        return MsgType.photo;
        break;
      case 3:
        return MsgType.video;
        break;
      case 4:
        return MsgType.audio;
        break;
      case 5:
        return MsgType.file;
        break;
      case 6:
        return MsgType.link;
        break;
      case 7:
        return MsgType.createConversation;
        break;
      case 8:
        return MsgType.renameConversation;
        break;
      case 9:
        return MsgType.location;
        break;
      case 10:
        return MsgType.contact;
        break;
      case 11:
        return MsgType.sticker;
        break;
      case 100:
        return MsgType.notification;
        break;
    }
  }
}

/// Type of noti [message]
enum MsgNotifyType {
  addParticipants,
  removeParticipants,
  changeGroupName,
}

extension MsgNotifyTypeExtension on int {
  // ignore: missing_return
  MsgNotifyType get notifyType {
    switch (this) {
      case 1:
        return MsgNotifyType.addParticipants;
        break;
      case 2:
        return MsgNotifyType.removeParticipants;
        break;
      case 3:
        return MsgNotifyType.changeGroupName;
        break;
    }
  }
}
