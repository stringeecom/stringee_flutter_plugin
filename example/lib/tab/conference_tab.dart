import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin_example/ui/room.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

StringeeClient client = new StringeeClient();

class ConferenceTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ConferenceTabState();
  }
}

class ConferenceTabState extends State<ConferenceTab> {
  String myUserId = 'Not connected...';
  String token = '';
  String roomToken = '';

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
            Center(
              child: new Container(
                height: 40.0,
                width: 175.0,
                child: new ElevatedButton(
                  onPressed: () {
                    if (client.hasConnected)
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => Room(
                            client,
                            roomToken,
                          ),
                        ),
                      );
                  },
                  child: Text('Join Room'),
                ),
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
      myUserId = client.userId!;
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

  @override
  // TODO: implement wantKeepAlive
  bool get wantKeepAlive => true;
}
