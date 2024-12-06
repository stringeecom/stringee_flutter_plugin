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
    assert(event != null);
    final Map<dynamic, dynamic> map = event;
    _selectedAudioDevice = AudioDeviceX.fromValue(map['code']);
    _availableAudioDevices =
        map['codeList'].map((e) => AudioDeviceX.fromValue(e)).toList() ?? [];
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
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('start');
    return Result.fromJson(result);
  }

  /// Stop the audio manager
  Future<Result> stop() async {
    Map<dynamic, dynamic> result = await methodChannel.invokeMethod('stop');
    return Result.fromJson(result);
  }

  /// Select an audio device
  Future<Result> selectDevice(AudioDevice device) async {
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('selectDevice', {
      'device': device.index,
    });
    return Result.fromJson(result);
  }
}
