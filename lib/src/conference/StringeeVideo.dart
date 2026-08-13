import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// Entry point for Stringee video-conference operations.
class StringeeVideo {
  late StringeeClient _client;

  /// Creates a conferencing API bound to [client].
  StringeeVideo(StringeeClient client) {
    _client = client;
  }

  /// Joins a [StringeeVideoRoom] with [roomToken].
  Future<Map<dynamic, dynamic>> joinRoom(String roomToken) async {
    if (roomToken.isEmpty) return await reportInvalidValue('roomToken');
    final params = {
      'roomToken': roomToken,
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.joinRoom', params);

    if (result['status']) {
      final body = StringeeValueParser.toMap(result['body']) ?? {};
      StringeeVideoRoom room = StringeeVideoRoom(
        _client,
        StringeeValueParser.toMap(body['room']) ?? {},
      );
      body['room'] = room;

      List<StringeeVideoTrackInfo> videoTrackList = [];
      List<dynamic> tracksData = StringeeValueParser.toList(
        body['videoTrackInfos'],
      );
      if (tracksData.length > 0)
        videoTrackList = tracksData
            .map((info) => StringeeVideoTrackInfo(
                  StringeeValueParser.toMap(info) ?? {},
                ))
            .toList();
      body['videoTrackInfos'] = videoTrackList;

      List<StringeeRoomUser> userList = [];
      List<dynamic> usersData = StringeeValueParser.toList(body['users']);
      if (usersData.length > 0)
        userList = usersData
            .map((info) => StringeeRoomUser(
                  StringeeValueParser.toMap(info) ?? {},
                ))
            .toList();
      body['users'] = userList;
      result['body'] = body;
    }
    return result;
  }

  /// Creates a local [StringeeVideoTrack] with [options].
  Future<Map<dynamic, dynamic>> createLocalVideoTrack(
      StringeeVideoTrackOption options) async {
    final params = {
      'options': options.toJson(),
      'uuid': _client.uuid,
    };

    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('video.createLocalVideoTrack', params);

    if (result['status']) {
      StringeeVideoTrack videoTrack = StringeeVideoTrack(
        _client,
        StringeeValueParser.toMap(result['body']) ?? {},
      );
      result['body'] = videoTrack;
    }
    return result;
  }

  // Creates a local screen-capture [StringeeVideoTrack].
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
