import 'dart:async';
import 'dart:math';

/// Events for StringeeClient
enum StringeeClientEvents {
  didConnect,
  didDisconnect,
  didFailWithError,
  requestAccessToken,
  didReceiveCustomMessage,
  incomingCall,
  incomingCall2,
  didReceiveChatRequest,
  didReceiveTransferChatRequest,
  timeoutAnswerChat,
  timeoutInQueue,
  conversationEnded,
  userBeginTyping,
  userEndTyping
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

/// Events for StringeeChat
enum StringeeChatEvents { didReceiveObjectChange }

enum StringeeChannelType {
  normal,
  livechat,
  facebook,
  zalo,
}

enum StringeeChatRequestType {
  normal,
  transfer,
}

/// Type of event
enum StringeeObjectEventType {
  client,
  call,
  call2,
  chat,
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
  String? _from;
  String? _to;
  bool? _isVideoCall;
  Map<dynamic, dynamic>? _customData;
  VideoQuality? _videoQuality;

  MakeCallParams(
    String from,
    String to, {
    bool? isVideoCall,
    Map<dynamic, dynamic>? customData,
    VideoQuality? videoQuality,
  })  : assert(from.trim().isNotEmpty),
        assert(to.trim().isNotEmpty) {
    this._from = from.trim();
    this._to = to.trim();
    this._isVideoCall = (isVideoCall != null) ? isVideoCall : false;
    if (customData != null) this._customData = customData;
    if (this._isVideoCall!)
      this._videoQuality =
          (videoQuality != null) ? videoQuality : VideoQuality.normal;
  }

  VideoQuality? get videoQuality => _videoQuality;

  Map<dynamic, dynamic>? get customData => _customData;

  bool? get isVideoCall => _isVideoCall;

  String? get to => _to;

  String? get from => _from;

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['from'] = this._from!.trim();
    params['to'] = this._to!.trim();
    if (this._customData != null) params['customData'] = this._customData;
    params['isVideoCall'] = this._isVideoCall;
    if (this._isVideoCall!) {
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
        default:
          params['videoResolution'] = "NORMAL";
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

extension MsgTypeValueExtension on MsgType? {
  // ignore: missing_return
  int get value {
    switch (this) {
      case MsgType.text:
        return 1;
      case MsgType.photo:
        return 2;
      case MsgType.video:
        return 3;
      case MsgType.audio:
        return 4;
      case MsgType.file:
        return 5;
      case MsgType.link:
        return 6;
      case MsgType.createConversation:
        return 7;
      case MsgType.renameConversation:
        return 8;
      case MsgType.location:
        return 9;
      case MsgType.contact:
        return 10;
      case MsgType.sticker:
        return 11;
      case MsgType.notification:
        return 100;
      default:
        return 1;
    }
  }
}

extension MsgTypeExtension on int? {
  // ignore: missing_return
  MsgType get msgType {
    switch (this) {
      case 1:
        return MsgType.text;
      case 2:
        return MsgType.photo;
      case 3:
        return MsgType.video;
      case 4:
        return MsgType.audio;
      case 5:
        return MsgType.file;
      case 6:
        return MsgType.link;
      case 7:
        return MsgType.createConversation;
      case 8:
        return MsgType.renameConversation;
      case 9:
        return MsgType.location;
      case 10:
        return MsgType.contact;
      case 11:
        return MsgType.sticker;
      case 100:
        return MsgType.notification;
      default:
        return MsgType.text;
    }
  }
}

/// Type of noti [message]
enum MsgNotifyType {
  addParticipants,
  removeParticipants,
  changeGroupName,
}

extension MsgNotifyTypeExtension on int? {
  // ignore: missing_return
  MsgNotifyType get notifyType {
    switch (this) {
      case 1:
        return MsgNotifyType.addParticipants;
      case 2:
        return MsgNotifyType.removeParticipants;
      case 3:
        return MsgNotifyType.changeGroupName;
      default:
        return MsgNotifyType.changeGroupName;
    }
  }
}

///Class represents options for create a new [StringeeConversation]
class StringeeConversationOption {
  String? _name;
  bool _isGroup = false;
  bool _isDistinct = false;

  StringeeConversationOption(
      {required bool isGroup, String? name, required bool isDistinct}) {
    if (name != null) this._name = name;
    this._isGroup = isGroup;
    this._isDistinct = isDistinct;
  }

  Map<String, dynamic> toJson() {
    return {
      if (_name != null) 'name': _name!.trim(),
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
    };
  }
}

/// Class represents the change of [StringeeConversation] and [StringeeMessage]
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
}

class StringeeServerAddress {
  String? _host;
  int? _port;

  String? get host => _host;

  int? get port => _port;

  StringeeServerAddress(String host, int port) {
    this._host = host;
    this._port = port;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    if (_host != null) params['host'] = _host!.trim();
    if (_port != null) params['port'] = _port;
    return params;
  }
}

class GUIDGen {
  static String generate() {
    Random random = new Random(new DateTime.now().millisecond);

    final String hexDigits = "0123456789abcdef";
    final List<String?> uuid =
        new List<String?>.filled(36, null, growable: false);

    for (int i = 0; i < 36; i++) {
      final int hexPos = random.nextInt(16);
      uuid[i] = (hexDigits.substring(hexPos, hexPos + 1));
    }

    int pos = (int.parse(uuid[19]!, radix: 16) & 0x3) |
        0x8; // bits 6-7 of the clock_seq_hi_and_reserved to 01

    uuid[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
    uuid[19] = hexDigits.substring(pos, pos + 1);

    uuid[8] = uuid[13] = uuid[18] = uuid[23] = "-";

    final StringBuffer buffer = new StringBuffer();
    buffer.writeAll(uuid);
    return buffer.toString();
  }
}
