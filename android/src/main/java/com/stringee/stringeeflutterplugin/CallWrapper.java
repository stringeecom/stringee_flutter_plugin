package com.stringee.stringeeflutterplugin;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.call.StringeeCall.SignalingState;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class CallWrapper implements StringeeCall.StringeeCallListener {
    private final ClientWrapper clientWrapper;
    private final StringeeCall call;
    private final StringeeManager stringeeManager;
    private Result makeCallResult;
    private MediaState _mediaState;
    private boolean localStreamShowed;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private final boolean isIncomingCall;

    public CallWrapper(ClientWrapper clientWrapper, StringeeCall call) {
        this.clientWrapper = clientWrapper;
        this.call = call;
        this.stringeeManager = StringeeManager.getInstance();
        this.isIncomingCall = true;
    }

    public CallWrapper(ClientWrapper client, StringeeCall call, Result result) {
        this.clientWrapper = client;
        this.call = call;
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
            Map<String, Object> map = Utils.createEventMap("didChangeAudioDevice", clientWrapper.getId(), StringeeManager.StringeeEventType.CallEvent);
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

        call.setCallListener(this);
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
        call.makeCall(new StatusListener() {
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
        call.ringing(new StatusListener() {
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

        call.answer(new StatusListener() {
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

        call.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        stringeeManager.getCallsMap().put(call.getCallId(), null);
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

        call.reject(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        stringeeManager.getCallsMap().put(call.getCallId(), null);
        Map<String, Object> map = Utils.createSuccessMap("reject");
        result.success(map);
    }

    /**
     * Send a DTMF
     *
     * @param dtmf   dtmf
     * @param result result
     */
    public void sendDTMF(final String dtmf, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendDTMF");
            result.success(map);
            return;
        }

        call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("sendDtmf");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("sendDtmf", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Send call info
     *
     * @param callInfo callInfo
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
            call.sendCallInfo(jsonObject, new StatusListener() {
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
            Logging.e(CallWrapper.class, e);
            Utils.post(() -> {
                Map<String, Object> map = Utils.createErrorMap("sendCallInfo", -2, e.getMessage());
                result.success(map);
            });
        }
    }

    /**
     * Mute or unmute
     *
     * @param mute   mute
     * @param result result
     */
    public void mute(final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("mute");
            result.success(map);
            return;
        }

        call.mute(mute);
        Map<String, Object> map = Utils.createSuccessMap("mute");
        result.success(map);
    }

    /**
     * enable Video
     *
     * @param enable enable
     * @param result result
     */
    public void enableVideo(final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("enableVideo");
            result.success(map);
            return;
        }

        call.enableVideo(enable);
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

        call.switchCamera(new StatusListener() {
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

        call.switchCamera(new StatusListener() {
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

        call.resumeVideo();
        Map<String, Object> map = Utils.createSuccessMap("resumeVideo");
        result.success(map);
    }

    /**
     * Get call statistic
     *
     * @param result result
     */
    public void getCallStats(final Result result) {
        call.getStats(stringeeCallStats -> Utils.post(() -> {
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
            call.getLocalView2().setMirror(isMirror);
        } else {
            call.getRemoteView2().setMirror(isMirror);
        }

        Map<String, Object> map = Utils.createSuccessMap("setMirror");
        result.success(map);
    }

    public TextureViewRenderer getLocalView() {
        return call.getLocalView2();
    }

    public TextureViewRenderer getRemoteView() {
        return call.getRemoteView2();
    }

    public void renderLocalView(ScalingType scalingType) {
        call.renderLocalView2(scalingType);
    }

    public void renderRemoteView(ScalingType scalingType) {
        call.renderRemoteView2(scalingType);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, final String s, final int i, final String s1) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING || signalingState == SignalingState.RINGING) {
                stringeeManager.getCallsMap().put(stringeeCall.getCallId(), CallWrapper.this);
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
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = 2;
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = 3;
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

            Map<String, Object> map = Utils.createEventMap("didChangeSignalingState", clientWrapper.getId(), StringeeEventType.CallEvent);
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
    public void onError(final StringeeCall stringeeCall, final int code, final String message) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createErrorMap("makeCall", code, message);
            if (makeCallResult != null) {
                makeCallResult.success(map);
                makeCallResult = null;
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, final String description) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didHandleOnAnotherDevice", clientWrapper.getId(), StringeeEventType.CallEvent);
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
    public void onMediaStateChange(final StringeeCall stringeeCall, final StringeeCall.MediaState mediaState) {
        Utils.post(() -> {
            _mediaState = mediaState;
            Map<String, Object> map = Utils.createEventMap("didChangeMediaState", clientWrapper.getId(), StringeeEventType.CallEvent);
            Logging.d("mediaState: " + mediaState);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("callId", stringeeCall.getCallId());
            bodyMap.put("code", mediaState.getValue());
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);

            if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                remoteStreamShowed = true;
                Map<String, Object> map1 = Utils.createEventMap("didReceiveRemoteStream", clientWrapper.getId(), StringeeEventType.CallEvent);
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
                Map<String, Object> map = Utils.createEventMap("didReceiveLocalStream", clientWrapper.getId(), StringeeEventType.CallEvent);
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
    public void onRemoteStream(final StringeeCall stringeeCall) {
        Utils.post(() -> {
            if (stringeeCall.isVideoCall()) {
                Map<String, Object> map = Utils.createEventMap("didReceiveRemoteStream", clientWrapper.getId(), StringeeEventType.CallEvent);
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
    public void onCallInfo(final StringeeCall stringeeCall, final JSONObject jsonObject) {
        Utils.post(() -> {
            try {
                Map<String, Object> map = Utils.createEventMap("didReceiveCallInfo", clientWrapper.getId(), StringeeEventType.CallEvent);
                Logging.d("callInfo: " + jsonObject.toString());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Logging.e(CallWrapper.class, e);
            }
        });
    }
}