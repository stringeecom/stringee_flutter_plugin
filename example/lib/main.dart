import 'dart:io';

import 'package:flutter/material.dart';
import 'package:permission/permission.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/Chat.dart';

import 'Call.dart';

var user1 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZULTE2MDg3MTY0ODAiLCJpc3MiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZUIiwiZXhwIjoxNjExMzA4NDgwLCJ1c2VySWQiOiJ1c2VyMSJ9.e5U4nCiHrKDpuqi8oWs0LHTtzcH6_2Q0hP1oqMdNeMw';
var user2 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZULTE2MDYxMjI4OTkiLCJpc3MiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZUIiwiZXhwIjoxNjA4NzE0ODk5LCJ1c2VySWQiOiJ1c2VyMiJ9.b_tG9wp0zharQV0EHVSGefXyCzUvmGjqTImEVNOg01o';
var token =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS3RVaTBMZzNLa0lISkVwRTNiakZmMmd6UGtsNzlsU1otMTYxMDMzOTM3MyIsImlzcyI6IlNLdFVpMExnM0trSUhKRXBFM2JqRmYyZ3pQa2w3OWxTWiIsImV4cCI6MTYxMDQyNTc3MywidXNlcklkIjoiQUM3RlRFTzZHVCIsImljY19hcGkiOnRydWUsImRpc3BsYXlOYW1lIjoiTmd1eVx1MWVjNW4gUXVhbmcgS1x1MWVmMyBBbmgiLCJhdmF0YXJVcmwiOm51bGwsInN1YnNjcmliZSI6Im9ubGluZV9zdGF0dXNfR1I2Nkw3SU4sQUxMX0NBTExfU1RBVFVTLGFnZW50X21hbnVhbF9zdGF0dXMiLCJhdHRyaWJ1dGVzIjoiW3tcImF0dHJpYnV0ZVwiOlwib25saW5lU3RhdHVzXCIsXCJ0b3BpY1wiOlwib25saW5lX3N0YXR1c19HUjY2TDdJTlwifSx7XCJhdHRyaWJ1dGVcIjpcImNhbGxcIixcInRvcGljXCI6XCJjYWxsX0dSNjZMN0lOXCJ9XSJ9.5KcPKokaYXErg1BygQ7YIxhPNXuGJA5OCtQjhbyyfjc';
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
  Future<void> initState() {
    // TODO: implement initState
    super.initState();

    if (Platform.isAndroid) requestPermissions();

    // Lắng nghe sự kiện của StringeeClient(kết nối, cuộc gọi đến...)
    client.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeClientEvents) {
        switch (map['eventType']) {
          case StringeeClientEvents.DidConnect:
            handleDidConnectEvent();
            break;
          case StringeeClientEvents.DidDisconnect:
            handleDiddisconnectEvent();
            break;
          case StringeeClientEvents.DidFailWithError:
            handleDidFailWithErrorEvent(map['code'], map['message']);
            break;
          case StringeeClientEvents.RequestAccessToken:
            handleRequestAccessTokenEvent();
            break;
          case StringeeClientEvents.DidReceiveCustomMessage:
            handleDidReceiveCustomMessageEvent(map['body']);
            break;
          case StringeeClientEvents.DidReceiveTopicMessage:
            handleDidReceiveTopicMessageEvent(map['body']);
            break;
          case StringeeClientEvents.IncomingCall:
            StringeeCall call = map['body'];
            handleIncomingCallEvent(call);
            break;
          case StringeeClientEvents.IncomingCall2:
            StringeeCall2 call = map['body'];
            handleIncomingCall2Event(call);
            break;
          case StringeeClientEvents.DidReceiveChange:
            StringeeChange stringeeChange = map['body'];
            print(
                stringeeChange.objectType.toString() + '\t' + stringeeChange.changeType.toString());
            switch (stringeeChange.objectType) {
              case ObjectType.CONVERSATION:
                Conversation conversation = stringeeChange.object;
                print(conversation.id.toString());
                break;
              case ObjectType.MESSAGE:
                Message message = stringeeChange.object;
                print(message.id.toString() + '\t' + message.type.toString());
            }
            break;
          default:
            break;
        }
      }
    });

    // Connect
    client.connect(token);
  }

  requestPermissions() async {
    List<PermissionName> permissionNames = [];
    permissionNames.add(PermissionName.Camera);
    permissionNames.add(PermissionName.Contacts);
    permissionNames.add(PermissionName.Microphone);
    permissionNames.add(PermissionName.Location);
    permissionNames.add(PermissionName.Storage);
    permissionNames.add(PermissionName.State);
    permissionNames.add(PermissionName.Internet);
    var permissions = await Permission.requestPermissions(permissionNames);
    permissions.forEach((permission) {});
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
        children: <Widget>[topText, new MyForm()],
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
      myUserId = 'Not connected';
    });
  }

  void handleDidFailWithErrorEvent(int code, String message) {
    print('code: ' + code.toString() + '\nmessage: ' + message);
  }

  void handleRequestAccessTokenEvent() {
    print('Request new access token');
  }

  void handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic> map) {
    print('from: ' + map['from'] + '\nmessage: ' + map['msg']);
  }

  void handleDidReceiveTopicMessageEvent(Map<dynamic, dynamic> map) {
    print('from: ' + map['from'] + '\nmessage: ' + map['msg']);
  }

  void handleIncomingCallEvent(StringeeCall call) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
              fromUserId: call.from,
              toUserId: call.to,
              isVideoCall: call.isVideoCall,
              callType: StringeeType.StringeeCall,
              showIncomingUi: true,
              incomingCall: call)),
    );
  }

  void handleIncomingCall2Event(StringeeCall2 call) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
              fromUserId: call.from,
              toUserId: call.to,
              isVideoCall: call.isVideoCall,
              callType: StringeeType.StringeeCall2,
              showIncomingUi: true,
              incomingCall2: call)),
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
            child: new Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                new Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  mainAxisSize: MainAxisSize.max,
                  children: [
                    new Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _CallTapped(false, StringeeType.StringeeCall);
                            },
                            child: Text('CALL'),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          margin: EdgeInsets.only(top: 20.0),
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _CallTapped(true, StringeeType.StringeeCall);
                            },
                            child: Text('VIDEOCALL'),
                          ),
                        ),
                      ],
                    ),
                    new Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            padding: EdgeInsets.only(left: 20.0, right: 20.0),
                            onPressed: () {
                              _CallTapped(false, StringeeType.StringeeCall2);
                            },
                            child: Text('CALL2'),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          margin: EdgeInsets.only(top: 20.0),
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            padding: EdgeInsets.only(left: 20.0, right: 20.0),
                            onPressed: () {
                              _CallTapped(true, StringeeType.StringeeCall2);
                            },
                            child: Text('VIDEOCALL2'),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                new Container(
                  height: 40.0,
                  width: 175.0,
                  margin: EdgeInsets.only(top: 20.0),
                  child: new RaisedButton(
                    color: Colors.grey[300],
                    textColor: Colors.black,
                    onPressed: () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => Chat(
                                    client: client,
                                  )));
                    },
                    child: Text('CHAT'),
                  ),
                ),
              ],
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

  void _CallTapped(bool isVideoCall, StringeeType callType) {
    if (strUserId.isEmpty || !client.hasConnected) return;

    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
              fromUserId: client.userId,
              toUserId: strUserId,
              isVideoCall: isVideoCall,
              callType: callType,
              showIncomingUi: false)),
    );
  }
}
