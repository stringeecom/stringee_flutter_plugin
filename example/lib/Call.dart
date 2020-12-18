import 'dart:io' show Platform;
import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

StringeeCall _stringeeCall;
StringeeCall2 _stringeeCall2;

class Call extends StatefulWidget {
  StringeeCall incomingCall;
  StringeeCall2 incomingCall2;
  final String toUserId;
  final String fromUserId;
  String callId;
  StringeeType callType;
  bool showIncomingUi = false;
  bool hasLocalStream = false;
  bool hasRemoteStream = false;
  bool isVideoCall = false;
  bool isSpeaker = false;
  bool isResumeVideo = false;
  bool isMirror = true;

  Call({
    Key key,
    @required this.fromUserId,
    @required this.toUserId,
    @required this.showIncomingUi,
    @required this.isVideoCall,
    this.callType,
    this.incomingCall2,
    this.incomingCall,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _CallState();
  }
}

class _CallState extends State<Call> {
  String status = "";

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    widget.isSpeaker = widget.isVideoCall;

    if (widget.callType == StringeeType.StringeeCall) {
      _makeOrInitAnswerCall();
    } else {
      _makeOrInitAnswerCall2();
    }
  }

  @override
  Widget build(BuildContext context) {
    Widget NameCalling = new Container(
      alignment: Alignment.topCenter,
      padding: EdgeInsets.only(top: 120.0),
      child: new Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          new Container(
            padding: EdgeInsets.only(bottom: 15.0),
            child: new Text(
              "${widget.toUserId}",
              style: new TextStyle(
                color: Colors.white,
                fontSize: 35.0,
              ),
            ),
          ),
          new Container(
            child: new Text(
              '${status}',
              style: new TextStyle(
                color: Colors.white,
                fontSize: 20.0,
              ),
            ),
          )
        ],
      ),
    );

    Widget BottomContainer = new Container(
      padding: EdgeInsets.only(bottom: 30.0),
      alignment: Alignment.bottomCenter,
      child: new Column(
          mainAxisAlignment: MainAxisAlignment.end,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: widget.showIncomingUi
              ? <Widget>[
                  new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: <Widget>[
                      new GestureDetector(
                        onTap: _rejectCallTapped,
                        child: Image.asset(
                          'images/end.png',
                          height: 75.0,
                          width: 75.0,
                        ),
                      ),
                      new GestureDetector(
                        onTap: _acceptCallTapped,
                        child: Image.asset(
                          'images/answer.png',
                          height: 75.0,
                          width: 75.0,
                        ),
                      ),
                    ],
                  )
                ]
              : <Widget>[
                  new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: <Widget>[
                      new ButtonSpeaker(isSpeaker: widget.isVideoCall),
                      new ButtonMicro(isMute: false),
                      new ButtonVideo(isVideoEnable: widget.isVideoCall),
                    ],
                  ),
                  new Container(
                    padding: EdgeInsets.only(top: 20.0, bottom: 20.0),
                    child: new GestureDetector(
                      onTap: _endCallTapped,
                      child: Image.asset(
                        'images/end.png',
                        height: 75.0,
                        width: 75.0,
                      ),
                    ),
                  )
                ]),
    );

    Widget localView = (widget.hasLocalStream)
        ? new StringeeVideoView(
            color: Colors.white,
            alignment: Alignment.topRight,
            callId: widget.callId,
            isLocal: true,
            isOverlay: true,
            isMirror: widget.isMirror,
            margin: EdgeInsets.only(top: 100.0, right: 25.0),
            height: 200.0,
            width: 150.0,
          )
        : Placeholder();

    Widget remoteView = (widget.hasRemoteStream)
        ? new StringeeVideoView(
            color: Colors.blue,
            callId: widget.callId,
            isLocal: false,
            isOverlay: false,
            isMirror: false,
            scalingType: ScalingType.SCALE_ASPECT_FIT,
          )
        : Placeholder();

    return new Scaffold(
      backgroundColor: Colors.black,
      body: new Stack(
        children: <Widget>[
          remoteView,
          localView,
          NameCalling,
          BottomContainer,
          ButtonSwitchCamera(
            isMirror: widget.isMirror,
          ),
        ],
      ),
    );
  }

  Future _makeOrInitAnswerCall() async {
    // Gán cuộc gọi đến cho biến global
    _stringeeCall = widget.incomingCall;

    if (!widget.showIncomingUi) {
      _stringeeCall = StringeeCall();
    }

    // Listen events
    _stringeeCall.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeCallEvents) {
        switch (map['eventType']) {
          case StringeeCallEvents.DidChangeSignalingState:
            handleSignalingStateChangeEvent(map['body']);
            break;
          case StringeeCallEvents.DidChangeMediaState:
            handleMediaStateChangeEvent(map['body']);
            break;
          case StringeeCallEvents.DidReceiveCallInfo:
            handleReceiveCallInfoEvent(map['body']);
            break;
          case StringeeCallEvents.DidHandleOnAnotherDevice:
            handleHandleOnAnotherDeviceEvent(map['body']);
            break;
          case StringeeCallEvents.DidReceiveLocalStream:
            handleReceiveLocalStreamEvent(map['body']);
            break;
          case StringeeCallEvents.DidReceiveRemoteStream:
            handleReceiveRemoteStreamEvent(map['body']);
            break;
          case StringeeCallEvents.DidChangeAudioDevice:
            if (Platform.isAndroid) {
              handleChangeAudioDeviceEvent(
                  map['selectedAudioDevice'], _stringeeCall, null);
            }
            break;
          default:
            break;
        }
      }
    });

    if (widget.showIncomingUi) {
      _stringeeCall.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      final parameters = {
        'from': widget.fromUserId,
        'to': widget.toUserId,
        'isVideoCall': widget.isVideoCall,
        'customData': null,
        'videoResolution': VideoQuality.FULLHD,
      };

      _stringeeCall.makeCall(parameters).then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        print(
            'MakeCall CallBack --- $status - $code - $message - ${_stringeeCall.id} - ${_stringeeCall.from} - ${_stringeeCall.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  Future _makeOrInitAnswerCall2() async {
    // Gán cuộc gọi đến cho biến global
    _stringeeCall2 = widget.incomingCall2;

    if (!widget.showIncomingUi) {
      _stringeeCall2 = StringeeCall2();
    }

    // Listen events
    _stringeeCall2.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeCall2Events) {
        switch (map['eventType']) {
          case StringeeCall2Events.DidChangeSignalingState:
            handleSignalingStateChangeEvent(map['body']);
            break;
          case StringeeCall2Events.DidChangeMediaState:
            handleMediaStateChangeEvent(map['body']);
            break;
          case StringeeCall2Events.DidReceiveCallInfo:
            handleReceiveCallInfoEvent(map['body']);
            break;
          case StringeeCall2Events.DidHandleOnAnotherDevice:
            handleHandleOnAnotherDeviceEvent(map['body']);
            break;
          case StringeeCall2Events.DidReceiveLocalStream:
            handleReceiveLocalStreamEvent(map['body']);
            break;
          case StringeeCall2Events.DidReceiveRemoteStream:
            handleReceiveRemoteStreamEvent(map['body']);
            break;
          case StringeeCall2Events.DidChangeAudioDevice:
            if (Platform.isAndroid) {
              handleChangeAudioDeviceEvent(
                  map['selectedAudioDevice'], null, _stringeeCall2);
            }
            break;
          default:
            break;
        }
      }
    });

    if (widget.showIncomingUi) {
      _stringeeCall2.initAnswer().then((event) {
        bool status = event['status'];
        if (!status) {
          clearDataEndDismiss();
        }
      });
    } else {
      final parameters = {
        'from': widget.fromUserId,
        'to': widget.toUserId,
        'isVideoCall': widget.isVideoCall,
        'customData': null,
        'videoResolution': VideoQuality.FULLHD,
      };

      _stringeeCall2.makeCall(parameters).then((result) {
        bool status = result['status'];
        int code = result['code'];
        String message = result['message'];
        print(
            'MakeCall CallBack --- $status - $code - $message - ${_stringeeCall2.id} - ${_stringeeCall2.from} - ${_stringeeCall2.to}');
        if (!status) {
          Navigator.pop(context);
        }
      });
    }
  }

  void _endCallTapped() {
    switch (widget.callType) {
      case StringeeType.StringeeCall:
        _stringeeCall.hangup().then((result) {
          print('_endCallTapped -- ${result['message']}');
          bool status = result['status'];
          if (status) {
            clearDataEndDismiss();
          }
        });
        break;
      case StringeeType.StringeeCall2:
        _stringeeCall2.hangup().then((result) {
          print('_endCallTapped -- ${result['message']}');
          bool status = result['status'];
          if (status) {
            clearDataEndDismiss();
          }
        });
        break;
    }
  }

  void _acceptCallTapped() {
    switch (widget.callType) {
      case StringeeType.StringeeCall:
        _stringeeCall.answer().then((result) {
          print('_acceptCallTapped -- ${result['message']}');
          bool status = result['status'];
          if (!status) {
            clearDataEndDismiss();
          }
        });
        break;
      case StringeeType.StringeeCall2:
        _stringeeCall2.answer().then((result) {
          print('_acceptCallTapped -- ${result['message']}');
          bool status = result['status'];
          if (!status) {
            clearDataEndDismiss();
          }
        });
        break;
    }
    setState(() {
      widget.showIncomingUi = !widget.showIncomingUi;
    });
  }

  void _rejectCallTapped() {
    switch (widget.callType) {
      case StringeeType.StringeeCall:
        _stringeeCall.reject().then((result) {
          print('_rejectCallTapped -- ${result['message']}');
          clearDataEndDismiss();
        });
        break;
      case StringeeType.StringeeCall2:
        _stringeeCall2.reject().then((result) {
          print('_rejectCallTapped -- ${result['message']}');
          clearDataEndDismiss();
        });
        break;
    }
  }

  void handleSignalingStateChangeEvent(StringeeSignalingState state) {
    print('handleSignalingStateChangeEvent - $state');
    setState(() {
      status = state.toString().split('.')[1];
    });
    switch (state) {
      case StringeeSignalingState.Calling:
        break;
      case StringeeSignalingState.Ringing:
        break;
      case StringeeSignalingState.Answered:
        break;
      case StringeeSignalingState.Busy:
        clearDataEndDismiss();
        break;
      case StringeeSignalingState.Ended:
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
      case StringeeMediaState.Connected:
        break;
      case StringeeMediaState.Disconnected:
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
  }

  void handleReceiveLocalStreamEvent(String callId) {
    print('handleReceiveLocalStreamEvent - $callId');
    setState(() {
      widget.hasLocalStream = true;
      widget.callId = callId;
    });
  }

  void handleReceiveRemoteStreamEvent(String callId) {
    print('handleReceiveRemoteStreamEvent - $callId');
    setState(() {
      widget.hasRemoteStream = true;
      widget.callId = callId;
    });
  }

  void handleChangeAudioDeviceEvent(
      AudioDevice audioDevice, StringeeCall call, StringeeCall2 call2) {
    print('handleChangeAudioDeviceEvent - $audioDevice');
    switch (audioDevice) {
      case AudioDevice.SPEAKER_PHONE:
        if (call != null) {
          call.setSpeakerphoneOn(widget.isSpeaker);
        }
        if (call2 != null) {
          call2.setSpeakerphoneOn(widget.isSpeaker);
        }
        break;
      case AudioDevice.BLUETOOTH:
      case AudioDevice.WIRED_HEADSET:
        widget.isSpeaker = false;
        if (call != null) {
          call.setSpeakerphoneOn(widget.isSpeaker);
        }
        if (call2 != null) {
          call2.setSpeakerphoneOn(widget.isSpeaker);
        }
        break;
    }
  }

  void clearDataEndDismiss() {
    if (_stringeeCall != null) {
      _stringeeCall.destroy();
      _stringeeCall = null;
      Navigator.pop(context);
    } else if (_stringeeCall2 != null) {
      _stringeeCall2.destroy();
      _stringeeCall2 = null;
      Navigator.pop(context);
    } else {
      Navigator.pop(context);
    }
  }
}

class ButtonSwitchCamera extends StatefulWidget {
  bool isMirror;

  ButtonSwitchCamera({
    Key key,
    this.isMirror,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _ButtonSwitchCameraState();
}

class _ButtonSwitchCameraState extends State<ButtonSwitchCamera> {
  void _toggleSwitchCamera() {
    if (_stringeeCall != null) {
      widget.isMirror = !widget.isMirror;
      _stringeeCall.switchCamera(widget.isMirror).then((result) {
        bool status = result['status'];
        if (status) {}
      });
    } else {
      widget.isMirror = !widget.isMirror;
      _stringeeCall2.switchCamera(widget.isMirror).then((result) {
        bool status = result['status'];
        if (status) {}
      });
    }
  }

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new Align(
      alignment: Alignment.topLeft,
      child: Padding(
        padding: EdgeInsets.only(left: 50.0, top: 50.0),
        child: new GestureDetector(
          onTap: _toggleSwitchCamera,
          child: Image.asset(
            'images/switch_camera.png',
            height: 30.0,
            width: 30.0,
          ),
        ),
      ),
    );
  }
}

class ButtonSpeaker extends StatefulWidget {
  final bool isSpeaker;

  ButtonSpeaker({
    Key key,
    @required this.isSpeaker,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _ButtonSpeakerState();
}

class _ButtonSpeakerState extends State<ButtonSpeaker> {
  bool _isSpeaker;

  void _toggleSpeaker() {
    if (_stringeeCall != null) {
      _stringeeCall.setSpeakerphoneOn(!_isSpeaker).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isSpeaker = !_isSpeaker;
          });
        }
      });
    } else {
      _stringeeCall2.setSpeakerphoneOn(!_isSpeaker).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isSpeaker = !_isSpeaker;
          });
        }
      });
    }
  }

  @override
  void initState() {
    super.initState();
    _isSpeaker = widget.isSpeaker;
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new GestureDetector(
      onTap: _toggleSpeaker,
      child: Image.asset(
        _isSpeaker ? 'images/ic_speaker_off.png' : 'images/ic_speaker_on.png',
        height: 75.0,
        width: 75.0,
      ),
    );
  }
}

class ButtonMicro extends StatefulWidget {
  final bool isMute;

  ButtonMicro({
    Key key,
    @required this.isMute,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _ButtonMicroState();
}

class _ButtonMicroState extends State<ButtonMicro> {
  bool _isMute;

  void _toggleMicro() {
    if (_stringeeCall != null) {
      _stringeeCall.mute(!_isMute).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isMute = !_isMute;
          });
        }
      });
    } else {
      _stringeeCall2.mute(!_isMute).then((result) {
        bool status = result['status'];
        if (status) {
          setState(() {
            _isMute = !_isMute;
          });
        }
      });
    }
  }

  @override
  void initState() {
    super.initState();
    _isMute = widget.isMute;
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new GestureDetector(
      onTap: _toggleMicro,
      child: Image.asset(
        _isMute ? 'images/ic_mute.png' : 'images/ic_mic.png',
        height: 75.0,
        width: 75.0,
      ),
    );
  }
}

class ButtonVideo extends StatefulWidget {
  final bool isVideoEnable;

  ButtonVideo({
    Key key,
    @required this.isVideoEnable,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _ButtonVideoState();
}

class _ButtonVideoState extends State<ButtonVideo> {
  bool _isVideoEnable;

  void _toggleVideo() {
    if (_stringeeCall != null) {
      // _stringeeCall.enableVideo(!_isVideoEnable).then((result) {
      //   bool status = result['status'];
      //   if (status) {
      //     setState(() {
      //       _isVideoEnable = !_isVideoEnable;
      //     });
      //   }
      // });
      _stringeeCall.resumeVideo().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    } else {
      // _stringeeCall2.enableVideo(!_isVideoEnable).then((result) {
      //   bool status = result['status'];
      //   if (status) {
      //     setState(() {
      //       _isVideoEnable = !_isVideoEnable;
      //     });
      //   }
      // });
      _stringeeCall2.resumeVideo().then((result) {
        bool status = result['status'];
        if (status) {}
      });
    }
  }

  @override
  void initState() {
    super.initState();
    _isVideoEnable = widget.isVideoEnable;
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new GestureDetector(
      onTap: widget.isVideoEnable ? _toggleVideo : null,
      child: Image.asset(
        _isVideoEnable ? 'images/ic_video.png' : 'images/ic_video_off.png',
        height: 75.0,
        width: 75.0,
      ),
    );
  }
}
