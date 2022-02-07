import 'dart:io';

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
  late StringeeVideoRoom _room;
  late StringeeVideoTrack _localTrack;
  late StringeeVideoView _localTrackView;
  List<StringeeVideoTrack> _remoteTracks = [];
  List<StringeeVideoView> _remoteTrackViews = [];

  late StringeeVideoTrack _shareTrack;

  bool _hasLocalView = false;
  bool _sharingScreen = false;
  bool _isMute = false;
  bool _isVideoEnable = true;
  int _cameraId = 1;

  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    widget._video.joinRoom(widget._roomToken).then((value) {
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
          CircleButton(
              icon: _sharingScreen
                  ? Icon(
                      Icons.stop_screen_share,
                      color: Colors.black,
                      size: 28,
                    )
                  : Icon(
                      Icons.screen_share,
                      color: Colors.white,
                      size: 28,
                    ),
              primary: _sharingScreen ? Colors.white : Colors.white54,
              onPressed: toggleShareScreen),
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
        child: ListView.builder(
          scrollDirection: Axis.horizontal,
          itemCount: _remoteTrackViews.length,
          itemBuilder: (context, index) {
            // return _trackList[index].attach(
            //   isOverlay: true,
            //   height: 200.0,
            //   width: 150.0,
            //   scalingType: ScalingType.fit,
            // );
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
        _room.publish(value['body']).then((value) {
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
          audio: trackInfo.audioEnable,
          video: trackInfo.videoEnable,
          screen: trackInfo.isScreenCapture,
        );
        _room.subscribe(trackInfo, options).then((value) {
          if (value['status']) {
            setState(() {
              StringeeVideoTrack videoTrack = value['body'];
              _remoteTracks.add(videoTrack);
            });
          }
        });
      });
    }
  }

  void handleJoinRoomEvent(StringeeRoomUser joinUser) {}

  void handleLeaveRoomEvent(StringeeRoomUser leaveUser) {}

  void handleAddVideoTrackEvent(StringeeVideoTrackInfo trackInfo) {
    StringeeVideoTrackOption options = StringeeVideoTrackOption(
      audio: trackInfo.audioEnable,
      video: trackInfo.videoEnable,
      screen: trackInfo.isScreenCapture,
    );
    _room.subscribe(trackInfo, options).then((value) {
      if (value['status']) {
        setState(() {
          StringeeVideoTrack videoTrack = value['body'];
          _remoteTracks.add(videoTrack);
        });
      }
    });
  }

  void handleRemoveVideoTrackEvent(StringeeVideoTrackInfo trackInfo) {
    setState(() {
      if (_remoteTracks.length > 0) {
        for (int i = 0; i < _remoteTracks.length; i++) {
          StringeeVideoTrack track = _remoteTracks[i];
          if (track.id == trackInfo.id) {
            _remoteTracks.removeAt(i);
            _remoteTrackViews.removeAt(i);
          }
        }
      }
    });
  }

  void handleReceiveRoomMessageEvent(Map<dynamic, dynamic> bodyMap) {}

  void handleTrackReadyToPlayEvent(StringeeVideoTrack track) {
    print("handleTrackReadyToPlayEvent");
    if (track.isLocal) {
      if (track.isScreenCapture) {
        StringeeVideoView videoView = track.attach(
          isOverlay: true,
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
            alignment: Alignment.center,
            scalingType: ScalingType.fit,
          );
        });
      }
    } else {
      StringeeVideoView videoView = track.attach(
        isOverlay: true,
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
            'Test description',
            importance: Importance.defaultImportance,
            priority: Priority.defaultPriority,
          ),
        );
  }

  void toggleShareScreen() {
    if (Platform.isAndroid) {
      if (_sharingScreen) {
        // remove foreground service notification
        flutterLocalNotificationsPlugin
            .resolvePlatformSpecificImplementation<
                AndroidFlutterLocalNotificationsPlugin>()
            ?.stopForegroundService();

        _room.unpublish(_shareTrack).then((result) {
          if (result['status']) {
            setState(() {
              _sharingScreen = false;
              if (_remoteTracks.length > 0) {
                for (int i = 0; i < _remoteTracks.length; i++) {
                  StringeeVideoTrack track = _remoteTracks[i];
                  if (track.localId == _shareTrack.localId) {
                    _remoteTracks.removeAt(i);
                    _remoteTrackViews.removeAt(i);
                  }
                }
              }
            });
          }
        });
      } else {
        createForegroundServiceNotification();
        widget._video.createCaptureScreenTrack().then((result) {
          if (result['status']) {
            _room.publish(result['body']).then((result) {
              if (result['status']) {
                setState(() {
                  _sharingScreen = true;
                  _shareTrack = result['body'];
                  _remoteTracks.add(_shareTrack);
                });
              }
            });
          }
        });
      }
    }
  }

  void toggleSwitchCamera() {
    setState(() {
      _cameraId = _cameraId == 1 ? 0 : 1;
    });
    _localTrack.switchCamera(cameraId: _cameraId).then((result) {
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
    _room.leave(allClient: false).then((result) {
      if (result['status']) {
        if (_sharingScreen) {
          createForegroundServiceNotification();
        }
        clearDataEndDismiss();
      }
    });
  }

  void clearDataEndDismiss() {
    if (_room != null) {
      _room.destroy();
    }
    Navigator.pop(context);
  }
}
