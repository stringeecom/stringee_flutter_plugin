import '../../stringee_flutter_plugin.dart';

class StringeeVideo {
  late StringeeClient _client;

  StringeeVideo(StringeeClient client) {
    _client = client;
  }

  /// Connect to [StringeeRoom]
  Future<Map<dynamic, dynamic>> connect(String roomToken) async {
    if (roomToken.isEmpty) return await reportInvalidValue('roomToken');
    final params = {'roomToken': roomToken, 'uuid': _client.uuid};

    /// Convert StringeeRoom, list participant
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.connect', params);
    return result;
  }

  /// Create local [StringeeVideoTrack]
  Future<Map<dynamic, dynamic>> createLocalVideoTrack(
      StringeeVideoTrackOptions options) async {
    final params = {'options': options.toJson(), 'uuid': _client.uuid};

    /// Convert VideoTrack
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.createLocalVideoTrack', params);
    return result;
  }

  /// Release all [StringeeVideoTrack] in [StringeeRoom]
  Future<Map<dynamic, dynamic>> release() async {
    final params = {'uuid': _client.uuid};
    return await StringeeClient.methodChannel
        .invokeMethod('video.release', params);
  }
}
