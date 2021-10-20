import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/button/circle_button.dart';
import 'package:stringee_flutter_plugin_example/button/rounded_button.dart';

class Room extends StatefulWidget {
   StringeeVideo _video;
   String _roomToken;

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
   StringeeRoom _room;
   StringeeVideoTrack _localTrack;
   StringeeVideoTrack _shareTrack;
   StringeeVideoView _localTrackView;
  bool _hasLocalView = false;
  bool _sharingScreen = false;
  bool _isMute = false;
  bool _isVideoEnable = true;
  int _cameraId = 1;
  List<StringeeVideoTrack> _trackList = [];
  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

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
          top: 20.0,
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
        default:
          break;
      }
    });

    StringeeVideoTrackOptions options = StringeeVideoTrackOptions(
      audio: true,
      video: true,
      screen: false,
    );
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
          audio: track.audioEnable,
          video: track.videoEnable,
          screen: track.isScreenCapture,
        );
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
      audio: addTrack.audioEnable,
      video: addTrack.videoEnable,
      screen: addTrack.isScreenCapture,
    );
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
            for (int i = 0; i < _trackList.length; i++) {
              StringeeVideoTrack track = _trackList[i];
              if (track.id == removeTrack.id) {
                _trackList.removeAt(i);
              }
            }
          }
        });
      }
    });
  }

  void handleReceiveRoomMessageEvent(Map<dynamic, dynamic> bodyMap) {}

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
    if (_sharingScreen) {
      // remove foreground service notification
      flutterLocalNotificationsPlugin
          .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>()
          ?.stopForegroundService();

      _room.unpublish(_shareTrack).then((result) {
        if (result['status']) {
          _shareTrack.close().then((value) {
            if (result['status']) {
              setState(() {
                _sharingScreen = false;
              });
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
                _trackList.add(_shareTrack);
              });
            }
          });
        }
      });
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
    _room.unpublish(_localTrack).then((result) {
      if (result['status']) {
        _localTrack.close().then((value) {
          if (result['status']) {
            _room.leave(allClient: false).then((value) {
              clearDataEndDismiss();
            });
          }
        });
      }
    });
  }

  void clearDataEndDismiss() {
    _room.destroy();
    Navigator.pop(context);
  }
}
