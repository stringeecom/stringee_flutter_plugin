import 'package:stringee_flutter_plugin/src/StringeeConstants.dart';

class StringeeUser {
  String? _userId;
  String? _name;
  String? _avatarUrl;
  UserRole _role = UserRole.member;
  String? _email;
  String? _phone;
  String? _location;
  String? _browser;
  String? _platform;
  String? _device;
  String? _ipAddress;
  String? _hostName;
  String? _userAgent;
  int? _lastMsgSeqReceived;
  int? _lastMsgSeqSeen;

  StringeeUser({
    required String userId,
    String? name,
    String? avatarUrl,
    int? lastMsgSeqReceived,
    int? lastMsgSeqSeen,
  }) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
    this._lastMsgSeqReceived = lastMsgSeqReceived;
    this._lastMsgSeqSeen = lastMsgSeqSeen;
  }

  StringeeUser.forUpdate({
    String? name,
    String? avatarUrl,
    String? email,
    String? phone,
    String? location,
    String? browser,
    String? platform,
    String? device,
    String? ipAddress,
    String? hostName,
    String? userAgent,
    int? lastMsgSeqReceived,
    int? lastMsgSeqSeen,
  }) {
    this._name = name;
    this._avatarUrl = avatarUrl;
    this._email = email;
    this._phone = phone;
    this._location = location;
    this._browser = browser;
    this._platform = platform;
    this._device = device;
    this._ipAddress = ipAddress;
    this._hostName = hostName;
    this._userAgent = userAgent;
    this._lastMsgSeqReceived = lastMsgSeqReceived;
    this._lastMsgSeqSeen = lastMsgSeqSeen;
  }

  String? get userId => _userId;

  String? get name => _name;

  String? get avatarUrl => _avatarUrl;

  UserRole get role => _role;

  String? get email => _email;

  String? get phone => _phone;

  String? get location => _location;

  String? get browser => _browser;

  String? get platform => _platform;

  String? get device => _device;

  String? get ipAddress => _ipAddress;

  String? get hostName => _hostName;

  String? get userAgent => _userAgent;

  int? get lastMsgSeqReceived => _lastMsgSeqReceived;

  int? get lastMsgSeqSeen => _lastMsgSeqSeen;

  @override
  String toString() {
    return {
      'user': _userId,
      'name': _name,
      'avatarUrl': _avatarUrl,
      'role': role.index,
      'email': _email,
      'phone': _phone,
      'location': _location,
      'browser': _browser,
      'platform': _platform,
      'device': _device,
      'ipAddress': _ipAddress,
      'hostName': _hostName,
      'userAgent': _userAgent,
      'lastMsgSeqReceived': _lastMsgSeqReceived,
      'lastMsgSeqSeen': _lastMsgSeqSeen,
    }.toString();
  }

  Map<String, dynamic> toJson() {
    return {
      if (_userId != null) 'user': _userId!.trim(),
      if (_name != null) 'name': _name!.trim(),
      if (_avatarUrl != null) 'avatarUrl': _avatarUrl!.trim(),
      'role': role.index,
      if (_email != null) 'email': _email!.trim(),
      if (_phone != null) 'phone': _phone!.trim(),
      if (_location != null) 'location': _location!.trim(),
      if (_browser != null) 'browser': _browser!.trim(),
      if (_platform != null) 'platform': _platform!.trim(),
      if (_device != null) 'device': _device!.trim(),
      if (_ipAddress != null) 'ipAddress': _ipAddress!.trim(),
      if (_hostName != null) 'hostName': _hostName!.trim(),
      if (_userAgent != null) 'userAgent': _userAgent!.trim(),
      if (_lastMsgSeqReceived != null)
        'lastMsgSeqReceived': _lastMsgSeqReceived,
      if (_lastMsgSeqSeen != null) 'lastMsgSeqSeen': _lastMsgSeqSeen,
    };
  }

  StringeeUser.fromJson(Map<dynamic, dynamic> json) {
    this._userId = json['user'];
    this._name = json['displayName'];
    this._avatarUrl = json['avatarUrl'];
    if (json.containsKey('role')) {
      String? role = json['role'];
      switch (role) {
        case 'member':
          this._role = UserRole.member;
          break;
        case 'admin':
          this._role = UserRole.admin;
          break;
        default:
          this._role = UserRole.member;
          break;
      }
    }
    this._email = json['email'];
    this._phone = json['phone'];
    this._location = json['location'];
    this._browser = json['browser'];
    this._device = json['device'];
    this._ipAddress = json['ipAddress'];
    this._hostName = json['hostName'];
    this._userAgent = json['userAgent'];
    this._lastMsgSeqReceived = json['lastMsgSeqReceived'];
    this._lastMsgSeqSeen = json['lastMsgSeqSeen'];
  }
}
