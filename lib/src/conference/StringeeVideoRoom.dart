import 'dart:async';

import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

class StringeeVideoRoom {
  late String _id;
  late bool _recorded;
  late StringeeClient _client;
  StreamController<dynamic> _eventStreamController = StreamController();
  late StreamSubscription<dynamic> _subscriber;

  String get id => _id;

  bool get recorded => _recorded;

  StreamController<dynamic> get eventStreamController => _eventStreamController;

  StringeeVideoRoom(StringeeClient client, Map<dynamic, dynamic> info) {
    this._client = client;
    this._id = StringeeValueParser.toStringValue(info['id']) ?? '';
    this._recorded = StringeeValueParser.toBool(info['recorded']) ?? false;
    _subscriber = client.eventStreamController.stream.listen(this._listener);
  }

  void _listener(dynamic event) {
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    if (map['nativeEventType'] == StringeeObjectEventType.room.index &&
        map['uuid'] == _client.uuid) {
      switch (map['event']) {
        case 'didJoinRoom':
          handleDidJoinRoom(map['body']);
          break;
        case 'didLeaveRoom':
          handleDidLeaveRoom(map['body']);
          break;
        case 'didAddVideoTrack':
          handleDidAddVideoTrack(map['body']);
          break;
        case 'didRemoveVideoTrack':
          handleDidRemoveVideoTrack(map['body']);
          break;
        case 'didReceiveRoomMessage':
          handleDidReceiveRoomMessage(map['body']);
          break;
        case 'trackReadyToPlay':
          handleTrackReadyToPlay(map['body']);
          break;
        // case 'didReceiveVideoTrackControlNotification':
        //   handleDidReceiveVideoTrackControlNotification(map['body']);
        //   break;
      }
    }
  }

  void handleDidJoinRoom(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != this._id) return;

    _eventStreamController.add({
      "eventType": StringeeRoomEvents.didJoinRoom,
      "body": StringeeRoomUser(StringeeValueParser.toMap(map['user']) ?? {})
    });
  }

  void handleDidLeaveRoom(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != this._id) return;

    _eventStreamController.add({
      "eventType": StringeeRoomEvents.didLeaveRoom,
      "body": StringeeRoomUser(StringeeValueParser.toMap(map['user']) ?? {})
    });
  }

  void handleDidAddVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != this._id) return;

    _eventStreamController.add({
      "eventType": StringeeRoomEvents.didAddVideoTrack,
      "body": StringeeVideoTrackInfo(
        StringeeValueParser.toMap(map['videoTrackInfo']) ?? {},
      )
    });
  }

  void handleDidRemoveVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != this._id) return;

    _eventStreamController.add({
      "eventType": StringeeRoomEvents.didRemoveVideoTrack,
      "body": StringeeVideoTrackInfo(
        StringeeValueParser.toMap(map['videoTrackInfo']) ?? {},
      )
    });
  }

  void handleDidReceiveRoomMessage(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != this._id) return;
    Map<dynamic, dynamic> bodyMap = {
      'msg': map['msg'],
      'from': StringeeRoomUser(StringeeValueParser.toMap(map['from']) ?? {})
    };
    _eventStreamController.add({
      "eventType": StringeeRoomEvents.didReceiveRoomMessage,
      "body": bodyMap
    });
  }

  void handleTrackReadyToPlay(Map<dynamic, dynamic> map) {
    String? roomId = StringeeValueParser.toStringValue(map['roomId']);
    if (roomId != null && roomId != this._id) return;

    _eventStreamController.add({
      "eventType": StringeeRoomEvents.trackReadyToPlay,
      "body": StringeeVideoTrack(
        _client,
        StringeeValueParser.toMap(map['track']) ?? {},
      )
    });
  }

  // void handleDidReceiveVideoTrackControlNotification(
  //     Map<dynamic, dynamic> map) {
  //   String? roomId = map['roomId'];
  //   if (roomId != this._id) return;
  //   Map<dynamic, dynamic> bodyMap = {
  //     'videoTrack': StringeeVideoTrack(_client, map['videoTrack']),
  //     'from': StringeeRoomUser(map['from'])
  //   };
  //   _eventStreamController.add({
  //     "eventType": StringeeRoomEvents.didReceiveVideoTrackControlNotification,
  //     "body": bodyMap
  //   });
  // }

  /// Publishes a local [videoTrack] to this room.
  Future<Map<dynamic, dynamic>> publish(StringeeVideoTrack videoTrack) async {
    final params = {
      'roomId': _id,
      'localId': videoTrack.localId,
      'uuid': _client.uuid,
    };
    Map<dynamic, dynamic> result =
        await StringeeClient.methodChannel.invokeMethod('room.publish', params);
    if (result['status']) {
      videoTrack = StringeeVideoTrack(_client, result['body']);
      result['body'] = videoTrack;
    }
    return result;
  }

  /// Unpublishes a local [videoTrack] from this room.
  Future<Map<dynamic, dynamic>> unpublish(StringeeVideoTrack videoTrack) async {
    final params = {
      'roomId': _id,
      'localId': videoTrack.localId,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.unpublish', params);
  }

  /// Subscribes to a remote [trackInfo] with [option].
  Future<Map<dynamic, dynamic>> subscribe(
      StringeeVideoTrackInfo trackInfo, StringeeVideoTrackOption option) async {
    final params = {
      'roomId': _id,
      'trackId': trackInfo.id,
      'options': option.toJson(),
      'uuid': _client.uuid,
    };
    Map<dynamic, dynamic> result = await StringeeClient.methodChannel
        .invokeMethod('room.subscribe', params);
    if (result['status']) {
      StringeeVideoTrack videoTrack =
          StringeeVideoTrack(_client, result['body']);
      result['body'] = videoTrack;
    }

    return result;
  }

  /// Unsubscribes from a remote [trackInfo].
  Future<Map<dynamic, dynamic>> unsubscribe(
      StringeeVideoTrackInfo trackInfo) async {
    final params = {
      'roomId': _id,
      'trackId': trackInfo.id,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.unsubscribe', params);
  }

  /// Leaves this room.
  Future<Map<dynamic, dynamic>> leave({
    required bool allClient,
  }) async {
    final params = {
      'roomId': _id,
      'allClient': allClient,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.leave', params);
  }

  /// Sends a data [msg] to this room.
  Future<Map<dynamic, dynamic>> sendMessage(Map<dynamic, dynamic> msg) async {
    final params = {
      'roomId': _id,
      'msg': msg,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.sendMessage', params);
  }

  /// Cancels native event subscription and closes the room event stream.
  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
