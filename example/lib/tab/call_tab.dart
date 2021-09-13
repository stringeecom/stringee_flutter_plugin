import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ui/call.dart';

StringeeClient client = new StringeeClient();

class CallTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return CallTabState();
  }
}

class CallTabState extends State<CallTab> {
  String myUserId = 'Not connected...';
  String token = 'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS3RVaTBMZzNLa0lISkVwRTNiakZmMmd6UGtsNzlsU1otMTYzMTUxNTQ0MSIsImlzcyI6IlNLdFVpMExnM0trSUhKRXBFM2JqRmYyZ3pQa2w3OWxTWiIsImV4cCI6MTYzMTYwMTg0MSwidXNlcklkIjoidmlkZW9fODQ5NjUyODAxOTYiLCJjYW5DYWxsb3V0IjpmYWxzZX0.TDI1nO478Og7bxNvg9_D3Q8Q06EXAUTHAwUIUZnnux4';
  String toUser = '';

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    /// Lắng nghe sự kiện của StringeeClient(kết nối, cuộc gọi đến...)
    client.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      switch (map['eventType']) {
        case StringeeClientEvents.didConnect:
          handleDidConnectEvent();
          break;
        case StringeeClientEvents.didDisconnect:
          handleDiddisconnectEvent();
          break;
        case StringeeClientEvents.didFailWithError:
          handleDidFailWithErrorEvent(
              map['body']['code'], map['body']['message']);
          break;
        case StringeeClientEvents.requestAccessToken:
          handleRequestAccessTokenEvent();
          break;
        case StringeeClientEvents.didReceiveCustomMessage:
          handleDidReceiveCustomMessageEvent(map['body']);
          break;
        case StringeeClientEvents.incomingCall:
          StringeeCall call = map['body'];
          handleIncomingCallEvent(call);
          break;
        case StringeeClientEvents.incomingCall2:
          StringeeCall2 call = map['body'];
          handleIncomingCall2Event(call);
          break;
        default:
          break;
      }
    });

    /// Connect
    if (token.isNotEmpty) {
      client.connect(token);
    }
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        body: new Stack(
          children: <Widget>[
            Container(
              padding: EdgeInsets.only(left: 10.0, top: 10.0),
              child: new Text(
                'Connected as: $myUserId',
                style: new TextStyle(
                  color: Colors.black,
                  fontSize: 20.0,
                ),
              ),
            ),
            Form(
              child: new Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  new Container(
                    padding: EdgeInsets.all(20.0),
                    child: new TextField(
                      onChanged: (String value) {
                        setState(() {
                          toUser = value;
                        });
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
                                  child: new ElevatedButton(
                                    onPressed: () {
                                      _callTapped(
                                          false, StringeeObjectEventType.call);
                                    },
                                    child: Text('CALL'),
                                  ),
                                ),
                                new Container(
                                  height: 40.0,
                                  width: 175.0,
                                  margin: EdgeInsets.only(top: 20.0),
                                  child: new ElevatedButton(
                                    onPressed: () {
                                      _callTapped(
                                          true, StringeeObjectEventType.call);
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
                                  child: new ElevatedButton(
                                    style: ElevatedButton.styleFrom(
                                        padding: EdgeInsets.only(
                                            left: 20.0, right: 20.0)),
                                    onPressed: () {
                                      _callTapped(
                                          false, StringeeObjectEventType.call2);
                                    },
                                    child: Text('CALL2'),
                                  ),
                                ),
                                new Container(
                                  height: 40.0,
                                  width: 175.0,
                                  margin: EdgeInsets.only(top: 20.0),
                                  child: new ElevatedButton(
                                    style: ElevatedButton.styleFrom(
                                        padding: EdgeInsets.only(
                                            left: 20.0, right: 20.0)),
                                    onPressed: () {
                                      _callTapped(
                                          true, StringeeObjectEventType.call2);
                                    },
                                    child: Text('VIDEOCALL2'),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
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
    print('from: ' + map['fromUserId'] + '\nmessage: ' + map['message']);
  }

  void handleIncomingCallEvent(StringeeCall call) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => Call(
          client,
          call.from,
          call.to,
          call.isVideoCall,
          true,
          StringeeObjectEventType.call,
          stringeeCall: call,
        ),
      ),
    );
  }

  void handleIncomingCall2Event(StringeeCall2 call) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => Call(
          client,
          call.from,
          call.to,
          call.isVideoCall,
          true,
          StringeeObjectEventType.call2,
          stringeeCall2: call,
        ),
      ),
    );
  }

  void _callTapped(bool isVideoCall, StringeeObjectEventType callType) {
    if (toUser.isEmpty || !client.hasConnected) return;

    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
                client,
                client.userId,
                toUser,
                false,
                isVideoCall,
                callType,
              )),
    );
  }

  @override
  // TODO: implement wantKeepAlive
  bool get wantKeepAlive => true;
}
