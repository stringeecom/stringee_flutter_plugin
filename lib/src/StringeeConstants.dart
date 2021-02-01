import 'package:flutter/material.dart';

/// Events for StringeeClient
enum StringeeClientEvents {
  didConnect,
  didDisconnect,
  didFailWithError,
  requestAccessToken,
  didReceiveCustomMessage,
  incomingCall,
  incomingCall2,
  didReceiveObjectChange,
}

/// Events for StringeeCall
enum StringeeCallEvents {
  didChangeSignalingState,
  didChangeMediaState,
  didReceiveCallInfo,
  didHandleOnAnotherDevice,
  didReceiveLocalStream,
  didReceiveRemoteStream,
  didChangeAudioDevice
}

/// Events for StringeeCall2
enum StringeeCall2Events {
  didChangeSignalingState,
  didChangeMediaState,
  didReceiveCallInfo,
  didHandleOnAnotherDevice,
  didReceiveLocalStream,
  didReceiveRemoteStream,
  didChangeAudioDevice
}

/// Type of event
enum StringeeObjectEventType {
  client,
  call,
  call2,
}

/// Error code and message in flutter:
/// -1 : StringeeClient is not initialized or disconnected
/// -2 : value is invalid
/// -3 : Object is not found
/// -4 : This function work only for Android
Future<Map<String, dynamic>> reportInvalidValue(String value) async {
  Map<String, dynamic> params = {
    'status': false,
    'code': -2,
    'message': value + ' value is invalid',
  };
  return params;
}

/// Type of Call
enum StringeeCallType {
  appToAppOutgoing,
  appToAppIncoming,
  appToPhone,
  phoneToApp,
}

/// Type of Audio Device
enum AudioDevice {
  speakerPhone,
  wiredHeadset,
  earpiece,
  bluetooth,
  none,
}

/// Type of Signaling State
enum StringeeSignalingState {
  calling,
  ringing,
  answered,
  busy,
  ended,
}

/// Type of Media State
enum StringeeMediaState {
  connected,
  disconnected,
}

/// Type of Video Quality
enum VideoQuality {
  normal,
  hd,
  fullHd,
}

///Type of Scaling type
enum ScalingType {
  fit,
  fill,
}

///Class represents options for make a call
class MakeCallParams {
  String _from;
  String _to;
  bool _isVideoCall;
  Map<dynamic, dynamic> _customData;
  VideoQuality _videoQuality;

  MakeCallParams(
    String from,
    String to, {
    bool isVideoCall,
    Map<dynamic, dynamic> customData,
    VideoQuality videoQuality,
  })  : assert(from != null || from.trim().isNotEmpty),
        assert(to != null || to.trim().isNotEmpty) {
    this._from = from.trim();
    this._to = to.trim();
    this._isVideoCall = (isVideoCall != null) ? isVideoCall : false;
    if (customData != null) this._customData = customData;
    if (this._isVideoCall)
      this._videoQuality = (videoQuality != null) ? videoQuality : VideoQuality.normal;
  }

  VideoQuality get videoQuality => _videoQuality;

  Map<dynamic, dynamic> get customData => _customData;

  bool get isVideoCall => _isVideoCall;

  String get to => _to;

  String get from => _from;

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['from'] = this._from.trim();
    params['to'] = this._to.trim();
    if (this._customData != null) params['customData'] = this._customData;
    params['isVideoCall'] = this._isVideoCall;
    if (this._isVideoCall) {
      switch (this._videoQuality) {
        case VideoQuality.normal:
          params['videoResolution'] = "NORMAL";
          break;
        case VideoQuality.hd:
          params['videoResolution'] = "HD";
          break;
        case VideoQuality.fullHd:
          params['videoResolution'] = "FULLHD";
          break;
      }
    }
    return params;
  }
}

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

///Class represents options for create a new [StringeeConversation]
class StringeeConversationOption {
  String _name;
  bool _isGroup;
  bool _isDistinct;

  StringeeConversationOption({@required bool isGroup, String name, bool isDistinct})
      : assert(isGroup != null) {
    if (name != null) this._name = name;
    this._isGroup = isGroup != null ? isGroup : true;
    this._isDistinct = isDistinct != null
        ? isDistinct
        : isGroup
            ? false
            : true;
  }

  Map<String, dynamic> toJson() {
    return {
      if (_name != null) 'name': _name.trim(),
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
    };
  }
}

/// Class represents the change of [StringeeConversation] and [StringeeMessage]
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
