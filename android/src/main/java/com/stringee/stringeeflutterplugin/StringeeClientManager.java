package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.ClientEvent;

public class StringeeClientManager implements StringeeConnectionListener, ChangeEventListenter {
    private static StringeeClientManager _clientManager;
    private static com.stringee.StringeeClient _client;
    private static StringeeManager _manager;
    private static Context _context;
    private static Handler _handler;

    private static final String TAG = "StringeeSDK";

    /**
     * @param context
     *
     * @return
     */
    public static synchronized StringeeClientManager getInstance(Context context) {
        if (_clientManager == null) {
            _clientManager = new StringeeClientManager();
            _context = context;
            _manager = StringeeManager.getInstance();
            _handler = _manager.getHandler();
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
     * @param socketAddressList
     */
    public void connect(final String token, List<SocketAddress> socketAddressList, final Result result) {
        Map map = new HashMap();
        if (token == null) {
            Log.d(TAG, "connect: false - -2 - token is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "token is invalid");
            result.success(map);
            return;
        }

        _client = _manager.getClient();
        if (_client == null) {
            _client = new com.stringee.StringeeClient(_context);
            _manager.setClient(_client);
        }

        if (socketAddressList != null) {
            _client.setHost(socketAddressList);
        }

        _client.setConnectionListener(this);
        _client.setChangeEventListenter(this);
        _client.connect(token);
        Log.d(TAG, "connect: success");
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect(final Result result) {
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "disconnect: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.disconnect();
        Log.d(TAG, "disconnect: success");
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Set base API url
     *
     * @param baseAPIUrl
     */
    public void setBaseAPIUrl(String baseAPIUrl, final Result result) {
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "setBaseAPIUrl: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.setBaseAPIUrl(baseAPIUrl);
        Log.d(TAG, "setBaseAPIUrl: success");
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Register push notification
     *
     * @param registrationToken
     */
    public void registerPush(String registrationToken, final Result result) {
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "registerPush: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (registrationToken == null) {
            Log.d(TAG, "registerPush: false - -2 - registrationToken is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "registrationToken is invalid");
            result.success(map);
            return;
        }

        _client.registerPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "registerPush: success");
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "initAnswer: false - " + error.getCode() + " - " + error.getMessage());
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
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "unregisterPush: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (registrationToken == null) {
            Log.d(TAG, "unregisterPush: false - -2 - registrationToken is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "registrationToken is invalid");
            result.success(map);
            return;
        }

        _client.unregisterPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unregisterPush: success");
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unregisterPush: false - " + error.getCode() + " - " + error.getMessage());
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
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "sendCustomMessage: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.sendCustomMessage(toUserId, jsonObject, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendCustomMessage: success");
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendCustomMessage: false - " + error.getCode() + " - " + error.getMessage());
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
            Log.d(TAG, "createConversation: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "createConversation: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertConversationToMap(conversation));
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "createConversation: false - " + error.getCode() + " - " + error.getMessage());
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
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "getConversationById: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (convId == null) {
            Log.d(TAG, "getConversationById: false - -2 - convId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        _client.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationById: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertConversationToMap(conversation));
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationById: false - " + error.getCode() + " - " + error.getMessage());
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
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "getConversationByUserId: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (userId == null) {
            Log.d(TAG, "getConversationByUserId: false - -2 - userId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "userId is invalid");
            result.success(map);
            return;
        }

        _client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationByUserId: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertConversationToMap(conversation));
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationByUserId: false - " + error.getCode() + " - " + error.getMessage());
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
            Log.d(TAG, "getLocalConversations: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.getLocalConversations(_client.getUserId(), new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        if (conversations.size() > 0) {
                            Log.d(TAG, "getLocalConversations: success");
                            List bodyArray = new ArrayList();
                            for (int i = 0; i < conversations.size(); i++) {
                                bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                            }
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            map.put("body", bodyArray);
                            result.success(map);
                        }
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getLocalConversations: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
            Log.d(TAG, "getLastConversation: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.getLastConversations(count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        if (conversations.size() > 0) {
                            Log.d(TAG, "getLastConversation: success");
                            List bodyArray = new ArrayList();
                            for (int i = 0; i < conversations.size(); i++) {
                                bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                            }
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            map.put("body", bodyArray);
                            result.success(map);
                        }
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getLastConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     * @param dateTime
     * @param count
     * @param result
     */
    public void getConversationsBefore(long dateTime, int count, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "getConversationsBefore: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.getConversationsBefore(dateTime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        if (conversations.size() > 0) {
                            Log.d(TAG, "getConversationsBefore: success");
                            List bodyArray = new ArrayList();
                            for (int i = 0; i < conversations.size(); i++) {
                                bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                            }
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            map.put("body", bodyArray);
                            result.success(map);
                        }
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationsBefore: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
            Log.d(TAG, "getConversationsAfter: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.getConversationsAfter(updateAt, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        if (conversations.size() > 0) {
                            Log.d(TAG, "getConversationsAfter: success");
                            List bodyArray = new ArrayList();
                            for (int i = 0; i < conversations.size(); i++) {
                                bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                            }
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            map.put("body", bodyArray);
                            result.success(map);
                        }
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getConversationsAfter: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "clearDb: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.clearDb();
        Log.d(TAG, "clearDb: success");
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Block user
     *
     * @param userId
     * @param result
     */
    public void blockUser(String userId, final Result result) {
        Map map = new HashMap();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "blockUser: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (userId == null) {
            Log.d(TAG, "blockUser: false - -2 - userId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "userId is invalid");
            result.success(map);
            return;
        }

        _client.blockUser(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "blockUser: success");
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
                        Log.d(TAG, "blockUser: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
            Log.d(TAG, "getTotalUnread: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getTotalUnread: success");
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getTotalUnread: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
    public void onConnectionConnected(final com.stringee.StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionConnected: " + stringeeClient.getUserId());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
    public void onConnectionDisconnected(final com.stringee.StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionDisconnected: " + stringeeClient.getUserId());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
                Log.d(TAG, "onIncomingCall: " + stringeeCall.getCallId());
                _manager.getCallsMap().put(stringeeCall.getCallId(), new MStringeeCall(stringeeCall));
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
                Log.d(TAG, "onIncomingCall2: " + stringeeCall2.getCallId());
                _manager.getCall2sMap().put(stringeeCall2.getCallId(), new com.stringee.stringeeflutterplugin.MStringeeCall2(stringeeCall2));
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
    public void onConnectionError(final com.stringee.StringeeClient stringeeClient, final StringeeError stringeeError) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionError: " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
    public void onRequestNewToken(final com.stringee.StringeeClient stringeeClient) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onRequestNewToken: " + stringeeClient.getUserId());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
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
                Log.d(TAG, "onCustomMessage: " + from + " - " + jsonObject.toString());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didReceiveCustomMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                bodyMap.put("message", jsonObject.toString());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onTopicMessage(final String from, final JSONObject jsonObject) {
    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " + stringeeChange.getChangeType());
                Map map = new HashMap();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didReceiveChangeEvent");
                Map bodyMap = new HashMap();
                StringeeObject.Type objectType = stringeeChange.getObjectType();
                bodyMap.put("objectType", objectType.getValue());
                bodyMap.put("changeType", stringeeChange.getChangeType().getValue());
                ArrayList objects = new ArrayList();
                Map objectMap = new HashMap();
                if (objectType == StringeeObject.Type.CONVERSATION) {
                    objectMap = Utils.convertConversationToMap((Conversation) stringeeChange.getObject());
                } else if (objectType == StringeeObject.Type.MESSAGE) {
                    objectMap = Utils.convertMessageToMap((Message) stringeeChange.getObject());
                }
                objects.add(objectMap);
                bodyMap.put("objects", objects);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
