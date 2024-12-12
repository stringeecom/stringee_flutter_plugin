import '../../stringee_plugin.dart';

class StringeeAudioEvent {
  late final String _key;
  void Function(
    AudioDevice selectedAudioDevice,
    List<AudioDevice> availableAudioDevices,
  ) onChangeAudioDevice;

  String get key => _key;

  StringeeAudioEvent({required this.onChangeAudioDevice}) {
    this._key = GUIDGen.generate();
  }
}
