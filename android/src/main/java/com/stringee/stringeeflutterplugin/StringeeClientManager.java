package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListenter;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEnventType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeClientManager implements StringeeConnectionListener, ChangeEventListenter {
    private static Context _context;
    private static StringeeClientManager _clientManager;
    private static StringeeClient _client;
    private static StringeeManager _stringeeManager;
    private static Handler _handler;
    private static final String TAG = "Stringee";

    /**
     * @param context
     * @param stringeeManager
     * @param handler
     *
     * @return
     */
    public static synchronized StringeeClientManager getInstance(Context context, StringeeManager stringeeManager, Handler handler) {
        if (_clientManager == null) {
            _clientManager = new StringeeClientManager();
            _context = context;
            _stringeeManager = stringeeManager;
            _handler = handler;
        }
        return _clientManager;
    }

    public enum StringeeCallType {
        AppToAppOutgoing(0),
        AppToAppIncoming(1),
        AppToPhone(2),
        PhoneToApp(3);

        public final short value;

        StringeeCallType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    /**
     * Connect to Stringee server
     *
     * @param token
     */
    public void connect(final String token) {
        _client = _stringeeManager.getClient();
        if (_client == null) {
            _client = new StringeeClient(_context);
            _stringeeManager.setClient(_client);
        }
        _client.setConnectionListener(this);
        _client.setChangeEventListenter(this);
        _client.connect(token);


    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect() {
        if (_client != null) {
            _client.disconnect();
        }
    }

    /**
     * Register push notification
     *
     * @param registrationToken
     */
    public void registerPush(String registrationToken, final Result result) {
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

        _client.registerPushToken(registrationToken, new StatusListener() {
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
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });

    }

    /**
     * Unregister push notification
     *
     * @param registrationToken
     */
    public void unregisterPush(String registrationToken, final Result result) {
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

        _client.unregisterPushToken(registrationToken, new StatusListener() {
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
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });

    }

    /**
     * Send a custom message
     *
     * @param toUserId
     * @param jsonObject
     */
    public void sendCustomMessage(String toUserId, JSONObject jsonObject, final Result result) {
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

        _client.sendCustomMessage(toUserId, jsonObject, new StatusListener() {
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
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Create new conversation
     *
     * @param participants
     * @param options
     * @param result
     */
    public void createConversation(final List<User> participants, ConversationOptions options, final Result result) {
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

        _client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _stringeeManager.getConversationMap().put(conversation.getId(), conversation);
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        bodyMap = Utils.convertConversationToMap(conversation);
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Get conversation by id
     *
     * @param convId
     * @param result
     */
    public void getConversationById(String convId, final Result result) {
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

        _client.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _stringeeManager.getConversationMap().put(conversation.getId(), conversation);
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        bodyMap = Utils.convertConversationToMap(conversation);
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Get conversation by id
     *
     * @param userId
     * @param result
     */
    public void getConversationByUserId(String userId, final Result result) {
        if (userId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "User id can not be null");
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

        _client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _stringeeManager.getConversationMap().put(conversation.getId(), conversation);
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        bodyMap = Utils.convertConversationToMap(conversation);
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Get conversation from server
     *
     * @param convId
     * @param result
     */
    public void getConversationFromServer(String convId, final Result result) {
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

        _client.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _stringeeManager.getConversationMap().put(conversation.getId(), conversation);
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        bodyMap = Utils.convertConversationToMap(conversation);
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyMap);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                super.onError(error);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Get local conversations
     *
     * @param result
     */
    public void getLocalConversations(final Result result) {
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

        _client.getLocalConversations(_client.getUserId(), new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < conversations.size(); i++) {
                            _stringeeManager.getConversationMap().put(conversations.get(i).getId(), conversations.get(i));
                            bodyMap.putAll(Utils.convertConversationToMap(conversations.get(i)));
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
     * Get last conversations
     *
     * @param count
     * @param result
     */
    public void getLastConversation(int count, final Result result) {
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

        _client.getLastConversations(count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < conversations.size(); i++) {
                            _stringeeManager.getConversationMap().put(conversations.get(i).getId(), conversations.get(i));
                            bodyMap.putAll(Utils.convertConversationToMap(conversations.get(i)));
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
     * Get conversations update before '$updateAt'
     *
     * @param updateAt
     * @param count
     * @param result
     */
    public void getConversationsBefore(long updateAt, int count, final Result result) {
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

        _client.getConversationsBefore(updateAt, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < conversations.size(); i++) {
                            _stringeeManager.getConversationMap().put(conversations.get(i).getId(), conversations.get(i));
                            bodyMap.putAll(Utils.convertConversationToMap(conversations.get(i)));
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
     * Get conversations update after '$updateAt'
     *
     * @param updateAt
     * @param count
     * @param result
     */
    public void getConversationsAfter(long updateAt, int count, final Result result) {
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

        _client.getConversationsAfter(updateAt, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        Map bodyMap = new HashMap();
                        for (int i = 0; i < conversations.size(); i++) {
                            _stringeeManager.getConversationMap().put(conversations.get(i).getId(), conversations.get(i));
                            bodyMap.putAll(Utils.convertConversationToMap(conversations.get(i)));
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
     * Clear local database
     *
     * @param result
     */
    public void clearDb(final Result result) {
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

        _client.clearDb();
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

    /**
     * Block user
     *
     * @param userId
     * @param result
     */
    public void blockUser(String userId, final Result result) {
        if (userId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "User id can not be null");
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

        _client.blockUser(userId, new StatusListener() {
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
     * Get total unread conversations
     *
     * @param result
     */
    public void getTotalUnread(final Result result) {
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

        _client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", integer);
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

    //listener
    @Override
    public void onConnectionConnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========Connected==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didConnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========Disconnected==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didDisconnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========IncomingCall==========");
                _stringeeManager.getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "incomingCall");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                int callType = StringeeCallType.AppToAppOutgoing.getValue();
                if (!stringeeCall.getFrom().equals(_client.getUserId())) {
                    callType = StringeeCallType.AppToAppIncoming.getValue();
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = StringeeCallType.AppToPhone.getValue();
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = StringeeCallType.PhoneToApp.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========IncomingCall2==========");
                _stringeeManager.getCall2sMap().put(stringeeCall2.getCallId(), stringeeCall2);
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "incomingCall2");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall2.getCallId());
                callInfoMap.put("from", stringeeCall2.getFrom());
                callInfoMap.put("to", stringeeCall2.getTo());
                callInfoMap.put("fromAlias", stringeeCall2.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall2.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall2.isVideoCall());
                int callType = StringeeCallType.AppToAppOutgoing.getValue();
                if (!stringeeCall2.getFrom().equals(_client.getUserId())) {
                    callType = StringeeCallType.AppToAppIncoming.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall2.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ConnectionError==========\n" + "code: " + stringeeError.getCode() + " -message: " + stringeeError.getMessage());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didFailWithError");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("code", stringeeError.getCode());
                bodyMap.put("message", stringeeError.getMessage());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========RequestNewToken==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "requestAccessToken");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveCustomMessage==========\n" + jsonObject.toString());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didReceiveCustomMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("infor", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onTopicMessage(final String from, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveTopicMessage==========\n" + jsonObject.toString());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didReceiveTopicMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("infor", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveChangeEvent==========\n" + stringeeChange.getObjectType() + "\t" + stringeeChange.getChangeType());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didReceiveChangeEvent");
                Map bodyMap = new HashMap();
                StringeeObject.Type objectType = stringeeChange.getObjectType();
                bodyMap.put("objectType", objectType.getValue());
                bodyMap.put("changeType", stringeeChange.getChangeType().getValue());
                Map objects = new HashMap();
                Map object = new HashMap();
                if (objectType == StringeeObject.Type.CONVERSATION) {
                    object = Utils.convertConversationToMap((Conversation) stringeeChange.getObject());
                } else if (objectType == StringeeObject.Type.MESSAGE) {
                    object = Utils.convertMessageToMap(_client, (Message) stringeeChange.getObject());
                }
                objects.putAll(object);
                bodyMap.put("objects", objects);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
