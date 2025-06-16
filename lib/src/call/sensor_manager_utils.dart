import 'dart:async';

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
    return Result.fromJson(await methodChannel.invokeMethod('turn_on_sensor'));
  }

  Future<Result> turnOffSensor() async {
    return Result.fromJson(await methodChannel.invokeMethod('turn_off_sensor'));
  }

  Future<Result> releaseSensor() async {
    return Result.fromJson(await methodChannel.invokeMethod('release_sensor'));
  }
}
