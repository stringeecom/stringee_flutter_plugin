import 'dart:io';

import '../../stringee_flutter_plugin.dart';

class StringeeVideo {
  late StringeeClient _client;

  StringeeVideo(StringeeClient client) {
    _client = client;
  }

  /// Connect to [StringeeRoom]
  Future<Map<dynamic, dynamic>> connect(String roomToken) async {
    if (roomToken.isEmpty) return await reportInvalidValue('roomToken');
    final params = {
      'roomToken': roomToken,
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.connect', params);

    StringeeRoom room = StringeeRoom(_client, result['body']['room']);
    result['body']['room'] = room;

    List<StringeeVideoTrack> videoTrackList = [];
    List<dynamic> tracksData = result['body']['videoTracks'];
    if (tracksData.length > 0)
      videoTrackList =
          tracksData.map((info) => StringeeVideoTrack(_client, info)).toList();
    result['body']['videoTracks'] = videoTrackList;

    List<StringeeRoomUser> userList = [];
    List<dynamic> usersData = result['body']['users'];
    if (usersData.length > 0)
      userList = usersData.map((info) => StringeeRoomUser(info)).toList();
    result['body']['users'] = userList;

    return result;
  }

  /// Create local [StringeeVideoTrack]
  Future<Map<dynamic, dynamic>> createLocalVideoTrack(
      StringeeVideoTrackOptions options) async {
    String localId = Platform.operatingSystem +
        DateTime.now().millisecondsSinceEpoch.toString();
    final params = {
      'localId': localId,
      'options': options.toJson(),
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.createLocalVideoTrack', params);

    StringeeVideoTrack videoTrack = StringeeVideoTrack.local(_client, result['body']);
    result['body'] = videoTrack;
    return result;
  }

  /// Release all [StringeeVideoTrack] in [StringeeRoom]
  Future<Map<dynamic, dynamic>> release(StringeeRoom room) async {
    final params = {
      'roomId': room.id,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('video.release', params);
  }
}
