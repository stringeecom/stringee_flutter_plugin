import '../../stringee_flutter_plugin.dart';

class StringeeRoomListener {
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeRoomUser roomUser,
  ) onJoinRoom;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeRoomUser roomUser,
  ) onLeaveRoom;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeVideoTrackInfo videoTrackInfo,
  ) onAddVideoTrack;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeVideoTrackInfo videoTrackInfo,
  ) onRemoveVideoTrack;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeRoomUser roomUser,
    Map<dynamic, dynamic> message,
  ) onReceiveRoomMessage;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeVideoTrackInfo videoTrackInfo,
    StringeeRoomUser roomUser,
    MediaType mediaType,
    bool enable,
  )? onTrackMediaStateChange;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    StringeeVideoTrack videoTrack,
  ) onTrackReadyToPlay;
  void Function(
    StringeeVideoRoom stringeeVideoRoom,
    AudioDevice selectedAudioDevice,
    List<AudioDevice> availableAudioDevices,
  )? onChangeAudioDevice;

  StringeeRoomListener({
    required this.onJoinRoom,
    required this.onLeaveRoom,
    required this.onAddVideoTrack,
    required this.onRemoveVideoTrack,
    required this.onReceiveRoomMessage,
    required this.onTrackReadyToPlay,
    this.onTrackMediaStateChange,
    this.onChangeAudioDevice,
  });
}
