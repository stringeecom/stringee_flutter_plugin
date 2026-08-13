package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.CallType;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.call.VideoQuality;
import com.stringee.common.SocketAddress;
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

/**
 * Owns one native {@link StringeeClient} and exposes its operations to the Flutter plugin.
 *
 * <p>Each wrapper is identified by a UUID so events from multiple Dart clients can share one
 * Flutter event channel without being mixed.</p>
 */
public class ClientWrapper implements StringeeConnectionListener, ChangeEventListener, LiveChatEventListener, UserTypingEventListener {

    private final StringeeClient client;
    private final ConversationManager conversationManager;
    private final MessageManager messageManager;
    private final ChatRequestManager chatRequestManager;
    private final VideoConferenceManager videoConferenceManager;
    private final String uuid;

    private static final String TAG = "StringeeSDK";

    /**
     * Creates a native client wrapper.
     *
     * @param context application context used by the Stringee SDK
     * @param uuid identifier supplied by the Dart client
     * @param baseAPIUrl optional private API endpoint; may be {@code null}
     */
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
        client.addConnectionListener(this);
        client.addChangeEventListener(this);
        client.addLiveChatEventListener(this);
        client.addUserTypingEventListener(this);
    }

    /** Returns the owned native Stringee client. */
    public StringeeClient getClient() {
        return client;
    }

    /** Returns the UUID used to route Flutter events to this client. */
    public String getId() {
        return uuid;
    }

    /** Returns the first-generation call wrapper for {@code callId}, or {@code null}. */
    public CallWrapper callWrapper(final String callId) {
        return StringeeManager.getInstance().getCallsMap().get(callId);
    }

    /**
     * Creates and stores an outgoing {@link StringeeCall} wrapper.
     *
     * @param from
     *         Caller user id.
     * @param to
     *         Callee user id or phone number.
     * @param isVideoCall
     *         True for a video call.
     * @param customData
     *         Optional custom call data.
     * @param videoResolution
     *         Optional video quality name from Flutter.
     * @param result
     *         Flutter method result for the make-call request.
     */
    public CallWrapper callWrapper(
            final String from, final String to, final boolean isVideoCall, final String customData,
            final String videoResolution, final Result result
    ) {
        StringeeCall call = new StringeeCall(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }
        if (videoResolution != null) {
            if (videoResolution.equalsIgnoreCase("NORMAL")) {
                call.setVideoQuality(VideoQuality.QUALITY_480P);
            } else if (videoResolution.equalsIgnoreCase("HD")) {
                call.setVideoQuality(VideoQuality.QUALITY_720P);
            } else if (videoResolution.equalsIgnoreCase("FULLHD")) {
                call.setVideoQuality(VideoQuality.QUALITY_1080P);
            }
        }
        CallWrapper callWrapper = new CallWrapper(this, call, result);
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    /** Returns the Call2 wrapper for {@code callId}, or {@code null}. */
    public Call2Wrapper call2Wrapper(final String callId) {
        return StringeeManager.getInstance().getCall2sMap().get(callId);
    }

    /**
     * Creates and stores an outgoing {@link StringeeCall2} wrapper.
     *
     * @param from
     *         Caller user id.
     * @param to
     *         Callee user id.
     * @param isVideoCall
     *         True for a video call.
     * @param customData
     *         Optional custom call data.
     * @param result
     *         Flutter method result for the make-call request.
     */
    public Call2Wrapper call2Wrapper(
            final String from, final String to, final boolean isVideoCall, final String customData,
            final Result result
    ) {
        StringeeCall2 call = new StringeeCall2(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }

        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call, result);
        StringeeManager.getInstance().getCall2sMap().put(call.getCallId(), call2Wrapper);
        return call2Wrapper;
    }

    /** Returns this client's conversation bridge. */
    public ConversationManager conversation() {
        return conversationManager;
    }

    /** Returns this client's message bridge. */
    public MessageManager message() {
        return messageManager;
    }

    /** Returns this client's live-chat request bridge. */
    public ChatRequestManager chatRequest() {
        return chatRequestManager;
    }

    /** Returns this client's video-conference bridge. */
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
            StringeeManager.getInstance().getCallsMap().put(
                    stringeeCall.getCallId(), new CallWrapper(
                            ClientWrapper.this, stringeeCall)
            );
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
            if (stringeeCall.getCallType() == CallType.APP_TO_PHONE) {
                callType = StringeeCallType.APP_TO_PHONE.getValue();
            } else if (stringeeCall.getCallType() == CallType.PHONE_TO_APP) {
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
            StringeeManager.getInstance().getCall2sMap().put(
                    stringeeCall2.getCallId(), new Call2Wrapper(ClientWrapper.this, stringeeCall2));
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
            callInfoMap.put(
                    "customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
            map.put("body", callInfoMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onConnectionError(
            final StringeeClient stringeeClient, final StringeeError stringeeError) {
        Utils.post(() -> {
            Log.d(
                    TAG, "onConnectionError: " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage()
            );
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
            Log.d(
                    TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " +
                            stringeeChange.getChangeType()
            );
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
                objectMap = Utils.convertConversationToMap(
                        (Conversation) stringeeChange.getObject());
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
            Log.d(
                    TAG, "onReceiveChatRequest: " + chatRequest.getConvId() + " - from: " +
                            chatRequest.getCustomerId()
            );
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
            Log.d(
                    TAG, "onReceiveTransferChatRequest: " + chatRequest.getConvId() + " - from: " +
                            chatRequest.getCustomerId()
            );
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
            Log.d(
                    TAG, "onTimeoutAnswerChat: " + chatRequest.getConvId() + " - from: " +
                            chatRequest.getCustomerId()
            );
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
            Log.d(
                    TAG, "onConversationEnded: " + conversation.getId() + " - endedBy: " +
                            user.getUserId()
            );
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
     * Connects to Stringee with custom socket addresses.
     *
     * @param socketAddressList
     *         Custom server addresses.
     * @param token
     *         Access token.
     * @param result
     *         Flutter method result.
     */
    public void connect(
            final List<SocketAddress> socketAddressList, final String token, final Result result) {
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
     * Connects to Stringee with the default server configuration.
     *
     * @param token
     *         Access token.
     * @param result
     *         Flutter method result.
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

    /** Returns whether the native client is currently connected. */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Disconnects the wrapped Stringee client.
     *
     * @param result
     *         Flutter method result.
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
     * Enables or disables trusting all SSL certificates.
     *
     * @param trustAll
     *         True to trust all SSL certificates.
     * @param result
     *         Flutter method result.
     */
    public void setTrustAllSsl(final boolean trustAll, final Result result) {
        client.setTrustAllSsl(trustAll);
        Log.d(TAG, "setTrustAllSsl: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Registers a push notification token for this client.
     *
     * @param registrationToken
     *         Device push token.
     * @param result
     *         Flutter method result.
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

        client.registerPushToken(
                registrationToken, new StatusListener() {
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
                            Log.d(
                                    TAG, "registerPush: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Registers a push token and deletes tokens registered by the provided packages.
     *
     * @param registrationToken
     *         Device push token.
     * @param packageNames
     *         Package names whose existing tokens should be removed.
     * @param result
     *         Flutter method result.
     */
    public void registerPushAndDeleteOthers(
            final String registrationToken, final List<String> packageNames, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "registerPush: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.registerPushTokenAndDeleteOthers(
                registrationToken, packageNames, new StatusListener() {
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
                            Log.d(
                                    TAG, "registerPush: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Unregisters a push notification token for this client.
     *
     * @param registrationToken
     *         Device push token.
     * @param result
     *         Flutter method result.
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

        client.unregisterPushToken(
                registrationToken, new StatusListener() {
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
                            Log.d(
                                    TAG, "unregisterPush: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Sends a custom message to another Stringee user.
     *
     * @param toUserId
     *         Receiver user id.
     * @param data
     *         Custom message payload.
     * @param result
     *         Flutter method result.
     */
    public void sendCustomMessage(
            final String toUserId, final JSONObject data, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "sendCustomMessage: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.sendCustomMessage(
                toUserId, data, new StatusListener() {
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
                            Log.d(
                                    TAG, "sendCustomMessage: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Checks whether a call exists on the Stringee server.
     *
     * @param callId
     *         Call id to check.
     * @param result
     *         Flutter method result.
     */
    public void existCall(final String callId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "existCall: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(callId)) {
            Log.d(TAG, "existCall: false - -2 - CallId is invalid.");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "CallId is invalid.");
            result.success(map);
            return;
        }

        client.checkExitsCall(
                callId, new CallbackListener<Boolean>() {
                    @Override
                    public void onSuccess(final Boolean isExist) {
                        Utils.post(() -> {
                            Log.d(TAG, "existCall: success - " + isExist);
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", Boolean.TRUE.equals(isExist));
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        Utils.post(() -> {
                            Log.d(
                                    TAG, "existCall: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Creates a conversation with the provided participants and options.
     *
     * @param participants
     *         Conversation participants.
     * @param options
     *         Conversation creation options.
     * @param result
     *         Flutter method result.
     */
    public void createConversation(
            final List<User> participants, final ConversationOptions options, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createConversation(
                participants, options, new CallbackListener<Conversation>() {
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
                            Log.d(
                                    TAG, "createConversation: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets a conversation by id.
     *
     * @param convId
     *         Conversation id.
     * @param result
     *         Flutter method result.
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

        client.getConversationFromServer(
                convId, new CallbackListener<Conversation>() {
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
                            Log.d(
                                    TAG, "getConversationById: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets a one-to-one conversation by user id.
     *
     * @param userId
     *         User id.
     * @param result
     *         Flutter method result.
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

        client.getConversationByUserId(
                userId, new CallbackListener<Conversation>() {
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
                            Log.d(
                                    TAG,
                                    "getConversationByUserId: false - " + error.getCode() + " - " +
                                            error.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets locally cached conversations.
     *
     * @param oaId
     *         Optional OA id filter.
     * @param result
     *         Flutter method result.
     */
    public void getLocalConversations(final String oaId, final Result result) {
        client.getLocalConversations(
                client.getUserId(), oaId, new CallbackListener<List<Conversation>>() {
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
                            Log.d(
                                    TAG,
                                    "getLocalConversations: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );

    }

    /**
     * Gets the latest conversations from the server.
     *
     * @param count
     *         Maximum number of conversations.
     * @param oaId
     *         Optional OA id filter.
     * @param result
     *         Flutter method result.
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

        client.getLastConversations(
                count, oaId, new CallbackListener<List<Conversation>>() {
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
                            Log.d(
                                    TAG, "getLastConversation: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets conversations updated before the provided timestamp.
     *
     * @param updateAt
     *         Reference update timestamp.
     * @param count
     *         Maximum number of conversations.
     * @param oaId
     *         Optional OA id filter.
     * @param result
     *         Flutter method result.
     */
    public void getConversationsBefore(
            final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationsBefore: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsBefore(
                updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
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
                            Log.d(
                                    TAG,
                                    "getConversationsBefore: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets conversations updated after the provided timestamp.
     *
     * @param updateAt
     *         Reference update timestamp.
     * @param count
     *         Maximum number of conversations.
     * @param oaId
     *         Optional OA id filter.
     * @param result
     *         Flutter method result.
     */
    public void getConversationsAfter(
            final long updateAt, final int count, final String oaId, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "getConversationsAfter: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.getConversationsAfter(
                updateAt, count, oaId, new CallbackListener<List<Conversation>>() {
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
                            Log.d(
                                    TAG,
                                    "getConversationsAfter: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Clears the local Stringee database.
     *
     * @param result
     *         Flutter method result.
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
     * Blocks a Stringee user.
     *
     * @param userId
     *         User id to block.
     * @param result
     *         Flutter method result.
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

        client.blockUser(
                userId, new StatusListener() {
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
                            Log.d(
                                    TAG, "blockUser: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets the total unread conversation count.
     *
     * @param result
     *         Flutter method result.
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
                    Log.d(
                            TAG, "getTotalUnread: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage()
                    );
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
     * Gets a live chat profile by key.
     *
     * @param key
     *         Live chat key.
     * @param result
     *         Flutter method result.
     */
    public void getChatProfile(String key, final Result result) {
        client.getChatProfile(
                key, new CallbackListener<ChatProfile>() {
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
                            Log.d(
                                    TAG,
                                    "getChatProfile: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Gets a live chat token for a visitor.
     *
     * @param key
     *         Live chat key.
     * @param name
     *         Visitor name.
     * @param email
     *         Visitor email.
     * @param result
     *         Flutter method result.
     */
    public void getLiveChatToken(String key, String name, String email, final Result result) {
        client.getLiveChatToken(
                key, name, email, new CallbackListener<String>() {
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
                            Log.d(
                                    TAG,
                                    "getLiveChatToken: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Updates the current live chat visitor information.
     *
     * @param name
     *         Visitor name.
     * @param email
     *         Visitor email.
     * @param avatar
     *         Visitor avatar URL.
     * @param phone
     *         Visitor phone number.
     * @param result
     *         Flutter method result.
     */
    public void updateUserInfo(
            String name, String email, String avatar, String phone, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "updateUserInfo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.updateUser(
                name, email, avatar, phone, new StatusListener() {
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
                            Log.d(
                                    TAG,
                                    "updateUserInfo: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Creates a live chat conversation in a queue.
     *
     * @param queueId
     *         Queue id.
     * @param customData
     *         Optional custom conversation data.
     * @param result
     *         Flutter method result.
     */
    public void createLiveChatConversation(
            final String queueId, final String customData, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createLiveChatConversation: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createLiveChat(
                queueId, customData, new CallbackListener<Conversation>() {
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
                            Log.d(
                                    TAG,
                                    "convertConversationToMap: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Creates a live chat ticket.
     *
     * @param key
     *         Live chat key.
     * @param name
     *         Visitor name.
     * @param email
     *         Visitor email.
     * @param note
     *         Ticket note or description.
     * @param result
     *         Flutter method result.
     */
    public void createLiveChatTicket(
            String key, String name, String email, String note, final Result result) {
        if (!isConnected()) {
            Log.d(TAG, "createLiveChatTicket: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        client.createLiveChatTicket(
                key, name, email, note, new StatusListener() {
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
                            Log.d(
                                    TAG,
                                    "createLiveChatTicket: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }

    /**
     * Joins an Official Account conversation.
     *
     * @param convId conversation to join
     * @param result Flutter method result
     */
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

        client.joinOaConversation(
                convId, new StatusListener() {
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
                            Log.d(
                                    TAG, "joinOaConversation: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }
        );
    }
}
