import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ui/call.dart';

import '../utils/Common.dart';

class CallTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return CallTabState();
  }
}

class CallTabState extends State<CallTab> {
  String _connectStatus = 'Not connected...';
  StringeeClient? _stringeeClient;
  String _token =
      'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZULTE2OTQ0MDQ1MDMiLCJpc3MiOiJTS0UxUmRVdFVhWXhOYVFRNFdyMTVxRjF6VUp1UWRBYVZUIiwiZXhwIjoxNjk2OTk2NTAzLCJ1c2VySWQiOiJhbmRyb2lkMSJ9.1fh_vAVXezPJ2uKiAx41ItFYiurmMH3BexYIUrRS3Ag';
  String _toUser = '';

  @override
  void initState() {
    super.initState();

    _stringeeClient ??= StringeeClient();
    _stringeeClient!.registerEvent(StringeeClientListener(
      onConnect: (stringeeClient, userId) {
        debugPrint('onConnect: $userId');
        setState(() {
          _connectStatus = 'Connected as $userId';
        });
      },
      onDisconnect: (stringeeClient) {
        debugPrint('onDisconnect');
        setState(() {
          _connectStatus = 'Disconnected';
        });
      },
      onFailWithError: (stringeeClient, code, message) {
        debugPrint('onFailWithError: code - $code - message - $message');
        setState(() {
          _connectStatus = 'Connect fail: $message';
        });
      },
      onRequestAccessToken: (stringeeClient) {
        debugPrint('onRequestAccessToken');
      },
      onIncomingCall: (stringeeClient, stringeeCall) {
        debugPrint('onIncomingCall: callId - ${stringeeCall.id}');
        if (Common.isInCall) {
          stringeeCall.reject();
          return;
        }
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => Call(
              stringeeClient,
              stringeeCall.from!,
              stringeeCall.to!,
              true,
              stringeeCall.isVideoCall,
              true,
              stringeeCall: stringeeCall,
            ),
          ),
        );
      },
      onIncomingCall2: (stringeeClient, stringeeCall2) {
        debugPrint('onIncomingCall2: callId - ${stringeeCall2.id}');
        if (Common.isInCall) {
          stringeeCall2.reject();
          return;
        }
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => Call(
              stringeeClient,
              stringeeCall2.from!,
              stringeeCall2.to!,
              true,
              stringeeCall2.isVideoCall,
              false,
              stringeeCall2: stringeeCall2,
            ),
          ),
        );
      },
    ));
    if (!_stringeeClient!.hasConnected) {
      _stringeeClient!.connect(_token);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        resizeToAvoidBottomInset: false,
        body: new Stack(
          children: <Widget>[
            Container(
              padding: EdgeInsets.only(left: 10.0, top: 10.0),
              child: new Text(
                _connectStatus,
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
                          _toUser = value;
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
                                      callTapped(false, true);
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
                                      callTapped(true, true);
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
                                      callTapped(false, false);
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
                                      callTapped(true, false);
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

  void callTapped(bool isVideoCall, bool isStringeeCall) {
    if (_toUser.isEmpty || !_stringeeClient!.hasConnected) return;

    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => Call(
                _stringeeClient!,
                _stringeeClient!.userId!,
                _toUser,
                false,
                isVideoCall,
                isStringeeCall,
              )),
    );
  }
}
