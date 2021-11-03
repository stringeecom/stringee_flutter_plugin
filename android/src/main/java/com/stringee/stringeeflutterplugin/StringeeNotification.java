package com.stringee.stringeeflutterplugin;

import androidx.annotation.NonNull;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeNotification implements MethodCallHandler, EventChannel.StreamHandler {
    private static StringeeNotification _instance;

    public static EventSink _eventSink;
    public static MethodChannel _channel;

    public StringeeNotification(BinaryMessenger messenger) {
        _channel = new MethodChannel(messenger, "com.stringee.flutter.methodchannel.notification");
        _channel.setMethodCallHandler(this);

        EventChannel eventChannel = new EventChannel(messenger, "com.stringee.flutter.eventchannel.notification");
        eventChannel.setStreamHandler(this);
    }

    public static synchronized StringeeNotification getInstance(BinaryMessenger messenger) {
        if (_instance == null) {
            _instance = new StringeeNotification(messenger);
        }
        return _instance;
    }


    @Override
    public void onListen(Object arguments, EventSink events) {
        _eventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        android.util.Log.d("Stringee", "onMethodCall: " +  call.method);
    }
}
