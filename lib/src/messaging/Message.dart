import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';
import 'package:stringee_flutter_plugin/src/messaging/User.dart';

import '../StringeeClient.dart';
import 'StringeeChange.dart';

class Message implements StringeeObject {
  String _id;
  String _convId;
  String _senderId;
  int _createdAt;
  int _updateAt;
  int _sequence;
  MsgState _state;
  MsgStateType _stateType;
  MsgType _type;
  String _text;
  String _thumbnail;
  double _latitude;
  double _longitude;
  String _filePath;
  String _fileUrl;
  String _fileName;
  int _fileLength;
  int _duration;
  double _ratio;
  String _contact;
  String _clientId;
  String _stickerCategory;
  String _stickerName;
  Map<dynamic, dynamic> _customData;
  bool _isDeleted;
  Map<dynamic, dynamic> _notiContent;

  Message.typeText({
    @required String convId,
    @required String text,
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

  Message.typePhoto({
    @required String convId,
    @required String filePath,
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

  Message.typeVideo({
    @required String convId,
    @required String filePath,
    @required int duration,
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

  Message.typeAudio({
    @required String convId,
    @required String filePath,
    @required int duration,
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

  Message.typeFile({
    @required String convId,
    @required String filePath,
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

  Message.typeLink({
    @required String convId,
    @required String text,
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

  Message.typeLocation({
    @required String convId,
    @required double latitude,
    @required double longitude,
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

  Message.typeContact({
    @required String convId,
    @required String contact,
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

  Message.typeSticker({
    @required String convId,
    @required String stickerCategory,
    @required String stickerName,
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

  bool get isDeleted => _isDeleted;

  Map<dynamic, dynamic> get customData => _customData;

  Map<dynamic, dynamic> get notiContent => _notiContent;

  String get stickerName => _stickerName;

  String get stickerCategory => _stickerCategory;

  String get clientId => _clientId;

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

  MsgStateType get stateType => _stateType;

  MsgState get state => _state;

  int get sequence => _sequence;

  int get updateAt => _updateAt;

  int get createdAt => _createdAt;

  String get senderId => _senderId;

  String get convId => _convId;

  String get id => _id;

  Message.fromJson(Map<dynamic, dynamic> msgInfor) {
    if (msgInfor == null) {
      return;
    }
    this._id = msgInfor['id'];
    this._convId = msgInfor['convId'];
    this._senderId = msgInfor['senderId'];
    this._createdAt = msgInfor['createAt'];
    this._updateAt = msgInfor['updateAt'];
    this._sequence = msgInfor['sequence'];
    this._isDeleted = msgInfor['isDeleted'];
    this._clientId = msgInfor['clientId'];
    this._customData = msgInfor['customData'];
    this._state = MsgState.values[msgInfor['state']];
    this._stateType = MsgStateType.values[msgInfor['msgType']];

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
          case MsgNotifyType.TYPE_END_CONV:
            break;
        }
        break;
    }
    this._text = text;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['convId'] = _convId;
    params['type'] = _type.value;
    if (_customData != null) params['customData'] = _customData;
    switch (this._type) {
      case MsgType.TYPE_TEXT:
      case MsgType.TYPE_LINK:
        params['text'] = _text;
        break;
      case MsgType.TYPE_PHOTO:
      case MsgType.TYPE_FILE:
        if (_filePath != null) params['filePath'] = _filePath;
        break;
      case MsgType.TYPE_VIDEO:
      case MsgType.TYPE_AUDIO:
        if (_filePath != null) params['filePath'] = _filePath;
        if (_duration != null) params['duration'] = _duration;
        break;
      case MsgType.TYPE_LOCATION:
        if (_latitude != null) params['latitude'] = _latitude;
        if (_longitude != null) params['longitude'] = _longitude;
        break;
      case MsgType.TYPE_CONTACT:
        if (_contact != null) params['contact'] = _contact;
        break;
      case MsgType.TYPE_STICKER:
        if (_stickerCategory != null) params['stickerCategory'] = _stickerCategory;
        if (_stickerName != null) params['stickerName'] = _stickerName;
        break;
    }
    return params;
  }

  /// Edit [Message.typeText]
  Future<Map<dynamic, dynamic>> edit(String convId, String content) async {
    assert(convId != null || convId.trim().isNotEmpty);
    assert(content != null || content.trim().isNotEmpty);
    final params = {
      'convId': convId,
      'msgIds': this._id,
      'content': content,
    };
    return await StringeeClient.methodChannel.invokeMethod('edit', params);
  }

  /// Pin/Un pin [Message]
  Future<Map<dynamic, dynamic>> pinOrUnPin(String convId, bool pinOrUnPin) async {
    assert(convId != null || convId.trim().isNotEmpty);
    final params = {
      'convId': convId,
      'msgIds': this._id,
      'pinOrUnPin': (pinOrUnPin != null) ? pinOrUnPin : false,
    };
    return await StringeeClient.methodChannel.invokeMethod('pinOrUnPin', params);
  }
}
