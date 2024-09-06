package com.stringee.stringeeflutterplugin;

import android.text.TextUtils;

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
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeCallType;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ClientWrapper implements StringeeConnectionListener, ChangeEventListener, LiveChatEventListener, UserTypingEventListener {
    private final StringeeClient client;
    private final StringeeManager stringeeManager;
    private final ConversationManager conversationManager;
    private final MessageManager messageManager;
    private final ChatRequestManager chatRequestManager;
    private final VideoConferenceManager videoConferenceManager;
    private final String uuid;

    public ClientWrapper(final String uuid) {
        stringeeManager = StringeeManager.getInstance();
        this.uuid = uuid;
        conversationManager = new ConversationManager(this);
        messageManager = new MessageManager(this);
        chatRequestManager = new ChatRequestManager(this);
        videoConferenceManager = new VideoConferenceManager(this);
        client = new StringeeClient(stringeeManager.getContext());
        setListener();
    }

    public ClientWrapper(final String uuid, final String baseAPIUrl) {
        stringeeManager = StringeeManager.getInstance();
        this.uuid = uuid;
        conversationManager = new ConversationManager(this);
        messageManager = new MessageManager(this);
        chatRequestManager = new ChatRequestManager(this);
        videoConferenceManager = new VideoConferenceManager(this);
        client = new StringeeClient(stringeeManager.getContext());
        if (!Utils.isStringEmpty(baseAPIUrl)) {
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
        return stringeeManager.getCallsMap().get(callId);
    }

    /**
     * Make a new call
     *
     * @param from            from
     * @param to              to
     * @param isVideoCall     isVideoCall
     * @param customData      customData
     * @param videoResolution videoResolution
     * @param result          result
     */
    public CallWrapper callWrapper(final String from, final String to, final boolean isVideoCall, final String customData, final String videoResolution, final Result result) {
        StringeeCall call = new StringeeCall(client, from, to);
        call.setVideoCall(isVideoCall);
        if (!Utils.isStringEmpty(customData)) {
            call.setCustom(customData);
        }
        if (!Utils.isStringEmpty(videoResolution)) {
            if (videoResolution.equalsIgnoreCase("NORMAL")) {
                call.setQuality(StringeeConstant.QUALITY_NORMAL);
            } else if (videoResolution.equalsIgnoreCase("HD")) {
                call.setQuality(StringeeConstant.QUALITY_HD);
            } else if (videoResolution.equalsIgnoreCase("FULLHD")) {
                call.setQuality(StringeeConstant.QUALITY_FULLHD);
            }
        }
        CallWrapper callWrapper = new CallWrapper(this, call, result);
        stringeeManager.getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    public Call2Wrapper call2Wrapper(final String callId) {
        return stringeeManager.getCall2sMap().get(callId);
    }

    /**
     * Make a new call2
     *
     * @param from        from
     * @param to          to
     * @param isVideoCall isVideoCall
     * @param customData  customData
     * @param result      result
     */
    public Call2Wrapper call2Wrapper(final String from, final String to, final boolean isVideoCall, final String customData, final Result result) {
        StringeeCall2 call = new StringeeCall2(client, from, to);
        call.setVideoCall(isVideoCall);
        if (!Utils.isStringEmpty(customData)) {
            call.setCustom(customData);
        }

        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call, result);
        stringeeManager.getCall2sMap().put(call.getCallId(), call2Wrapper);
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
            Map<String, Object> map = Utils.createEventMap("didConnect", uuid, StringeeEventType.ClientEvent);
            Logging.d("userId: " + stringeeClient.getUserId());
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
            Map<String, Object> map = Utils.createEventMap("didDisconnect", uuid, StringeeEventType.ClientEvent);
            Logging.d("userId: " + stringeeClient.getUserId());
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
            stringeeManager.getCallsMap().put(stringeeCall.getCallId(), new CallWrapper(ClientWrapper.this, stringeeCall));
            Map<String, Object> map = Utils.createEventMap("incomingCall", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> callInfoMap = new HashMap<>();
            callInfoMap.put("callId", stringeeCall.getCallId());
            callInfoMap.put("from", stringeeCall.getFrom());
            callInfoMap.put("to", stringeeCall.getTo());
            callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
            callInfoMap.put("toAlias", stringeeCall.getToAlias());
            callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
            int callType = StringeeCallType.AppToAppOutgoing.getValue();
            if (!stringeeCall.getFrom().equals(client.getUserId())) {
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
            Logging.d("callInfo: " + callInfoMap);
            map.put("body", callInfoMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        Utils.post(() -> {
            stringeeManager.getCall2sMap().put(stringeeCall2.getCallId(), new Call2Wrapper(ClientWrapper.this, stringeeCall2));
            Map<String, Object> map = Utils.createEventMap("incomingCall2", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> callInfoMap = new HashMap<>();
            callInfoMap.put("callId", stringeeCall2.getCallId());
            callInfoMap.put("from", stringeeCall2.getFrom());
            callInfoMap.put("to", stringeeCall2.getTo());
            callInfoMap.put("fromAlias", stringeeCall2.getFromAlias());
            callInfoMap.put("toAlias", stringeeCall2.getToAlias());
            callInfoMap.put("isVideocall", stringeeCall2.isVideoCall());
            int callType = StringeeCallType.AppToAppOutgoing.getValue();
            if (!stringeeCall2.getFrom().equals(client.getUserId())) {
                callType = StringeeCallType.AppToAppIncoming.getValue();
            }
            callInfoMap.put("callType", callType);
            callInfoMap.put("isVideoCall", stringeeCall2.isVideoCall());
            callInfoMap.put("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
            Logging.d("callInfo: " + callInfoMap);
            map.put("body", callInfoMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didFailWithError", uuid, StringeeEventType.ClientEvent);
            Logging.d("error: " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
            Map<String, Object> map = Utils.createEventMap("requestAccessToken", uuid, StringeeEventType.ClientEvent);
            Logging.d("userId: " + stringeeClient.getUserId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("userId", stringeeClient.getUserId());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        Utils.post(() -> {
            try {
                Map<String, Object> map = Utils.createEventMap("didReceiveCustomMessage", uuid, StringeeEventType.ClientEvent);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("fromUserId", from);
                bodyMap.put("message", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Logging.e(ClientWrapper.class, e);
            }
            Logging.d("customMessage: " + from + " - " + jsonObject.toString());
        });
    }

    @Override
    public void onTopicMessage(String s, JSONObject jsonObject) {

    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didReceiveChangeEvent", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            Type objectType = stringeeChange.getObjectType();
            bodyMap.put("objectType", objectType.getValue());
            bodyMap.put("changeType", stringeeChange.getChangeType().getValue());
            ArrayList<Map<String, Object>> objects = new ArrayList<>();
            Map<String, Object> objectMap = new HashMap<>();
            if (objectType == Type.CONVERSATION) {
                objectMap = Utils.convertConversationToMap((Conversation) stringeeChange.getObject());
            } else if (objectType == Type.MESSAGE) {
                objectMap = Utils.convertMessageToMap((Message) stringeeChange.getObject());
            }
            objects.add(objectMap);
            Logging.d("objects: " + objects);
            bodyMap.put("objects", objects);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onReceiveChatRequest(ChatRequest chatRequest) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didReceiveChatRequest", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> chatRequestMap = Utils.convertChatRequestToMap(chatRequest);
            Logging.d("chatRequest: " + chatRequestMap);
            map.put("body", chatRequestMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didReceiveTransferChatRequest", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> chatRequestMap = Utils.convertChatRequestToMap(chatRequest);
            Logging.d("chatRequest: " + chatRequestMap);
            map.put("body", chatRequestMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onHandleOnAnotherDevice(ChatRequest chatRequest, State state) {

    }

    @Override
    public void onTimeoutAnswerChat(ChatRequest chatRequest) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("timeoutAnswerChat", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> chatRequestMap = Utils.convertChatRequestToMap(chatRequest);
            Logging.d("chatRequest: " + chatRequestMap);
            map.put("body", chatRequestMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onTimeoutInQueue(Conversation conversation) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("timeoutInQueue", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            User user = client.getUser(client.getUserId());
            bodyMap.put("customerId", user.getUserId());
            bodyMap.put("customerName", user.getName());
            Logging.d("conversation: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConversationEnded(Conversation conversation, User user) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("conversationEnded", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("endedby", user.getUserId());
            Logging.d("conversation: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onTyping(Conversation conversation, User user) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("userBeginTyping", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("userId", user.getUserId());
            bodyMap.put("displayName", user.getUserId());
            String userName = user.getName();
            if (!Utils.isStringEmpty(userName)) {
                if (!TextUtils.isEmpty(userName.trim())) {
                    bodyMap.put("displayName", userName);
                }
            }
            Logging.d("conversation: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onEndTyping(Conversation conversation, User user) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("userEndTyping", uuid, StringeeEventType.ClientEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("convId", conversation.getId());
            bodyMap.put("userId", user.getUserId());
            bodyMap.put("displayName", user.getUserId());
            String userName = user.getName();
            if (!Utils.isStringEmpty(userName)) {
                bodyMap.put("displayName", userName);
            }
            Logging.d("conversation: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    /**
     * Connect to Stringee server
     *
     * @param token             token
     * @param socketAddressList socketAddressList
     * @param result            result
     */
    public void connect(final List<SocketAddress> socketAddressList, final String token, final Result result) {
        if (!Utils.isListEmpty(socketAddressList)) {
            client.setHost(socketAddressList);
        }
        client.connect(token);
        Map<String, Object> map = Utils.createSuccessMap("connect");
        result.success(map);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Disconnect from Stringee server
     *
     * @param result result
     */
    public void disconnect(final Result result) {
        client.disconnect();
        Map<String, Object> map = Utils.createSuccessMap("disconnect");
        result.success(map);
    }

    /**
     * Register push notification
     *
     * @param registrationToken registrationToken
     * @param result            result
     */
    public void registerPush(final String registrationToken, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("registerPush");
            result.success(map);
            return;
        }

        client.registerPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("registerPush");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("registerPush", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Register push notification and delete another token of other packages
     *
     * @param registrationToken registrationToken
     * @param result            result
     */
    public void registerPushAndDeleteOthers(final String registrationToken, final List<String> packageNames, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("registerPushAndDeleteOthers");
            result.success(map);
            return;
        }

        client.registerPushTokenAndDeleteOthers(registrationToken, packageNames, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("registerPushAndDeleteOthers");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("registerPushAndDeleteOthers", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Unregister push notification
     *
     * @param registrationToken registrationToken
     * @param result            result
     */
    public void unregisterPush(final String registrationToken, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("unregisterPush");
            result.success(map);
            return;
        }

        client.unregisterPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("unregisterPush");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("unregisterPush", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send a custom message
     *
     * @param toUserId toUserId
     * @param data     data
     * @param result   result
     */
    public void sendCustomMessage(final String toUserId, final JSONObject data, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendCustomMessage");
            result.success(map);
            return;
        }

        client.sendCustomMessage(toUserId, data, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("sendCustomMessage");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("sendCustomMessage", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Create new conversation
     *
     * @param participants participants
     * @param options      options
     * @param result       result
     */
    public void createConversation(final List<User> participants, final ConversationOptions options, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("createConversation");
            result.success(map);
            return;
        }

        client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("createConversation");
                    Map<String, Object> convMap = Utils.convertConversationToMap(conversation);
                    Logging.d(convMap.toString());
                    map.put("body", convMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("createConversation", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get conversation by id
     *
     * @param convId convId
     * @param result result
     */
    public void getConversationById(final String convId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getConversationById");
            result.success(map);
            return;
        }

        client.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getConversationById");
                    Map<String, Object> convMap = Utils.convertConversationToMap(conversation);
                    Logging.d(convMap.toString());
                    map.put("body", convMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getConversationById", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get conversation by user id
     *
     * @param userId userId
     * @param result result
     */
    public void getConversationByUserId(final String userId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getConversationByUserId");
            result.success(map);
            return;
        }

        client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getConversationByUserId");
                    Map<String, Object> convMap = Utils.convertConversationToMap(conversation);
                    Logging.d(convMap.toString());
                    map.put("body", convMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getConversationByUserId", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get local conversations
     *
     * @param result result
     */
    public void getLocalConversations(final String oaId, final Result result) {
        client.getLocalConversations(client.getUserId(), oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getLocalConversations");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (Conversation conversation : conversations) {
                        bodyArray.add(Utils.convertConversationToMap(conversation));
                    }
                    Logging.d(bodyArray.toString());
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getLocalConversations", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });

    }

    /**
     * Get last conversations
     *
     * @param count  count
     * @param result result
     */
    public void getLastConversation(final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getLastConversation");
            result.success(map);
            return;
        }

        client.getLastConversations(count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getLastConversation");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (Conversation conversation : conversations) {
                        bodyArray.add(Utils.convertConversationToMap(conversation));
                    }
                    Logging.d(bodyArray.toString());
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getLastConversation", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get conversations update before '$updateAt'
     *
     * @param updateAt updateAt
     * @param count    count
     * @param result   result
     */
    public void getConversationsBefore(final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getConversationsBefore");
            result.success(map);
            return;
        }

        client.getConversationsBefore(updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getConversationsBefore");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (Conversation conversation : conversations) {
                        bodyArray.add(Utils.convertConversationToMap(conversation));
                    }
                    Logging.d(bodyArray.toString());
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getConversationsBefore", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get conversations update after '$updateAt'
     *
     * @param updateAt updateAt
     * @param count    count
     * @param result   result
     */
    public void getConversationsAfter(final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getConversationsAfter");
            result.success(map);
            return;
        }

        client.getConversationsAfter(updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getConversationsAfter");
                    List<Map<String, Object>> bodyArray = new ArrayList<>();
                    for (Conversation conversation : conversations) {
                        bodyArray.add(Utils.convertConversationToMap(conversation));
                    }
                    Logging.d(bodyArray.toString());
                    map.put("body", bodyArray);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getConversationsAfter", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Clear local database
     *
     * @param result result
     */
    public void clearDb(final Result result) {
        client.clearDb();
        Map<String, Object> map = Utils.createSuccessMap("clearDb");
        result.success(map);
    }

    /**
     * Block user
     *
     * @param userId userId
     * @param result result
     */
    public void blockUser(final String userId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("blockUser");
            result.success(map);
            return;
        }

        client.blockUser(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("blockUser");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("blockUser", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get total unread conversations
     *
     * @param result result
     */
    public void getTotalUnread(final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("getTotalUnread");
            result.success(map);
            return;
        }

        client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getTotalUnread");
                    Logging.d(String.valueOf(integer));
                    map.put("body", integer);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getTotalUnread", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get chat profile
     *
     * @param key    key
     * @param result result
     */
    public void getChatProfile(String key, final Result result) {
        client.getChatProfile(key, new CallbackListener<ChatProfile>() {
            @Override
            public void onSuccess(final ChatProfile chatProfile) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getChatProfile");
                    Map<String, Object> chatProfileMap = Utils.convertChatProfileToMap(chatProfile);
                    Logging.d(chatProfileMap.toString());
                    map.put("body", chatProfileMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getChatProfile", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Get live chat token
     *
     * @param key    key
     * @param name   name
     * @param email  email
     * @param result result
     */
    public void getLiveChatToken(String key, String name, String email, final Result result) {
        client.getLiveChatToken(key, name, email, new CallbackListener<String>() {
            @Override
            public void onSuccess(final String token) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("getLiveChatToken");
                    Logging.d(token);
                    map.put("body", token);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("getLiveChatToken", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Update user info
     *
     * @param name   name
     * @param email  email
     * @param avatar avatar
     * @param result result
     */
    public void updateUserInfo(String name, String email, String avatar, String phone, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("updateUserInfo");
            result.success(map);
            return;
        }

        client.updateUser(name, email, avatar, phone, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("updateUserInfo");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("updateUserInfo", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Create live chat conversation
     *
     * @param queueId queueId
     * @param result  result
     */
    public void createLiveChatConversation(final String queueId, final String customData, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("createLiveChatConversation");
            result.success(map);
            return;
        }

        client.createLiveChat(queueId, customData, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("createLiveChatConversation");
                    Map<String, Object> convMap = Utils.convertConversationToMap(conversation);
                    Logging.d(convMap.toString());
                    map.put("body", convMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("convertConversationToMap", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Create live chat ticket
     *
     * @param key    key
     * @param name   name
     * @param email  email
     * @param note   note
     * @param result result
     */
    public void createLiveChatTicket(String key, String name, String email, String note, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("createLiveChatTicket");
            result.success(map);
            return;
        }

        client.createLiveChatTicket(key, name, email, note, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("createLiveChatTicket");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("createLiveChatTicket", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void joinOaConversation(final String convId, final Result result) {
        if (!isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("joinOaConversation");
            result.success(map);
            return;
        }

        client.joinOaConversation(convId, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("joinOaConversation");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("joinOaConversation", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}