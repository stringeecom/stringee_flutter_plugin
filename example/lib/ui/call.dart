import 'dart:io' show Platform;

import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/button/circle_button.dart';

class Call extends StatefulWidget {
  late StringeeClient _stringeeClient;
  late StringeeCall? _stringeeCall;
  late StringeeCall2? _stringeeCall2;
  late String _toUserId;
  late String _fromUserId;
  late bool _isStringeeCall;
  bool _showIncomingUi = false;
  bool _isVideoCall = false;

  Call(
    StringeeClient client,
    String fromUserId,
    String toUserId,
    bool showIncomingUi,
    bool isVideoCall,
    bool isStringeeCall, {
    Key? key,
    StringeeCall2? stringeeCall2,
    StringeeCall? stringeeCall,
  }) : super(key: key) {
    _stringeeClient = client;
    _fromUserId = fromUserId;
    _toUserId = toUserId;
    _showIncomingUi = showIncomingUi;
    _isVideoCall = isVideoCall;
    _isStringeeCall = isStringeeCall;
    if (stringeeCall2 != null) {
      _stringeeCall2 = stringeeCall2;
    }
    if (stringeeCall != null) {
      _stringeeCall = stringeeCall;
    }
  }

  @override
  State<StatefulWidget> createState() {
    return _CallState();
  }
}

class _CallState extends State<Call> {
  String status = "";
  bool _isSpeaker = false;
  bool _isMute = false;
  bool _isVideoEnable = false;

  Widget? localScreen = null;
  Widget? remoteScreen = null;

  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();

  @override
  void initState() {
    super.initState();

    _isSpeaker = widget._isVideoCall;
    _isVideoEnable = widget._isVideoCall;

    if (widget._isStringeeCall) {
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
      widget._stringeeCall = StringeeCall(
          widget._stringeeClient, widget._fromUserId, widget._toUserId);
      widget._stringeeCall!.isVideoCall = widget._isVideoCall;
      widget._stringeeCall!.videoQuality = VideoQuality.fullHd;
    }

    // Listen events
    widget._stringeeCall!.registerEvent(StringeeCallListener(
      onChangeSignalingState: (stringeeCall, signalingState) =>
          handleSignalingStateChangeEvent(signalingState),
      onChangeMediaState: (stringeeCall, mediaState) =>
          handleMediaStateChangeEvent(mediaState),
      onReceiveCallInfo: (stringeeCall, callInfo) =>
          handleReceiveCallInfoEvent(callInfo),
      onHandleOnAnotherDevice: (stringeeCall, signalingState) =>
          handleHandleOnAnotherDeviceEvent(signalingState),
      onReceiveLocalStream: (stringeeCall) =>
          handleReceiveLocalStreamEvent(stringeeCall.id!),
      onReceiveRemoteStream: (stringeeCall) =>
          handleReceiveRemoteStreamEvent(stringeeCall.id!),
      onChangeAudioDevice:
          (stringeeCall, selectedAudioDevice, availableAudioDevices) {
        if (Platform.isAndroid) {
          handleChangeAudioDeviceEvent(selectedAudioDevice);
        }
      },
    ));

    if (widget._showIncomingUi) {
      widget._stringeeCall!.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      widget._stringeeCall!.makeCall().then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        debugPrint(
            'MakeCall CallBack --- $status - $code - $message - ${widget._stringeeCall!.id} - ${widget._stringeeCall!.from} - ${widget._stringeeCall!.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  Future makeOrInitAnswerCall2() async {
    if (!widget._showIncomingUi) {
      widget._stringeeCall2 = StringeeCall2(
          widget._stringeeClient, widget._fromUserId, widget._toUserId);
      widget._stringeeCall!.isVideoCall = widget._isVideoCall;
      widget._stringeeCall!.videoQuality = VideoQuality.fullHd;
    }

    widget._stringeeCall2!.registerEvent(StringeeCall2Listener(
      onChangeSignalingState: (stringeeCall2, signalingState) =>
          handleSignalingStateChangeEvent(signalingState),
      onChangeMediaState: (stringeeCall2, mediaState) =>
          handleMediaStateChangeEvent(mediaState),
      onReceiveCallInfo: (stringeeCall2, callInfo) =>
          handleReceiveCallInfoEvent(callInfo),
      onHandleOnAnotherDevice: (stringeeCall2, signalingState) =>
          handleHandleOnAnotherDeviceEvent(signalingState),
      onReceiveLocalStream: (stringeeCall2) =>
          handleReceiveLocalStreamEvent(stringeeCall2.id!),
      onReceiveRemoteStream: (stringeeCall2) =>
          handleReceiveRemoteStreamEvent(stringeeCall2.id!),
      onChangeAudioDevice:
          (stringeeCall, selectedAudioDevice, availableAudioDevices) {
        if (Platform.isAndroid) {
          handleChangeAudioDeviceEvent(selectedAudioDevice);
        }
      },
      onAddVideoTrack: (stringeeCall2, videoTrack) =>
          handleAddVideoTrackEvent(videoTrack),
    ));

    if (widget._showIncomingUi) {
      widget._stringeeCall2!.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      widget._stringeeCall2!.makeCall().then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        debugPrint(
            'MakeCall CallBack --- $status - $code - $message - ${widget._stringeeCall2!.id} - ${widget._stringeeCall2!.from} - ${widget._stringeeCall2!.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  void endCallTapped() {
    if (widget._isStringeeCall) {
      widget._stringeeCall!.hangup().then((result) {
        debugPrint('_endCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (status) {
          if (Platform.isAndroid) {
            clearDataEndDismiss();
          }
        }
      });
    } else {
      widget._stringeeCall2!.hangup().then((result) {
        debugPrint('_endCallTapped -- ${result['message']}');
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
    if (widget._isStringeeCall) {
      widget._stringeeCall!.answer().then((result) {
        debugPrint('_acceptCallTapped -- ${result['message']}');
        bool status = result['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      widget._stringeeCall2!.answer().then((result) {
        debugPrint('_acceptCallTapped -- ${result['message']}');
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
    if (widget._isStringeeCall) {
      widget._stringeeCall!.reject().then((result) {
        debugPrint('_rejectCallTapped -- ${result['message']}');
        if (Platform.isAndroid) {
          clearDataEndDismiss();
        }
      });
    } else {
      widget._stringeeCall2!.reject().then((result) {
        debugPrint('_rejectCallTapped -- ${result['message']}');
        if (Platform.isAndroid) {
          clearDataEndDismiss();
        }
      });
    }
  }

  void handleSignalingStateChangeEvent(StringeeSignalingState state) {
    debugPrint('handleSignalingStateChangeEvent - $state');
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
    debugPrint('handleMediaStateChangeEvent - $state');
    setState(() {
      status = state.toString().split('.')[1];
    });
    switch (state) {
      case StringeeMediaState.connected:
        if (widget._isStringeeCall) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
        } else {
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
    debugPrint('handleReceiveCallInfoEvent - $info');
  }

  void handleHandleOnAnotherDeviceEvent(StringeeSignalingState state) {
    debugPrint('handleHandleOnAnotherDeviceEvent - $state');
    if (state == StringeeSignalingState.answered ||
        state == StringeeSignalingState.ended ||
        state == StringeeSignalingState.busy) {
      clearDataEndDismiss();
    }
  }

  void handleReceiveLocalStreamEvent(String callId) {
    debugPrint('handleReceiveLocalStreamEvent - $callId');
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
    debugPrint('handleReceiveRemoteStreamEvent - $callId');
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
    debugPrint('handleAddVideoTrackEvent - ${track.id}');
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
    debugPrint('handleRemoveVideoTrackEvent - ${track.id}');
  }

  void handleChangeAudioDeviceEvent(AudioDevice audioDevice) {
    debugPrint('handleChangeAudioDeviceEvent - $audioDevice');
    switch (audioDevice) {
      case AudioDevice.speakerPhone:
      case AudioDevice.earpiece:
        if (widget._isStringeeCall) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
          widget._stringeeCall!.setBluetoothScoOn(false);
        } else {
          widget._stringeeCall2!.setSpeakerphoneOn(_isSpeaker);
          widget._stringeeCall2!.setBluetoothScoOn(false);
        }
        break;
      case AudioDevice.wiredHeadset:
      case AudioDevice.bluetooth:
        setState(() {
          _isSpeaker = false;
        });
        if (widget._isStringeeCall) {
          widget._stringeeCall!.setSpeakerphoneOn(_isSpeaker);
          widget._stringeeCall!
              .setBluetoothScoOn(audioDevice == AudioDevice.bluetooth);
        } else {
          widget._stringeeCall2!.setSpeakerphoneOn(_isSpeaker);
          widget._stringeeCall!
              .setBluetoothScoOn(audioDevice == AudioDevice.bluetooth);
        }
        break;
      case AudioDevice.none:
        debugPrint('handleChangeAudioDeviceEvent - non audio devices connected');
        break;
    }
  }

  void clearDataEndDismiss() {
    Navigator.pop(context);
  }

  void toggleSwitchCamera() {
    if (widget._isStringeeCall) {
      widget._stringeeCall!.switchCamera().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    } else {
      widget._stringeeCall2!.switchCamera().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    }
  }

  void toggleSpeaker() {
    if (widget._isStringeeCall) {
      widget._stringeeCall!.setSpeakerphoneOn(!_isSpeaker).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isSpeaker = !_isSpeaker;
          });
        }
      });
    } else {
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
    if (widget._isStringeeCall) {
      widget._stringeeCall!.mute(!_isMute).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isMute = !_isMute;
          });
        }
      });
    } else {
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
    if (widget._isStringeeCall) {
      widget._stringeeCall!.enableVideo(!_isVideoEnable).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isVideoEnable = !_isVideoEnable;
          });
        }
      });
    } else {
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
}
