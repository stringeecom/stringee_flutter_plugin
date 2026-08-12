import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// Metadata describing an available conference video track.
class StringeeVideoTrackInfo {
  late String _id;
  late bool _audioEnable;
  late bool _videoEnable;
  late bool _isScreenCapture;
  late StringeeRoomUser _publisher;

  /// Server-assigned track ID.
  String get id => _id;

  /// Whether audio is available on the track.
  bool get audioEnable => _audioEnable;

  /// Whether video is available on the track.
  bool get videoEnable => _videoEnable;

  /// Whether the source is screen capture.
  bool get isScreenCapture => _isScreenCapture;

  /// User publishing the track.
  StringeeRoomUser get publisher => _publisher;

  /// Creates metadata from a native [info] payload.
  StringeeVideoTrackInfo(Map<dynamic, dynamic> info) {
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
    this._audioEnable = StringeeValueParser.toBool(info['audio']) ?? false;
    this._videoEnable = StringeeValueParser.toBool(info['video']) ?? false;
    this._isScreenCapture = StringeeValueParser.toBool(info['screen']) ?? false;
    this._publisher = StringeeRoomUser(
      StringeeValueParser.toMap(info['publisher']) ?? {},
    );
  }

  /// Creates metadata from an existing [track].
  StringeeVideoTrackInfo.fromTrack(StringeeVideoTrack track) {
    this._id = track.id;
    this._audioEnable = track.audioEnable;
    this._videoEnable = track.videoEnable;
    this._isScreenCapture = track.isScreenCapture;
    this._publisher = track.publisher;
  }
}
