package com.stringee.stringeeflutterplugin.chat;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.chat.enumeration.UserRole;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ConversationManager {
    private final ClientWrapper clientWrapper;

    public ConversationManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    /**
     * Delete conversation
     *
     * @param convId
     * @param result
     */
    public void delete(final String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "delete: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "deleteConversation: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.delete(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "deleteConversation: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "deleteConversation: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "deleteConversation: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Add participants
     *
     * @param convId
     * @param participants
     * @param result
     */
    public void addParticipants(final String convId, final List<User> participants,
                                final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "addParticipants: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "addParticipants: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.addParticipants(clientWrapper.getClient(), participants,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<User> users) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "addParticipants: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> participantsArray = new ArrayList<>();
                                    for (int j = 0; j < users.size(); j++) {
                                        participantsArray.add(
                                                ChatUtils.convertUserToMap(users.get(j)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", participantsArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG,
                                            "addParticipants: false - " + stringeeError.getCode() +
                                                    " - " + stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "addParticipants: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Remove participants
     *
     * @param convId
     * @param participants
     * @param result
     */
    public void removeParticipants(final String convId, final List<User> participants,
                                   final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "removeParticipants: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "removeParticipants: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.removeParticipants(clientWrapper.getClient(), participants,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<User> users) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "removeParticipants: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> participantsArray = new ArrayList<>();
                                    for (int j = 0; j < users.size(); j++) {
                                        participantsArray.add(
                                                ChatUtils.convertUserToMap(users.get(j)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", participantsArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "removeParticipants: false - " +
                                            stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "removeParticipants: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send message
     *
     * @param message
     * @param result
     */
    public void sendMessage(final String convId, final Message message, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "sendMessage: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "sendMessage: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendMessage(clientWrapper.getClient(), message, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "sendMessage: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "sendMessage: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "sendMessage: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages with Id
     *
     * @param convId
     * @param msgIds
     * @param result
     */
    public void getMessages(String convId, final String[] msgIds, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "getMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "getMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<Message> messages) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getMessages: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> msgArray = new ArrayList<>();
                                    for (int i = 0; i < messages.size(); i++) {
                                        msgArray.add(
                                                ChatUtils.convertMessageToMap(messages.get(i)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", msgArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG,
                                            "getMessages: false - " + stringeeError.getCode() +
                                                    " - " + stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getMessages: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get local messages
     *
     * @param convId
     * @param count
     * @param result
     */
    public void getLocalMessages(String convId, final int count, final Result result) {
        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "getLocalMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(clientWrapper.getClient(), count,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<Message> messages) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getLocalMessages: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> msgArray = new ArrayList<>();
                                    for (int i = 0; i < messages.size(); i++) {
                                        msgArray.add(
                                                ChatUtils.convertMessageToMap(messages.get(i)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", msgArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Map<String, Object> map = new HashMap<>();
                                    Log.d(Constants.TAG,
                                            "getLocalMessages: false - " + stringeeError.getCode() +
                                                    " - " + stringeeError.getMessage());
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getLocalMessages: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get last messages
     *
     * @param convId
     * @param count
     * @param result
     */
    public void getLastMessages(String convId, final int count, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "getLastMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "getLastMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), count,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<Message> messages) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getLastMessages: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> msgArray = new ArrayList<>();
                                    for (int i = 0; i < messages.size(); i++) {
                                        msgArray.add(
                                                ChatUtils.convertMessageToMap(messages.get(i)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", msgArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG,
                                            "getLastMessages: false - " + stringeeError.getCode() +
                                                    " - " + stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getLastMessages: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages which have sequence greater than seq
     *
     * @param convId
     * @param seq
     * @param count
     * @param result
     */
    public void getMessagesAfter(String convId, final long seq, final int count,
                                 final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "getMessagesAfter: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "getMessagesAfter: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(clientWrapper.getClient(), seq, count,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<Message> messages) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getMessagesAfter: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> msgArray = new ArrayList<>();
                                    for (int i = 0; i < messages.size(); i++) {
                                        msgArray.add(
                                                ChatUtils.convertMessageToMap(messages.get(i)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", msgArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG,
                                            "getMessagesAfter: false - " + stringeeError.getCode() +
                                                    " - " + stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getMessagesAfter: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages which have sequence smaller than seq
     *
     * @param convId
     * @param seq
     * @param count
     * @param result
     */
    public void getMessagesBefore(String convId, final long seq, final int count,
                                  final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "getMessagesBefore: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "getMessagesBefore: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(clientWrapper.getClient(), seq, count,
                        new CallbackListener<>() {
                            @Override
                            public void onSuccess(final List<Message> messages) {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getMessagesBefore: success");
                                    Map<String, Object> map = new HashMap<>();
                                    List<Map<String, Object>> msgArray = new ArrayList<>();
                                    for (int i = 0; i < messages.size(); i++) {
                                        msgArray.add(
                                                ChatUtils.convertMessageToMap(messages.get(i)));
                                    }
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", msgArray);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "getMessagesBefore: false - " +
                                            stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getMessagesBefore: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Update conversation
     *
     * @param convId
     * @param name
     * @param avatar
     * @param result
     */
    public void updateConversation(String convId, final String name, final String avatar,
                                   final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "updateConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "updateConversation: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(clientWrapper.getClient(), name, avatar,
                        new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "updateConversation: success");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "updateConversation: false - " +
                                            stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "updateConversation: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Set role of participant
     *
     * @param convId
     * @param userId
     * @param role
     * @param result
     */
    public void setRole(String convId, final String userId, final UserRole role,
                        final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "setRole: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "setRole: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                switch (role) {
                    case ADMIN:
                        conversation.setAsAdmin(clientWrapper.getClient(), userId,
                                new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG, "setRole: success");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", true);
                                            map.put("code", 0);
                                            map.put("message", "Success");
                                            result.success(map);
                                        });
                                    }

                                    @Override
                                    public void onError(final StringeeError stringeeError) {
                                        super.onError(stringeeError);
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG,
                                                    "setRole: false - " + stringeeError.getCode() +
                                                            " - " + stringeeError.getMessage());
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                });
                        break;
                    case MEMBER:
                        conversation.setAsMember(clientWrapper.getClient(), userId,
                                new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG, "setRole: success");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", true);
                                            map.put("code", 0);
                                            map.put("message", "Success");
                                            result.success(map);
                                        });
                                    }

                                    @Override
                                    public void onError(final StringeeError stringeeError) {
                                        super.onError(stringeeError);
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG,
                                                    "setRole: false - " + stringeeError.getCode() +
                                                            " - " + stringeeError.getMessage());
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                });
                        break;
                }
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "setRole: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Delete messages
     *
     * @param convId
     * @param msgIdArray
     * @param result
     */
    public void deleteMessages(String convId, JSONArray msgIdArray, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "deleteMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "deleteMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        clientWrapper.getClient().deleteMessages(convId, msgIdArray, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "deleteMessages: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "deleteMessages: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void revokeMessages(String convId, JSONArray msgIdArray, boolean deleted,
                               final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "revokeMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "revokeMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        clientWrapper.getClient().revokeMessages(convId, msgIdArray, deleted, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "revokeMessages: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "revokeMessages: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Mark conversation readed
     *
     * @param convId
     * @param result
     */
    public void markAsRead(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "markAsRead: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "markAsRead: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.markAllAsRead(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "markAsRead: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "markAsRead: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "markAsRead: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send chat transcript
     *
     * @param convId
     * @param email
     * @param domain
     * @param result
     */
    public void sendChatTranscript(String convId, String email, String domain,
                                   final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "sendChatTranscript: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "sendChatTranscript: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendChatTranscriptTo(clientWrapper.getClient(), email, domain,
                        new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "sendChatTranscript: success");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "sendChatTranscript: false - " +
                                            stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", false);
                                    map.put("code", stringeeError.getCode());
                                    map.put("message", stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "sendChatTranscript: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * End chat
     *
     * @param convId
     * @param result
     */
    public void endChat(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "endChat: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "endChat: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endChat(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "endChat: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "endChat: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "endChat: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send state begin typing
     *
     * @param convId
     * @param result
     */
    public void beginTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "beginTyping: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "beginTyping: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.beginTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "beginTyping: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "beginTyping: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "beginTyping: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send state end typing
     *
     * @param convId
     * @param result
     */
    public void endTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "endTyping: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "endTyping: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "endTyping: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "endTyping: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "endTyping: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}
