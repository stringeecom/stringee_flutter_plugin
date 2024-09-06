package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.StringeeManager.UserRole;

import org.json.JSONArray;

import java.util.ArrayList;
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
     * @param convId convId
     * @param result result
     */
    public void delete(final String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("delete");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("deleteConversation", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.delete(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("deleteConversation");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("deleteConversation", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("deleteConversation", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Add participants
     *
     * @param convId       convId
     * @param participants participants
     * @param result       result
     */
    public void addParticipants(final String convId, final List<User> participants, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("addParticipants");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("addParticipants", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.addParticipants(clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("addParticipants");
                            List<Map<String, Object>> participantsArray = new ArrayList<>();
                            for (User user : users) {
                                participantsArray.add(Utils.convertUserToMap(user));
                            }
                            Logging.d(participantsArray.toString());
                            map.put("body", participantsArray);
                            result.success(map);
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("addParticipants", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("addParticipants", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Remove participants
     *
     * @param convId       convId
     * @param participants participants
     * @param result       result
     */
    public void removeParticipants(final String convId, final List<User> participants, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("removeParticipants");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("removeParticipants", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.removeParticipants(clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("removeParticipants");
                            List<Map<String, Object>> participantsArray = new ArrayList<>();
                            for (User user : users) {
                                participantsArray.add(Utils.convertUserToMap(user));
                            }
                            Logging.d(participantsArray.toString());
                            map.put("body", participantsArray);
                            result.success(map);
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("removeParticipants", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("removeParticipants", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send message
     *
     * @param message message
     * @param result  result
     */
    public void sendMessage(final String convId, final Message message, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendMessage");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("sendMessage", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendMessage(clientWrapper.getClient(), message, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("sendMessage");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("sendMessage", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("sendMessage", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages with Id
     *
     * @param convId convId
     * @param msgIds msgIds
     * @param result result
     */
    public void getMessages(String convId, final String[] msgIds, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getMessages");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("getMessages", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("getMessages");
                            List<Map<String, Object>> msgArray = new ArrayList<>();
                            for (Message message : messages) {
                                msgArray.add(Utils.convertMessageToMap(message));
                            }
                            Logging.d(msgArray.toString());
                            map.put("body", msgArray);
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("getMessages", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getMessages", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get local messages
     *
     * @param convId convId
     * @param count  count
     * @param result result
     */
    public void getLocalMessages(String convId, final int count, final Result result) {
        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("getLocalMessages", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("getLocalMessages");
                            List<Map<String, Object>> msgArray = new ArrayList<>();
                            for (Message message : messages) {
                                msgArray.add(Utils.convertMessageToMap(message));
                            }
                            Logging.d(msgArray.toString());
                            map.put("body", msgArray);
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("getLocalMessages", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getLocalMessages", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get last messages
     *
     * @param convId convId
     * @param count  count
     * @param result result
     */
    public void getLastMessages(String convId, final int count, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getLastMessages");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("getLastMessages", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("getLastMessages");
                            List<Map<String, Object>> msgArray = new ArrayList<>();
                            for (Message message : messages) {
                                msgArray.add(Utils.convertMessageToMap(message));
                            }
                            Logging.d(msgArray.toString());
                            map.put("body", msgArray);
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("getLastMessages", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getLastMessages", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages which have sequence greater than seq
     *
     * @param convId convId
     * @param seq    seq
     * @param count  count
     * @param result result
     */
    public void getMessagesAfter(String convId, final long seq, final int count, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getMessagesAfter");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("getMessagesAfter", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(clientWrapper.getClient(), seq, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("getMessagesAfter");
                            List<Map<String, Object>> msgArray = new ArrayList<>();
                            for (Message message : messages) {
                                msgArray.add(Utils.convertMessageToMap(message));
                            }
                            Logging.d(msgArray.toString());
                            map.put("body", msgArray);
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("getMessagesAfter", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getMessagesAfter", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get messages which have sequence smaller than seq
     *
     * @param convId convId
     * @param seq    seq
     * @param count  count
     * @param result result
     */
    public void getMessagesBefore(String convId, final long seq, final int count, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getMessagesBefore");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("getMessagesBefore", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(clientWrapper.getClient(), seq, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("getMessagesBefore");
                            List<Map<String, Object>> msgArray = new ArrayList<>();
                            for (Message message : messages) {
                                msgArray.add(Utils.convertMessageToMap(message));
                            }
                            Logging.d(msgArray.toString());
                            map.put("body", msgArray);
                            result.success(map);
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("getMessagesBefore", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getMessagesBefore", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Update conversation
     *
     * @param convId convId
     * @param name   name
     * @param avatar avatar
     * @param result result
     */
    public void updateConversation(String convId, final String name, final String avatar, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("updateConversation");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("updateConversation", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(clientWrapper.getClient(), name, avatar, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("updateConversation");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("updateConversation", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("updateConversation", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Set role of participant
     *
     * @param convId convId
     * @param userId userId
     * @param role   role
     * @param result result
     */
    public void setRole(String convId, final String userId, final UserRole role, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("setRole");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("setRole", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                switch (role) {
                    case Admin:
                        conversation.setAsAdmin(clientWrapper.getClient(), userId, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Utils.post(() -> {
                                    Map<String, Object> map = Utils.createSuccessMap("setRole");
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Map<String, Object> map = Utils.createErrorMap("setRole", stringeeError.getCode(), stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
                        break;
                    case Member:
                        conversation.setAsMember(clientWrapper.getClient(), userId, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Utils.post(() -> {
                                    Map<String, Object> map = Utils.createSuccessMap("setRole");
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Map<String, Object> map = Utils.createErrorMap("setRole", stringeeError.getCode(), stringeeError.getMessage());
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
                    Map<String, Object> map = Utils.createErrorMap("setRole", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Delete messages
     *
     * @param convId     convId
     * @param msgIdArray msgIdArray
     * @param result     result
     */
    public void deleteMessages(String convId, JSONArray msgIdArray, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("deleteMessages");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("deleteMessages", "convId");
            result.success(map);
            return;
        }

        clientWrapper.getClient().deleteMessages(convId, msgIdArray, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("deleteMessages");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("deleteMessages", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void revokeMessages(String convId, JSONArray msgIdArray, boolean deleted, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("revokeMessages");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("revokeMessages", "convId");
            result.success(map);
            return;
        }

        clientWrapper.getClient().revokeMessages(convId, msgIdArray, deleted, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("revokeMessages");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("revokeMessages", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Mark conversation readed
     *
     * @param convId convId
     * @param result result
     */
    public void markAsRead(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("markAsRead");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("markAsRead", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.markAllAsRead(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("markAsRead");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("markAsRead", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("markAsRead", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send chat transcript
     *
     * @param convId convId
     * @param email  email
     * @param domain domain
     * @param result result
     */
    public void sendChatTranscript(String convId, String email, String domain, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendChatTranscript");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("sendChatTranscript", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendChatTranscriptTo(clientWrapper.getClient(), email, domain, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("sendChatTranscript");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("sendChatTranscript", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("sendChatTranscript", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * End chat
     *
     * @param convId convId
     * @param result result
     */
    public void endChat(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("endChat");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("endChat", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endChat(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("endChat");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("endChat", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("endChat", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send state begin typing
     *
     * @param convId convId
     * @param result result
     */
    public void beginTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("beginTyping");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("beginTyping", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.beginTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("beginTyping");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("beginTyping", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("beginTyping", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send state end typing
     *
     * @param convId convId
     * @param result result
     */
    public void endTyping(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("endTyping");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("endTyping", "convId");
            result.success(map);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("endTyping");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("endTyping", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("endTyping", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}