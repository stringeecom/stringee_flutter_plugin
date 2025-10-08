package com.stringee.stringeeflutterplugin.call;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.StringeeFlutterPlugin;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.StringeeEventType;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.conference.ConferenceUtils;
import com.stringee.stringeeflutterplugin.conference.VideoTrackManager;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.MediaType;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.flutter.plugin.common.MethodChannel.Result;

public class Call2Wrapper extends StringeeCallWrapper implements StringeeCall2.StringeeCallListener {
    private final StringeeCall2 call2;
    private StringeeCall2.MediaState mediaState;
    private String shareId = "";

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call2) {
        super(clientWrapper);
        this.call2 = call2;
    }

    @Override
    public void prepareCall() {
        super.prepareCall();
        call2.setCallListener(this);
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
        call2.makeCall(new StatusListener() {
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
        call2.ringing(new StatusListener() {
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

        call2.answer(new StatusListener() {
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

        call2.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCallsMap().put(call2.getCallId(), null);
        if (result != null) {
            result.success(FlutterResult.success("hangup").getMap());
        }
    }

    @Override
    public void reject(final Result result) {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.reject(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCallsMap().put(call2.getCallId(), null);
        if (result != null) {
            result.success(FlutterResult.success("reject").getMap());
        }
    }

    @Override
    public void sendDTMF(String dtmf, Result result) {

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
            call2.sendCallInfo(jsonObject, new StatusListener() {
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
            Utils.reportException(Call2Wrapper.class, e);
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

        call2.mute(mute);
        result.success(FlutterResult.success("mute").getMap());
    }

    @Override
    public void enableVideo(final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("enableVideo", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call2.enableVideo(enable);
        result.success(FlutterResult.success("enableVideo").getMap());
    }

    @Override
    public void switchCamera(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("switchCamera", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call2.switchCamera(new StatusListener() {
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

        call2.switchCamera(new StatusListener() {
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

        call2.resumeVideo();
        result.success(FlutterResult.success("resumeVideo").getMap());
    }

    @Override
    public void getCallStats(final Result result) {
        call2.getStats(stringeeCallStats -> Utils.post(() -> {
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
            call2.getLocalView2().setMirror(isMirror);
        } else {
            call2.getRemoteView2().setMirror(isMirror);
        }
        result.success(FlutterResult.success("setMirror").getMap());
    }

    @Override
    public void startCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("startCapture", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);

        StringeeManager.getInstance()
                .getCaptureManager()
                .getActivityResult((requestCode, resultCode, data) -> {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        StringeeManager.getInstance()
                                .getCaptureManager()
                                .getScreenCapture()
                                .createCapture(data, new CallbackListener<StringeeVideoTrack>() {
                                    @Override
                                    public void onSuccess(StringeeVideoTrack var1) {

                                    }
                                });
                    }
                    return false;
                });

        call2.startCaptureScreen(
                StringeeManager.getInstance().getCaptureManager().getScreenCapture(),
                new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> result.success(
                                FlutterResult.success("startCapture").getMap()));
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> result.success(
                                FlutterResult.error("startCapture", stringeeError.getCode(),
                                        stringeeError.getMessage()).getMap()));
                    }
                });
    }

    @Override
    public void stopCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            result.success(FlutterResult.error("stopCapture", -1, "StringeeClient is disconnected")
                    .getMap());
            return;
        }

        call2.stopCaptureScreen(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> result.success(FlutterResult.success("stopCapture").getMap()));
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> result.success(
                        FlutterResult.error("stopCapture", stringeeError.getCode(),
                                stringeeError.getMessage()).getMap()));
            }
        });
    }

    @Override
    public TextureViewRenderer getLocalView() {
        return call2.getLocalView2();
    }

    @Override
    public TextureViewRenderer getRemoteView() {
        return call2.getRemoteView2();
    }

    @Override
    public void renderLocalView(ScalingType scalingType) {
        call2.renderLocalView2(scalingType);
    }

    @Override
    public void renderRemoteView(ScalingType scalingType) {
        call2.renderRemoteView2(scalingType);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall2 stringeeCall,
                                       final StringeeCall2.SignalingState signalingState,
                                       final String s, final int i, final String s1) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING) {
                Log.d(Constants.TAG, "makeCall: success");
                StringeeManager.getInstance()
                        .getCallsMap()
                        .put(stringeeCall.getCallId(), Call2Wrapper.this);
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
                    Log.d(Constants.TAG, "onSignalingStateChange2: " + signalingState);
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
                    map.put("event", "didChangeSignalingState");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    bodyMap.put("code", signalingState.getValue());
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            } else {
                Log.d(Constants.TAG, "onSignalingStateChange2: " + signalingState);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
    public void onError(final StringeeCall2 stringeeCall, final int code, final String message) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "onError2: code: " + code + " -message: " + message);
            if (makeCallResult != null) {
                makeCallResult.success(FlutterResult.error("makeCall", code, message).getMap());
                makeCallResult = null;
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall,
                                         final StringeeCall2.SignalingState signalingState,
                                         final String description) {
        Utils.post(() -> {
            Log.d(Constants.TAG,
                    "onHandledOnAnotherDevice2:" + "\nsignalingState: " + signalingState +
                            " - description: " + description);
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
    public void onMediaStateChange(final StringeeCall2 stringeeCall,
                                   final StringeeCall2.MediaState mediaState) {
        Utils.post(() -> {
            this.mediaState = mediaState;
            Log.d(Constants.TAG, "onMediaStateChange2: " + mediaState);
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
                map1.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
    public void onLocalStream(final StringeeCall2 stringeeCall) {
        Utils.post(() -> {
            if (stringeeCall.isVideoCall()) {
                Log.d(Constants.TAG, "onLocalStream2");
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
    public void onRemoteStream(final StringeeCall2 stringeeCall) {
        Utils.post(() -> {
            if (stringeeCall.isVideoCall()) {
                Log.d(Constants.TAG, "onRemoteStream2");
                if (mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                    remoteStreamShowed = true;
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
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
    public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "didAddVideoTrack");
            if (stringeeVideoTrack.isLocal()) {
                shareId = ConferenceUtils.createLocalId();
            }
            VideoTrackManager videoTrackManager =
                    new VideoTrackManager(clientWrapper, stringeeVideoTrack,
                            stringeeVideoTrack.isLocal() ? shareId : "", true);
            StringeeManager.getInstance()
                    .getTracksMap()
                    .put(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId(),
                            videoTrackManager);

            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
            map.put("event", "didAddVideoTrack");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("videoTrack", ConferenceUtils.convertVideoTrackToMap(videoTrackManager));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "didRemoveVideoTrack");
            VideoTrackManager videoTrackManager = StringeeManager.getInstance()
                    .getTracksMap()
                    .get(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId());

            if (videoTrackManager != null) {
                StringeeVideoTrack videoTrack = videoTrackManager.getVideoTrack();
                if (videoTrack != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
                    map.put("event", "didRemoveVideoTrack");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("videoTrack",
                            ConferenceUtils.convertVideoTrackToMap(videoTrackManager));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onCallInfo(final StringeeCall2 stringeeCall, final JSONObject jsonObject) {
        Utils.post(() -> {
            try {
                Log.d(Constants.TAG, "onCallInfo2: " + jsonObject.toString());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.CALL2_EVENT.getValue());
                map.put("event", "didReceiveCallInfo");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Utils.reportException(Call2Wrapper.class, e);
            }
        });
    }

    @Override
    public void onTrackMediaStateChange(String s, MediaType mediaType, boolean b) {

    }

    @Override
    public void onLocalTrackAdded(StringeeCall2 stringeeCall2,
                                  StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onRemoteTrackAdded(StringeeCall2 stringeeCall2,
                                   StringeeVideoTrack stringeeVideoTrack) {

    }
}
