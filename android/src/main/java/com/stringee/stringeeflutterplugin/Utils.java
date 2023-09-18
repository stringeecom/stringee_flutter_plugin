package com.stringee.stringeeflutterplugin;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.messaging.ChannelType;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationFilter;
import com.stringee.messaging.Message;
import com.stringee.messaging.Queue;
import com.stringee.messaging.User;
import com.stringee.messaging.User.Role;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.VideoDimensions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.flutter.plugin.common.MethodChannel.Result;

public class Utils {
    public static boolean isStringEmpty(@Nullable CharSequence text) {
        if (text != null) {
            if (text.toString().equalsIgnoreCase("null")) {
                return true;
            } else {
                return text.toString().trim().length() == 0;
            }
        } else {
            return true;
        }
    }

    public static boolean isListEmpty(@Nullable List list) {
        if (list != null) {
            return list.isEmpty();
        } else {
            return true;
        }
    }

    public static boolean isArrayEmpty(@Nullable JSONArray array) {
        if (array != null) {
            return array.length() == 0;
        } else {
            return true;
        }
    }

    public static boolean isCallWrapperAvailable(String methodName, String callId, Result result) {
        if (callId == null || callId.isEmpty()) {
            Log.d(StringeeFlutterPlugin.TAG, methodName + ": false - -2 - callId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Log.d(StringeeFlutterPlugin.TAG, methodName + ": false - -3 - StringeeCall is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    public static boolean isCall2WrapperAvailable(String methodName, String callId, Result result) {
        if (callId == null || callId.isEmpty()) {
            Log.d(StringeeFlutterPlugin.TAG, methodName + ": false - -2 - callId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        Call2Wrapper call = StringeeManager.getInstance().getCall2sMap().get(callId);
        if (call == null) {
            Log.d(StringeeFlutterPlugin.TAG, methodName + ": false - -3 - StringeeCall2 is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    public static Map<String, Object> convertJsonToMap(JSONObject object) throws JSONException {
        if (object != null) {
            Map<String, Object> map = new HashMap<>();
            Iterator<String> keysItr = object.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = object.get(key);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = convertJsonToMap((JSONObject) value);
                }
                map.put(key, value);
            }
            return map;
        } else {
            return null;
        }
    }

    public static JSONObject convertMapToJson(Map<String, Object> map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = convertJsonToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static List<User> getListUser(String userList) throws JSONException {
        JSONArray array = new JSONArray(userList);
        List<User> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            User user = new User(object.optString("userId", ""));
            user.setName(object.optString("name", ""));
            user.setAvatarUrl(object.optString("avatarUrl", ""));
            if (object.has("role")) {
                short role = (short) object.getInt("role");
                switch (role) {
                    case 0:
                        user.setRole(Role.ADMIN);
                        break;
                    case 1:
                    default:
                        user.setRole(Role.MEMBER);
                        break;
                }
            }
            list.add(user);
        }
        return list;
    }

    public static Map<String, Object> convertChatRequestToMap(@NonNull ChatRequest chatRequest) {
        Map<String, Object> chatRequestMap = new HashMap<>();
        chatRequestMap.put("convId", chatRequest.getConvId());
        chatRequestMap.put("customerId", chatRequest.getCustomerId());
        chatRequestMap.put("customerName", chatRequest.getName());
        chatRequestMap.put("channelType", chatRequest.getChannelType().getValue());
        chatRequestMap.put("type", chatRequest.getRequestType().getValue());
        return chatRequestMap;
    }

    public static Map<String, Object> convertConversationToMap(@NonNull Conversation conversation) {
        Map<String, Object> conversationMap = new HashMap<>();
        conversationMap.put("id", conversation.getId());
        conversationMap.put("name", conversation.getName());
        conversationMap.put("isGroup", conversation.isGroup());
        conversationMap.put("creator", conversation.getCreator());
        conversationMap.put("createdAt", conversation.getCreateAt());
        conversationMap.put("updatedAt", conversation.getUpdateAt());
        conversationMap.put("totalUnread", conversation.getTotalUnread());
        String text = null;
        if (!Utils.isStringEmpty(conversation.getText())) {
            text = conversation.getText();
        }
        conversationMap.put("text", text);
        conversationMap.put("lastMsg", Utils.convertMessageToMap(conversation.getLastMessage()));
        conversationMap.put("pinnedMsgId", conversation.getPinnedMsgId());

        List<User> participants = conversation.getParticipants();
        List<Map<String, Object>> participantsList = new ArrayList<>();
        for (int j = 0; j < participants.size(); j++) {
            participantsList.add(convertUserToMap(participants.get(j)));
        }
        conversationMap.put("participants", participantsList);
        conversationMap.put("oaId", conversation.getOaId());
        conversationMap.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
        conversationMap.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
        conversationMap.put("localId", conversation.getLocalId());
        conversationMap.put("channelType", conversation.getChannelType().getValue());
        conversationMap.put("ended", conversation.isEnded());
        conversationMap.put("lastSequence", conversation.getLastSequence());
        return conversationMap;
    }

    public static Map<String, Object> convertMessageToMap(@NonNull Message message) {
        Map<String, Object> msgMap = new HashMap<>();
        try {
            msgMap.put("id", message.getId());
            msgMap.put("localId", message.getLocalId());
            msgMap.put("convId", message.getConversationId());
            msgMap.put("senderId", message.getSenderId());
            msgMap.put("createdAt", message.getCreatedAt());
            msgMap.put("sequence", message.getSequence());
            msgMap.put("customData", convertJsonToMap(message.getCustomData()));
            msgMap.put("state", message.getState().getValue());
            msgMap.put("type", message.getType().getValue());
            msgMap.put("convLocalId", message.getConversationLocalId());
            msgMap.put("sender", Utils.convertUserToMap(message.getSender()));
            msgMap.put("updateAt", message.getUpdateAt());
            msgMap.put("deleted", message.isDeleted());
            Map<String, Object> contentMap = new HashMap<>();
            switch (message.getType()) {
                case TEXT:
                case LINK:
                    contentMap.put("content", message.getText());
                    break;
                case PHOTO:
                    Map<String, Object> photoMap = new HashMap<>();
                    photoMap.put("filePath", message.getFilePath());
                    photoMap.put("fileUrl", message.getFileUrl());
                    photoMap.put("thumbnail", message.getThumbnailUrl());
                    photoMap.put("ratio", message.getImageRatio());
                    contentMap.put("photo", photoMap);
                    break;
                case VIDEO:
                    Map<String, Object> videoMap = new HashMap<>();
                    videoMap.put("filePath", message.getFilePath());
                    videoMap.put("fileUrl", message.getFileUrl());
                    videoMap.put("thumbnail", message.getThumbnailUrl());
                    videoMap.put("ratio", message.getImageRatio());
                    videoMap.put("duration", (double) message.getDuration());
                    contentMap.put("video", videoMap);
                    break;
                case AUDIO:
                    Map<String, Object> audioMap = new HashMap<>();
                    audioMap.put("filePath", message.getFilePath());
                    audioMap.put("fileUrl", message.getFileUrl());
                    audioMap.put("duration", (double) message.getDuration());
                    contentMap.put("audio", audioMap);
                    break;
                case FILE:
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("filePath", message.getFilePath());
                    fileMap.put("fileUrl", message.getFileUrl());
                    fileMap.put("fileName", message.getFileName());
                    fileMap.put("fileLength", message.getFileLength());
                    contentMap.put("file", fileMap);
                    break;
                case CREATE_CONVERSATION:
                case RENAME_CONVERSATION:
                    JSONObject messageObject = new JSONObject(message.getText());
                    contentMap.put("groupName", messageObject.getString("groupName"));
                    contentMap.put("creator", messageObject.getString("creator"));
                    JSONArray participantsArray = messageObject.getJSONArray("participants");
                    List<Map<String, Object>> participants = new ArrayList<>();
                    if (!Utils.isArrayEmpty(participantsArray)) {
                        for (int i = 0; i < participantsArray.length(); i++) {
                            Map<String, Object> participantMap = (Map<String, Object>) participantsArray.get(i);
                            participants.add(participantMap);
                        }
                    }
                    contentMap.put("participants", participants);
                    break;
                case LOCATION:
                    Map<String, Object> locationMap = new HashMap<>();
                    locationMap.put("lat", message.getLatitude());
                    locationMap.put("lon", message.getLongitude());
                    contentMap.put("location", locationMap);
                    break;
                case CONTACT:
                    Map<String, Object> contactMap = new HashMap<>();
                    contactMap.put("vcard", message.getContact());
                    contentMap.put("contact", contactMap);
                    break;
                case STICKER:
                    Map<String, Object> stickerMap = new HashMap<>();
                    stickerMap.put("name", message.getStickerName());
                    stickerMap.put("category", message.getStickerCategory());
                    contentMap.put("sticker", stickerMap);
                    break;
                case NOTIFICATION:
                    try {
                        contentMap = convertNotifyContentToMap(new JSONObject(message.getText()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            msgMap.put("content", contentMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgMap;
    }

    public static Map<String, Object> convertUserToMap(@NonNull User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user", user.getUserId());
        userMap.put("displayName", user.getName());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("email", user.getEmail());
        userMap.put("phone", user.getPhone());
        userMap.put("location", user.getLocation());
        userMap.put("browser", user.getBrowser());
        userMap.put("device", user.getDevice());
        userMap.put("ipAddress", user.getIpAddress());
        userMap.put("hostName", user.getHostName());
        userMap.put("userAgent", user.getUserAgent());
        userMap.put("lastMsgSeqReceived", user.getLastMsgSeqReceived());
        userMap.put("lastMsgSeqSeen", user.getLastMsgSeqSeen());
        return userMap;
    }

    public static User convertMapToUser(@NonNull Map<String, Object> map) {
        User user = new User();
        user.setName((String) map.get("name"));
        user.setEmail((String) map.get("email"));
        user.setAvatarUrl((String) map.get("avatarUrl"));
        user.setLocation((String) map.get("location"));
        user.setBrowser((String) map.get("browser"));
        user.setDevice((String) map.get("device"));
        user.setIpAddress((String) map.get("ipAddress"));
        user.setHostName((String) map.get("hostName"));
        user.setUserAgent((String) map.get("userAgent"));
        return user;
    }

    public static StringeeVideoTrack.Options convertMapToStringeeVideoOption(@NonNull Map<String, Object> map) {
        StringeeVideoTrack.Options options = new StringeeVideoTrack.Options();
        options.screen((Boolean) map.get("audio"));
        options.video((Boolean) map.get("video"));
        options.audio((Boolean) map.get("screen"));
        String videoDimensions = (String) map.get("videoDimension");
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
        return options;
    }

    public static Map<String, Object> convertNotifyContentToMap(@NonNull JSONObject notifyObject) {
        Map<String, Object> contentMap = new HashMap<>();
        try {
            int type = notifyObject.optInt("type");
            contentMap.put("type", type);
            switch (type) {
                case 1:
                    User addUser = new User(notifyObject.getString("addedby"));
                    JSONObject addedInfoObject = notifyObject.optJSONObject("addedInfo");
                    if (addedInfoObject != null) {
                        addUser.setName(addedInfoObject.optString("displayName", ""));
                    }
                    addUser.setAvatarUrl(null);
                    contentMap.put("addedInfo", convertUserToMap(addUser));
                    contentMap.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    JSONObject removedInfoObject = notifyObject.optJSONObject("removedInfo");
                    if (removedInfoObject != null) {
                        removeUser.setName(removedInfoObject.optString("displayName", ""));
                    }
                    removeUser.setAvatarUrl(null);
                    contentMap.put("removedInfo", convertUserToMap(removeUser));
                    contentMap.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 3:
                    contentMap.put("groupName", notifyObject.get("groupName"));
                    break;
                case 4:
                    contentMap.put("content", notifyObject.toString());
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contentMap;
    }

    public static Map<String, Object> convertChatProfileToMap(@NonNull ChatProfile chatProfile) {
        Map<String, Object> chatProfileMap = new HashMap<>();
        chatProfileMap.put("id", chatProfile.getId());
        chatProfileMap.put("portalId", chatProfile.getPortalId());
        chatProfileMap.put("projectId", chatProfile.getProjectId());
        chatProfileMap.put("language", chatProfile.getLanguage());
        chatProfileMap.put("background", chatProfile.getBackground());
        chatProfileMap.put("isAutoCreateTicket", chatProfile.isAutoCreateTicket());
        chatProfileMap.put("popupAnswerUrl", chatProfile.getPopupAnswerUrl());
        chatProfileMap.put("numberOfAgents", chatProfile.getNumberOfAgents());
        chatProfileMap.put("logoUrl", chatProfile.getLogoUrl());
        chatProfileMap.put("enabledBusinessHour", chatProfile.isEnabledBusinessHour());
        chatProfileMap.put("businessHourId", chatProfile.getBusinessHourId());
        chatProfileMap.put("businessHour", chatProfile.getBusinessHour());
        List<Queue> queues = chatProfile.getQueues();
        List<Map<String, Object>> queueList = new ArrayList<>();
        for (int i = 0; i < queues.size(); i++) {
            queueList.add(convertQueueToMap(queues.get(i)));
        }
        chatProfileMap.put("queues", queueList);
        chatProfileMap.put("facebookAsLivechat", chatProfile.isFacebookAsLivechat());
        chatProfileMap.put("zaloAsLivechat", chatProfile.isZaloAsLivechat());
        return chatProfileMap;
    }

    public static Map<String, Object> convertQueueToMap(@NonNull Queue queue) {
        Map<String, Object> queueMap = new HashMap<>();
        queueMap.put("id", queue.getId());
        queueMap.put("name", queue.getName());
        return queueMap;
    }

    public static Map<String, Object> convertRoomToMap(@NonNull StringeeRoom room) {
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("recorded", room.isRecorded());
        return roomMap;
    }

    public static Map<String, Object> convertRoomUserToMap(@NonNull RemoteParticipant participant) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", participant.getId());
        return userMap;
    }

    public static Map<String, Object> convertLocalVideoTrackToMap(@NonNull VideoTrackManager trackManager, String clientId) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        trackMap.put("isLocal", videoTrack.isLocal());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", clientId);
        trackMap.put("publisher", userMap);
        return trackMap;
    }

    public static Map<String, Object> convertVideoTrackToMap(@NonNull VideoTrackManager trackManager) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        trackMap.put("isLocal", videoTrack.isLocal());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", videoTrack.getUserId());
        trackMap.put("publisher", userMap);
        return trackMap;
    }

    public static Map<String, Object> convertVideoTrackInfoToMap(@NonNull VideoTrackManager trackManager) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", videoTrack.getUserId());
        trackMap.put("publisher", userMap);
        return trackMap;
    }

    public static String createLocalId() {
        return "android-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    public static List<Map<String, Object>> getParticipantsFromNotify(@NonNull JSONArray participantsArray) {
        List<Map<String, Object>> resultArray = new ArrayList<>();
        try {
            for (int i = 0; i < participantsArray.length(); i++) {
                JSONObject userObject = participantsArray.getJSONObject(i);
                User user = new User(userObject.getString("user"));
                user.setName(userObject.optString("displayName", ""));
                user.setAvatarUrl(userObject.optString("avatarUrl", ""));
                user.setRole(Role.getRole(userObject.optString("role")));

                resultArray.add(convertUserToMap(user));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public static void getConversation(@NonNull StringeeClient client, @NonNull String convId, @NonNull final CallbackListener<Conversation> callbackListener) {
        client.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onSuccess(conversation);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(error);
                    }
                });
            }
        });
    }

    public static void getMessage(@NonNull final StringeeClient client, @NonNull String convId, @NonNull final String[] msgId, @NonNull final CallbackListener<Message> callbackListener) {
        getConversation(client, convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        conversation.getMessages(client, msgId, new CallbackListener<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                if (messages != null && messages.size() > 0) {
                                    callbackListener.onSuccess(messages.get(0));
                                }
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                callbackListener.onError(stringeeError);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(error);
                    }
                });
            }
        });
    }

    public static void getChatRequest(@NonNull final StringeeClient client, @NonNull final String convId, @NonNull CallbackListener<ChatRequest> callbackListener) {
        client.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ChatRequest finalChatRequest = null;
                        for (int i = 0; i < chatRequestList.size(); i++) {
                            ChatRequest chatRequest = chatRequestList.get(i);
                            if (chatRequest.getConvId().equals(convId)) {
                                finalChatRequest = chatRequest;
                            }
                        }
                        if (finalChatRequest != null) {
                            callbackListener.onSuccess(finalChatRequest);
                        } else {
                            callbackListener.onError(new StringeeError(-3, "No chat request found"));
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(stringeeError);
                    }
                });
            }
        });
    }

    public static ConversationFilter getConversationFilter(String filter) {
        ConversationFilter conversationFilter = new ConversationFilter();
        if (!Utils.isStringEmpty(filter)) {
            try {
                JSONObject jsonObject = new JSONObject(filter);
                if (jsonObject.has("oaId")) {
                    conversationFilter.setOaId(jsonObject.optString("oaId"));
                }
                conversationFilter.setDeleted(jsonObject.optBoolean("isDeleted"));
                conversationFilter.setUnread(jsonObject.optBoolean("isUnread"));
                if (jsonObject.has("chatSupportStatus")) {
                    conversationFilter.setFilterChatStatus(ConversationFilter.ConversationFilterChatSupportStatus.getStats(jsonObject.optInt("chatSupportStatus")));
                }
                conversationFilter.setChannelTypes(getChannelTypes(jsonObject.getJSONArray("channelTypes")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return conversationFilter;
    }

    public static List<ChannelType> getChannelTypes(List<Integer> channels) {
        List<ChannelType> channelTypes = new ArrayList<>();
        for (int channel : channels) {
            channelTypes.add(ChannelType.getType(channel));
        }
        return channelTypes;
    }

    public static List<ChannelType> getChannelTypes(JSONArray channels) throws JSONException {
        List<ChannelType> channelTypes = new ArrayList<>();
        for (int i = 0; i < channels.length(); i++) {
            channelTypes.add(ChannelType.getType(channels.getInt(i)));
        }
        return channelTypes;
    }

    public static boolean isClientConnected(ClientWrapper clientWrapper, String function, Result result) {
        if (!clientWrapper.isConnected()) {
            sendErrorResponse(function, -1, "StringeeClient is disconnected", result);
            return false;
        }
        return true;
    }

    public static void sendErrorResponse(String function, int code, String message, Result result) {
        Log.d(StringeeFlutterPlugin.TAG, function + ": false - " + code + " - " + message);
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        map.put("code", code);
        map.put("message", message);
        result.success(map);
    }

    public static void sendSuccessResponse(String function, Object body, Result result) {
        sendSuccessResponse(function, "body", body, result);
    }

    public static void sendSuccessResponse(String function, String bodyKey, Object body, Result result) {
        Log.d(StringeeFlutterPlugin.TAG, function + ": success");
        Map<String, Object> map = getSuccessMap();
        if (body != null) {
            map.put(bodyKey, body);
        }
        result.success(map);
    }

    private static Map<String, Object> getSuccessMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        return map;
    }
}