import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';
import 'package:stringee_flutter_plugin/src/messaging/User.dart';

import '../StringeeClient.dart';
import '../StringeeConstants.dart';
import 'StringeeChange.dart';

class StringeeMessage implements StringeeObject {
  /// Base
  String _id;
  String _localId;
  String _convId;
  String _senderId;
  int _createdAt;
  int _sequence;
  MsgState _state;
  MsgType _type;
  String _text;

  /// Photo
  String _thumbnail;
  String _filePath;
  String _fileUrl;

  /// Location
  double _latitude;
  double _longitude;

  /// File
  String _fileName;
  int _fileLength;

  /// Audio + Video
  int _duration;
  double _ratio;

  String _contact;

  /// Sticker
  String _stickerCategory;
  String _stickerName;

  Map<dynamic, dynamic> _customData;
  Map<dynamic, dynamic> _notiContent;

  StringeeMessage.typeText(
    String convId,
    String text, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(text != null || text.trim().isNotEmpty) {
    this._type = MsgType.TYPE_TEXT;
    this._convId = convId.trim();
    this._text = text.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typePhoto(
    String convId,
    String filePath, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(filePath != null || filePath.trim().isNotEmpty) {
    this._type = MsgType.TYPE_PHOTO;
    this._convId = convId.trim();
    this._filePath = filePath.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeVideo(
    String convId,
    String filePath,
    int duration, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(filePath != null || filePath.trim().isNotEmpty),
        assert(duration != null || duration > 0) {
    this._type = MsgType.TYPE_VIDEO;
    this._convId = convId.trim();
    this._filePath = filePath.trim();
    this._duration = duration;
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeAudio(
    String convId,
    String filePath,
    int duration, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(filePath != null || filePath.trim().isNotEmpty),
        assert(duration != null || duration > 0) {
    this._type = MsgType.TYPE_AUDIO;
    this._convId = convId.trim();
    this._filePath = filePath.trim();
    this._duration = duration;
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeFile(
    String convId,
    String filePath, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(filePath != null || filePath.trim().isNotEmpty) {
    this._type = MsgType.TYPE_FILE;
    this._convId = convId.trim();
    this._filePath = filePath.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeLink(
    String convId,
    String text, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(text != null || text.trim().isNotEmpty) {
    this._type = MsgType.TYPE_LINK;
    this._convId = convId.trim();
    this._text = text.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeLocation(
    String convId,
    double latitude,
    double longitude, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(latitude != null || latitude > 0),
        assert(longitude != null || longitude > 0) {
    this._type = MsgType.TYPE_LOCATION;
    this._convId = convId.trim();
    this._latitude = latitude;
    this._longitude = longitude;
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeContact(
    String convId,
    String contact, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(contact != null || contact.trim().isNotEmpty) {
    this._type = MsgType.TYPE_CONTACT;
    this._convId = convId.trim();
    this._contact = contact.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeSticker(
    String convId,
    String stickerCategory,
    String stickerName, {
    Map<dynamic, dynamic> customData,
  })  : assert(convId != null || convId.trim().isNotEmpty),
        assert(stickerCategory != null || stickerCategory.trim().isNotEmpty),
        assert(stickerName != null || stickerName.trim().isNotEmpty) {
    this._type = MsgType.TYPE_CONTACT;
    this._convId = convId.trim();
    this._stickerName = stickerName.trim();
    this._stickerCategory = stickerCategory.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  Map<dynamic, dynamic> get customData => _customData;

  Map<dynamic, dynamic> get notiContent => _notiContent;

  String get stickerName => _stickerName;

  String get stickerCategory => _stickerCategory;

  String get contact => _contact;

  double get ratio => _ratio;

  int get duration => _duration;

  int get fileLength => _fileLength;

  String get fileName => _fileName;

  String get fileUrl => _fileUrl;

  String get filePath => _filePath;

  double get longitude => _longitude;

  double get latitude => _latitude;

  String get thumbnail => _thumbnail;

  String get text => _text;

  MsgType get type => _type;

  MsgState get state => _state;

  int get sequence => _sequence;

  int get createdAt => _createdAt;

  String get senderId => _senderId;

  String get convId => _convId;

  String get id => _id;

  String get localId => _localId;

  StringeeMessage.fromJson(Map<dynamic, dynamic> msgInfor) {
    if (msgInfor == null) {
      return;
    }
    this._id = msgInfor['id'];
    this._convId = msgInfor['convId'];
    this._senderId = msgInfor['senderId'];
    this._createdAt = msgInfor['createdAt'];
    this._sequence = msgInfor['sequence'];
    this._customData = msgInfor['customData'];
    this._state = MsgState.values[msgInfor['state']];

    MsgType msgType = (msgInfor['type'] as int).msgType;
    this._type = msgType;
    String text = '';
    switch (this._type) {
      case MsgType.TYPE_TEXT:
      case MsgType.TYPE_LINK:
      case MsgType.TYPE_CREATE_CONVERSATION:
      case MsgType.TYPE_RENAME_CONVERSATION:
        text = msgInfor['content']['text'];
        break;
      case MsgType.TYPE_PHOTO:
        Map<dynamic, dynamic> photoMap = msgInfor['content']['photo'];
        this._filePath = photoMap['filePath'];
        this._fileUrl = photoMap['fileUrl'];
        this._thumbnail = photoMap['thumbnail'];
        this._ratio = photoMap['ratio'];
        break;
      case MsgType.TYPE_VIDEO:
        Map<dynamic, dynamic> videoMap = msgInfor['content']['video'];
        this._filePath = videoMap['filePath'];
        this._fileUrl = videoMap['fileUrl'];
        this._thumbnail = videoMap['thumbnail'];
        this._ratio = videoMap['ratio'];
        this._duration = videoMap['duration'];
        break;
      case MsgType.TYPE_AUDIO:
        Map<dynamic, dynamic> audioMap = msgInfor['content']['audio'];
        this._filePath = audioMap['filePath'];
        this._fileUrl = audioMap['fileUrl'];
        this._duration = audioMap['duration'];
        break;
      case MsgType.TYPE_FILE:
        Map<dynamic, dynamic> fileMap = msgInfor['content']['file'];
        this._filePath = fileMap['filePath'];
        this._fileUrl = fileMap['fileUrl'];
        this._fileName = fileMap['fileName'];
        this._fileLength = fileMap['fileLength'];
        break;
      case MsgType.TYPE_LOCATION:
        Map<dynamic, dynamic> locationMap = msgInfor['content']['location'];
        this._latitude = locationMap['lat'];
        this._longitude = locationMap['lon'];
        break;
      case MsgType.TYPE_CONTACT:
        Map<dynamic, dynamic> contactMap = msgInfor['content']['contact'];
        this._contact = contactMap['vcard'];
        break;
      case MsgType.TYPE_STICKER:
        Map<dynamic, dynamic> stickerMap = msgInfor['content']['sticker'];
        this._stickerName = stickerMap['name'];
        this._stickerCategory = stickerMap['category'];
        break;
      case MsgType.TYPE_NOTIFICATION:
        Map<dynamic, dynamic> notifyMap = msgInfor['content'];
        this._notiContent = new Map<dynamic, dynamic>();
        MsgNotifyType notifyType = (notifyMap['type'] as int).notifyType;
        this._notiContent['type'] = notifyType;
        switch (notifyType) {
          case MsgNotifyType.TYPE_ADD_PARTICIPANTS:
            User user = new User.fromJson(notifyMap['addedby']);
            this._notiContent['addedby'] = user;
            List<User> participants = [];
            List<dynamic> participantArray = json.decode(notifyMap['participants']);
            for (int i = 0; i < participantArray.length; i++) {
              User user = User.fromJson(participantArray[i]);
              participants.add(user);
            }
            this._notiContent[participants] = participants;
            break;
          case MsgNotifyType.TYPE_REMOVE_PARTICIPANTS:
            User user = new User.fromJson(notifyMap['removedBy']);
            this._notiContent['removedBy'] = user;
            List<User> participants = [];
            List<dynamic> participantArray = json.decode(notifyMap['participants']);
            for (int i = 0; i < participantArray.length; i++) {
              User user = User.fromJson(participantArray[i]);
              participants.add(user);
            }
            this._notiContent[participants] = participants;
            break;
          case MsgNotifyType.TYPE_CHANGE_GROUP_NAME:
            this._notiContent = notifyMap;
            break;
          case MsgNotifyType.TYPE_END_CONV:
            this._notiContent = notifyMap;
            break;
        }
        break;
    }
    this._text = text;
  }

  StringeeMessage.lstMsg(
      String msgId,
      String convId,
      MsgType msgType,
      String senderId,
      int sequence,
      MsgState msgState,
      int createdAt,
      Map<dynamic, dynamic> msgInfor) {
    if (msgId == null ||
        msgType == null ||
        senderId == null ||
        sequence == null ||
        msgState == null ||
        createdAt == null ||
        msgInfor == null) {
      return;
    }
    this._id = msgId;
    this._convId = convId;
    this._senderId = senderId;
    this._createdAt = createdAt;
    this._sequence = sequence;
    if (msgInfor.containsKey('metadata')) this._customData = msgInfor['metadata'];
    this._state = msgState;
    this._type = msgType;
    String text = '';
    switch (this._type) {
      case MsgType.TYPE_TEXT:
      case MsgType.TYPE_LINK:
        text = msgInfor['text'];
        break;
      case MsgType.TYPE_CREATE_CONVERSATION:
      case MsgType.TYPE_RENAME_CONVERSATION:
        this._senderId = msgInfor['creator'];
        break;
      case MsgType.TYPE_PHOTO:
        Map<dynamic, dynamic> photoMap = msgInfor['photo'];
        this._filePath = photoMap['filePath'];
        this._fileUrl = photoMap['fileUrl'];
        this._thumbnail = photoMap['thumbnail'];
        this._ratio = photoMap['ratio'];
        break;
      case MsgType.TYPE_VIDEO:
        Map<dynamic, dynamic> videoMap = msgInfor['video'];
        this._filePath = videoMap['filePath'];
        this._fileUrl = videoMap['fileUrl'];
        this._thumbnail = videoMap['thumbnail'];
        this._ratio = videoMap['ratio'];
        this._duration = videoMap['duration'];
        break;
      case MsgType.TYPE_AUDIO:
        Map<dynamic, dynamic> audioMap = msgInfor['audio'];
        this._filePath = audioMap['filePath'];
        this._fileUrl = audioMap['fileUrl'];
        this._duration = audioMap['duration'];
        break;
      case MsgType.TYPE_FILE:
        Map<dynamic, dynamic> fileMap = msgInfor['file'];
        this._filePath = fileMap['filePath'];
        this._fileUrl = fileMap['fileUrl'];
        this._fileName = fileMap['fileName'];
        this._fileLength = fileMap['fileLength'];
        break;
      case MsgType.TYPE_LOCATION:
        Map<dynamic, dynamic> locationMap = msgInfor['location'];
        this._latitude = locationMap['lat'];
        this._longitude = locationMap['lon'];
        break;
      case MsgType.TYPE_CONTACT:
        Map<dynamic, dynamic> contactMap = msgInfor['contact'];
        this._contact = contactMap['vcard'];
        break;
      case MsgType.TYPE_STICKER:
        Map<dynamic, dynamic> stickerMap = msgInfor['sticker'];
        this._stickerName = stickerMap['name'];
        this._stickerCategory = stickerMap['category'];
        break;
      case MsgType.TYPE_NOTIFICATION:
        this._notiContent = new Map<dynamic, dynamic>();
        MsgNotifyType notifyType = (msgInfor['type'] as int).notifyType;
        this._notiContent['type'] = notifyType;
        switch (notifyType) {
          case MsgNotifyType.TYPE_ADD_PARTICIPANTS:
            User user = new User.fromJsonNotify(msgInfor['addedInfo']);
            this._notiContent['addedby'] = user;
            List<User> participants = [];
            List<dynamic> participantArray = msgInfor['participants'];
            for (int i = 0; i < participantArray.length; i++) {
              User user = User.fromJsonNotify(participantArray[i]);
              participants.add(user);
            }
            this._notiContent[participants] = participants;
            break;
          case MsgNotifyType.TYPE_REMOVE_PARTICIPANTS:
            User user = new User.fromJsonNotify(msgInfor['removedInfo']);
            this._notiContent['removedBy'] = user;
            List<User> participants = [];
            List<dynamic> participantArray = msgInfor['participants'];
            for (int i = 0; i < participantArray.length; i++) {
              User user = User.fromJsonNotify(participantArray[i]);
              participants.add(user);
            }
            this._notiContent[participants] = participants;
            break;
          case MsgNotifyType.TYPE_CHANGE_GROUP_NAME:
          case MsgNotifyType.TYPE_END_CONV:
            break;
        }
        break;
    }
    this._text = text;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['convId'] = _convId.trim();
    params['type'] = _type.value;
    if (_customData != null) params['customData'] = _customData;
    switch (this._type) {
      case MsgType.TYPE_TEXT:
      case MsgType.TYPE_LINK:
        params['text'] = _text.trim();
        break;
      case MsgType.TYPE_PHOTO:
      case MsgType.TYPE_FILE:
        if (_filePath != null) params['filePath'] = _filePath.trim();
        break;
      case MsgType.TYPE_VIDEO:
      case MsgType.TYPE_AUDIO:
        if (_filePath != null) params['filePath'] = _filePath.trim();
        if (_duration != null) params['duration'] = _duration;
        break;
      case MsgType.TYPE_LOCATION:
        if (_latitude != null) params['latitude'] = _latitude;
        if (_longitude != null) params['longitude'] = _longitude;
        break;
      case MsgType.TYPE_CONTACT:
        if (_contact != null) params['contact'] = _contact.trim();
        break;
      case MsgType.TYPE_STICKER:
        if (_stickerCategory != null) params['stickerCategory'] = _stickerCategory.trim();
        if (_stickerName != null) params['stickerName'] = _stickerName.trim();
        break;
    }
    return params;
  }

  /// Edit [StringeeMessage.typeText]
  Future<Map<dynamic, dynamic>> edit(String convId, String content) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(content != null || content.trim().isNotEmpty);
    final params = {
      'convId': convId.trim(),
      'msgId': this._id,
      'content': content,
    };
    return await StringeeClient.methodChannel.invokeMethod('edit', params);
  }

  /// Pin/Un pin [StringeeMessage]
  Future<Map<dynamic, dynamic>> pinOrUnPin(String convId, bool pinOrUnPin) async {
    if (convId == null || convId.trim().isEmpty) return await reportInvalidValue('convId');
    if (pinOrUnPin == null) return await reportInvalidValue('pinOrUnPin');
    final params = {
      'convId': convId.trim(),
      'msgId': this._id,
      'pinOrUnPin': pinOrUnPin,
    };
    return await StringeeClient.methodChannel.invokeMethod('pinOrUnPin', params);
  }
}
