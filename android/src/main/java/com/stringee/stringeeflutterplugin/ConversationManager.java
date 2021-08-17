package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.StringeeManager.UserRole;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ConversationManager {
    private ClientWrapper _clientWrapper;
    private StringeeManager _manager;
    private Handler _handler;
    private static final String TAG = "StringeeSDK";

    public ConversationManager(ClientWrapper clientWrapper) {
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _clientWrapper = clientWrapper;
    }

    /**
     * Delete conversation
     *
     * @param convId
     * @param result
     */
    public void delete(final String convId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "delete: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "deleteConversation: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.delete(_clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "deleteConversation: success");
                                Map map1 = new HashMap();
                                map1.put("status", true);
                                map1.put("code", 0);
                                map1.put("message", "Success");
                                result.success(map1);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "deleteConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map12 = new HashMap();
                                map12.put("status", false);
                                map12.put("code", stringeeError.getCode());
                                map12.put("message", stringeeError.getMessage());
                                result.success(map12);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "deleteConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map13 = new HashMap();
                        map13.put("status", false);
                        map13.put("code", stringeeError.getCode());
                        map13.put("message", stringeeError.getMessage());
                        result.success(map13);
                    }
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
    public void addParticipants(final String convId, final List<User> participants, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "addParticipants: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "addParticipants: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.addParticipants(_clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "addParticipants: success");
                                Map map1 = new HashMap();
                                java.util.List participantsArray = new java.util.ArrayList();
                                for (int j = 0; j < users.size(); j++) {
                                    participantsArray.add(Utils.convertUserToMap(users.get(j)));
                                }
                                map1.put("status", true);
                                map1.put("code", 0);
                                map1.put("message", "Success");
                                map1.put("body", participantsArray);
                                result.success(map1);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "addParticipants: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map12 = new HashMap();
                                map12.put("status", false);
                                map12.put("code", stringeeError.getCode());
                                map12.put("message", stringeeError.getMessage());
                                result.success(map12);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "addParticipants: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map13 = new HashMap();
                        map13.put("status", false);
                        map13.put("code", stringeeError.getCode());
                        map13.put("message", stringeeError.getMessage());
                        result.success(map13);
                    }
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
    public void removeParticipants(final String convId, final List<User> participants, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "removeParticipants: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "removeParticipants: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.removeParticipants(_clientWrapper.getClient(), participants, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(final List<User> users) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "removeParticipants: success");
                                Map map = new HashMap();
                                java.util.List participantsArray = new java.util.ArrayList();
                                for (int j = 0; j < users.size(); j++) {
                                    participantsArray.add(Utils.convertUserToMap(users.get(j)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", participantsArray);
                                result.success(map);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "removeParticipants: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "removeParticipants: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "sendMessage: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "sendMessage: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendMessage(_clientWrapper.getClient(), message, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "sendMessage: success");
                                Map map = new HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "sendMessage: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendMessage: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "getMessages: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "getMessages: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(_clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessages: success");
                                Map map = new HashMap();
                                java.util.List msgArray = new java.util.ArrayList();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", msgArray);
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "getLocalMessages: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(_clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getLocalMessages: success");
                                Map map = new HashMap();
                                java.util.List msgArray = new java.util.ArrayList();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", msgArray);
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Map map = new HashMap();
                                Log.d(TAG, "getLocalMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getLocalMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "getLastMessages: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "getLastMessages: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(_clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getLastMessages: success");
                                Map map = new HashMap();
                                java.util.List msgArray = new java.util.ArrayList();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", msgArray);
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getLastMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getLastMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
    public void getMessagesAfter(String convId, final long seq, final int count, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "getMessagesAfter: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "getMessagesAfter: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(_clientWrapper.getClient(), seq, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessagesAfter: success");
                                Map map = new HashMap();
                                java.util.List msgArray = new java.util.ArrayList();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", msgArray);
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessagesAfter: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getMessagesAfter: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
    public void getMessagesBefore(String convId, final long seq, final int count, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "getMessagesBefore: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "getMessagesBefore: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(_clientWrapper.getClient(), seq, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessagesBefore: success");
                                Map map = new HashMap();
                                java.util.List msgArray = new java.util.ArrayList();
                                for (int i = 0; i < messages.size(); i++) {
                                    msgArray.add(Utils.convertMessageToMap(messages.get(i)));
                                }
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                map.put("body", msgArray);
                                result.success(map);
                            }
                        });

                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "getMessagesBefore: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getMessagesBefore: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
    public void updateConversation(String convId, final String name, final String avatar, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "updateConversation: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "updateConversation: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(_clientWrapper.getClient(), name, avatar, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "updateConversation: success");
                                Map map = new HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "updateConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "updateConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
    public void setRole(String convId, final String userId, final UserRole role, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "setRole: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "setRole: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getConversation(_clientWrapper.getClient(), convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                switch (role) {
                    case Admin:
                        conversation.setAsAdmin(_clientWrapper.getClient(), userId, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "setRole: success");
                                        Map map = new HashMap();
                                        map.put("status", true);
                                        map.put("code", 0);
                                        map.put("message", "Success");
                                        result.success(map);
                                    }
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "setRole: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                        Map map = new HashMap();
                                        map.put("status", false);
                                        map.put("code", stringeeError.getCode());
                                        map.put("message", stringeeError.getMessage());
                                        result.success(map);
                                    }
                                });
                            }
                        });
                        break;
                    case Member:
                        conversation.setAsMember(_clientWrapper.getClient(), userId, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "setRole: success");
                                        Map map = new HashMap();
                                        map.put("status", true);
                                        map.put("code", 0);
                                        map.put("message", "Success");
                                        result.success(map);
                                    }
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "setRole: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                        Map map = new HashMap();
                                        map.put("status", false);
                                        map.put("code", stringeeError.getCode());
                                        map.put("message", stringeeError.getMessage());
                                        result.success(map);
                                    }
                                });
                            }
                        });
                        break;
                }
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "setRole: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "deleteMessages: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "deleteMessages: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        _clientWrapper.getClient().deleteMessages(convId, msgIdArray, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "deleteMessages: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "deleteMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void revokeMessages(String convId, JSONArray msgIdArray, boolean deleted, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "revokeMessages: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "revokeMessages: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        _clientWrapper.getClient().revokeMessages(convId, msgIdArray, deleted, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "revokeMessages: success");
                                Map map1 = new HashMap();
                                map1.put("status", true);
                                map1.put("code", 0);
                                map1.put("message", "Success");
                                result.success(map1);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "revokeMessages: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map12 = new HashMap();
                                map12.put("status", false);
                                map12.put("code", stringeeError.getCode());
                                map12.put("message", stringeeError.getMessage());
                                result.success(map12);
                            }
                        });
                    }
                }
        );
    }

    /**
     * Mark conversation readed
     *
     * @param convId
     * @param result
     */
    public void markAsRead(String convId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "markAsRead: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "markAsRead: false - -2 - convId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getLastMessage(_clientWrapper.getClient(), convId, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.markAsRead(_clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "markAsRead: success");
                                Map map = new HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "markAsRead: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "markAsRead: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }
}