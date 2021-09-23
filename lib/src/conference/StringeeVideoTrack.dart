import '../../stringee_flutter_plugin.dart';

class StringeeVideoTrack {
  late final String _id;
  late bool _audioEnable;
  late bool _videoEnable;
  late bool _isScreenCapture;
  late bool _isLocal;
  late final StringeeClient _client;

  String get id => _id;

  bool get audioEnable => _audioEnable;

  bool get videoEnable => _videoEnable;

  bool get isScreenCapture => _isScreenCapture;

  bool get isLocal => _isLocal;

  @override
  String toString() {
    return '{id: $_id, audioEnable: $_audioEnable, videoEnable: $_videoEnable, isScreenCapture: $_isScreenCapture, isLocal: $_isLocal}';
  }

  StringeeVideoTrack.fromTrackInfo(
    Map<dynamic, dynamic> info,
    StringeeClient client,
  ) {
    this._client = client;
    this._id = info['id'];
    this._audioEnable = info['audio'];
    this._videoEnable = info['video'];
    this._isScreenCapture = info['screen'];
    this._isLocal = info['isLocal'];
  }

  void mute(bool mute) {}

  void enableVideo(bool enable) {}

  void switchCamera() {}

  void attach() {}

  void detach() {}

  void close(){

  }
}
