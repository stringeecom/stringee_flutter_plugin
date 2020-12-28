package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class FlutterPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin {

    private static StringeeManager _stringeeManager;
    private static StringeeClientManager _clientManager;
    private static StringeeCallManager _callManager;
    private static StringeeCall2Manager _call2Manager;
    private static ConversationManager _conversationManager;
    private static MessageManager _messageManager;
    public static EventChannel.EventSink _eventSink;
    private static Handler _handler;
    public static MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        _handler = new Handler(Looper.getMainLooper());
        _stringeeManager = StringeeManager.getInstance();
        _clientManager = StringeeClientManager.getInstance(binding.getApplicationContext(), _stringeeManager, _handler);
        _callManager = StringeeCallManager.getInstance(binding.getApplicationContext(), _stringeeManager, _handler);
        _call2Manager = StringeeCall2Manager.getInstance(binding.getApplicationContext(), _stringeeManager, _handler);
        _conversationManager = ConversationManager.getInstance(_stringeeManager, _handler);
        _messageManager = MessageManager.getInstance(_stringeeManager, _handler);

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
                _callManager.makeCall(from, to, isVideoCall, customData, resolution, result);
                break;
            case "initAnswer":
                _callManager.initAnswer((String) call.arguments, result);
                break;
            case "answer":
                _callManager.answer((String) call.arguments, result);
                break;
            case "hangup":
                _callManager.hangup((String) call.arguments, result);
                break;
            case "reject":
                _callManager.reject((String) call.arguments, result);
                break;
            case "sendDtmf":
                _callManager.sendDtmf((String) call.argument("callId"), (String) call.argument("dtmf"), result);
                break;
            case "sendCallInfo":
                _callManager.sendCallInfo((String) call.argument("callId"), (Map) call.argument("callInfo"), result);
                break;
            case "getCallStats":
                _callManager.getCallStats((String) call.arguments, result);
                break;
            case "mute":
                _callManager.mute((String) call.argument("callId"), (Boolean) call.argument("mute"), result);
                break;
            case "enableVideo":
                _callManager.enableVideo((String) call.argument("callId"), (Boolean) call.argument("enableVideo"), result);
                break;
            case "setSpeakerphoneOn":
                _callManager.setSpeakerphoneOn((String) call.argument("callId"), (Boolean) call.argument("speaker"), result);
                break;
            case "switchCamera":
                _callManager.switchCamera((String) call.argument("callId"), (boolean) call.argument("isMirror"), result);
                break;
            case "resumeVideo":
                _callManager.resumeVideo((String) call.argument("callId"), result);
                break;
            case "makeCall2":
                String from2 = call.argument("from");
                String to2 = call.argument("to");
                boolean isVideoCall2 = false;
                if (call.hasArgument("isVideoCall")) {
                    isVideoCall2 = call.argument("isVideoCall");
                }
                String customData2 = null;
                if (call.hasArgument("customData")) {
                    customData2 = call.argument("customData");
                }
                String resolution2 = null;
                if (call.hasArgument("videoResolution")) {
                    resolution2 = call.argument("videoResolution");
                }
                _call2Manager.makeCall(from2, to2, isVideoCall2, customData2, resolution2, result);
                break;
            case "initAnswer2":
                _call2Manager.initAnswer((String) call.arguments, result);
                break;
            case "answer2":
                _call2Manager.answer((String) call.arguments, result);
                break;
            case "hangup2":
                _call2Manager.hangup((String) call.arguments, result);
                break;
            case "reject2":
                _call2Manager.reject((String) call.arguments, result);
                break;
            case "getCallStats2":
                _call2Manager.getCallStats((String) call.arguments, result);
                break;
            case "mute2":
                _call2Manager.mute((String) call.argument("callId"), (Boolean) call.argument("mute"), result);
                break;
            case "enableVideo2":
                _call2Manager.enableVideo((String) call.argument("callId"), (Boolean) call.argument("enableVideo"), result);
                break;
            case "setSpeakerphoneOn2":
                _call2Manager.setSpeakerphoneOn((String) call.argument("callId"), (Boolean) call.argument("speaker"), result);
                break;
            case "switchCamera2":
                _call2Manager.switchCamera((String) call.argument("callId"), (Boolean) call.argument("isMirror"), result);
                break;
            case "resumeVideo2":
                _call2Manager.resumeVideo((String) call.argument("callId"), result);
                break;
            case "createConversation":
                try {
                    List<User> users = new ArrayList<>();
                    users = Utils.getListUser((String) call.argument("users"));
                    ConversationOptions option = new ConversationOptions();
                    JSONObject optionObject = new JSONObject((String) call.argument("option"));
                    String name = optionObject.optString("name");
                    if (name != null) option.setName(name);
                    option.setGroup(optionObject.optBoolean("isGroup", false));
                    option.setDistinct(optionObject.optBoolean("isDistinct", false));
                    _clientManager.createConversation(users, option, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "getConversationById":
                _clientManager.getConversationById((String) call.argument("convId"), result);
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
