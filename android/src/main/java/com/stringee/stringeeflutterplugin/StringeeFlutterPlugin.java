package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.common.StringeeConstant;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * StringeeFlutterPlugin
 */
public class StringeeFlutterPlugin implements MethodCallHandler, EventChannel.StreamHandler, StringeeConnectionListener, StringeeCall.StringeeCallListener {

    private StringeeClient client;
    private Registrar registrar;
    private static StringeeManager stringeeManager;
    private Result makeCallResult;
    private static EventChannel.EventSink mEventSink;
    private Handler handler;

    public StringeeFlutterPlugin(Registrar registrar) {
        this.registrar = registrar;
        stringeeManager = StringeeManager.getInstance();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        MethodChannel channel = new MethodChannel(registrar.messenger(), "com.stringee.flutter.methodchannel");
        channel.setMethodCallHandler(new StringeeFlutterPlugin(registrar));

        EventChannel eventChannel = new EventChannel(registrar.messenger(), "com.stringee.flutter.eventchannel");
        eventChannel.setStreamHandler(new StringeeFlutterPlugin(registrar));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("connect")) {
            connect(registrar.context(), (String) call.arguments);
        } else if (call.method.equals("disconnect")) {
            disconnect();
        } else if (call.method.equals("registerPush")) {
            registerPush((String) call.argument("deviceToken"), result);
        } else if (call.method.equals("unregisterPush")) {
            unregisterPush((String) call.arguments, result);
        } else if (call.method.equals("sendCustomMessage")) {
            try {
                sendCustomMessage((String) call.argument("toUserId"), convertMapToJson((Map) call.argument("message")), result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (call.method.equals("makeCall")) {
            String from = call.argument("from");
            String to = call.argument("to");
            boolean isVideoCall = false;
            if (call.hasArgument("isVideoCall")) {
                isVideoCall = call.argument("isVideoCall");
            }
            String customData = null;
            if (call.hasArgument("customData")) {
                customData = call.argument("customData");
            }
            String resolution = null;
            if (call.hasArgument("videoResolution")) {
                resolution = call.argument("videoResolution");
            }
            makeCall(from, to, isVideoCall, customData, resolution, result);
        } else if (call.method.equals("initAnswer")) {
            initAnswer((String) call.arguments, result);
        } else if (call.method.equals("answer")) {
            answer((String) call.arguments, result);
        } else if (call.method.equals("hangup")) {
            hangup((String) call.arguments, result);
        } else if (call.method.equals("reject")) {
            reject((String) call.arguments, result);
        } else if (call.method.equals("sendDtmf")) {
            sendDtmf((String) call.argument("callId"), (String) call.argument("dtmf"), result);
        } else if (call.method.equals("sendCallInfo")) {
            sendCallInfo((String) call.argument("callId"), (Map) call.argument("callInfo"), result);
        } else if (call.method.equals("getCallStats")) {
            getCallStats((String) call.arguments, result);
        } else if (call.method.equals("mute")) {
            mute((String) call.argument("callId"), (Boolean) call.argument("mute"), result);
        } else if (call.method.equals("setSpeakerphoneOn")) {
            setSpeakerphoneOn((String) call.argument("callId"), (Boolean) call.argument("speaker"), result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        mEventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {

    }

    /**
     * Connect to Stringee server
     *
     * @param context
     * @param token
     */
    public void connect(Context context, final String token) {
        client = stringeeManager.getClient();
        if (client == null) {
            client = new StringeeClient(context);
            stringeeManager.setClient(client);
        }
        client.setConnectionListener(this);
        client.connect(token);
    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }

    /**
     * Register push notification
     *
     * @param registrationToken
     */
    public void registerPush(String registrationToken, final Result result) {
        if (client != null) {
            client.registerPushToken(registrationToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    handler.post(new Runnable() {
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
                    handler.post(new Runnable() {
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
    }

    /**
     * Unregister push notification
     *
     * @param registrationToken
     */
    public void unregisterPush(String registrationToken, final Result result) {
        if (client != null) {
            client.unregisterPushToken(registrationToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    handler.post(new Runnable() {
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
                    handler.post(new Runnable() {
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
    }

    /**
     * Send a custom message
     *
     * @param toUserId
     * @param jsonObject
     */
    public void sendCustomMessage(String toUserId, JSONObject jsonObject, final Result result) {
        if (client != null) {
            client.sendCustomMessage(toUserId, jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    handler.post(new Runnable() {
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
                    handler.post(new Runnable() {
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
    }

    /**
     * Make a call
     *
     * @param from
     * @param to
     * @param isVideoCall
     * @param customData
     * @param videoResolution
     */
    public void makeCall(String from, String to, boolean isVideoCall, String customData, String videoResolution, Result result) {
        StringeeCall stringeeCall = new StringeeCall(registrar.context(), client, from, to);
        stringeeCall.setVideoCall(isVideoCall);
        if (customData != null) {
            stringeeCall.setCustom(customData);
        }
        if (videoResolution != null) {
            if (videoResolution.equalsIgnoreCase("NORMAL")) {
                stringeeCall.setQuality(StringeeConstant.QUALITY_NORMAL);
            } else if (videoResolution.equalsIgnoreCase("HD")) {
                stringeeCall.setQuality(StringeeConstant.QUALITY_HD);
            }
        }
        stringeeCall.setCallListener(this);
        makeCallResult = result;
        stringeeCall.makeCall();
    }

    private JSONObject convertMapToJson(Map map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    /**
     * Init an answer
     *
     * @param callId
     */
    public void initAnswer(String callId, Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        call.setCallListener(this);
        call.initAnswer(registrar.context(), client);
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Answer a call
     *
     * @param callId
     * @param result
     */
    public void answer(String callId, Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        call.answer();
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * End call
     *
     * @param callId
     * @param result
     */
    public void hangup(String callId, Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        call.hangup();
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Reject a call
     *
     * @param callId
     * @param result
     */
    public void reject(String callId, Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        call.reject();
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Send a DTMF
     *
     * @param callId
     * @param dtmf
     * @param result
     */
    public void sendDtmf(String callId, String dtmf, final Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }
        call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
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
                handler.post(new Runnable() {
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
     * Send call info
     *
     * @param callId
     * @param callInfo
     * @param result
     */
    public void sendCallInfo(String callId, Map callInfo, Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = convertMapToJson(callInfo);
            call.sendCallInfo(jsonObject);
            Map map = new HashMap();
            map.put("status", true);
            map.put("code", 0);
            map.put("message", "Success");
            result.success(map);
        } catch (JSONException e) {
            e.printStackTrace();
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "The call info format is invalid.");
            result.success(map);
        }
    }

    /**
     * Mute or unmute
     *
     * @param callId
     * @param mute
     * @param result
     */
    public void mute(String callId, boolean mute, Result result) {
        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }
        call.mute(mute);
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Set speaker on/off
     *
     * @param callId
     * @param on
     * @param result
     */
    public void setSpeakerphoneOn(String callId, boolean on, Result result) {
        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }
        call.setSpeakerphoneOn(on);
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Get call statistic
     *
     * @param callId
     * @param result
     */
    public void getCallStats(String callId, final Result result) {
        if (client == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or connected.");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "The call id is invalid.");
            result.success(map);
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "The call is not found.");
            result.success(map);
            return;
        }

        call.getStats(new StringeeCall.CallStatsListener() {
            @Override
            public void onCallStats(StringeeCall.StringeeCallStats stringeeCallStats) {
                Map map = new HashMap();
                map.put("status", true);
                map.put("code", 0);
                map.put("message", "Success");
                Map dataMap = new HashMap();
                dataMap.put("bytesReceived", stringeeCallStats.callBytesReceived);
                dataMap.put("packetsLost", stringeeCallStats.callPacketsLost);
                dataMap.put("packetsReceived", stringeeCallStats.callPacketsReceived);
                dataMap.put("timeStamp", stringeeCallStats.timeStamp);
                map.put("stats", dataMap);
                result.success(map);
            }
        });
    }

    private static Map convertJsonToMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = convertJsonToMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = convertJsonToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    @Override
    public void onConnectionConnected(final StringeeClient stringeeClient, final boolean b) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didConnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(String.valueOf(stringeeClient.getProjectId())));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didDisconnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                stringeeManager.getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
                Map map = new HashMap();
                map.put("event", "incomingCall");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                int callType = 0;
                if (!stringeeCall.getFrom().equals(client.getUserId())) {
                    callType = 1;
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = 2;
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = 3;
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didFailWithError");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("code", stringeeError.getCode());
                bodyMap.put("message", stringeeError.getMessage());
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "requestAccessToken");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onCustomMessage(final String s, final JSONObject jsonObject) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didReceiveCustomMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", s);
                bodyMap.put("message", jsonObject.toString());
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onTopicMessage(String from, JSONObject msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onSignalingStateChange(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, String s, int i, String s1) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (signalingState == StringeeCall.SignalingState.CALLING) {
                    stringeeManager.getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    Map callInfoMap = new HashMap();
                    callInfoMap.put("callId", stringeeCall.getCallId());
                    callInfoMap.put("from", stringeeCall.getFrom());
                    callInfoMap.put("to", stringeeCall.getTo());
                    callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                    callInfoMap.put("toAlias", stringeeCall.getToAlias());
                    int callType = 0;
                    if (!stringeeCall.getFrom().equals(client.getUserId())) {
                        callType = 1;
                    }
                    if (stringeeCall.isAppToPhoneCall()) {
                        callType = 2;
                    } else if (stringeeCall.isPhoneToAppCall()) {
                        callType = 3;
                    }
                    callInfoMap.put("callType", callType);
                    callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                    callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                    map.put("callInfo", callInfoMap);
                    makeCallResult.success(map);
                }

                Map map = new HashMap();
                map.put("event", "didChangeSignalingState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onError(StringeeCall stringeeCall, final int code, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", code);
                map.put("message", message);
                makeCallResult.success(map);
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didHandleOnAnotherDevice");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                bodyMap.put("description", s);
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onMediaStateChange(final StringeeCall stringeeCall, final StringeeCall.MediaState mediaState) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didChangeMediaState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }

    @Override
    public void onLocalStream(StringeeCall stringeeCall) {

    }

    @Override
    public void onRemoteStream(StringeeCall stringeeCall) {

    }

    @Override
    public void onCallInfo(final StringeeCall stringeeCall, final JSONObject jsonObject) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map map = new HashMap();
                map.put("event", "didReceiveCallInfo");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                try {
                    bodyMap.put("info", convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                mEventSink.success(map);
            }
        });
    }
}
