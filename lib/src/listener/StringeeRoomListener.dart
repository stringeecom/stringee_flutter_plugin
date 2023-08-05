import '../../stringee_flutter_plugin.dart';

class StringeeRoomListener {
  void Function(StringeeRoomUser roomUser) onJoinRoom;
  void Function(StringeeRoomUser roomUser) onLeaveRoom;
  void Function(StringeeVideoTrackInfo videoTrackInfo) onAddVideoTrack;
  void Function(StringeeVideoTrackInfo videoTrackInfo) onRemoveVideoTrack;
  void Function(StringeeRoomUser roomUser, Map<dynamic, dynamic> message)
      onReceiveRoomMessage;
  void Function(StringeeVideoTrack videoTrack) onTrackReadyToPlay;

  StringeeRoomListener({
    required this.onJoinRoom,
    required this.onLeaveRoom,
    required this.onAddVideoTrack,
    required this.onRemoveVideoTrack,
    required this.onReceiveRoomMessage,
    required this.onTrackReadyToPlay,
  });
}
