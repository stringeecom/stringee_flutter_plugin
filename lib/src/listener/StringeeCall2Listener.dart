import '../../stringee_flutter_plugin.dart';

class StringeeCall2Listener {
  void Function(StringeeSignalingState signalingState) onChangeSignalingState;
  void Function(StringeeMediaState mediaState) onChangeMediaState;
  void Function(Map<dynamic, dynamic> callInfo) onReceiveCallInfo;
  void Function(StringeeSignalingState signalingState) onHandleOnAnotherDevice;
  void Function() onReceiveLocalStream;
  void Function() onReceiveRemoteStream;
  void Function(StringeeVideoTrack videoTrack)? onAddVideoTrack;
  void Function(StringeeVideoTrack videoTrack)? onRemoveVideoTrack;
  void Function(AudioDevice selectedAudioDevice,
      List<AudioDevice> availableAudioDevices)? onChangeAudioDevice;


  StringeeCall2Listener({
    required this.onChangeSignalingState,
    required this.onChangeMediaState,
    required this.onReceiveCallInfo,
    required this.onHandleOnAnotherDevice,
    required this.onReceiveLocalStream,
    required this.onReceiveRemoteStream,
    this.onAddVideoTrack,
    this.onRemoveVideoTrack,
    this.onChangeAudioDevice,
  });
}
