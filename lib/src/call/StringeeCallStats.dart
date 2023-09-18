class StringeeCallStats {
  late int _callPacketsLost;
  late int _callPacketsReceived;
  late int _callBytesReceived;
  late int _videoPacketsLost;
  late int _videoPacketsReceived;
  late int _videoBytesReceived;
  late int _timeStamp;

  StringeeCallStats(Map<String, Object> callStats) {
    this._callPacketsLost = callStats['callPacketsLost'] as int;
    this._callPacketsReceived = callStats['callPacketsReceived'] as int;
    this._callBytesReceived = callStats['callBytesReceived'] as int;
    this._videoPacketsLost = callStats['videoPacketsLost'] as int;
    this._videoPacketsReceived = callStats['videoPacketsReceived'] as int;
    this._videoBytesReceived = callStats['videoBytesReceived'] as int;
    this._timeStamp = callStats['timeStamp'] as int;
  }

  int get callPacketsLost => _callPacketsLost;

  int get callPacketsReceived => _callPacketsReceived;

  int get callBytesReceived => _callBytesReceived;

  int get videoPacketsLost => _videoPacketsLost;

  int get videoPacketsReceived => _videoPacketsReceived;

  int get videoBytesReceived => _videoBytesReceived;

  int get timeStamp => _timeStamp;

  @override
  String toString() {
    return toJson().toString();
  }

  Map<String, dynamic> toJson() {
    return {
      'callPacketsLost': callPacketsLost,
      'callPacketsReceived': callPacketsReceived,
      'callBytesReceived': callBytesReceived,
      'videoPacketsLost': videoPacketsLost,
      'videoPacketsReceived': videoPacketsReceived,
      'videoBytesReceived': videoBytesReceived,
      'timeStamp': timeStamp,
    };
  }
}
