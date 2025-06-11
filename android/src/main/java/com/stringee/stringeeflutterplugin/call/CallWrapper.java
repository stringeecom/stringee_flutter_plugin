package com.stringee.stringeeflutterplugin.call;

import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.call.StringeeCall.SignalingState;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.StringeeFlutterPlugin;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.StringeeEventType;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class CallWrapper extends StringeeCallWrapper implements StringeeCall.StringeeCallListener {
    private final StringeeCall call;
    private MediaState mediaState;

    public CallWrapper(ClientWrapper clientWrapper, StringeeCall call) {
        super(clientWrapper);
        this.call = call;
    }

    @Override
    public void prepareCall() {
        super.prepareCall();
        call.setCallListener(this);
    }

    @Override
    public void makeCall(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(
                    FlutterResult.error("makeCall", -1, "StringeeClient is disconnected").getMap());
            return;
        }
        super.makeCall(result);
        isIncomingCall = false;
        prepareCall();
        call.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    @Override
    public void initAnswer(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("initAnswer", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }
        isIncomingCall = true;
        prepareCall();
        call.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> result.success(FlutterResult.success("initAnswer").getMap()));
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> result.success(
                        FlutterResult.error("initAnswer", stringeeError.getCode(),
                                stringeeError.getMessage()).getMap()));
            }
        });
    }

    @Override
    public void answer(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(
                    FlutterResult.error("answer", -1, "StringeeClient is disconnected").getMap());
            return;
        }

        call.answer(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        result.success(FlutterResult.success("answer").getMap());
    }

    @Override
    public void hangup(final Result result) {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), null);
        if (result != null) {
            result.success(FlutterResult.success("hangup").getMap());
        }
    }

    @Override
    public void reject(final Result result) {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call.reject(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCallsMap().put(call.getCallId(), null);
        if (result != null) {
            result.success(FlutterResult.success("reject").getMap());
        }
    }

    @Override
    public void sendDTMF(final String dtmf, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(
                    FlutterResult.error("sendDTMF", -1, "StringeeClient is disconnected").getMap());
            return;
        }

        call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> result.success(FlutterResult.success("sendDTMF").getMap()));
            }

            @Override
            public void onError(final StringeeError error) {
                Utils.post(() -> result.success(
                        FlutterResult.error("sendDTMF", error.getCode(), error.getMessage())
                                .getMap()));
            }
        });
    }

    @Override
    public void sendCallInfo(final Map<String, Object> callInfo, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("sendCallInfo", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        try {
            JSONObject jsonObject = Utils.convertMapToJson(callInfo);
            call.sendCallInfo(jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    Utils.post(
                            () -> result.success(FlutterResult.success("sendCallInfo").getMap()));
                }

                @Override
                public void onError(final StringeeError stringeeError) {
                    Utils.post(() -> result.success(
                            FlutterResult.error("sendCallInfo", stringeeError.getCode(),
                                    stringeeError.getMessage()).getMap()));
                }
            });
        } catch (final JSONException e) {
            Utils.reportException(CallWrapper.class, e);
            Utils.post(() -> result.success(
                    FlutterResult.error("sendCallInfo", -101, e.getMessage()).getMap()));
        }
    }

    @Override
    public void mute(final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(
                    FlutterResult.error("mute", -1, "StringeeClient is disconnected").getMap());
            return;
        }

        call.mute(mute);
        result.success(FlutterResult.success("mute").getMap());
    }

    @Override
    public void enableVideo(final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("enableVideo", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call.enableVideo(enable);
        result.success(FlutterResult.success("enableVideo").getMap());
    }

    @Override
    public void switchCamera(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("switchCamera", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> result.success(FlutterResult.success("switchCamera").getMap()));
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> result.success(
                        FlutterResult.error("switchCamera", stringeeError.getCode(),
                                stringeeError.getMessage()).getMap()));
            }
        });
    }

    @Override
    public void switchCamera(String cameraName, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("switchCamera", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> result.success(FlutterResult.success("switchCamera").getMap()));
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> result.success(
                        FlutterResult.error("switchCamera", stringeeError.getCode(),
                                stringeeError.getMessage()).getMap()));
            }
        }, cameraName);
    }

    @Override
    public void resumeVideo(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("resumeVideo", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call.resumeVideo();
        result.success(FlutterResult.success("resumeVideo").getMap());
    }

    @Override
    public void getCallStats(final Result result) {
        call.getStats(stringeeCallStats -> Utils.post(() -> {
            Log.d(Constants.TAG,
                    "getCallStats: callBytesReceived: " + stringeeCallStats.callBytesReceived +
                            " - callPacketsLost: " + stringeeCallStats.callPacketsLost +
                            " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived +
                            " - timeStamp: " + stringeeCallStats.timeStamp);
            FlutterResult flutterResult = FlutterResult.success("getCallStats");
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("bytesReceived", stringeeCallStats.callBytesReceived);
            dataMap.put("packetsLost", stringeeCallStats.callPacketsLost);
            dataMap.put("packetsReceived", stringeeCallStats.callPacketsReceived);
            dataMap.put("timeStamp", stringeeCallStats.timeStamp);
            flutterResult.put("stats", dataMap);
            result.success(flutterResult.getMap());
        }));
    }

    @Override
    public void setMirror(final boolean isLocal, final boolean isMirror, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("setMirror", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        if (isLocal) {
            call.getLocalView2().setMirror(isMirror);
        } else {
            call.getRemoteView2().setMirror(isMirror);
        }

        result.success(FlutterResult.success("setMirror").getMap());
    }

    @Override
    public void startCapture(Result result) {

    }

    @Override
    public void stopCapture(Result result) {

    }

    @Override
    public TextureViewRenderer getLocalView() {
        return call.getLocalView2();
    }

    @Override
    public TextureViewRenderer getRemoteView() {
        return call.getRemoteView2();
    }

    @Override
    public void renderLocalView(ScalingType scalingType) {
        call.renderLocalView2(scalingType);
    }

    @Override
    public void renderRemoteView(ScalingType scalingType) {
        call.renderRemoteView2(scalingType);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall stringeeCall,
                                       final StringeeCall.SignalingState signalingState,
                                       final String s, final int i, final String s1) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING ||
                    signalingState == SignalingState.RINGING) {
                Log.d(Constants.TAG, "makeCall: success");
                StringeeManager.getInstance()
                        .getCallsMap()
                        .put(stringeeCall.getCallId(), CallWrapper.this);
                FlutterResult flutterResult = FlutterResult.success("makeCall");
                Map<String, Object> callInfoMap = new HashMap<>();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                int callType = 0;
                if (!stringeeCall.getFrom().equals(clientWrapper.getClient().getUserId())) {
                    callType = 1;
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = 2;
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = 3;
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer",
                        stringeeCall.getCustomDataFromYourServer());
                flutterResult.put("callInfo", callInfoMap);
                if (makeCallResult != null) {
                    makeCallResult.success(flutterResult.getMap());
                    makeCallResult = null;
                }
            }

            if (isIncomingCall) {
                if (signalingState != SignalingState.ANSWERED) {
                    Log.d(Constants.TAG, "onSignalingStateChange: " + signalingState);
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                    map.put("event", "didChangeSignalingState");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    bodyMap.put("code", signalingState.getValue());
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            } else {
                Log.d(Constants.TAG, "onSignalingStateChange: " + signalingState);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                map.put("event", "didChangeSignalingState");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onError(final StringeeCall stringeeCall, final int code, final String message) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onError: code: " + code + " -message: " + message);
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", code);
            map.put("message", message);
            if (makeCallResult != null) {
                makeCallResult.success(map);
                makeCallResult = null;
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall stringeeCall,
                                         final StringeeCall.SignalingState signalingState,
                                         final String description) {
        Utils.post(() -> {
            Log.d(Constants.TAG,
                    "onHandledOnAnotherDevice:" + "\nsignalingState: " + signalingState +
                            " - description: " + description);
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
            map.put("event", "didHandleOnAnotherDevice");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", signalingState.getValue());
            bodyMap.put("description", description);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onMediaStateChange(final StringeeCall stringeeCall,
                                   final StringeeCall.MediaState mediaState) {
        Utils.post(() -> {
            this.mediaState = mediaState;
            Log.d(Constants.TAG, "onMediaStateChange: " + mediaState);
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
            map.put("event", "didChangeMediaState");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", mediaState.getValue());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);

            if (this.mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed &&
                    stringeeCall.isVideoCall()) {
                remoteStreamShowed = true;
                Map<String, Object> map1 = new HashMap<>();
                map1.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                map1.put("event", "didReceiveRemoteStream");
                map1.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap1 = new HashMap<>();
                bodyMap1.put("callId", stringeeCall.getCallId());
                map1.put("body", bodyMap1);
                StringeeFlutterPlugin.eventSink.success(map1);
            }
        });
    }

    @Override
    public void onLocalStream(final StringeeCall stringeeCall) {
        Utils.post(() -> {
            if (stringeeCall.isVideoCall()) {
                Log.d(Constants.TAG, "onLocalStream");
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                map.put("event", "didReceiveLocalStream");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                map.put("body", bodyMap);
                if (localStreamShowed) {
                    Map<String, Object> localViewOptions = StringeeManager.getInstance()
                            .getLocalViewOptions()
                            .get(stringeeCall.getCallId());
                    if (localViewOptions != null) {
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        boolean isMirror = Boolean.TRUE.equals(localViewOptions.get("isMirror"));
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");
                        if (localView != null) {
                            localView.removeAllViews();
                            if (getLocalView().getParent() != null) {
                                ((FrameLayout) getLocalView().getParent()).removeView(
                                        getLocalView());
                            }
                            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER;
                            localView.addView(getLocalView(), layoutParams);
                            renderLocalView(scalingType);
                            getLocalView().setMirror(isMirror);
                        }
                    }
                }
                localStreamShowed = true;
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onRemoteStream(final StringeeCall stringeeCall) {
        Utils.post(() -> {
            if (stringeeCall.isVideoCall()) {
                Log.d(Constants.TAG, "onRemoteStream");
                if (mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                    remoteStreamShowed = true;
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                    map.put("event", "didReceiveRemoteStream");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                } else {
                    hasRemoteStream = true;
                }

                Map<String, Object> remoteViewOptions = StringeeManager.getInstance()
                        .getRemoteViewOptions()
                        .get(stringeeCall.getCallId());
                if (remoteViewOptions != null) {
                    FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                    boolean isMirror = Boolean.TRUE.equals(remoteViewOptions.get("isMirror"));
                    ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");
                    if (remoteView != null) {
                        remoteView.removeAllViews();
                        if (getRemoteView().getParent() != null) {
                            ((FrameLayout) getRemoteView().getParent()).removeView(getRemoteView());
                        }
                        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER;
                        remoteView.addView(getRemoteView(), layoutParams);
                        renderRemoteView(scalingType);
                        getRemoteView().setMirror(isMirror);
                    }
                }
            }
        });
    }

    @Override
    public void onCallInfo(final StringeeCall stringeeCall, final JSONObject jsonObject) {
        Utils.post(() -> {
            try {
                Log.d(Constants.TAG, "onCallInfo: " + jsonObject.toString());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL_EVENT.getValue());
                map.put("event", "didReceiveCallInfo");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Utils.reportException(CallWrapper.class, e);
            }
        });
    }
}
