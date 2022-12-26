import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:stringee_flutter_plugin_example/tab/call_tab.dart';
import 'package:stringee_flutter_plugin_example/tab/chat_tab.dart';
import 'package:stringee_flutter_plugin_example/tab/conference_tab.dart';

import 'tab/live_chat_tab.dart';

void main() {
  runApp(new MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
        debugShowCheckedModeBanner: false,
        title: "Stringee flutter sample",
        home: new MyHomePage());
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
  List<Widget> _children = [
    CallTab(),
    ChatTab(),
    LiveChatTab(),
    ConferenceTab(),
  ];

  @override
  void initState() {
    super.initState();
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.manual,
        overlays: [SystemUiOverlay.bottom]);

    if (Platform.isAndroid) {
      requestPermissions();
    }
  }

  requestPermissions() async {
    DeviceInfoPlugin deviceInfoPlugin = DeviceInfoPlugin();
    deviceInfoPlugin.androidInfo.then((value) async {
      if (value.version.sdkInt! >= 31) {
        Map<Permission, PermissionStatus> statuses = await [
          Permission.camera,
          Permission.microphone,
          Permission.bluetoothConnect,
        ].request();
        print(statuses);
      } else {
        Map<Permission, PermissionStatus> statuses = await [
          Permission.camera,
          Permission.microphone,
        ].request();
        print(statuses);
      }
    });
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
        children: _children,
      ),
      bottomNavigationBar: BottomNavigationBar(
          currentIndex: _currentIndex,
          type: BottomNavigationBarType.fixed,
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
            BottomNavigationBarItem(
              icon: Icon(Icons.ondemand_video),
              label: 'Conference',
            ),
          ]),
    );
  }
}
