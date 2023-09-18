import '../../stringee_flutter_plugin.dart';

class StringeeMessage {
  late StringeeClient _client;
  String? _id;
  String? _localId;
  String? _convId;
  String? _convLocalId;
  StringeeUser? _sender;
  @deprecated
  String? _senderId;
  int? _createdAt;
  int? _updateAt;
  int? _sequence;
  MsgState? _state;
  MsgType? _type;
  String? _text;
  bool? _deleted = false;

  /// Photo
  String? _thumbnail;
  String? _filePath;
  String? _fileUrl;

  /// Location
  double? _latitude;
  double? _longitude;

  /// File
  String? _fileName;
  int? _fileLength;

  /// Audio + Video
  double? _duration;
  double? _ratio;

  String? _vcard;

  /// Sticker
  String? _stickerCategory;
  String? _stickerName;

  Map<dynamic, dynamic>? _customData;
  Map<dynamic, dynamic>? _notiContent;

  @override
  String toString() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _client.userId;
    params['uuid'] = _client.uuid;
    params['id'] = _id;
    params['localId'] = _localId;
    params['convId'] = _convId;
    params['convLocalId'] = _convLocalId;
    params['sender'] = _sender!.toJson();
    params['createdAt'] = _createdAt;
    params['updateAt'] = _updateAt;
    params['sequence'] = _sequence;
    params['state'] = _state!.index;
    params['type'] = _type.value;
    params['text'] = _text;
    params['deleted'] = _deleted;
    params['customData'] = _customData;
    switch (this._type) {
      case MsgType.photo:
        params['filePath'] = _filePath!.trim();
        params['fileUrl'] = _fileUrl!.trim();
        params['thumbnail'] = _thumbnail!.trim();
        params['ratio'] = _ratio;
        break;
      case MsgType.file:
        params['filePath'] = _filePath!.trim();
        params['fileUrl'] = _fileUrl!.trim();
        params['filename'] = _fileName!.trim();
        params['length'] = _fileLength;
        break;
      case MsgType.video:
        params['filePath'] = _filePath!.trim();
        params['fileUrl'] = _fileUrl!.trim();
        params['thumbnail'] = _thumbnail!.trim();
        params['ratio'] = _ratio;
        params['duration'] = _duration;
        break;
      case MsgType.audio:
        params['filePath'] = _filePath!.trim();
        params['fileUrl'] = _fileUrl!.trim();
        params['duration'] = _duration;
        break;
      case MsgType.location:
        params['lat'] = _latitude;
        params['lon'] = _longitude;
        break;
      case MsgType.contact:
        params['vcard'] = _vcard!.trim();
        break;
      case MsgType.sticker:
        params['stickerCategory'] = _stickerCategory!.trim();
        params['stickerName'] = _stickerName!.trim();
        break;
      case MsgType.createConversation:
      case MsgType.renameConversation:
      case MsgType.notification:
        params['notiContent'] = _notiContent!.toString();
        break;
    }
    return params.toString();
  }

  StringeeMessage.typeText(
    StringeeClient client,
    String text, {
    Map<dynamic, dynamic>? customData,
  }) : assert(text.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.text;
    this._text = text.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typePhoto(
    StringeeClient client,
    String filePath, {
    String? thumbnail,
    double? ratio,
    Map<dynamic, dynamic>? customData,
  }) : assert(filePath.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.photo;
    this._filePath = filePath.trim();
    if (thumbnail != null) {
      this._thumbnail = thumbnail.trim();
    }
    if (ratio != null) {
      this._ratio = ratio;
    }
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeVideo(
    StringeeClient client,
    String filePath,
    double duration, {
    String? thumbnail,
    double? ratio,
    Map<dynamic, dynamic>? customData,
  })  : assert(filePath.trim().isNotEmpty),
        assert(duration > 0) {
    _client = client;

    this._type = MsgType.video;
    this._filePath = filePath.trim();
    this._duration = duration;
    if (thumbnail != null) {
      this._thumbnail = thumbnail.trim();
    }
    if (ratio != null) {
      this._ratio = ratio;
    }
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeAudio(
    StringeeClient client,
    String filePath,
    double duration, {
    Map<dynamic, dynamic>? customData,
  })  : assert(filePath.trim().isNotEmpty),
        assert(duration > 0) {
    _client = client;

    this._type = MsgType.audio;
    this._filePath = filePath.trim();
    this._duration = duration;
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeFile(
    StringeeClient client,
    String filePath, {
    String? fileName,
    int? fileLength,
    Map<dynamic, dynamic>? customData,
  }) : assert(filePath.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.file;
    this._filePath = filePath.trim();
    if (fileName != null) {
      this._fileName = fileName.trim();
    }
    if (fileLength != null) {
      this._fileLength = fileLength;
    }

    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeLink(
    StringeeClient client,
    String text, {
    Map<dynamic, dynamic>? customData,
  }) : assert(text.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.link;
    this._text = text.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeLocation(
    StringeeClient client,
    double latitude,
    double longitude, {
    Map<dynamic, dynamic>? customData,
  })  : assert(latitude > 0),
        assert(longitude > 0) {
    _client = client;

    this._type = MsgType.location;
    this._latitude = latitude;
    this._longitude = longitude;
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeContact(
    StringeeClient client,
    String vcard, {
    Map<dynamic, dynamic>? customData,
  }) : assert(vcard.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.contact;
    this._vcard = vcard.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  StringeeMessage.typeSticker(
    StringeeClient client,
    String stickerCategory,
    String stickerName, {
    Map<dynamic, dynamic>? customData,
  })  : assert(stickerCategory.trim().isNotEmpty),
        assert(stickerName.trim().isNotEmpty) {
    _client = client;

    this._type = MsgType.contact;
    this._stickerName = stickerName.trim();
    this._stickerCategory = stickerCategory.trim();
    if (customData != null) {
      this._customData = customData;
    }
  }

  String? get id => _id;

  String? get localId => _localId;

  String? get convId => _convId;

  String? get convLocalId => _convLocalId;

  StringeeUser? get sender => _sender;

  @deprecated
  String? get senderId => _senderId;

  int? get createdAt => _createdAt;

  int? get updateAt => _updateAt;

  int? get sequence => _sequence;

  MsgState? get state => _state;

  MsgType? get type => _type;

  String? get text => _text;

  bool? get deleted => _deleted;

  String? get thumbnail => _thumbnail;

  String? get filePath => _filePath;

  String? get fileUrl => _fileUrl;

  double? get longitude => _longitude;

  double? get latitude => _latitude;

  String? get fileName => _fileName;

  int? get fileLength => _fileLength;

  double? get duration => _duration;

  String? get vcard => _vcard;

  double? get ratio => _ratio;

  String? get stickerCategory => _stickerCategory;

  String? get stickerName => _stickerName;

  Map<dynamic, dynamic>? get customData => _customData;

  Map<dynamic, dynamic>? get notiContent => _notiContent;

  set convId(String? value) {
    _convId = value;
  }

  StringeeMessage.fromJson(
      Map<dynamic, dynamic> msgInfor, StringeeClient client) {
    _client = client;

    this._id = msgInfor['id'];
    this._localId = msgInfor['localId'];
    this._convId = msgInfor['convId'];
    this._senderId = msgInfor['senderId'];
    this._createdAt = msgInfor['createdAt'];
    this._sequence = msgInfor['sequence'];
    this._customData = msgInfor['customData'];
    this._state = MsgState.values[msgInfor['state']];
    this._convLocalId = msgInfor['convLocalId'];
    this._sender = StringeeUser.fromJson(msgInfor['sender']);
    this._updateAt = msgInfor['updateAt'];
    this._deleted = msgInfor['deleted'];

    MsgType msgType = (msgInfor['type'] as int?).msgType;
    this._type = msgType;
    String? text = '';
    switch (this._type) {
      case MsgType.text:
      case MsgType.link:
        if (msgInfor['content'].containsKey('content')) {
          text = msgInfor['content']['content'];
        }
        break;
      case MsgType.createConversation:
      case MsgType.renameConversation:
        String? groupName = msgInfor['content']['groupName'];
        String? creator = msgInfor['content']['creator'];
        List<StringeeUser> participants = [];
        List<dynamic> participantArray = msgInfor['content']['participants'];
        if (participantArray.length > 0) {
          for (int i = 0; i < participantArray.length; i++) {
            StringeeUser user = StringeeUser(userId: participantArray[i]);
            participants.add(user);
          }
        }
        this._notiContent = new Map<dynamic, dynamic>();
        this._notiContent!["groupName"] = groupName;
        this._notiContent!["creator"] = creator;
        this._notiContent!["participants"] = participants;
        break;
      case MsgType.photo:
        if (msgInfor['content'].containsKey('photo')) {
          Map<dynamic, dynamic> photoMap = msgInfor['content']['photo'];
          this._filePath = photoMap['filePath'];
          this._fileUrl = photoMap['fileUrl'];
          this._thumbnail = photoMap['thumbnail'];
          this._ratio =
              photoMap['ratio'] == null ? 0 : photoMap['ratio'].toDouble();
        }
        break;
      case MsgType.video:
        if (msgInfor['content'].containsKey('video')) {
          Map<dynamic, dynamic> videoMap = msgInfor['content']['video'];
          this._filePath = videoMap['filePath'];
          this._fileUrl = videoMap['fileUrl'];
          this._thumbnail = videoMap['thumbnail'];
          this._ratio =
              videoMap['ratio'] == null ? 0 : videoMap['ratio'].toDouble();
          this._duration = videoMap['duration'] == null
              ? 0
              : videoMap['duration'].toDouble();
        }
        break;
      case MsgType.audio:
        if (msgInfor['content'].containsKey('audio')) {
          Map<dynamic, dynamic> audioMap = msgInfor['content']['audio'];
          this._filePath = audioMap['filePath'];
          this._fileUrl = audioMap['fileUrl'];
          this._duration = audioMap['duration'] == null
              ? 0
              : audioMap['duration'].toDouble();
        }
        break;
      case MsgType.file:
        if (msgInfor['content'].containsKey('file')) {
          Map<dynamic, dynamic> fileMap = msgInfor['content']['file'];
          this._filePath = fileMap['filePath'];
          this._fileUrl = fileMap['fileUrl'];
          this._fileName = fileMap['fileName'];
          this._fileLength = fileMap['fileLength'];
        }
        break;
      case MsgType.location:
        if (msgInfor['content'].containsKey('location')) {
          Map<dynamic, dynamic> locationMap = msgInfor['content']['location'];
          this._latitude = locationMap['lat'];
          this._longitude = locationMap['lon'];
        }
        break;
      case MsgType.contact:
        if (msgInfor['content'].containsKey('contact')) {
          Map<dynamic, dynamic> contactMap = msgInfor['content']['contact'];
          this._vcard = contactMap['vcard'];
        }
        break;
      case MsgType.sticker:
        if (msgInfor['content'].containsKey('sticker')) {
          Map<dynamic, dynamic> stickerMap = msgInfor['content']['sticker'];
          this._stickerName = stickerMap['name'];
          this._stickerCategory = stickerMap['category'];
        }
        break;
      case MsgType.notification:
        Map<dynamic, dynamic> notifyMap = msgInfor['content'];
        this._notiContent = new Map<dynamic, dynamic>();
        MsgNotifyType notifyType = (notifyMap['type'] as int?).notifyType;
        this._notiContent!['type'] = notifyType;
        switch (notifyType) {
          case MsgNotifyType.addParticipants:
            StringeeUser user =
                new StringeeUser.fromJson(notifyMap['addedInfo']);
            this._notiContent!['addedby'] = user;
            List<StringeeUser> participants = [];
            List<dynamic> participantArray = notifyMap['participants'];
            for (int i = 0; i < participantArray.length; i++) {
              StringeeUser user = StringeeUser.fromJson(participantArray[i]);
              participants.add(user);
            }
            this._notiContent!["participants"] = participants;
            break;
          case MsgNotifyType.removeParticipants:
            StringeeUser user =
                new StringeeUser.fromJson(notifyMap['removedInfo']);
            this._notiContent!['removedBy'] = user;
            List<StringeeUser> participants = [];
            List<dynamic> participantArray = notifyMap['participants'];
            for (int i = 0; i < participantArray.length; i++) {
              StringeeUser user = StringeeUser.fromJson(participantArray[i]);
              participants.add(user);
            }
            this._notiContent!["participants"] = participants;
            break;
          case MsgNotifyType.changeGroupName:
            this._notiContent!['groupName'] = notifyMap['groupName'];
            break;
        }
        break;
    }
    this._text = text;
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['convId'] = _convId!.trim();
    params['type'] = _type.value;
    if (_customData != null) params['customData'] = _customData;
    switch (this._type) {
      case MsgType.text:
      case MsgType.link:
        params['text'] = _text!.trim();
        break;
      case MsgType.photo:
        if (_filePath != null) params['filePath'] = _filePath!.trim();
        if (_thumbnail != null) params['thumbnail'] = _thumbnail!.trim();
        if (_ratio != null) params['ratio'] = _ratio;
        break;
      case MsgType.file:
        if (_filePath != null) params['filePath'] = _filePath!.trim();
        if (_fileName != null) params['filename'] = _fileName!.trim();
        if (_fileLength != null) params['length'] = _fileLength;
        break;
      case MsgType.video:
        if (_filePath != null) params['filePath'] = _filePath!.trim();
        if (_thumbnail != null) params['thumbnail'] = _thumbnail!.trim();
        if (_ratio != null) params['ratio'] = _ratio;
        if (_duration != null) params['duration'] = _duration;
        break;
      case MsgType.audio:
        if (_filePath != null) params['filePath'] = _filePath!.trim();
        if (_duration != null) params['duration'] = _duration;
        break;
      case MsgType.location:
        if (_latitude != null) params['lat'] = _latitude;
        if (_longitude != null) params['lon'] = _longitude;
        break;
      case MsgType.contact:
        if (_vcard != null) params['vcard'] = _vcard!.trim();
        break;
      case MsgType.sticker:
        if (_stickerCategory != null)
          params['stickerCategory'] = _stickerCategory!.trim();
        if (_stickerName != null) params['stickerName'] = _stickerName!.trim();
        break;
    }
    return params;
  }

  /// Edit [StringeeMessage.typeText]
  Future<Map<dynamic, dynamic>> edit(String content) async {
    assert(content.trim().isNotEmpty);
    final params = {
      'convId': this._convId!.trim(),
      'msgId': this._id,
      'content': content,
      'uuid': _client.uuid
    };
    return await _client.methodChannel.invokeMethod('editMsg', params);
  }

  /// Pin/Un pin [StringeeMessage]
  Future<Map<dynamic, dynamic>> pinOrUnPin(bool pinOrUnPin) async {
    final params = {
      'convId': this._convId!.trim(),
      'msgId': this._id,
      'pinOrUnPin': pinOrUnPin,
      'uuid': _client.uuid
    };
    return await _client.methodChannel.invokeMethod('pinOrUnPin', params);
  }
}
