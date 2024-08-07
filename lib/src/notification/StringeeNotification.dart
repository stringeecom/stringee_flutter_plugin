import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import '../../stringee_plugin.dart';

typedef CallBack = Future<void> Function(String actionId);

@Deprecated('This class will be removed in the next major release.')
class StringeeNotification {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.methodchannel.notification');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.eventchannel.notification');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  StringeeNotification.initialize() {}

  static final StringeeNotification instance =
      StringeeNotification.initialize();

  factory StringeeNotification() {
    return instance;
  }

  void listenActionPress(CallBack callBack) {
    broadcastStream.listen((event) {
      assert(event != null);
      String actionId = event['actionId'];
      callBack(actionId);
    });
  }

  /// Create notification channel in android API >= 26
  Future<Map<dynamic, dynamic>> createChannel(
      NotificationChannel notificationChannel) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      return await methodChannel.invokeMethod(
          'createChannel', notificationChannel.toJson());
    }
  }

  /// Show notification
  Future<Map<dynamic, dynamic>> showNotification(
      NotificationAndroid notification) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      return await methodChannel.invokeMethod(
          'showNotification', notification.toJson());
    }
  }

  /// Cancel notification
  Future<Map<dynamic, dynamic>> cancel(int notificationId) async {
    if (Platform.isIOS) {
      final params = {
        'status': false,
        "code": '-4',
        "message": "This function work only for Android",
      };
      return params;
    } else {
      return await methodChannel.invokeMethod('cancel', notificationId);
    }
  }

  /// Start foreground service
  Future<void> startForegroundService(NotificationAndroid notification) async {
    if (Platform.isAndroid) {
      await methodChannel.invokeMethod(
          'startForegroundService', notification.toJson());
    }
  }

  /// Stop foreground service
  Future<void> stopForegroundService() async {
    if (Platform.isAndroid) {
      await methodChannel.invokeMethod('stopForegroundService');
    }
  }
}
