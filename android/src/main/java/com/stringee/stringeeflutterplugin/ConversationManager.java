package com.stringee.stringeeflutterplugin;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

/**
 * Bridges conversation, participant, history, typing, and live-chat operations to Flutter.
 */
public class ConversationManager {

    private final ClientWrapper clientWrapper;

    private static final String TAG = "StringeeSDK";

    /** Creates a manager bound to {@code clientWrapper}. */
    public ConversationManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    /**
     * Deletes a conversation.
     *
     * @param convId
     *         Conversation id to delete.
     * @param result
     *         Flutter method result.
     */
    public void delete(final String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "delete: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "deleteConversation: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.delete(
                                clientWrapper.getClient(), new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "deleteConversation: success");
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
                                            Log.d(
                                                    TAG, "deleteConversation: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "deleteConversation: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Adds participants to a conversation.
     *
     * @param convId
     *         Conversation id to update.
     * @param participants
     *         Users to add.
     * @param result
     *         Flutter method result.
     */
    public void addParticipants(
            final String convId, final List<User> participants,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "addParticipants: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "addParticipants: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.addParticipants(
                                clientWrapper.getClient(), participants,
                                new CallbackListener<List<User>>() {
                                    @Override
                                    public void onSuccess(final List<User> users) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "addParticipants: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> participantsArray = new ArrayList<>();
                                            for (int j = 0; j < users.size(); j++) {
                                                participantsArray.add(
                                                        Utils.convertUserToMap(users.get(j)));
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
                                            Log.d(
                                                    TAG, "addParticipants: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "addParticipants: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Removes participants from a conversation.
     *
     * @param convId
     *         Conversation id to update.
     * @param participants
     *         Users to remove.
     * @param result
     *         Flutter method result.
     */
    public void removeParticipants(
            final String convId, final List<User> participants,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "removeParticipants: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "removeParticipants: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.removeParticipants(
                                clientWrapper.getClient(), participants,
                                new CallbackListener<List<User>>() {
                                    @Override
                                    public void onSuccess(final List<User> users) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "removeParticipants: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> participantsArray = new ArrayList<>();
                                            for (int j = 0; j < users.size(); j++) {
                                                participantsArray.add(
                                                        Utils.convertUserToMap(users.get(j)));
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
                                            Log.d(
                                                    TAG, "removeParticipants: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "removeParticipants: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Sends a message to a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param message
     *         Message payload.
     * @param result
     *         Flutter method result.
     */
    public void sendMessage(final String convId, final Message message, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "sendMessage: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "sendMessage: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.sendMessage(
                                clientWrapper.getClient(), message, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "sendMessage: success");
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
                                            Log.d(
                                                    TAG, "sendMessage: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "sendMessage: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets messages by message id.
     *
     * @param convId
     *         Conversation id.
     * @param msgIds
     *         Message ids to fetch.
     * @param result
     *         Flutter method result.
     */
    public void getMessages(String convId, final String[] msgIds, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "getMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "getMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.getMessages(
                                clientWrapper.getClient(), msgIds,
                                new CallbackListener<List<Message>>() {
                                    @Override
                                    public void onSuccess(final List<Message> messages) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "getMessages: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> msgArray = new ArrayList<>();
                                            for (int i = 0; i < messages.size(); i++) {
                                                msgArray.add(
                                                        Utils.convertMessageToMap(messages.get(i)));
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
                                            Log.d(
                                                    TAG, "getMessages: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "getMessages: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets messages from the local database.
     *
     * @param convId
     *         Conversation id.
     * @param count
     *         Maximum number of messages.
     * @param result
     *         Flutter method result.
     */
    public void getLocalMessages(String convId, final int count, final Result result) {
        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "getLocalMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.getLocalMessages(
                                clientWrapper.getClient(), count,
                                new CallbackListener<List<Message>>() {
                                    @Override
                                    public void onSuccess(final List<Message> messages) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "getLocalMessages: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> msgArray = new ArrayList<>();
                                            for (int i = 0; i < messages.size(); i++) {
                                                msgArray.add(
                                                        Utils.convertMessageToMap(messages.get(i)));
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
                                            Log.d(
                                                    TAG, "getLocalMessages: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "getLocalMessages: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets the latest messages from the server.
     *
     * @param convId
     *         Conversation id.
     * @param count
     *         Maximum number of messages.
     * @param result
     *         Flutter method result.
     */
    public void getLastMessages(String convId, final int count, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "getLastMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "getLastMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.getLastMessages(
                                clientWrapper.getClient(), count,
                                new CallbackListener<List<Message>>() {
                                    @Override
                                    public void onSuccess(final List<Message> messages) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "getLastMessages: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> msgArray = new ArrayList<>();
                                            for (int i = 0; i < messages.size(); i++) {
                                                msgArray.add(
                                                        Utils.convertMessageToMap(messages.get(i)));
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
                                            Log.d(
                                                    TAG, "getLastMessages: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "getLastMessages: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets messages whose sequence is greater than the provided sequence.
     *
     * @param convId
     *         Conversation id.
     * @param seq
     *         Reference sequence.
     * @param count
     *         Maximum number of messages.
     * @param result
     *         Flutter method result.
     */
    public void getMessagesAfter(
            String convId, final long seq, final int count,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "getMessagesAfter: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "getMessagesAfter: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.getMessagesAfter(
                                clientWrapper.getClient(), seq, count,
                                new CallbackListener<List<Message>>() {
                                    @Override
                                    public void onSuccess(final List<Message> messages) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "getMessagesAfter: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> msgArray = new ArrayList<>();
                                            for (int i = 0; i < messages.size(); i++) {
                                                msgArray.add(
                                                        Utils.convertMessageToMap(messages.get(i)));
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
                                            Log.d(
                                                    TAG, "getMessagesAfter: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "getMessagesAfter: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets messages whose sequence is smaller than the provided sequence.
     *
     * @param convId
     *         Conversation id.
     * @param seq
     *         Reference sequence.
     * @param count
     *         Maximum number of messages.
     * @param result
     *         Flutter method result.
     */
    public void getMessagesBefore(
            String convId, final long seq, final int count,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "getMessagesBefore: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "getMessagesBefore: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.getMessagesBefore(
                                clientWrapper.getClient(), seq, count,
                                new CallbackListener<List<Message>>() {
                                    @Override
                                    public void onSuccess(final List<Message> messages) {
                                        Utils.post(() -> {
                                            Log.d(TAG, "getMessagesBefore: success");
                                            Map<String, Object> map = new HashMap<>();
                                            List<Map<String, Object>> msgArray = new ArrayList<>();
                                            for (int i = 0; i < messages.size(); i++) {
                                                msgArray.add(
                                                        Utils.convertMessageToMap(messages.get(i)));
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
                                            Log.d(
                                                    TAG, "getMessagesBefore: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "getMessagesBefore: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Updates conversation profile fields.
     *
     * @param convId
     *         Conversation id to update.
     * @param name
     *         New conversation name.
     * @param avatar
     *         New conversation avatar URL.
     * @param result
     *         Flutter method result.
     */
    public void updateConversation(
            String convId, final String name, final String avatar,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "updateConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "updateConversation: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.updateConversation(
                                clientWrapper.getClient(), name, avatar, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "updateConversation: success");
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
                                            Log.d(
                                                    TAG, "updateConversation: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "updateConversation: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Updates a participant role in a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param userId
     *         Participant user id.
     * @param role
     *         New role.
     * @param result
     *         Flutter method result.
     */
    public void setRole(
            String convId, final String userId, final User.Role role,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "setRole: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "setRole: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.setRole(
                                clientWrapper.getClient(), userId, role, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "setRole: success");
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
                                            Log.d(
                                                    TAG,
                                                    "setRole: false - " + stringeeError.getCode() +
                                                            " - " + stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "setRole: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Deletes messages in a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param msgIdArray
     *         Message ids to delete.
     * @param result
     *         Flutter method result.
     */
    public void deleteMessages(String convId, JSONArray msgIdArray, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "deleteMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "deleteMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(msgIdArray)) {
            Log.d(TAG, "deleteMessages: false - -1 - No message id found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "No message id found");
            result.success(map);
            return;
        }

        clientWrapper.getClient().getConversationFromServer(
                convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.deleteMessages(
                                clientWrapper.getClient(), msgIdArray, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "deleteMessages: success");
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
                                            Log.d(
                                                    TAG, "deleteMessages: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "deleteMessages: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Revokes messages in a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param msgIdArray
     *         Message ids to revoke.
     * @param deleted
     *         True to mark revoked messages as deleted.
     * @param result
     *         Flutter method result.
     */
    public void revokeMessages(
            String convId, JSONArray msgIdArray, boolean deleted,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "revokeMessages: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "revokeMessages: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        List<String> idList = new ArrayList<>();
        for (int i = 0; i < msgIdArray.length(); i++) {
            String id = msgIdArray.optString(i);
            if (!Utils.isEmpty(id)) {
                idList.add(id);
            }
        }

        if (Utils.isEmpty(idList)) {
            Log.d(TAG, "revokeMessages: false - -1 - No message id found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "No message id found");
            result.success(map);
            return;
        }

        clientWrapper.getClient().getConversationFromServer(
                convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.revokeMessages(
                                clientWrapper.getClient(), idList, deleted, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "revokeMessages: success");
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
                                            Log.d(
                                                    TAG, "revokeMessages: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG,
                                    "revokeMessages: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Marks all messages in a conversation as read.
     *
     * @param convId
     *         Conversation id.
     * @param result
     *         Flutter method result.
     */
    public void markAsRead(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "markAsRead: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "markAsRead: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.markAllAsRead(
                                clientWrapper.getClient(), new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "markAsRead: success");
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
                                            Log.d(
                                                    TAG, "markAsRead: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "markAsRead: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Sends a live chat transcript to an email address.
     *
     * @param convId
     *         Conversation id.
     * @param email
     *         Destination email.
     * @param domain
     *         Domain used by the transcript request.
     * @param result
     *         Flutter method result.
     */
    public void sendChatTranscript(
            String convId, String email, String domain,
            final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "sendChatTranscript: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "sendChatTranscript: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.sendChatTranscriptTo(
                                clientWrapper.getClient(), email, domain, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "sendChatTranscript: success");
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
                                            Log.d(
                                                    TAG, "sendChatTranscript: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "sendChatTranscript: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Ends a live chat conversation.
     *
     * @param convId
     *         Conversation id.
     * @param result
     *         Flutter method result.
     */
    public void endChat(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "endChat: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "endChat: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.endChat(
                                clientWrapper.getClient(), new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "endChat: success");
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
                                            Log.d(
                                                    TAG,
                                                    "endChat: false - " + stringeeError.getCode() +
                                                            " - " + stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "endChat: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Sends a begin-typing state for a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param result
     *         Flutter method result.
     */
    public void beginTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "beginTyping: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "beginTyping: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.beginTyping(
                                clientWrapper.getClient(), new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "beginTyping: success");
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
                                            Log.d(
                                                    TAG, "beginTyping: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "beginTyping: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Sends an end-typing state for a conversation.
     *
     * @param convId
     *         Conversation id.
     * @param result
     *         Flutter method result.
     */
    public void endTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "endTyping: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(TAG, "endTyping: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(
                clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        conversation.endTyping(
                                clientWrapper.getClient(), new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(TAG, "endTyping: success");
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
                                            Log.d(
                                                    TAG, "endTyping: false - " +
                                                            stringeeError.getCode() + " - " +
                                                            stringeeError.getMessage()
                                            );
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "endTyping: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }
}
