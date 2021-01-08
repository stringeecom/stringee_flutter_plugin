package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
    public static boolean isInternetConnected(Context context) {
        boolean haveInternetConnection = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfo) {
            if (info.getTypeName().equalsIgnoreCase("WIFI")) {
                if (info.isConnected()) {
                    haveInternetConnection = true;
                }
            }
            if (info.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (info.isConnected()) {
                    haveInternetConnection = true;
                }
            }
        }
        return haveInternetConnection;
    }

    public static Map convertJsonToMap(JSONObject object) throws JSONException {
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

    public static JSONObject convertConversationToJSON(@NonNull Conversation conversation) {
        JSONObject conversationObject = new JSONObject();
        try {
            conversationObject.put("id", conversation.getId());
            conversationObject.put("localId", conversation.getLocalId());
            conversationObject.put("name", conversation.getName());
            conversationObject.put("isDistinct", conversation.isDistinct());
            conversationObject.put("isGroup", conversation.isGroup());
            conversationObject.put("isEnded", conversation.isEnded());
            conversationObject.put("clientId", conversation.getClientId());
            conversationObject.put("creator", conversation.getCreator());
            conversationObject.put("createAt", conversation.getCreateAt());
            conversationObject.put("updatedAt", conversation.getUpdateAt());
            conversationObject.put("totalUnread", conversation.getTotalUnread());
            conversationObject.put("text", conversation.getText());
            conversationObject.put("state", conversation.getState());
            conversationObject.put("lastMsgSender", conversation.getLastMsgSender());
            conversationObject.put("lastMsgType", conversation.getLastMsgType());
            conversationObject.put("lastMsgId", conversation.getLastMsgId());
            conversationObject.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
            conversationObject.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
            conversationObject.put("lastMsgState", conversation.getLastMsgState());

            if (conversation.getLastMsg() != null) {
                JSONObject lastMsgMap = new JSONObject(conversation.getLastMsg());
                conversationObject.put("lastMsg", lastMsgMap);
            }
            conversationObject.put("pinnedMsgId", conversation.getPinnedMsgId());

            List<User> participants = conversation.getParticipants();
            JSONArray participantsArray = new JSONArray();
            for (int j = 0; j < participants.size(); j++) {
                participantsArray.put(convertUserToJSON(participants.get(j)));
            }
            conversationObject.put("participants", participantsArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return conversationObject;
    }

    public static Message getMessageFromJSON(@NonNull JSONObject object, @NonNull Message message) {
        try {
            switch (message.getType()) {
                case Message.TYPE_TEXT:
                case Message.TYPE_LINK:
                    message = new Message(object.getString("text"));
                    break;
                case Message.TYPE_PHOTO:
                case Message.TYPE_FILE:
                    message.setFilePath(object.optString("filePath", null));
                    break;
                case Message.TYPE_VIDEO:
                case Message.TYPE_AUDIO:
                    message.setFilePath(object.getString("filePath"));
                    message.setDuration(object.getInt("duration"));
                    break;
                case Message.TYPE_LOCATION:
                    message.setLatitude(object.getDouble("latitude"));
                    message.setLongitude(object.getDouble("longitude"));
                    break;
                case Message.TYPE_CONTACT:
                    message.setContact(object.getString("contact"));
                    break;
                case Message.TYPE_STICKER:
                    message.setStickerCategory(object.getString("stickerCategory"));
                    message.setStickerName(object.getString("stickerName"));
                    break;
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static JSONObject convertMessageToJSON(@NonNull Message message) {
        JSONObject msgObject = new JSONObject();
        try {
            msgObject.put("id", message.getId());
            msgObject.put("convId", message.getConversationId());
            msgObject.put("senderId", message.getSenderId());
            msgObject.put("createdAt", message.getCreatedAt());
            msgObject.put("updateAt", message.getUpdateAt());
            msgObject.put("sequence", message.getSequence());
            msgObject.put("isDeleted", message.isDeleted());
            msgObject.put("clientId", message.getClientId());
            msgObject.put("customData", message.getCustomData());
            msgObject.put("state", message.getState().getValue());
            msgObject.put("msgType", message.getMsgType());
            msgObject.put("type", message.getType());
            JSONObject contentObject = new JSONObject();
            switch (message.getType()) {
                case 1:
                case 6:
                    contentObject.put("text", message.getText());
                    break;
                case 2:
                    JSONObject photoObject = new JSONObject();
                    photoObject.put("filePath", message.getFilePath());
                    photoObject.put("fileUrl", message.getFileUrl());
                    photoObject.put("thumbnail", message.getThumbnailUrl());
                    photoObject.put("ratio", message.getImageRatio());
                    contentObject.put("photo", photoObject);
                    break;
                case 3:
                    JSONObject videoObject = new JSONObject();
                    videoObject.put("filePath", message.getFilePath());
                    videoObject.put("fileUrl", message.getFileUrl());
                    videoObject.put("thumbnail", message.getThumbnailUrl());
                    videoObject.put("ratio", message.getImageRatio());
                    videoObject.put("duration", message.getDuration());
                    contentObject.put("video", videoObject);
                    break;
                case 4:
                    JSONObject audioObject = new JSONObject();
                    audioObject.put("filePath", message.getFilePath());
                    audioObject.put("fileUrl", message.getFileUrl());
                    audioObject.put("duration", message.getDuration());
                    contentObject.put("audio", audioObject);
                    break;
                case 5:
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("filePath", message.getFilePath());
                    fileObject.put("fileUrl", message.getFileUrl());
                    fileObject.put("filename", message.getFileName());
                    fileObject.put("length", message.getFileLength());
                    contentObject.put("file", fileObject);
                    break;
                case 7:
                case 8:
                    contentObject.put("text", "");
                case 9:
                    JSONObject locationObject = new JSONObject();
                    locationObject.put("lat", message.getLatitude());
                    locationObject.put("lon", message.getLongitude());
                    contentObject.put("location", locationObject);
                    break;
                case 10:
                    JSONObject contactObject = new JSONObject();
                    contactObject.put("vcard", message.getContact());
                    contentObject.put("contact", contactObject);
                    break;
                case 11:
                    JSONObject stickerObject = new JSONObject();
                    stickerObject.put("name", message.getStickerName());
                    stickerObject.put("category", message.getStickerCategory());
                    contentObject.put("sticker", stickerObject);
                    break;
                case 100:
                    try {
                        contentObject = convertNotifyContentToJSON(new JSONObject(message.getText()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            msgObject.put("content", contentObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgObject;
    }

    public static JSONObject convertUserToJSON(@NonNull User user) {
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("userId", user.getUserId());
            userObject.put("name", user.getName());
            userObject.put("avatarUrl", user.getAvatarUrl());
            userObject.put("role", user.getRole());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userObject;
    }

    public static JSONObject convertNotifyContentToJSON(@NonNull JSONObject notifyObject) {
        JSONObject contentObject = new JSONObject();
        try {
            int type = notifyObject.getInt("type");
            contentObject.put("type", type);
            switch (type) {
                case 1:
                    User addUser = new User(notifyObject.getString("addedby"));
                    addUser.setName(notifyObject.getJSONObject("addedInfo").getString("displayName"));
                    addUser.setAvatarUrl(null);
                    contentObject.put("addedby", convertUserToJSON(addUser));
                    contentObject.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")).toString());
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    removeUser.setName(notifyObject.getJSONObject("removedInfo").getString("displayName"));
                    removeUser.setAvatarUrl(null);
                    contentObject.put("removedBy", convertUserToJSON(removeUser));
                    contentObject.put("participants", getParticipantsFromNotify(notifyObject.getJSONArray("participants")).toString());
                    break;
                case 3:
                    break;
                case 4:
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contentObject;
    }

    public static JSONArray getParticipantsFromNotify(@NonNull JSONArray participantsArray) {
        JSONArray resultArray = new JSONArray();
        try {
            for (int i = 0; i < participantsArray.length(); i++) {
                JSONObject userObject = new JSONObject();
                userObject = participantsArray.getJSONObject(i);

                User user = new User(userObject.getString("user"));
                user.setName(userObject.optString("displayName", null));
                user.setAvatarUrl(userObject.optString("avatarUrl", null));
                user.setRole(userObject.optString("role", null));

                resultArray.put(convertUserToJSON(user));
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
