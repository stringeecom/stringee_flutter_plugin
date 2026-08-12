import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const eventChannel = MethodChannel('com.stringee.flutter.eventchannel');
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  final calls = <MethodCall>[];

  setUp(() {
    calls.clear();
    messenger.setMockMethodCallHandler(eventChannel, (call) async => null);
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel,
        (call) async {
      calls.add(call);
      switch (call.method) {
        case 'makeCall':
          return {
            'status': true,
            'code': 0,
            'message': 'Success',
            'callInfo': {
              'callId': 'call-id',
              'serial': 7,
              'from': 'caller',
              'to': 'callee',
              'isVideoCall': true,
              'callType': StringeeCallType.appToAppOutgoing.index,
            },
          };
        default:
          return {'status': true, 'code': 0, 'message': 'Success'};
      }
    });
  });

  tearDown(() {
    messenger.setMockMethodCallHandler(eventChannel, null);
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel, null);
  });

  test('client setup and connect send normalized channel arguments', () async {
    final client = StringeeClient(
      baseAPIUrl: 'https://api.example.com',
      serverAddresses: [StringeeServerAddress('signal.example.com', 443)],
    );
    await Future<void>.delayed(Duration.zero);

    final result = await client.connect(' access-token ');

    expect(result['status'], isTrue);
    expect(calls.first.method, 'setupClient');
    expect(calls.first.arguments, {
      'uuid': client.uuid,
      'baseAPIUrl': 'https://api.example.com',
    });

    final connect = calls.singleWhere((call) => call.method == 'connect');
    expect(connect.arguments['token'], 'access-token');
    expect(connect.arguments['uuid'], client.uuid);
    expect(
      jsonDecode(connect.arguments['serverAddresses']),
      [
        {'host': 'signal.example.com', 'port': 443},
      ],
    );
  });

  test('client rejects an empty token without invoking native connect',
      () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
    calls.clear();

    final result = await client.connect('   ');

    expect(result, {
      'status': false,
      'code': -2,
      'message': 'token value is invalid',
    });
    expect(calls.where((call) => call.method == 'connect'), isEmpty);
  });

  test('outgoing call maps native call info and forwards controls', () async {
    final client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
    final call = StringeeCall(client);

    final result = await call.makeCallFromParams(
      MakeCallParams(
        ' caller ',
        ' callee ',
        isVideoCall: true,
        videoQuality: VideoQuality.hd,
      ),
    );

    expect(result, {'status': true, 'code': 0, 'message': 'Success'});
    expect(call.id, 'call-id');
    expect(call.serial, 7);
    expect(call.from, 'caller');
    expect(call.to, 'callee');
    expect(call.isVideoCall, isTrue);

    final makeCall = calls.singleWhere((item) => item.method == 'makeCall');
    expect(makeCall.arguments, {
      'from': 'caller',
      'to': 'callee',
      'isVideoCall': true,
      'videoQuality': 'HD',
      'uuid': client.uuid,
    });

    await call.mute(true);
    final mute = calls.singleWhere((item) => item.method == 'mute');
    expect(mute.arguments, {
      'callId': 'call-id',
      'mute': true,
      'uuid': client.uuid,
    });

    call.destroy();
  });
}
