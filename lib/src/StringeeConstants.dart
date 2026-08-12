import 'dart:async';
import 'dart:math';
import '../stringee_plugin.dart';

/// Events emitted by [StringeeClient].
enum StringeeClientEvents {
  /// The client connected successfully.
  didConnect,

  /// The client disconnected.
  didDisconnect,

  /// Connecting or maintaining the connection failed.
  didFailWithError,

  /// The SDK needs a refreshed access token.
  requestAccessToken,

  /// A custom message arrived from another Stringee user.
  didReceiveCustomMessage,

  /// A first-generation incoming call arrived.
  incomingCall,

  /// A Call2 incoming call arrived.
  incomingCall2,

  /// A new live-chat request arrived.
  didReceiveChatRequest,

  /// A transferred live-chat request arrived.
  didReceiveTransferChatRequest,

  /// A live-chat request timed out before being answered.
  timeoutAnswerChat,

  /// A live-chat customer timed out while waiting in a queue.
  timeoutInQueue,

  /// A live-chat conversation ended.
  conversationEnded,

  /// A remote user started typing.
  userBeginTyping,

  /// A remote user stopped typing.
  userEndTyping
}

/// Events emitted by [StringeeCall].
enum StringeeCallEvents {
  /// The call signaling state changed.
  didChangeSignalingState,

  /// The call media connection state changed.
  didChangeMediaState,

  /// Custom call information arrived.
  didReceiveCallInfo,

  /// The call was handled on another signed-in device.
  didHandleOnAnotherDevice,

  /// The local video stream became available.
  didReceiveLocalStream,

  /// The remote video stream became available.
  didReceiveRemoteStream,
}

/// Events emitted by [StringeeCall2].
enum StringeeCall2Events {
  /// The call signaling state changed.
  didChangeSignalingState,

  /// The call media connection state changed.
  didChangeMediaState,

  /// Custom call information arrived.
  didReceiveCallInfo,

  /// The call was handled on another signed-in device.
  didHandleOnAnotherDevice,

  /// The local video stream became available.
  didReceiveLocalStream,

  /// The remote video stream became available.
  didReceiveRemoteStream,

  /// A remote video track was added.
  didAddVideoTrack,

  /// A remote video track was removed.
  didRemoveVideoTrack,
}

/// Events emitted by [StringeeChat].
enum StringeeChatEvents {
  /// Conversations or messages were inserted, updated, or deleted.
  didReceiveObjectChange,
}

/// Events emitted by [StringeeVideoRoom].
enum StringeeRoomEvents {
  /// A participant joined the room.
  didJoinRoom,

  /// A participant left the room.
  didLeaveRoom,

  /// A participant published a video track.
  didAddVideoTrack,

  /// A participant removed a video track.
  didRemoveVideoTrack,

  /// A room data message arrived.
  didReceiveRoomMessage,

  /// A subscribed video track is ready to render.
  trackReadyToPlay
  // didReceiveVideoTrackControlNotification,
}

/// Channel through which a live-chat request originated.
enum StringeeChannelType {
  /// Standard Stringee chat.
  normal,

  /// Website or application live chat.
  livechat,

  /// Facebook messaging channel.
  facebook,

  /// Zalo messaging channel.
  zalo,
}

/// Whether a chat request is new or transferred from another agent.
enum StringeeChatRequestType {
  /// A new chat request.
  normal,

  /// A chat transferred from another agent.
  transfer,
}

/// Native object event type.
enum StringeeObjectEventType {
  /// Client-level event.
  client,

  /// First-generation call event.
  call,

  /// Call2 event.
  call2,

  /// Chat object-change event.
  chat,

  /// Video-room event.
  room,
}

/// Error codes returned by Flutter-side validation:
/// -1: StringeeClient is not initialized or disconnected.
/// -2: Value is invalid.
/// -3: Object is not found.
/// -4: This function works only on Android.
Future<Map<String, dynamic>> reportInvalidValue(String value) async {
  Map<String, dynamic> params = {
    'status': false,
    'code': -2,
    'message': value + ' value is invalid',
  };
  return params;
}

/// Call direction and call endpoint type.
enum StringeeCallType {
  /// Outgoing call between Stringee application users.
  appToAppOutgoing,

  /// Incoming call between Stringee application users.
  appToAppIncoming,

  /// Outgoing call from an application user to a phone number.
  appToPhone,

  /// Incoming call from a phone number to an application user.
  phoneToApp,
}

/// Call signaling state.
enum StringeeSignalingState {
  /// Outgoing call setup is in progress.
  calling,

  /// The remote endpoint is ringing.
  ringing,

  /// The call was answered.
  answered,

  /// The remote endpoint is busy.
  busy,

  /// The call ended.
  ended,
}

/// Call media state.
enum StringeeMediaState {
  /// Media transport is connected.
  connected,

  /// Media transport is disconnected.
  disconnected,
}

/// Video quality used for outgoing calls.
enum VideoQuality {
  /// Standard-definition video.
  normal,

  /// 720p high-definition video.
  hd,

  /// 1080p full high-definition video.
  fullHd,
}

/// Video view scaling mode.
enum ScalingType {
  /// Fit the complete frame inside the view while preserving aspect ratio.
  fit,

  /// Fill the view while preserving aspect ratio, cropping when needed.
  fill,
}

/// Options used to make an outgoing call.
class MakeCallParams {
  String? _from;
  String? _to;
  bool? _isVideoCall;
  Map<dynamic, dynamic>? _customData;
  VideoQuality? _videoQuality;

  /// Creates validated parameters for an outgoing call from [from] to [to].
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

  /// Requested video quality when [isVideoCall] is `true`.
  VideoQuality? get videoQuality => _videoQuality;

  /// Application-specific data attached to the call.
  Map<dynamic, dynamic>? get customData => _customData;

  /// Whether to establish a video call.
  bool? get isVideoCall => _isVideoCall;

  /// Callee identifier.
  String? get to => _to;

  /// Caller identifier.
  String? get from => _from;

  /// Converts these options to a platform-channel payload.
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

/// Chat object type.
enum ObjectType {
  /// A conversation object.
  conversation,

  /// A message object.
  message,
}

/// Chat object change type.
enum ChangeType {
  /// An object was inserted.
  insert,

  /// An object was updated.
  update,

  /// An object was deleted.
  delete,
}

/// Conversation user role.
enum UserRole {
  /// Conversation administrator.
  admin,

  /// Regular conversation member.
  member,
}

/// Message delivery state.
enum MsgState {
  /// Message object was created but not queued.
  initialize,

  /// Message is being sent.
  sending,

  /// Message reached the Stringee server.
  sent,

  /// Message reached the recipient device.
  delivered,

  /// Message was read by the recipient.
  read,
}

/// Message content type.
enum MsgType {
  /// Plain text content.
  text,

  /// Photo attachment.
  photo,

  /// Video attachment.
  video,

  /// Audio attachment.
  audio,

  /// Generic file attachment.
  file,

  /// Link content.
  link,

  /// Conversation-created system message.
  createConversation,

  /// Conversation-renamed system message.
  renameConversation,

  /// Geographic location.
  location,

  /// vCard contact.
  contact,

  /// Sticker content.
  sticker,

  /// Participant or group notification.
  notification,
}

/// Converts a nullable [MsgType] to its native integer representation.
extension MsgTypeValueExtension on MsgType? {
  /// Native integer value, defaulting to the text-message value.
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

/// Converts a native integer value to a [MsgType].
extension MsgTypeExtension on int? {
  /// Parsed message type, defaulting to [MsgType.text].
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

/// Notification message subtype.
enum MsgNotifyType {
  /// Participants were added.
  addParticipants,

  /// Participants were removed.
  removeParticipants,

  /// The group name changed.
  changeGroupName,
}

/// Converts a native notification subtype value to [MsgNotifyType].
extension MsgNotifyTypeExtension on int? {
  /// Parsed notification subtype.
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

/// Options used to create a new [StringeeConversation].
class StringeeConversationOption {
  String? _name;
  bool _isGroup = false;
  bool _isDistinct = false;
  String? _oaId;
  String? _customData;
  String? _creatorId;

  /// Creates options for a direct, group, or OA conversation.
  StringeeConversationOption(
      {required bool isGroup,
      required bool isDistinct,
      String? name,
      String? oaId,
      String? customData,
      String? creatorId}) {
    if (name != null) this._name = name;
    this._isGroup = isGroup;
    this._isDistinct = isDistinct;
    if (oaId != null) this._oaId = oaId;
    if (customData != null) this._customData = customData;
    if (creatorId != null) this._creatorId = creatorId;
  }

  /// Converts these options to a platform-channel payload.
  Map<String, dynamic> toJson() {
    return {
      if (_name != null) 'name': _name!.trim(),
      'isGroup': _isGroup,
      'isDistinct': _isDistinct,
      if (_oaId != null) 'oaId': _oaId!.trim(),
      if (_customData != null) 'customData': _customData!.trim(),
      if (_creatorId != null) 'creatorId': _creatorId!.trim(),
    };
  }
}

/// Change event for [StringeeConversation] and [StringeeMessage] objects.
class StringeeObjectChange {
  ChangeType? _type;
  ObjectType? _objectType;
  List<dynamic>? _objects;

  /// The operation performed on the objects.
  ChangeType? get type => _type;

  /// The kind of object that changed.
  ObjectType? get objectType => _objectType;

  /// Parsed [StringeeConversation] or [StringeeMessage] instances.
  List<dynamic>? get objects => _objects;

  /// Creates a change event with its [type], [objectType], and [objects].
  StringeeObjectChange(
      ChangeType type, ObjectType objectType, List<dynamic> objects) {
    this._type = type;
    this._objects = objects;
    this._objectType = objectType;
  }
}

/// Server address used by native Stringee SDK.
class StringeeServerAddress {
  String? _host;
  int? _port;

  /// Signaling server hostname or IP address.
  String? get host => _host;

  /// Signaling server port.
  int? get port => _port;

  /// Creates a signaling endpoint at [host]:[port].
  StringeeServerAddress(String host, int port) {
    this._host = host;
    this._port = port;
  }

  /// Converts this address to a platform-channel payload.
  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    if (_host != null) params['host'] = _host!.trim();
    if (_port != null) params['port'] = _port;
    return params;
  }
}

/// Options used to create or subscribe to a [StringeeVideoTrack].
class StringeeVideoTrackOption {
  late bool _audio;
  late bool _video;
  late bool _screen;
  StringeeVideoDimensions _videoDimension =
      StringeeVideoDimensions.dimesion_288;

  /// Whether the track includes audio.
  bool get audio => _audio;

  /// Whether the track includes camera or screen video.
  bool get video => _video;

  /// Whether the video source is screen capture.
  bool get screen => _screen;

  /// Requested capture dimensions.
  StringeeVideoDimensions get videoDimension => _videoDimension;

  /// Creates local-track or subscription options.
  StringeeVideoTrackOption(
      {required bool audio,
      required bool video,
      required bool screen,
      StringeeVideoDimensions? videoDimension}) {
    this._audio = audio;
    this._video = video;
    this._screen = screen;
    if (videoDimension != null) this._videoDimension = videoDimension;
  }

  /// Converts these options to a platform-channel payload.
  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['audio'] = _audio;
    params['video'] = _video;
    params['screen'] = _screen;
    switch (this._videoDimension) {
      case StringeeVideoDimensions.dimesion_288:
        params['videoDimension'] = '288';
        break;
      case StringeeVideoDimensions.dimesion_480:
        params['videoDimension'] = '480';
        break;
      case StringeeVideoDimensions.dimesion_720:
        params['videoDimension'] = '720';
        break;
      case StringeeVideoDimensions.dimesion_1080:
        params['videoDimension'] = '1080';
        break;
      default:
        params['videoDimension'] = '288';
        break;
    }
    return params;
  }
}

/// Video dimension for a [StringeeVideoTrack].
enum StringeeVideoDimensions {
  /// 1080-line capture preset.
  dimesion_1080,

  /// 720-line capture preset.
  dimesion_720,

  /// 480-line capture preset.
  dimesion_480,

  /// 288-line capture preset.
  dimesion_288,
}

/// Audio device type.
enum AudioType {
  /// Built-in loudspeaker.
  speakerPhone,

  /// Wired headset or headphones.
  wiredHeadset,

  /// Built-in earpiece.
  earpiece,

  /// Bluetooth audio device.
  bluetooth,

  /// Platform-specific audio route.
  other,

  /// No audio route is selected.
  none,
}

/// Conversion helpers for native audio-device values.
extension AudioTypeX on AudioType {
  /// Converts the native [value] to an [AudioType].
  static AudioType fromValue(int? value) {
    switch (value) {
      case 0:
        return AudioType.speakerPhone;
      case 1:
        return AudioType.wiredHeadset;
      case 2:
        return AudioType.earpiece;
      case 3:
        return AudioType.bluetooth;
      case 4:
        return AudioType.other;
      default:
        return AudioType.none;
    }
  }
}

/// Generates identifiers used to route platform-channel events.
class GUIDGen {
  /// Generates a random UUID version 4 string.
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
