import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

StringeeClient client = new StringeeClient();
StringeeChat chat = new StringeeChat(client);

class VisitorPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _VisitorPageState();
  }
}

class _VisitorPageState extends State<VisitorPage>
    with AutomaticKeepAliveClientMixin<VisitorPage> {
  String key = '';
  String queueId = '';

  String userId = 'Not connected...';

  String visitorName = '';
  String visitorEmail = '';
  String visitorPhone = '';

  late StringeeConversation _conversation;

  bool connected = false;
  bool inConv = false;

  List<String> _log = [];

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

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
        case StringeeClientEvents.timeoutInQueue:
          setState(() {
            _log.add('Time out chat in queue convId: ${map['body']['convId']}');
            inConv = false;
          });
          break;
        case StringeeClientEvents.conversationEnded:
          setState(() {
            _log.add('Conversation ended convId: ${map['body']['convId']}');
            inConv = false;
          });
          break;
        default:
          break;
      }
    });

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
        if (objectChange.objectType == ObjectType.message) {
          StringeeMessage message = objectChange.objects!.first;
          setState(() {
            _log.add((message.id != null)
                ? message.id!
                : 'null' + ' ' + objectChange.type.toString());
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget connectWidget = SingleChildScrollView(
      child: new Container(
        padding: EdgeInsets.only(left: 20.0, right: 20.0, top: 20.0),
        child: new Column(
          children: <Widget>[
            Container(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    'Name: ',
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                  ),
                  Flexible(
                    child: TextField(
                      onChanged: (String value) {
                        setState(() {
                          visitorName = value;
                        });
                      },
                      style: new TextStyle(
                        color: Colors.black,
                        fontSize: 18.0,
                      ),
                      decoration:
                          InputDecoration.collapsed(hintText: 'Enter name'),
                    ),
                  )
                ],
              ),
            ),
            Divider(
              color: Colors.black,
            ),
            Container(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    'Email: ',
                    style: new TextStyle(
                      color: Colors.black,
                      fontSize: 18.0,
                    ),
                  ),
                  Flexible(
                    child: TextField(
                      onChanged: (String value) {
                        setState(() {
                          visitorEmail = value;
                        });
                      },
                      style: new TextStyle(
                        color: Colors.black,
                        fontSize: 18.0,
                      ),
                      decoration:
                          InputDecoration.collapsed(hintText: 'Enter email'),
                    ),
                  )
                ],
              ),
            ),
            Divider(
              color: Colors.black,
            ),
            Container(
              height: 40.0,
              width: 175.0,
              margin: EdgeInsets.only(top: 20.0),
              child: new ElevatedButton(
                onPressed: () {
                  connect();
                },
                child: Text('Connect'),
              ),
            ),
          ],
        ),
      ),
    );

    Widget liveChatAction = Row(
      children: [
        new Expanded(
          child: Container(
            margin: EdgeInsets.only(right: 10.0),
            child: new ElevatedButton(
              onPressed: () {
                StringeeMessage msg = StringeeMessage.typeText(
                  client,
                  'test',
                );
                _conversation.sendMessage(msg).then((value) {
                  setState(() {
                    _log.add('Send message: msg:' + value['message']);
                  });
                });
              },
              child: Text(
                'Send message',
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ),
        new Expanded(
          child: Container(
            margin: EdgeInsets.only(left: 10.0),
            child: new ElevatedButton(
              onPressed: () {
                _conversation.endChat().then((value) {
                  setState(() {
                    _log.add('End chat: msg:' + value['message']);
                    if (value['status']) {
                      inConv = false;
                    }
                  });
                });
              },
              child: Text(
                'End chat',
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ),
      ],
    );

    Widget chatWidget = SingleChildScrollView(
      child: new Container(
        padding: EdgeInsets.only(left: 20.0, right: 20.0, top: 20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              child: new Text(
                'Connected as: $userId',
                style: new TextStyle(
                  color: Colors.black,
                  fontSize: 20.0,
                ),
              ),
            ),
            Container(
              padding: EdgeInsets.only(top: 20.0),
              alignment: Alignment.topLeft,
              child: Text(
                'Log',
                style: TextStyle(
                  color: Colors.black,
                  fontSize: 20.0,
                ),
              ),
            ),
            Container(
              margin: EdgeInsets.only(top: 20.0),
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
            Divider(
              color: Colors.black,
            ),
            inConv
                ? liveChatAction
                : Center(
                    child: Container(
                      child: new ElevatedButton(
                        onPressed: () {
                          chat
                              .createLiveChatConversation(queueId)
                              .then((value) {
                            if (value['status']) {
                              setState(() {
                                _log.add('Create Live Chat Conversation: msg:' +
                                    value['message']);
                                inConv = true;
                                _conversation = value['body'];
                              });
                            }
                          });
                        },
                        child: Text(
                          'Create Live Chat Conversation',
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                  ),
          ],
        ),
      ),
    );
    return new Scaffold(
      resizeToAvoidBottomInset: false,
      body: new Form(
        child: connected
            ? chatWidget
            : Center(
                child: connectWidget,
              ),
      ),
    );
  }

  void handleDidConnectEvent() {
    setState(() {
      userId = client.userId!;
      connected = true;
    });

    /// if you want to change user info then use this fucntion
    client
        .updateUserInfo(StringeeUser.forUpdate(
            name: "new name",
            email: "new email",
            avatarUrl: "new avatar url",
            phone: "146845641565"))
        .then((value) {
      bool status = value['status'];
      print("updateUserInfo: " + status.toString());
      if (status) {}
    });
  }

  void handleDiddisconnectEvent() {
    setState(() {
      userId = 'Not connected';
      connected = false;
    });
  }

  void handleDidFailWithErrorEvent(int code, String message) {
    print('code: ' + code.toString() + '\nmessage: ' + message);
  }

  void connect() {
    /// Get chat profile
    chat.getChatProfile(key).then((value) {
      print("getChatProfile 12345: " + value.toString());
      bool status = value['status'];
      if (status) {
        List queueList = value['body']['queues'];
        setState(() {
          queueId = queueList[0]['id'];
        });

        chat.getLiveChatToken(key, visitorName, visitorEmail).then((value) {
          print("getLiveChatToken: " + value.toString());

          bool status = value['status'];
          if (status) {
            String token = value['body'];
            client.connect(token);
          }
        });
      }
    });
  }

  @override
  // TODO: implement wantKeepAlive
  bool get wantKeepAlive => true;
}
