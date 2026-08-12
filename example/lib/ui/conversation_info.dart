import 'package:flutter/material.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class ConversationInfor extends StatefulWidget {
  final StringeeConversation _conversation;
  final StringeeClient _client;
  final StringeeChat _chat;

  const ConversationInfor(
    this._client,
    this._chat,
    this._conversation, {
    Key? key,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return ConversationInforState();
  }
}

class ConversationInforState extends State<ConversationInfor> {
  final List<String> _log = [];
  final List<StringeeMessage> _messages = [];
  List<StringeeUser> users = [];
  late StringeeMessage msg;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    widget._conversation.getLastMessages(50).then((value) {
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

    msg = StringeeMessage.typeText(widget._client, 'test',
        customData: {'custom': 'abc'});

    widget._chat.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      if (map['eventType'] == StringeeChatEvents.didReceiveObjectChange) {
        StringeeObjectChange objectChange = map['body'];
        if (objectChange.objectType == ObjectType.message) {
          StringeeMessage message = objectChange.objects!.first;
          setState(() {
            _log.add((message.id != null)
                ? message.id!
                : 'null ${objectChange.type}');
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
      appBar: AppBar(
        title: const Text("Conversation infor"),
        backgroundColor: Colors.indigo[600],
      ),
      body: Column(
        children: [
          Container(
            alignment: Alignment.topLeft,
            margin: const EdgeInsets.only(top: 5.0, left: 5.0),
            child: const Text(
              'Log',
              style: TextStyle(
                color: Colors.black,
                fontSize: 20.0,
              ),
            ),
          ),
          Container(
            margin: const EdgeInsets.all(10.0),
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
                  margin:
                      const EdgeInsets.only(top: 5.0, right: 5.0, left: 5.0),
                  child: Text(
                    _log[index],
                    style: const TextStyle(
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
            margin: const EdgeInsets.only(left: 5.0),
            child: const Text(
              'Message',
              style: TextStyle(
                color: Colors.black,
                fontSize: 20.0,
              ),
            ),
          ),
          Container(
            margin: const EdgeInsets.all(10.0),
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
                  padding:
                      const EdgeInsets.only(top: 5.0, right: 5.0, left: 5.0),
                  decoration: const BoxDecoration(
                    border: Border(
                      bottom: BorderSide(
                        color: Colors.black,
                        width: 2.0,
                      ),
                    ),
                  ),
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
                              'msgId: ${_messages[index].id!}',
                            ),
                            Text(
                              'msgType: ${_messages[index].type}',
                            ),
                            Text(
                              'text: ${_messages[index].text!}',
                            ),
                          ],
                        )),
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
                      SizedBox(
                        height: 40.0,
                        width: 175.0,
                        child: ElevatedButton(
                          onPressed: () {
                            widget._conversation.delete().then((value) {
                              print(value.toString());
                              setState(() {
                                _log.add('Delete conversation: msg:' +
                                    value['message']);
                              });
                            });
                          },
                          child: const Text(
                            'Delete Conversation',
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                      SizedBox(
                        height: 40.0,
                        width: 175.0,
                        child: ElevatedButton(
                          onPressed: () {
                            widget._conversation
                                .addParticipants(users)
                                .then((value) {
                              print(value.toString());
                              setState(() {
                                _log.add('Add participants: msg:' +
                                    value['message']);
                              });
                            });
                          },
                          child: const Text(
                            'Add participants',
                            textAlign: TextAlign.center,
                          ),
                        ),
                      )
                    ],
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
                                  .removeParticipants(users)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Remove participants: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Remove participants',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
                                  .sendMessage(msg)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add(
                                      'Send message: msg:' + value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Send message',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation.getMessages([
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
                            child: const Text(
                              'Get messages',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
                                  .getLocalMessages(3)
                                  .then((value) {
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
                            child: const Text(
                              'Get local Messages',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
                                  .getLastMessages(50)
                                  .then((value) {
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
                            child: const Text(
                              'Get last Messages',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
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
                            child: const Text(
                              'Get Messages after',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
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
                            child: const Text(
                              'Get Messages before',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              String newConvName =
                                  '${widget._conversation.name!} NEW NAME';
                              widget._conversation
                                  .updateConversation(newConvName)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Update Conversation: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Update Conversation',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation
                                  .setRole('id1', UserRole.member)
                                  .then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Set role: msg:' + value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Set role',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation.deleteMessages(
                                  ['msgid1', 'msgid2']).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Delete messages: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Delete messages',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 20.0, bottom: 20.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      mainAxisSize: MainAxisSize.max,
                      children: [
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation.revokeMessages(
                                  ['msgid1', 'msgid2'], true).then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Revoke messages: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: const Text(
                              'Revoke messages',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        ),
                        SizedBox(
                          height: 40.0,
                          width: 175.0,
                          child: ElevatedButton(
                            onPressed: () {
                              widget._conversation.markAsRead().then((value) {
                                print(value.toString());
                                setState(() {
                                  _log.add('Mark conversation as read: msg:' +
                                      value['message']);
                                });
                              });
                            },
                            child: const Text(
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
        title: const Text('Message'),
        content: Text('msgId: ${message.id!}'),
        actions: [
          ElevatedButton(
            onPressed: () {
              message.edit('ok ok').then((value) {
                print(value.toString());
                setState(() {
                  _log.add('Edit messages: msg:' + value['message']);
                  Navigator.of(context, rootNavigator: true).pop();
                });
              });
            },
            child: const Text(
              'Edit messages',
              textAlign: TextAlign.center,
            ),
          ),
          ElevatedButton(
            onPressed: () {
              message.pinOrUnPin(false).then((value) {
                print(value.toString());
                setState(() {
                  _log.add('Pin/UnPin messages: msg:' + value['message']);
                  Navigator.of(context, rootNavigator: true).pop();
                });
              });
            },
            child: const Text(
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
