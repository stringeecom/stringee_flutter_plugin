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

  StringeeUser({
    required String userId,
    String? name,
    String? avatarUrl,
    UserRole? role,
    String? email,
    String? phone,
    String? location,
    String? browser,
    String? platform,
    String? device,
    String? ipAddress,
    String? hostName,
    String? userAgent,
  }) {
    this._userId = userId;
    this._name = name;
    this._avatarUrl = avatarUrl;
    if (role != null) {
      this._role = role;
    } else {
      this._role = UserRole.member;
    }
    this._email = email;
    this._phone = phone;
    this._location = location;
    this._browser = browser;
    this._platform = platform;
    this._device = device;
    this._ipAddress = ipAddress;
    this._hostName = hostName;
    this._userAgent = userAgent;
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

  @override
  String toString() {
    return '{userId: $_userId, name: $name, avatarUrl: $avatarUrl, role: $role, email: $email, phone: $phone, location: $location, browser: $browser, platform: $platform, device: $device, ipAddress: $ipAddress, hostName: $hostName, userAgent: $userAgent}';
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['userId'] = _userId!.trim();
    if (_name != null) params['name'] = _name!.trim();
    if (_avatarUrl != null) params['avatarUrl'] = _avatarUrl!.trim();
    params['role'] = _role.index;
    if (_email != null) params['email'] = _email!.trim();
    if (_phone != null) params['phone'] = _phone!.trim();
    if (_location != null) params['location'] = _location!.trim();
    if (_browser != null) params['browser'] = _browser!.trim();
    if (_platform != null) params['platform'] = _platform!.trim();
    if (_device != null) params['device'] = _device!.trim();
    if (_ipAddress != null) params['ipAddress'] = _ipAddress!.trim();
    if (_hostName != null) params['hostName'] = _hostName!.trim();
    if (_userAgent != null) params['userAgent'] = _userAgent!.trim();
    return params;
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
    this._platform = json['platform'];
    this._device = json['device'];
    this._ipAddress = json['ipAddress'];
    this._hostName = json['hostName'];
    this._userAgent = json['userAgent'];
  }
}
