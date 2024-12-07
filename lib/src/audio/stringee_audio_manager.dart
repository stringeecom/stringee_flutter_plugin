import 'package:flutter/services.dart';

import '../../stringee_plugin.dart';

class StringeeAudioManager {
  static final StringeeAudioManager _instance =
      StringeeAudioManager._internal();

  factory StringeeAudioManager() => _instance;

  // Native
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.audio.method_channel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.audio.event_channel');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  AudioDevice _selectedAudioDevice = AudioDevice.none;
  List<AudioDevice> _availableAudioDevices = [];
  final List<StringeeAudioEvent> _events = [];

  StringeeAudioManager._internal() {
    broadcastStream.listen(this._listener);
  }

  AudioDevice get selectedAudioDevice => _selectedAudioDevice;

  List<AudioDevice> get availableAudioDevices => _availableAudioDevices;

  void _listener(dynamic event) {
    if (event == null) return;
    _selectedAudioDevice = AudioDeviceX.fromValue(event['code']);
    _availableAudioDevices.clear();
    event['codeList'].forEach(
      (e) {
        _availableAudioDevices.add(AudioDeviceX.fromValue(e));
      },
    );
    _events.forEach(
      (e) {
        e.onChangeAudioDevice
            .call(_selectedAudioDevice, _availableAudioDevices);
      },
    );
  }

  /// Add a listener to the list of listeners
  void addListener(StringeeAudioEvent event) {
    _events.add(event);
  }

  /// Remove a listener from the list of listeners
  void removeListener(StringeeAudioEvent event) {
    _events.removeWhere((e) => e.key == event.key);
  }

  /// Start the audio manager
  Future<Result> start() async {
    return Result.fromJson(await methodChannel.invokeMethod('start'));
  }

  /// Stop the audio manager
  Future<Result> stop() async {
    return Result.fromJson(await methodChannel.invokeMethod('stop'));
  }

  /// Select an audio device
  Future<Result> selectDevice(AudioDevice device) async {
    print('Select device: $device');
    if (!_availableAudioDevices.contains(device)) {
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
          'device': device.index,
        },
      ),
    );
  }
}
