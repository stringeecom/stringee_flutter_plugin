package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.stringeeflutterplugin.ConversationManager.UserRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeFlutterPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin {

    private static StringeeManager _stringeeManager;
    private static StringeeClientManager _clientManager;
    private static StringeeCallManager _callManager;
    private static StringeeCall2Manager _call2Manager;
    private static ConversationManager _conversationManager;
    private static MessageManager _messageManager;
    public static EventChannel.EventSink _eventSink;
    private static Handler _handler;
    FlutterPluginBinding _binding;
    public static MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        _binding = binding;
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
    public void onMethodCall(MethodCall call, final Result result) {
        if (!Utils.isInternetConnected(_binding.getApplicationContext())) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -5);
                    map.put("message", "No internet connection");
                    result.success(map);
                }
            });
        } else {
            switch (call.method) {
                case "connect":
                    _clientManager.connect((String) call.arguments, result);
                    break;
                case "disconnect":
                    _clientManager.disconnect(result);
                    break;
                case "registerPush":
                    _clientManager.registerPush((String) call.arguments, result);
                    break;
                case "unregisterPush":
                    _clientManager.unregisterPush((String) call.arguments, result);
                    break;
                case "sendCustomMessage":
                    try {
                        _clientManager.sendCustomMessage((String) call.argument("userId"), new JSONObject((String) call.argument("msg")), result);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "makeCall":
                    String from = (String) call.argument("from");
                    String to = (String) call.argument("to");
                    String resolution = null;
                    boolean isVideoCall = false;
                    if (call.hasArgument("isVideoCall")) {
                        isVideoCall = (boolean) call.argument("isVideoCall");
                        if (call.hasArgument("videoQuality")) {
                            resolution = (String) call.argument("videoQuality");
                        }
                    }
                    String customData = null;
                    if (call.hasArgument("customData")) {
                        customData = (String) call.argument("customData");
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
                    String from2 = (String) call.argument("from");
                    String to2 = (String) call.argument("to");
                    String resolution2 = null;
                    boolean isVideoCall2 = false;
                    if (call.hasArgument("isVideoCall")) {
                        isVideoCall2 = (boolean) call.argument("isVideoCall");
                        if (call.hasArgument("videoQuality")) {
                            resolution2 = (String) call.argument("videoQuality");
                        }
                    }
                    String customData2 = null;
                    if (call.hasArgument("customData")) {
                        customData2 = (String) call.argument("customData");
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
                        List<User> participants = new ArrayList<>();
                        participants = Utils.getListUser((String) call.argument("participants"));
                        ConversationOptions option = new ConversationOptions();
                        JSONObject optionObject = new JSONObject((String) call.argument("option"));
                        option.setName(optionObject.optString("name", null));
                        option.setGroup(optionObject.getBoolean("isGroup"));
                        option.setDistinct(optionObject.getBoolean("isDistinct"));
                        _clientManager.createConversation(participants, option, result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "getConversationById":
                    _clientManager.getConversationById((String) call.arguments, result);
                    break;
                case "getConversationByUserId":
                    _clientManager.getConversationByUserId((String) call.arguments, result);
                    break;
                case "getConversationFromServer":
                    _clientManager.getConversationFromServer((String) call.arguments, result);
                    break;
                case "getLocalConversations":
                    _clientManager.getLocalConversations(result);
                    break;
                case "getLastConversation":
                    _clientManager.getLastConversation((int) call.arguments, result);
                    break;
                case "getConversationsBefore":
                    _clientManager.getConversationsBefore((long) call.argument("milliseconds"), (int) call.argument("count"), result);
                    break;
                case "getConversationsAfter":
                    _clientManager.getConversationsAfter((long) call.argument("milliseconds"), (int) call.argument("count"), result);
                    break;
                case "clearDb":
                    _clientManager.clearDb(result);
                    break;
                case "blockUser":
                    _clientManager.blockUser((String) call.arguments, result);
                    break;
                case "getTotalUnread":
                    _clientManager.getTotalUnread(result);
                    break;
                case "delete":
                    _conversationManager.deleteConversation((String) call.arguments, result);
                    break;
                case "addParticipants":
                    try {
                        List<User> participants = new ArrayList<>();
                        participants = Utils.getListUser((String) call.argument("participants"));
                        _conversationManager.addParticipants((String) call.argument("convId"), participants, result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "removeParticipants":
                    try {
                        List<User> participants = new ArrayList<>();
                        participants = Utils.getListUser((String) call.argument("participants"));
                        _conversationManager.removeParticipants((String) call.argument("convId"), participants, result);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "sendMessage":
                    try {
                        JSONObject msgObject = new JSONObject((String) call.arguments);
                        String convId = msgObject.getString("convId");
                        int msgType = msgObject.getInt("type");
                        Message message = new com.stringee.messaging.Message(msgType);
                        switch (message.getType()) {
                            case Message.TYPE_TEXT:
                            case Message.TYPE_LINK:
                                message = new Message(msgObject.getString("text"));
                                break;
                            case Message.TYPE_PHOTO:
                            case Message.TYPE_FILE:
                                message.setFilePath(msgObject.optString("filePath", null));
                                break;
                            case Message.TYPE_VIDEO:
                            case Message.TYPE_AUDIO:
                                message.setFilePath(msgObject.getString("filePath"));
                                message.setDuration(msgObject.getInt("duration"));
                                break;
                            case Message.TYPE_LOCATION:
                                message.setLatitude(msgObject.getDouble("latitude"));
                                message.setLongitude(msgObject.getDouble("longitude"));
                                break;
                            case Message.TYPE_CONTACT:
                                message.setContact(msgObject.getString("contact"));
                                break;
                            case Message.TYPE_STICKER:
                                message.setStickerCategory(msgObject.getString("stickerCategory"));
                                message.setStickerName(msgObject.getString("stickerName"));
                                break;
                        }
                        if (msgObject.has("customData")){
                            message.setCustomData(msgObject.getJSONObject("customData"));
                        }
                        _conversationManager.sendMessage(convId, message, result);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "getMessages":
                    String[] msgIds = ((List<String>) call.argument("msgIds")).toArray(new String[0]);
                    _conversationManager.getMessages((String) call.argument("convId"), msgIds, result);
                    break;
                case "getLocalMessages":
                    _conversationManager.getLocalMessages((String) call.argument("convId"), (int) call.argument("count"), result);
                    break;
                case "getLastMessages":
                    _conversationManager.getLastMessages((String) call.argument("convId"), (int) call.argument("count"), result);
                    break;
                case "getMessagesAfter":
                    _conversationManager.getMessagesAfter((String) call.argument("convId"), (int) call.argument("seq"), (int) call.argument("count"), result);
                    break;
                case "getMessagesBefore":
                    _conversationManager.getMessagesBefore((String) call.argument("convId"), (int) call.argument("seq"), (int) call.argument("count"), result);
                    break;
                case "updateConversation":
                    _conversationManager.updateConversation((String) call.argument("convId"), (String) call.argument("name"), (String) call.argument("avatar"), result);
                    break;
                case "setRole":
                    int role = (int) call.argument("role");
                    if (role == UserRole.Admin.getValue()) {
                        _conversationManager.setRole((String) call.argument("convId"), (String) call.argument("userId"), UserRole.Admin, result);
                    } else if (role == UserRole.Member.getValue()) {
                        _conversationManager.setRole((String) call.argument("convId"), (String) call.argument("userId"), UserRole.Member, result);
                    }
                    break;
                case "deleteMessages":
                    try {
                        JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                        _conversationManager.deleteMessages((String) call.argument("convId"), msgIdArray, result);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "revokeMessages":
                    try {
                        JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                        _conversationManager.revokeMessages((String) call.argument("convId"), msgIdArray, (boolean) call.argument("isDeleted"), result);
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "markAsRead":
                    _conversationManager.markAsRead((String) call.arguments, result);
                    break;
                case "edit":
                    _messageManager.edit((String) call.argument("convId"), (String) call.argument("msgId"), (String) call.argument("content"), result);
                    break;
                case "pinOrUnPin":
                    _messageManager.pinOrUnPin((String) call.argument("convId"), (String) call.argument("msgId"), (boolean) call.argument("pinOrUnPin"), result);
                    break;
                default:
                    result.notImplemented();
            }
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
