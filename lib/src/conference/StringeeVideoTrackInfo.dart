import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

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
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
    this._audioEnable = StringeeValueParser.toBool(info['audio']) ?? false;
    this._videoEnable = StringeeValueParser.toBool(info['video']) ?? false;
    this._isScreenCapture = StringeeValueParser.toBool(info['screen']) ?? false;
    this._publisher = StringeeRoomUser(
      StringeeValueParser.toMap(info['publisher']) ?? {},
    );
  }

  StringeeVideoTrackInfo.fromTrack(StringeeVideoTrack track) {
    this._id = track.id;
    this._audioEnable = track.audioEnable;
    this._videoEnable = track.videoEnable;
    this._isScreenCapture = track.isScreenCapture;
    this._publisher = track.publisher;
  }
}
