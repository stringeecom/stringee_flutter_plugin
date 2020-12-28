import 'package:flutter/cupertino.dart';
import 'package:stringee_flutter_plugin/src/messaging/MessagingConstants.dart';

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
  String _thumbnailUrl;
  double _latitude;
  double _longitude;
  String _address;
  String _filePath;
  String _fileUrl;
  String _fileName;
  int _fileLength;
  int _duration;
  int _imageRatio;
  String _contact;
  String _clientId;
  String _stickerCategory;
  String _stickerName;
  String _customData;
  bool _isDeleted;

  Message();

  String get id => _id;

  String get convId => _convId;

  String get senderId => _senderId;

  int get createdAt => _createdAt;

  int get updateAt => _updateAt;

  int get sequence => _sequence;

  MsgState get state => _state;

  MsgStateType get msgType => _stateType;

  MsgType get type => _type;

  String get text => _text;

  String get thumbnail => _thumbnail;

  String get thumbnailUrl => _thumbnailUrl;

  double get latitude => _latitude;

  double get longitude => _longitude;

  String get address => _address;

  String get filePath => _filePath;

  String get fileUrl => _fileUrl;

  String get fileName => _fileName;

  int get fileLength => _fileLength;

  int get duration => _duration;

  int get imageRatio => _imageRatio;

  String get contact => _contact;

  String get clientId => _clientId;

  String get stickerCategory => _stickerCategory;

  String get stickerName => _stickerName;

  String get customData => _customData;

  bool get isDeleted => _isDeleted;

  Message.initFromEvent(Map<dynamic, dynamic> msgInfor) {
    if (msgInfor == null) {
      return;
    }
    this._id = msgInfor['id'];
    this._convId = msgInfor['convId'];
    this._senderId = msgInfor['senderId'];
    this._createdAt = msgInfor['createAt'];
    this._updateAt = msgInfor['updateAt'];
    this._sequence = msgInfor['sequence'];
    this._state = msgInfor['state'];
    this._stateType = msgInfor['msgType'];
    this._type = msgInfor['type'];
    this._text = msgInfor['text'];
    this._thumbnail = msgInfor['thumbnail'];
    this._thumbnailUrl = msgInfor['thumbnailUrl'];
    this._latitude = msgInfor['latitude'];
    this._longitude = msgInfor['longitude'];
    this._address = msgInfor['address'];
    this._filePath = msgInfor['filePath'];
    this._fileUrl = msgInfor['fileUrl'];
    this._fileName = msgInfor['fileName'];
    this._fileLength = msgInfor['fileLength'];
    this._duration = msgInfor['duration'];
    this._imageRatio = msgInfor['imageRatio'];
    this._contact = msgInfor['contact'];
    this._clientId = msgInfor['clientId'];
    this._stickerCategory = msgInfor['stickerCategory'];
    this._stickerName = msgInfor['stickerName'];
    this._customData = msgInfor['customData'];
    this._isDeleted = msgInfor['isDeleted'];

  }
}
