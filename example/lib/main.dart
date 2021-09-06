import 'dart:io';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:stringee_flutter_plugin_example/tab/call_tab.dart';
import 'package:stringee_flutter_plugin_example/tab/chat_tab.dart';

import 'tab/live_chat_tab.dart';

var user2 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyLTE2MzAzMTYyMDMiLCJpc3MiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyIiwiZXhwIjoxNjMyOTA4MjAzLCJ1c2VySWQiOiJ1c2VyMiJ9.r41arMsQmj1wwwca7OhZccc-afEU1c4GvvwqLzxEOWo';
var user1 =
    'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyLTE2MzAzMTYxOTMiLCJpc3MiOiJTS0xIb2NCdDl6Qk5qc1pLeThZaUVkSzRsU3NBZjhCSHpyIiwiZXhwIjoxNjMyOTA4MTkzLCJ1c2VySWQiOiJ1c2VyMSJ9.HbeDbBkm8FcyFf0WcfBLAbWJXeHowAKLWh6vqDOgrH4';

String strUserId = "";

void main() {
  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
        title: "Stringee flutter sample", home: new MyHomePage());
  }
}

class MyHomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _MyHomePageState();
  }
}

class _MyHomePageState extends State<MyHomePage> {
  int _currentIndex = 0;
  List<Widget> _childrent = [
    CallTab(),
    ChatTab(),
    LiveChatTab(),
  ];

  @override
  void initState() {
    super.initState();

    if (Platform.isAndroid) {
      requestPermissions();
    }
  }

  requestPermissions() async {
    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
      Permission.microphone,
    ].request();
    print(statuses);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: new AppBar(
        title: new Text("Stringee flutter sample"),
        backgroundColor: Colors.indigo[600],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: _childrent,
      ),
      bottomNavigationBar: BottomNavigationBar(
          currentIndex: _currentIndex,
          onTap: (index) {
            setState(() {
              _currentIndex = index;
            });
          },
          items: [
            BottomNavigationBarItem(
              icon: Icon(Icons.call),
              label: 'Call',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.chat),
              label: 'Chat',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.chat),
              label: 'Live chat',
            ),
          ]),
    );
  }
}
