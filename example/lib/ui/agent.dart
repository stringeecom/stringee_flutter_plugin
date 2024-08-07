import 'package:flutter/material.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

StringeeClient client = new StringeeClient();
StringeeChat chat = new StringeeChat(client);

class AgentPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return AgentPageState();
  }
}

class AgentPageState extends State<AgentPage>
    with AutomaticKeepAliveClientMixin<AgentPage> {
  String token = '';
  String userId = 'Not connected...';

  late StringeeChatRequest? _chatRequest;
  late StringeeConversation _conversation;

  bool hasChatRequest = false;
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
        case StringeeClientEvents.didReceiveChatRequest:
          handleDidReceiveChatRequestEvent(event['body']);
          break;
        case StringeeClientEvents.didReceiveTransferChatRequest:
          handleDidReceiveTransferChatRequestEvent(event['body']);
          break;
        case StringeeClientEvents.timeoutAnswerChat:
          handleTimeoutAnswerChatEvent(event['body']);
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

    /// Connect
    if (token.isNotEmpty) {
      client.connect(token);
    }
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    Widget chatRequestAction = Row(
      children: [
        new Expanded(
          child: Container(
            margin: EdgeInsets.only(right: 10.0),
            child: new ElevatedButton(
              onPressed: () {
                _chatRequest!.accept().then((value) {
                  bool status = value['status'];
                  setState(() {
                    _log.add('Accept chat request: msg:' + value['message']);
                  });
                  if (status) {
                    chat
                        .getConversationById(_chatRequest!.convId)
                        .then((value) {
                      bool status = value['status'];
                      setState(() {
                        _log.add('get conversation: msg:' + value['message']);
                      });
                      if (status) {
                        setState(() {
                          _conversation = value['body'];
                          hasChatRequest = false;
                          inConv = true;
                        });
                      }
                    });
                  }
                });
              },
              child: Text(
                'Accept',
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
                _chatRequest!.reject().then((value) {
                  bool status = value['status'];
                  setState(() {
                    _log.add('Reject chat request: msg:' + value['message']);
                  });
                  if (status) {
                    setState(() {
                      hasChatRequest = false;
                    });
                  }
                });
              },
              child: Text(
                'Reject',
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ),
      ],
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

    return new Scaffold(
      body: new Form(
        child: SingleChildScrollView(
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
                        margin:
                            EdgeInsets.only(top: 10.0, right: 10.0, left: 10.0),
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
                Center(
                  child: hasChatRequest
                      ? chatRequestAction
                      : inConv
                          ? liveChatAction
                          : null,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void handleDidConnectEvent() {
    setState(() {
      userId = client.userId!;
    });
  }

  void handleDiddisconnectEvent() {
    setState(() {
      userId = 'Not connected';
    });
  }

  void handleDidFailWithErrorEvent(int code, String message) {
    print('code: ' + code.toString() + '\nmessage: ' + message);
  }

  void handleDidReceiveChatRequestEvent(StringeeChatRequest chatRequest) {
    setState(() {
      _chatRequest = chatRequest;
      hasChatRequest = true;
    });
  }

  void handleDidReceiveTransferChatRequestEvent(
      StringeeChatRequest chatRequest) {
    setState(() {
      _chatRequest = chatRequest;
      hasChatRequest = true;
    });
  }

  void handleTimeoutAnswerChatEvent(StringeeChatRequest chatRequest) {
    setState(() {
      _chatRequest = null;
      hasChatRequest = false;
    });
  }

  @override
  // TODO: implement wantKeepAlive
  bool get wantKeepAlive => true;
}
