import '../../stringee_flutter_plugin.dart';

class StringeeCallListener {
  void Function(
    StringeeCall stringeeCall,
    StringeeSignalingState signalingState,
  ) onChangeSignalingState;
  void Function(
    StringeeCall stringeeCall,
    StringeeMediaState mediaState,
  ) onChangeMediaState;
  void Function(
    StringeeCall stringeeCall,
    Map<dynamic, dynamic> callInfo,
  ) onReceiveCallInfo;
  void Function(
    StringeeCall stringeeCall,
    StringeeSignalingState signalingState,
  ) onHandleOnAnotherDevice;
  void Function(
    StringeeCall stringeeCall,
  ) onReceiveLocalStream;
  void Function(
    StringeeCall stringeeCall,
  ) onReceiveRemoteStream;
  void Function(
    StringeeCall stringeeCall,
    AudioDevice selectedAudioDevice,
    List<AudioDevice> availableAudioDevices,
  )? onChangeAudioDevice;

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
