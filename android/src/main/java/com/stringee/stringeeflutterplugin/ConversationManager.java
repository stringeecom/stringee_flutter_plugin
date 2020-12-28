package com.stringee.stringeeflutterplugin;

import android.os.Handler;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ConversationManager {
    private static ConversationManager _conversationManager;
    private static StringeeClient _client;
    private static StringeeManager _stringeeManager;
    private static Handler _handler;

    public static synchronized ConversationManager getInstance(StringeeManager stringeeManager, Handler handler) {
        if (_conversationManager == null) {
            _conversationManager = new ConversationManager();
            _stringeeManager = stringeeManager;
            _handler = handler;
            _client = stringeeManager.getClient();
        }
        return _conversationManager;
    }

    public enum UserRole {
        Admin(0),
        Member(1);

        public final short value;

        UserRole(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    /**
     * Delete conversation
     *
     * @param convId
     * @param result
     */
    public void deleteConversation(final String convId, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.delete(_client, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _stringeeManager.getConversationMap().remove(convId);
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
     * Add participants
     *
     * @param convId
     * @param participants
     * @param result
     */
    public void addParticipants(final String convId, final List<User> participants, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.addParticipants(_client, participants, new CallbackListener<List<User>>() {
            @Override
            public void onSuccess(final List<User> users) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < users.size(); i++) {
                            try {
                                User user = users.get(i);
                                bodyMap.putAll(com.stringee.stringeeflutterplugin.Utils.convertJsonToMap(com.stringee.stringeeflutterplugin.Utils.convertUserToMap(user)));
                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("participants", bodyMap);
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
     * Remove participants
     *
     * @param convId
     * @param participants
     * @param result
     */
    public void removeParticipants(final String convId, final List<User> participants, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.removeParticipants(_client, participants, new CallbackListener<List<User>>() {
            @Override
            public void onSuccess(final List<User> users) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < users.size(); i++) {
                            try {
                                User user = users.get(i);
                                bodyMap.putAll(com.stringee.stringeeflutterplugin.Utils.convertJsonToMap(com.stringee.stringeeflutterplugin.Utils.convertUserToMap(user)));
                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.sendMessage(_client, message, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (message.getId() != null) {
                            _stringeeManager.getMessageMap().put(message.getId(), message);
                        }
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
    public void getMessages(String convId, String[] msgIds, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.getMessages(_client, msgIds, new CallbackListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> messages) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getId() != null) {
                                _stringeeManager.getMessageMap().put(message.getId(), message);
                            }
                            bodyMap.putAll(Utils.convertMessageToMap(_client, message));
                        }

                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.getLocalMessages(_client, count, new CallbackListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> messages) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getId() != null) {
                                _stringeeManager.getMessageMap().put(message.getId(), message);
                            }
                            bodyMap.putAll(Utils.convertMessageToMap(_client, message));
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.getLastMessages(_client, count, new CallbackListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> messages) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getId() != null) {
                                _stringeeManager.getMessageMap().put(message.getId(), message);
                            }
                            bodyMap.putAll(Utils.convertMessageToMap(_client, message));
                        }

                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.getMessagesAfter(_client, seq, count, new CallbackListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> messages) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getId() != null) {
                                _stringeeManager.getMessageMap().put(message.getId(), message);
                            }
                            bodyMap.putAll(Utils.convertMessageToMap(_client, message));
                        }

                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.getMessagesBefore(_client, seq, count, new CallbackListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> messages) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            if (message.getId() != null) {
                                _stringeeManager.getMessageMap().put(message.getId(), message);
                            }
                            bodyMap.putAll(Utils.convertMessageToMap(_client, message));
                        }

                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
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
    public void updateConversation(String convId, String name, String avatar, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        conversation.updateConversation(_client, name, avatar, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
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
     * @param result
     */
    public void setRole(String convId, String userId, UserRole role, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        Conversation conversation = _stringeeManager.getConversationMap().get(convId);
        switch (role) {
            case Admin:
                conversation.setAsAdmin(_client, userId, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
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
                conversation.setAsMember(_client, userId, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
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

    /**
     * Delete messages
     *
     * @param convId
     * @param msgIdArray
     * @param result
     */
    public void deleteMessages(String convId, JSONArray msgIdArray, final Result result) {
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        _client.deleteMessages(convId, msgIdArray, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
            return;
        }

        _client.revokeMessages(convId, msgIdArray, deleted, new com.stringee.messaging.listeners.CallbackListener<org.json.JSONArray>() {
                    @Override
                    public void onSuccess(final JSONArray jsonArray) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Map map = new HashMap();
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    map.put("body", Utils.toList(jsonArray));
                                    result.success(map);
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                }
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
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                }
        );
    }
}
