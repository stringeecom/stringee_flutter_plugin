import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  late StreamController<dynamic> nativeEvents;
  late List<MethodCall> calls;
  late StringeeClient client;

  final userJson = <String, dynamic>{
    'user': 'user-1',
    'displayName': 'User One',
    'avatarUrl': 'avatar',
    'role': 'member',
  };
  final messageJson = <String, dynamic>{
    'id': 'message-1',
    'localId': 'local-1',
    'convId': 'conv-1',
    'senderId': 'user-1',
    'createdAt': 100,
    'sequence': 2,
    'state': MsgState.sent.index,
    'type': MsgType.text.value,
    'customData': {'source': 'test'},
    'content': {'content': 'hello'},
  };
  late Map<String, dynamic> conversationJson;

  Map<String, dynamic> success({dynamic body}) => {
        'status': true,
        'code': 0,
        'message': 'Success',
        if (body != null) 'body': body,
      };

  setUp(() async {
    conversationJson = <String, dynamic>{
      'id': 'conv-1',
      'name': 'Support',
      'isGroup': true,
      'creator': 'creator',
      'createdAt': 1,
      'updatedAt': 2,
      'totalUnread': 3,
      'text': {'text': 'last message'},
      'lastMsgId': 'last-1',
      'lastMsgType': MsgType.text.value,
      'lastMsgSender': 'user-1',
      'lastMsgSeqReceived': 5,
      'lastMsgState': MsgState.read.index,
      'lastTimeNewMsg': 6,
      'pinnedMsgId': 'pinned-1',
      'participants': [userJson],
      'oaId': 'oa-1',
      'customData': 'custom',
    };
    nativeEvents = StreamController<dynamic>.broadcast();
    StringeeClient.broadcastStream = nativeEvents.stream;
    calls = <MethodCall>[];
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel,
        (MethodCall call) async {
      calls.add(call);
      switch (call.method) {
        case 'createLiveChatConversation':
        case 'createConversation':
        case 'getConversationById':
        case 'getConversationByUserId':
          return success(body: Map<String, dynamic>.from(conversationJson));
        case 'getLocalConversations':
        case 'getLastConversation':
        case 'getConversationsBefore':
        case 'getConversationsAfter':
          return success(body: [Map<String, dynamic>.from(conversationJson)]);
        case 'addParticipants':
        case 'removeParticipants':
          return success(body: [userJson]);
        case 'getMessages':
        case 'getLocalMessages':
        case 'getLastMessages':
        case 'getMessagesAfter':
        case 'getMessagesBefore':
          return success(body: [messageJson]);
        default:
          return success();
      }
    });
    client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
  });

  tearDown(() async {
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel, null);
    await nativeEvents.close();
  });

  test('outgoing message constructors serialize every supported payload', () {
    final messages = <StringeeMessage>[
      StringeeMessage.typeText(client, ' text ', customData: {'a': 1}),
      StringeeMessage.typePhoto(
        client,
        ' photo.jpg ',
        thumbnail: ' thumb ',
        ratio: 1.5,
        customData: {'a': 1},
      ),
      StringeeMessage.typeVideo(
        client,
        ' video.mp4 ',
        3.5,
        thumbnail: ' thumb ',
        ratio: 1.7,
        customData: {'a': 1},
      ),
      StringeeMessage.typeAudio(
        client,
        ' audio.mp3 ',
        2.5,
        customData: {'a': 1},
      ),
      StringeeMessage.typeFile(
        client,
        ' file.pdf ',
        fileName: ' report.pdf ',
        fileLength: 99,
        customData: {'a': 1},
      ),
      StringeeMessage.typeLink(client, ' https://example.com '),
      StringeeMessage.typeLocation(client, 10.5, 106.7),
      StringeeMessage.typeContact(client, ' vcard '),
      StringeeMessage.typeSticker(client, ' category ', ' sticker '),
    ];

    for (final message in messages) {
      message.convId = ' conv-1 ';
      expect(message.toJson()['convId'], 'conv-1');
      expect(message.toString(), contains('convId'));
    }
    expect(messages[0].toJson()['text'], 'text');
    expect(messages[1].toJson(), containsPair('filePath', 'photo.jpg'));
    expect(messages[2].toJson(), containsPair('duration', 3.5));
    expect(messages[3].toJson(), containsPair('duration', 2.5));
    expect(messages[4].toJson(), containsPair('filename', 'report.pdf'));
    expect(messages[6].toJson(), containsPair('lat', 10.5));
    expect(messages[7].toJson(), containsPair('vcard', 'vcard'));
  });

  test('fromJson parses all message content and notification variants', () {
    Map<String, dynamic> payload(int type, Map<String, dynamic> content) => {
          ...messageJson,
          'type': type,
          'content': content,
        };

    final messages = <StringeeMessage>[
      StringeeMessage.fromJson(
        payload(MsgType.text.value, {'content': 'text'}),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.link.value, {'content': 'link'}),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.createConversation.value, {
          'groupName': 'Group',
          'creator': 'creator',
          'participants': [userJson, 'user-2', null],
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.photo.value, {
          'photo': {
            'filePath': 'photo.jpg',
            'fileUrl': 'photo-url',
            'thumbnail': 'thumb',
            'ratio': '1.5',
          }
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.video.value, {
          'video': {
            'filePath': 'video.mp4',
            'fileUrl': 'video-url',
            'thumbnail': 'thumb',
            'ratio': 1.7,
            'duration': '3.5',
          }
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.audio.value, {
          'audio': {
            'filePath': 'audio.mp3',
            'fileUrl': 'audio-url',
            'duration': 2,
          }
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.file.value, {
          'file': {
            'filePath': 'file.pdf',
            'fileUrl': 'file-url',
            'fileName': 'file.pdf',
            'fileLength': '50',
          }
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.location.value, {
          'location': {'lat': '10.5', 'lon': 106.7}
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.contact.value, {
          'contact': {'vcard': 'vcard'}
        }),
        client,
      ),
      StringeeMessage.fromJson(
        payload(MsgType.sticker.value, {
          'sticker': {'name': 'smile', 'category': 'faces'}
        }),
        client,
      ),
      for (final notification in <Map<String, dynamic>>[
        {
          'type': 1,
          'addedInfo': userJson,
          'participants': [userJson]
        },
        {
          'type': 2,
          'removedInfo': 'user-2',
          'participants': [userJson]
        },
        {'type': 3, 'groupName': 'Renamed'},
      ])
        StringeeMessage.fromJson(
          payload(MsgType.notification.value, notification),
          client,
        ),
    ];

    expect(messages[0].text, 'text');
    expect(messages[2].notiContent!['participants'], hasLength(2));
    expect(messages[3].fileUrl, 'photo-url');
    expect(messages[4].duration, 3.5);
    expect(messages[5].filePath, 'audio.mp3');
    expect(messages[6].fileLength, 50);
    expect(messages[7].longitude, 106.7);
    expect(messages[8].vcard, 'vcard');
    expect(messages[9].stickerCategory, 'faces');
    expect(messages[10].notiContent!['type'], MsgNotifyType.addParticipants);
    expect(messages[11].notiContent!['type'], MsgNotifyType.removeParticipants);
    expect(messages[12].notiContent!['groupName'], 'Renamed');
  });

  test('last-message parser covers every content type', () {
    final contentByType = <MsgType, Map<String, dynamic>>{
      MsgType.text: {
        'text': 'text',
        'metadata': {'a': 1}
      },
      MsgType.link: {'text': 'link'},
      MsgType.createConversation: {
        'groupName': 'Group',
        'creator': 'creator',
        'participants': [userJson, 'user-2'],
      },
      MsgType.renameConversation: {
        'groupName': 'Renamed',
        'creator': 'creator',
        'participants': [userJson],
      },
      MsgType.photo: {
        'photo': {
          'filePath': 'photo',
          'fileUrl': 'url',
          'thumbnail': 'thumb',
          'ratio': 1.5,
        }
      },
      MsgType.video: {
        'video': {
          'filePath': 'video',
          'fileUrl': 'url',
          'thumbnail': 'thumb',
          'ratio': 1.5,
          'duration': 2,
        }
      },
      MsgType.audio: {
        'audio': {'filePath': 'audio', 'fileUrl': 'url', 'duration': 2}
      },
      MsgType.file: {
        'file': {
          'filePath': 'file',
          'fileUrl': 'url',
          'fileName': 'name',
          'fileLength': 10,
        }
      },
      MsgType.location: {
        'location': {'lat': 10, 'lon': 20}
      },
      MsgType.contact: {
        'contact': {'vcard': 'vcard'}
      },
      MsgType.sticker: {
        'sticker': {'name': 'smile', 'category': 'faces'}
      },
    };

    for (final entry in contentByType.entries) {
      final message = StringeeMessage.lstMsg(
        'id',
        'conv',
        entry.key,
        'sender',
        1,
        MsgState.sent,
        2,
        entry.value,
      );
      expect(message.id, 'id');
      expect(message.type, entry.key);
    }
    for (final notification in <Map<String, dynamic>>[
      {
        'type': 1,
        'addedInfo': userJson,
        'participants': [userJson]
      },
      {
        'type': 2,
        'removedInfo': userJson,
        'participants': [userJson]
      },
      {'type': 3, 'groupName': 'Renamed'},
      {'type': 0},
    ]) {
      expect(
        StringeeMessage.lstMsg(
          'id',
          'conv',
          MsgType.notification,
          'sender',
          1,
          MsgState.sent,
          2,
          notification,
        ).type,
        MsgType.notification,
      );
    }
    expect(
      StringeeMessage.lstMsg(
        null,
        'conv',
        MsgType.text,
        null,
        null,
        MsgState.sent,
        null,
        null,
      ).id,
      isNull,
    );
  });

  test('conversation parses fields and forwards its complete API', () async {
    final conversation =
        StringeeConversation.fromJson(conversationJson, client);
    expect(conversation.id, 'conv-1');
    expect(conversation.name, 'Support');
    expect(conversation.isGroup, isTrue);
    expect(conversation.creator, 'creator');
    expect(conversation.createdAt, 1);
    expect(conversation.updatedAt, 2);
    expect(conversation.totalUnread, 3);
    expect(conversation.lastMsg!.id, 'last-1');
    expect(conversation.participants, hasLength(1));
    expect(conversation.toString(), contains('conv-1'));

    await conversation.sendChatTranscript(' email ', ' domain ');
    await conversation.endChat();
    await conversation.beginTyping();
    await conversation.endTyping();
    await conversation.delete();
    expect((await conversation.addParticipants(<StringeeUser>[]))['code'], -2);
    final user = StringeeUser(userId: 'user-2', name: 'User Two');
    expect((await conversation.addParticipants([user]))['body'], hasLength(1));
    expect(
        (await conversation.removeParticipants([user]))['body'], hasLength(1));
    await conversation.sendMessage(StringeeMessage.typeText(client, 'hello'));
    expect((await conversation.getMessages(<String>[]))['code'], -2);
    expect(
        (await conversation.getMessages(['message-1']))['body'], hasLength(1));
    expect((await conversation.getLocalMessages(1))['body'], hasLength(1));
    expect((await conversation.getLastMessages(1))['body'], hasLength(1));
    expect((await conversation.getMessagesAfter(1, 0))['body'], hasLength(1));
    expect((await conversation.getMessagesBefore(1, 0))['body'], hasLength(1));
    await conversation.updateConversation(' New name ');
    await conversation.setRole(' user-2 ', UserRole.admin);
    await conversation.deleteMessages(['message-1']);
    await conversation.revokeMessages(['message-1'], true);
    await conversation.markAsRead();

    expect((await conversation.getLocalMessages(0))['code'], -2);
    expect((await conversation.getMessagesAfter(0, 0))['code'], -2);
    expect((await conversation.getMessagesAfter(1, -1))['code'], -2);
    expect((await conversation.getMessagesBefore(0, 0))['code'], -2);
    expect((await conversation.updateConversation(''))['code'], -2);
    expect((await conversation.setRole('', UserRole.member))['code'], -2);
    expect((await conversation.deleteMessages(<String>[]))['code'], -2);
    expect((await conversation.revokeMessages(<String>[], false))['code'], -2);

    final parsedMessage = StringeeMessage.fromJson(messageJson, client);
    await parsedMessage.edit('edited');
    await parsedMessage.pinOrUnPin(true);
    expect(
      calls.map((item) => item.method),
      containsAll(
          <String>['sendMessage', 'editMsg', 'pinOrUnPin', 'markAsRead']),
    );
  });

  test('chat API validates, maps conversations and emits change events',
      () async {
    final chat = StringeeChat(client);
    final changes = <Map<dynamic, dynamic>>[];
    final subscription = chat.eventStreamController.stream.listen(
      (dynamic event) => changes.add(event as Map<dynamic, dynamic>),
    );

    expect((await chat.getChatProfile(''))['code'], -2);
    await chat.getChatProfile(' key ');
    expect((await chat.getLiveChatToken('', 'name', 'email'))['code'], -2);
    expect((await chat.getLiveChatToken('key', '', 'email'))['code'], -2);
    expect((await chat.getLiveChatToken('key', 'name', ''))['code'], -2);
    await chat.getLiveChatToken(' key ', ' name ', ' email ');
    await chat.updateUserInfo(
      name: ' name ',
      email: ' email ',
      avatar: ' avatar ',
      phone: ' phone ',
    );
    expect((await chat.createLiveChatConversation(''))['code'], -2);
    expect(
      (await chat.createLiveChatConversation('queue'))['body'],
      isA<StringeeConversation>(),
    );
    expect(
        (await chat.createLiveChatTicket('', 'name', 'email', ''))['code'], -2);
    await chat.createLiveChatTicket('key', 'name', 'email', ' description ');
    expect(
      (await chat.createConversation(
        StringeeConversationOption(isGroup: false, isDistinct: true),
        <StringeeUser>[],
      ))['code'],
      -2,
    );
    final created = await chat.createConversation(
      StringeeConversationOption(isGroup: true, isDistinct: false),
      [StringeeUser(userId: 'user-1')],
    );
    expect(created['body'], isA<StringeeConversation>());
    expect((await chat.getConversationById(''))['code'], -2);
    expect((await chat.getConversationById('conv-1'))['body'],
        isA<StringeeConversation>());
    expect((await chat.getConversationByUserId(''))['code'], -2);
    expect((await chat.getConversationByUserId('user-1'))['body'],
        isA<StringeeConversation>());
    expect(
        (await chat.getLocalConversations(oaId: 'oa'))['body'], hasLength(1));
    expect((await chat.getLastConversation(0))['code'], -2);
    expect(
        (await chat.getLastConversation(1, oaId: 'oa'))['body'], hasLength(1));
    expect((await chat.getConversationsBefore(0, 1))['code'], -2);
    expect((await chat.getConversationsBefore(1, 0))['code'], -2);
    expect((await chat.getConversationsBefore(1, 2))['body'], hasLength(1));
    expect((await chat.getConversationsAfter(0, 1))['code'], -2);
    expect((await chat.getConversationsAfter(1, 0))['code'], -2);
    expect((await chat.getConversationsAfter(1, 2))['body'], hasLength(1));
    await chat.clearDb();
    await chat.getTotalUnread();
    await chat.joinOaConversation('conv-1');

    void emit(ObjectType type, dynamic object) {
      nativeEvents.add({
        'nativeEventType': StringeeObjectEventType.chat.index,
        'uuid': client.uuid,
        'event': 'didReceiveChangeEvent',
        'body': {
          'changeType': ChangeType.insert.index,
          'objectType': type.index,
          'objects': [object],
        },
      });
    }

    emit(ObjectType.conversation, conversationJson);
    emit(ObjectType.message, messageJson);
    await Future<void>.delayed(Duration.zero);
    expect(changes, hasLength(2));
    expect(changes.first['body'], isA<StringeeObjectChange>());
    expect(
      (changes.first['body'] as StringeeObjectChange).objects!.first,
      isA<StringeeConversation>(),
    );
    expect(
      (changes.last['body'] as StringeeObjectChange).objects!.first,
      isA<StringeeMessage>(),
    );

    await subscription.cancel();
    chat.destroy();
  });

  test('chat request parses values and accepts or rejects', () async {
    final request = StringeeChatRequest({
      'convId': 12,
      'customerId': 'customer',
      'customerName': 'Customer',
      'channelType': StringeeChannelType.facebook.index,
      'type': StringeeChatRequestType.transfer.index,
    }, client);
    expect(request.convId, '12');
    expect(request.customerId, 'customer');
    expect(request.customerName, 'Customer');
    expect(request.channelType, StringeeChannelType.facebook);
    expect(request.type, StringeeChatRequestType.transfer);
    await request.accept();
    await request.reject();

    final invalid = StringeeChatRequest(const {}, client);
    expect((await invalid.accept())['code'], -2);
    expect((await invalid.reject())['code'], -2);

    expect(
      calls.map((item) => item.method),
      containsAll(<String>['acceptChatRequest', 'rejectChatRequest']),
    );
  });
}
