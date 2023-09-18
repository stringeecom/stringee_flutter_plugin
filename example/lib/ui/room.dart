import 'dart:io';

import 'package:flat_list/flat_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/button/circle_button.dart';
import 'package:stringee_flutter_plugin_example/button/rounded_button.dart';

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
  late StringeeVideoRoom? _room;
  late StringeeVideoTrack _localTrack;
  late StringeeVideoView _localTrackView;
  List<StringeeVideoView> _remoteTrackViews = [];
  Map<String, StringeeVideoTrack> _remoteTracks = {};

  bool _hasLocalView = false;
  bool _isMute = false;
  bool _isVideoEnable = true;

  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    widget._video.joinRoom(widget._roomToken).then((value) {
      print(widget._roomToken);
      if (value['status']) {
        _room = value['body']['room'];
        initRoom(value['body']['videoTrackInfos'], value['body']['users']);
      } else {
        clearDataEndDismiss();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget _localView = (_hasLocalView)
        ? _localTrackView
        : Placeholder(
            color: Colors.transparent,
          );

    Container _bottomContainer = new Container(
      padding: EdgeInsets.only(bottom: 30.0),
      alignment: Alignment.bottomCenter,
      child: new Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          CircleButton(
              icon: Icon(
                Icons.switch_camera,
                color: Colors.white,
                size: 28,
              ),
              primary: Colors.white54,
              onPressed: toggleSwitchCamera),
          CircleButton(
              icon: _isMute
                  ? Icon(
                      Icons.mic,
                      color: Colors.black,
                      size: 28,
                    )
                  : Icon(
                      Icons.mic_off,
                      color: Colors.white,
                      size: 28,
                    ),
              primary: _isMute ? Colors.white : Colors.white54,
              onPressed: toggleMicro),
          CircleButton(
              icon: _isVideoEnable
                  ? Icon(
                      Icons.videocam_off,
                      color: Colors.white,
                      size: 28,
                    )
                  : Icon(
                      Icons.videocam,
                      color: Colors.black,
                      size: 28,
                    ),
              primary: _isVideoEnable ? Colors.white54 : Colors.white,
              onPressed: toggleVideo),
        ],
      ),
    );

    Widget _btnLeaveRoom = Align(
      alignment: Alignment.topRight,
      child: Container(
        margin: EdgeInsets.only(
          top: 40.0,
          right: 20.0,
        ),
        child: RoundedButton(
            icon: Icon(
              Icons.call_end,
              color: Colors.white,
              size: 28,
            ),
            color: Colors.red,
            radius: 10.0,
            onPressed: leaveRoomTapped),
      ),
    );

    Widget _participantView = Align(
      alignment: Alignment.bottomCenter,
      child: Container(
        height: 200.0,
        margin: EdgeInsets.only(bottom: 100.0),
        child: FlatList(
          data: _remoteTrackViews,
          horizontal: true,
          buildItem: (item, index) {
            return _remoteTrackViews[index];
          },
        ),
      ),
    );
    return WillPopScope(
        child: Scaffold(
          body: Stack(
            children: [
              _localView,
              _participantView,
              _btnLeaveRoom,
              _bottomContainer,
            ],
          ),
        ),
        onWillPop: () {
          leaveRoomTapped();
          return Future.value(false);
        });
  }

  void initRoom(List<StringeeVideoTrackInfo> videoTrackInfos,
      List<StringeeRoomUser> userList) {
    _room!.eventStreamController.stream.listen((event) {
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
        case StringeeRoomEvents.trackReadyToPlay:
          handleTrackReadyToPlayEvent(map['body']);
          break;
        default:
          break;
      }
    });

    StringeeVideoTrackOption options = StringeeVideoTrackOption(
      audio: true,
      video: true,
      screen: false,
    );
    widget._video.createLocalVideoTrack(options).then((value) {
      if (value['status']) {
        _room!.publish(value['body']).then((value) {
          if (value['status']) {
            setState(() {
              _localTrack = value['body'];
            });
          }
        });
      }
    });

    if (videoTrackInfos.length > 0) {
      videoTrackInfos.forEach((trackInfo) {
        StringeeVideoTrackOption options = StringeeVideoTrackOption(
          audio: trackInfo.audioEnable!,
          video: trackInfo.videoEnable!,
          screen: trackInfo.isScreenCapture!,
        );
        _room!.subscribe(trackInfo, options).then((value) {
          if (value['status']) {
            setState(() {
              StringeeVideoTrack videoTrack = value['body'];
              _remoteTracks[videoTrack.id] = videoTrack;
            });
          }
        });
      });
    }
  }

  void handleJoinRoomEvent(StringeeRoomUser joinUser) {}

  void handleLeaveRoomEvent(StringeeRoomUser leaveUser) {}

  void handleAddVideoTrackEvent(StringeeVideoTrackInfo trackInfo) {
    print("handleAddVideoTrackEvent - ${trackInfo.id}");
    StringeeVideoTrackOption options = StringeeVideoTrackOption(
      audio: trackInfo.audioEnable!,
      video: trackInfo.videoEnable!,
      screen: trackInfo.isScreenCapture!,
    );
    _room!.subscribe(trackInfo, options).then((value) {
      if (value['status']) {
        setState(() {
          print("subscribe - ${trackInfo.id}");
          StringeeVideoTrack videoTrack = value['body'];
          _remoteTracks[videoTrack.id] = videoTrack;
        });
      }
    });
  }

  handleRemoveVideoTrackEvent(StringeeVideoTrackInfo trackInfo) {
    print("handleRemoveVideoTrackEvent - ${trackInfo.id}");
    setState(() {
      _remoteTracks.remove(trackInfo.id);
    });
    for (int i = 0; i < _remoteTrackViews.length; i++) {
      if (_remoteTrackViews[i].trackId! == trackInfo.id) print("remove - ${i}");
      setState(() {
        _remoteTrackViews.removeAt(i);
      });
    }
  }

  void handleReceiveRoomMessageEvent(Map<dynamic, dynamic> bodyMap) {}

  void handleTrackReadyToPlayEvent(StringeeVideoTrack track) {
    print("handleTrackReadyToPlayEvent - ${track.id}");
    if (track.isLocal) {
      if (track.isScreenCapture) {
        StringeeVideoView videoView = track.attach(
          key: Key(track.id),
          height: 200.0,
          width: 150.0,
          scalingType: ScalingType.fit,
        );

        setState(() {
          _remoteTrackViews.add(videoView);
        });
      } else {
        setState(() {
          _hasLocalView = true;
          _localTrackView = track.attach(
            key: Key(track.id),
            alignment: Alignment.center,
            scalingType: ScalingType.fit,
          );
        });
      }
    } else {
      StringeeVideoView videoView = track.attach(
        key: Key(track.id),
        height: 200.0,
        width: 150.0,
        scalingType: ScalingType.fit,
      );

      setState(() {
        _remoteTrackViews.add(videoView);
      });
    }
  }

  void createForegroundServiceNotification() {
    flutterLocalNotificationsPlugin.initialize(InitializationSettings(
      android: AndroidInitializationSettings('ic_launcher'),
    ));

    flutterLocalNotificationsPlugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>()
        ?.startForegroundService(
          1,
          'Screen capture',
          'Capturing',
          notificationDetails: AndroidNotificationDetails(
            'Test id',
            'Test name',
            channelDescription: 'Test description',
            importance: Importance.defaultImportance,
            priority: Priority.defaultPriority,
          ),
        );
  }

  void toggleSwitchCamera() {
    _localTrack.switchCamera().then((result) {
      bool status = result['status'];
      if (status) {}
    });
  }

  void toggleMicro() {
    _localTrack.mute(!_isMute).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isMute = !_isMute;
        });
      }
    });
  }

  void toggleVideo() {
    _localTrack.enableVideo(!_isVideoEnable).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isVideoEnable = !_isVideoEnable;
        });
      }
    });
  }

  void leaveRoomTapped() {
    _room!.leave(allClient: false).then((result) {
      if (result['status']) {
        clearDataEndDismiss();
      }
    });
  }

  void clearDataEndDismiss() {
    if (_room != null) {
      _room!.destroy();
    }
    Navigator.pop(context);
  }
}
