import '../../stringee_flutter_plugin.dart';

class StringeeCall2Listener {
  void Function(
    StringeeCall2 stringeeCall2,
    StringeeSignalingState signalingState,
  ) onChangeSignalingState;
  void Function(
    StringeeCall2 stringeeCall2,
    StringeeMediaState mediaState,
  ) onChangeMediaState;
  void Function(
    StringeeCall2 stringeeCall2,
    Map<dynamic, dynamic> callInfo,
  ) onReceiveCallInfo;
  void Function(
    StringeeCall2 stringeeCall2,
    StringeeSignalingState signalingState,
  ) onHandleOnAnotherDevice;
  void Function(
    StringeeCall2 stringeeCall2,
  ) onReceiveLocalStream;
  void Function(
    StringeeCall2 stringeeCall2,
  ) onReceiveRemoteStream;
  void Function(
    StringeeCall2 stringeeCall2,
    StringeeVideoTrack videoTrack,
  )? onAddVideoTrack;
  void Function(
    StringeeCall2 stringeeCall2,
    StringeeVideoTrack videoTrack,
  )? onRemoveVideoTrack;
  void Function(
    StringeeCall2 stringeeCall2,
    String from,
    MediaType mediaType,
    bool enable,
  )? onTrackMediaStateChange;
  void Function(
    StringeeCall2 stringeeCall2,
    AudioDevice selectedAudioDevice,
    List<AudioDevice> availableAudioDevices,
  )? onChangeAudioDevice;

  StringeeCall2Listener({
    required this.onChangeSignalingState,
    required this.onChangeMediaState,
    required this.onReceiveCallInfo,
    required this.onHandleOnAnotherDevice,
    required this.onReceiveLocalStream,
    required this.onReceiveRemoteStream,
    this.onAddVideoTrack,
    this.onRemoveVideoTrack,
    this.onTrackMediaStateChange,
    this.onChangeAudioDevice,
  });
}
