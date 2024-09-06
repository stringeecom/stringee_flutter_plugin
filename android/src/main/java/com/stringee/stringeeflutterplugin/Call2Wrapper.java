package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.MediaType;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class Call2Wrapper implements StringeeCall2.StringeeCallListener {
    private final ClientWrapper clientWrapper;
    private final StringeeCall2 call2;
    private final StringeeManager stringeeManager;
    private Result makeCallResult;
    private StringeeCall2.MediaState _mediaState;
    private boolean localStreamShowed;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private final boolean isIncomingCall;
    private String shareId = "";

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.stringeeManager = StringeeManager.getInstance();
        this.isIncomingCall = true;
    }

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call, Result result) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.makeCallResult = result;
        this.stringeeManager = StringeeManager.getInstance();
        this.isIncomingCall = false;
    }

    public void prepareCall() {
        stringeeManager.startAudioManager(stringeeManager.getContext(), (selectedAudioDevice, availableAudioDevices) -> Utils.post(() -> {
            List<AudioDevice> audioDeviceList = new ArrayList<>(availableAudioDevices);
            List<Integer> codeList = new ArrayList<>();
            for (int i = 0; i < audioDeviceList.size(); i++) {
                codeList.add(audioDeviceList.get(i).ordinal());
            }
            Map<String, Object> map = Utils.createEventMap("didChangeAudioDevice", clientWrapper.getId(), StringeeEventType.Call2Event);
            Logging.d("availableAudioDevices: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("code", selectedAudioDevice.ordinal());
            bodyMap.put("codeList", codeList);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        }));

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.setCallListener(this);
    }

    /**
     * Make a new call
     */
    public void makeCall() {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("makeCall");
            makeCallResult.success(map);
            return;
        }

        prepareCall();
        call2.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    /**
     * Init an answer
     *
     * @param result result
     */
    public void initAnswer(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("initAnswer");
            result.success(map);
            return;
        }

        prepareCall();
        call2.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("initAnswer");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("initAnswer", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Answer a call
     *
     * @param result result
     */
    public void answer(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("answer");
            result.success(map);
            return;
        }

        call2.answer(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        Map<String, Object> map = Utils.createSuccessMap("answer");
        result.success(map);
    }

    /**
     * Hang up call
     *
     * @param result result
     */
    public void hangup(final Result result) {
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
        Map<String, Object> map = Utils.createSuccessMap("hangup");
        result.success(map);
    }

    /**
     * Reject a call
     *
     * @param result result
     */
    public void reject(final Result result) {
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.reject(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
        Map<String, Object> map = Utils.createSuccessMap("reject");
        result.success(map);
    }

    /**
     * Send call info
     *
     * @param callInfo call info
     * @param result   result
     */
    public void sendCallInfo(final Map<String, Object> callInfo, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendCallInfo");
            result.success(map);
            return;
        }

        try {
            JSONObject jsonObject = Utils.convertMapToJson(callInfo);
            call2.sendCallInfo(jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    Utils.post(() -> {
                        Map<String, Object> map = Utils.createSuccessMap("sendCallInfo");
                        result.success(map);
                    });
                }

                @Override
                public void onError(final StringeeError stringeeError) {
                    Utils.post(() -> {
                        Map<String, Object> map = Utils.createErrorMap("sendCallInfo", stringeeError.getCode(), stringeeError.getMessage());
                        result.success(map);
                    });
                }
            });
        } catch (final JSONException e) {
            Logging.e(Call2Wrapper.class, e);
            Utils.post(() -> {
                Map<String, Object> map = Utils.createErrorMap("sendCallInfo", -2, e.getMessage());
                result.success(map);
            });
        }
    }

    /**
     * Mute or unmute
     *
     * @param mute   mute or unmute
     * @param result result
     */
    public void mute(final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("mute");
            result.success(map);
            return;
        }

        call2.mute(mute);
        Map<String, Object> map = Utils.createSuccessMap("mute");
        result.success(map);
    }

    /**
     * enable Video
     *
     * @param enable enable or disable
     * @param result result
     */
    public void enableVideo(final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("enableVideo");
            result.success(map);
            return;
        }

        call2.enableVideo(enable);
        Map<String, Object> map = Utils.createSuccessMap("enableVideo");
        result.success(map);
    }

    /**
     * Switch Camera
     *
     * @param result result
     */
    public void switchCamera(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("switchCamera");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("switchCamera");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("switchCamera", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Switch Camera
     *
     * @param result     result
     * @param cameraName cameraName
     */
    public void switchCamera(String cameraName, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("switchCamera");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("switchCamera");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("switchCamera", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        }, cameraName);
    }

    /**
     * Resume Video
     *
     * @param result result
     */
    public void resumeVideo(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("resumeVideo");
            result.success(map);
            return;
        }

        call2.resumeVideo();
        Map<String, Object> map = Utils.createSuccessMap("resumeVideo");
        result.success(map);
    }

    /**
     * Get call statistic
     *
     * @param result result
     */
    public void getCallStats(final Result result) {
        call2.getStats(stringeeCallStats -> Utils.post(() -> {
            Map<String, Object> map = Utils.createSuccessMap("getCallStats");
            Logging.d("callBytesReceived: " + stringeeCallStats.callBytesReceived + " - callPacketsLost: " + stringeeCallStats.callPacketsLost + " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived + " - timeStamp: " + stringeeCallStats.timeStamp);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("bytesReceived", stringeeCallStats.callBytesReceived);
            dataMap.put("packetsLost", stringeeCallStats.callPacketsLost);
            dataMap.put("packetsReceived", stringeeCallStats.callPacketsReceived);
            dataMap.put("timeStamp", stringeeCallStats.timeStamp);
            map.put("stats", dataMap);
            result.success(map);
        }));
    }

    /**
     * Set local stream is mirror or not
     *
     * @param result result
     */
    public void setMirror(final boolean isLocal, final boolean isMirror, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("setMirror");
            result.success(map);
            return;
        }

        if (isLocal) {
            call2.getLocalView2().setMirror(isMirror);
        } else {
            call2.getRemoteView2().setMirror(isMirror);
        }

        Map<String, Object> map = Utils.createSuccessMap("setMirror");
        result.success(map);
    }

    /**
     * Start capture screen
     *
     * @param result result
     */
    public void startCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("startCapture");
            result.success(map);
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);

        stringeeManager.getCaptureManager().createCapture(REQUEST_CODE, new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                    stringeeManager.getCaptureManager().getScreenCapture().createCapture(data, new CallbackListener<StringeeVideoTrack>() {
                        @Override
                        public void onSuccess(StringeeVideoTrack stringeeVideoTrack) {
                            call2.startCaptureScreen(stringeeManager.getCaptureManager().getScreenCapture(), new StatusListener() {
                                @Override
                                public void onSuccess() {
                                    Utils.post(() -> {
                                        Map<String, Object> map = Utils.createSuccessMap("startCapture");
                                        result.success(map);
                                    });
                                }

                                @Override
                                public void onError(StringeeError stringeeError) {
                                    super.onError(stringeeError);
                                    Utils.post(() -> {
                                        Map<String, Object> map = Utils.createErrorMap("startCapture", stringeeError.getCode(), stringeeError.getMessage());
                                        result.success(map);
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(StringeeError stringeeError) {
                            super.onError(stringeeError);
                            Utils.post(() -> {
                                Map<String, Object> map = Utils.createErrorMap("startCapture", stringeeError.getCode(), stringeeError.getMessage());
                                result.success(map);
                            });
                        }
                    });
                } else {
                    Utils.post(() -> {
                        Map<String, Object> map = Utils.createErrorMap("startCapture", -101, "Capture request is rejected");
                        result.success(map);
                    });
                }
                return false;
            }
        });
    }

    /**
     * Stop capture screen
     *
     * @param result result
     */
    public void stopCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("stopCapture");
            result.success(map);
            return;
        }

        call2.stopCaptureScreen(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("stopCapture");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("stopCapture", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }


    public TextureViewRenderer getLocalView() {
        return call2.getLocalView2();
    }

    public TextureViewRenderer getRemoteView() {
        return call2.getRemoteView2();
    }

    public void renderLocalView(ScalingType scalingType) {
        call2.renderLocalView2(scalingType);
    }

    public void renderRemoteView(ScalingType scalingType) {
        call2.renderRemoteView2(scalingType);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String s, final int i, final String s1) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING) {
                stringeeManager.getCall2sMap().put(stringeeCall.getCallId(), Call2Wrapper.this);
                Map<String, Object> map = Utils.createSuccessMap("makeCall");
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
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                Logging.d(callInfoMap.toString());
                map.put("callInfo", callInfoMap);
                if (makeCallResult != null) {
                    makeCallResult.success(map);
                    makeCallResult = null;
                }
            }

            Map<String, Object> map = Utils.createEventMap("didChangeSignalingState", clientWrapper.getId(), StringeeEventType.Call2Event);
            Logging.d("signalingState: " + signalingState);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", signalingState.getValue());
            map.put("body", bodyMap);
            if (isIncomingCall) {
                if (signalingState != SignalingState.ANSWERED) {
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            } else {
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onError(final StringeeCall2 stringeeCall, final int code, final String message) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createErrorMap("makeCall", code, message);
            if (makeCallResult != null) {
                makeCallResult.success(map);
                makeCallResult = null;
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String description) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didHandleOnAnotherDevice", clientWrapper.getId(), StringeeEventType.Call2Event);
            Logging.d("signalingState: " + signalingState + " - description: " + description);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", signalingState.getValue());
            bodyMap.put("description", description);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onMediaStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.MediaState mediaState) {
        Utils.post(() -> {
            _mediaState = mediaState;
            Map<String, Object> map = Utils.createEventMap("didChangeMediaState", clientWrapper.getId(), StringeeEventType.Call2Event);
            Logging.d("mediaState: " + mediaState);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", mediaState.getValue());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);

            if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                remoteStreamShowed = true;
                Map<String, Object> map1 = Utils.createEventMap("didReceiveRemoteStream", clientWrapper.getId(), StringeeEventType.Call2Event);
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
                Map<String, Object> map = Utils.createEventMap("didReceiveLocalStream", clientWrapper.getId(), StringeeEventType.Call2Event);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                map.put("body", bodyMap);
                if (localStreamShowed) {
                    Map<String, Object> localViewOptions = stringeeManager.getLocalViewOptions().get(stringeeCall.getCallId());
                    if (localViewOptions != null) {
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        Object isMirrorObj = localViewOptions.get("isMirror");
                        boolean isMirror = isMirrorObj instanceof Boolean && (boolean) isMirrorObj;
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                        if (localView != null) {
                            localView.removeAllViews();
                            if (getLocalView().getParent() != null) {
                                ((FrameLayout) getLocalView().getParent()).removeView(getLocalView());
                            }
                            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
                Map<String, Object> map = Utils.createEventMap("didReceiveRemoteStream", clientWrapper.getId(), StringeeEventType.Call2Event);
                if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                    remoteStreamShowed = true;
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                } else {
                    hasRemoteStream = true;
                }

                Map<String, Object> remoteViewOptions = stringeeManager.getRemoteViewOptions().get(stringeeCall.getCallId());
                if (remoteViewOptions != null) {
                    FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                    Object isMirrorObj = remoteViewOptions.get("isMirror");
                    boolean isMirror = isMirrorObj instanceof Boolean && (boolean) isMirrorObj;
                    ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");

                    if (remoteView != null) {
                        remoteView.removeAllViews();
                        if (getRemoteView().getParent() != null) {
                            ((FrameLayout) getRemoteView().getParent()).removeView(getRemoteView());
                        }
                        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
            if (stringeeVideoTrack.isLocal()) {
                shareId = Utils.createLocalId();
            }
            VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, stringeeVideoTrack.isLocal() ? shareId : "", true);
            stringeeManager.getTracksMap().put(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId(), videoTrackManager);

            Map<String, Object> map = Utils.createEventMap("didAddVideoTrack", clientWrapper.getId(), StringeeEventType.Call2Event);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didRemoveVideoTrack", clientWrapper.getId(), StringeeEventType.Call2Event);
            VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId());

            if (videoTrackManager != null) {
                StringeeVideoTrack videoTrack = videoTrackManager.getVideoTrack();
                if (videoTrack != null) {
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
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
                Map<String, Object> map = Utils.createEventMap("didReceiveCallInfo", clientWrapper.getId(), StringeeEventType.Call2Event);
                Logging.d("callInfo: " + jsonObject.toString());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Logging.e(Call2Wrapper.class, e);
            }
        });
    }

    @Override
    public void onTrackMediaStateChange(String s, MediaType mediaType, boolean b) {

    }

    @Override
    public void onLocalTrackAdded(StringeeCall2 stringeeCall2, StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onRemoteTrackAdded(StringeeCall2 stringeeCall2, StringeeVideoTrack stringeeVideoTrack) {

    }
}