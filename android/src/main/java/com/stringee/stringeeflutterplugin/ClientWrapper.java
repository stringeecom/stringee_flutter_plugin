package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.common.StringeeConstant;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.ChatRequest.State;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject.Type;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListener;
import com.stringee.messaging.listeners.LiveChatEventListener;
import com.stringee.messaging.listeners.UserTypingEventListener;
import com.stringee.stringeeflutterplugin.call.Call2Wrapper;
import com.stringee.stringeeflutterplugin.call.CallWrapper;
import com.stringee.stringeeflutterplugin.call.StringeeCallWrapper;
import com.stringee.stringeeflutterplugin.call.enumeration.StringeeCallType;
import com.stringee.stringeeflutterplugin.chat.ChatRequestManager;
import com.stringee.stringeeflutterplugin.chat.ChatUtils;
import com.stringee.stringeeflutterplugin.chat.ConversationManager;
import com.stringee.stringeeflutterplugin.chat.MessageManager;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.StringeeEventType;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.conference.VideoConferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ClientWrapper implements StringeeConnectionListener, ChangeEventListener, LiveChatEventListener, UserTypingEventListener {
    private final StringeeClient client;
    private final ConversationManager conversationManager;
    private final MessageManager messageManager;
    private final ChatRequestManager chatRequestManager;
    private final VideoConferenceManager videoConferenceManager;
    private final String uuid;
    private StatusListener autoReconnectListener;

    public ClientWrapper(final Context context, final String uuid, final String baseAPIUrl) {
        this.uuid = uuid;
        conversationManager = new ConversationManager(this);
        messageManager = new MessageManager(this);
        chatRequestManager = new ChatRequestManager(this);
        videoConferenceManager = new VideoConferenceManager(this);
        client = new StringeeClient(context);
        if (!Utils.isEmpty(baseAPIUrl)) {
            client.setBaseAPIUrl(baseAPIUrl);
        }
        setListener();
    }

    private void setListener() {
        client.setConnectionListener(this);
        client.setChangeEventListener(this);
        client.setLiveChatEventListener(this);
        client.setUserTypingEventListener(this);
    }

    public StringeeClient getClient() {
        return client;
    }

    public String getId() {
        return uuid;
    }

    public StringeeCallWrapper callWrapper(final String callId) {
        return StringeeManager.getInstance().getCallsMap().get(callId);
    }

    /**
     * Make a new call
     */
    public CallWrapper callWrapper(final String from, final String to, final boolean isVideoCall,
                                   final String customData, final String videoResolution) {
        StringeeCall call = new StringeeCall(client, from, to);
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
        CallWrapper callWrapper = new CallWrapper(this, call);
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    /**
     * Make a new call2
     */
    public Call2Wrapper call2Wrapper(final String from, final String to, final boolean isVideoCall,
                                     final String customData) {
        StringeeCall2 call = new StringeeCall2(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }

        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call);
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), call2Wrapper);
        return call2Wrapper;
    }

    public ConversationManager conversation() {
        return conversationManager;
    }

    public MessageManager message() {
        return messageManager;
    }

    public ChatRequestManager chatRequest() {
        return chatRequestManager;
    }

    public VideoConferenceManager videoConference() {
        return videoConferenceManager;
    }

    @Override
    public void onConnectionConnected(final StringeeClient stringeeClient, final boolean b) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onConnectionConnected: " + stringeeClient.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didConnect");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
            bodyMap.put("isReconnecting", b);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onConnectionDisconnected: " + stringeeClient.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didDisconnect");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
            bodyMap.put("isReconnecting", b);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onIncomingCall: " + stringeeCall.getCallId());
            StringeeManager.getInstance()
                    .getCallsMap()
                    .put(stringeeCall.getCallId(),
                            new CallWrapper(ClientWrapper.this, stringeeCall));
            if (autoReconnectListener != null) {
                autoReconnectListener.onSuccess();
                autoReconnectListener = null;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
                map.put("event", "incomingCall");
                map.put("uuid", uuid);
                Map<String, Object> callInfoMap = new HashMap<>();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                int callType = StringeeCallType.APP_TO_APP_OUTGOING.getValue();
                if (!stringeeCall.getFrom().equals(client.getUserId())) {
                    callType = StringeeCallType.APP_TO_APP_Incoming.getValue();
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = StringeeCallType.APP_TO_PHONE.getValue();
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = StringeeCallType.PHONE_TO_APP.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer",
                        stringeeCall.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onIncomingCall2: " + stringeeCall2.getCallId());
            StringeeManager.getInstance()
                    .getCallsMap()
                    .put(stringeeCall2.getCallId(),
                            new Call2Wrapper(ClientWrapper.this, stringeeCall2));
            if (autoReconnectListener != null) {
                autoReconnectListener.onSuccess();
                autoReconnectListener = null;
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
                map.put("event", "incomingCall2");
                map.put("uuid", uuid);
                Map<String, Object> callInfoMap = new HashMap<>();
                callInfoMap.put("callId", stringeeCall2.getCallId());
                callInfoMap.put("from", stringeeCall2.getFrom());
                callInfoMap.put("to", stringeeCall2.getTo());
                callInfoMap.put("fromAlias", stringeeCall2.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall2.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall2.isVideoCall());
                int callType = StringeeCallType.APP_TO_APP_OUTGOING.getValue();
                if (!stringeeCall2.getFrom().equals(client.getUserId())) {
                    callType = StringeeCallType.APP_TO_APP_Incoming.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall2.isVideoCall());
                callInfoMap.put("customDataFromYourServer",
                        stringeeCall2.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient,
                                  final StringeeError stringeeError) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onConnectionError: " + stringeeError.getCode() + " - " +
                    stringeeError.getMessage());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didFailWithError");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("userId", stringeeClient.getUserId());
            bodyMap.put("code", stringeeError.getCode());
            bodyMap.put("message", stringeeError.getMessage());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
            if (autoReconnectListener != null) {
                autoReconnectListener.onError(stringeeError);
                autoReconnectListener = null;
            }
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onRequestNewToken: " + stringeeClient.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "requestAccessToken");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("userId", stringeeClient.getUserId());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onCustomMessage: " + from + " - " + jsonObject.toString());
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
                map.put("event", "didReceiveCustomMessage");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("fromUserId", from);
                bodyMap.put("message", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Utils.reportException(ClientWrapper.class, e);
            }
        });
    }

    @Override
    public void onTopicMessage(String s, JSONObject jsonObject) {

    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " +
                    stringeeChange.getChangeType());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CHAT_EVENT.getValue());
            map.put("event", "didReceiveChangeEvent");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            Type objectType = stringeeChange.getObjectType();
            bodyMap.put("objectType", objectType.getValue());
            bodyMap.put("changeType", stringeeChange.getChangeType().getValue());
            List<Map<String, Object>> objects = new ArrayList<>();
            Map<String, Object> objectMap = new HashMap<>();
            if (objectType == Type.CONVERSATION) {
                objectMap = ChatUtils.convertConversationToMap(
                        (Conversation) stringeeChange.getObject());
            } else if (objectType == Type.MESSAGE) {
                objectMap = ChatUtils.convertMessageToMap((Message) stringeeChange.getObject());
            }
            objects.add(objectMap);
            bodyMap.put("objects", objects);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onReceiveChatRequest(ChatRequest chatRequest) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onReceiveChatRequest: " + chatRequest.getConvId() + " - from: " +
                    chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didReceiveChatRequest");
            map.put("uuid", uuid);
            map.put("body", ChatUtils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
        Utils.post(() -> {
            Log.d(Constants.TAG,
                    "onReceiveTransferChatRequest: " + chatRequest.getConvId() + " - from: " +
                            chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didReceiveTransferChatRequest");
            map.put("uuid", uuid);
            map.put("body", ChatUtils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onHandleOnAnotherDevice(ChatRequest chatRequest, State state) {

    }

    @Override
    public void onTimeoutAnswerChat(ChatRequest chatRequest) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onTimeoutAnswerChat: " + chatRequest.getConvId() + " - from: " +
                    chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "timeoutAnswerChat");
            map.put("uuid", uuid);
            map.put("body", ChatUtils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onTimeoutInQueue(Conversation conversation) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onTimeoutInQueue: " + conversation.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "timeoutInQueue");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            User user = client.getUser(client.getUserId());
            bodyMap.put("customerId", user.getUserId());
            bodyMap.put("customerName", user.getName());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConversationEnded(Conversation conversation, User user) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onConversationEnded: " + conversation.getId() + " - endedBy: " +
                    user.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "conversationEnded");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("endedby", user.getUserId());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onTyping(Conversation conversation, User user) {
        Utils.post(() -> {
            Log.d(Constants.TAG,
                    "onTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "userBeginTyping");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("userId", user.getUserId());
            bodyMap.put("displayName", user.getUserId());
            String userName = user.getName();
            if (userName != null) {
                if (!Utils.isEmpty(userName.trim())) {
                    bodyMap.put("displayName", userName);
                }
            }
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onEndTyping(Conversation conversation, User user) {
        Utils.post(() -> {
            Log.d(Constants.TAG,
                    "onEndTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "userEndTyping");
            map.put("uuid", uuid);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("userId", user.getUserId());
            bodyMap.put("displayName", user.getUserId());
            String userName = user.getName();
            if (userName != null) {
                if (!Utils.isEmpty(userName.trim())) {
                    bodyMap.put("displayName", userName);
                }
            }
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    /**
     * Connect to Stringee server
     */
    public void connect(final String serverAddresses, final String token, final Result result) {
        connect(serverAddresses, token);
        Log.d(Constants.TAG, "connect: success");
        result.success(FlutterResult.success("connect").getMap());
    }

    public void connect(final String serverAddresses, final String token) {
        List<SocketAddress> socketAddressList = new ArrayList<>();
        if (!Utils.isEmpty(serverAddresses)) {
            try {
                JSONArray array = new JSONArray(serverAddresses);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = (JSONObject) array.get(i);
                    String host = object.optString("host");
                    int port = object.optInt("port", -1);
                    if (!Utils.isEmpty(host) && port != -1) {
                        SocketAddress socketAddress = new SocketAddress(host, port);
                        socketAddressList.add(socketAddress);
                    }
                }
            } catch (Exception e) {
                Utils.reportException(StringeeFlutterPlugin.class, e);
            }
        }
        if (!Utils.isEmpty(socketAddressList)) {
            client.setHost(socketAddressList);
        }
        client.connect(token);
    }

    public void connect(final String serverAddresses, final String token,
                        final StatusListener listener) {
        autoReconnectListener = listener;
        connect(serverAddresses, token);
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect(final Result result) {
        client.disconnect();
        Log.d(Constants.TAG, "disconnect: success");
        result.success(FlutterResult.success("disconnect").getMap());
    }

    /**
     * Register push notification
     */
    public void registerPush(final String registrationToken, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "registerPush: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.registerPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "registerPush: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "registerPush: false - " + error.getCode() + " - " +
                            error.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Register push notification and delete another token of other packages
     */
    public void registerPushAndDeleteOthers(final String registrationToken,
                                            final List<String> packageNames, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "registerPush: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.registerPushTokenAndDeleteOthers(registrationToken, packageNames,
                new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "registerPush: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError error) {
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "registerPush: false - " + error.getCode() + " - " +
                                            error.getMessage());
                            Map<String, Object> map = new HashMap<>();
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
     */
    public void unregisterPush(final String registrationToken, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "unregisterPush: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.unregisterPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "unregisterPush: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "unregisterPush: false - " + error.getCode() + " - " +
                            error.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", error.getCode());
                    map.put("message", error.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send a custom message
     */
    public void sendCustomMessage(final String toUserId, final JSONObject data,
                                  final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "sendCustomMessage: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.sendCustomMessage(toUserId, data, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "sendCustomMessage: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "sendCustomMessage: false - " + error.getCode() + " - " +
                            error.getMessage());
                    Map<String, Object> map = new HashMap<>();
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
     */
    public void createConversation(final List<User> participants, final ConversationOptions options,
                                   final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "createConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createConversation(participants, options, new CallbackListener<>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "createConversation: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ChatUtils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "createConversation: false - " + error.getCode() + " - " +
                            error.getMessage());
                    Map<String, Object> map = new HashMap<>();
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
     */
    public void getConversationById(final String convId, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "getConversationById: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationFromServer(convId, new CallbackListener<>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getConversationById: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ChatUtils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getConversationById: false - " + error.getCode() + " - " +
                            error.getMessage());
                    Map<String, Object> map = new HashMap<>();
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
     */
    public void getConversationByUserId(final String userId, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "getConversationByUserId: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationByUserId(userId, new CallbackListener<>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getConversationByUserId: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ChatUtils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getConversationByUserId: false - " + error.getCode() + " - " +
                                    error.getMessage());
                    Map<String, Object> map = new HashMap<>();
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
     */
    public void getLocalConversations(final String oaId, final Result result) {
        client.getLocalConversations(client.getUserId(), oaId, new CallbackListener<>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(Constants.TAG, "getLocalConversations: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(ChatUtils.convertConversationToMap(conversations.get(i)));
                    }
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getLocalConversations: false - " + stringeeError.getCode() + " - " +
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
     * Get last conversations
     */
    public void getLastConversation(final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "getLastConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getLastConversations(count, oaId, new CallbackListener<>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(Constants.TAG, "getLastConversation: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(ChatUtils.convertConversationToMap(conversations.get(i)));
                    }
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getLastConversation: false - " + stringeeError.getCode() + " - " +
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
     * Get conversations update before '$updateAt'
     */
    public void getConversationsBefore(final long updateAt, final int count, final String oaId,
                                       final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "getConversationsBefore: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsBefore(updateAt, count, oaId, new CallbackListener<>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(Constants.TAG, "getConversationsBefore: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(ChatUtils.convertConversationToMap(conversations.get(i)));
                    }
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getConversationsBefore: false - " + stringeeError.getCode() + " - " +
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
     * Get conversations update after '$updateAt'
     */
    public void getConversationsAfter(final long updateAt, final int count, final String oaId,
                                      final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "getConversationsAfter: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsAfter(updateAt, count, oaId, new CallbackListener<>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(Constants.TAG, "getConversationsAfter: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(ChatUtils.convertConversationToMap(conversations.get(i)));
                    }
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getConversationsAfter: false - " + stringeeError.getCode() + " - " +
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
     * Clear local database
     */
    public void clearDb(final Result result) {
        client.clearDb();
        Log.d(Constants.TAG, "clearDb: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Block user
     */
    public void blockUser(final String userId, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "blockUser: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.blockUser(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "blockUser: success");
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
                    Log.d(Constants.TAG, "blockUser: false - " + stringeeError.getCode() + " - " +
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
     * Get total unread conversations
     */
    public void getTotalUnread(final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "getTotalUnread: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getTotalUnread(new CallbackListener<>() {
            @Override
            public void onSuccess(final Integer integer) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getTotalUnread: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", integer);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getTotalUnread: false - " + stringeeError.getCode() + " - " +
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
     * Get chat profile
     */
    public void getChatProfile(String key, final Result result) {
        client.getChatProfile(key, new CallbackListener<>() {
            @Override
            public void onSuccess(final ChatProfile chatProfile) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getChatProfile: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ChatUtils.convertChatProfileToMap(chatProfile));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getChatProfile: false - " + stringeeError.getCode() + " - " +
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
     * Get live chat token
     */
    public void getLiveChatToken(String key, String name, String email, final Result result) {
        client.getLiveChatToken(key, name, email, new CallbackListener<>() {
            @Override
            public void onSuccess(final String token) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "getLiveChatToken: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", token);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "getLiveChatToken: false - " + stringeeError.getCode() + " - " +
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
     * Update user info
     */
    public void updateUserInfo(String name, String email, String avatar, String phone,
                               final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "updateUserInfo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.updateUser(name, email, avatar, phone, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "updateUserInfo: success");
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
                            "updateUserInfo: false - " + stringeeError.getCode() + " - " +
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
     * Create live chat conversation
     */
    public void createLiveChatConversation(final String queueId, final String customData,
                                           final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "createLiveChatConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createLiveChat(queueId, customData, new CallbackListener<>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "createLiveChatConversation: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ChatUtils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "convertConversationToMap: false - " + stringeeError.getCode() + " - " +
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
     * Create live chat ticket
     */
    public void createLiveChatTicket(String key, String name, String email, String note,
                                     final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG,
                    "createLiveChatTicket: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createLiveChatTicket(key, name, email, note, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "createLiveChatTicket: success");
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
                            "createLiveChatTicket: false - " + stringeeError.getCode() + " - " +
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

    public void joinOaConversation(final String convId, final Result result) {
        if (!isConnected()) {
            Log.d(Constants.TAG, "joinOaConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.joinOaConversation(convId, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "joinOaConversation: success");
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
                            "joinOaConversation: false - " + stringeeError.getCode() + " - " +
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
