import 'dart:io' show Platform;

import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:stringee_flutter_plugin_example/button/circle_button.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class Call extends StatefulWidget {
  late StringeeClient _client;
  late StringeeCall? _stringeeCall;
  late StringeeCall2? _stringeeCall2;
  late String _toUserId;
  late String _fromUserId;
  late StringeeObjectEventType _callType;
  late StringeeAudioManager _audioManager;
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
    _audioManager = StringeeAudioManager();
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _CallState();
  }
}

class _CallState extends State<Call> {
  String status = "";
  bool _isMute = false;
  bool _isVideoEnable = false;

  Widget? localScreen = null;
  Widget? remoteScreen = null;
  bool _initializingAudio = true;
  AudioDevice _preAudioDevice = AudioDevice(audioType: AudioType.earpiece);
  AudioDevice _audioDevice = AudioDevice(audioType: AudioType.earpiece);
  List<AudioDevice> _availableAudioDevices = [];

  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  StringeeAudioEvent? _event;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    _isVideoEnable = widget._isVideoCall;
    _event = StringeeAudioEvent(
      onChangeAudioDevice: (selectedAudioDevice, availableAudioDevices) {
        print('onChangeAudioDevice - $selectedAudioDevice');
        print('onChangeAudioDevice - $availableAudioDevices');
        _availableAudioDevices = availableAudioDevices;
        if (_initializingAudio) {
          _initializingAudio = false;
          int bluetoothIndex = -1;
          int wiredHeadsetIndex = -1;
          int speakerIndex = -1;
          int earpieceIndex = -1;
          availableAudioDevices.forEach((element) {
            if (element.audioType == AudioType.bluetooth) {
              bluetoothIndex = availableAudioDevices.indexOf(element);
            }
            if (element.audioType == AudioType.wiredHeadset) {
              wiredHeadsetIndex = availableAudioDevices.indexOf(element);
            }
            if (element.audioType == AudioType.speakerPhone) {
              speakerIndex = availableAudioDevices.indexOf(element);
            }
            if (element.audioType == AudioType.earpiece) {
              earpieceIndex = availableAudioDevices.indexOf(element);
            }
          });
          _preAudioDevice = availableAudioDevices.elementAt(0);
          if (widget._isVideoCall) {
            if (speakerIndex != -1) {
              _preAudioDevice = availableAudioDevices.elementAt(speakerIndex);
            }
          } else {
            if (earpieceIndex != -1) {
              _preAudioDevice = availableAudioDevices.elementAt(earpieceIndex);
            }
          }
          if (bluetoothIndex != -1) {
            selectedAudioDevice =
                availableAudioDevices.elementAt(bluetoothIndex);
          } else if (wiredHeadsetIndex != -1) {
            selectedAudioDevice =
                availableAudioDevices.elementAt(wiredHeadsetIndex);
          } else if (widget._isVideoCall) {
            if (speakerIndex != -1) {
              selectedAudioDevice =
                  availableAudioDevices.elementAt(speakerIndex);
            }
          } else {
            if (earpieceIndex != -1) {
              selectedAudioDevice =
                  availableAudioDevices.elementAt(earpieceIndex);
            }
          }
          changeAudioDevice(selectedAudioDevice);
        } else {
          switch (selectedAudioDevice.audioType) {
            case AudioType.wiredHeadset:
            case AudioType.bluetooth:
              if (_audioDevice.audioType != AudioType.bluetooth &&
                  _audioDevice.audioType != AudioType.wiredHeadset) {
                _preAudioDevice = _audioDevice;
              }
              changeAudioDevice(selectedAudioDevice);
              break;
            case AudioType.earpiece:
            case AudioType.speakerPhone:
              if (_audioDevice.audioType == AudioType.speakerPhone ||
                  _audioDevice.audioType == AudioType.earpiece) {
                changeAudioDevice(_audioDevice);
                return;
              }
              if (_preAudioDevice != selectedAudioDevice) {
                changeAudioDevice(_preAudioDevice);
              } else {
                changeAudioDevice(selectedAudioDevice);
              }
              break;
          }
        }
      },
    );
    widget._audioManager.addListener(_event!);
    widget._audioManager.start();

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

    Widget getBtnAudio() {
      IconData? icon = Icons.volume_down;
      Color? color = Colors.black;
      Color? primary = Colors.white;
      switch (_audioDevice.audioType) {
        case AudioType.speakerPhone:
          icon = Icons.volume_up;
          color = Colors.white;
          primary = Colors.white54;
          break;
        case AudioType.wiredHeadset:
          icon = Icons.headphones;
          color = Colors.black;
          primary = Colors.white;
          break;
        case AudioType.earpiece:
          icon = Icons.volume_down;
          color = Colors.black;
          primary = Colors.white;
          break;
        case AudioType.bluetooth:
          icon = Icons.bluetooth;
          color = Colors.black;
          primary = Colors.white;
          break;
      }
      return CircleButton(
          icon: Icon(
            icon,
            color: color,
            size: 28,
          ),
          primary: primary,
          onPressed: toggleSpeaker);
    }

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
                      getBtnAudio(),
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
            remoteScreen != null
                ? remoteScreen!
                : Placeholder(
                    color: Colors.transparent,
                  ),
            localScreen != null
                ? localScreen!
                : Placeholder(
                    color: Colors.transparent,
                  ),
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
      print('stringeeCallId: ${widget._stringeeCall!.id}');
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
      print('stringeeCall2Id: ${widget._stringeeCall2!.id}');
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
    if (localScreen != null) {
      setState(() {
        localScreen = null;
      });
      Future.delayed(Duration(milliseconds: 200), () {
        setState(() {
          localScreen = new StringeeVideoView(
            callId,
            true,
            alignment: Alignment.topRight,
            margin: EdgeInsets.only(top: 25.0, right: 25.0),
            height: 150.0,
            width: 100.0,
            scalingType: ScalingType.fit,
          );
        });
      });
    } else {
      setState(() {
        localScreen = new StringeeVideoView(
          callId,
          true,
          alignment: Alignment.topRight,
          margin: EdgeInsets.only(top: 25.0, right: 25.0),
          height: 150.0,
          width: 100.0,
          scalingType: ScalingType.fit,
        );
      });
    }
  }

  void handleReceiveRemoteStreamEvent(String callId) {
    print('handleReceiveRemoteStreamEvent - $callId');
    if (remoteScreen != null) {
      setState(() {
        remoteScreen = null;
      });
      Future.delayed(Duration(milliseconds: 200), () {
        setState(() {
          remoteScreen = new StringeeVideoView(
            callId,
            false,
            isMirror: false,
            scalingType: ScalingType.fit,
          );
        });
      });
    } else {
      setState(() {
        remoteScreen = new StringeeVideoView(
          callId,
          false,
          isMirror: false,
          scalingType: ScalingType.fit,
        );
      });
    }
  }

  void handleAddVideoTrackEvent(StringeeVideoTrack track) {
    print('handleAddVideoTrackEvent - ${track.id}');
    if (track.isLocal) {
      setState(() {
        localScreen = null;
      });
      Future.delayed(Duration(milliseconds: 200), () {
        setState(() {
          localScreen = track.attach(
            alignment: Alignment.topRight,
            margin: EdgeInsets.only(top: 25.0, right: 25.0),
            height: 150.0,
            width: 100.0,
            scalingType: ScalingType.fit,
          );
        });
      });
    } else {
      setState(() {
        remoteScreen = null;
      });
      Future.delayed(Duration(milliseconds: 200), () {
        setState(() {
          remoteScreen = track.attach(
            isMirror: false,
            scalingType: ScalingType.fit,
          );
        });
      });
    }
  }

  void handleRemoveVideoTrackEvent(StringeeVideoTrack track) {
    print('handleRemoveVideoTrackEvent - ${track.id}');
  }

  void clearDataEndDismiss() {
    widget._audioManager.removeListener(_event!);
    widget._audioManager.stop();
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
    if (widget._callType == StringeeObjectEventType.call) {
      widget._stringeeCall!.switchCamera().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    } else if (widget._callType == StringeeObjectEventType.call2) {
      widget._stringeeCall2!.switchCamera().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    }
  }

  void changeAudioDevice(AudioDevice device) {
    print('changeAudioDevice - $device');
    widget._audioManager.selectDevice(device).then((value) {
      print(value);
      if (value.status) {
        setState(() {
          _audioDevice = device;
        });
      }
    });
  }

  void toggleSpeaker() {
    if (_availableAudioDevices.length < 3) {
      if (_availableAudioDevices.length <= 1) {
        return;
      }
      int position = _availableAudioDevices.indexOf(_audioDevice);
      if (position == _availableAudioDevices.length - 1) {
        changeAudioDevice(_availableAudioDevices[0]);
      } else {
        changeAudioDevice(_availableAudioDevices[position + 1]);
      }
    } else {
      showModalBottomSheet(
        context: context,
        builder: (context) {
          return ListView.separated(
            itemCount: _availableAudioDevices.length,
            separatorBuilder: (context, index) {
              return Divider();
            },
            itemBuilder: (context, index) {
              return ListTile(
                title: Text(_availableAudioDevices[index].name!),
                onTap: () {
                  changeAudioDevice(_availableAudioDevices[index]);
                  Navigator.pop(context);
                },
              );
            },
          );
        },
      );
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
            channelDescription: 'Test description',
            importance: Importance.defaultImportance,
            priority: Priority.defaultPriority,
          ),
        );
  }
}
