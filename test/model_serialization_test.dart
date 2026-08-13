import 'package:flutter_test/flutter_test.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

void main() {
  group('Result', () {
    test('parses a native result payload', () {
      final result = Result.fromJson({
        'status': true,
        'code': 0,
        'message': 'Success',
        'body': {'id': 'body-id'},
      });

      expect(result.status, isTrue);
      expect(result.code, 0);
      expect(result.message, 'Success');
      expect(result.data, {'id': 'body-id'});
    });
  });

  group('call and conference options', () {
    test('serializes typed outgoing-call parameters', () {
      final params = MakeCallParams(
        ' caller ',
        ' callee ',
        isVideoCall: true,
        customData: {'source': 'test'},
        videoQuality: VideoQuality.hd,
      );

      expect(params.toJson(), {
        'from': 'caller',
        'to': 'callee',
        'customData': {'source': 'test'},
        'isVideoCall': true,
        'videoResolution': 'HD',
      });
    });

    test('serializes conversation options without absent optional values', () {
      final options = StringeeConversationOption(
        isGroup: true,
        isDistinct: false,
        name: ' Support ',
        oaId: ' oa-id ',
      );

      expect(options.toJson(), {
        'name': 'Support',
        'isGroup': true,
        'isDistinct': false,
        'oaId': 'oa-id',
      });
    });

    test('serializes server and video-track options', () {
      expect(
        StringeeServerAddress(' signal.example.com ', 443).toJson(),
        {'host': 'signal.example.com', 'port': 443},
      );

      expect(
        StringeeVideoTrackOption(
          audio: true,
          video: true,
          screen: false,
          videoDimension: StringeeVideoDimensions.dimesion_720,
        ).toJson(),
        {
          'audio': true,
          'video': true,
          'screen': false,
          'videoDimension': '720',
        },
      );
    });
  });

  group('audio and chat models', () {
    test('parses audio devices and supplies default names', () {
      final device = AudioDevice.fromJson({
        'type': AudioType.bluetooth.index,
        'uuid': 'device-id',
      }) as AudioDevice;

      expect(device.audioType, AudioType.bluetooth);
      expect(device.name, 'Bluetooth');
      expect(device.uuid, 'device-id');
      expect(device.toJson(), {
        'type': AudioType.bluetooth.index,
        'name': 'Bluetooth',
        'uuid': 'device-id',
      });
    });

    test('parses and serializes conversation users', () {
      final user = StringeeUser.fromJson({
        'user': 'user-id',
        'displayName': 'Test User',
        'avatarUrl': 'https://example.com/avatar.png',
        'role': 'admin',
      });

      expect(user.userId, 'user-id');
      expect(user.name, 'Test User');
      expect(user.role, UserRole.admin);
      expect(user.toJson(), {
        'userId': 'user-id',
        'name': 'Test User',
        'avatarUrl': 'https://example.com/avatar.png',
        'role': UserRole.admin.index,
      });
    });
  });

  group('enum channel mappings', () {
    test('maps message and notification values in both directions', () {
      expect(MsgType.video.value, 3);
      expect(11.msgType, MsgType.sticker);
      expect(999.msgType, MsgType.text);
      expect(1.notifyType, MsgNotifyType.addParticipants);
      expect(999.notifyType, MsgNotifyType.changeGroupName);
    });

    test('maps native audio values', () {
      expect(AudioTypeX.fromValue(0), AudioType.speakerPhone);
      expect(AudioTypeX.fromValue(3), AudioType.bluetooth);
      expect(AudioTypeX.fromValue(999), AudioType.none);
    });
  });
}
