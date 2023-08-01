import 'dart:io';

import '../../stringee_flutter_plugin.dart';

class StringeeVideo {
  late StringeeClient _client;

  StringeeVideo(StringeeClient client) {
    _client = client;
  }

  /// Connect to [StringeeVideoRoom]
  Future<Map<dynamic, dynamic>> joinRoom(String roomToken) async {
    if (roomToken.isEmpty) return await reportInvalidValue('roomToken');
    final params = {
      'roomToken': roomToken,
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.joinRoom', params);

    if (result['status']) {
      StringeeVideoRoom room =
          StringeeVideoRoom(_client, result['body']['room']);
      result['body']['room'] = room;

      List<StringeeVideoTrackInfo> videoTrackList = [];
      List<dynamic> tracksData = result['body']['videoTrackInfos'];
      if (tracksData.length > 0)
        videoTrackList =
            tracksData.map((info) => StringeeVideoTrackInfo(info)).toList();
      result['body']['videoTrackInfos'] = videoTrackList;

      List<StringeeRoomUser> userList = [];
      List<dynamic> usersData = result['body']['users'];
      if (usersData.length > 0)
        userList = usersData.map((info) => StringeeRoomUser(info)).toList();
      result['body']['users'] = userList;
    }
    return result;
  }

  /// Create local [StringeeVideoTrack]
  Future<Map<dynamic, dynamic>> createLocalVideoTrack(
      StringeeVideoTrackOption options) async {
    final params = {
      'options': options.toJson(),
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.createLocalVideoTrack', params);

    if (result['status']) {
      StringeeVideoTrack videoTrack =
          StringeeVideoTrack(_client, result['body']);
      result['body'] = videoTrack;
    }
    return result;
  }

  /// Create capture screen [StringeeVideoTrack]
  // Future<Map<dynamic, dynamic>> createCaptureScreenTrack() async {
  //   if (Platform.isAndroid) {
  //     final params = {
  //       'uuid': _client.uuid,
  //     };
  //
  //     Map<dynamic, dynamic> result = await StringeeClient.methodChannel
  //         .invokeMethod('video.createCaptureScreenTrack', params);
  //
  //     if (result['status']) {
  //       StringeeVideoTrack videoTrack =
  //           StringeeVideoTrack(_client, result['body']);
  //       result['body'] = videoTrack;
  //     }
  //     return result;
  //   } else {
  //     Map<dynamic, dynamic> result = {
  //       'status': false,
  //       'code': -1,
  //       'message': 'This function is only available in Android',
  //     };
  //     return result;
  //   }
  // }
}
