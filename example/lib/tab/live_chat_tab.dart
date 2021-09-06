import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin_example/ui/agent.dart';
import 'package:stringee_flutter_plugin_example/ui/visitor.dart';

class LiveChatTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return LiveChatTabState();
  }
}

class LiveChatTabState extends State<LiveChatTab> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: DefaultTabController(
        length: 2,
        child: Scaffold(
          body: Column(
            children: [
              TabBar(
                  indicatorColor: Colors.indigoAccent,
                  unselectedLabelColor: Colors.black,
                  labelColor: Colors.indigoAccent,
                  tabs: [
                    Tab(
                      child: Text('Visistor'),
                    ),
                    Tab(
                      child: Text('Agent'),
                    )
                  ]),
              Expanded(
                child: TabBarView(children: [
                  VisitorPage(),
                  AgentPage(),
                ]),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
