package com.stringee.stringeeflutterplugin;

import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.stringeeflutterplugin.common.enumeration.StringeeEventType;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.MediaType;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

/**
 * Bridges a native {@link StringeeCall2} to Flutter method results and event-channel payloads.
 *
 * <p>In addition to normal call state, this wrapper forwards Call2 video-track lifecycle events to
 * the Dart {@code StringeeCall2} API.</p>
 */
public class Call2Wrapper implements StringeeCall2.StringeeCallListener {

    private final ClientWrapper clientWrapper;
    private final StringeeCall2 call2;
    private Result makeCallResult;
    private StringeeCall2.MediaState mediaState;
    private boolean localStreamShowed;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private final boolean isIncomingCall;

    private static final String TAG = "StringeeSDK";

    /** Creates a wrapper for an incoming {@code call}. */
    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.isIncomingCall = true;
    }

    /** Creates a wrapper for an outgoing {@code call} and its pending Flutter {@code result}. */
    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call, Result result) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.makeCallResult = result;
        this.isIncomingCall = false;
    }

    /** Resets transient media state and registers the native call listener. */
    public void prepareCall() {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.setCallListener(this);
    }

    /**
     * Starts the outgoing {@link StringeeCall2}.
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
     * Sends ringing state before answering an incoming call.
     *
     * @param result
     *         Flutter method result.
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
                    Log.d(
                            TAG, "initAnswer: false - " + stringeeError.getCode() + " - " +
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
     * Answers the current incoming call.
     *
     * @param result
     *         Flutter method result.
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
     * Hangs up the current call and clears local call state.
     *
     * @param result
     *         Flutter method result.
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
     * Rejects the current incoming call and clears local call state.
     *
     * @param result
     *         Flutter method result.
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
     * Sends custom call info to the remote participant.
     *
     * @param callInfo
     *         Custom call info payload.
     * @param result
     *         Flutter method result.
     */
    public void sendCallInfo(final Map<String, Object> callInfo, final Result result) {
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
            call2.sendCallInfo(
                    jsonObject, new StatusListener() {
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
                                Log.d(
                                        TAG,
                                        "sendCallInfo: false - " + stringeeError.getCode() + " - " +
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
     * Mutes or unmutes the local audio stream.
     *
     * @param mute
     *         True to mute; false to unmute.
     * @param result
     *         Flutter method result.
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
     * Enables or disables the local video stream.
     *
     * @param enable
     *         True to enable video; false to disable it.
     * @param result
     *         Flutter method result.
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
     * Switches to the next available camera.
     *
     * @param result
     *         Flutter method result.
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
                    Log.d(
                            TAG, "switchCamera: false - code: " + stringeeError.getCode() +
                                    " - message: " + stringeeError.getMessage()
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
     * Switches to a specific camera by name or identifier.
     *
     * @param cameraName
     *         Camera name or identifier.
     * @param result
     *         Flutter method result.
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

        call2.switchCamera(
                new StatusListener() {
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
                            Log.d(
                                    TAG, "switchCamera: false - code: " + stringeeError.getCode() +
                                            " - message: " + stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }, cameraName
        );
    }

    /**
     * Resumes the local video stream after it was interrupted.
     *
     * @param result
     *         Flutter method result.
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
     * Gets media statistics for the current call.
     *
     * @param result
     *         Flutter method result.
     */
    public void getCallStats(final Result result) {
        call2.getStats(stringeeCallStats -> Utils.post(() -> {
            Log.d(
                    TAG, "getCallStats: callBytesReceived: " + stringeeCallStats.callBytesReceived +
                            " - callPacketsLost: " + stringeeCallStats.callPacketsLost +
                            " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived +
                            " - timeStamp: " + stringeeCallStats.timeStamp
            );
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
     * Sets mirror mode for the local or remote video renderer.
     *
     * @param isLocal
     *         True for local renderer; false for remote renderer.
     * @param isMirror
     *         True to mirror the selected renderer.
     * @param result
     *         Flutter method result.
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

    /** Returns the local native video renderer, or {@code null} until media is available. */
    public TextureViewRenderer getLocalView() {
        return call2.getLocalView2();
    }

    /** Returns the remote native video renderer, or {@code null} until media is available. */
    public TextureViewRenderer getRemoteView() {
        return call2.getRemoteView2();
    }

    /** Attaches the local renderer using {@code scalingType}. */
    public void renderLocalView(ScalingType scalingType) {
        call2.renderLocalView2(scalingType);
    }

    /** Attaches the remote renderer using {@code scalingType}. */
    public void renderRemoteView(ScalingType scalingType) {
        call2.renderRemoteView2(scalingType);
    }

    @Override
    public void onSignalingStateChange(
            final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState,
            final String s, final int i, final String s1
    ) {
        Utils.post(() -> {
            if (signalingState == SignalingState.CALLING) {
                Log.d(TAG, "makeCall: success");
                StringeeManager.getInstance().getCall2sMap().put(
                        stringeeCall.getCallId(), Call2Wrapper.this);
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
                callInfoMap.put(
                        "customDataFromYourServer",
                        stringeeCall.getCustomDataFromYourServer()
                );
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
    public void onHandledOnAnotherDevice(
            final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState,
            final String description
    ) {
        Utils.post(() -> {
            Log.d(
                    TAG, "onHandledOnAnotherDevice2:" + "\nsignalingState: " + signalingState +
                            " - description: " + description
            );
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
    public void onMediaStateChange(
            final StringeeCall2 stringeeCall,
            final StringeeCall2.MediaState mediaState
    ) {
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

    /**
     * Emits the legacy Flutter local-stream event for the SDK 2.x local-track callback.
     *
     * @param stringeeCall
     *         Source call.
     */
    private void handleLocalStream(final StringeeCall2 stringeeCall) {
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
                    Map<String, Object> localViewOptions = StringeeManager.getInstance().getLocalViewOptions().get(
                            stringeeCall.getCallId());
                    if (localViewOptions != null) {
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        boolean isMirror = Boolean.TRUE.equals(localViewOptions.get("isMirror"));
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                        if (localView != null) {
                            localView.removeAllViews();
                            LayoutParams layoutParams = new LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT
                            );
                            layoutParams.gravity = Gravity.CENTER;
                            TextureViewRenderer renderer = getLocalView();
                            if (Utils.attachVideoRenderer(localView, renderer, layoutParams)) {
                                renderLocalView(scalingType);
                                renderer.setMirror(isMirror);
                            }
                        }
                    }
                }
                localStreamShowed = true;
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    /**
     * Emits the legacy Flutter remote-stream event for the SDK 2.x remote-track callback.
     *
     * @param stringeeCall
     *         Source call.
     */
    private void handleRemoteStream(final StringeeCall2 stringeeCall) {
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

                Map<String, Object> remoteViewOptions = StringeeManager.getInstance().getRemoteViewOptions().get(
                        stringeeCall.getCallId());
                if (remoteViewOptions != null) {
                    FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                    boolean isMirror = Boolean.TRUE.equals(remoteViewOptions.get("isMirror"));
                    ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");

                    if (remoteView != null) {
                        remoteView.removeAllViews();
                        LayoutParams layoutParams = new LayoutParams(
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT
                        );
                        layoutParams.gravity = Gravity.CENTER;
                        TextureViewRenderer renderer = getRemoteView();
                        if (Utils.attachVideoRenderer(remoteView, renderer, layoutParams)) {
                            renderRemoteView(scalingType);
                            renderer.setMirror(isMirror);
                        }
                    }
                }
            }
        });
    }

    /**
     * Registers a screen-share video track and emits the Flutter add-track event.
     *
     * @param stringeeVideoTrack
     *         Added video track.
     */
    private void handleVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(TAG, "didAddVideoTrack");
            VideoTrackManager videoTrackManager = new VideoTrackManager(
                    clientWrapper,
                    stringeeVideoTrack,
                    stringeeVideoTrack.getLocalId(),
                    true
            );
            StringeeManager.getInstance().getTracksMap().put(
                    stringeeVideoTrack.getId(), videoTrackManager);

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

    /**
     * Emits the Flutter remove-track event for a screen-share video track.
     *
     * @param stringeeVideoTrack
     *         Removed video track.
     */
    private void handleVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(TAG, "didRemoveVideoTrack");
            VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                    stringeeVideoTrack.getId());

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
    public void onLocalTrackAdded(
            StringeeCall2 stringeeCall2,
            StringeeVideoTrack stringeeVideoTrack
    ) {
        if (stringeeVideoTrack.getTrackType() == StringeeVideoTrack.TrackType.SCREEN) {
            return;
        }
        handleLocalStream(stringeeCall2);
    }

    @Override
    public void onRemoteTrackAdded(
            StringeeCall2 stringeeCall2,
            StringeeVideoTrack stringeeVideoTrack
    ) {
        if (stringeeVideoTrack.getTrackType() == StringeeVideoTrack.TrackType.SCREEN) {
            handleVideoTrackAdded(stringeeVideoTrack);
            return;
        }
        handleRemoteStream(stringeeCall2);
    }

    @Override
    public void onRemoteTrackRemoved(
            StringeeCall2 stringeeCall2,
            StringeeVideoTrack stringeeVideoTrack
    ) {
        if (stringeeVideoTrack.getTrackType() == StringeeVideoTrack.TrackType.SCREEN) {
            handleVideoTrackRemoved(stringeeVideoTrack);
        }
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
