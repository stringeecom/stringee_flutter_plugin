import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';
import 'package:stringee_flutter_plugin_example/bottom_sheet/sheet_input_one_row.dart';
import 'package:stringee_flutter_plugin_example/bottom_sheet/sheet_update_user.dart';
import 'package:stringee_flutter_plugin_example/ui/conversation_info.dart';

StringeeClient client = new StringeeClient(serverAddresses: [new StringeeServerAddress('test3.stringee.com', 9879)]);
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
  String token =
      'eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTS3ZWY1V0UkxvYUJ5YzdyRE9RVzRpVjMxZ1RqQm85V2xwLTE2NjM4MTUwMDciLCJpc3MiOiJTS3ZWY1V0UkxvYUJ5YzdyRE9RVzRpVjMxZ1RqQm85V2xwIiwiZXhwIjoxNjY2NDA3MDA3LCJ1c2VySWQiOiJ1c2VyMSJ9.OsVbiKRO85yi8QeEwX9BD9OLbMkMNAY4K9woeCyF1uY';

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
          handleDidDisconnectEvent();
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
          StringeeConversation conversation = objectChange.objects!.first;
          setState(() {
            _log.add(conversation.id! + ' ' + objectChange.type.toString());
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
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
                              'convId: ' + _conversations[index].id!,
                            ),
                            Text(
                              'name: ' + _conversations[index].name!,
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
                  Container(
                    margin: EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new ElevatedButton(
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
                                print(
                                    "Flutter - createConversation - result: " +
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
                          child: new ElevatedButton(
                            onPressed: () {
                              chat
                                  .getConversationById('getConversationById')
                                  .then((value) {
                                print(
                                    "Flutter - getConversationById - result: " +
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
                            onPressed: () {
                              chat.getUserInfo([client.userId!]).then((value) {
                                if (value['status']) {
                                  SheetUpdateUser sheet =
                                      new SheetUpdateUser(value['body'][0]);
                                  sheet.show(context, (userInfo) {
                                    chat
                                        .updateUserInfo2(userInfo)
                                        .then((value) {
                                      print(
                                          "Flutter - updateUserInfo - result: " +
                                              value.toString());
                                      setState(() {
                                        _log.add('Update user info: msg:' +
                                            value['message']);
                                      });
                                    });
                                  });
                                } else {
                                  setState(() {
                                    _log.add('Get user info: msg: ' +
                                        value['message']);
                                  });
                                }
                              });
                            },
                            child: Text(
                              'Update user info',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        new Container(
                          height: 40.0,
                          width: 175.0,
                          child: new ElevatedButton(
                            onPressed: () {
                              SheetInputOneRow sheet = new SheetInputOneRow(
                                  'User id', 'userId1, userId2', 'Get');
                              sheet.show(context, (value) {
                                if (value.length != 0) {
                                  List<String> userIds = value.split(',');
                                  chat.getUserInfo(userIds).then((value) {
                                    print("Flutter - getUserInfo - result: " +
                                        value.toString());
                                    setState(() {
                                      _log.add('Get user info: msg: ' +
                                          value['message'] +
                                          ' - body: ' +
                                          (value['status']
                                              ? value['body'].toString()
                                              : ''));
                                    });
                                  });
                                }
                              });
                            },
                            child: Text(
                              'Get user info',
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
                          child: new ElevatedButton(
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
                          child: new ElevatedButton(
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
      myUserId = client.userId!;
    });
  }

  void handleDidDisconnectEvent() {
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
