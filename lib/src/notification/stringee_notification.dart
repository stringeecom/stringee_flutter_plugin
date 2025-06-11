import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

import '../../stringee_plugin.dart';

typedef CallBack = Future<void> Function(Map<dynamic, dynamic> event);

class StringeeNotification {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.notification.method_channel');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.notification.event_channel');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  StringeeNotification.initialize() {}

  static final StringeeNotification instance =
      StringeeNotification.initialize();

  factory StringeeNotification() {
    return instance;
  }

  void listenNotificationPress(CallBack callBack) {
    broadcastStream.listen((event) {
      Map<dynamic, dynamic>? eventMap;
      if (event is Map<dynamic, dynamic>) {
        eventMap = event;
      } else if (event is String) {
        eventMap = json.decode(event);
      }
      if (eventMap != null) {
        callBack(eventMap);
      }
    });
  }

  /// Cancel notification
  Future<Result> cancelNotification(int notificationId) async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result = await methodChannel.invokeMethod(
          'cancel_notification', notificationId);
      return Result.fromJson(result);
    }
  }

  /// Notify incoming call
  Future<Result> notifyIncomingCall(IncomingCallNotificationInfo info) async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result = await methodChannel.invokeMethod(
          'notify_incoming_call', info.toJson());
      return Result.fromJson(result);
    }
  }

  /// Start in-call service
  Future<Result> startInCallService(InCallServiceInfo info) async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result = await methodChannel.invokeMethod(
          'start_in_call_service', info.toJson());
      return Result.fromJson(result);
    }
  }

  /// Stop in-call service
  Future<Result> stopInCallService() async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result = await methodChannel.invokeMethod('stop_in_call_service');
      return Result.fromJson(result);
    }
  }

  /// Start screen capture service
  Future<Result> startScreenCaptureService(
      ScreenCaptureServiceInfo info) async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result = await methodChannel.invokeMethod(
          'start_screen_capture_service', info.toJson());
      return Result.fromJson(result);
    }
  }

  /// Stop screen capture service
  Future<Result> stopScreenCaptureService() async {
    if (Platform.isIOS) {
      return Result(
          status: false,
          code: -4,
          message: "This function work only for Android");
    } else {
      dynamic result =
          await methodChannel.invokeMethod('stop_screen_capture_service');
      return Result.fromJson(result);
    }
  }
}
