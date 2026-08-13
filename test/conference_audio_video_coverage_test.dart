import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
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

  final userInfo = <String, dynamic>{'id': 'publisher'};
  late Map<String, dynamic> trackInfo;
  late Map<String, dynamic> roomInfo;

  Map<String, dynamic> success({dynamic body}) => {
        'status': true,
        'code': 0,
        'message': 'Success',
        if (body != null) 'body': body,
      };

  setUp(() async {
    trackInfo = <String, dynamic>{
      'id': 'track-1',
      'localId': 'local-1',
      'audio': 1,
      'video': true,
      'screen': false,
      'isLocal': true,
      'publisher': userInfo,
    };
    roomInfo = <String, dynamic>{'id': 'room-1', 'recorded': 1};
    nativeEvents = StreamController<dynamic>.broadcast();
    StringeeClient.broadcastStream = nativeEvents.stream;
    calls = <MethodCall>[];
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel,
        (MethodCall call) async {
      calls.add(call);
      switch (call.method) {
        case 'video.joinRoom':
          return success(body: {
            'room': Map<String, dynamic>.from(roomInfo),
            'videoTrackInfos': [Map<String, dynamic>.from(trackInfo)],
            'users': [userInfo],
          });
        case 'video.createLocalVideoTrack':
        case 'room.publish':
        case 'room.subscribe':
          return success(body: Map<String, dynamic>.from(trackInfo));
        default:
          return success();
      }
    });
    client = StringeeClient();
    await Future<void>.delayed(Duration.zero);
  });

  tearDown(() async {
    debugDefaultTargetPlatformOverride = null;
    messenger.setMockMethodCallHandler(StringeeClient.methodChannel, null);
    await nativeEvents.close();
  });

  test('video API joins rooms and creates typed local tracks', () async {
    final video = StringeeVideo(client);
    expect((await video.joinRoom(''))['code'], -2);

    final joined = await video.joinRoom('room-token');
    expect(joined['body']['room'], isA<StringeeVideoRoom>());
    expect(joined['body']['videoTrackInfos'], hasLength(1));
    expect(joined['body']['users'], hasLength(1));

    final created = await video.createLocalVideoTrack(
      StringeeVideoTrackOption(
        audio: true,
        video: true,
        screen: false,
        videoDimension: StringeeVideoDimensions.dimesion_1080,
      ),
    );
    final track = created['body'] as StringeeVideoTrack;
    expect(track.id, 'track-1');
    expect(track.localId, 'local-1');
    expect(track.publisher.id, 'publisher');
    expect(track.audioEnable, isTrue);
    expect(track.videoEnable, isTrue);
    expect(track.isScreenCapture, isFalse);
    expect(track.isLocal, isTrue);
    expect(track.toString(), contains('track-1'));

    final info = track.getInfo();
    expect(info.id, track.id);
    expect(info.publisher.id, 'publisher');
    expect(info.audioEnable, isTrue);
    expect(info.videoEnable, isTrue);
    expect(info.isScreenCapture, isFalse);

    await track.mute(true);
    await track.enableVideo(false);
    await track.switchCamera();
    await track.switchCamera(cameraId: 'front');
    final view = track.attach(
      isMirror: true,
      height: 100,
      width: 200,
      scalingType: ScalingType.fit,
    );
    expect(view.trackId, 'local-1');
    expect(view.forCall, isFalse);
    expect(
      calls.map((item) => item.method),
      containsAll(<String>[
        'track.mute',
        'track.enableVideo',
        'track.switchCamera',
      ]),
    );
  });

  test('room maps events and forwards publish/subscribe operations', () async {
    final room = StringeeVideoRoom(client, roomInfo);
    expect(room.id, 'room-1');
    expect(room.recorded, isTrue);
    final events = <Map<dynamic, dynamic>>[];
    final subscription = room.eventStreamController.stream.listen(
      (dynamic event) => events.add(event as Map<dynamic, dynamic>),
    );

    void emit(String event, Map<String, dynamic> body) {
      nativeEvents.add({
        'nativeEventType': StringeeObjectEventType.room.index,
        'uuid': client.uuid,
        'event': event,
        'body': body,
      });
    }

    emit('didJoinRoom', {'roomId': 'room-1', 'user': userInfo});
    emit('didLeaveRoom', {'roomId': 'room-1', 'user': userInfo});
    emit('didAddVideoTrack', {
      'roomId': 'room-1',
      'videoTrackInfo': trackInfo,
    });
    emit('didRemoveVideoTrack', {
      'roomId': 'room-1',
      'videoTrackInfo': trackInfo,
    });
    emit('didReceiveRoomMessage', {
      'roomId': 'room-1',
      'msg': {'hello': 'world'},
      'from': userInfo,
    });
    emit('trackReadyToPlay', {'roomId': 'room-1', 'track': trackInfo});
    emit('didJoinRoom', {'roomId': 'another', 'user': userInfo});
    await Future<void>.delayed(Duration.zero);
    expect(events, hasLength(6));
    expect(
      events.map((event) => event['eventType']),
      containsAll(StringeeRoomEvents.values),
    );

    final track = StringeeVideoTrack(client, trackInfo);
    final info = StringeeVideoTrackInfo(trackInfo);
    expect((await room.publish(track))['body'], isA<StringeeVideoTrack>());
    await room.unpublish(track);
    expect(
      (await room.subscribe(
        info,
        StringeeVideoTrackOption(audio: true, video: true, screen: false),
      ))['body'],
      isA<StringeeVideoTrack>(),
    );
    await room.unsubscribe(info);
    await room.leave(allClient: true);
    await room.sendMessage({'hello': 'room'});
    expect(
      calls.map((item) => item.method),
      containsAll(<String>[
        'room.publish',
        'room.unpublish',
        'room.subscribe',
        'room.unsubscribe',
        'room.leave',
        'room.sendMessage',
      ]),
    );

    await subscription.cancel();
    room.destroy();
  });

  testWidgets('video view builds call and track platform configurations',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      const MediaQuery(
        data: MediaQueryData(size: Size(320, 640)),
        child: Directionality(
          textDirection: TextDirection.ltr,
          child: SizedBox(key: Key('context')),
        ),
      ),
    );
    final context = tester.element(find.byKey(const Key('context')));

    final callView = StringeeVideoView(
      'call-id',
      true,
      isMirror: true,
      width: 200,
      height: 100,
      margin: const EdgeInsets.all(1),
      padding: const EdgeInsets.all(2),
      alignment: Alignment.center,
      child: const Text('overlay'),
      scalingType: ScalingType.fit,
      borderRadius: BorderRadius.circular(8),
    );
    expect(callView.callId, 'call-id');
    expect(callView.forCall, isTrue);
    debugDefaultTargetPlatformOverride = TargetPlatform.iOS;
    expect(callView.createVideoView(context), isA<UiKitView>());
    expect(callView.build(context), isA<Align>());

    final trackView = StringeeVideoView.forTrack(
      'track-id',
      scalingType: null,
    );
    expect(trackView.trackId, 'track-id');
    debugDefaultTargetPlatformOverride = TargetPlatform.android;
    expect(trackView.createVideoView(context), isA<PlatformViewLink>());

    debugDefaultTargetPlatformOverride = TargetPlatform.linux;
    expect(
      () => trackView.createVideoView(context),
      throwsA(isA<UnsupportedError>()),
    );
    debugDefaultTargetPlatformOverride = null;
  });

  test('room user and empty native values use safe defaults', () {
    final user = StringeeRoomUser({'id': 10});
    expect(user.id, '10');
    expect(user.toString(), '{id: 10}');

    final info = StringeeVideoTrackInfo(const {});
    expect(info.id, isEmpty);
    expect(info.audioEnable, isFalse);
    expect(info.videoEnable, isFalse);
    expect(info.isScreenCapture, isFalse);
    expect(info.publisher.id, isEmpty);
  });

  test('audio manager publishes routes and validates device selection',
      () async {
    final audioEvents = StreamController<dynamic>.broadcast();
    StringeeAudioManager.broadcastStream = audioEvents.stream;
    final audioCalls = <MethodCall>[];
    messenger.setMockMethodCallHandler(StringeeAudioManager.methodChannel,
        (MethodCall call) async {
      audioCalls.add(call);
      return success();
    });
    final manager = StringeeAudioManager();
    var callbackCount = 0;
    final listener = StringeeAudioEvent(
      onChangeAudioDevice: (selected, available) {
        callbackCount++;
        expect(selected.audioType, AudioType.bluetooth);
        expect(available, hasLength(2));
      },
    );
    manager.addListener(listener);

    audioEvents.add({
      'device': {
        'type': AudioType.bluetooth.index,
        'name': 'Headset',
        'uuid': 'bluetooth-id',
      },
      'devices': [
        {
          'type': AudioType.bluetooth.index,
          'name': 'Headset',
          'uuid': 'bluetooth-id',
        },
        {
          'type': AudioType.speakerPhone.index,
          'name': 'Speaker',
          'uuid': 'speaker-id',
        },
      ],
    });
    await Future<void>.delayed(Duration.zero);
    expect(callbackCount, 1);
    expect(manager.selectedAudioDevice.name, 'Headset');
    expect(manager.availableAudioDevices, hasLength(2));

    expect((await manager.start()).status, isTrue);
    expect((await manager.stop()).status, isTrue);
    expect(
      (await manager.selectDevice(manager.availableAudioDevices.first)).status,
      isTrue,
    );
    final unavailable = await manager.selectDevice(
      AudioDevice(audioType: AudioType.wiredHeadset, name: 'Missing'),
    );
    expect(unavailable.status, isFalse);
    expect(unavailable.code, -3);
    manager.removeListener(listener);
    expect(
      audioCalls.map((item) => item.method),
      containsAll(<String>['start', 'stop', 'selectDevice']),
    );

    messenger.setMockMethodCallHandler(
        StringeeAudioManager.methodChannel, null);
    await audioEvents.close();
  });
}
