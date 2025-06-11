package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.Message.Type;
import com.stringee.messaging.User;
import com.stringee.stringeeflutterplugin.audio.AudioManager;
import com.stringee.stringeeflutterplugin.call.CallUtils;
import com.stringee.stringeeflutterplugin.call.receiver.GSMCallStateReceiver;
import com.stringee.stringeeflutterplugin.chat.ChatUtils;
import com.stringee.stringeeflutterplugin.chat.enumeration.UserRole;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.PacketSenderThread;
import com.stringee.stringeeflutterplugin.common.PrefUtils;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.conference.ScreenCaptureManager;
import com.stringee.stringeeflutterplugin.notification.StringeeNotification;
import com.stringee.stringeeflutterplugin.video_view.StringeeVideoViewFactory;
import com.stringee.video.StringeeVideoTrack.Options;
import com.stringee.video.VideoDimensions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();

        /// Audio manager
        new MethodChannel(binding.getBinaryMessenger(),
                Constants.audioMethodChannel).setMethodCallHandler(
                AudioManager.getInstance(context));
        new EventChannel(binding.getBinaryMessenger(),
                Constants.audioEventChannel).setStreamHandler(AudioManager.getInstance(context));

        /// Notification
        new MethodChannel(binding.getBinaryMessenger(),
                Constants.notificationMethodChannel).setMethodCallHandler(
                StringeeNotification.getInstance(context));
        new EventChannel(binding.getBinaryMessenger(),
                Constants.notificationEventChannel).setStreamHandler(
                StringeeNotification.getInstance(context));

        /// GSM Call State
        new MethodChannel(binding.getBinaryMessenger(),
                Constants.gsmMethodChannel).setMethodCallHandler(
                GSMCallStateReceiver.getInstance(context));
        new EventChannel(binding.getBinaryMessenger(), Constants.gsmEventChannel).setStreamHandler(
                GSMCallStateReceiver.getInstance(context));

        /// Main plugin
        new MethodChannel(binding.getBinaryMessenger(),
                Constants.methodChannel).setMethodCallHandler(this);
        new EventChannel(binding.getBinaryMessenger(), Constants.eventChannel).setStreamHandler(
                this);

        binding.getPlatformViewRegistry()
                .registerViewFactory("stringeeVideoView", new StringeeVideoViewFactory());
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
            String baseAPIUrl = call.argument("baseAPIUrl");
            ClientWrapper clientWrapper = new ClientWrapper(context, uuid, baseAPIUrl);
            PrefUtils.getInstance(context)
                    .putString(Constants.PREF_BASE_API_URL + uuid, baseAPIUrl);
            StringeeManager.getInstance().getClientMap().put(uuid, clientWrapper);
            return;
        }

        ClientWrapper clientWrapper = StringeeManager.getInstance().getClientMap().get(uuid);
        Map<String, Object> map = new HashMap<>();
        if (clientWrapper == null) {
            Log.d(Constants.TAG, call.method + ": false - -100 - Wrapper is not found");
            map.put("status", false);
            map.put("code", -100);
            map.put("message", "Wrapper is not found");
            result.success(map);
            return;
        }

        if (call.method.equals("connect")) {
            String token = call.argument("token");
            String serverAddresses = call.argument("serverAddresses");
            PrefUtils.getInstance(context).putString(Constants.PREF_ACCESS_TOKEN + uuid, token);
            PrefUtils.getInstance(context)
                    .putString(Constants.PREF_SERVER_ADDRESS + uuid, serverAddresses);
            clientWrapper.connect(serverAddresses, token, result);
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
                clientWrapper.registerPushAndDeleteOthers(call.argument("deviceToken"),
                        call.argument("packageNames"), result);
                break;
            case "unregisterPush":
                clientWrapper.unregisterPush(call.argument("deviceToken"), result);
                break;
            case "sendCustomMessage":
                try {
                    Map<String, Object> customMsg = (Map<String, Object>) call.arguments;
                    clientWrapper.sendCustomMessage(call.argument("userId"),
                            Utils.convertMapToJson(customMsg), result);
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "makeCall":
            case "makeCall2":
                String from = call.argument("from");
                String to = call.argument("to");
                boolean isVideoCall = Boolean.TRUE.equals(call.argument("isVideoCall"));
                String callCustomData = null;
                if (call.hasArgument("customData")) {
                    callCustomData = call.argument("customData");
                }
                if (call.method.equals("makeCall")) {
                    String resolution = null;
                    if (call.hasArgument("videoQuality")) {
                        resolution = call.argument("videoQuality");
                    }
                    clientWrapper.callWrapper(from, to, isVideoCall, callCustomData, resolution)
                            .makeCall(result);
                } else {
                    clientWrapper.call2Wrapper(from, to, isVideoCall, callCustomData)
                            .makeCall(result);
                }
                break;
            case "initAnswer":
            case "initAnswer2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).initAnswer(result);
                }
                break;
            case "answer":
            case "answer2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).answer(result);
                }
                break;
            case "hangup":
            case "hangup2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).hangup(result);
                }
                break;
            case "reject":
            case "reject2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).reject(result);
                }
                break;
            case "sendDtmf":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).sendDTMF(call.argument("dtmf"), result);
                }
                break;
            case "sendCallInfo":
            case "sendCallInfo2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId)
                            .sendCallInfo(call.argument("callInfo"), result);
                }
                break;
            case "getCallStats":
            case "getCallStats2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).getCallStats(result);
                }
                break;
            case "mute":
            case "mute2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId)
                            .mute(Boolean.TRUE.equals(call.argument("mute")), result);
                }
                break;
            case "enableVideo":
            case "enableVideo2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId)
                            .enableVideo(Boolean.TRUE.equals(call.argument("enableVideo")), result);
                }
                break;
            case "switchCamera":
            case "switchCamera2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    if (call.hasArgument("cameraId")) {
                        clientWrapper.callWrapper(callId)
                                .switchCamera(call.argument("cameraId"), result);
                    } else {
                        clientWrapper.callWrapper(callId).switchCamera(result);
                    }
                }
                break;
            case "resumeVideo":
            case "resumeVideo2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).resumeVideo(result);
                }
                break;
            case "setMirror":
            case "setMirror2":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId)
                            .setMirror(Boolean.TRUE.equals(call.argument("isLocal")),
                                    Boolean.TRUE.equals(call.argument("isMirror")), result);
                }
                break;
            case "startCapture":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).startCapture(result);
                }
                break;
            case "stopCapture":
                if (CallUtils.isCallAvailable(call.method, callId, result)) {
                    clientWrapper.callWrapper(callId).stopCapture(result);
                }
                break;
            case "createConversation":
                try {
                    List<User> participants = ChatUtils.getListUser(call.argument("participants"));
                    ConversationOptions option = new ConversationOptions();
                    JSONObject optionObject = new JSONObject(
                            (String) Objects.requireNonNull(call.argument("option")));
                    option.setName(optionObject.optString("name").trim());
                    option.setGroup(optionObject.getBoolean("isGroup"));
                    option.setDistinct(optionObject.getBoolean("isDistinct"));
                    option.setOaId(optionObject.optString("oaId").trim());
                    option.setCustomData(optionObject.optString("customData").trim());
                    option.setCreatorId(optionObject.optString("creatorId").trim());
                    clientWrapper.createConversation(participants, option, result);
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
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
                try {
                    Integer count = call.argument("count");
                    if (count != null) {
                        clientWrapper.getLastConversation(count, oaId, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getConversationsBefore":
                try {
                    Integer count = call.argument("count");
                    Long datetime = call.argument("datetime");
                    if (count != null && datetime != null) {
                        clientWrapper.getConversationsBefore(datetime, count, oaId, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getConversationsAfter":
                try {
                    Integer count = call.argument("count");
                    Long datetime = call.argument("datetime");
                    if (count != null && datetime != null) {
                        clientWrapper.getConversationsAfter(datetime, count, oaId, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
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
                    List<User> participants = ChatUtils.getListUser(call.argument("participants"));
                    clientWrapper.conversation()
                            .addParticipants(call.argument("convId"), participants, result);
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "removeParticipants":
                try {
                    List<User> participants = ChatUtils.getListUser(call.argument("participants"));
                    clientWrapper.conversation()
                            .removeParticipants(call.argument("convId"), participants, result);
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "sendMessage":
                try {
                    Map<String, Object> msgMap = (Map<String, Object>) call.arguments;
                    String convId = (String) msgMap.get("convId");
                    Integer msgTypeInt = (Integer) msgMap.get("type");
                    Type msgType = Type.TEXT;
                    if (msgTypeInt != null) {
                        msgType = Type.getType(msgTypeInt);
                    }
                    Message message = new Message(msgType);
                    switch (message.getType()) {
                        case TEXT:
                        case LINK:
                            message = new Message((String) msgMap.get("text"));
                            break;
                        case PHOTO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("thumbnail")) {
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            }
                            if (msgMap.containsKey("ratio")) {
                                Double ratio = (Double) msgMap.get("ratio");
                                if (ratio != null) {
                                    message.setImageRatio(ratio.floatValue());
                                }
                            }
                            break;
                        case VIDEO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("duration")) {
                                Double duration = (Double) msgMap.get("duration");
                                if (duration != null) {
                                    message.setDuration(duration.intValue());
                                }
                            }
                            if (msgMap.containsKey("thumbnail")) {
                                message.setThumbnailUrl((String) msgMap.get("thumbnail"));
                            }
                            if (msgMap.containsKey("ratio")) {
                                Double ratio = (Double) msgMap.get("ratio");
                                if (ratio != null) {
                                    message.setImageRatio(ratio.floatValue());
                                }
                            }
                            break;
                        case AUDIO:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("duration")) {
                                Double duration = (Double) msgMap.get("duration");
                                if (duration != null) {
                                    message.setDuration(duration.intValue());
                                }
                            }
                            break;
                        case FILE:
                            message.setFileUrl((String) msgMap.get("filePath"));
                            if (msgMap.containsKey("filename")) {
                                message.setFileName((String) msgMap.get("filename"));
                            }
                            if (msgMap.containsKey("length")) {
                                Integer length = (Integer) msgMap.get("length");
                                if (length != null) {
                                    message.setFileLength(length.longValue());
                                }
                            }
                            break;
                        case LOCATION:
                            Double lat = (Double) msgMap.get("lat");
                            Double lon = (Double) msgMap.get("lon");
                            if (lat != null && lon != null) {
                                message.setLatitude(lat);
                                message.setLongitude(lon);
                            }
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
                        Object customData = msgMap.get("customData");
                        if (customData != null) {
                            if (customData instanceof String) {
                                message.setCustomData(new JSONObject(String.valueOf(customData)));
                            } else {
                                message.setCustomData(
                                        Utils.convertMapToJson((Map<String, Object>) customData));
                            }
                        }
                    }
                    clientWrapper.conversation().sendMessage(convId, message, result);
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getMessages":
                try {
                    List<String> msgIds = call.argument("msgIds");
                    if (msgIds != null) {
                        clientWrapper.conversation()
                                .getMessages(call.argument("convId"), msgIds.toArray(new String[0]),
                                        result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getLocalMessages":
                try {
                    Integer count = call.argument("count");
                    if (count != null) {
                        clientWrapper.conversation()
                                .getLocalMessages(call.argument("convId"), count, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getLastMessages":
                try {
                    Integer count = call.argument("count");
                    if (count != null) {
                        clientWrapper.conversation()
                                .getLastMessages(call.argument("convId"), count, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getMessagesAfter":
                try {
                    Integer count = call.argument("count");
                    Long seq = call.argument("seq");
                    if (count != null && seq != null) {
                        clientWrapper.conversation()
                                .getMessagesAfter(call.argument("convId"), seq, count, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "getMessagesBefore":
                try {
                    Integer count = call.argument("count");
                    Long seq = call.argument("seq");
                    if (count != null && seq != null) {
                        clientWrapper.conversation()
                                .getMessagesBefore(call.argument("convId"), seq, count, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "updateConversation":
                clientWrapper.conversation()
                        .updateConversation(call.argument("convId"), call.argument("name"),
                                call.argument("avatar"), result);
                break;
            case "setRole":
                Integer role = call.argument("role");
                if (role != null) {
                    if (role == UserRole.ADMIN.getValue()) {
                        clientWrapper.conversation()
                                .setRole(call.argument("convId"), call.argument("userId"),
                                        UserRole.ADMIN, result);
                    } else if (role == UserRole.MEMBER.getValue()) {
                        clientWrapper.conversation()
                                .setRole(call.argument("convId"), call.argument("userId"),
                                        UserRole.MEMBER, result);
                    }
                }
                break;
            case "deleteMessages":
                try {
                    List<String> msgIds = call.argument("msgIds");
                    if (msgIds != null) {
                        clientWrapper.conversation()
                                .deleteMessages(call.argument("convId"),
                                        new JSONArray(msgIds.toArray(new String[0])), result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "revokeMessages":
                try {
                    List<String> msgIds = call.argument("msgIds");
                    if (msgIds != null) {
                        clientWrapper.conversation()
                                .revokeMessages(call.argument("convId"),
                                        new JSONArray(msgIds.toArray(new String[0])),
                                        Boolean.TRUE.equals(call.argument("isDeleted")), result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "markAsRead":
                clientWrapper.conversation().markAsRead(call.argument("convId"), result);
                break;
            case "editMsg":
                clientWrapper.message()
                        .edit(call.argument("convId"), call.argument("msgId"),
                                call.argument("content"), result);
                break;
            case "pinOrUnPin":
                clientWrapper.message()
                        .pinOrUnPin(call.argument("convId"), call.argument("msgId"),
                                Boolean.TRUE.equals(call.argument("pinOrUnPin")), result);
                break;
            case "getChatProfile":
                clientWrapper.getChatProfile(call.argument("key"), result);
                break;
            case "getLiveChatToken":
                clientWrapper.getLiveChatToken(call.argument("key"), call.argument("name"),
                        call.argument("email"), result);
                break;
            case "updateUserInfo":
                clientWrapper.updateUserInfo(call.argument("name"), call.argument("email"),
                        call.argument("avatar"), call.argument("phone"), result);
                break;
            case "createLiveChatConversation":
                String customData = null;
                if (call.hasArgument("customData")) {
                    customData = call.argument("customData");
                }
                clientWrapper.createLiveChatConversation(call.argument("queueId"), customData,
                        result);
                break;
            case "createLiveChatTicket":
                clientWrapper.createLiveChatTicket(call.argument("key"), call.argument("name"),
                        call.argument("email"), call.argument("description"), result);
                break;
            case "sendChatTranscript":
                clientWrapper.conversation()
                        .sendChatTranscript(call.argument("convId"), call.argument("email"),
                                call.argument("domain"), result);
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
                    Object optionsObject = call.argument("options");
                    if (optionsObject != null) {
                        JSONObject videoOptionsObject =
                                Utils.convertMapToJson((Map<String, Object>) optionsObject);
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

                        clientWrapper.videoConference()
                                .createLocalVideoTrack(context, options, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "video.createCaptureScreenTrack":
                clientWrapper.videoConference().createCaptureScreenTrack(result);
                break;
            case "room.publish":
                clientWrapper.videoConference()
                        .publish(call.argument("roomId"), call.argument("localId"), result);
                break;
            case "room.unpublish":
                clientWrapper.videoConference()
                        .unpublish(call.argument("roomId"), call.argument("localId"), result);
                break;
            case "room.subscribe":
                try {
                    Map<String, Object> optionMap = call.argument("options");
                    if (optionMap != null) {
                        JSONObject videoOptionsObject = Utils.convertMapToJson(optionMap);
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
                        clientWrapper.videoConference()
                                .subscribe(call.argument("roomId"), call.argument("trackId"),
                                        options, result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "room.unsubscribe":
                clientWrapper.videoConference()
                        .unsubscribe(call.argument("roomId"), call.argument("trackId"), result);
                break;
            case "room.leave":
                clientWrapper.videoConference()
                        .leave(call.argument("roomId"),
                                Boolean.TRUE.equals(call.argument("allClient")), result);
                break;
            case "room.sendMessage":
                try {
                    Object msgObject = call.argument("msg");
                    if (msgObject != null) {
                        clientWrapper.videoConference()
                                .sendMessage(call.argument("roomId"),
                                        Utils.convertMapToJson((Map<String, Object>) msgObject),
                                        result);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeFlutterPlugin.class, e);
                }
                break;
            case "track.mute":
                clientWrapper.videoConference()
                        .mute(call.argument("localId"), Boolean.TRUE.equals(call.argument("mute")),
                                result);
                break;
            case "track.enableVideo":
                clientWrapper.videoConference()
                        .enableVideo(call.argument("localId"),
                                Boolean.TRUE.equals(call.argument("enable")), result);
                break;
            case "track.switchCamera":
                if (call.hasArgument("cameraId")) {
                    clientWrapper.videoConference()
                            .switchCamera(call.argument("localId"), call.argument("cameraId"),
                                    result);
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
        StringeeManager.getInstance().setCaptureManager(ScreenCaptureManager.getInstance(binding));
        sendActionEvent(binding.getActivity().getIntent());
        binding.addOnNewIntentListener(intent -> {
            sendActionEvent(intent);
            return false;
        });
    }

    private void sendActionEvent(Intent intent) {
        if (intent != null) {
            String intentAction = intent.getAction();
            if (intentAction != null) {
                if (intentAction.startsWith(Constants.ACTION_BASE)) {
                    PacketSenderThread.getInstance().send(intent);
                }
            }
        }
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
