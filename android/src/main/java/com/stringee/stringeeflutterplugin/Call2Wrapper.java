package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import com.stringee.stringeeflutterplugin.common.enumeration.StringeeEventType;
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

public class Call2Wrapper implements StringeeCall2.StringeeCallListener {
    private final ClientWrapper clientWrapper;
    private final StringeeCall2 call2;
    private Result makeCallResult;
    private StringeeCall2.MediaState mediaState;
    private boolean localStreamShowed;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private final boolean isIncomingCall;
    private String shareId = "";

    private static final String TAG = "StringeeSDK";

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.isIncomingCall = true;
    }

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call, Result result) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.makeCallResult = result;
        this.isIncomingCall = false;
    }

    public void prepareCall() {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.setCallListener(this);
    }

    /**
     * Make a new call
     */
    public void makeCall() {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "makeCall: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
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
     * @param result
     */
    public void initAnswer(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "initAnswer: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        prepareCall();
        call2.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "initAnswer: success");
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
                    Log.d(TAG, "initAnswer: false - " + stringeeError.getCode() + " - " +
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
     * Answer a call
     *
     * @param result
     */
    public void answer(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "answer: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.answer(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        Log.d(TAG, "answer: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Hang up call
     *
     * @param result
     */
    public void hangup(final Result result) {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCall2sMap().put(call2.getCallId(), null);
        Log.d(TAG, "hangup: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Reject a call
     *
     * @param result
     */
    public void reject(final Result result) {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.reject(new StatusListener() {
            @Override
            public void onSuccess() {
            }
        });
        StringeeManager.getInstance().getCall2sMap().put(call2.getCallId(), null);
        Log.d(TAG, "hangup: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Send call info
     *
     * @param callInfo
     * @param result
     */
    public void sendCallInfo(final Map callInfo, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "sendCallInfo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        try {
            JSONObject jsonObject = Utils.convertMapToJson(callInfo);
            call2.sendCallInfo(jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    Utils.post(() -> {
                        Log.d(TAG, "sendCallInfo: success");
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
                        Log.d(TAG, "sendCallInfo: false - " + stringeeError.getCode() + " - " +
                                stringeeError.getMessage());
                        Map<String, Object> map = new HashMap<>();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    });
                }
            });
        } catch (final JSONException e) {
            Utils.reportException(Call2Wrapper.class, e);
            Utils.post(() -> {
                Log.d(TAG, "sendCallInfo: false - -2 - " + e.getMessage());
                Map<String, Object> map = new HashMap<>();
                map.put("status", false);
                map.put("code", -2);
                map.put("message", e.getMessage());
                result.success(map);
            });
        }
    }

    /**
     * Mute or unmute
     *
     * @param mute
     * @param result
     */
    public void mute(final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "mute: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.mute(mute);
        Log.d(TAG, "mute: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * enable Video
     *
     * @param enable
     * @param result
     */
    public void enableVideo(final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "enableVideo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.enableVideo(enable);
        Log.d(TAG, "enableVideo: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Switch Camera
     *
     * @param result
     */
    public void switchCamera(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "switchCamera: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(TAG, "switchCamera: false - code: " + stringeeError.getCode() +
                            " - message: " + stringeeError.getMessage());
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
     * Switch Camera
     *
     * @param result
     * @param cameraName
     */
    public void switchCamera(String cameraName, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "switchCamera: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(TAG, "switchCamera: false - code: " + stringeeError.getCode() +
                            " - message: " + stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        }, cameraName);
    }

    /**
     * Resume Video
     *
     * @param result
     */
    public void resumeVideo(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "resumeVideo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.resumeVideo();
        Log.d(TAG, "resumeVideo: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Get call statistic
     *
     * @param result
     */
    public void getCallStats(final Result result) {
        call2.getStats(stringeeCallStats -> Utils.post(() -> {
            Log.d(TAG, "getCallStats: callBytesReceived: " + stringeeCallStats.callBytesReceived +
                    " - callPacketsLost: " + stringeeCallStats.callPacketsLost +
                    " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived +
                    " - timeStamp: " + stringeeCallStats.timeStamp);
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("code", 0);
            map.put("message", "Success");
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
     * @param result
     */
    public void setMirror(final boolean isLocal, final boolean isMirror, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "setMirror: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (isLocal) {
            call2.getLocalView2().setMirror(isMirror);
        } else {
            call2.getRemoteView2().setMirror(isMirror);
        }

        Log.d(TAG, "setMirror: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Start capture screen
     *
     * @param result
     */
    public void startCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "startCapture: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            final int REQUEST_CODE = new Random().nextInt(65536);

            StringeeManager.getInstance()
                    .getCaptureManager()
                    .getActivityResult((requestCode, resultCode, data) -> {
                        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                            StringeeManager.getInstance()
                                    .getCaptureManager()
                                    .getScreenCapture()
                                    .createCapture(data,
                                            new CallbackListener<StringeeVideoTrack>() {
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
                            Utils.post(() -> {
                                Log.d(TAG, "startCapture: success");
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
                                Log.d(TAG,
                                        "startCapture: false - " + stringeeError.getCode() + " - " +
                                                stringeeError.getMessage());
                                Map<String, Object> map = new HashMap<>();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            });
                        }
                    });
        } else {
            Log.d(TAG, "startCapture: false - -5 - This feature requires android api level >= 21");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -5);
            map.put("message", "This feature requires android api level >= 21");
            result.success(map);
        }
    }

    /**
     * Stop capture screen
     *
     * @param result
     */
    public void stopCapture(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "stopCapture: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.stopCaptureScreen(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "stopCapture: success");
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
                    Log.d(TAG, "stopCapture: false - " + stringeeError.getCode() + " - " +
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
    public void onSignalingStateChange(final StringeeCall2 stringeeCall,
                                       final StringeeCall2.SignalingState signalingState,
                                       final String s, final int i, final String s1) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING) {
                Log.d(TAG, "makeCall: success");
                StringeeManager.getInstance()
                        .getCall2sMap()
                        .put(stringeeCall.getCallId(), Call2Wrapper.this);
                Map<String, Object> map = new HashMap<>();
                map.put("status", true);
                map.put("code", 0);
                map.put("message", "Success");
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
                map.put("callInfo", callInfoMap);
                if (makeCallResult != null) {
                    makeCallResult.success(map);
                    makeCallResult = null;
                }
            }

            if (isIncomingCall) {
                if (signalingState != SignalingState.ANSWERED) {
                    Log.d(TAG, "onSignalingStateChange2: " + signalingState);
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
                Log.d(TAG, "onSignalingStateChange2: " + signalingState);
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
            Log.d(TAG, "onError2: code: " + code + " -message: " + message);
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
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall,
                                         final StringeeCall2.SignalingState signalingState,
                                         final String description) {
        Utils.post(() -> {
            Log.d(TAG, "onHandledOnAnotherDevice2:" + "\nsignalingState: " + signalingState +
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
            Log.d(TAG, "onMediaStateChange2: " + mediaState);
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
                Log.d(TAG, "onLocalStream2");
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
                Log.d(TAG, "onRemoteStream2");
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
            Log.d(TAG, "didAddVideoTrack");
            if (stringeeVideoTrack.isLocal()) {
                shareId = Utils.createLocalId();
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
            bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(TAG, "didRemoveVideoTrack");
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
                Log.d(TAG, "onCallInfo2: " + jsonObject.toString());
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
}
