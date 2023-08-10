import 'dart:async';

import '../../stringee_flutter_plugin.dart';

class StringeeVideoRoom {
  late String _id;
  late bool _recorded;
  late StringeeClient _client;
  @Deprecated('')
  StreamController<dynamic> _eventStreamController = StreamController();
  late StreamSubscription<dynamic> _subscriber;

  String get id => _id;

  bool get recorded => _recorded;

  @Deprecated('')
  StreamController<dynamic> get eventStreamController => _eventStreamController;
  StringeeRoomListener? _roomListener;

  StringeeVideoRoom(StringeeClient client, Map<dynamic, dynamic> info) {
    this._client = client;
    this._id = info['id'];
    this._recorded = info['recorded'];
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

  void registerEvent(StringeeRoomListener roomListener) {
    _roomListener = roomListener;
  }

  void handleDidJoinRoom(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != this._id) return;

    StringeeRoomUser roomUser = new StringeeRoomUser(map['user']);
    if (_roomListener != null) {
      _roomListener!.onJoinRoom(roomUser);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeRoomEvents.didJoinRoom, "body": roomUser});
    }
  }

  void handleDidLeaveRoom(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != this._id) return;

    StringeeRoomUser roomUser = new StringeeRoomUser(map['user']);
    if (_roomListener != null) {
      _roomListener!.onLeaveRoom(roomUser);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeRoomEvents.didLeaveRoom, "body": roomUser});
    }
  }

  void handleDidAddVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != this._id) return;

    StringeeVideoTrackInfo videoTrackInfo =
        new StringeeVideoTrackInfo(map['videoTrackInfo']);
    if (_roomListener != null) {
      _roomListener!.onAddVideoTrack(videoTrackInfo);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didAddVideoTrack,
        "body": videoTrackInfo
      });
    }
  }

  void handleDidRemoveVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != this._id) return;

    StringeeVideoTrackInfo videoTrackInfo =
        new StringeeVideoTrackInfo(map['videoTrackInfo']);
    if (_roomListener != null) {
      _roomListener!.onRemoveVideoTrack(videoTrackInfo);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didRemoveVideoTrack,
        "body": videoTrackInfo
      });
    }
  }

  void handleDidReceiveRoomMessage(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != this._id) return;
    StringeeRoomUser roomUser = new StringeeRoomUser(map['from']);
    Map<dynamic, dynamic> bodyMap = {'msg': map['msg'], 'from': roomUser};

    if (_roomListener != null) {
      _roomListener!.onReceiveRoomMessage(roomUser, map['msg']);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didReceiveRoomMessage,
        "body": bodyMap
      });
    }
  }

  void handleTrackReadyToPlay(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeVideoTrack videoTrack =
        new StringeeVideoTrack(_client, map['track']);
    if (_roomListener != null) {
      _roomListener!.onTrackReadyToPlay(videoTrack);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeRoomEvents.trackReadyToPlay, "body": videoTrack});
    }
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

  /// Publish local [StringeeVideoTrack]
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

  /// Un publish local [StringeeVideoTrack]
  Future<Map<dynamic, dynamic>> unpublish(StringeeVideoTrack videoTrack) async {
    final params = {
      'roomId': _id,
      'localId': videoTrack.localId,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.unpublish', params);
  }

  /// Subscribe [StringeeVideoTrackInfo]
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

  /// Un subscribe [StringeeVideoTrack]
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

  /// Leave [StringeeVideoRoom]
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

  /// Send a message to [StringeeVideoRoom]
  Future<Map<dynamic, dynamic>> sendMessage(Map<dynamic, dynamic> msg) async {
    final params = {
      'roomId': _id,
      'msg': msg,
      'uuid': _client.uuid,
    };
    return await StringeeClient.methodChannel
        .invokeMethod('room.sendMessage', params);
  }

  /// close event stream
  void destroy() {
    _subscriber.cancel();
    _eventStreamController.close();
  }
}
