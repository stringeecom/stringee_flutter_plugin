import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'Call.dart';

var user1 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS2pSUmY1R0ltUldZdGVLZ0VDNVJNbzhXQkZnM3pUWTItMTU0MzAzMzEwMCIsImlzcyI6IlNLalJSZjVHSW1SV1l0ZUtnRUM1Uk1vOFdCRmczelRZMiIsImV4cCI6MTU0NTYyNTEwMCwidXNlcklkIjoidXNlcjEifQ.hQsEb7rQZmhq3tGC-h7IT2EJSaLgTxSmQB9Ow7fbMRc';
var user2 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS2pSUmY1R0ltUldZdGVLZ0VDNVJNbzhXQkZnM3pUWTItMTU0MzA0MjY5NCIsImlzcyI6IlNLalJSZjVHSW1SV1l0ZUtnRUM1Uk1vOFdCRmczelRZMiIsImV4cCI6MTU0NTYzNDY5NCwidXNlcklkIjoidXNlcjIifQ.McsHrklVWdGZRub0Y1Q9VV2xNw2C0nPxQeB1r9Eo1TE';

var client = StringeeClient();
String strUserId = "";

void main() {
  SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp])
      .then((_) {
    runApp(new MyApp());
  });
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
        title: "OneToOneCallSample",
        home: new MyHomePage()
    );
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
      switch(eventType) {
        case StringeeClientEventType.DidConnect:
          handleDidConnectEvent();
          break;
        case StringeeClientEventType.DidDisconnect:
          handleDiddisconnectEvent();
          break;
        case StringeeClientEventType.DidFailWithError:
          break;
        case StringeeClientEventType.RequestAccessToken:
          break;
        case StringeeClientEventType.DidReceiveCustomMessage:
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

  void handleIncomingCallEvent(StringeeCall call) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => Call(fromUserId: call.from, toUserId: call.to, showIncomingUi: true, incomingCall: call)),
    );
  }

  //endregion
}

class MyForm extends StatefulWidget{
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
                ]
            ),

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
      MaterialPageRoute(builder: (context) => Call(fromUserId: client.userId, toUserId: strUserId, showIncomingUi: false)),
    );
  }

}





