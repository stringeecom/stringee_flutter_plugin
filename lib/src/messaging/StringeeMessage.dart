import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// A Stringee chat message.
///
/// Use one of the `type...` constructors to create an outgoing message. Message
/// fields that do not apply to its [type] are `null`.
class StringeeMessage {
  // Base fields.
  String? _id;
  String? _localId;
  String? _convId;
  String? _senderId;
  int? _createdAt;
  int? _sequence;
  MsgState? _state;
  MsgType? _type;
  String? _text;

  // Photo fields.
  String? _thumbnail;
  String? _filePath;
  String? _fileUrl;

  // Location fields.
  double? _latitude;
  double? _longitude;

  // File fields.
  String? _fileName;
  int? _fileLength;

  // Audio and video fields.
  double? _duration;
  double? _ratio;

  String? _vcard;

  // Sticker fields.
  String? _stickerCategory;
  String? _stickerName;

  Map<dynamic, dynamic>? _customData;
  Map<dynamic, dynamic>? _notiContent;

  // Owning client instance.
  late StringeeClient _client;

  @override
  String toString() {
    return '{id: $id, localId: $localId, convId: $convId, senderId: $senderId, createdAt: $createdAt, sequence: $sequence, state: $state, type: $type, text: $text,'
        ' thumbnail: $thumbnail, filePath: $filePath, fileUrl: $fileUrl,'
        ' latitude: $latitude, longitude: $longitude, fileName: $fileName, fileLength: $fileLength,'
        ' duration: $duration, ratio: $ratio, vcard: $vcard, stickerCategory: $stickerCategory,'
        ' stickerName: $stickerName, customData: $customData, notiContent: $notiContent}';
  }

  /// Creates a text message.
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

  /// Creates a photo message from a local [filePath].
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

  /// Creates a video message from a local [filePath] and [duration].
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

  /// Creates an audio message from a local [filePath] and [duration].
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

  /// Creates a file attachment message from a local [filePath].
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

  /// Creates a link message containing [text].
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

  /// Creates a geographic location message.
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

  /// Creates a contact message containing a [vcard].
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

  /// Creates a sticker message.
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

  /// Application-specific metadata attached to the message.
  Map<dynamic, dynamic>? get customData => _customData;

  /// Parsed content for notification messages.
  Map<dynamic, dynamic>? get notiContent => _notiContent;

  /// Sticker name for [MsgType.sticker].
  String? get stickerName => _stickerName;

  /// Sticker category for [MsgType.sticker].
  String? get stickerCategory => _stickerCategory;

  /// Media aspect ratio.
  double? get ratio => _ratio;

  /// Audio or video duration in seconds.
  double? get duration => _duration;

  /// File size in bytes.
  int? get fileLength => _fileLength;

  /// Original attachment filename.
  String? get fileName => _fileName;

  /// Remote URL of an uploaded attachment.
  String? get fileUrl => _fileUrl;

  /// Local attachment path used for upload.
  String? get filePath => _filePath;

  /// Longitude for [MsgType.location].
  double? get longitude => _longitude;

  /// Latitude for [MsgType.location].
  double? get latitude => _latitude;

  /// Thumbnail data or URL for photo and video messages.
  String? get thumbnail => _thumbnail;

  /// Text content for text and link messages.
  String? get text => _text;

  /// Message content type.
  MsgType? get type => _type;

  /// Current delivery state.
  MsgState? get state => _state;

  /// Server ordering sequence within the conversation.
  int? get sequence => _sequence;

  /// Creation time in milliseconds since Unix epoch.
  int? get createdAt => _createdAt;

  /// ID of the user who sent the message.
  String? get senderId => _senderId;

  /// ID of the containing conversation.
  String? get convId => _convId;

  /// Server-assigned message ID.
  String? get id => _id;

  /// Client-generated message ID.
  String? get localId => _localId;

  /// Contact card for [MsgType.contact].
  String? get vcard => _vcard;

  /// Assigns this message to a conversation before sending.
  set convId(String? value) {
    _convId = value;
  }

  static String? _string(dynamic value) {
    return StringeeValueParser.toStringValue(value);
  }

  static int? _int(dynamic value) {
    return StringeeValueParser.toInt(value);
  }

  static double _double(dynamic value) {
    return StringeeValueParser.toDouble(value) ?? 0.0;
  }

  static Map<dynamic, dynamic> _map(dynamic value) {
    return StringeeValueParser.toMap(value) ?? {};
  }

  static List<dynamic> _list(dynamic value) {
    return StringeeValueParser.toList(value);
  }

  static StringeeUser? _user(dynamic value) {
    final userMap = StringeeValueParser.toMap(value);
    if (userMap != null) return StringeeUser.fromJson(userMap);
    final userId = _string(value);
    if (userId == null || userId.isEmpty) return null;
    return StringeeUser(userId: userId);
  }

  /// Creates a message from a native [msgInfor] payload.
  StringeeMessage.fromJson(
      Map<dynamic, dynamic> msgInfor, StringeeClient client) {
    _client = client;

    this._id = _string(msgInfor['id']);
    this._localId = _string(msgInfor['localId']);
    this._convId = _string(msgInfor['convId']);
    this._senderId = _string(msgInfor['senderId']);
    this._createdAt = _int(msgInfor['createdAt']);
    this._sequence = _int(msgInfor['sequence']);
    this._customData = StringeeValueParser.toMap(msgInfor['customData']);
    this._state = StringeeValueParser.enumValue(
      MsgState.values,
      msgInfor['state'],
      MsgState.initialize,
    );

    MsgType msgType = _int(msgInfor['type']).msgType;
    this._type = msgType;
    String? text = '';
    final content = _map(msgInfor['content']);
    switch (this._type) {
      case MsgType.text:
      case MsgType.link:
        if (content.containsKey('content')) {
          text = _string(content['content']);
        }
        break;
      case MsgType.createConversation:
      case MsgType.renameConversation:
        String? groupName = _string(content['groupName']);
        String? creator = _string(content['creator']);
        List<StringeeUser> participants = [];
        List<dynamic> participantArray = _list(content['participants']);
        for (int i = 0; i < participantArray.length; i++) {
          final user = _user(participantArray[i]);
          if (user != null) {
            participants.add(user);
          }
        }
        this._notiContent = new Map<dynamic, dynamic>();
        this._notiContent!["groupName"] = groupName;
        this._notiContent!["creator"] = creator;
        this._notiContent!["participants"] = participants;
        break;
      case MsgType.photo:
        if (content.containsKey('photo')) {
          Map<dynamic, dynamic> photoMap = _map(content['photo']);
          this._filePath = _string(photoMap['filePath']);
          this._fileUrl = _string(photoMap['fileUrl']);
          this._thumbnail = _string(photoMap['thumbnail']);
          this._ratio = _double(photoMap['ratio']);
        }
        break;
      case MsgType.video:
        if (content.containsKey('video')) {
          Map<dynamic, dynamic> videoMap = _map(content['video']);
          this._filePath = _string(videoMap['filePath']);
          this._fileUrl = _string(videoMap['fileUrl']);
          this._thumbnail = _string(videoMap['thumbnail']);
          this._ratio = _double(videoMap['ratio']);
          this._duration = _double(videoMap['duration']);
        }
        break;
      case MsgType.audio:
        if (content.containsKey('audio')) {
          Map<dynamic, dynamic> audioMap = _map(content['audio']);
          this._filePath = _string(audioMap['filePath']);
          this._fileUrl = _string(audioMap['fileUrl']);
          this._duration = _double(audioMap['duration']);
        }
        break;
      case MsgType.file:
        if (content.containsKey('file')) {
          Map<dynamic, dynamic> fileMap = _map(content['file']);
          this._filePath = _string(fileMap['filePath']);
          this._fileUrl = _string(fileMap['fileUrl']);
          this._fileName = _string(fileMap['fileName']);
          this._fileLength = _int(fileMap['fileLength']);
        }
        break;
      case MsgType.location:
        if (content.containsKey('location')) {
          Map<dynamic, dynamic> locationMap = _map(content['location']);
          this._latitude = _double(locationMap['lat']);
          this._longitude = _double(locationMap['lon']);
        }
        break;
      case MsgType.contact:
        if (content.containsKey('contact')) {
          Map<dynamic, dynamic> contactMap = _map(content['contact']);
          this._vcard = _string(contactMap['vcard']);
        }
        break;
      case MsgType.sticker:
        if (content.containsKey('sticker')) {
          Map<dynamic, dynamic> stickerMap = _map(content['sticker']);
          this._stickerName = _string(stickerMap['name']);
          this._stickerCategory = _string(stickerMap['category']);
        }
        break;
      case MsgType.notification:
        Map<dynamic, dynamic> notifyMap = content;
        this._notiContent = new Map<dynamic, dynamic>();
        MsgNotifyType notifyType = _int(notifyMap['type']).notifyType;
        this._notiContent!['type'] = notifyType;
        switch (notifyType) {
          case MsgNotifyType.addParticipants:
            this._notiContent!['addedby'] = _user(notifyMap['addedInfo']);
            List<StringeeUser> participants = [];
            List<dynamic> participantArray = _list(notifyMap['participants']);
            for (int i = 0; i < participantArray.length; i++) {
              final user = _user(participantArray[i]);
              if (user != null) {
                participants.add(user);
              }
            }
            this._notiContent!["participants"] = participants;
            break;
          case MsgNotifyType.removeParticipants:
            this._notiContent!['removedBy'] = _user(notifyMap['removedInfo']);
            List<StringeeUser> participants = [];
            List<dynamic> participantArray = _list(notifyMap['participants']);
            for (int i = 0; i < participantArray.length; i++) {
              final user = _user(participantArray[i]);
              if (user != null) {
                participants.add(user);
              }
            }
            this._notiContent!["participants"] = participants;
            break;
          case MsgNotifyType.changeGroupName:
            this._notiContent!['groupName'] = _string(notifyMap['groupName']);
            break;
        }
        break;
      case null:
        break;
    }
    this._text = text;
  }

  /// Creates the compact last-message representation stored on a conversation.
  StringeeMessage.lstMsg(
      String? msgId,
      String? convId,
      MsgType msgType,
      String? senderId,
      int? sequence,
      MsgState msgState,
      int? createdAt,
      Map<dynamic, dynamic>? msgInfor) {
    if (msgId == null ||
        senderId == null ||
        sequence == null ||
        createdAt == null ||
        msgInfor == null) {
      return;
    }
    this._id = msgId;
    this._convId = convId;
    this._senderId = senderId;
    this._createdAt = createdAt;
    this._sequence = sequence;
    if (msgInfor.containsKey('metadata') &&
        msgInfor['metadata'] != null &&
        msgInfor['metadata'].toString().isNotEmpty) {
      this._customData = msgInfor['metadata'];
    }
    this._state = msgState;
    this._type = msgType;
    String? text = '';
    switch (this._type) {
      case MsgType.text:
      case MsgType.link:
        if (msgInfor.containsKey('text')) {
          text = _string(msgInfor['text']);
        }
        break;
      case MsgType.createConversation:
      case MsgType.renameConversation:
        List<StringeeUser> participants = [];
        List<dynamic> participantArray = _list(msgInfor['participants']);
        for (int i = 0; i < participantArray.length; i++) {
          final user = _user(participantArray[i]);
          if (user != null) {
            participants.add(user);
          }
        }
        this._notiContent = new Map<dynamic, dynamic>();
        this._notiContent!["groupName"] = _string(msgInfor['groupName']);
        this._notiContent!["creator"] = _string(msgInfor['creator']);
        this._notiContent!["participants"] = participants;
        break;
      case MsgType.photo:
        if (msgInfor.containsKey('photo')) {
          Map<dynamic, dynamic> photoMap = _map(msgInfor['photo']);
          this._filePath = _string(photoMap['filePath']);
          this._fileUrl = _string(photoMap['fileUrl']);
          this._thumbnail = _string(photoMap['thumbnail']);
          this._ratio = _double(photoMap['ratio']);
        }
        break;
      case MsgType.video:
        if (msgInfor.containsKey('video')) {
          Map<dynamic, dynamic> videoMap = _map(msgInfor['video']);
          this._filePath = _string(videoMap['filePath']);
          this._fileUrl = _string(videoMap['fileUrl']);
          this._thumbnail = _string(videoMap['thumbnail']);
          this._ratio = _double(videoMap['ratio']);
          this._duration = _double(videoMap['duration']);
        }
        break;
      case MsgType.audio:
        if (msgInfor.containsKey('audio')) {
          Map<dynamic, dynamic> audioMap = _map(msgInfor['audio']);
          this._filePath = _string(audioMap['filePath']);
          this._fileUrl = _string(audioMap['fileUrl']);
          this._duration = _double(audioMap['duration']);
        }
        break;
      case MsgType.file:
        if (msgInfor.containsKey('file')) {
          Map<dynamic, dynamic> fileMap = _map(msgInfor['file']);
          this._filePath = _string(fileMap['filePath']);
          this._fileUrl = _string(fileMap['fileUrl']);
          this._fileName = _string(fileMap['fileName']);
          this._fileLength = _int(fileMap['fileLength']);
        }
        break;
      case MsgType.location:
        if (msgInfor.containsKey('location')) {
          Map<dynamic, dynamic> locationMap = _map(msgInfor['location']);
          this._latitude = _double(locationMap['lat']);
          this._longitude = _double(locationMap['lon']);
        }
        break;
      case MsgType.contact:
        if (msgInfor.containsKey('contact')) {
          Map<dynamic, dynamic> contactMap = _map(msgInfor['contact']);
          this._vcard = _string(contactMap['vcard']);
        }
        break;
      case MsgType.sticker:
        if (msgInfor.containsKey('sticker')) {
          Map<dynamic, dynamic> stickerMap = _map(msgInfor['sticker']);
          this._stickerName = _string(stickerMap['name']);
          this._stickerCategory = _string(stickerMap['category']);
        }
        break;
      case MsgType.notification:
        this._notiContent = new Map<dynamic, dynamic>();
        int notiType = _int(msgInfor['type']) ?? 0;
        if (notiType != 0) {
          MsgNotifyType notifyType = notiType.notifyType;
          this._notiContent!['type'] = notifyType;
          switch (notifyType) {
            case MsgNotifyType.addParticipants:
              this._notiContent!['addedby'] = _user(msgInfor['addedInfo']);
              List<StringeeUser> participants = [];
              List<dynamic> participantArray = _list(msgInfor['participants']);
              for (int i = 0; i < participantArray.length; i++) {
                final user = _user(participantArray[i]);
                if (user != null) {
                  participants.add(user);
                }
              }
              this._notiContent!["participants"] = participants;
              break;
            case MsgNotifyType.removeParticipants:
              this._notiContent!['removedBy'] = _user(msgInfor['removedInfo']);
              List<StringeeUser> participants = [];
              List<dynamic> participantArray = _list(msgInfor['participants']);
              for (int i = 0; i < participantArray.length; i++) {
                final user = _user(participantArray[i]);
                if (user != null) {
                  participants.add(user);
                }
              }
              this._notiContent!["participants"] = participants;
              break;
            case MsgNotifyType.changeGroupName:
              this._notiContent!['groupName'] = _string(msgInfor['groupName']);
              break;
          }
          break;
        }
        break;
      case null:
        break;
    }
    this._text = text;
  }

  /// Converts this outgoing message to a platform-channel payload.
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
      case MsgType.createConversation:
      case MsgType.renameConversation:
      case MsgType.notification:
      case null:
        break;
    }
    return params;
  }

  /// Edits this message text content.
  Future<Map<dynamic, dynamic>> edit(String content) async {
    assert(content.trim().isNotEmpty);
    final params = {
      'convId': this._convId!.trim(),
      'msgId': this._id,
      'content': content,
      'uuid': _client.uuid
    };
    return await StringeeClient.methodChannel.invokeMethod('editMsg', params);
  }

  /// Pins or unpins this message.
  Future<Map<dynamic, dynamic>> pinOrUnPin(bool pinOrUnPin) async {
    final params = {
      'convId': this._convId!.trim(),
      'msgId': this._id,
      'pinOrUnPin': pinOrUnPin,
      'uuid': _client.uuid
    };
    return await StringeeClient.methodChannel
        .invokeMethod('pinOrUnPin', params);
  }
}
