package com.stringee.stringeeflutterplugin;

import android.widget.FrameLayout;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;

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
            User user = new com.stringee.messaging.User(object.optString("userId", null));
            user.setName(object.optString("name", null));
            user.setAvatarUrl(object.optString("avatarUrl"));
            list.add(user);
        }
        return list;
    }

    public static Conversation getConversationFromMap(Map map) {
        Conversation conversation = new Conversation();

        return conversation;
    }

    public static Map convertConversationToMap(Conversation conversation) {
        Map conversationMap = new HashMap();
        conversationMap.put("id", conversation.getId());
        conversationMap.put("localId", conversation.getLocalId());
        conversationMap.put("name", conversation.getName());
        conversationMap.put("isDistinct", conversation.isDistinct());
        conversationMap.put("isGroup", conversation.isGroup());
        conversationMap.put("isEnded", conversation.isEnded());
        conversationMap.put("clientId", conversation.getClientId());
        conversationMap.put("creator", conversation.getCreator());
        conversationMap.put("createAt", conversation.getCreateAt());
        conversationMap.put("updatedAt", conversation.getUpdateAt());
        conversationMap.put("totalUnread", conversation.getTotalUnread());
        conversationMap.put("text", conversation.getText());
        conversationMap.put("state", conversation.getState());
        conversationMap.put("lastMsgSender", conversation.getLastMsgSender());
        conversationMap.put("lastMsgType", conversation.getLastMsgType());
        conversationMap.put("lastMsgId", conversation.getLastMsgId());
        conversationMap.put("lastMsgSeqReceived", conversation.getLastMsgSeqReceived());
        conversationMap.put("lastTimeNewMsg", conversation.getLastTimeNewMsg());
        conversationMap.put("lastMsgState", conversation.getLastMsgState());

        if (conversation.getLastMsg() != null) {
            try {
                Map lastMsgMap = new HashMap();
                lastMsgMap = Utils.convertJsonToMap(new JSONObject(conversation.getLastMsg()));
                conversationMap.put("lastMsg", lastMsgMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        conversationMap.put("pinnedMsgId", conversation.getPinnedMsgId());

        List<User> participants = conversation.getParticipants();
        JSONArray participantsArray = new JSONArray();
        for (int j = 0; j < participants.size(); j++) {
            participantsArray.put(convertUserToMap(participants.get(j)));
        }
        conversationMap.put("participants", participantsArray.toString());
        return conversationMap;
    }


    public static Message getMessageFromMap(Map map) {
        int type = (int) map.get("type");
        Message message = new Message(type);
        switch (type) {
            case 1:
                message = new Message((String) map.get("content"));
                break;
            case 2:
                message.setFileUrl((String) map.get("filePath"));
                message.setThumbnailUrl((String) map.get("thumbnail"));
                message.setImageRatio((Float) map.get("ratio"));
                break;
            case 3:
                message.setFileUrl((String) map.get("filePath"));
                message.setThumbnailUrl((String) map.get("thumbnail"));
                message.setImageRatio((Float) map.get("ratio"));
                message.setDuration((Integer) map.get("duration"));
                break;
            case 4:
                message.setFileUrl((String) map.get("filePath"));
                message.setDuration((Integer) map.get("duration"));
                break;
            case 5:
                message.setFileUrl((String) map.get("filePath"));
                message.setFileName((String) map.get("filename"));
                message.setFileLength((Long) map.get("length"));
                break;
            case 9:
                message.setLatitude((Double) map.get("lat"));
                message.setLongitude((Double) map.get("lon"));
                break;
            case 10:
                message.setContact((String) map.get("vcard"));
                break;
            case 11:
                message.setStickerCategory((String) map.get("category"));
                message.setStickerName((String) map.get("name"));
                break;
            default:
                break;
        }
        return message;
    }

    public static Map convertMessageToMap(StringeeClient client, Message message) {
        Map messageMap = new HashMap();
        messageMap.put("id", message.getId());
        messageMap.put("convId", message.getConversationId());
        messageMap.put("senderId", message.getSenderId());
        messageMap.put("createdAt", message.getCreatedAt());
        messageMap.put("updateAt", message.getUpdateAt());
        messageMap.put("sequence", message.getSequence());
        messageMap.put("state", message.getState().getValue());
        messageMap.put("msgType", message.getMsgType());
        messageMap.put("type", message.getType());
        String text = "";
        Map contentMap = new HashMap();
        switch (message.getType()) {
            case 1:
            case 6:
                text = message.getText();
                contentMap.put("content", message.getText());
                break;
            case 2:
                Map photoMap = new HashMap();
                photoMap.put("filePath", message.getFileUrl());
                photoMap.put("thumbnail", message.getThumbnailUrl());
                photoMap.put("ratio", message.getImageRatio());
                contentMap.put("photo", photoMap);
                break;
            case 3:
                Map videoMap = new HashMap();
                videoMap.put("filePath", message.getFileUrl());
                videoMap.put("thumbnail", message.getThumbnailUrl());
                videoMap.put("ratio", message.getImageRatio());
                videoMap.put("duration", message.getDuration());
                contentMap.put("video", videoMap);
                break;
            case 4:
                Map audioMap = new HashMap();
                audioMap.put("filePath", message.getFileUrl());
                audioMap.put("duration", message.getDuration());
                contentMap.put("audio", audioMap);
                break;
            case 5:
                Map fileMap = new HashMap();
                fileMap.put("filePath", message.getFileUrl());
                fileMap.put("filename", message.getFileName());
                fileMap.put("length", message.getFileLength());
                contentMap.put("file", fileMap);
                break;
            case 7:
            case 8:
                contentMap.put("content", "");
            case 9:
                Map locationMap = new HashMap();
                locationMap.put("lat", message.getLatitude());
                locationMap.put("lon", message.getLongitude());
                contentMap.put("location", locationMap);
                break;
            case 10:
                Map contactMap = new HashMap();
                contactMap.put("vcard", message.getContact());
                contentMap.put("contact", contactMap);
                break;
            case 11:
                Map stickerMap = new HashMap();
                stickerMap.put("name", message.getStickerName());
                stickerMap.put("category", message.getStickerCategory());
                contentMap.put("sticker", stickerMap);
                break;
            case 100:
                try {
                    JSONObject object = new JSONObject(message.getText());
                    contentMap = Utils.convertJsonToMap(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        messageMap.put("content", contentMap);
        return messageMap;
    }

    public static User getUserFromMap(Map map) {
        String userId = "";
        User user = new User(userId);
        return user;
    }

    public static JSONObject convertUserToMap(User user) {
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("userId", user.getUserId());
            userObject.put("name", user.getName());
            userObject.put("avatarUrl", user.getAvatarUrl());
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return userObject;
    }
}
