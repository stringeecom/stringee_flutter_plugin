import 'dart:async';
import 'dart:typed_data';

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

  Future<NotificationChannel?> createChannel(
    String channelId,
    String channelName,
    String description, {
    NotificationImportance? importance,
    bool? enableLights,
    bool? enableVibration,
    Int64List? vibrationPattern,
    bool? lockscreenVisibility,
    bool? playSound,
    String? soundSource,
    NotificationRingtoneType? defaultRingtoneType,
  }) async {
    NotificationChannel notificationChannel = new NotificationChannel(
      channelId,
      channelName,
      description,
      importance: importance,
      enableLights: enableLights,
      enableVibration: enableVibration,
      vibrationPattern: vibrationPattern,
      lockscreenVisibility: lockscreenVisibility,
      playSound: playSound,
      soundSource: soundSource,
      defaultRingtoneType: defaultRingtoneType,
    );
    Map<String, dynamic> params = notificationChannel.toJson();
    Map<dynamic, dynamic> result =
        await methodChannel.invokeMethod('createChannel', params);
    if (result['status']) {
      return notificationChannel;
    } else {
      return null;
    }
  }

  Future<void> showNotification(
      NotificationChannel channel, Notification notification) async {
    Map<String, dynamic> params = {
      'channel': channel.toJson(),
      'notification': notification.toJson(),
    };
    await methodChannel.invokeMethod('showNotification', params);
  }

  Future<void> cancel(String notificationId) async {
    await methodChannel.invokeMethod('cancel', notificationId);
  }

  Future<void> startForegroundService(
      NotificationChannel channel, Notification notification) async {
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
