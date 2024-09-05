package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.common.SocketAddress;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.Message.Type;
import com.stringee.messaging.User;
import com.stringee.stringeeflutterplugin.StringeeManager.UserRole;
import com.stringee.video.StringeeVideoTrack.Options;
import com.stringee.video.VideoDimensions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeFlutterPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin, ActivityAware {
    public static EventSink eventSink;
    private StringeeManager stringeeManager;

    private static final String TAG = "StringeeSDK";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        stringeeManager = StringeeManager.getInstance();
        stringeeManager.setHandler(new Handler(Looper.getMainLooper()));
        stringeeManager.setContext(binding.getApplicationContext());

        MethodChannel channel = new MethodChannel(binding.getBinaryMessenger(), "com.stringee.flutter.methodchannel");
        channel.setMethodCallHandler(this);

        EventChannel eventChannel = new EventChannel(binding.getBinaryMessenger(), "com.stringee.flutter.eventchannel");
        eventChannel.setStreamHandler(this);

        binding.getPlatformViewRegistry().registerViewFactory("stringeeVideoView", new StringeeVideoViewFactory());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull final Result result) {
        String uuid = call.argument("uuid");
        String callId = call.argument("callId");
        String oaId = null;
        if (call.hasArgument("oaId")) {
            oaId = call.argument("oaId");
        }
        if (call.method.equals("setupClient")) {
            ClientWrapper clientWrapper;
            String baseAPIUrl = call.argument("baseAPIUrl");
            if (!TextUtils.isEmpty(baseAPIUrl)) {
                clientWrapper = new ClientWrapper(uuid, baseAPIUrl);
            } else {
                clientWrapper = new ClientWrapper(uuid);
            }
            stringeeManager.getClientMap().put(uuid, clientWrapper);
            return;
        }

        ClientWrapper clientWrapper = stringeeManager.getClientMap().get(uuid);
        Map<String, Object> map = new HashMap<>();
        if (clientWrapper == null) {
            Log.d(TAG, call.method + ": false - -100 - Wrapper is not found");
            map.put("status", false);
            map.put("code", -100);
            map.put("message", "Wrapper is not found");
            result.success(map);
            return;
        }

        if (call.method.equals("connect")) {
            String serverAddresses = call.argument("serverAddresses");
            String token = call.argument("token");
            List<SocketAddress> socketAddressList = new ArrayList<>();
            if (!Utils.isStringEmpty(serverAddresses)) {
                try {
                    JSONArray array = new JSONArray((String) call.argument("serverAddresses"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = (JSONObject) array.get(i);
                        String host = object.optString("host", null);
                        int port = object.optInt("port", -1);
                        if (!Utils.isStringEmpty(host) && port != -1) {
                            socketAddressList.add(new SocketAddress(host, port));
                        }
                    }
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
            }
            clientWrapper.connect(socketAddressList, token, result);
            return;
        }

        switch (call.method) {
            case "disconnect":
                clientWrapper.disconnect(result);
                break;
            case "registerPush":
                clientWrapper.registerPush(call.argument("deviceToken"), result);
                break;
            case "registerPushAndDeleteOthers":
                clientWrapper.registerPushAndDeleteOthers(call.argument("deviceToken"), call.argument("packageNames"), result);
                break;
            case "unregisterPush":
                clientWrapper.unregisterPush(call.argument("deviceToken"), result);
                break;
            case "sendCustomMessage":
                try {
                    clientWrapper.sendCustomMessage(call.argument("userId"), Utils.convertMapToJson(call.argument("msg")), result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "makeCall":
                String from = call.argument("from");
                String to = call.argument("to");
                String resolution = null;
                boolean isVideoCall = false;
                if (call.hasArgument("isVideoCall")) {
                    isVideoCall = Boolean.TRUE.equals(call.argument("isVideoCall"));
                    if (call.hasArgument("videoQuality")) {
                        resolution = call.argument("videoQuality");
                    }
                }
                String callCustomData = null;
                if (call.hasArgument("customData")) {
                    callCustomData = call.argument("customData");
                }
                clientWrapper.callWrapper(from, to, isVideoCall, callCustomData, resolution, result).makeCall();
                break;
            case "initAnswer":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).initAnswer(result);
                }
                break;
            case "answer":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).answer(result);
                }
                break;
            case "hangup":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).hangup(result);
                }
                break;
            case "reject":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).reject(result);
                }
                break;
            case "sendDtmf":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).sendDTMF(call.argument("dtmf"), result);
                }
                break;
            case "sendCallInfo":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).sendCallInfo(call.argument("callInfo"), result);
                }
                break;
            case "getCallStats":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).getCallStats(result);
                }
                break;
            case "mute":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).mute(Boolean.TRUE.equals(call.argument("mute")), result);
                }
                break;
            case "enableVideo":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).enableVideo(Boolean.TRUE.equals(call.argument("enableVideo")), result);
                }
                break;
            case "setSpeakerphoneOn":
            case "setSpeakerphoneOn2":
                stringeeManager.setSpeakerphoneOn(Boolean.TRUE.equals(call.argument("speaker")), result);
                break;
            case "switchCamera":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    if (call.hasArgument("cameraId")) {
                        clientWrapper.callWrapper(callId).switchCamera(call.argument("cameraId"), result);
                    } else {
                        clientWrapper.callWrapper(callId).switchCamera(result);
                    }
                }
                break;
            case "resumeVideo":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).resumeVideo(result);
                }
                break;
            case "setMirror":
                if (Utils.isCallWrapperAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).setMirror(Boolean.TRUE.equals(call.argument("isLocal")), Boolean.TRUE.equals(call.argument("isMirror")), result);
                }
                break;
            case "makeCall2":
                String from2 = call.argument("from");
                String to2 = call.argument("to");
                boolean isVideoCall2 = false;
                if (call.hasArgument("isVideoCall")) {
                    isVideoCall2 = Boolean.TRUE.equals(call.argument("isVideoCall"));
                }
                String call2CustomData = null;
                if (call.hasArgument("customData")) {
                    call2CustomData = call.argument("customData");
                }
                clientWrapper.call2Wrapper(from2, to2, isVideoCall2, call2CustomData, result).makeCall();
                break;
            case "initAnswer2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).initAnswer(result);
                }
                break;
            case "answer2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).answer(result);
                }
                break;
            case "hangup2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).hangup(result);
                }
                break;
            case "reject2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).reject(result);
                }
                break;
            case "getCallStats2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).getCallStats(result);
                }
                break;
            case "sendCallInfo2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).sendCallInfo(call.argument("callInfo"), result);
                }
                break;
            case "mute2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).mute(Boolean.TRUE.equals(call.argument("mute")), result);
                }
                break;
            case "enableVideo2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).enableVideo(Boolean.TRUE.equals(call.argument("enableVideo")), result);
                }
                break;
            case "switchCamera2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    if (call.hasArgument("cameraId")) {
                        clientWrapper.call2Wrapper(callId).switchCamera(call.argument("cameraId"), result);
                    } else {
                        clientWrapper.call2Wrapper(callId).switchCamera(result);
                    }
                }
                break;
            case "resumeVideo2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).resumeVideo(result);
                }
                break;
            case "setMirror2":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).setMirror(Boolean.TRUE.equals(call.argument("isLocal")), Boolean.TRUE.equals(call.argument("isMirror")), result);
                }
            case "startCapture":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).startCapture(result);
                }
                break;
            case "stopCapture":
                if (Utils.isCall2WrapperAvailable(call.method, callId, result)) {
                    clientWrapper.call2Wrapper(callId).stopCapture(result);
                }
                break;
            case "createConversation":
                try {
                    List<User> participants = Utils.getListUser(call.argument("participants"));
                    ConversationOptions option = new ConversationOptions();
                    JSONObject optionObject = new JSONObject((String) call.argument("option"));
                    option.setName(optionObject.optString("name").trim());
                    option.setGroup(optionObject.getBoolean("isGroup"));
                    option.setDistinct(optionObject.getBoolean("isDistinct"));
                    option.setOaId(optionObject.optString("oaId").trim());
                    option.setCustomData(optionObject.optString("customData").trim());
                    option.setCreatorId(optionObject.optString("creatorId").trim());
                    clientWrapper.createConversation(participants, option, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getConversationById":
                clientWrapper.getConversationById(call.argument("convId"), result);
                break;
            case "getConversationByUserId":
                clientWrapper.getConversationByUserId(call.argument("userId"), result);
                break;
            case "getLocalConversations":
                clientWrapper.getLocalConversations(oaId, result);
                break;
            case "getLastConversation":
                Integer count = call.argument("count");
                clientWrapper.getLastConversation(count != null ? count : 20, oaId, result);
                break;
            case "getConversationsBefore":
                clientWrapper.getConversationsBefore(call.argument("datetime"), call.argument("count"), oaId, result);
                break;
            case "getConversationsAfter":
                clientWrapper.getConversationsAfter(call.argument("datetime"), call.argument("count"), oaId, result);
                break;
            case "joinOaConversation":
                clientWrapper.joinOaConversation(call.argument("convId"), result);
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
                clientWrapper.conversation().delete(call.argument("convId"), result);
                break;
            case "addParticipants":
                try {
                    List<User> participants = new ArrayList<>();
                    participants = Utils.getListUser(call.argument("participants"));
                    clientWrapper.conversation().addParticipants(call.argument("convId"), participants, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "removeParticipants":
                try {
                    List<User> participants = new ArrayList<>();
                    participants = Utils.getListUser(call.argument("participants"));
                    clientWrapper.conversation().removeParticipants(call.argument("convId"), participants, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "sendMessage":
                try {
                    Map msgMap = (Map) call.arguments;
                    String convId = (String) msgMap.get("convId");
                    Type msgType = Type.getType((int) msgMap.get("type"));
                    Message message = new Message(msgType);
                    switch (message.getType()) {
                        case TEXT:
                        case LINK:
                            message = new Message((String) msgMap.get("text"));
                            break;
                        case PHOTO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("thumbnail"))
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            if (msgMap.containsKey("ratio"))
                                message.setImageRatio(((Double) msgMap.get("ratio")).floatValue());
                            break;
                        case VIDEO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            message.setDuration(((Double) msgMap.get("duration")).intValue());
                            if (msgMap.containsKey("thumbnail"))
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            if (msgMap.containsKey("ratio"))
                                message.setImageRatio(((Double) msgMap.get("ratio")).floatValue());
                            break;
                        case AUDIO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            message.setDuration(((Double) msgMap.get("duration")).intValue());
                            break;
                        case FILE:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("filename"))
                                message.setFileName((String) msgMap.get("filename"));
                            if (msgMap.containsKey("length"))
                                message.setFileLength(((Integer) msgMap.get("length")).longValue());
                            break;
                        case LOCATION:
                            message.setLatitude((Double) msgMap.get("lat"));
                            message.setLongitude((Double) msgMap.get("lon"));
                            break;
                        case CONTACT:
                            message.setContact((String) msgMap.get("vcard"));
                            break;
                        case STICKER:
                            message.setStickerCategory((String) msgMap.get("stickerCategory"));
                            message.setStickerName((String) msgMap.get("stickerName"));
                            break;
                    }
                    if (msgMap.containsKey("customData")) {
                        message.setCustomData(Utils.convertMapToJson((Map) msgMap.get("customData")));
                    }
                    clientWrapper.conversation().sendMessage(convId, message, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getMessages":
                String[] msgIds = ((List<String>) call.argument("msgIds")).toArray(new String[0]);
                clientWrapper.conversation().getMessages(call.argument("convId"), msgIds, result);
                break;
            case "getLocalMessages":
                clientWrapper.conversation().getLocalMessages(call.argument("convId"), call.argument("count"), result);
                break;
            case "getLastMessages":
                clientWrapper.conversation().getLastMessages(call.argument("convId"), call.argument("count"), result);
                break;
            case "getMessagesAfter":
                clientWrapper.conversation().getMessagesAfter(call.argument("convId"), call.argument("seq"), call.argument("count"), result);
                break;
            case "getMessagesBefore":
                clientWrapper.conversation().getMessagesBefore(call.argument("convId"), call.argument("seq"), call.argument("count"), result);
                break;
            case "updateConversation":
                clientWrapper.conversation().updateConversation(call.argument("convId"), call.argument("name"), call.argument("avatar"), result);
                break;
            case "setRole":
                int role = call.argument("role");
                if (role == UserRole.Admin.getValue()) {
                    clientWrapper.conversation().setRole(call.argument("convId"), call.argument("userId"), UserRole.Admin, result);
                } else if (role == UserRole.Member.getValue()) {
                    clientWrapper.conversation().setRole(call.argument("convId"), call.argument("userId"), UserRole.Member, result);
                }
                break;
            case "deleteMessages":
                try {
                    JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                    clientWrapper.conversation().deleteMessages(call.argument("convId"), msgIdArray, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "revokeMessages":
                try {
                    JSONArray msgIdArray = new JSONArray(((List<String>) call.argument("msgIds")).toArray(new String[0]));
                    clientWrapper.conversation().revokeMessages(call.argument("convId"), msgIdArray, call.argument("isDeleted"), result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "markAsRead":
                clientWrapper.conversation().markAsRead(call.argument("convId"), result);
                break;
            case "editMsg":
                clientWrapper.message().edit(call.argument("convId"), call.argument("msgId"), call.argument("content"), result);
                break;
            case "pinOrUnPin":
                clientWrapper.message().pinOrUnPin(call.argument("convId"), call.argument("msgId"), call.argument("pinOrUnPin"), result);
                break;
            case "getChatProfile":
                clientWrapper.getChatProfile(call.argument("key"), result);
                break;
            case "getLiveChatToken":
                clientWrapper.getLiveChatToken(call.argument("key"), call.argument("name"), call.argument("email"), result);
                break;
            case "updateUserInfo":
                clientWrapper.updateUserInfo(call.argument("name"), call.argument("email"), call.argument("avatar"), call.argument("phone"), result);
                break;
            case "createLiveChatConversation":
                String customData = null;
                if (call.hasArgument("customData")) {
                    customData = call.argument("customData");
                }
                clientWrapper.createLiveChatConversation(call.argument("queueId"), customData, result);
                break;
            case "createLiveChatTicket":
                clientWrapper.createLiveChatTicket(call.argument("key"), call.argument("name"), call.argument("email"), call.argument("description"), result);
                break;
            case "sendChatTranscript":
                clientWrapper.conversation().sendChatTranscript(call.argument("convId"), call.argument("email"), call.argument("domain"), result);
                break;
            case "endChat":
                clientWrapper.conversation().endChat(call.argument("convId"), result);
                break;
            case "beginTyping":
                clientWrapper.conversation().beginTyping(call.argument("convId"), result);
                break;
            case "endTyping":
                clientWrapper.conversation().endTyping(call.argument("convId"), result);
                break;
            case "acceptChatRequest":
                clientWrapper.chatRequest().acceptChatRequest(call.argument("convId"), result);
                break;
            case "rejectChatRequest":
                clientWrapper.chatRequest().rejectChatRequest(call.argument("convId"), result);
                break;
            case "video.joinRoom":
                clientWrapper.videoConference().connect(call.argument("roomToken"), result);
                break;
            case "video.createLocalVideoTrack":
                try {
                    JSONObject videoOptionsObject = Utils.convertMapToJson(call.argument("options"));
                    Options options = new Options();
                    options.audio(videoOptionsObject.optBoolean("audio"));
                    options.video(videoOptionsObject.optBoolean("video"));
                    options.screen(videoOptionsObject.optBoolean("screen"));
                    String videoDimensions = videoOptionsObject.optString("videoDimension");
                    switch (videoDimensions) {
                        case "288":
                            options.videoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS);
                            break;
                        case "480":
                            options.videoDimensions(VideoDimensions.WVGA_VIDEO_DIMENSIONS);
                            break;
                        case "720":
                            options.videoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS);
                            break;
                        case "1080":
                            options.videoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS);
                            break;
                    }

                    clientWrapper.videoConference().createLocalVideoTrack(options, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "video.createCaptureScreenTrack":
                clientWrapper.videoConference().createCaptureScreenTrack(result);
                break;
            case "room.publish":
                clientWrapper.videoConference().publish(call.argument("roomId"), call.argument("localId"), result);
                break;
            case "room.unpublish":
                clientWrapper.videoConference().unpublish(call.argument("roomId"), call.argument("localId"), result);
                break;
            case "room.subscribe":
                try {
                    JSONObject videoOptionsObject = Utils.convertMapToJson(call.argument("options"));
                    Options options = new Options();
                    options.audio(videoOptionsObject.optBoolean("audio"));
                    options.video(videoOptionsObject.optBoolean("video"));
                    options.screen(videoOptionsObject.optBoolean("screen"));
                    String videoDimensions = videoOptionsObject.optString("videoDimension");
                    switch (videoDimensions) {
                        case "288":
                            options.videoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS);
                            break;
                        case "480":
                            options.videoDimensions(VideoDimensions.WVGA_VIDEO_DIMENSIONS);
                            break;
                        case "720":
                            options.videoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS);
                            break;
                        case "1080":
                            options.videoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS);
                            break;
                    }

                    clientWrapper.videoConference().subscribe(call.argument("roomId"), call.argument("trackId"), options, result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "room.unsubscribe":
                clientWrapper.videoConference().unsubscribe(call.argument("roomId"), call.argument("trackId"), result);
                break;
            case "room.leave":
                clientWrapper.videoConference().leave(call.argument("roomId"), call.argument("allClient"), result);
                break;
            case "room.sendMessage":
                try {
                    clientWrapper.videoConference().sendMessage(call.argument("roomId"), Utils.convertMapToJson(call.argument("msg")), result);
                } catch (JSONException e) {
                    Logging.e(StringeeFlutterPlugin.class, e);
                }
                break;
            case "track.mute":
                clientWrapper.videoConference().mute(call.argument("localId"), call.argument("mute"), result);
                break;
            case "track.enableVideo":
                clientWrapper.videoConference().enableVideo(call.argument("localId"), call.argument("enable"), result);
                break;
            case "track.switchCamera":
                if (call.hasArgument("cameraId")) {
                    clientWrapper.videoConference().switchCamera(call.argument("localId"), call.argument("cameraId"), result);
                } else {
                    clientWrapper.videoConference().switchCamera(call.argument("localId"), result);
                }
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public void onListen(Object o, EventSink eventSink) {
        StringeeFlutterPlugin.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {

    }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        stringeeManager.setCaptureManager(ScreenCaptureManager.create(binding));
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}