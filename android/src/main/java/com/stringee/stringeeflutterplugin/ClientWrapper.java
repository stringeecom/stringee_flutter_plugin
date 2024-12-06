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
import com.stringee.stringeeflutterplugin.common.enumeration.StringeeCallType;
import com.stringee.stringeeflutterplugin.common.enumeration.StringeeEventType;

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

    private static final String TAG = "StringeeSDK";

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

    public CallWrapper callWrapper(final String callId) {
        return StringeeManager.getInstance().getCallsMap().get(callId);
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
        CallWrapper callWrapper = new CallWrapper(this, call, result);
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    public Call2Wrapper call2Wrapper(final String callId) {
        return StringeeManager.getInstance().getCall2sMap().get(callId);
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
        StringeeCall2 call = new StringeeCall2(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }

        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call, result);
        StringeeManager.getInstance().getCall2sMap().put(call.getCallId(), call2Wrapper);
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
            Log.d(TAG, "onConnectionConnected: " + stringeeClient.getUserId());
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
            Log.d(TAG, "onConnectionDisconnected: " + stringeeClient.getUserId());
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
            Log.d(TAG, "onIncomingCall: " + stringeeCall.getCallId());
            StringeeManager.getInstance().getCallsMap().put(stringeeCall.getCallId(), new CallWrapper(ClientWrapper.this, stringeeCall));
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
            callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
            map.put("body", callInfoMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        Utils.post(() -> {
            Log.d(TAG, "onIncomingCall2: " + stringeeCall2.getCallId());
            StringeeManager.getInstance().getCall2sMap().put(stringeeCall2.getCallId(), new Call2Wrapper(ClientWrapper.this, stringeeCall2));
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
            callInfoMap.put("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
            map.put("body", callInfoMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        Utils.post(() -> {
            Log.d(TAG, "onConnectionError: " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        Utils.post(() -> {
            Log.d(TAG, "onRequestNewToken: " + stringeeClient.getUserId());
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
            Log.d(TAG, "onCustomMessage: " + from + " - " + jsonObject.toString());
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
            Log.d(TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " + stringeeChange.getChangeType());
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
                objectMap = Utils.convertConversationToMap((Conversation) stringeeChange.getObject());
            } else if (objectType == Type.MESSAGE) {
                objectMap = Utils.convertMessageToMap((Message) stringeeChange.getObject());
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
            Log.d(TAG, "onReceiveChatRequest: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didReceiveChatRequest");
            map.put("uuid", uuid);
            map.put("body", Utils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
        Utils.post(() -> {
            Log.d(TAG, "onReceiveTransferChatRequest: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "didReceiveTransferChatRequest");
            map.put("uuid", uuid);
            map.put("body", Utils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onHandleOnAnotherDevice(ChatRequest chatRequest, State state) {

    }

    @Override
    public void onTimeoutAnswerChat(ChatRequest chatRequest) {
        Utils.post(() -> {
            Log.d(TAG, "onTimeoutAnswerChat: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CLIENT_EVENT.getValue());
            map.put("event", "timeoutAnswerChat");
            map.put("uuid", uuid);
            map.put("body", Utils.convertChatRequestToMap(chatRequest));
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onTimeoutInQueue(Conversation conversation) {
        Utils.post(() -> {
            Log.d(TAG, "onTimeoutInQueue: " + conversation.getId());
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
            Log.d(TAG, "onConversationEnded: " + conversation.getId() + " - endedBy: " + user.getUserId());
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
            Log.d(TAG, "onTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
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
            Log.d(TAG, "onEndTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
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
     *
     * @param token
     * @param socketAddressList
     * @param result
     */
    public void connect(final List<SocketAddress> socketAddressList, final String token, final Result result) {
        client.setHost(socketAddressList);
        client.connect(token);
        Log.d(TAG, "connect: success");
        Map<String, Object> map = new HashMap<>();
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
        client.connect(token);
        Log.d(TAG, "connect: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Disconnect from Stringee server
     *
     * @param result
     */
    public void disconnect(final Result result) {
        client.disconnect();
        Log.d(TAG, "disconnect: success");
        Map<String, Object> map = new HashMap<>();
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
        if (!isConnected()) {
            Log.d(TAG, "registerPush: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "registerPush: success");
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
                    Log.d(TAG, "registerPush: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param registrationToken
     * @param result
     */
    public void registerPushAndDeleteOthers(final String registrationToken, final List<String> packageNames, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "registerPush: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.registerPushTokenAndDeleteOthers(registrationToken, packageNames, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "registerPush: success");
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
                    Log.d(TAG, "registerPush: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param registrationToken
     * @param result
     */
    public void unregisterPush(final String registrationToken, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "unregisterPush: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "unregisterPush: success");
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
                    Log.d(TAG, "unregisterPush: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param toUserId
     * @param data
     * @param result
     */
    public void sendCustomMessage(final String toUserId, final JSONObject data, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "sendCustomMessage: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "sendCustomMessage: success");
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
                    Log.d(TAG, "sendCustomMessage: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param participants
     * @param options
     * @param result
     */
    public void createConversation(final List<User> participants, final ConversationOptions options, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(TAG, "createConversation: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(TAG, "createConversation: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param convId
     * @param result
     */
    public void getConversationById(final String convId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationById: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(TAG, "getConversationById: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(TAG, "getConversationById: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param userId
     * @param result
     */
    public void getConversationByUserId(final String userId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationByUserId: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(TAG, "getConversationByUserId: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> {
                    Log.d(TAG, "getConversationByUserId: false - " + error.getCode() + " - " + error.getMessage());
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
     *
     * @param result
     */
    public void getLocalConversations(final String oaId, final Result result) {
        client.getLocalConversations(client.getUserId(), oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(TAG, "getLocalConversations: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
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
                    Log.d(TAG, "getLocalConversations: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param count
     * @param result
     */
    public void getLastConversation(final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getLastConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getLastConversations(count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(TAG, "getLastConversation: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
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
                    Log.d(TAG, "getLastConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param updateAt
     * @param count
     * @param result
     */
    public void getConversationsBefore(final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationsBefore: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsBefore(updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(TAG, "getConversationsBefore: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
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
                    Log.d(TAG, "getConversationsBefore: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param updateAt
     * @param count
     * @param result
     */
    public void getConversationsAfter(final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationsAfter: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsAfter(updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    Log.d(TAG, "getConversationsAfter: success");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (int i = 0; i < conversations.size(); i++) {
                        bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
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
                    Log.d(TAG, "getConversationsAfter: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param result
     */
    public void clearDb(final Result result) {
        client.clearDb();
        Log.d(TAG, "clearDb: success");
        Map<String, Object> map = new HashMap<>();
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
        if (!isConnected()) {
            Log.d(TAG, "blockUser: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "blockUser: success");
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
                    Log.d(TAG, "blockUser: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param result
     */
    public void getTotalUnread(final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getTotalUnread: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                Utils.post(() -> {
                    Log.d(TAG, "getTotalUnread: success");
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
                    Log.d(TAG, "getTotalUnread: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param key
     * @param result
     */
    public void getChatProfile(String key, final Result result) {
        client.getChatProfile(key, new CallbackListener<ChatProfile>() {
            @Override
            public void onSuccess(final ChatProfile chatProfile) {
                Utils.post(() -> {
                    Log.d(TAG, "getChatProfile: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertChatProfileToMap(chatProfile));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(TAG, "getChatProfile: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param key
     * @param name
     * @param email
     * @param result
     */
    public void getLiveChatToken(String key, String name, String email, final Result result) {
        client.getLiveChatToken(key, name, email, new CallbackListener<String>() {
            @Override
            public void onSuccess(final String token) {
                Utils.post(() -> {
                    Log.d(TAG, "getLiveChatToken: success");
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
                    Log.d(TAG, "getLiveChatToken: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param name
     * @param email
     * @param avatar
     * @param result
     */
    public void updateUserInfo(String name, String email, String avatar, String phone, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "updateUserInfo: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "updateUserInfo: success");
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
                    Log.d(TAG, "updateUserInfo: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param queueId
     * @param result
     */
    public void createLiveChatConversation(final String queueId, final String customData, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createLiveChatConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createLiveChat(queueId, customData, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Log.d(TAG, "createLiveChatConversation: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertConversationToMap(conversation));
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Log.d(TAG, "convertConversationToMap: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
     *
     * @param key
     * @param name
     * @param email
     * @param note
     * @param result
     */
    public void createLiveChatTicket(String key, String name, String email, String note, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createLiveChatTicket: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "createLiveChatTicket: success");
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
                    Log.d(TAG, "createLiveChatTicket: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
            Log.d(TAG, "joinOaConversation: false - -1 - StringeeClient is disconnected");
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
                    Log.d(TAG, "joinOaConversation: success");
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
                    Log.d(TAG, "joinOaConversation: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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