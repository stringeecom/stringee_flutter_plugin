import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class SensorManagerUtils {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.sensor.method_channel');

  SensorManagerUtils._privateConstructor() {}

  static SensorManagerUtils? _instance;

  factory SensorManagerUtils() {
    return _instance ??= SensorManagerUtils._privateConstructor();
  }

  Future<Result> turnOnSensor() async {
    if (Platform.isIOS) {
      return Result(
        status: false,
        code: -4,
        message: 'This function work only for Android',
      );
    }
    return Result.fromJson(await methodChannel.invokeMethod('turn_on_sensor'));
  }

  Future<Result> turnOffSensor() async {
    if (Platform.isIOS) {
      return Result(
        status: false,
        code: -4,
        message: 'This function work only for Android',
      );
    }
    return Result.fromJson(await methodChannel.invokeMethod('turn_off_sensor'));
  }

  Future<Result> releaseSensor() async {
    if (Platform.isIOS) {
      return Result(
        status: false,
        code: -4,
        message: 'This function work only for Android',
      );
    }
    return Result.fromJson(await methodChannel.invokeMethod('release_sensor'));
  }
}
