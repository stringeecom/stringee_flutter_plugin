import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

class AudioDevice {
  late String? _uuid;
  late String? _name;
  late AudioType _audioType;

  AudioDevice({
    required AudioType? audioType,
    String? uuid,
    String? name,
  }) {
    this._audioType = audioType ?? AudioType.none;
    this._uuid = uuid;
    this._name = name;
  }

  AudioType get audioType => _audioType;

  String? get name => _name;

  String? get uuid => _uuid;

  Map<String, dynamic> toJson() {
    return {
      'type': _audioType.index,
      'name': _name,
      'uuid': _uuid,
    };
  }

  static fromJson(dynamic json) {
    final map = StringeeValueParser.toMap(json) ?? {};
    AudioType audioType = AudioTypeX.fromValue(
      StringeeValueParser.toInt(map['type']),
    );
    String? name = StringeeValueParser.toStringValue(map['name']);
    if (name == null) {
      switch (audioType) {
        case AudioType.speakerPhone:
          name = 'Speaker Phone';
          break;
        case AudioType.wiredHeadset:
          name = 'Wired Headset';
          break;
        case AudioType.earpiece:
          name = 'Earpiece';
          break;
        case AudioType.bluetooth:
          name = 'Bluetooth';
          break;
        case AudioType.other:
          name = 'Other';
          break;
        case AudioType.none:
          name = 'None';
          break;
      }
    }
    return AudioDevice(
      audioType: audioType,
      name: name,
      uuid: StringeeValueParser.toStringValue(map['uuid']),
    );
  }

  @override
  String toString() {
    return 'AudioDevice{uuid: $_uuid, name: $_name, audioType: $_audioType}';
  }
}
