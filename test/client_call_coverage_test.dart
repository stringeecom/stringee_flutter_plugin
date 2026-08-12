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

  Map<String, dynamic> success({dynamic body}) => {
        'status': true,
        'code': 0,
        'message': 'Success',
        if (body != null) 'body': body,
      };

  setUp(() {
    nativeEvents = StreamController<dynamic>.broadcast();
    StringeeClient.broadcastStream = nativeEvents.stream;
    calls = <MethodCall>[];
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel,
        (MethodCall call) async {
      calls.add(call);
      if (call.method == 'makeCall' || call.method == 'makeCall2') {
        return {
          ...success(),
          'callInfo': {
            'callId': call.method == 'makeCall' ? 'call-1' : 'call-2',
            'serial': '10',
            'from': 'caller',
            'to': 'callee',
            'fromAlias': 'Caller',
            'toAlias': 'Callee',
            'isVideoCall': 1,
            'customDataFromYourServer': 'server-data',
            'callType': StringeeCallType.appToAppOutgoing.index,
          },
        };
      }
      if (call.method == 'existCall') {
        return success(body: {'exists': true});
      }
      return success();
    });
  });

  tearDown(() async {
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel, null);
    await nativeEvents.close();
  });

  test('client forwards commands and validates invalid arguments', () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);

    expect(await client.disconnect(), containsPair('status', true));
    expect(await client.registerPush(' token '), containsPair('status', true));
    expect(
      await client.registerPushAndDeleteOthers(
        ' token ',
        <String>['old.package'],
      ),
      containsPair('status', true),
    );
    expect(
      await client.unregisterPush(' token '),
      containsPair('status', true),
    );
    expect(
      await client.sendCustomMessage(' user ', {'hello': 'world'}),
      containsPair('status', true),
    );
    final exists = await client.existCall(' call-id ');
    expect(exists.status, isTrue);
    expect(exists.data, {'exists': true});

    expect((await client.registerPush(''))['code'], -2);
    expect(
        (await client.registerPushAndDeleteOthers('', <String>[]))['code'], -2);
    expect(
      (await client.registerPushAndDeleteOthers('token', <String>[]))['code'],
      -2,
    );
    expect((await client.unregisterPush(''))['code'], -2);
    expect((await client.sendCustomMessage('', const {}))['code'], -2);
    expect((await client.existCall('')).code, -2);

    final trustResult = await client.setTrustAllSsl(true);
    expect(trustResult['status'], isFalse);
    expect(trustResult['code'], -1);

    final methods = calls.map((call) => call.method).toList();
    expect(
      methods,
      containsAll(<String>[
        'disconnect',
        'registerPush',
        'registerPushAndDeleteOthers',
        'unregisterPush',
        'sendCustomMessage',
        'existCall',
      ]),
    );
    final register = calls.singleWhere((call) => call.method == 'registerPush');
    expect(register.arguments['deviceToken'], 'token');
  });

  test('client converts every native client event', () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
    final received = <Map<dynamic, dynamic>>[];
    final subscription = client.eventStreamController.stream.listen(
      (dynamic event) => received.add(event as Map<dynamic, dynamic>),
    );

    void emit(String event, dynamic body) {
      nativeEvents.add({
        'nativeEventType': StringeeObjectEventType.client.index,
        'uuid': client.uuid,
        'event': event,
        'body': body,
      });
    }

    emit('didConnect', {
      'userId': 123,
      'projectId': 'project',
      'isReconnecting': 1,
    });
    emit('didDisconnect', {
      'userId': 'user',
      'projectId': 'project',
      'isReconnecting': false,
    });
    emit('didFailWithError', {
      'userId': 'user',
      'code': '7',
      'message': 'failed',
    });
    emit('requestAccessToken', {'userId': 'user'});
    emit('didReceiveCustomMessage', {'hello': 'world'});
    emit('incomingCall', {'callId': 'incoming-1'});
    emit('incomingCall2', {'callId': 'incoming-2'});
    for (final event in <String>[
      'didReceiveChatRequest',
      'didReceiveTransferChatRequest',
      'timeoutAnswerChat',
    ]) {
      emit(event, {
        'convId': 'conv',
        'customerId': 'customer',
        'customerName': 'Customer',
      });
    }
    emit('timeoutInQueue', {'convId': 'conv'});
    emit('conversationEnded', {'convId': 'conv'});
    emit('userBeginTyping', {'convId': 'conv'});
    emit('userEndTyping', {'convId': 'conv'});
    await Future<void>.delayed(Duration.zero);

    expect(client.userId, 'user');
    expect(client.projectId, 'project');
    expect(client.hasConnected, isFalse);
    expect(client.isReconnecting, isFalse);
    expect(received, hasLength(14));
    expect(
      received.map((event) => event['eventType']),
      containsAll(StringeeClientEvents.values),
    );
    (received.singleWhere(
      (event) => event['eventType'] == StringeeClientEvents.incomingCall,
    )['body'] as StringeeCall)
        .destroy();
    (received.singleWhere(
      (event) => event['eventType'] == StringeeClientEvents.incomingCall2,
    )['body'] as StringeeCall2)
        .destroy();
    await subscription.cancel();
  });

  test('StringeeCall covers metadata, events, validation and controls',
      () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
    final call = StringeeCall(client);

    call.initCallInfo(null);
    expect((await call.makeCall({'from': '', 'to': 'callee'}))['code'], -2);
    expect(
      await call.makeCall({
        'from': ' caller ',
        'to': ' callee ',
        'customData': {'key': 'value'},
        'isVideoCall': true,
        'videoQuality': VideoQuality.fullHd,
      }),
      containsPair('status', true),
    );
    expect(call.id, 'call-1');
    expect(call.serial, 10);
    expect(call.fromAlias, 'Caller');
    expect(call.toAlias, 'Callee');
    expect(call.customDataFromYourServer, 'server-data');
    expect(call.callType, StringeeCallType.appToAppOutgoing);

    final events = <Map<dynamic, dynamic>>[];
    final subscription = call.eventStreamController.stream.listen(
      (dynamic event) => events.add(event as Map<dynamic, dynamic>),
    );
    void emit(String event, Map<String, dynamic> body) {
      nativeEvents.add({
        'nativeEventType': StringeeObjectEventType.call.index,
        'uuid': client.uuid,
        'event': event,
        'body': body,
      });
    }

    emit('didChangeSignalingState', {'callId': 'call-1', 'code': 2});
    emit('didChangeMediaState', {'callId': 'call-1', 'code': 0});
    emit('didReceiveCallInfo', {
      'callId': 'call-1',
      'info': {'a': 1}
    });
    emit('didHandleOnAnotherDevice', {'code': 4});
    emit('didReceiveLocalStream', {'callId': 'call-1'});
    emit('didReceiveRemoteStream', {'callId': 'call-1'});
    emit('didChangeMediaState', {'callId': 'another', 'code': 0});
    await Future<void>.delayed(Duration.zero);
    expect(events, hasLength(6));

    expect((await call.sendDtmf(''))['code'], -2);
    await call.initAnswer();
    await call.answer();
    await call.hangup();
    await call.reject();
    await call.sendDtmf(' 12# ');
    await call.sendCallInfo({'key': 'value'});
    await call.getCallStats();
    await call.mute(false);
    await call.enableVideo(true);
    await call.switchCamera();
    await call.switchCamera(cameraId: 'front');
    await call.resumeVideo();
    await call.setMirror(true, true);

    expect(
      calls.map((item) => item.method),
      containsAll(<String>[
        'initAnswer',
        'answer',
        'hangup',
        'reject',
        'sendDtmf',
        'sendCallInfo',
        'getCallStats',
        'mute',
        'enableVideo',
        'switchCamera',
        'resumeVideo',
        'setMirror',
      ]),
    );
    await subscription.cancel();
    call.destroy();
  });

  test('StringeeCall2 covers events and all native controls', () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
    final call = StringeeCall2.fromCallInfo(null, client);

    expect((await call.makeCall({'from': 'caller', 'to': ''}))['code'], -2);
    await call.makeCallFromParams(
      MakeCallParams(
        'caller',
        'callee',
        isVideoCall: true,
        customData: {'key': 'value'},
        videoQuality: VideoQuality.normal,
      ),
    );
    expect(call.id, 'call-2');
    expect(call.isVideoCall, isTrue);

    final events = <Map<dynamic, dynamic>>[];
    final subscription = call.eventStreamController.stream.listen(
      (dynamic event) => events.add(event as Map<dynamic, dynamic>),
    );
    void emit(String event, Map<String, dynamic> body) {
      nativeEvents.add({
        'nativeEventType': StringeeObjectEventType.call2.index,
        'uuid': client.uuid,
        'event': event,
        'body': body,
      });
    }

    emit('didChangeSignalingState', {'callId': 'call-2', 'code': 1});
    emit('didChangeMediaState', {'callId': 'call-2', 'code': 1});
    emit('didReceiveCallInfo', {
      'callId': 'call-2',
      'info': {'a': 1}
    });
    emit('didHandleOnAnotherDevice', {'code': 3});
    emit('didReceiveLocalStream', {'callId': 'call-2'});
    emit('didReceiveRemoteStream', {'callId': 'call-2'});
    final track = {
      'id': 'track-id',
      'localId': 'local-id',
      'publisher': {'id': 'publisher'},
    };
    emit('didAddVideoTrack', {'videoTrack': track});
    emit('didRemoveVideoTrack', {'videoTrack': track});
    emit('didChangeSignalingState', {'callId': 'another', 'code': 1});
    await Future<void>.delayed(Duration.zero);
    expect(events, hasLength(8));

    expect((await call.sendDtmf(' '))['code'], -2);
    await call.initAnswer();
    await call.answer();
    await call.hangup();
    await call.reject();
    await call.sendDtmf('9');
    await call.sendCallInfo({'key': 'value'});
    await call.getCallStats();
    await call.mute(true);
    await call.enableVideo(false);
    await call.switchCamera();
    await call.switchCamera(cameraId: 'back');
    await call.resumeVideo();
    await call.setMirror(false, true);

    expect(
      calls.map((item) => item.method),
      containsAll(<String>[
        'initAnswer2',
        'answer2',
        'hangup2',
        'reject2',
        'sendDtmf2',
        'sendCallInfo2',
        'getCallStats2',
        'mute2',
        'enableVideo2',
        'switchCamera2',
        'resumeVideo2',
        'setMirror2',
      ]),
    );
    await subscription.cancel();
    call.destroy();
  });
}
