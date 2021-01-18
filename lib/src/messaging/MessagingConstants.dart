/// Chat object type
enum ObjectType {
  Conversation,
  Message,
}

/// Chat change type
enum ChangeType {
  Insert,
  Update,
  Delete,
}

/// Role of user
enum UserRole {
  Admin,
  Member,
}

/// [Message]'s State
enum MsgState {
  Initialize,
  Sending,
  Sent,
  Delivered,
  Read,
}

/// Type of [Message]
enum MsgType {
  Text,
  Photo,
  Video,
  Audio,
  File,
  Link,
  CreateConversation,
  RenameConversation,
  Location,
  Contact,
  Sticker,
  Notification,
}

extension MsgTypeValueExtension on MsgType {
  // ignore: missing_return
  int get value {
    switch (this) {
      case MsgType.Text:
        return 1;
        break;
      case MsgType.Photo:
        return 2;
        break;
      case MsgType.Video:
        return 3;
        break;
      case MsgType.Audio:
        return 4;
        break;
      case MsgType.File:
        return 5;
        break;
      case MsgType.Link:
        return 6;
        break;
      case MsgType.CreateConversation:
        return 7;
        break;
      case MsgType.RenameConversation:
        return 8;
        break;
      case MsgType.Location:
        return 9;
        break;
      case MsgType.Contact:
        return 10;
        break;
      case MsgType.Sticker:
        return 11;
        break;
      case MsgType.Notification:
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
        return MsgType.Text;
        break;
      case 2:
        return MsgType.Photo;
        break;
      case 3:
        return MsgType.Video;
        break;
      case 4:
        return MsgType.Audio;
        break;
      case 5:
        return MsgType.File;
        break;
      case 6:
        return MsgType.Link;
        break;
      case 7:
        return MsgType.CreateConversation;
        break;
      case 8:
        return MsgType.RenameConversation;
        break;
      case 9:
        return MsgType.Location;
        break;
      case 10:
        return MsgType.Contact;
        break;
      case 11:
        return MsgType.Sticker;
        break;
      case 100:
        return MsgType.Notification;
        break;
    }
  }
}

/// Type of noti [Message]
enum MsgNotifyType {
  AddParticipants,
  RemoveParticipants,
  ChangeGroupName,
  EndConversation,
}

extension MsgNotifyTypeExtension on int {
  // ignore: missing_return
  MsgNotifyType get notifyType {
    switch (this) {
      case 1:
        return MsgNotifyType.AddParticipants;
        break;
      case 2:
        return MsgNotifyType.RemoveParticipants;
        break;
      case 3:
        return MsgNotifyType.ChangeGroupName;
        break;
      case 4:
        return MsgNotifyType.EndConversation;
        break;
    }
  }
}
