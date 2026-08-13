import '../../stringee_plugin.dart';

/// Listener invoked when the selected or available audio devices change.
class StringeeAudioEvent {
  late final String _key;

  /// Callback receiving the selected device and all available devices.
  void Function(
    AudioDevice selectedAudioDevice,
    List<AudioDevice> availableAudioDevices,
  ) onChangeAudioDevice;

  /// Stable key used to register and remove this listener.
  String get key => _key;

  /// Creates an audio-device listener.
  StringeeAudioEvent({required this.onChangeAudioDevice}) {
    this._key = GUIDGen.generate();
  }
}
