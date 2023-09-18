import 'dart:async';

import '../../stringee_flutter_plugin.dart';

class StringeeVideoRoom {
  String? _id;
  late bool _recorded;
  late StringeeClient _client;
  @deprecated
  StreamController<dynamic> _eventStreamController = StreamController();
  late StreamSubscription<dynamic> _subscriber;

  String? get id => _id;

  bool get recorded => _recorded;

  @deprecated
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
          _handleDidJoinRoom(map['body']);
          break;
        case 'didLeaveRoom':
          _handleDidLeaveRoom(map['body']);
          break;
        case 'didAddVideoTrack':
          _handleDidAddVideoTrack(map['body']);
          break;
        case 'didRemoveVideoTrack':
          _handleDidRemoveVideoTrack(map['body']);
          break;
        case 'didReceiveRoomMessage':
          _handleDidReceiveRoomMessage(map['body']);
          break;
        case 'trackReadyToPlay':
          _handleTrackReadyToPlay(map['body']);
          break;
        case 'didTrackMediaStateChange':
          _handleDidTrackMediaStateChange(map['body']);
          break;
        case 'didChangeAudioDevice':
          _handleDidChangeAudioDevice(map['body']);
          break;
      }
    }
  }

  void registerEvent(StringeeRoomListener roomListener) {
    _roomListener = roomListener;
  }

  void _handleDidJoinRoom(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeRoomUser roomUser = new StringeeRoomUser(map['user']);
    if (_roomListener != null) {
      _roomListener!.onJoinRoom(this, roomUser);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController
          .add({"eventType": StringeeRoomEvents.didJoinRoom, "body": roomUser});
    }
  }

  void _handleDidLeaveRoom(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeRoomUser roomUser = new StringeeRoomUser(map['user']);
    if (_roomListener != null) {
      _roomListener!.onLeaveRoom(this, roomUser);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add(
          {"eventType": StringeeRoomEvents.didLeaveRoom, "body": roomUser});
    }
  }

  void _handleDidAddVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeVideoTrackInfo videoTrackInfo =
        new StringeeVideoTrackInfo(map['videoTrackInfo']);
    if (_roomListener != null) {
      _roomListener!.onAddVideoTrack(this, videoTrackInfo);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didAddVideoTrack,
        "body": videoTrackInfo
      });
    }
  }

  void _handleDidRemoveVideoTrack(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeVideoTrackInfo videoTrackInfo =
        new StringeeVideoTrackInfo(map['videoTrackInfo']);
    if (_roomListener != null) {
      _roomListener!.onRemoveVideoTrack(this, videoTrackInfo);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didRemoveVideoTrack,
        "body": videoTrackInfo
      });
    }
  }

  void _handleDidReceiveRoomMessage(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;
    StringeeRoomUser roomUser = new StringeeRoomUser(map['from']);
    Map<dynamic, dynamic> bodyMap = {'msg': map['msg'], 'from': roomUser};

    if (_roomListener != null) {
      _roomListener!.onReceiveRoomMessage(this, roomUser, map['msg']);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.didReceiveRoomMessage,
        "body": bodyMap
      });
    }
  }

  void _handleTrackReadyToPlay(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    StringeeVideoTrack videoTrack =
        new StringeeVideoTrack(_client, map['track']);
    if (_roomListener != null) {
      _roomListener!.onTrackReadyToPlay(this, videoTrack);
    }
    if (!_eventStreamController.isClosed) {
      _eventStreamController.add({
        "eventType": StringeeRoomEvents.trackReadyToPlay,
        "body": videoTrack
      });
    }
  }

  void _handleDidTrackMediaStateChange(Map<dynamic, dynamic> map) {
    String? roomId = map['roomId'];
    if (roomId != null && roomId != this._id) return;

    if (_roomListener != null) {
      if (_roomListener!.onTrackMediaStateChange != null) {
        _roomListener!.onTrackMediaStateChange!(
            this,
            new StringeeVideoTrackInfo(map['videoTrackInfo']),
            new StringeeRoomUser(map['from']),
            MediaType.values[map['mediaType']],
            map['enable']);
      }
    }
  }

  void _handleDidChangeAudioDevice(Map<dynamic, dynamic> map) {
    AudioDevice selectedAudioDevice = AudioDevice.values[map['code']];
    List<dynamic> codeList = [];
    codeList.addAll(map['codeList']);
    List<AudioDevice> availableAudioDevices = [];
    for (int i = 0; i < codeList.length; i++) {
      AudioDevice audioDevice = AudioDevice.values[codeList[i]];
      availableAudioDevices.add(audioDevice);
    }
    if (_roomListener != null) {
      if (_roomListener!.onChangeAudioDevice != null) {
        _roomListener!.onChangeAudioDevice!(
            this, selectedAudioDevice, availableAudioDevices);
      }
    }
  }

  /// Publish local [StringeeVideoTrack]
  Future<Map<dynamic, dynamic>> publish(StringeeVideoTrack videoTrack,
      {StringeeVideoTrackOption? option}) async {
    final params = {
      'roomId': _id,
      'localId': videoTrack.localId,
      if (option != null) 'options': option.toJson(),
      'uuid': _client.uuid,
    };
    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('room.publish', params);
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
    return await _client.methodChannel.invokeMethod('room.unpublish', params);
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
    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('room.subscribe', params);
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
    return await _client.methodChannel.invokeMethod('room.unsubscribe', params);
  }

  /// Leave [StringeeVideoRoom]
  Future<Map<dynamic, dynamic>> leave({
    bool? allClient,
  }) async {
    final params = {
      'roomId': _id,
      'allClient': allClient != null ? allClient : false,
      'uuid': _client.uuid,
    };
    Map<dynamic, dynamic> result =
        await _client.methodChannel.invokeMethod('room.leave', params);
    if (result['status']) {
      destroy();
    }
    return result;
  }

  /// Send a message to [StringeeVideoRoom]
  Future<Map<dynamic, dynamic>> sendMessage(Map<dynamic, dynamic> msg) async {
    final params = {
      'roomId': _id,
      'msg': msg,
      'uuid': _client.uuid,
    };
    return await _client.methodChannel.invokeMethod('room.sendMessage', params);
  }

  /// Close event stream
  @deprecated
  void destroy() {
    _subscriber.cancel();
    if (!_eventStreamController.isClosed) _eventStreamController.close();
  }
}
