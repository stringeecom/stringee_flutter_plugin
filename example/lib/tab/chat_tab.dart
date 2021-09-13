import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/ui/conversation_info.dart';

StringeeClient client = new StringeeClient();
StringeeChat chat = new StringeeChat(client);

class ChatTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ChatTabState();
  }
}

class ChatTabState extends State<ChatTab> {
  String myUserId = 'Not connected...';
  String token = '';

  List<String> _log = [];
  List<StringeeConversation> _conversations = [];

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
          break;
        case StringeeClientEvents.didReceiveCustomMessage:
          break;
        case StringeeClientEvents.incomingCall:
          break;
        case StringeeClientEvents.incomingCall2:
          break;
        default:
          break;
      }
    });

    /// Connect
    if (token.isNotEmpty) {
      client.connect(token);
    }

    chat.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['eventType'] == StringeeChatEvents.didReceiveObjectChange) {
        StringeeObjectChange objectChange = map['body'];
        if (objectChange.objectType == ObjectType.conversation) {
          StringeeConversation conversation = objectChange.objects.first;
          setState(() {
            _log.add(conversation.id + ' ' + objectChange.type.toString());
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
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
          Container(
            alignment: Alignment.topLeft,
            margin: EdgeInsets.only(top: 10.0, left: 10.0),
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
                  margin: EdgeInsets.only(top: 10.0, right: 10.0, left: 10.0),
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
            margin: EdgeInsets.only(left: 10.0),
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
                  padding: EdgeInsets.only(top: 10.0, right: 10.0, left: 10.0),
                  child: Container(
                    alignment: Alignment.centerLeft,
                    child: GestureDetector(
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => ConversationInfor(
                                client,
                                chat,
                                _conversations[index],
                              ),
                            ),
                          );
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
                            List<StringeeUser> participants = [];
                            StringeeUser user1 =
                                StringeeUser(userId: 'id1', name: 'user1');
                            StringeeUser user2 =
                                StringeeUser(userId: 'id2', name: 'user2');
                            participants.add(user1);
                            participants.add(user2);

                            StringeeConversationOption options =
                                StringeeConversationOption(
                                    name: 'name',
                                    isGroup: true,
                                    isDistinct: true);

                            chat
                                .createConversation(options, participants)
                                .then((value) {
                              print("Flutter - createConversation - result: " +
                                  value.toString());
                              setState(() {
                                _log.add('Create conversation: msg:' +
                                    value['message']);
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
                            chat.getConversationById('convid').then((value) {
                              print("Flutter - getConversationById - result: " +
                                  value.toString());

                              setState(() {
                                _log.add('Get conversation by Id: msg:' +
                                    value['message']);
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
                              chat.getConversationByUserId('id2').then((value) {
                                print(
                                    "Flutter - getConversationByUserId - result: " +
                                        value.toString());
                                setState(() {
                                  _log.add('Get Conversation by UserId: msg:' +
                                      value['message']);
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
                              chat.getTotalUnread().then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get total unread: msg:' +
                                      value['message']);
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
                              chat.getLocalConversations().then((value) {
                                print(
                                    "Flutter - getLocalConversations - result: " +
                                        value.toString());
                                setState(() {
                                  _log.add('Get local Conversation: msg:' +
                                      value['message']);
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
                              chat.getLastConversation(50).then((value) {
                                print(
                                    "Flutter - getLastConversation - result: " +
                                        value.toString());
                                setState(() {
                                  _log.add('Get last Conversation: msg:' +
                                      value['message']);
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
                              chat
                                  .getConversationsBefore(2, 1602215811388)
                                  .then((value) {
                                print(
                                    "Flutter - getConversationsBefore - result: " +
                                        value.toString());
                                setState(() {
                                  _log.add('Get Conversation before: msg:' +
                                      value['message']);
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
                              chat
                                  .getConversationsAfter(2, 1602215811388)
                                  .then((value) {
                                print(
                                    "Flutter - getConversationsAfter - result: " +
                                        value.toString());
                                setState(() {
                                  if (value['status']) {
                                    _log.add('Get Conversation after: msg:' +
                                        value['message']);
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
                              chat.clearDb().then((value) {
                                setState(() {
                                  _log.add('Clear database: msg:' +
                                      value['message']);
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

  @override
  // TODO: implement wantKeepAlive
  bool get wantKeepAlive => true;
}
