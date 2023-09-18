import 'dart:async';
import 'dart:math';

import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

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
  didAddVideoTrack,
  didRemoveVideoTrack,
  didChangeAudioDevice
}

/// Events for StringeeChat
enum StringeeChatEvents {
  didReceiveObjectChange,
}

/// Events for StringeeChat
enum StringeeRoomEvents {
  didJoinRoom,
  didLeaveRoom,
  didAddVideoTrack,
  didRemoveVideoTrack,
  didReceiveRoomMessage,
  trackReadyToPlay,
}

/// Type of event
enum StringeeObjectEventType {
  client,
  call,
  call2,
  chat,
  room,
}

/// Type of chat request
enum StringeeChatRequestType {
  normal,
  transfer,
}

/// Type of Call
enum StringeeCallType {
  appToAppOutgoing,
  appToAppIncoming,
  appToPhone,
  phoneToApp,
}

/// Type of the audio device
enum AudioDevice {
  speakerPhone,
  wiredHeadset,
  earpiece,
  bluetooth,
  none,
}

/// State of the signaling
enum StringeeSignalingState {
  calling,
  ringing,
  answered,
  busy,
  ended,
}

/// State of the media
enum StringeeMediaState {
  connected,
  disconnected,
}

/// Quality of the video
enum VideoQuality {
  normal,
  hd,
  fullHd,
}

/// Type of scaling.
enum ScalingType {
  fit,
  fill,
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

/// Role of the user in conversation.
enum UserRole {
  admin,
  member,
}

/// State of the message
enum MsgState {
  initialize,
  sending,
  sent,
  delivered,
  read,
}

/// Type of the message
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

/// Type of the noti message
enum MsgNotifyType {
  addParticipants,
  removeParticipants,
  changeGroupName,
}

extension MsgNotifyTypeExtension on int? {
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

/// Dimensions of the video track
enum StringeeVideoDimensions {
  dimension_1080,
  dimension_720,
  dimension_480,
  dimension_288,
}

/// Channel of [StringeeConversation] and [StringeeChatRequest]
enum ChannelType {
  normal,
  live_chat,
  facebook,
  zalo,
}

extension ListChannelTypeExtension on List<ChannelType>? {
  List<int> get getListTypes {
    List<int> channelTypes = [];
    this!.forEach((element) {
      channelTypes.add(element.index);
    });
    return channelTypes;
  }
}

/// Status of chat support conversation
enum ChatSupportStatus {
  current_chat,
  past_chat,
  all,
}

/// State of chat request
enum ChatRequestState {
  accepted,
  rejected,
}

/// Media type
enum MediaType {
  audio,
  video,
}

/// Type of rate chat
enum Rating {
  bad,
  good,
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
