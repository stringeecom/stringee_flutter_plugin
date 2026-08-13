package com.stringee.stringeeflutterplugin;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.Message.Type;
import com.stringee.messaging.Queue;
import com.stringee.messaging.User;
import com.stringee.messaging.User.Role;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.TextureViewRenderer;

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

/** Shared conversion, validation, lookup, and main-thread helpers for the Android bridge. */
@SuppressLint("NewApi")
public class Utils {

    private static final String TAG = "StringeeSDK";

    /** Posts {@code runnable} to the Android main thread. */
    public static void post(Runnable runnable) {
        post(runnable, 0);
    }

    /** Posts {@code runnable} to the Android main thread after {@code delayMillis}. */
    public static void post(Runnable runnable, long delayMillis) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * Moves a native video renderer into {@code layout} when the SDK has made it available.
     *
     * <p>The Stringee SDK may return {@code null} while a remote track is being replaced. That
     * state is expected and must not crash the Flutter platform view. A renderer already attached
     * to another Android {@link ViewGroup} is detached before it is added to the destination, since
     * an Android view can have only one parent.</p>
     *
     * @param layout destination Flutter platform-view container
     * @param renderer native Stringee renderer, or {@code null} while media is unavailable
     * @param layoutParams layout parameters used when attaching the renderer
     * @return {@code true} when the renderer was attached; {@code false} when it was unavailable or
     * could not be safely detached from its existing parent
     */
    public static boolean attachVideoRenderer(
            @NonNull FrameLayout layout,
            @Nullable TextureViewRenderer renderer,
            @NonNull FrameLayout.LayoutParams layoutParams
    ) {
        if (renderer == null) {
            return false;
        }

        ViewParent parent = renderer.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(renderer);
        } else if (parent != null) {
            Log.w(TAG, "Unable to detach video renderer from its current parent");
            return false;
        }

        layout.addView(renderer, layoutParams);
        return true;
    }

    /** Returns whether {@code object} is null or an empty supported collection or string. */
    public static boolean isEmpty(@Nullable Object object) {
        if (object != null) {
            if (object instanceof JSONArray) {
                return ((JSONArray) object).length() == 0;
            }
            if (object instanceof List) {
                return ((List<?>) object).isEmpty();
            }
            if (object instanceof CharSequence) {
                CharSequence charSequence = (CharSequence) object;
                if (charSequence.toString().equalsIgnoreCase("null")) {
                    return true;
                } else {
                    return charSequence.toString().trim().isEmpty();
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /** Logs an exception together with the bridge class that raised it. */
    public static <T> void reportException(@NonNull Class<T> clazz, Exception exception) {
        Log.e("Stringee exception", clazz.getName(), exception);
    }

    /** Validates and resolves a first-generation call, reporting failures to Flutter. */
    public static boolean isCallWrapperAvailable(String methodName, String callId, Result result) {
        if (isEmpty(callId)) {
            Log.d(TAG, methodName + ": false - -2 - callId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Log.d(TAG, methodName + ": false - -3 - StringeeCall is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    /** Validates and resolves a Call2 instance, reporting failures to Flutter. */
    public static boolean isCall2WrapperAvailable(String methodName, String callId, Result result) {
        if (isEmpty(callId)) {
            Log.d(TAG, methodName + ": false - -2 - callId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        Call2Wrapper call = StringeeManager.getInstance().getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, methodName + ": false - -3 - StringeeCall2 is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    /** Recursively converts a JSON object to a platform-channel-compatible map. */
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

    /** Converts a platform-channel map to JSON for native Stringee APIs. */
    public static JSONObject convertMapToJson(Map<String, Object> map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    /** Recursively converts a JSON array to a platform-channel-compatible list. */
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

    /** Parses a JSON participant list into native Stringee users. */
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
                if (role == 0) {
                    user.setRole(Role.ADMIN);
                } else if (role == 1) {
                    user.setRole(Role.MEMBER);
                }
            }
            list.add(user);
        }
        return list;
    }

    /** Converts a native chat request to a Dart payload. */
    public static Map<String, Object> convertChatRequestToMap(@NonNull ChatRequest chatRequest) {
        Map<String, Object> chatRequestMap = new HashMap<>();
        chatRequestMap.put("convId", chatRequest.getConvId());
        chatRequestMap.put("customerId", chatRequest.getCustomerId());
        chatRequestMap.put("customerName", chatRequest.getName());
        chatRequestMap.put("channelType", chatRequest.getChannelType().getValue());
        chatRequestMap.put("type", chatRequest.getRequestType().getValue());
        return chatRequestMap;
    }

    /** Converts a native conversation to a Dart payload. */
    public static Map<String, Object> convertConversationToMap(@NonNull Conversation conversation) {
        Map<String, Object> conversationMap = new HashMap<>();
        try {
            conversationMap.put("id", conversation.getId());
            conversationMap.put("name", conversation.getName());
            conversationMap.put("isGroup", conversation.isGroup());
            conversationMap.put("creator", conversation.getCreator());
            conversationMap.put("createdAt", conversation.getCreateAt());
            conversationMap.put("updatedAt", conversation.getUpdateAt());
            conversationMap.put("totalUnread", conversation.getTotalUnread());
            String text = null;
            if (!isEmpty(conversation.getText())) {
                text = conversation.getText();
            }
            conversationMap.put("text", text);
            conversationMap.put("lastMsgSender", conversation.getLastMessage().getSender());
            conversationMap.put(
                    "lastMsgType", conversation.getLastMessage().getMsgType().getValue());
            conversationMap.put("lastMsgId", conversation.getLastMessage().getId());
            conversationMap.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
            conversationMap.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
            conversationMap.put(
                    "lastMsgState", conversation.getLastMessage().getState().getValue());

            if (conversation.getLastMsg() != null) {
                String lastMsg = conversation.getLastMsg();
                if (!isEmpty(lastMsg)) {
                    JSONObject lastMsgMap = new JSONObject(conversation.getLastMsg());
                    conversationMap.put(
                            "text", convertLastMessageToMap(
                                    lastMsgMap,
                                    conversation.getLastMessage().getType()
                            )
                    );
                }
            }
            conversationMap.put("pinnedMsgId", conversation.getPinnedMsgId());

            List<User> participants = conversation.getParticipants();
            List<Map<String, Object>> participantsList = new ArrayList<>();
            for (int j = 0; j < participants.size(); j++) {
                participantsList.add(convertUserToMap(participants.get(j)));
            }
            conversationMap.put("participants", participantsList);
            conversationMap.put("oaId", conversation.getOaId());
            conversationMap.put("customData", conversation.getCustomData());
        } catch (JSONException e) {
            reportException(Utils.class, e);
        }
        return conversationMap;
    }

    /** Converts a conversation's last-message summary to a Dart payload. */
    public static Map<String, Object> convertLastMessageToMap(
            @NonNull JSONObject msgObj, Type type) {
        Map<String, Object> msgMap = new HashMap<>();
        try {
            if (msgObj.has("metadata")) {
                msgMap.put("metadata", Utils.convertJsonToMap(msgObj.optJSONObject("metadata")));
            }
            switch (type) {
                case TEXT:
                case LINK:
                    msgMap.put("text", msgObj.optString("content"));
                    break;
                case PHOTO:
                    JSONObject photoObj = msgObj.optJSONObject("photo");
                    if (photoObj != null) {
                        Map<String, Object> photoMap = new HashMap<>();
                        photoMap.put("filePath", photoObj.optString("filePath"));
                        photoMap.put("fileUrl", photoObj.optString("fileUrl"));
                        photoMap.put("thumbnail", photoObj.optString("thumbnail"));
                        photoMap.put("ratio", ((Integer) photoObj.optInt("ratio")).floatValue());
                        msgMap.put("photo", photoMap);
                    }
                    break;
                case VIDEO:
                    JSONObject videoObj = msgObj.optJSONObject("video");
                    if (videoObj != null) {
                        Map<String, Object> videoMap = new HashMap<>();
                        videoMap.put("filePath", videoObj.optString("filePath"));
                        videoMap.put("fileUrl", videoObj.optString("fileUrl"));
                        videoMap.put("thumbnail", videoObj.optString("thumbnail"));
                        videoMap.put("ratio", ((Integer) videoObj.optInt("ratio")).floatValue());
                        videoMap.put("duration", (double) videoObj.optInt("duration"));
                        msgMap.put("video", videoMap);
                    }
                    break;
                case AUDIO:
                    JSONObject audioObj = msgObj.optJSONObject("audio");
                    if (audioObj != null) {
                        Map<String, Object> audioMap = new HashMap<>();
                        audioMap.put("filePath", audioObj.optString("filePath"));
                        audioMap.put("fileUrl", audioObj.optString("fileUrl"));
                        audioMap.put("duration", (double) audioObj.optInt("duration"));
                        msgMap.put("audio", audioMap);
                    }
                    break;
                case FILE:
                    JSONObject fileObj = msgObj.optJSONObject("file");
                    if (fileObj != null) {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("filePath", fileObj.optString("filePath"));
                        fileMap.put("fileUrl", fileObj.optString("fileUrl"));
                        fileMap.put("fileName", fileObj.optString("fileName"));
                        fileMap.put("fileLength", fileObj.optLong("length"));
                        msgMap.put("file", fileMap);
                    }
                    break;
                case CREATE_CONVERSATION:
                case RENAME_CONVERSATION:
                    msgMap.put("groupName", msgObj.optString("groupName"));
                    msgMap.put("creator", msgObj.optString("creator"));
                    JSONArray participantsArray = msgObj.optJSONArray("participants");
                    List<Object> participants = new ArrayList<>();
                    if (!isEmpty(participantsArray)) {
                        for (int i = 0; i < participantsArray.length(); i++) {
                            participants.add(participantsArray.get(i));
                        }
                    }
                    msgMap.put("participants", participants);
                    break;
                case LOCATION:
                    JSONObject locationObj = msgObj.optJSONObject("location");
                    if (locationObj != null) {
                        Map<String, Object> locationMap = new HashMap<>();
                        locationMap.put("lat", (double) locationObj.optInt("lat"));
                        locationMap.put("lon", (double) locationObj.optInt("lon"));
                        msgMap.put("location", locationMap);
                    }
                    break;
                case CONTACT:
                    JSONObject contactObj = msgObj.optJSONObject("contact");
                    if (contactObj != null) {
                        Map<String, Object> contactMap = new HashMap<>();
                        contactMap.put("vcard", contactObj.optString("vcard"));
                        msgMap.put("contact", contactMap);
                    }
                    break;
                case STICKER:
                    JSONObject stickerObj = msgObj.optJSONObject("sticker");
                    if (stickerObj != null) {
                        Map<String, Object> stickerMap = new HashMap<>();
                        stickerMap.put("name", stickerObj.optString("name"));
                        stickerMap.put("category", stickerObj.optString("category"));
                        msgMap.put("sticker", stickerMap);
                    }
                    break;
                case NOTIFICATION:
                    msgMap = convertNotifyContentToMap(msgObj);
                    break;
            }
        } catch (JSONException e) {
            reportException(Utils.class, e);
        }
        return msgMap;
    }

    /** Converts a native message to a Dart payload. */
    public static Map<String, Object> convertMessageToMap(@NonNull Message message) {
        Map<String, Object> msgMap = new HashMap<>();
        try {
            msgMap.put("id", message.getId());
            msgMap.put("localId", message.getLocalId());
            msgMap.put("convId", message.getConversationId());
            msgMap.put("senderId", message.getSender().getUserId());
            msgMap.put("createdAt", message.getCreatedAt());
            msgMap.put("sequence", message.getSequence());
            msgMap.put("customData", convertJsonToMap(message.getCustomData()));
            msgMap.put("state", message.getState().getValue());
            msgMap.put("type", message.getType().getValue());
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
                    List<Object> participants = new ArrayList<>();
                    if (!isEmpty(participantsArray)) {
                        for (int i = 0; i < participantsArray.length(); i++) {
                            participants.add(participantsArray.get(i));
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
                        reportException(Utils.class, e);
                    }
                    break;
            }
            msgMap.put("content", contentMap);
        } catch (JSONException e) {
            reportException(Utils.class, e);
        }
        return msgMap;
    }

    /** Converts a native conversation user to a Dart payload. */
    public static Map<String, Object> convertUserToMap(@NonNull User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user", user.getUserId());
        userMap.put("displayName", user.getName());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("role", user.getRole().getValue());
        return userMap;
    }

    /** Converts notification-message JSON content to a Dart payload. */
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
                    contentMap.put(
                            "participants", getParticipantsFromNotify(
                                    notifyObject.getJSONArray("participants"))
                    );
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    JSONObject removedInfoObject = notifyObject.optJSONObject("removedInfo");
                    if (removedInfoObject != null) {
                        removeUser.setName(removedInfoObject.optString("displayName", ""));
                    }
                    removeUser.setAvatarUrl(null);
                    contentMap.put("removedInfo", convertUserToMap(removeUser));
                    contentMap.put(
                            "participants", getParticipantsFromNotify(
                                    notifyObject.getJSONArray("participants"))
                    );
                    break;
                case 3:
                    contentMap.put("groupName", notifyObject.get("groupName"));
                    break;
                case 4:
                    contentMap.put("content", notifyObject.toString());
                    break;
            }

        } catch (JSONException e) {
            reportException(Utils.class, e);
        }
        return contentMap;
    }

    /** Converts a native live-chat profile to a Dart payload. */
    public static Map<String, Object> convertChatProfileToMap(@NonNull ChatProfile chatProfile) {
        Map<String, Object> chatProfileMap = new HashMap<>();
        chatProfileMap.put("id", chatProfile.getId());
        chatProfileMap.put("background", chatProfile.getBackground());
        chatProfileMap.put("hour", chatProfile.getBusinessHour());
        chatProfileMap.put("language", chatProfile.getLanguage());
        chatProfileMap.put("logo_url", chatProfile.getLogoUrl());
        chatProfileMap.put("popup_answer_url", chatProfile.getPopupAnswerUrl());
        chatProfileMap.put("portal", chatProfile.getPortalId());
        List<Queue> queues = chatProfile.getQueues();
        List<Map<String, Object>> queueList = new ArrayList<>();
        for (int i = 0; i < queues.size(); i++) {
            queueList.add(convertQueueToMap(queues.get(i)));
        }
        chatProfileMap.put("queues", queueList);
        chatProfileMap.put("auto_create_ticket", chatProfile.isAutoCreateTicket());
        chatProfileMap.put("enabled", chatProfile.isEnabledBusinessHour());
        chatProfileMap.put("facebook_as_livechat", chatProfile.isFacebookAsLivechat());
        chatProfileMap.put("project_id", chatProfile.getProjectId());
        chatProfileMap.put("zalo_as_livechat", chatProfile.isZaloAsLivechat());
        return chatProfileMap;
    }

    /** Converts a native live-chat queue to a Dart payload. */
    public static Map<String, Object> convertQueueToMap(@NonNull Queue queue) {
        Map<String, Object> queueMap = new HashMap<>();
        queueMap.put("id", queue.getId());
        queueMap.put("name", queue.getName());
        return queueMap;
    }

    /** Converts a native video room to a Dart payload. */
    public static Map<String, Object> convertRoomToMap(@NonNull StringeeRoom room) {
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("recorded", room.isRecorded());
        return roomMap;
    }

    /** Converts a native room participant to a Dart payload. */
    public static Map<String, Object> convertRoomUserToMap(@NonNull RemoteParticipant participant) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", participant.getId());
        return userMap;
    }

    /** Converts a local native video track to a Dart payload. */
    public static Map<String, Object> convertLocalVideoTrackToMap(
            @NonNull VideoTrackManager trackManager, String clientId) {
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

    /** Converts a managed video track to a Dart payload. */
    public static Map<String, Object> convertVideoTrackToMap(
            @NonNull VideoTrackManager trackManager
    ) {
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

    /** Converts video-track metadata to a Dart payload. */
    public static Map<String, Object> convertVideoTrackInfoToMap(
            @NonNull VideoTrackManager trackManager
    ) {
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

    /** Creates a unique local track identifier. */
    public static String createLocalId() {
        return "android-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /** Extracts participant payloads from a notification message. */
    public static List<Map<String, Object>> getParticipantsFromNotify(
            @NonNull JSONArray participantsArray
    ) {
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
            reportException(Utils.class, e);
        }
        return resultArray;
    }

    /** Resolves a conversation from local cache and then from the server if necessary. */
    public static void getConversation(
            @NonNull StringeeClient client, @NonNull String convId,
            @NonNull final CallbackListener<Conversation> callbackListener
    ) {
        client.getConversationFromServer(
                convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(final Conversation conversation) {
                        post(() -> callbackListener.onSuccess(conversation));
                    }

                    @Override
                    public void onError(final StringeeError error) {
                        super.onError(error);
                        post(() -> callbackListener.onError(error));
                    }
                }
        );
    }

    /** Resolves one of the requested messages and returns it through {@code callbackListener}. */
    public static void getMessage(
            @NonNull final StringeeClient client, @NonNull String convId,
            @NonNull final String[] msgId, @NonNull final CallbackListener<Message> callbackListener
    ) {
        getConversation(
                client, convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                conversation.getMessages(
                                        client, msgId, new CallbackListener<List<Message>>() {
                                            @Override
                                            public void onSuccess(List<Message> messages) {
                                                if (messages != null && !messages.isEmpty()) {
                                                    callbackListener.onSuccess(messages.get(0));
                                                }
                                            }

                                            @Override
                                            public void onError(StringeeError stringeeError) {
                                                super.onError(stringeeError);
                                                callbackListener.onError(stringeeError);
                                            }
                                        }
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError error) {
                        super.onError(error);
                        post(() -> callbackListener.onError(error));
                    }
                }
        );
    }

    /** Resolves a pending chat request for a conversation. */
    public static void getChatRequest(
            @NonNull final StringeeClient client,
            @NonNull final String convId,
            @NonNull CallbackListener<ChatRequest> callbackListener
    ) {
        client.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                post(() -> {
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
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                post(() -> callbackListener.onError(stringeeError));
            }
        });
    }


}
