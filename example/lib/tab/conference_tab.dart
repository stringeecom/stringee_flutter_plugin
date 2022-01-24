import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ui/room.dart';

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
  String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyLTE2NDI3MzkxNTgiLCJpc3MiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyIiwiZXhwIjoxNzQyNzM5MTU4LCJ1c2VySWQiOiJ1c2VyMSJ9.L6yQsh53Riv29pADP3wqQKjDAJCtYnMLD_csz289p-c";
  String roomToken = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyLTE2NDI3MzkxNTgiLCJpc3MiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyIiwiZXhwIjoxNzQyNzM5MTU4LCJyb29tSWQiOiJyb29tLXZuLTEtVEMwRjUxSDhCUC0xNTg5MzcwMDM4Nzg4IiwicGVybWlzc2lvbnMiOnsicHVibGlzaCI6dHJ1ZSwic3Vic2NyaWJlIjp0cnVlLCJjb250cm9sX3Jvb20iOnRydWUsInJlY29yZCI6dHJ1ZX19.ML0dQSac6H9xPGRa9iUwfIccf-rvTmwBCbWgO6fibug";

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
