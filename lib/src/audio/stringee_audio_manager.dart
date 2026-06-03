import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../../stringee_plugin.dart';

class StringeeAudioManager {
  static final StringeeAudioManager _instance =
      StringeeAudioManager._internal();

  factory StringeeAudioManager() => _instance;

  // Native channels.
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.audio.method_channel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.audio.event_channel');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  AudioDevice _selectedAudioDevice = AudioDevice(audioType: AudioType.none);
  List<AudioDevice> _availableAudioDevices = [];
  final List<StringeeAudioEvent> _events = [];

  StringeeAudioManager._internal() {
    broadcastStream.listen(this._listener);
  }

  AudioDevice get selectedAudioDevice => _selectedAudioDevice;

  List<AudioDevice> get availableAudioDevices => _availableAudioDevices;

  void _listener(dynamic event) {
    if (event == null) return;
    _selectedAudioDevice = AudioDevice.fromJson(event['device']);
    _availableAudioDevices.clear();
    event['devices'].forEach(
      (e) {
        _availableAudioDevices.add(AudioDevice.fromJson(e));
      },
    );
    _events.forEach(
      (e) {
        e.onChangeAudioDevice
            .call(_selectedAudioDevice, _availableAudioDevices);
      },
    );
  }

  /// Adds [event] to audio device change listeners.
  void addListener(StringeeAudioEvent event) {
    _events.add(event);
  }

  /// Removes a matching audio device change [event] listener.
  void removeListener(StringeeAudioEvent event) {
    _events.removeWhere((e) => e.key == event.key);
  }

  /// Starts audio device management on the native side.
  Future<Result> start() async {
    return Result.fromJson(await methodChannel.invokeMethod('start'));
  }

  /// Stops audio device management on the native side.
  Future<Result> stop() async {
    return Result.fromJson(await methodChannel.invokeMethod('stop'));
  }

  /// Selects an available audio [device].
  Future<Result> selectDevice(AudioDevice device) async {
    if (kDebugMode) {
      print('Selected device: $device');
    }
    var deviceSelectable = false;
    for (final item in _availableAudioDevices) {
      if (item.audioType == device.audioType &&
          item.name == device.name &&
          item.uuid == device.uuid) {
        deviceSelectable = true;
        break;
      }
    }
    if (!deviceSelectable) {
      return Result(
        status: false,
        code: -3,
        message: 'Audio device not available to select',
      );
    }
    return Result.fromJson(
      await methodChannel.invokeMethod(
        'selectDevice',
        {
          'device': device.toJson(),
        },
      ),
    );
  }
}
