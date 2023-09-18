package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.ChatEvent;
import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.ClientEvent;

import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.call.VideoQuality;
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.ChannelType;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.ChatRequest.State;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationFilter;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject.Type;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListener;
import com.stringee.messaging.listeners.LiveChatEventListener;
import com.stringee.messaging.listeners.UserTypingEventListener;
import com.stringee.network.tcpclient.StringeeCertificate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.flutter.plugin.common.MethodChannel.Result;

public class ClientWrapper implements StringeeConnectionListener, ChangeEventListener, LiveChatEventListener, UserTypingEventListener {
    private StringeeClient client;
    private StringeeManager stringeeManager;
    private ConversationManager conversationManager;
    private MessageManager messageManager;
    private ChatRequestManager chatRequestManager;
    private VideoConferenceManager videoConferenceManager;
    private String uuid;

    public ClientWrapper(final String uuid) {
        this.stringeeManager = StringeeManager.getInstance();
        this.uuid = uuid;
        this.conversationManager = new ConversationManager(this);
        this.messageManager = new MessageManager(this);
        this.chatRequestManager = new ChatRequestManager(this);
        this.videoConferenceManager = new VideoConferenceManager(this);
        this.client = new StringeeClient(stringeeManager.getContext());
        setListener();
    }

    public void setBaseAPIUrl(String baseAPIUrl) {
        this.client.setBaseAPIUrl(baseAPIUrl);
    }

    public void setStringeeXBaseUrl(String stringeeXBaseUrl) {
        this.client.setStringeeXBaseUrl(stringeeXBaseUrl);
    }

    private void setListener() {
        this.client.setConnectionListener(this);
        this.client.setChangeEventListener(this);
        this.client.setLiveChatEventListener(this);
        this.client.setUserTypingEventListener(this);
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
     */
    public CallWrapper callWrapper(final String from, final String to, final boolean isVideoCall, final String customData, final VideoQuality videoQuality) {
        StringeeCall call = new StringeeCall(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }
        call.setVideoQuality(videoQuality);
        CallWrapper callWrapper = new CallWrapper(this, call, false);
        stringeeManager.getCallsMap().put(call.getCallId(), callWrapper);
        return callWrapper;
    }

    public Call2Wrapper call2Wrapper(final String callId) {
        return stringeeManager.getCall2sMap().get(callId);
    }

    /**
     * Make a new call2
     */
    public Call2Wrapper call2Wrapper(final String from, final String to, final boolean isVideoCall, final String customData, final VideoQuality videoQuality) {
        StringeeCall2 call = new StringeeCall2(client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }
        call.setVideoQuality(videoQuality);
        Call2Wrapper call2Wrapper = new Call2Wrapper(this, call, false);
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onConnectionConnected: " + stringeeClient.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didConnect");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onConnectionDisconnected: " + stringeeClient.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didDisconnect");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onIncomingCall: " + stringeeCall.getCallId());
                stringeeManager.getCallsMap().put(stringeeCall.getCallId(), new CallWrapper(ClientWrapper.this, stringeeCall, true));
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "incomingCall");
                map.put("uuid", uuid);
                Map<String, Object> callInfoMap = new HashMap<>();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("callType", stringeeCall.getCallType().getValue());
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                int videoQuality = 0;
                if (stringeeCall.getVideoQuality() != null) {
                    VideoQuality quality = stringeeCall.getVideoQuality();
                    if (Objects.requireNonNull(quality) == VideoQuality.QUALITY_720P) {
                        videoQuality = 1;
                    } else if (quality == VideoQuality.QUALITY_1080P) {
                        videoQuality = 2;
                    }
                }
                callInfoMap.put("videoQuality", videoQuality);
                callInfoMap.put("isP2P", stringeeCall.isP2P());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onIncomingCall2: " + stringeeCall2.getCallId());
                stringeeManager.getCall2sMap().put(stringeeCall2.getCallId(), new Call2Wrapper(ClientWrapper.this, stringeeCall2, true));
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "incomingCall2");
                map.put("uuid", uuid);
                Map<String, Object> callInfoMap = new HashMap<>();
                callInfoMap.put("callId", stringeeCall2.getCallId());
                callInfoMap.put("from", stringeeCall2.getFrom());
                callInfoMap.put("to", stringeeCall2.getTo());
                callInfoMap.put("fromAlias", stringeeCall2.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall2.getToAlias());
                callInfoMap.put("isVideoCall", stringeeCall2.isVideoCall());
                callInfoMap.put("callType", stringeeCall2.getCallType().getValue());
                callInfoMap.put("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
                int videoQuality = 0;
                if (stringeeCall2.getVideoQuality() != null) {
                    VideoQuality quality = stringeeCall2.getVideoQuality();
                    if (Objects.requireNonNull(quality) == VideoQuality.QUALITY_720P) {
                        videoQuality = 1;
                    } else if (quality == VideoQuality.QUALITY_1080P) {
                        videoQuality = 2;
                    }
                }
                callInfoMap.put("videoQuality", videoQuality);
                map.put("body", callInfoMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onConnectionError: " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didFailWithError");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("code", stringeeError.getCode());
                bodyMap.put("message", stringeeError.getMessage());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onRequestNewToken: " + stringeeClient.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "requestAccessToken");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("userId", stringeeClient.getUserId());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onCustomMessage: " + from + " - " + jsonObject.toString());
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", ClientEvent.getValue());
                    map.put("event", "didReceiveCustomMessage");
                    map.put("uuid", uuid);
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("fromUserId", from);
                    bodyMap.put("message", Utils.convertJsonToMap(jsonObject));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTopicMessage(String s, JSONObject jsonObject) {

    }

    @Override
    public void onChangeEvent(final StringeeChange stringeeChange) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onChangeEvent: " + stringeeChange.getObjectType() + " - " + stringeeChange.getChangeType());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ChatEvent.getValue());
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
            }
        });
    }

    @Override
    public void onReceiveChatRequest(ChatRequest chatRequest) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onReceiveChatRequest: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didReceiveChatRequest");
                map.put("uuid", uuid);
                map.put("body", Utils.convertChatRequestToMap(chatRequest));
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onReceiveTransferChatRequest: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didReceiveTransferChatRequest");
                map.put("uuid", uuid);
                map.put("body", Utils.convertChatRequestToMap(chatRequest));
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onHandleOnAnotherDevice(ChatRequest chatRequest, State state) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onChatRequestHandleOnAnotherDevice: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "didChatRequestHandleOnAnotherDevice");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("chatRequest", Utils.convertChatRequestToMap(chatRequest));
                bodyMap.put("state", state.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onTimeoutAnswerChat(ChatRequest chatRequest) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onTimeoutAnswerChat: " + chatRequest.getConvId() + " - from: " + chatRequest.getCustomerId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "timeoutAnswerChat");
                map.put("uuid", uuid);
                map.put("body", Utils.convertChatRequestToMap(chatRequest));
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onTimeoutInQueue(Conversation conversation) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onTimeoutInQueue: " + conversation.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "timeoutInQueue");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("convId", conversation.getId());
                User user = client.getUser(client.getUserId());
                bodyMap.put("customerId", user.getUserId());
                bodyMap.put("customerName", user.getName());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onConversationEnded(Conversation conversation, User user) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onConversationEnded: " + conversation.getId() + " - endedBy: " + user.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "conversationEnded");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("convId", conversation.getId());
                bodyMap.put("endedby", user.getUserId());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onTyping(Conversation conversation, User user) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "userBeginTyping");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("convId", conversation.getId());
                bodyMap.put("userId", user.getUserId());
                bodyMap.put("displayName", user.getUserId());
                String userName = user.getName();
                if (!Utils.isStringEmpty(userName)) {
                    bodyMap.put("displayName", userName);
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onEndTyping(Conversation conversation, User user) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onEndTyping: " + conversation.getId() + " - endedBy: " + user.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", ClientEvent.getValue());
                map.put("event", "userEndTyping");
                map.put("uuid", uuid);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("convId", conversation.getId());
                bodyMap.put("userId", user.getUserId());
                bodyMap.put("displayName", user.getUserId());
                String userName = user.getName();
                if (!Utils.isStringEmpty(userName)) {
                    bodyMap.put("displayName", userName);
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    /**
     * Connect to Stringee server
     */
    public void connect(final List<SocketAddress> socketAddressList, final String token, final Result result) {
        if (!Utils.isListEmpty(socketAddressList)) {
            client.setHost(socketAddressList);
        }
        client.connect(token);
        Utils.sendSuccessResponse("connect", null, result);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect(final Result result) {
        client.disconnect();
        Utils.sendSuccessResponse("disconnect", null, result);
    }

    /**
     * Register push notification
     */
    public void registerPush(final String registrationToken, final Result result) {
        if (!Utils.isClientConnected(this, "registerPush", result)) {
            return;
        }

        client.registerPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("registerPush", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("registerPush", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Register push notification and delete another token of other packages
     */
    public void registerPushAndDeleteOthers(final String registrationToken, final List<String> packageNames, final Result result) {
        if (!Utils.isClientConnected(this, "registerPushTokenAndDeleteOthers", result)) {
            return;
        }

        client.registerPushTokenAndDeleteOthers(registrationToken, packageNames, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("registerPushTokenAndDeleteOthers", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("registerPush", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Unregister push notification
     */
    public void unregisterPush(final String registrationToken, final Result result) {
        if (!Utils.isClientConnected(this, "unregisterPush", result)) {
            return;
        }

        client.unregisterPushToken(registrationToken, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("unregisterPush", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("unregisterPush", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send a custom message
     */
    public void sendCustomMessage(final String toUserId, final JSONObject data, final Result result) {
        if (!Utils.isClientConnected(this, "sendCustomMessage", result)) {
            return;
        }

        client.sendCustomMessage(toUserId, data, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("sendCustomMessage", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendCustomMessage", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Create new conversation
     */
    public void createConversation(final List<User> participants, final ConversationOptions options, final Result result) {
        if (!Utils.isClientConnected(this, "createConversation", result)) {
            return;
        }

        client.createConversation(participants, options, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("createConversation", Utils.convertConversationToMap(conversation), result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("createConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get conversation by id
     */
    public void getConversationById(final String convId, final Result result) {
        if (!Utils.isClientConnected(this, "getConversationById", result)) {
            return;
        }

        client.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("getConversationById", Utils.convertConversationToMap(conversation), result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getConversationById", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get conversation by user id
     */
    public void getConversationByUserId(final String userId, final Result result) {
        if (!Utils.isClientConnected(this, "getConversationByUserId", result)) {
            return;
        }

        client.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("getConversationByUserId", Utils.convertConversationToMap(conversation), result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getConversationByUserId", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get local conversations
     */
    public void getLocalConversations(final String oaId, final Result result) {
        client.getLocalConversations(client.getUserId(), oaId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getLocalConversations", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLocalConversations", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get local conversations with channel type
     */
    public void getLocalConversationsByChannelType(final List<ChannelType> channelTypes, final boolean isEnded, final Result result) {
        client.getLocalConversationsByChannelType(client.getUserId(), isEnded, channelTypes, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getLocalConversationsByChannelType", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLocalConversationsByChannelType", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get last conversations
     */
    public void getLastConversation(final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getLastConversation", result)) {
            return;
        }

        client.getLastConversations(count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getLastConversation", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLastConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get last unread conversations
     */
    public void getLastUnreadConversations(final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getLastUnreadConversations", result)) {
            return;
        }

        client.getLastUnreadConversations(count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getLastUnreadConversations", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLastUnreadConversations", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get conversations update before '$updateAt'
     */
    public void getConversationsBefore(final long updateAt, final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getConversationsBefore", result)) {
            return;
        }

        client.getConversationsBefore(updateAt, count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getConversationsBefore", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getConversationsBefore", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get unread conversations update before '$updateAt'
     */
    public void getUnreadConversationsBefore(final long updateAt, final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getUnreadConversationsBefore", result)) {
            return;
        }

        client.getUnreadConversationsBefore(updateAt, count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getUnreadConversationsBefore", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getUnreadConversationsBefore", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get conversations update after '$updateAt'
     */
    public void getConversationsAfter(final long updateAt, final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getConversationsAfter", result)) {
            return;
        }

        client.getConversationsAfter(updateAt, count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getConversationsAfter", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getConversationsAfter", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get unread conversations update after '$updateAt'
     */
    public void getUnreadConversationsAfter(final long updateAt, final int count, final ConversationFilter conversationFilter, final Result result) {
        if (!Utils.isClientConnected(this, "getUnreadConversationsAfter", result)) {
            return;
        }

        client.getUnreadConversationsAfter(updateAt, count, conversationFilter, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(final List<Conversation> conversations) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < conversations.size(); i++) {
                            bodyArray.add(Utils.convertConversationToMap(conversations.get(i)));
                        }
                        Utils.sendSuccessResponse("getUnreadConversationsAfter", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getUnreadConversationsAfter", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get chat request
     */
    public void getChatRequest(final Result result) {
        if (!Utils.isClientConnected(this, "getChatRequest", result)) {
            return;
        }

        client.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(final List<ChatRequest> chatRequests) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> bodyArray = new ArrayList<>();
                        for (int i = 0; i < chatRequests.size(); i++) {
                            bodyArray.add(Utils.convertChatRequestToMap(chatRequests.get(i)));
                        }
                        Utils.sendSuccessResponse("getChatRequests", bodyArray, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getChatRequests", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Clear local database
     */
    public void clearDb(final Result result) {
        client.clearDb();
        Utils.sendSuccessResponse("clearDb", null, result);
    }

    /**
     * Block user
     */
    public void blockUser(final String userId, final Result result) {
        if (!Utils.isClientConnected(this, "blockUser", result)) {
            return;
        }

        client.blockUser(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("blockUser", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("blockUser", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Block user
     */
    public void preventAddingToGroup(final String convId, final Result result) {
        if (!Utils.isClientConnected(this, "preventAddingToGroup", result)) {
            return;
        }

        client.preventAddingToGroup(convId, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("preventAddingToGroup", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("preventAddingToGroup", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get total unread conversations
     */
    public void getTotalUnread(final Result result) {
        if (!Utils.isClientConnected(this, "getTotalUnread", result)) {
            return;
        }

        client.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(final Integer integer) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("getTotalUnread", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getTotalUnread", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get chat profile
     */
    public void getChatProfile(String key, final Result result) {
        client.getChatProfile(key, new CallbackListener<ChatProfile>() {
            @Override
            public void onSuccess(final ChatProfile chatProfile) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("getChatProfile", Utils.convertChatProfileToMap(chatProfile), result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getChatProfile", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get live chat token
     */
    public void getLiveChatToken(String key, String name, String email, final Result result) {
        client.getLiveChatToken(key, name, email, new CallbackListener<String>() {
            @Override
            public void onSuccess(final String token) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("getChatProfile", token, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getLiveChatToken", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Update user info
     */
    public void updateUserInfo(User user, final Result result) {
        if (!Utils.isClientConnected(this, "updateUserInfo", result)) {
            return;
        }

        client.updateUser(user, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("updateUserInfo", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("updateUserInfo", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Get user info
     */
    public void getUserInfo(List<String> userIds, final Result result) {
        if (!Utils.isClientConnected(this, "getUserInfo", result)) {
            return;
        }

        client.getUserInfo(userIds, new CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> userList = new ArrayList<>();
                        for (User user : users) {
                            userList.add(Utils.convertUserToMap(user));
                        }
                        Utils.sendSuccessResponse("getUserInfo", userList, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("getUserInfo", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Create live chat conversation
     */
    public void createLiveChatConversation(final String queueId, final String customData, final Result result) {
        if (!Utils.isClientConnected(this, "createLiveChatConversation", result)) {
            return;
        }

        client.createLiveChat(queueId, customData, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("createLiveChatConversation", Utils.convertConversationToMap(conversation), result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("convertConversationToMap", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Create live chat ticket
     */
    public void createLiveChatTicket(String key, String name, String email, String note, String phone, final Result result) {
        if (!Utils.isClientConnected(this, "createLiveChatTicket", result)) {
            return;
        }

        if (Utils.isStringEmpty(phone)) {
            phone = "";
        }

        client.createLiveChatTicket(key, name, email, note, phone, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("createLiveChatTicket", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("createLiveChatTicket", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void joinOaConversation(final String convId, final Result result) {
        if (!Utils.isClientConnected(this, "joinOaConversation", result)) {
            return;
        }

        client.joinOaConversation(convId, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("joinOaConversation", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("joinOaConversation", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Set trust all ssl
     */
    public void setTrustAllSsl(final boolean trustAll, final Result result) {
        client.setTrustAllSsl(trustAll);
        Utils.sendSuccessResponse("setTrustAllSsl", null, result);
    }

    /**
     * Enable sslSpinning with file certificate
     */
    public void enableSSLSpinning(final List<String> certificateNames, final Result result) {
        List<StringeeCertificate> certificates = new ArrayList<>();
        for (String certificationName : certificateNames) {
            int cerId = stringeeManager.getBinding().getActivity().getResources().getIdentifier(certificationName, "raw", stringeeManager.getPackageName());
            certificates.add(new StringeeCertificate(stringeeManager.getActivity(), cerId));
        }
        client.enableSSLSpinning(certificates);
        Utils.sendSuccessResponse("enableSSLSpinning", null, result);
    }

    /**
     * Enable sslSpinning with public key
     */
    public void enableSSLSpinningWithPublicKeys(final List<String> publicKeys, final Result result) {
        client.enableSSLSpinningWithPublicKeys(publicKeys);
        Utils.sendSuccessResponse("enableSSLSpinningWithPublicKeys", null, result);
    }
}