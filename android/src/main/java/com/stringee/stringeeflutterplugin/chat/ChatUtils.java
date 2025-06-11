package com.stringee.stringeeflutterplugin.chat;

import androidx.annotation.NonNull;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.Queue;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.chat.enumeration.UserRole;
import com.stringee.stringeeflutterplugin.common.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUtils {
    public static void getConversation(@NonNull StringeeClient client, @NonNull String convId,
                                       @NonNull final CallbackListener<Conversation> callbackListener) {
        client.getConversationFromServer(convId, new CallbackListener<>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> callbackListener.onSuccess(conversation));
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                Utils.post(() -> callbackListener.onError(error));
            }
        });
    }

    public static void getMessage(@NonNull final StringeeClient client, @NonNull String convId,
                                  @NonNull final String[] msgId,
                                  @NonNull final CallbackListener<Message> callbackListener) {
        getConversation(client, convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Utils.post(new Runnable() {
                    @Override
                    public void run() {
                        conversation.getMessages(client, msgId, new CallbackListener<>() {
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
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                Utils.post(() -> callbackListener.onError(error));
            }
        });
    }

    public static void getChatRequest(@NonNull final StringeeClient client,
                                      @NonNull final String convId,
                                      @NonNull CallbackListener<ChatRequest> callbackListener) {
        client.getChatRequests(new CallbackListener<>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                Utils.post(() -> {
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
                Utils.post(() -> callbackListener.onError(stringeeError));
            }
        });
    }

    public static List<User> getListUser(String userList) throws JSONException {
        JSONArray array = new JSONArray(userList);
        List<User> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            User user = new User(object.optString("userId"));
            user.setName(object.optString("name"));
            user.setAvatarUrl(object.optString("avatarUrl"));
            if (object.has("role")) {
                short role = (short) object.getInt("role");
                if (role == UserRole.ADMIN.getValue()) {
                    user.setRole(User.Role.ADMIN);
                } else if (role == UserRole.MEMBER.getValue()) {
                    user.setRole(User.Role.MEMBER);
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
        try {
            conversationMap.put("id", conversation.getId());
            conversationMap.put("name", conversation.getName());
            conversationMap.put("isGroup", conversation.isGroup());
            conversationMap.put("creator", conversation.getCreator());
            conversationMap.put("createdAt", conversation.getCreateAt());
            conversationMap.put("updatedAt", conversation.getUpdateAt());
            conversationMap.put("totalUnread", conversation.getTotalUnread());
            String text = null;
            if (!Utils.isEmpty(conversation.getText())) {
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
                if (!Utils.isEmpty(lastMsg)) {
                    JSONObject lastMsgMap = new JSONObject(conversation.getLastMsg());
                    conversationMap.put("text",
                            convertLastMessageToMap(lastMsgMap, conversation.getLastMsgType()));
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
            Utils.reportException(Utils.class, e);
        }
        return conversationMap;
    }

    public static Map<String, Object> convertLastMessageToMap(@NonNull JSONObject msgObj,
                                                              Message.Type type) {
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
                    if (!Utils.isEmpty(participantsArray)) {
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
            Utils.reportException(Utils.class, e);
        }
        return msgMap;
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
            msgMap.put("customData", Utils.convertJsonToMap(message.getCustomData()));
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
                    if (!Utils.isEmpty(participantsArray)) {
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
                        Utils.reportException(Utils.class, e);
                    }
                    break;
            }
            msgMap.put("content", contentMap);
        } catch (JSONException e) {
            Utils.reportException(Utils.class, e);
        }
        return msgMap;
    }

    public static Map<String, Object> convertUserToMap(@NonNull User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user", user.getUserId());
        userMap.put("displayName", user.getName());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("role", user.getRole().getValue());
        return userMap;
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
                        addUser.setName(addedInfoObject.optString("displayName"));
                    }
                    addUser.setAvatarUrl(null);
                    contentMap.put("addedInfo", convertUserToMap(addUser));
                    contentMap.put("participants",
                            getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 2:
                    User removeUser = new User(notifyObject.getString("removedBy"));
                    JSONObject removedInfoObject = notifyObject.optJSONObject("removedInfo");
                    if (removedInfoObject != null) {
                        removeUser.setName(removedInfoObject.optString("displayName"));
                    }
                    removeUser.setAvatarUrl(null);
                    contentMap.put("removedInfo", convertUserToMap(removeUser));
                    contentMap.put("participants",
                            getParticipantsFromNotify(notifyObject.getJSONArray("participants")));
                    break;
                case 3:
                    contentMap.put("groupName", notifyObject.get("groupName"));
                    break;
                case 4:
                    contentMap.put("content", notifyObject.toString());
                    break;
            }
        } catch (JSONException e) {
            Utils.reportException(Utils.class, e);
        }
        return contentMap;
    }

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

    public static Map<String, Object> convertQueueToMap(@NonNull Queue queue) {
        Map<String, Object> queueMap = new HashMap<>();
        queueMap.put("id", queue.getId());
        queueMap.put("name", queue.getName());
        return queueMap;
    }

    public static List<Map<String, Object>> getParticipantsFromNotify(
            @NonNull JSONArray participantsArray) {
        List<Map<String, Object>> resultArray = new ArrayList<>();
        try {
            for (int i = 0; i < participantsArray.length(); i++) {
                JSONObject userObject = participantsArray.getJSONObject(i);

                User user = new User(userObject.getString("user"));
                user.setName(userObject.optString("displayName"));
                user.setAvatarUrl(userObject.optString("avatarUrl"));
                user.setRole(User.Role.getRole(userObject.optString("role")));

                resultArray.add(convertUserToMap(user));
            }
        } catch (JSONException e) {
            Utils.reportException(Utils.class, e);
        }
        return resultArray;
    }
}
