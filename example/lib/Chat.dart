import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ConversationInfor.dart';

StringeeClient _client;

class Chat extends StatefulWidget {
  Chat({@required StringeeClient client}) {
    _client = client;
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ChatState();
  }
}

class ChatState extends State<Chat> {
  List<String> _log;
  List<StringeeConversation> _conversations;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    _log = new List();
    _conversations = new List();

    _client.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeClientEvents &&
          map['eventType'] == StringeeClientEvents.DidReceiveChange) {
        StringeeChange stringeeChange = map['body'];
        if (stringeeChange.objectType == ObjectType.CONVERSATION) {
          StringeeConversation conversation = stringeeChange.object;
          setState(() {
            _log.add(conversation.id + ' ' + stringeeChange.changeType.toString());
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
                              'convId: ' + _conversations[index].id,
                            ),
                            Text(
                              'name: ' + _conversations[index].name,
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
                            List<User> participants = new List();
                            User user1 = User(userId: 'ACTXV7BTAP', name: 'Okumura Rin');
                            User user2 = User(userId: 'ACX3H6EJHW', name: 'Kỳ ANh');
                            participants.add(user1);
                            participants.add(user2);

                            ConversationOption options =
                                ConversationOption(name: 'Test', isGroup: true, isDistinct: false);

                            _client.createConversation(options, participants).then((value) {
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
                            _client
                                .getConversationById('conv-vn-1-73JJ5R8BMN-1606410119987')
                                .then((value) {
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
                              _client.getConversationByUserId('ACTXV7BTAP').then((value) {
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
                              _client
                                  .getConversationFromServer('conv-vn-1-73JJ5R8BMN-1606410119987')
                                  .then((value) {
                                setState(() {
                                  _log.add('Get Conversation from Server: msg:' + value['message']);
                                  if (value['status']) {
                                    _conversations.clear();
                                    _conversations.add(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Conversation from Server',
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
                              _client.getLocalConversations().then((value) {
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
                              _client.getLastConversation(3).then((value) {
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
                              _client.getConversationsBefore(3, 1609952400000).then((value) {
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
                              _client.getConversationsAfter(3, 1609952400000).then((value) {
                                setState(() {
                                  _log.add('Get Conversation after: msg:' + value['message']);
                                  if (value['status']) {
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
                              _client.clearDb().then((value) {
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
                              _client.blockUser('assss').then((value) {
                                setState(() {
                                  _log.add('Block user: msg:' + value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Block user',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: 20.0, bottom: 20.0),
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
                              _client.getTotalUnread().then((value) {
                                setState(() {
                                  _log.add('Get total unread:' + value['message']);
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
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
