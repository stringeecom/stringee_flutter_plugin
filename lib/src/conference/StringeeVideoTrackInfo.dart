import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

class StringeeVideoTrackInfo {
  late String _id;
  late bool _audioEnable;
  late bool _videoEnable;
  late bool _isScreenCapture;
  late StringeeRoomUser _publisher;

  String get id => _id;

  bool get audioEnable => _audioEnable;

  bool get videoEnable => _videoEnable;

  bool get isScreenCapture => _isScreenCapture;

  StringeeRoomUser get publisher => _publisher;

  StringeeVideoTrackInfo(Map<dynamic, dynamic> info) {
    this._id = info['id'];
    this._audioEnable = info['audio'];
    this._videoEnable = info['video'];
    this._isScreenCapture = info['screen'];
    this._publisher = StringeeRoomUser(info['publisher']);
  }

  StringeeVideoTrackInfo.fromTrack(StringeeVideoTrack track) {
    this._id = track.id;
    this._audioEnable = track.audioEnable;
    this._videoEnable = track.videoEnable;
    this._isScreenCapture = track.isScreenCapture;
    this._publisher = track.publisher;
  }

  @override
  String toString() {
    return {
      'id': _id,
      'audioEnable': _audioEnable,
      'videoEnable': _videoEnable,
      'isScreenCapture': _isScreenCapture,
      'publisher': _publisher.toString(),
    }.toString();
  }
}
