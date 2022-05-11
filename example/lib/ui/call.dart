import 'dart:io' show Platform;

import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/button/circle_button.dart';

class Call extends StatefulWidget {
  late StringeeClient _client;
  late StringeeCall? _stringeeCall;
  late StringeeCall2? _stringeeCall2;
  late String _toUserId;
  late String _fromUserId;
  late String _callId;
  late StringeeObjectEventType _callType;
  bool _showIncomingUi = false;
  bool _isVideoCall = false;

  Call(
    StringeeClient client,
    String fromUserId,
    String toUserId,
    bool showIncomingUi,
    bool isVideoCall,
    StringeeObjectEventType callType, {
    Key? key,
    StringeeCall2? stringeeCall2,
    StringeeCall? stringeeCall,
  }) : super(key: key) {
    _client = client;
    _fromUserId = fromUserId;
    _toUserId = toUserId;
    _showIncomingUi = showIncomingUi;
    _isVideoCall = isVideoCall;
    _callType = callType;
    if (stringeeCall2 != null) {
      _stringeeCall2 = stringeeCall2;
    }
    if (stringeeCall != null) {
      _stringeeCall = stringeeCall;
    }
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _CallState();
  }
}

class _CallState extends State<Call> {
  String status = "";
  bool _isSpeaker = false;
  bool _isMute = false;
  bool _isVideoEnable = false;
  bool _sharingScreen = false;
  bool _hasLocalStream = false;
  bool _hasRemoteStream = false;

  bool _hasLocalScreen = false;
  late StringeeVideoTrack _localScreenTrack;
  bool _hasRemoteScreen = false;
  late StringeeVideoTrack _remoteScreenTrack;

  int _cameraId = 1;
  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    _isSpeaker = widget._isVideoCall;
    _isVideoEnable = widget._isVideoCall;
    
    if (widget._callType == StringeeObjectEventType.call) {
      makeOrInitAnswerCall();
    } else {
      makeOrInitAnswerCall2();
    }
  }

  @override
  Widget build(BuildContext context) {
    Widget nameCalling = new Container(
      alignment: Alignment.topCenter,
      padding: EdgeInsets.only(top: 120.0),
      child: new Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          new Container(
            alignment: Alignment.center,
            padding: EdgeInsets.only(bottom: 15.0),
            child: new Text(
              "${widget._toUserId}",
              style: new TextStyle(
                color: Colors.white,
                fontSize: 35.0,
              ),
            ),
          ),
          new Container(
            alignment: Alignment.center,
            child: new Text(
              '$status',
              style: new TextStyle(
                color: Colors.white,
                fontSize: 20.0,
              ),
            ),
          )
        ],
      ),
    );

    Widget localView = (_hasLocalStream)
        ? new StringeeVideoView(
            widget._callId,
            true,
            color: Colors.white,
            alignment: Alignment.topRight,
            isOverlay: true,
            margin: EdgeInsets.only(top: 25.0, right: 25.0),
            height: 150.0,
            width: 100.0,
            scalingType: ScalingType.fill,
          )
        : Placeholder(
            color: Colors.transparent,
          );

    Widget remoteView = (_hasRemoteStream)
        ? new StringeeVideoView(
            widget._callId,
            false,
            color: Colors.blue,
            isOverlay: false,
            isMirror: false,
            scalingType: ScalingType.fill,
          )
        : Placeholder(
            color: Colors.transparent,
          );

    Widget localScreen = (_hasLocalScreen)
        ? _localScreenTrack.attach(
            color: Colors.white,
            alignment: Alignment.topRight,
            isOverlay: true,
            margin: EdgeInsets.only(top: 200.0, right: 25.0),
            height: 150.0,
            width: 100.0,
            scalingType: ScalingType.fill,
          )
        : Placeholder(
            color: Colors.transparent,
          );

    Widget remoteScreen = (_hasRemoteScreen)
        ? _remoteScreenTrack.attach(
            color: Colors.white,
            alignment: Alignment.topRight,
            isOverlay: true,
            margin: EdgeInsets.only(top: 375.0, right: 25.0),
            height: 150.0,
            width: 100.0,
            scalingType: ScalingType.fill,
          )
        : Placeholder(
            color: Colors.transparent,
          );

    Widget btnSwitch = Align(
      alignment: Alignment.topLeft,
      child: Padding(
        padding: EdgeInsets.only(left: 25.0, top: 25.0),
        child: CircleButton(
            icon: Icon(
              Icons.switch_camera,
              color: Colors.white,
              size: 28,
            ),
            primary: Colors.transparent,
            onPressed: toggleSwitchCamera),
      ),
    );

    Container bottomContainer = new Container(
      padding: EdgeInsets.only(bottom: 30.0),
      alignment: Alignment.bottomCenter,
      child: new Column(
          mainAxisAlignment: MainAxisAlignment.end,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: widget._showIncomingUi
              ? <Widget>[
                  new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: <Widget>[
                      CircleButton(
                          icon: Icon(
                            Icons.call_end,
                            color: Colors.white,
                            size: 28,
                          ),
                          primary: Colors.red,
                          onPressed: rejectCallTapped),
                      CircleButton(
                          icon: Icon(
                            Icons.call,
                            color: Colors.white,
                            size: 28,
                          ),
                          primary: Colors.green,
                          onPressed: acceptCallTapped),
                    ],
                  )
                ]
              : <Widget>[
                  new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: <Widget>[
                      CircleButton(
                          icon: _isSpeaker
                              ? Icon(
                                  Icons.volume_off,
                                  color: Colors.black,
                                  size: 28,
                                )
                              : Icon(
                                  Icons.volume_up,
                                  color: Colors.white,
                                  size: 28,
                                ),
                          primary: _isSpeaker ? Colors.white : Colors.white54,
                          onPressed: toggleSpeaker),
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
                          primary:
                              _isVideoEnable ? Colors.white54 : Colors.white,
                          onPressed: toggleVideo),
                      if (widget._isVideoCall &&
                          widget._callType == StringeeObjectEventType.call2)
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
                            primary:
                                _sharingScreen ? Colors.white : Colors.white54,
                            onPressed: toggleShareScreen),
                      CircleButton(
                          icon: Icon(
                            Icons.call_end,
                            color: Colors.white,
                            size: 28,
                          ),
                          primary: Colors.red,
                          onPressed: endCallTapped),
                    ],
                  ),
                ]),
    );

    return WillPopScope(
      child: Scaffold(
        backgroundColor: Colors.black,
        body: new Stack(
          children: <Widget>[
            remoteView,
            localView,
            localScreen,
            remoteScreen,
            nameCalling,
            bottomContainer,
            btnSwitch,
          ],
        ),
      ),
      onWillPop: () {
        endCallTapped();
        return Future.value(false);
      },
    );
  }

  Future makeOrInitAnswerCall() async {
    if (!widget._showIncomingUi) {
      widget._stringeeCall = StringeeCall(widget._client);
    }

    // Listen events
    widget._stringeeCall!.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      print("Call " + map.toString());
      switch (map['eventType']) {
        case StringeeCallEvents.didChangeSignalingState:
          handleSignalingStateChangeEvent(map['body']);
          break;
        case StringeeCallEvents.didChangeMediaState:
          handleMediaStateChangeEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveCallInfo:
          handleReceiveCallInfoEvent(map['body']);
          break;
        case StringeeCallEvents.didHandleOnAnotherDevice:
          handleHandleOnAnotherDeviceEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveLocalStream:
          handleReceiveLocalStreamEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveRemoteStream:
          handleReceiveRemoteStreamEvent(map['body']);
          break;
        case StringeeCallEvents.didChangeAudioDevice:
          if (Platform.isAndroid) {
            handleChangeAudioDeviceEvent(map['selectedAudioDevice']);
          }
          break;
        default:
          break;
      }
    });

    if (widget._showIncomingUi) {
      widget._stringeeCall!.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      final parameters = {
        'from': widget._fromUserId,
        'to': widget._toUserId,
        'isVideoCall': widget._isVideoCall,
        'customData': null,
        'videoQuality': VideoQuality.fullHd,
      };

      widget._stringeeCall!.makeCall(parameters).then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        print(
            'MakeCall CallBack --- $status - $code - $message - ${widget._stringeeCall!.id} - ${widget._stringeeCall!.from} - ${widget._stringeeCall!.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  Future makeOrInitAnswerCall2() async {
    if (!widget._showIncomingUi) {
      widget._stringeeCall2 = StringeeCall2(widget._client);
    }

    // Listen events
    widget._stringeeCall2!.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      switch (map['eventType']) {
        case StringeeCall2Events.didChangeSignalingState:
          handleSignalingStateChangeEvent(map['body']);
          break;
        case StringeeCall2Events.didChangeMediaState:
          handleMediaStateChangeEvent(map['body']);
          break;
        case StringeeCall2Events.didReceiveCallInfo:
          handleReceiveCallInfoEvent(map['body']);
          break;
        case StringeeCall2Events.didHandleOnAnotherDevice:
          handleHandleOnAnotherDeviceEvent(map['body']);
          break;
        case StringeeCall2Events.didReceiveLocalStream:
          handleReceiveLocalStreamEvent(map['body']);
          break;
        case StringeeCall2Events.didReceiveRemoteStream:
          handleReceiveRemoteStreamEvent(map['body']);
          break;
        case StringeeCall2Events.didAddVideoTrack:
          handleAddVideoTrackEvent(map['body']);
          break;
        case StringeeCall2Events.didRemoveVideoTrack:
          handleRemoveVideoTrackEvent(map['body']);
          break;
        case StringeeCall2Events.didChangeAudioDevice:
          if (Platform.isAndroid) {
            handleChangeAudioDeviceEvent(map['selectedAudioDevice']);
          }
          break;
        default:
          break;
      }
    });

    if (widget._showIncomingUi) {
      widget._stringeeCall2!.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      final parameters = {
        'from': widget._fromUserId,
        'to': widget._toUserId,
        'isVideoCall': widget._isVideoCall,
        'customData': null,
        'videoQuality': VideoQuality.fullHd,
      };

      widget._stringeeCall2!.makeCall(parameters).then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        print(
            'MakeCall CallBack --- $status - $code - $message - ${widget._stringeeCall2!.id} - ${widget._stringeeCall2!.from} - ${widget._stringeeCall2!.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  void endCallTapped() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.hangup().then((result) {
        print('_endCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (status) {
          if (Platform.isAndroid) {
            clearDataEndDismiss();
          }
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.hangup().then((result) {
        print('_endCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (status) {
          if (Platform.isAndroid) {
            clearDataEndDismiss();
          }
        }
      });
    }
  }

  void acceptCallTapped() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.answer().then((result) {
        print('_acceptCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.answer().then((result) {
        print('_acceptCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    }
    setState(() {
      widget._showIncomingUi = !widget._showIncomingUi;
    });
  }

  void rejectCallTapped() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.reject().then((result) {
        print('_rejectCallTapped -- ${result['message']}');
        if (Platform.isAndroid) {
          clearDataEndDismiss();
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.reject().then((result) {
        print('_rejectCallTapped -- ${result['message']}');
        if (Platform.isAndroid) {
          clearDataEndDismiss();
        }
      });
    }
  }

  void handleSignalingStateChangeEvent(StringeeSignalingState state) {
    print('handleSignalingStateChangeEvent - $state');
    setState(() {
      status = state.toString().split('.')[1];
    });
    switch (state) {
      case StringeeSignalingState.calling:
        break;
      case StringeeSignalingState.ringing:
        break;
      case StringeeSignalingState.answered:
        break;
      case StringeeSignalingState.busy:
        clearDataEndDismiss();
        break;
      case StringeeSignalingState.ended:
        clearDataEndDismiss();
        break;
      default:
        break;
    }
  }

  void handleMediaStateChangeEvent(StringeeMediaState state) {
    print('handleMediaStateChangeEvent - $state');
    setState(() {
      status = state.toString().split('.')[1];
    });
    switch (state) {
      case StringeeMediaState.connected:
        if (widget._callType == StringeeObjectEventType.call) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
        } else if (widget._callType == StringeeObjectEventType.call2) {
          widget._stringeeCall2!.setSpeakerphoneOn(_isSpeaker);
        }
        break;
      case StringeeMediaState.disconnected:
        break;
      default:
        break;
    }
  }

  void handleReceiveCallInfoEvent(Map<dynamic, dynamic> info) {
    print('handleReceiveCallInfoEvent - $info');
  }

  void handleHandleOnAnotherDeviceEvent(StringeeSignalingState state) {
    print('handleHandleOnAnotherDeviceEvent - $state');
    if (state == StringeeSignalingState.answered ||
        state == StringeeSignalingState.ended ||
        state == StringeeSignalingState.busy) {
      clearDataEndDismiss();
    }
  }

  void handleReceiveLocalStreamEvent(String callId) {
    print('handleReceiveLocalStreamEvent - $callId');
    setState(() {
      _hasLocalStream = true;
      widget._callId = callId;
    });
  }

  void handleReceiveRemoteStreamEvent(String callId) {
    print('handleReceiveRemoteStreamEvent - $callId');
    if (_hasRemoteStream) {
      setState(() {
        _hasRemoteStream = false;
        widget._callId = callId;
      });

      Future.delayed(Duration(milliseconds: 100), () {
        setState(() {
          _hasRemoteStream = true;
          widget._callId = callId;
        });
      });

    } else {
      setState(() {
        _hasRemoteStream = true;
        widget._callId = callId;
      });
    }
  }

  void handleAddVideoTrackEvent(StringeeVideoTrack track) {
    print('handleAddVideoTrackEvent - ${track.id}');
    setState(() {
      if (track.isLocal) {
        _hasLocalScreen = true;
        _localScreenTrack = track;
      } else {
        _hasRemoteScreen = true;
        _remoteScreenTrack = track;
      }
    });
  }

  void handleRemoveVideoTrackEvent(StringeeVideoTrack track) {
    print('handleRemoveVideoTrackEvent - ${track.id}');
    setState(() {
      if (track.isLocal) {
        _hasLocalScreen = false;
      } else {
        _hasRemoteScreen = false;
      }
    });
  }

  void handleChangeAudioDeviceEvent(AudioDevice audioDevice) {
    print('handleChangeAudioDeviceEvent - $audioDevice');
    switch (audioDevice) {
      case AudioDevice.speakerPhone:
      case AudioDevice.earpiece:
        if (widget._callType == StringeeObjectEventType.call) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
        } else if (widget._callType == StringeeObjectEventType.call2) {
          widget._stringeeCall2!.setSpeakerphoneOn(_isSpeaker);
        }
        break;
      case AudioDevice.bluetooth:
      case AudioDevice.wiredHeadset:
        setState(() {
          _isSpeaker = false;
        });
        if (widget._callType == StringeeObjectEventType.call) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
        } else if (widget._callType == StringeeObjectEventType.call2) {
          widget._stringeeCall2!.setSpeakerphoneOn(_isSpeaker);
        }
        break;
      case AudioDevice.none:
        print('handleChangeAudioDeviceEvent - non audio devices connected');
        break;
    }
  }

  void clearDataEndDismiss() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.destroy();
      widget._stringeeCall = null;
      Navigator.pop(context);
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.destroy();
      widget._stringeeCall2 = null;
      Navigator.pop(context);
    } else {
      Navigator.pop(context);
    }
  }

  void toggleSwitchCamera() {
    setState(() {
      _cameraId = _cameraId == 1 ? 0 : 1;
    });
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.switchCamera(cameraId: _cameraId).then((result) {
        bool status = result['status'];
        if (status) {}
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.switchCamera(cameraId: _cameraId).then((result) {
        bool status = result['status'];
        if (status) {}
      });
    }
  }

  void toggleSpeaker() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.setSpeakerphoneOn(!_isSpeaker).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isSpeaker = !_isSpeaker;
          });
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.setSpeakerphoneOn(!_isSpeaker).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isSpeaker = !_isSpeaker;
          });
        }
      });
    }
  }

  void toggleMicro() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.mute(!_isMute).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isMute = !_isMute;
          });
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.mute(!_isMute).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isMute = !_isMute;
          });
        }
      });
    }
  }

  void toggleVideo() {
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.enableVideo(!_isVideoEnable).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isVideoEnable = !_isVideoEnable;
          });
        }
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.enableVideo(!_isVideoEnable).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isVideoEnable = !_isVideoEnable;
          });
        }
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
    if (_sharingScreen) {
      // remove foreground service notification
      flutterLocalNotificationsPlugin
          .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>()
          ?.stopForegroundService();

      widget._stringeeCall2!.stopCapture().then((result) {
        bool status = result['status'];
        print('flutter stopCapture: $status');
        if (status) {
          setState(() {
            _sharingScreen = false;
          });
        }
      });
    } else {
      createForegroundServiceNotification();
      widget._stringeeCall2!.startCapture().then((result) {
        bool status = result['status'];
        print('flutter startCapture: $status');
        if (status) {
          setState(() {
            _sharingScreen = true;
          });
        }
      });
    }
  }
}
