package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.exception.StringeeError;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.Queue;
import com.stringee.messaging.User;
import com.stringee.messaging.User.Role;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.StringeeManager.UserRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class Utils {
    private static final String TAG = "StringeeSDK";

    public static boolean isCallWrapperAvaiable(String methodName, String callId, Result result) {
        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, methodName + ": false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Log.d(TAG, methodName + ": false - -3 - StringeeCall is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    public static boolean isCall2WrapperAvaiable(String methodName, String callId, Result result) {
        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, methodName + ": false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return false;
        }

        Call2Wrapper call = StringeeManager.getInstance().getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, methodName + ": false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return false;
        }

        return true;
    }

    public static Map convertJsonToMap(JSONObject object) throws JSONException {
        if (object != null) {
            Map<String, Object> map = new HashMap<String, Object>();
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

    public static JSONObject convertMapToJson(Map map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
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
            User user = new User(object.optString("userId", null));
            user.setName(object.optString("name", null));
            user.setAvatarUrl(object.optString("avatarUrl", null));
            if (object.has("role")) {
                short role = (short) object.getInt("role");
                if (role == UserRole.Admin.getValue()) {
                    user.setRole(Role.ADMIN);
                } else if (role == UserRole.Admin.getValue()) {
                    user.setRole(com.stringee.messaging.User.Role.MEMBER);
                }
            }
            list.add(user);
        }
        return list;
    }

    public static Map convertChatRequestToMap(@NonNull ChatRequest chatRequest) {
        Map chatRequestMap = new HashMap();
        chatRequestMap.put("convId", chatRequest.getConvId());
        chatRequestMap.put("customerId", chatRequest.getCustomerId());
        chatRequestMap.put("customerName", chatRequest.getName());
        chatRequestMap.put("channelType", chatRequest.getChannelType().getValue());
        chatRequestMap.put("type", chatRequest.getRequestType().getValue());
        return chatRequestMap;
    }

    public static Map convertConversationToMap(@NonNull Conversation conversation) {
        Map conversationMap = new HashMap();
        try {
            conversationMap.put("id", conversation.getId());
            conversationMap.put("name", conversation.getName());
            conversationMap.put("isGroup", conversation.isGroup());
            conversationMap.put("creator", conversation.getCreator());
            conversationMap.put("createdAt", conversation.getCreateAt());
            conversationMap.put("updatedAt", conversation.getUpdateAt());
            conversationMap.put("totalUnread", conversation.getTotalUnread());
            String text = null;
            if (!TextUtils.isEmpty(conversation.getText())) {
                text = conversation.getText();
            }
            conversationMap.put("text", text);
            conversationMap.put("lastMsgSender", conversation.getLastMsgSender());
            conversationMap.put("lastMsgType", conversation.getLastMsgType().getValue());
            conversationMap.put("lastMsgId", conversation.getLastMsgId());
            conversationMap.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
            conversationMap.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
            conversationMap.put("lastMsgState", conversation.getLastMsgState().getValue());

            if (conversation.getLastMsg() != null) {
                String lastMsg = conversation.getLastMsg();
                if (!TextUtils.isEmpty(lastMsg)) {
                    JSONObject lastMsgMap = new JSONObject(conversation.getLastMsg());
                    conversationMap.put("text", convertJsonToMap(lastMsgMap));
                }
            }
            conversationMap.put("pinnedMsgId", conversation.getPinnedMsgId());

            List<User> participants = conversation.getParticipants();
            List participantsList = new ArrayList();
            for (int j = 0; j < participants.size(); j++) {
                participantsList.add(convertUserToMap(participants.get(j)));
            }
            conversationMap.put("participants", participantsList);
            conversationMap.put("oaId", conversation.getOaId());
            conversationMap.put("customData", conversation.getCustomData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return conversationMap;
    }

    public static Map convertMessageToMap(@NonNull Message message) {
        Map msgMap = new HashMap();
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
            Map contentMap = new HashMap();
            switch (message.getType()) {
                case TEXT:
                case LINK:
                    contentMap.put("content", message.getText());
                    break;
                case PHOTO:
                    Map photoMap = new HashMap();
                    photoMap.put("filePath", message.getFilePath());
                    photoMap.put("fileUrl", message.getFileUrl());
                    photoMap.put("thumbnail", message.getThumbnailUrl());
                    photoMap.put("ratio", message.getImageRatio());
                    contentMap.put("photo", photoMap);
                    break;
                case VIDEO:
                    Map videoMap = new HashMap();
                    videoMap.put("filePath", message.getFilePath());
                    videoMap.put("fileUrl", message.getFileUrl());
                    videoMap.put("thumbnail", message.getThumbnailUrl());
                    videoMap.put("ratio", message.getImageRatio());
                    videoMap.put("duration", message.getDuration());
                    contentMap.put("video", videoMap);
                    break;
                case AUDIO:
                    Map audioMap = new HashMap();
                    audioMap.put("filePath", message.getFilePath());
                    audioMap.put("fileUrl", message.getFileUrl());
                    audioMap.put("duration", message.getDuration());
                    contentMap.put("audio", audioMap);
                    break;
                case FILE:
                    Map fileMap = new HashMap();
                    fileMap.put("filePath", message.getFilePath());
                    fileMap.put("fileUrl", message.getFileUrl());
                    fileMap.put("filename", message.getFileName());
                    fileMap.put("fileLength", message.getFileLength());
                    contentMap.put("file", fileMap);
                    break;
                case CREATE_CONVERSATION:
                case RENAME_CONVERSATION:
                    JSONObject messageObject = new JSONObject(message.getText());
                    contentMap.put("groupName", messageObject.getString("groupName"));
                    contentMap.put("creator", messageObject.getString("creator"));
                    JSONArray participantsArray = messageObject.getJSONArray("participants");
                    List participants = new ArrayList();
                    for (int i = 0; i < participantsArray.length(); i++) {
                        participants.add(participantsArray.get(i));
                    }
                    contentMap.put("participants", participants);
                    break;
                case LOCATION:
                    Map locationMap = new HashMap();
                    locationMap.put("lat", message.getLatitude());
                    locationMap.put("lon", message.getLongitude());
                    contentMap.put("location", locationMap);
                    break;
                case CONTACT:
                    Map contactMap = new HashMap();
                    contactMap.put("vcard", message.getContact());
                    contentMap.put("contact", contactMap);
                    break;
                case STICKER:
                    Map stickerMap = new HashMap();
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

    public static Map convertUserToMap(@NonNull User user) {
        Map userMap = new HashMap();
        userMap.put("userId", user.getUserId());
        userMap.put("name", user.getName());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("role", user.getRole().getValue());
        return userMap;
    }

    public static Map convertNotifyContentToMap(@NonNull JSONObject notifyObject) {
        Map contentMap = new HashMap();
        try {
            int type = notifyObject.getInt("type");
            contentMap.put("type", type);
            switch (type) {
                case 1:
                    User addUser = new User(notifyObject.getString("addedby"));
                    JSONObject addedInfoObject = notifyObject.optJSONObject("addedInfo");
                    addUser.setName(addedInfoObject.optString("displayName", null));
                    addUser.setAvatarUrl(null);
                    contentMap.put("addedInfo", convertUserToMap(addUser));
                    contentMap.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    JSONObject removedInfoObject = notifyObject.optJSONObject("removedInfo");
                    removeUser.setName(removedInfoObject.optString("displayName", null));
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

    public static Map convertChatProfileToMap(@NonNull ChatProfile chatProfile) {
        Map chatProfileMap = new HashMap();
        chatProfileMap.put("id", chatProfile.getId());
        chatProfileMap.put("background", chatProfile.getBackground());
        chatProfileMap.put("hour", chatProfile.getBusinessHour());
        chatProfileMap.put("language", chatProfile.getLanguage());
        chatProfileMap.put("logo_url", chatProfile.getLogoUrl());
        chatProfileMap.put("popup_answer_url", chatProfile.getPopupAnswerUrl());
        chatProfileMap.put("portal", chatProfile.getPortalId());
        List<Queue> queues = chatProfile.getQueues();
        List queueList = new ArrayList();
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

    public static Map convertQueueToMap(@NonNull Queue queue) {
        Map queueMap = new HashMap();
        queueMap.put("id", queue.getId());
        queueMap.put("name", queue.getName());
        return queueMap;
    }

    public static List getParticipantsFromNotify(@NonNull JSONArray participantsArray) {
        List resultArray = new ArrayList();
        try {
            for (int i = 0; i < participantsArray.length(); i++) {
                JSONObject userObject = new JSONObject();
                userObject = participantsArray.getJSONObject(i);

                User user = new User(userObject.getString("user"));
                user.setName(userObject.optString("displayName", null));
                user.setAvatarUrl(userObject.optString("avatarUrl", null));
                user.setRole(Role.getRole(userObject.optString("role")));

                resultArray.add(convertUserToMap(user));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public static void getConversation(@NonNull com.stringee.StringeeClient client, @NonNull String convId, @NonNull final CallbackListener<Conversation> callbackListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        client.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onSuccess(conversation);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(error);
                    }
                });
            }
        });
    }

    public static void getLastMessage(@NonNull final com.stringee.StringeeClient client, @NonNull String convId, @NonNull final CallbackListener<Message> callbackListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        getConversation(client, convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversation.getLastMessages(client, 1, true, true, false, new CallbackListener<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (messages != null && messages.size() > 0) {
                                            callbackListener.onSuccess(messages.get(0));
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callbackListener.onError(stringeeError);
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(error);
                    }
                });
            }
        });
    }

    public static void getMessage(@NonNull final com.stringee.StringeeClient client, @NonNull String convId, @NonNull final String[] msgId, @NonNull final CallbackListener<Message> callbackListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        getConversation(client, convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                handler.post(new Runnable() {
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(error);
                    }
                });
            }
        });
    }

    public static void getChatRequest(@NonNull final com.stringee.StringeeClient client, @NonNull final String convId, @NonNull CallbackListener<ChatRequest> callbackListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        client.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                handler.post(new Runnable() {
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackListener.onError(stringeeError);
                    }
                });
            }
        });
    }
}