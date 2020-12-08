import 'dart:io' show Platform;
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

StringeeCall _stringeeCall;

class Call extends StatefulWidget {
  StringeeCall incomingCall;
  final String toUserId;
  final String fromUserId;
  String callId;
  bool showIncomingUi = false;
  bool hasLocalStream = false;
  bool hasRemoteStream = false;
  bool isVideoCall = false;

  Call(
      {Key key,
      @required this.fromUserId,
      @required this.toUserId,
      @required this.showIncomingUi,
      @required this.isVideoCall,
      this.incomingCall})
      : super(key: key);

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
    _makeOrInitAnswerCall();
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
                      new ButtonSpeaker(isSpeaker: false),
                      new ButtonMicro(isMute: false),
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
            isMirror: true,
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
          )
        : Placeholder();

    return new Scaffold(
      backgroundColor: Colors.black,
      body: new Stack(
        children: <Widget>[remoteView, localView, NameCalling, BottomContainer],
      ),
    );
  }

  Future _makeOrInitAnswerCall() async {
    if (Platform.isAndroid) {
      await [Permission.microphone, Permission.camera].request();
      if (!(await Permission.camera.isGranted &&
          await Permission.microphone.isGranted)) {
        clearDataEndDismiss();
      }
    }

    // Gán cuộc gọi đến cho biến global
    _stringeeCall = widget.incomingCall;

    if (!widget.showIncomingUi) {
      _stringeeCall = StringeeCall();
    }

    // Listen events
    _stringeeCall.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      StringeeCallEventType eventType = map['eventType'];
      switch (eventType) {
        case StringeeCallEventType.DidChangeSignalingState:
          handleSignalingStateChangeEvent(map['body']);
          break;
        case StringeeCallEventType.DidChangeMediaState:
          handleMediaStateChangeEvent(map['body']);
          break;
        case StringeeCallEventType.DidReceiveCallInfo:
          handleReceiveCallInfoEvent(map['body']);
          break;
        case StringeeCallEventType.DidHandleOnAnotherDevice:
          handleHandleOnAnotherDeviceEvent(map['body']);
          break;
        case StringeeCallEventType.DidReceiveLocalStream:
          handleReceiveLocalStreamEvent(map['body']);
          break;
        case StringeeCallEventType.DidReceiveRemoteStream:
          handleReceiveRemoteStreamEvent(map['body']);
          break;
        case StringeeCallEventType.DidChangeAudioDevice:
          if (Platform.isAndroid) {
            handleChangeAudioDeviceEvent(map['selectedAudioDevice']);
          }
          break;
        default:
          break;
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
        'videoResolution': null,
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

  void _endCallTapped() {
    _stringeeCall.hangup().then((result) {
      print('_endCallTapped -- ${result['message']}');
      bool status = result['status'];
      if (status) {
        clearDataEndDismiss();
      }
    });
  }

  void _acceptCallTapped() {
    _stringeeCall.answer().then((result) {
      print('_acceptCallTapped -- ${result['message']}');
      bool status = result['status'];
      if (!status) {
        clearDataEndDismiss();
      }
    });
    setState(() {
      widget.showIncomingUi = !widget.showIncomingUi;
    });
  }

  void _rejectCallTapped() {
    _stringeeCall.reject().then((result) {
      print('_rejectCallTapped -- ${result['message']}');
      clearDataEndDismiss();
    });
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

  void handleHandleOnAnotherDeviceEvent(StringeeMediaState state) {
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

  void handleChangeAudioDeviceEvent(AudioDevice audioDevice) {
    print('handleChangeAudioDeviceEvent - $audioDevice');
  }

  void clearDataEndDismiss() {
    if (_stringeeCall != null) {
      _stringeeCall.destroy();
      _stringeeCall = null;
      Navigator.pop(context);
    } else {
      Navigator.pop(context);
    }
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
    _stringeeCall.setSpeakerphoneOn(!_isSpeaker).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isSpeaker = !_isSpeaker;
        });
      }
    });
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
    _stringeeCall.mute(!_isMute).then((result) {
      bool status = result['status'];
      if (status) {
        setState(() {
          _isMute = !_isMute;
        });
      }
    });
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
