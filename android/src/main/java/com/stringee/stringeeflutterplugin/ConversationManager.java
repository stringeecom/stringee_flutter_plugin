package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ConversationManager {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;

    public ConversationManager(ClientWrapper clientWrapper) {
        this.stringeeManager = StringeeManager.getInstance();
        this.clientWrapper = clientWrapper;
    }

    /**
     * Delete conversation
     */
    public void delete(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "delete", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("delete", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.delete(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("deleteConversation", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("deleteConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("deleteConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Add participants
     */
    public void addParticipants(final String convId, final List<User> participants, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "addParticipants", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("addParticipants", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.addParticipants(clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> participantsArray = new ArrayList<>();
                                for (int j = 0; j < users.size(); j++) {
                                    participantsArray.add(Utils.convertUserToMap(users.get(j)));
                                }
                                Utils.sendSuccessResponse("addParticipants", participantsArray, result);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("addParticipants", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("addParticipants", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Remove participants
     */
    public void removeParticipants(final String convId, final List<User> participants, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "removeParticipants", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("removeParticipants", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.removeParticipants(clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> participantsArray = new ArrayList<>();
                                for (int j = 0; j < users.size(); j++) {
                                    participantsArray.add(Utils.convertUserToMap(users.get(j)));
                                }
                                Utils.sendSuccessResponse("removeParticipants", participantsArray, result);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("removeParticipants", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("removeParticipants", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send message
     */
    public void sendMessage(final String convId, final Message message, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendMessage", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("sendMessage", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendMessage(clientWrapper.getClient(), message, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("sendMessage", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("sendMessage", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendMessage", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get messages with Id
     */
    public void getMessages(final String convId, final String[] msgIds, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "getMessages", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getMessages", msgArray, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get local messages
     */
    public void getLocalMessages(final String convId, final int count, final Result result) {
        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getLocalMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getLocalMessages", msgArray, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getLocalMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLocalMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get last messages
     */
    public void getLastMessages(final String convId, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final boolean loadAll, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "getLastMessages", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getLastMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getLastMessages", msgArray, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getLastMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLastMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get messages which have sequence greater than seq
     */
    public void getMessagesAfter(final String convId, final long seq, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final boolean loadAll, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "getMessagesAfter", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getMessagesAfter", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(clientWrapper.getClient(), seq, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getMessagesAfter", msgArray, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getMessagesAfter", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getMessagesAfter", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get messages which have sequence smaller than seq
     */
    public void getMessagesBefore(final String convId, final long seq, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final boolean loadAll, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "getMessagesBefore", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getMessagesBefore", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(clientWrapper.getClient(), seq, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getMessagesBefore", msgArray, result);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getMessagesBefore", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getMessagesBefore", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get attachment messages
     */
    public void getAttachmentMessages(final String convId, final int start, final int count, final Message.Type type, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "getAttachmentMessages", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("getAttachmentMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getAttachmentMessages(clientWrapper.getClient(), type, start, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                List<Map<String, Object>> msgArray = new ArrayList<>();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                Utils.sendSuccessResponse("getAttachmentMessages", msgArray, result);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("getAttachmentMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getAttachmentMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Update conversation
     */
    public void updateConversation(final String convId, final String name, final String avatar, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "updateConversation", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("updateConversation", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(clientWrapper.getClient(), name, avatar, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("updateConversation", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("updateConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("updateConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Set role of participant
     */
    public void setRole(final String convId, final String userId, final User.Role role, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "setRole", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("setRole", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.setRole(clientWrapper.getClient(), userId, role, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("setRole", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("setRole", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("setRole", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Delete messages
     */
    public void deleteMessages(final String convId, final JSONArray messageIds, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "deleteMessages", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("deleteMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.deleteMessages(clientWrapper.getClient(), messageIds, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("deleteMessages", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("deleteMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("deleteMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void revokeMessages(final String convId, final List<String> messageIds, final boolean deleted, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "revokeMessages", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("revokeMessages", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.revokeMessages(clientWrapper.getClient(), messageIds, deleted, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("revokeMessages", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("revokeMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("revokeMessages", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Mark conversation read
     */
    public void markAsRead(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "markAsRead", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("markAsRead", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.markAllAsRead(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("markAsRead", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("markAsRead", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("markAsRead", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Mark conversation read
     */
    public void rateChat(final String convId, final Conversation.Rating rating, final String comment, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "rateChat", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("rateChat", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.rateChat(clientWrapper.getClient(), comment, rating, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("rateChat", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("rateChat", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("rateChat", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Transfer chat support to other agent
     */
    public void transferTo(final String convId, final String userId, final String customerId, final String customerName, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "transferTo", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("transferTo", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.transferTo(clientWrapper.getClient(), userId, customerId, customerName, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("transferTo", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("transferTo", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("transferTo", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send chat transcript
     */
    public void sendChatTranscript(String convId, String email, String domain, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendChatTranscript", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("sendChatTranscript", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendChatTranscriptTo(clientWrapper.getClient(), email, domain, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("sendChatTranscript", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("sendChatTranscript", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendChatTranscript", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * End chat
     */
    public void endChat(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "endChat", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("endChat", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endChat(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("endChat", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("endChat", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("endChat", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send state begin typing
     */
    public void beginTyping(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "beginTyping", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("beginTyping", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.beginTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("beginTyping", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("beginTyping", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("beginTyping", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send state end typing
     */
    public void endTyping(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "endTyping", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("endTyping", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endTyping(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("endTyping", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("endTyping", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("endTyping", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * End chat
     */
    public void continueChatting(final String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "continueChatting", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("continueChatting", -2, "convId is invalid", result);
            return;
        }

        Utils.getConversation(clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.continueChatting(clientWrapper.getClient(), new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("continueChatting", Utils.convertConversationToMap(conversation), result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("continueChatting", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("continueChatting", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }
}