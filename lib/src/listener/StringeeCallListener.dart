import '../../stringee_flutter_plugin.dart';

class StringeeCallListener {
  void Function(StringeeSignalingState signalingState) onChangeSignalingState;
  void Function(StringeeMediaState mediaState) onChangeMediaState;
  void Function(Map<dynamic, dynamic> callInfo) onReceiveCallInfo;
  void Function(StringeeSignalingState signalingState) onHandleOnAnotherDevice;
  void Function() onReceiveLocalStream;
  void Function() onReceiveRemoteStream;
  void Function(AudioDevice selectedAudioDevice,
      List<AudioDevice> availableAudioDevices)? onChangeAudioDevice;

  StringeeCallListener({
    required this.onChangeSignalingState,
    required this.onChangeMediaState,
    required this.onReceiveCallInfo,
    required this.onHandleOnAnotherDevice,
    required this.onReceiveLocalStream,
    required this.onReceiveRemoteStream,
    this.onChangeAudioDevice,
  });
}
