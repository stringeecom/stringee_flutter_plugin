import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ConversationInfor.dart';

StringeeClient? _client;

class Chat extends StatefulWidget {
  Chat({required StringeeClient client}) {
    _client = client;
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ChatState();
  }
}

class ChatState extends State<Chat> {
  late List<String> _log;
  late List<StringeeConversation?> _conversations;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    _log = [];
    _conversations = [];

    _client!.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeClientEvents &&
          map['eventType'] == StringeeClientEvents.didReceiveObjectChange) {
        StringeeObjectChange objectChange = map['body'];
        if (objectChange.objectType == ObjectType.conversation) {
          StringeeConversation? conversation = objectChange.objects!.first;
          setState(() {
            _log.add(conversation!.id! + ' ' + objectChange.type.toString());
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new Scaffold(
      appBar: AppBar(
        title: Text("Chat"),
        backgroundColor: Colors.indigo[600],
      ),
      body: Column(
        children: [
          Container(
            alignment: Alignment.topLeft,
            margin: EdgeInsets.only(top: 5.0, left: 5.0),
            child: Text(
              'Log',
              style: TextStyle(
                color: Colors.black,
                fontSize: 20.0,
              ),
            ),
          ),
          Container(
            margin: EdgeInsets.all(10.0),
            decoration: BoxDecoration(
              border: Border.all(
                color: Colors.black,
                width: 2,
              ),
            ),
            height: 150.0,
            child: ListView.builder(
              itemCount: _log.length,
              shrinkWrap: true,
              itemBuilder: (context, index) {
                return Container(
                  margin: EdgeInsets.only(top: 5.0, right: 5.0, left: 5.0),
                  child: Text(
                    _log[index],
                    style: TextStyle(
                      color: Colors.black,
                      fontSize: 12.0,
                    ),
                  ),
                );
              },
            ),
          ),
          Container(
            alignment: Alignment.topLeft,
            margin: EdgeInsets.only(left: 5.0),
            child: Text(
              'Conversation',
              style: TextStyle(
                color: Colors.black,
                fontSize: 20.0,
              ),
            ),
          ),
          Container(
            margin: EdgeInsets.all(10.0),
            decoration: BoxDecoration(
              border: Border.all(
                color: Colors.black,
                width: 2,
              ),
            ),
            height: 150.0,
            child: ListView.builder(
              itemCount: _conversations.length,
              itemBuilder: (context, index) {
                return Container(
                  padding: EdgeInsets.only(top: 5.0, right: 5.0, left: 5.0),
                  child: Container(
                    alignment: Alignment.centerLeft,
                    child: GestureDetector(
                        onTap: () {
                          Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (context) => ConversationInfor(
                                        client: _client,
                                        conversation: _conversations[index],
                                      )));
                        },
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'convId: ' + _conversations[index]!.id!,
                            ),
                            Text(
                              'name: ' + _conversations[index]!.name!,
                            ),
                          ],
                        )),
                  ),
                  decoration: BoxDecoration(
                    border: Border(
                      bottom: BorderSide(
                        color: Colors.black,
                        width: 2.0,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                mainAxisSize: MainAxisSize.max,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    mainAxisSize: MainAxisSize.max,
                    children: [
                      new Container(
                        height: 40.0,
                        width: 175.0,
                        child: new RaisedButton(
                          color: Colors.grey[300],
                          textColor: Colors.black,
                          onPressed: () {
                            List<StringeeUser> participants = [];
                            StringeeUser user1 = StringeeUser(userId: 'id1', name: 'user1');
                            StringeeUser user2 = StringeeUser(userId: 'id2', name: 'user2');
                            participants.add(user1);
                            participants.add(user2);

                            StringeeConversationOption options = StringeeConversationOption(
                                name: 'name', isGroup: true, isDistinct: true);

                            _client!.createConversation(options, participants).then((value) {
                              print("Flutter - createConversation - result: " + value.toString());
                              setState(() {
                                _log.add('Create conversation: msg:' + value['message']);
                                if (value['status']) {
                                  _conversations.clear();
                                  _conversations.add(value['body']);
                                }
                              });
                            });
                          },
                          child: Text(
                            'Create Conversation',
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                      new Container(
                        height: 40.0,
                        width: 175.0,
                        child: new RaisedButton(
                          color: Colors.grey[300],
                          textColor: Colors.black,
                          onPressed: () {
                            _client!.getConversationById('convid').then((value) {
                              print("Flutter - getConversationById - result: " + value.toString());

                              setState(() {
                                _log.add('Get conversation by Id: msg:' + value['message']);
                                if (value['status']) {
                                  _conversations.clear();
                                  _conversations.add(value['body']);
                                }
                              });
                            });
                          },
                          child: Text(
                            'Get Conversation by Id',
                            textAlign: TextAlign.center,
                          ),
                        ),
                      )
                    ],
                  ),
                  Container(
                    margin: EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getConversationByUserId('id2').then((value) {
                                print("Flutter - getConversationByUserId - result: " +
                                    value.toString());
                                setState(() {
                                  _log.add('Get Conversation by UserId: msg:' + value['message']);
                                  if (value['status']) {
                                    _conversations.clear();
                                    _conversations.add(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Conversation by UserId',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getTotalUnread().then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get total unread: msg:' + value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Get total unread',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getLocalConversations().then((value) {
                                print("Flutter - getLocalConversations - result: " +
                                    value.toString());
                                setState(() {
                                  _log.add('Get local Conversation: msg:' + value['message']);
                                  if (value['status']) {
                                    _conversations.clear();
                                    _conversations.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get local Conversation',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getLastConversation(50).then((value) {
                                print(
                                    "Flutter - getLastConversation - result: " + value.toString());
                                setState(() {
                                  _log.add('Get last Conversation: msg:' + value['message']);
                                  if (value['status']) {
                                    _conversations.clear();
                                    _conversations.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get last Conversation',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getConversationsBefore(2, 1602215811388).then((value) {
                                print("Flutter - getConversationsBefore - result: " +
                                    value.toString());
                                setState(() {
                                  _log.add('Get Conversation before: msg:' + value['message']);
                                  if (value['status']) {
                                    _conversations.clear();
                                    _conversations.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Conversation before',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.getConversationsAfter(2, 1602215811388).then((value) {
                                print("Flutter - getConversationsAfter - result: " +
                                    value.toString());
                                setState(() {
                                  if (value['status']) {
                                    _log.add('Get Conversation after: msg:' + value['message']);
                                    _conversations.clear();
                                    _conversations.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Conversation after',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              _client!.clearDb().then((value) {
                                setState(() {
                                  _log.add('Clear database: msg:' + value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Clear database',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new RaisedButton(
                            color: Colors.grey[300],
                            textColor: Colors.black,
                            onPressed: () {
                              setState(() {
                                _conversations.clear();
                              });
                            },
                            child: Text(
                              'Clear Console',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
