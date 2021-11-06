import 'dart:async';

import 'package:flutter/services.dart';
import 'package:stringee_flutter_plugin/src/notification/NotificationChannel.dart';

import '../../stringee_flutter_plugin.dart';

class StringeeNotification {
  static const MethodChannel methodChannel =
      MethodChannel('com.stringee.flutter.methodchannel.notification');
  static const EventChannel eventChannel =
      EventChannel('com.stringee.flutter.eventchannel.notification');
  static Stream broadcastStream = eventChannel.receiveBroadcastStream();

  Map<int, NotificationAction> _actionMap = {};

  Map<int, NotificationAction> get actionMap => _actionMap;

  StringeeNotification.initialize() {
    broadcastStream.listen((event) {
      assert(event != null);
      int actionId = event['actionId'];
      if (actionMap[actionId] != null) {
        actionMap[actionId]!.press();
      }
    });
  }

  static final StringeeNotification instance =
      StringeeNotification.initialize();

  factory StringeeNotification() {
    return instance;
  }

  Future<Map<dynamic, dynamic>> createChannel(
      NotificationChannel notificationChannel) async {
    return await methodChannel.invokeMethod(
        'createChannel', notificationChannel.toJson());
  }

  Future<Map<dynamic, dynamic>> showNotification(
      NotificationAndroid notification) async {
    return await methodChannel.invokeMethod(
        'showNotification', notification.toJson());
  }

  Future<Map<dynamic, dynamic>> cancel(String notificationId) async {
    return await methodChannel.invokeMethod('cancel', notificationId);
  }

  Future<void> startForegroundService(
      NotificationChannel channel, NotificationAndroid notification) async {
    Map<String, dynamic> params = {
      'channel': channel.toJson(),
      'notification': notification.toJson(),
    };
    await methodChannel.invokeMethod('startForegroundService', params);
  }

  Future<void> stopForegroundService(String notificationId) async {
    await methodChannel.invokeMethod('stopForegroundService', notificationId);
  }
}
