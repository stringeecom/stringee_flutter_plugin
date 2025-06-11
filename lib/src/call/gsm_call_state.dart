import 'dart:async';

import 'package:flutter/services.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

enum PhoneState {
  idle,
  ringing,
  offHook,
}

extension PhoneStateX on PhoneState {
  static PhoneState from(String? value) {
    switch (value) {
      case 'ringing':
        return PhoneState.ringing;
      case 'offHook':
        return PhoneState.offHook;
      default:
        return PhoneState.idle;
    }
  }

  String get value {
    switch (this) {
      case PhoneState.idle:
        return 'idle';
      case PhoneState.ringing:
        return 'ringing';
      case PhoneState.offHook:
        return 'offHook';
    }
  }

  bool get isRinging => this == PhoneState.ringing;

  bool get isOffHook => this == PhoneState.offHook;

  bool get isIdle => this == PhoneState.idle;
}

class GSMCallState {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.gsm_call_state.method_channel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.gsm_call_state.event_channel');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  Function(PhoneState phoneState)? _onPhoneStateChange;

  GSMCallState._privateConstructor() {
    broadcastStream.listen(
      (event) {
        _onPhoneStateChange?.call(PhoneStateX.from(event));
      },
    );
  }

  static GSMCallState? _instance;

  factory GSMCallState() {
    return _instance ??= GSMCallState._privateConstructor();
  }

  void setOnPhoneStateChange(
      Function(PhoneState phoneState) onPhoneStateChange) {
    _onPhoneStateChange = onPhoneStateChange;
  }

  Future<Result> startListening() async {
    return Result.fromJson(
        await methodChannel.invokeMethod('start_listen_gms_call_state'));
  }

  Future<Result> stopListening() async {
    return Result.fromJson(
        await methodChannel.invokeMethod('stop_listen_gms_call_state'));
  }

  Future<PhoneState> getCurrentCallState() async {
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('get_current_call_state');
    if (result['status'] == true) {
      return PhoneStateX.from(result['state']);
    }
    return PhoneState.idle;
  }
}
