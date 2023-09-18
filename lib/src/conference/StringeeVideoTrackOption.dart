import '../../stringee_flutter_plugin.dart';

class StringeeVideoTrackOption {
  bool? _audio = true;
  bool? _video = true;
  bool? _screen = false;
  StringeeVideoDimensions? _videoDimension =
      StringeeVideoDimensions.dimension_288;

  bool? get audio => _audio;

  bool? get video => _video;

  bool? get screen => _screen;

  StringeeVideoDimensions? get videoDimension => _videoDimension;

  StringeeVideoTrackOption(
      {required bool audio,
      required bool video,
      required bool screen,
      StringeeVideoDimensions? videoDimension}) {
    this._audio = audio;
    this._video = video;
    this._screen = screen;
    if (videoDimension != null) this._videoDimension = videoDimension;
  }

  @override
  String toString() {
    return {
      'audio': _audio,
      'video': _video,
      'screen': _screen,
      'videoDimension': _videoDimension!.index,
    }.toString();
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> params = new Map();
    params['audio'] = _audio;
    params['video'] = _video;
    params['screen'] = _screen;
    switch (this._videoDimension) {
      case StringeeVideoDimensions.dimension_288:
        params['videoDimension'] = '288';
        break;
      case StringeeVideoDimensions.dimension_480:
        params['videoDimension'] = '480';
        break;
      case StringeeVideoDimensions.dimension_720:
        params['videoDimension'] = '720';
        break;
      case StringeeVideoDimensions.dimension_1080:
        params['videoDimension'] = '1080';
        break;
      default:
        params['videoDimension'] = '288';
        break;
    }
    return params;
  }
}
