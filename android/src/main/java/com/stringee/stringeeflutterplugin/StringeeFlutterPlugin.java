package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.common.SocketAddress;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.stringeeflutterplugin.StringeeManager.UserRole;

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
    private static StringeeManager _manager;
    public static EventChannel.EventSink _eventSink;
    public static MethodChannel channel;
    private static Context _context;

    private static final String TAG = "StringeeSDK";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        _manager = StringeeManager.getInstance();
        _manager.setHandler(new Handler(Looper.getMainLooper()));
        _manager.setContext(binding.getApplicationContext());
        _context = binding.getApplicationContext();

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
        String uuid = (String) call.argument("uuid");
        String callId = (String) call.argument("callId");
        if (call.method.equals("setupClient")) {
            ClientWrapper clientWrapper;
            String baseAPIUrl = (String) call.argument("baseAPIUrl");
            if (!TextUtils.isEmpty(baseAPIUrl)) {
                clientWrapper = new ClientWrapper(uuid, baseAPIUrl);
            } else {
                clientWrapper = new ClientWrapper(uuid);
            }
            _manager.getClientMap().put(uuid, clientWrapper);
            return;
        }

        ClientWrapper clientWrapper = _manager.getClientMap().get(uuid);
        Map map = new HashMap();
        if (clientWrapper == null) {
            Log.d(TAG, call.method + ": false - -100 - Wrapper is not found");
            map.put("status", false);
            map.put("code", -100);
            map.put("message", "Wrapper is not found");
            result.success(map);
            return;
        }

        if (call.method.equals("connect")) {
            String serverAddresses = (String) call.argument("serverAddresses");
            String token = (String) call.argument("token");
            if (serverAddresses == null || serverAddresses.equalsIgnoreCase("") || serverAddresses.equalsIgnoreCase("null")) {
                clientWrapper.connect(token, result);
            } else {
                try {
                    List<SocketAddress> socketAddressList = new ArrayList<>();
                    JSONArray array = new JSONArray((String) call.argument("serverAddresses"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = (JSONObject) array.get(i);
                        String host = object.optString("host", null);
                        int port = object.optInt("port", -1);
                        if (host == null || host.equalsIgnoreCase("null") || host.equalsIgnoreCase("")) {
                            Log.d(TAG, "host is invalid");
                        }

                        if (port == -1) {
                            Log.d(TAG, "port is invalid");
                        }

                        if (host != null && !host.equalsIgnoreCase("null") && !host.equalsIgnoreCase("") && port != -1) {
                            SocketAddress socketAddress = new SocketAddress(host, port);
                            socketAddressList.add(socketAddress);
                        }

                    }
                    clientWrapper.connect(socketAddressList, token, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if (!clientWrapper.isConnected()) {
            Log.d(TAG, call.method + ": false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        switch (call.method) {
            case "disconnect":
                clientWrapper.disconnect(result);
                break;
            case "registerPush":
                clientWrapper.registerPush((String) call.argument("deviceToken"), result);
                break;
            case "unregisterPush":
                clientWrapper.unregisterPush((String) call.argument("deviceToken"), result);
                break;
            case "sendCustomMessage":
                try {
                    clientWrapper.sendCustomMessage((String) call.argument("userId"), new JSONObject((String) call.argument("msg")), result);
                } catch (JSONException e) {
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
                clientWrapper.callWrapper(from, to, isVideoCall, customData, resolution, result).makeCall();
                break;
            case "initAnswer":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).initAnswer(result);
                }
                break;
            case "answer":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).answer(result);
                }
                break;
            case "hangup":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).hangup(result);
                }
                break;
            case "reject":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).reject(result);
                }
                break;
            case "sendDtmf":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).sendDTMF((String) call.argument("dtmf"), result);
                }
                break;
            case "sendCallInfo":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).sendCallInfo((Map) call.argument("callInfo"), result);
                }
                break;
            case "getCallStats":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).getCallStats(result);
                }
                break;
            case "mute":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).mute((Boolean) call.argument("mute"), result);
                }
                break;
            case "enableVideo":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).enableVideo((Boolean) call.argument("enableVideo"), result);
                }
                break;
            case "setSpeakerphoneOn":
            case "setSpeakerphoneOn2":
                _manager.setSpeakerphoneOn((Boolean) call.argument("speaker"), result);
                break;
            case "switchCamera":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).switchCamera(result);
                }
                break;
            case "resumeVideo":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).resumeVideo(result);
                }
                break;
            case "setMirror":
                if (Utils.isCallWrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).setMirror((boolean) call.argument("isLocal"), (boolean) call.argument("isMirror"), result);
                }
                break;
            case "makeCall2":
                String from2 = (String) call.argument("from");
                String to2 = (String) call.argument("to");
                boolean isVideoCall2 = false;
                String customData2 = null;
                if (call.hasArgument("isVideoCall")) {
                    isVideoCall2 = (boolean) call.argument("isVideoCall");
                }
                if (call.hasArgument("customData")) {
                    customData2 = (String) call.argument("customData");
                }
                clientWrapper.call2Wrapper(from2, to2, isVideoCall2, customData2, result).makeCall();
                break;
            case "initAnswer2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).initAnswer(result);
                }
                break;
            case "answer2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).answer(result);
                }
                break;
            case "hangup2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).hangup(result);
                }
                break;
            case "reject2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).reject(result);
                }
                break;
            case "getCallStats2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).getCallStats(result);
                }
                break;
            case "sendCallInfo2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).sendCallInfo((Map) call.argument("callInfo"), result);
                }
                break;
            case "mute2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).mute((Boolean) call.argument("mute"), result);
                }
                break;
            case "enableVideo2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).enableVideo((Boolean) call.argument("enableVideo"), result);
                }
                break;
            case "switchCamera2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).switchCamera(result);
                }
                break;
            case "resumeVideo2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).resumeVideo(result);
                }
                break;
            case "setMirror2":
                if (Utils.isCall2WrapperAvaiable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).setMirror((boolean) call.argument("isLocal"), (boolean) call.argument("isMirror"), result);
                }
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
                    clientWrapper.createConversation(participants, option, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "getConversationById":
                clientWrapper.getConversationById((String) call.argument("convId"), result);
                break;
            case "getConversationByUserId":
                clientWrapper.getConversationByUserId((String) call.argument("userId"), result);
                break;
            case "getLocalConversations":
                clientWrapper.getLocalConversations(result);
                break;
            case "getLastConversation":
                clientWrapper.getLastConversation((int) call.argument("count"), result);
                break;
            case "getConversationsBefore":
                clientWrapper.getConversationsBefore((long) call.argument("datetime"), (int) call.argument("count"), result);
                break;
            case "getConversationsAfter":
                clientWrapper.getConversationsAfter((long) call.argument("datetime"), (int) call.argument("count"), result);
                break;
            case "clearDb":
                clientWrapper.clearDb(result);
                break;
            case "blockUser":
                clientWrapper.blockUser((String) call.arguments, result);
                break;
            case "getTotalUnread":
                clientWrapper.getTotalUnread(result);
                break;
            case "delete":
                clientWrapper.conversation().delete((String) call.argument("convId"), result);
                break;
            case "addParticipants":
                try {
                    List<User> participants = new ArrayList<>();
                    participants = Utils.getListUser((String) call.argument("participants"));
                    clientWrapper.conversation().addParticipants((String) call.argument("convId"), participants, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "removeParticipants":
                try {
                    List<User> participants = new ArrayList<>();
                    participants = Utils.getListUser((String) call.argument("participants"));
                    clientWrapper.conversation().removeParticipants((String) call.argument("convId"), participants, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "sendMessage":
                try {
                    Map msgMap = (Map) call.arguments;
                    String convId = (String) msgMap.get("convId");
                    int msgType = (int) msgMap.get("type");
                    Message message = new Message(msgType);
                    switch (message.getType()) {
                        case Message.TYPE_TEXT:
                        case Message.TYPE_LINK:
                            message = new Message((String) msgMap.get("text"));
                            break;
                        case Message.TYPE_PHOTO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("thumbnail"))
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            if (msgMap.containsKey("ratio"))
                                message.setImageRatio((Float) msgMap.get("ratio"));
                            break;
                        case Message.TYPE_VIDEO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            message.setDuration((Integer) msgMap.get("duration"));
                            if (msgMap.containsKey("thumbnail"))
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            if (msgMap.containsKey("ratio"))
                                message.setImageRatio((Float) msgMap.get("ratio"));
                            break;
                        case Message.TYPE_AUDIO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            message.setDuration((Integer) msgMap.get("duration"));
                            break;
                        case Message.TYPE_FILE:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("filename"))
                                message.setFileName((String) msgMap.get("filename"));
                            if (msgMap.containsKey("length"))
                                message.setFileLength((Long) msgMap.get("length"));
                            break;
                        case Message.TYPE_LOCATION:
                            message.setLatitude((Double) msgMap.get("lat"));
                            message.setLongitude((Double) msgMap.get("lon"));
                            break;
                        case Message.TYPE_CONTACT:
                            message.setContact((String) msgMap.get("vcard"));
                            break;
                        case Message.TYPE_STICKER:
                            message.setStickerCategory((String) msgMap.get("stickerCategory"));
                            message.setStickerName((String) msgMap.get("stickerName"));
                            break;
                    }
                    if (msgMap.containsKey("customData")) {
                        message.setCustomData(Utils.convertMapToJson((java.util.Map) msgMap.get("customData")));
                    }
                    clientWrapper.conversation().sendMessage(convId, message, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "getMessages":
                String[] msgIds = ((List<String>) call.argument("msgIds")).toArray(new String[0]);
                clientWrapper.conversation().getMessages((String) call.argument("convId"), msgIds, result);
                break;
            case "getLocalMessages":
                clientWrapper.conversation().getLocalMessages((String) call.argument("convId"), (int) call.argument("count"), result);
                break;
            case "getLastMessages":
                clientWrapper.conversation().getLastMessages((String) call.argument("convId"), (int) call.argument("count"), result);
                break;
            case "getMessagesAfter":
                clientWrapper.conversation().getMessagesAfter((String) call.argument("convId"), (int) call.argument("seq"), (int) call.argument("count"), result);
                break;
            case "getMessagesBefore":
                clientWrapper.conversation().getMessagesBefore((String) call.argument("convId"), (int) call.argument("seq"), (int) call.argument("count"), result);
                break;
            case "updateConversation":
                clientWrapper.conversation().updateConversation((String) call.argument("convId"), (String) call.argument("name"), (String) call.argument("avatar"), result);
                break;
            case "setRole":
                int role = (int) call.argument("role");
                if (role == UserRole.Admin.getValue()) {
                    clientWrapper.conversation().setRole((String) call.argument("convId"), (String) call.argument("userId"), UserRole.Admin, result);
                } else if (role == UserRole.Member.getValue()) {
                    clientWrapper.conversation().setRole((String) call.argument("convId"), (String) call.argument("userId"), UserRole.Member, result);
                }
                break;
            case "deleteMessages":
                try {
                    JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                    clientWrapper.conversation().deleteMessages((String) call.argument("convId"), msgIdArray, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "revokeMessages":
                try {
                    JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                    clientWrapper.conversation().revokeMessages((String) call.argument("convId"), msgIdArray, (boolean) call.argument("isDeleted"), result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "markAsRead":
                clientWrapper.conversation().markAsRead((String) call.argument("convId"), result);
                break;
            case "editMsg":
                clientWrapper.message().edit((String) call.argument("convId"), (String) call.argument("msgId"), (String) call.argument("content"), result);
                break;
            case "pinOrUnPin":
                clientWrapper.message().pinOrUnPin((String) call.argument("convId"), (String) call.argument("msgId"), (boolean) call.argument("pinOrUnPin"), result);
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