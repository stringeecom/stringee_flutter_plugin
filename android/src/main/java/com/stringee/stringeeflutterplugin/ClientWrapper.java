package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.common.StringeeConstant;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject.Type;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListenter;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeCallType;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.ClientEvent;

public class ClientWrapper implements StringeeConnectionListener, ChangeEventListenter {
    private StringeeClient _client;
    private StringeeManager _manager;
    private ConversationManager _conversationManager;
    private MessageManager _messageManager;
    private Handler _handler;
    private String _uuid;
    private static final String TAG = "StringeeSDK";

    public ClientWrapper(final String uuid) {
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _uuid = uuid;
        _conversationManager = new ConversationManager(this);
        _messageManager = new MessageManager(this);
        _client = new StringeeClient(_manager.getContext());
        _client.setConnectionListener(this);
        _client.setChangeEventListenter(this);
    }

    public ClientWrapper(final String uuid, final String baseAPIUrl) {
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _uuid = uuid;
        _conversationManager = new ConversationManager(this);
        _messageManager = new MessageManager(this);
        _client = new StringeeClient(_manager.getContext());
        if (baseAPIUrl != null) {
            _client.setBaseAPIUrl(baseAPIUrl);
        }
        _client.setConnectionListener(this);
        _client.setChangeEventListenter(this);
    }

    public StringeeClient getClient() {
        return _client;
    }

    public String getId() {
        return _uuid;
    }

    public CallWrapper callWrapper(final String callId) {
        return _manager.getCallsMap().get(callId);
    }

    /**
     * Make a new call
     *
     * @param from
     * @param to
     * @param isVideoCall
     * @param customData
     * @param videoResolution
     * @param result
     */
    public CallWrapper callWrapper(final String from, final String to, final boolean isVideoCall, final String customData, final String videoResolution, final Result result) {
        StringeeCall call = new StringeeCall(_client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }
        if (videoResolution != null) {
            if (videoResolution.equalsIgnoreCase("NORMAL")) {
                call.setQuality(StringeeConstant.QUALITY_NORMAL);
            } else if (videoResolution.equalsIgnoreCase("HD")) {
                call.setQuality(StringeeConstant.QUALITY_HD);
            } else if (videoResolution.equalsIgnoreCase("FULLHD")) {
                call.setQuality(StringeeConstant.QUALITY_FULLHD);
            }
        }
        CallWrapper callWrapper = new CallWrapper(this, call, result);
        _manager.getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    public Call2Wrapper call2Wrapper(final String callId) {
        return _manager.getCall2sMap().get(callId);
    }

    /**
     * Make a new call2
     *
     * @param from
     * @param to
     * @param isVideoCall
     * @param customData
     * @param result
     */
    public Call2Wrapper call2Wrapper(final String from, final String to, final boolean isVideoCall, final String customData, final Result result) {
        StringeeCall2 call = new StringeeCall2(_client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }

        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call, result);
        _manager.getCall2sMap().put(call.getCallId(), call2Wrapper);
        return call2Wrapper;
    }

    public ConversationManager conversation() {
        return _conversationManager;
    }

    public MessageManager message() {
        return _messageManager;
    }

    @Override
    public void onConnectionConnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(() -> {
            Log.d(TAG, "onConnectionConnected: " + stringeeClient.getUserId());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "didConnect");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
            bodyMap.put("isReconnecting", b);
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(() -> {
            Log.d(TAG, "onConnectionDisconnected: " + stringeeClient.getUserId());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "didDisconnect");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
            bodyMap.put("isReconnecting", b);
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        _handler.post(() -> {
            Log.d(TAG, "onIncomingCall: " + stringeeCall.getCallId());
            _manager.getCallsMap().put(stringeeCall.getCallId(), new CallWrapper(this, stringeeCall));
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "incomingCall");
            map.put("uuid", _uuid);
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
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        _handler.post(() -> {
            Log.d(TAG, "onIncomingCall2: " + stringeeCall2.getCallId());
            _manager.getCall2sMap().put(stringeeCall2.getCallId(), new Call2Wrapper(this, stringeeCall2));
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "incomingCall2");
            map.put("uuid", _uuid);
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
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        _handler.post(() -> {
            Log.d(TAG, "onConnectionError: " + stringeeError.getCode() + " - " + stringeeError.getMessage());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "didFailWithError");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("code", stringeeError.getCode());
            bodyMap.put("message", stringeeError.getMessage());
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        _handler.post(() -> {
            Log.d(TAG, "onRequestNewToken: " + stringeeClient.getUserId());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "requestAccessToken");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            bodyMap.put("userId", stringeeClient.getUserId());
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        _handler.post(() -> {
            Log.d(TAG, "onCustomMessage: " + from + " - " + jsonObject.toString());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "didReceiveCustomMessage");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            bodyMap.put("fromUserId", from);
            bodyMap.put("message", jsonObject.toString());
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    @Override
    public void onTopicMessage(String s, JSONObject jsonObject) {

    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        _handler.post(() -> {
            Log.d(TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " + stringeeChange.getChangeType());
            Map map = new HashMap();
            map.put("nativeEventType", ClientEvent.getValue());
            map.put("event", "didReceiveChangeEvent");
            map.put("uuid", _uuid);
            Map bodyMap = new HashMap();
            Type objectType = stringeeChange.getObjectType();
            bodyMap.put("objectType", objectType.getValue());
            bodyMap.put("changeType", stringeeChange.getChangeType().getValue());
            java.util.ArrayList objects = new java.util.ArrayList();
            Map objectMap = new HashMap();
            if (objectType == Type.CONVERSATION) {
                objectMap = Utils.convertConversationToMap((Conversation) stringeeChange.getObject());
            } else if (objectType == Type.MESSAGE) {
                objectMap = Utils.convertMessageToMap((Message) stringeeChange.getObject());
            }
            objects.add(objectMap);
            bodyMap.put("objects", objects);
            map.put("body", bodyMap);
            StringeeFlutterPlugin._eventSink.success(map);
        });
    }

    /**
     * Connect to Stringee server
     *
     * @param token
     * @param socketAddressList
     * @param result
     */
    public void connect(final List<SocketAddress> socketAddressList, final String token, final Result result) {
        _client.setHost(socketAddressList);
        _client.connect(token);
        Log.d(TAG, "connect: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Connect to Stringee server
     *
     * @param token
     * @param result
     */
    public void connect(final String token, final Result result) {
        _client.connect(token);
        Log.d(TAG, "connect: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    public boolean isConnected() {
        return _client.isConnected();
    }

    /**
     * Disconnect from Stringee server
     *
     * @param result
     */
    public void disconnect(final Result result) {
        _client.disconnect();
        Log.d(TAG, "disconnect: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Register push notification
     *
     * @param registrationToken
     * @param result
     */
    public void registerPush(final String registrationToken, final Result result) {
        _client.registerPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(() -> {
                    Log.d(TAG, "registerPush: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "registerPush: false - " + error.getCode() + " - " + error.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
                });
            }
        });

    }

    /**
     * Unregister push notification
     *
     * @param registrationToken
     * @param result
     */
    public void unregisterPush(final String registrationToken, final Result result) {
        _client.unregisterPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(() -> {
                    Log.d(TAG, "unregisterPush: success");
                    Map map1 = new HashMap();
                    map1.put("status", true);
                    map1.put("code", 0);
                    map1.put("message", "Success");
                    result.success(map1);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "unregisterPush: false - " + error.getCode() + " - " + error.getMessage());
                    Map map12 = new HashMap();
                    map12.put("status", false);
                    map12.put("code", error.getCode());
                    map12.put("message", error.getMessage());
                    result.success(map12);
                });
            }
        });
    }

    /**
     * Send a custom message
     *
     * @param toUserId
     * @param data
     * @param result
     */
    public void sendCustomMessage(final String toUserId, final JSONObject data, final Result result) {
        _client.sendCustomMessage(toUserId, data, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(() -> {
                    Log.d(TAG, "sendCustomMessage: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "sendCustomMessage: false - " + error.getCode() + " - " + error.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
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
    public void createConversation(final List<User> participants, final ConversationOptions options, final Result result) {
        _client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(() -> {
                    Log.d(TAG, "createConversation: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "createConversation: false - " + error.getCode() + " - " + error.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
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
    public void getConversationById(final String convId, final Result result) {
        _client.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationById: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationById: false - " + error.getCode() + " - " + error.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get conversation by user id
     *
     * @param userId
     * @param result
     */
    public void getConversationByUserId(final String userId, final Result result) {
        _client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationByUserId: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationByUserId: false - " + error.getCode() + " - " + error.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
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
        _client.getLocalConversations(_client.getUserId(), new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(() -> {
                    Map map = new HashMap();
                    if (conversations.size() > 0) {
                        Log.d(TAG, "getLocalConversations: success");
                        List bodyArray = new java.util.ArrayList();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyArray);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "getLocalConversations: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
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
        _client.getLastConversations(count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(() -> {
                    Map map = new HashMap();
                    if (conversations.size() > 0) {
                        Log.d(TAG, "getLastConversation: success");
                        List bodyArray = new java.util.ArrayList();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyArray);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "getLastConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
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
    public void getConversationsBefore(final long updateAt, final int count, final Result result) {
        _client.getConversationsBefore(updateAt, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(() -> {
                    Map map = new HashMap();
                    if (conversations.size() > 0) {
                        Log.d(TAG, "getConversationsBefore: success");
                        List bodyArray = new java.util.ArrayList();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyArray);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationsBefore: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
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
    public void getConversationsAfter(final long updateAt, final int count, final Result result) {
        _client.getConversationsAfter(updateAt, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                _handler.post(() -> {
                    Map map = new HashMap();
                    if (conversations.size() > 0) {
                        Log.d(TAG, "getConversationsAfter: success");
                        List bodyArray = new java.util.ArrayList();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", bodyArray);
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "getConversationsAfter: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
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
        _client.clearDb();
        Log.d(TAG, "clearDb: success");
        Map map = new HashMap();
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
    public void blockUser(final String userId, final Result result) {
        _client.blockUser(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(() -> {
                    Log.d(TAG, "blockUser: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "blockUser: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
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
        _client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                _handler.post(() -> {
                    Log.d(TAG, "getTotalUnread: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", integer);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                _handler.post(() -> {
                    Log.d(TAG, "getTotalUnread: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}