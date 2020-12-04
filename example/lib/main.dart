import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'Call.dart';

var user1 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZULTE2MDYxMjQwODgiLCJpc3MiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZUIiwiZXhwIjoxNjA4NzE2MDg4LCJ1c2VySWQiOiJ1c2VyMSJ9.RkqiRpfvDoMU9rZORzJKHTmtN_w70Tr5rnp5IePOJFE';
var user2 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZULTE2MDYxMjI4OTkiLCJpc3MiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZUIiwiZXhwIjoxNjA4NzE0ODk5LCJ1c2VySWQiOiJ1c2VyMiJ9.b_tG9wp0zharQV0EHVSGefXyCzUvmGjqTImEVNOg01o';
var client = StringeeClient();
String strUserId = "";

void main() {
  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(title: "OneToOneCallSample", home: new MyHomePage());
  }
}

class MyHomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _MyHomePageState();
  }
}

class _MyHomePageState extends State<MyHomePage> {
  String myUserId = 'Not connected...';

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    // Lắng nghe sự kiện của StringeeClient(kết nối, cuộc gọi đến...)
    client.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      StringeeClientEventType eventType = map['eventType'];
      switch (eventType) {
        case StringeeClientEventType.DidConnect:
          handleDidConnectEvent();
          break;
        case StringeeClientEventType.DidDisconnect:
          handleDiddisconnectEvent();
          break;
        case StringeeClientEventType.DidFailWithError:
          handleDidFailWithErrorEvent(map['code'], map['message']);
          break;
        case StringeeClientEventType.RequestAccessToken:
          handleRequestAccessTokenEvent();
          break;
        case StringeeClientEventType.DidReceiveCustomMessage:
          handleDidReceiveCustomMessageEvent(map['from'], map['message']);
          break;
        case StringeeClientEventType.DidReceiveTopicMessage:
          handleDidReceiveTopicMessageEvent(map['from'], map['message']);
          break;
        case StringeeClientEventType.IncomingCall:
          StringeeCall call = map['body'];
          handleIncomingCallEvent(call);
          break;
        default:
          break;
      }
    });

    // Connect
    client.connect(user1);
  }

  @override
  Widget build(BuildContext context) {
    Widget topText = new Container(
      padding: EdgeInsets.only(left: 10.0, top: 10.0),
      child: new Text(
        'Connected as: $myUserId',
        style: new TextStyle(
          color: Colors.black,
          fontSize: 20.0,
        ),
      ),
    );

    return new Scaffold(
      appBar: new AppBar(
        title: new Text("OneToOneCallSample"),
        backgroundColor: Colors.indigo[600],
      ),
      body: new Stack(
        children: <Widget>[
          topText,
          new MyForm(),
        ],
      ),
    );
  }

  //region Handle Client Event
  void handleDidConnectEvent() {
    setState(() {
      myUserId = client.userId;
    });
  }

  void handleDiddisconnectEvent() {
    setState(() {
      myUserId = 'Not connected...';
    });
  }

  void handleDidFailWithErrorEvent(int code, String message) {
    print('code: ' + code.toString() + '\nmessage: ' + message);
  }

  void handleRequestAccessTokenEvent() {
    print('Request new access token');
  }

  void handleDidReceiveCustomMessageEvent(
      String from, Map<dynamic, dynamic> message) {
    print('from: ' + from + '\nmessage: ' + message.toString());
  }

  void handleDidReceiveTopicMessageEvent(
      String from, Map<dynamic, dynamic> message) {
    print('from: ' + from + '\nmessage: ' + message.toString());
  }

  void handleIncomingCallEvent(StringeeCall call) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
              fromUserId: call.from,
              toUserId: call.to,
              isVideoCall: call.isVideocall,
              showIncomingUi: true,
              incomingCall: call)),
    );
  }

//endregion
}

class MyForm extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _MyFormState();
  }
}

class _MyFormState extends State<MyForm> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new Form(
//      key: _formKey,
      child: new Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          new Container(
            padding: EdgeInsets.all(20.0),
            child: new TextField(
              onChanged: (String value) {
                _changeText(value);
              },
              decoration: InputDecoration(
                focusedBorder: UnderlineInputBorder(
                  borderSide: BorderSide(color: Colors.red),
                ),
              ),
            ),
          ),
          new Container(
            child: new Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  new RaisedButton(
                    color: Colors.grey[300],
                    textColor: Colors.black,
                    padding: EdgeInsets.only(left: 40.0, right: 40.0),
                    onPressed: _voiceCallTapped,
                    child: Text('CALL'),
                  ),
                ]),
          ),
        ],
      ),
    );
  }

  void _changeText(String val) {
    setState(() {
      strUserId = val;
    });
  }

  void _voiceCallTapped() {
    if (strUserId.isEmpty || !client.hasConnected) return;

    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
              fromUserId: client.userId,
              toUserId: strUserId,
              isVideoCall: true,
              showIncomingUi: false)),
    );
  }
}
