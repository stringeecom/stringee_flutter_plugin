import '../../stringee_flutter_plugin.dart';

class StringeeChatProfile {
  String? _id;
  String? _portalId;
  int? _projectId;
  String? _language;
  String? _background;
  bool? _isAutoCreateTicket;
  String? _popupAnswerUrl;
  int? _numberOfAgents;
  String? _logoUrl;
  bool? _enabledBusinessHour;
  String? _businessHourId;
  String? _businessHour;
  List<StringeeQueue>? _queues;
  bool? _facebookAsLivechat;
  bool? _zaloAsLivechat;

  String? get id => _id;

  String? get portalId => _portalId;

  int? get projectId => _projectId;

  String? get language => _language;

  String? get background => _background;

  bool? get isAutoCreateTicket => _isAutoCreateTicket;

  String? get popupAnswerUrl => _popupAnswerUrl;

  int? get numberOfAgents => _numberOfAgents;

  String? get logoUrl => _logoUrl;

  bool? get enabledBusinessHour => _enabledBusinessHour;

  String? get businessHourId => _businessHourId;

  String? get businessHour => _businessHour;

  List<StringeeQueue>? get queues => _queues;

  bool? get facebookAsLivechat => _facebookAsLivechat;

  bool? get zaloAsLivechat => _zaloAsLivechat;

  @override
  String toString() {
    return {
      if (_id != null) 'id': _id!.trim(),
      if (_portalId != null) 'portalId': _portalId!.trim(),
      if (_projectId != null) 'projectId': _projectId!,
      if (_language != null) 'language': _language!.trim(),
      if (_background != null) 'background': _background!.trim(),
      if (_isAutoCreateTicket != null)
        'isAutoCreateTicket': _isAutoCreateTicket!,
      if (_popupAnswerUrl != null) 'popupAnswerUrl': _popupAnswerUrl!.trim(),
      if (_numberOfAgents != null) 'numberOfAgents': _numberOfAgents!,
      if (_logoUrl != null) 'logoUrl': _logoUrl!.trim(),
      if (_enabledBusinessHour != null)
        'enabledBusinessHour': _enabledBusinessHour!,
      if (_businessHourId != null) 'businessHourId': _businessHourId!.trim(),
      if (_businessHour != null) 'businessHour': _businessHour!.trim(),
      if (_queues != null) 'queues': _queues!.toString(),
      if (_facebookAsLivechat != null)
        'facebookAsLivechat': _facebookAsLivechat!,
      if (_zaloAsLivechat != null) 'zaloAsLivechat': _zaloAsLivechat!,
    }.toString();
  }

  StringeeChatProfile.fromJson(Map<dynamic, dynamic> json) {
    this._id = json['id'];
    this._portalId = json['portalId'];
    this._projectId = json['projectId'];
    this._language = json['language'];
    this._background = json['background'];
    this._isAutoCreateTicket = json['isAutoCreateTicket'];
    this._popupAnswerUrl = json['popupAnswerUrl'];
    this._numberOfAgents = json['numberOfAgents'];
    this._logoUrl = json['logoUrl'];
    this._enabledBusinessHour = json['enabledBusinessHour'];
    this._businessHourId = json['businessHourId'];
    this._businessHour = json['businessHour'];
    List<StringeeQueue> queues = [];
    List<dynamic> queueArray = json['queues'];
    for (int i = 0; i < queueArray.length; i++) {
      StringeeQueue queue = StringeeQueue.fromJson(queueArray[i]);
      queues.add(queue);
    }
    this._queues = queues;
    this._facebookAsLivechat = json['facebookAsLivechat'];
    this._zaloAsLivechat = json['zaloAsLivechat'];
  }
}
