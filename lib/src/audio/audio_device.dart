import '../../stringee_plugin.dart';
import '../helper/value_parser.dart';

/// An audio route reported by the native Stringee audio manager.
class AudioDevice {
  late String? _uuid;
  late String? _name;
  late AudioType _audioType;

  /// Creates an audio route with its [audioType], display [name], and [uuid].
  AudioDevice({
    required AudioType? audioType,
    String? uuid,
    String? name,
  }) {
    this._audioType = audioType ?? AudioType.none;
    this._uuid = uuid;
    this._name = name;
  }

  /// The route category.
  AudioType get audioType => _audioType;

  /// Human-readable route name when supplied by the platform.
  String? get name => _name;

  /// Native identifier used when selecting this route.
  String? get uuid => _uuid;

  /// Converts this route to a platform-channel payload.
  Map<String, dynamic> toJson() {
    return {
      'type': _audioType.index,
      'name': _name,
      'uuid': _uuid,
    };
  }

  /// Parses an audio route from a native [json] payload.
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
