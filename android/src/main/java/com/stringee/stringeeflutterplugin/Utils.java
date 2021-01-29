package com.stringee.stringeeflutterplugin;

import androidx.annotation.NonNull;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.ConversationManager.UserRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {
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
                    user.setRole("admin");
                } else if (role == UserRole.Admin.getValue()) {
                    user.setRole("member");
                }
            }
            list.add(user);
        }
        return list;
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
            conversationMap.put("text", conversation.getText());
            conversationMap.put("lastMsgSender", conversation.getLastMsgSender());
            conversationMap.put("lastMsgType", conversation.getLastMsgType());
            conversationMap.put("lastMsgId", conversation.getLastMsgId());
            conversationMap.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
            conversationMap.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
            conversationMap.put("lastMsgState", conversation.getLastMsgState());

            if (conversation.getLastMsg() != null) {
                JSONObject lastMsgMap = new JSONObject(conversation.getLastMsg());
                conversationMap.put("text", convertJsonToMap(lastMsgMap));
            }
            conversationMap.put("pinnedMsgId", conversation.getPinnedMsgId());

            List<User> participants = conversation.getParticipants();
            List participantsList = new ArrayList();
            for (int j = 0; j < participants.size(); j++) {
                participantsList.add(convertUserToMap(participants.get(j)));
            }
            conversationMap.put("participants", participantsList);
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
            msgMap.put("type", message.getType());
            Map contentMap = new HashMap();
            switch (message.getType()) {
                case Message.TYPE_TEXT:
                case Message.TYPE_LINK:
                    contentMap.put("content", message.getText());
                    break;
                case Message.TYPE_PHOTO:
                    Map photoMap = new HashMap();
                    photoMap.put("filePath", message.getFilePath());
                    photoMap.put("fileUrl", message.getFileUrl());
                    photoMap.put("thumbnail", message.getThumbnailUrl());
                    photoMap.put("ratio", message.getImageRatio());
                    contentMap.put("photo", photoMap);
                    break;
                case Message.TYPE_VIDEO:
                    Map videoMap = new HashMap();
                    videoMap.put("filePath", message.getFilePath());
                    videoMap.put("fileUrl", message.getFileUrl());
                    videoMap.put("thumbnail", message.getThumbnailUrl());
                    videoMap.put("ratio", message.getImageRatio());
                    videoMap.put("duration", message.getDuration());
                    contentMap.put("video", videoMap);
                    break;
                case Message.TYPE_AUDIO:
                    Map audioMap = new HashMap();
                    audioMap.put("filePath", message.getFilePath());
                    audioMap.put("fileUrl", message.getFileUrl());
                    audioMap.put("duration", message.getDuration());
                    contentMap.put("audio", audioMap);
                    break;
                case Message.TYPE_FILE:
                    Map fileMap = new HashMap();
                    fileMap.put("filePath", message.getFilePath());
                    fileMap.put("fileUrl", message.getFileUrl());
                    fileMap.put("filename", message.getFileName());
                    fileMap.put("fileLength", message.getFileLength());
                    contentMap.put("file", fileMap);
                    break;
                case Message.TYPE_CREATE_CONVERSATION:
                case Message.TYPE_RENAME_CONVERSATION:
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
                case Message.TYPE_LOCATION:
                    Map locationMap = new HashMap();
                    locationMap.put("lat", message.getLatitude());
                    locationMap.put("lon", message.getLongitude());
                    contentMap.put("location", locationMap);
                    break;
                case Message.TYPE_CONTACT:
                    Map contactMap = new HashMap();
                    contactMap.put("vcard", message.getContact());
                    contentMap.put("contact", contactMap);
                    break;
                case Message.TYPE_STICKER:
                    Map stickerMap = new HashMap();
                    stickerMap.put("name", message.getStickerName());
                    stickerMap.put("category", message.getStickerCategory());
                    contentMap.put("sticker", stickerMap);
                    break;
                case Message.TYPE_NOTIFICATION:
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
        userMap.put("role", user.getRole());
        return userMap;
    }

    public static Map convertNotifyContentToMap(@NonNull JSONObject notifyObject) {
        Map contentMap = new java.util.HashMap();
        try {
            int type = notifyObject.getInt("type");
            contentMap.put("type", type);
            switch (type) {
                case 1:
                    User addUser = new User(notifyObject.getString("addedby"));
                    addUser.setName(notifyObject.getJSONObject("addedInfo").getString("displayName"));
                    addUser.setAvatarUrl(null);
                    contentMap.put("addedby", convertUserToMap(addUser));
                    contentMap.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    removeUser.setName(notifyObject.getJSONObject("removedInfo").getString("displayName"));
                    removeUser.setAvatarUrl(null);
                    contentMap.put("removedBy", convertUserToMap(removeUser));
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

    public static List getParticipantsFromNotify(@NonNull JSONArray participantsArray) {
        List resultArray = new ArrayList();
        try {
            for (int i = 0; i < participantsArray.length(); i++) {
                JSONObject userObject = new JSONObject();
                userObject = participantsArray.getJSONObject(i);

                User user = new User(userObject.getString("user"));
                user.setName(userObject.optString("displayName", null));
                user.setAvatarUrl(userObject.optString("avatarUrl", null));
                user.setRole(userObject.optString("role", null));

                resultArray.add(convertUserToMap(user));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public static void getConversation(@NonNull StringeeClient client, @NonNull String convId, @NonNull final CallbackListener<Conversation> callbackListener) {
        client.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                callbackListener.onSuccess(conversation);
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                callbackListener.onError(error);
            }
        });
    }

    public static void getLastMessage(@NonNull final StringeeClient client, @NonNull String convId, @NonNull final CallbackListener<Message> callbackListener) {
        getConversation(client, convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(client, 1, true, true, false, new CallbackListener<List<Message>>() {
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

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                callbackListener.onError(error);
            }
        });
    }

    public static void getMessage(@NonNull final StringeeClient client, @NonNull String convId, @NonNull final String[] msgId, @NonNull final CallbackListener<Message> callbackListener) {
        getConversation(client, convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
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

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                callbackListener.onError(error);
            }
        });
    }
}
