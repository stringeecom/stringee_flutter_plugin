import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:stringee_plugin/stringee_plugin.dart';

class WindowFlagManager {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.window.flag.method_channel');

  WindowFlagManager._privateConstructor() {}

  static WindowFlagManager? _instance;

  factory WindowFlagManager() {
    return _instance ??= WindowFlagManager._privateConstructor();
  }

  Future<Result> addWindowFlag() async {
    if (Platform.isIOS) {
      return Result(
        status: false,
        code: -4,
        message: 'This function work only for Android',
      );
    }
    return Result.fromJson(await methodChannel.invokeMethod('add_window_flag'));
  }

  Future<Result> clearWindowFlag() async {
    if (Platform.isIOS) {
      return Result(
        status: false,
        code: -4,
        message: 'This function work only for Android',
      );
    }
    return Result.fromJson(await methodChannel.invokeMethod('clear_window_flag'));
  }
}
