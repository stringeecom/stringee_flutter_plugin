import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

class Room extends StatefulWidget {
  late StringeeVideo _video;
  late String _roomToken;

  Room(StringeeClient client, String roomToken) {
    _video = StringeeVideo(client);
    this._roomToken = roomToken;
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return RoomState();
  }
}

class RoomState extends State<Room> {
  late StringeeRoom _room;
  late StringeeVideoTrack _localTrack;
  late StringeeVideoView _localTrackView;
  bool _hasLocalView = false;
  bool _isMute = false;
  bool _isVideoEnable = true;
  int _cameraId = 1;
  List<StringeeVideoTrack> _trackList = [];

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    widget._video.connect(widget._roomToken).then((value) {
      if (value['status']) {
        _room = value['body']['room'];
        initRoom(value['body']['videoTracks'], value['body']['users']);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget _localView = (_hasLocalView) ? _localTrackView : Placeholder();

    Container bottomContainer = new Container(
      padding: EdgeInsets.only(bottom: 30.0),
      alignment: Alignment.bottomCenter,
      child: new Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          new GestureDetector(
            onTap: _toggleSwitchCamera,
            child: Image.asset(
              'images/ic_switch.png',
              height: 50.0,
              width: 50.0,
            ),
          ),
          GestureDetector(
            onTap: _toggleMicro,
            child: Image.asset(
              _isMute ? 'images/ic_mute.png' : 'images/ic_mic.png',
              height: 50.0,
              width: 50.0,
            ),
          ),
          GestureDetector(
            onTap: _toggleVideo,
            child: Image.asset(
              _isVideoEnable
                  ? 'images/ic_video.png'
                  : 'images/ic_video_off.png',
              height: 50.0,
              width: 50.0,
            ),
          ),
          new GestureDetector(
            onTap: _leaveRoomTapped,
            child: Image.asset(
              'images/end.png',
              height: 50.0,
              width: 50.0,
            ),
          ),
        ],
      ),
    );

    return Scaffold(
      body: Stack(
        children: [
          _localView,
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              height: 200.0,
              margin: EdgeInsets.only(bottom: 100.0),
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _trackList.length,
                itemBuilder: (context, index) {
                  return _trackList[index].attach(
                    isOverlay: true,
                    height: 200.0,
                    width: 150.0,
                    scalingType: ScalingType.fit,
                  );
                },
              ),
            ),
          ),
          bottomContainer,
        ],
      ),
    );
  }

  void initRoom(List<StringeeVideoTrack> videoTrackList,
      List<StringeeRoomUser> userList) {
    _room.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      print("Room " + map.toString());
      switch (map['eventType']) {
        case StringeeRoomEvents.didJoinRoom:
          handleJoinRoomEvent(map['body']);
          break;
        case StringeeRoomEvents.didLeaveRoom:
          handleLeaveRoomEvent(map['body']);
          break;
        case StringeeRoomEvents.didAddVideoTrack:
          handleAddVideoTrackEvent(map['body']);
          break;
        case StringeeRoomEvents.didRemoveVideoTrack:
          handleRemoveVideoTrackEvent(map['body']);
          break;
        case StringeeRoomEvents.didReceiveRoomMessage:
          handleReceiveRoomMessageEvent(map['body']);
          break;
        case StringeeRoomEvents.didReceiveVideoTrackControlNotification:
          handleReceiveVideoTrackControlNotification(map['body']);
          break;
        default:
          break;
      }
    });

    StringeeVideoTrackOptions options =
        StringeeVideoTrackOptions(true, true, false);
    widget._video.createLocalVideoTrack(options).then((value) {
      if (value['status']) {
        _room.publish(value['body']).then((value) {
          if (value['status']) {
            setState(() {
              _hasLocalView = true;
              _localTrack = value['body'];
              _localTrackView = _localTrack.attach(
                alignment: Alignment.center,
                scalingType: ScalingType.fit,
              );
            });
          }
        });
      }
    });

    if (videoTrackList.length > 0) {
      videoTrackList.forEach((track) {
        StringeeVideoTrackOptions options = StringeeVideoTrackOptions(
            track.audioEnable, track.videoEnable, track.isScreenCapture);
        _room.subscribe(track, options).then((value) {
          if (value['status']) {
            setState(() {
              _trackList.add(track);
            });
          }
        });
      });
    }
  }

  void handleJoinRoomEvent(StringeeRoomUser joinUser) {}

  void handleLeaveRoomEvent(StringeeRoomUser leaveUser) {}

  void handleAddVideoTrackEvent(StringeeVideoTrack addTrack) {
    StringeeVideoTrackOptions options = StringeeVideoTrackOptions(
        addTrack.audioEnable, addTrack.videoEnable, addTrack.isScreenCapture);
    _room.subscribe(addTrack, options).then((value) {
      if (value['status']) {
        setState(() {
          _trackList.add(addTrack);
        });
      }
    });
  }

  void handleRemoveVideoTrackEvent(StringeeVideoTrack removeTrack) {
    _room.unsubscribe(removeTrack).then((value) {
      if (value['status']) {
        setState(() {
          if (_trackList.length > 0) {
            _trackList.removeAt(
                _trackList.indexWhere((track) => track.id == removeTrack.id));
          }
        });
      }
    });
  }

  void handleReceiveRoomMessageEvent(Map<dynamic, dynamic> bodyMap) {}

  void handleReceiveVideoTrackControlNotification(
      Map<dynamic, dynamic> bodyMap) {}

  void _toggleSwitchCamera() {
    setState(() {
      _cameraId = _cameraId == 1 ? 0 : 1;
    });
    _localTrack.switchCamera(cameraId: _cameraId).then((result) {
      bool status = result['status'];
      if (status) {}
    });
  }

  void _toggleMicro() {
    _localTrack.mute(!_isMute).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isMute = !_isMute;
        });
      }
    });
  }

  void _toggleVideo() {
    _localTrack.enableVideo(!_isVideoEnable).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isVideoEnable = !_isVideoEnable;
        });
      }
    });
  }

  void _leaveRoomTapped() {
    _room.unPublish(_localTrack).then((result) {
      if (result['status']) {
        _localTrack.close().then((value) {
          if (result['status']) {
            _room.leave(false).then((value) {
              clearDataEndDismiss();
            });
          }
        });
      }
    });
    _localTrack.enableVideo(!_isVideoEnable).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isVideoEnable = !_isVideoEnable;
        });
      }
    });
  }

  void clearDataEndDismiss() {
    _room.destroy();
    Navigator.pop(context);
  }
}
