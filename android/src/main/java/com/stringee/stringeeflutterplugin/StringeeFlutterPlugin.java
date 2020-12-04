package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * StringeeFlutterPlugin
 */
public class StringeeFlutterPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin {

    private static StringeeManager _stringeeManager;
    private static StringeeClientManager _clientManager;
    private static StringeeCallManager _CallManager;
    public static EventChannel.EventSink _eventSink;
    private static Handler _handler;
    public static MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        _handler = new Handler(Looper.getMainLooper());
        _stringeeManager = StringeeManager.getInstance();
        _clientManager = StringeeClientManager.getInstance(binding.getApplicationContext(), _stringeeManager, _handler);
        _CallManager = StringeeCallManager.getInstance(binding.getApplicationContext(), _stringeeManager, _handler);

        channel = new MethodChannel(binding.getBinaryMessenger(), "com.stringee.flutter.methodchannel");
        channel.setMethodCallHandler(this);

        EventChannel eventChannel = new EventChannel(binding.getBinaryMessenger(), "com.stringee.flutter.eventchannel");
        eventChannel.setStreamHandler(this);

        binding
                .getPlatformViewRegistry()
                .registerViewFactory("stringeeVideoView", new StringeeVideoViewFactory());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "connect":
                _clientManager.connect((String) call.arguments);
                break;
            case "disconnect":
                _clientManager.disconnect();
                break;
            case "registerPush":
                _clientManager.registerPush((String) call.argument("deviceToken"), result);
                break;
            case "unregisterPush":
                _clientManager.unregisterPush((String) call.arguments, result);
                break;
            case "sendCustomMessage":
                try {
                    _clientManager.sendCustomMessage((String) call.argument("toUserId"), Utils.convertMapToJson((Map) call.argument("message")), result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "makeCall":
                String from = call.argument("from");
                String to = call.argument("to");
                boolean isVideoCall = false;
                if (call.hasArgument("isVideoCall")) {
                    isVideoCall = call.argument("isVideoCall");
                }
                String customData = null;
                if (call.hasArgument("customData")) {
                    customData = call.argument("customData");
                }
                String resolution = null;
                if (call.hasArgument("videoResolution")) {
                    resolution = call.argument("videoResolution");
                }
                _CallManager.makeCall(from, to, isVideoCall, customData, resolution, result);
                break;
            case "initAnswer":
                _CallManager.initAnswer((String) call.arguments, result);
                break;
            case "answer":
                _CallManager.answer((String) call.arguments, result);
                break;
            case "hangup":
                _CallManager.hangup((String) call.arguments, result);
                break;
            case "reject":
                _CallManager.reject((String) call.arguments, result);
                break;
            case "sendDtmf":
                _CallManager.sendDtmf((String) call.argument("callId"), (String) call.argument("dtmf"), result);
                break;
            case "sendCallInfo":
                _CallManager.sendCallInfo((String) call.argument("callId"), (Map) call.argument("callInfo"), result);
                break;
            case "getCallStats":
                _CallManager.getCallStats((String) call.arguments, result);
                break;
            case "mute":
                _CallManager.mute((String) call.argument("callId"), (Boolean) call.argument("mute"), result);
                break;
            case "setSpeakerphoneOn":
                _CallManager.setSpeakerphoneOn((String) call.argument("callId"), (Boolean) call.argument("speaker"), result);
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        _eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {

    }
}
