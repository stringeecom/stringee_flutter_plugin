import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'main.dart' as main;

StringeeClient _client;
StringeeConversation _conversation;

class ConversationInfor extends StatefulWidget {
  ConversationInfor(
      {StringeeClient client, StringeeConversation conversation}) {
    _client = client;
    _conversation = conversation;
  }

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ConversationInforState();
  }
}

class ConversationInforState extends State<ConversationInfor> {
  List<String> _log;
  List<StringeeMessage> _messages;
  List<StringeeUser> users;
  StringeeMessage msg;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    _log = [];
    _messages = [];

    _conversation.getLastMessages(50).then((value) {
      print(value.toString());
      setState(() {
        _log.add('Get last messages: msg:' + value['message']);
        if (value['status']) {
          _messages.clear();
          _messages.addAll(value['body']);
        }
      });
    });

    users = [];
    StringeeUser user1 = StringeeUser(userId: 'id1', name: 'user1');
    StringeeUser user2 = StringeeUser(userId: 'id2', name: 'user2');
    users.add(user1);
    users.add(user2);

    msg = StringeeMessage.typeText(main.client, 'test',
        customData: {'custom': 'abc'});

    _client.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['typeEvent'] == StringeeClientEvents &&
          map['eventType'] == StringeeClientEvents.didReceiveObjectChange) {
        StringeeObjectChange objectChange = map['body'];
        if (objectChange.objectType == ObjectType.message) {
          StringeeMessage message = objectChange.objects.first;
          setState(() {
            _log.add((message.id != null)
                ? message.id
                : 'null' + ' ' + objectChange.type.toString());
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
        title: Text("Conversation infor"),
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
              'Message',
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
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                return Container(
                  padding: EdgeInsets.only(top: 5.0, right: 5.0, left: 5.0),
                  child: Container(
                    alignment: Alignment.centerLeft,
                    child: GestureDetector(
                        onTap: () {
                          showMsgDialog(_messages[index]);
                        },
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'msgId: ' + _messages[index].id,
                            ),
                            Text(
                              'msgType: ' + _messages[index].type.toString(),
                            ),
                            Text(
                              'text: ' + _messages[index].text,
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
                            _conversation.delete().then((value) {
                              print(value.toString());
                              setState(() {
                                _log.add('Delete conversation: msg:' +
                                    value['message']);
                              });
                            });
                          },
                          child: Text(
                            'Delete Conversation',
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
                            _conversation.addParticipants(users).then((value) {
                              print(value.toString());
                              setState(() {
                                _log.add('Add participants: msg:' +
                                    value['message']);
                              });
                            });
                          },
                          child: Text(
                            'Add participants',
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
                              _conversation
                                  .removeParticipants(users)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Remove participants: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Remove participants',
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
                              _conversation.sendMessage(msg).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add(
                                      'Send message: msg:' + value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Send message',
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
                              _conversation.getMessages([
                                'msg-vn-1-MWE3BG0IJE-1610578358918',
                                'msg-vn-1-MWE3BG0IJE-1610578360615'
                              ]).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add(
                                      'Get messages: msg:' + value['message']);
                                  if (value['status']) {
                                    _messages.clear();
                                    _messages.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get messages',
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
                              _conversation.getLocalMessages(3).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get local Messages: msg:' +
                                      value['message']);
                                  if (value['status']) {
                                    _messages.clear();
                                    _messages.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get local Messages',
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
                              _conversation.getLastMessages(50).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get last Messages: msg:' +
                                      value['message']);
                                  if (value['status']) {
                                    _messages.clear();
                                    _messages.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get last Messages',
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
                              _conversation
                                  .getMessagesAfter(50, 4)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get Messages after: msg:' +
                                      value['message']);
                                  if (value['status']) {
                                    _messages.clear();
                                    _messages.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Messages after',
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
                              _conversation
                                  .getMessagesBefore(50, 4)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Get Messages before: msg:' +
                                      value['message']);
                                  if (value['status']) {
                                    _messages.clear();
                                    _messages.addAll(value['body']);
                                  }
                                });
                              });
                            },
                            child: Text(
                              'Get Messages before',
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
                              String newConvName =
                                  _conversation.name + ' NEW NAME';
                              _conversation
                                  .updateConversation(newConvName)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Update Conversation: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Update Conversation',
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
                              _conversation
                                  .setRole('id1', UserRole.member)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Set role: msg:' + value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Set role',
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
                              _conversation.deleteMessages(
                                  ['msgid1', 'msgid2']).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Delete messages: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Delete messages',
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
                              _conversation.revokeMessages(
                                  ['msgid1', 'msgid2'], true).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Revoke messages: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Revoke messages',
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
                              _conversation.markAsRead().then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Mark conversation as read: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: Text(
                              'Mark conversation as read',
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
          ),
        ],
      ),
    );
  }

  void showMsgDialog(StringeeMessage message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Message'),
        content: Text('msgId: ' + message.id),
        actions: [
          new FlatButton(
            color: Colors.grey[300],
            textColor: Colors.black,
            onPressed: () {
              message.edit('ok ok').then((value) {
                print(value.toString());
                setState(() {
                  _log.add('Edit messages: msg:' + value['message']);
                  Navigator.of(context, rootNavigator: true).pop();
                });
              });
            },
            child: Text(
              'Edit messages',
              textAlign: TextAlign.center,
            ),
          ),
          new FlatButton(
            color: Colors.grey[300],
            textColor: Colors.black,
            onPressed: () {
              message.pinOrUnPin(false).then((value) {
                print(value.toString());
                setState(() {
                  _log.add('Pin/UnPin messages: msg:' + value['message']);
                  Navigator.of(context, rootNavigator: true).pop();
                });
              });
            },
            child: Text(
              'Pin/UnPin messages',
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
      barrierDismissible: true,
    );
  }
}
