package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.Call2Event;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.CallStatsListener;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.call.StringeeCall2.StringeeCallStats;
import com.stringee.call.VideoQuality;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.MediaType;
import com.stringee.video.TextureViewRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class Call2Wrapper implements StringeeCall2.StringeeCallListener {
    private ClientWrapper clientWrapper;
    private StringeeCall2 call2;
    private StringeeManager stringeeManager;
    private Result makeCallResult;
    private StringeeCall2.MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;
    private boolean isIncomingCall = false;
    private String shareId = "";

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call, boolean isIncomingCall) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.stringeeManager = StringeeManager.getInstance();
        this.isIncomingCall = isIncomingCall;
    }

    public void prepareCall() {
        stringeeManager.startAudioManager(stringeeManager.getContext(), new AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(StringeeFlutterPlugin.TAG, "didChangeAudioDevice: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<AudioDevice> audioDeviceList = new ArrayList<>(availableAudioDevices);
                        List<Integer> codeList = new ArrayList<>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).ordinal());
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didChangeAudioDevice");
                        map.put("uuid", clientWrapper.getId());
                        Map<String, Object> bodyMap = new HashMap<>();
                        bodyMap.put("code", selectedAudioDevice.ordinal());
                        bodyMap.put("codeList", codeList);
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    }
                });
            }
        });

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.setCallListener(this);
    }

    /**
     * Make a new call
     */
    public void makeCall(final Result result) {
        this.makeCallResult = result;

        if (!Utils.isClientConnected(clientWrapper, "makeCall", result)) {
            makeCallResult = null;
            return;
        }

        prepareCall();
        call2.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        stringeeManager.getCall2sMap().put(call2.getCallId(), Call2Wrapper.this);
                        if (makeCallResult != null) {
                            Map<String, Object> callInfoMap = new HashMap<>();
                            callInfoMap.put("callId", call2.getCallId());
                            callInfoMap.put("from", call2.getFrom());
                            callInfoMap.put("to", call2.getTo());
                            callInfoMap.put("fromAlias", call2.getFromAlias());
                            callInfoMap.put("toAlias", call2.getToAlias());
                            callInfoMap.put("callType", call2.getCallType().getValue());
                            callInfoMap.put("isVideoCall", call2.isVideoCall());
                            callInfoMap.put("customDataFromYourServer", call2.getCustomDataFromYourServer());
                            int videoQuality = 0;
                            if (call2.getVideoQuality() != null) {
                                VideoQuality quality = call2.getVideoQuality();
                                if (Objects.requireNonNull(quality) == VideoQuality.QUALITY_720P) {
                                    videoQuality = 1;
                                } else if (quality == VideoQuality.QUALITY_1080P) {
                                    videoQuality = 2;
                                }
                            }
                            callInfoMap.put("videoQuality", videoQuality);
                            Utils.sendSuccessResponse("makeCall", "callInfo", callInfoMap, makeCallResult);
                            makeCallResult = null;
                        }
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (makeCallResult != null) {
                            Utils.sendErrorResponse("makeCall", stringeeError.getCode(), stringeeError.getMessage(), makeCallResult);
                            makeCallResult = null;
                        }
                    }
                });
            }
        });
    }

    /**
     * Init an answer
     */
    public void initAnswer(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "initAnswer", result)) {
            return;
        }

        prepareCall();
        call2.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("initAnswer", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("initAnswer", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Answer a call
     */
    public void answer(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "answer", result)) {
            return;
        }

        call2.answer(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("answer", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("answer", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Hang up call
     */
    public void hangup(final Result result) {
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.hangup(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("hangup", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("hangup", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
    }

    /**
     * Reject a call
     */
    public void reject(final Result result) {
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.reject(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("reject", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("reject", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
    }

    /**
     * Send call info
     */
    public void sendCallInfo(final Map<String, Object> callInfo, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendCallInfo", result)) {
            return;
        }

        try {
            JSONObject jsonObject = Utils.convertMapToJson(callInfo);
            call2.sendCallInfo(jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    stringeeManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.sendSuccessResponse("sendCallInfo", null, result);
                        }
                    });
                }

                @Override
                public void onError(final StringeeError stringeeError) {
                    stringeeManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.sendErrorResponse("sendCallInfo", stringeeError.getCode(), stringeeError.getMessage(), result);
                        }
                    });
                }
            });
        } catch (final JSONException e) {
            e.printStackTrace();
            stringeeManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Utils.sendErrorResponse("sendCallInfo", -2, e.getMessage(), result);
                }
            });
        }
    }

    /**
     * Mute or unmute
     */
    public void mute(final boolean mute, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "mute", result)) {
            return;
        }

        call2.mute(mute);
        Utils.sendSuccessResponse("mute", null, result);
    }

    /**
     * enable Video
     */
    public void enableVideo(final boolean enable, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "enableVideo", result)) {
            return;
        }

        call2.enableVideo(enable);
        Utils.sendSuccessResponse("enableVideo", null, result);
    }

    /**
     * Switch Camera
     */
    public void switchCamera(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "switchCamera", result)) {
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("switchCamera", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendCallInfo", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Switch Camera
     */
    public void switchCamera(String cameraName, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "switchCamera", result)) {
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("switchCamera", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendCallInfo", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        }, cameraName);
    }

    /**
     * Resume Video
     */
    public void resumeVideo(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "resumeVideo", result)) {
            return;
        }

        isResumeVideo = true;
        call2.resumeVideo();
        Utils.sendSuccessResponse("resumeVideo", null, result);
    }

    /**
     * Get call statistic
     */
    public void getCallStats(final Result result) {
        call2.getStats(new CallStatsListener() {
            @Override
            public void onCallStats(final StringeeCallStats stringeeCallStats) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("callBytesReceived", stringeeCallStats.callBytesReceived);
                        dataMap.put("callPacketsLost", stringeeCallStats.callPacketsLost);
                        dataMap.put("callPacketsReceived", stringeeCallStats.callPacketsReceived);
                        dataMap.put("videoBytesReceived", stringeeCallStats.videoBytesReceived);
                        dataMap.put("videoPacketsLost", stringeeCallStats.videoPacketsLost);
                        dataMap.put("videoPacketsReceived", stringeeCallStats.videoPacketsReceived);
                        dataMap.put("timeStamp", stringeeCallStats.timeStamp);
                        Utils.sendSuccessResponse("getCallStats", "stats", dataMap, result);
                        Log.d(StringeeFlutterPlugin.TAG, "callBytesReceived: " + stringeeCallStats.callBytesReceived + " - callPacketsLost: " + stringeeCallStats.callPacketsLost + " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived + " - timeStamp: " + stringeeCallStats.timeStamp);
                    }
                });
            }
        });
    }

    /**
     * Set local/remote stream is mirror or not
     */
    public void setMirror(final boolean isLocal, final boolean isMirror, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "setMirror", result)) {
            return;
        }

        if (isLocal) {
            call2.getLocalView2().setMirror(isMirror);

            //save localView option
            Map<String, Object> localViewOptions = StringeeManager.getInstance().getLocalViewOption().get(call2.getCallId());
            if (localViewOptions != null) {
                localViewOptions.put("isMirror", isMirror);
                StringeeManager.getInstance().getLocalViewOption().put(call2.getCallId(), localViewOptions);
            }
        } else {
            call2.getRemoteView2().setMirror(isMirror);

            //save remoteView option
            Map<String, Object> remoteViewOptions = StringeeManager.getInstance().getRemoteViewOption().get(call2.getCallId());
            if (remoteViewOptions != null) {
                remoteViewOptions.put("isMirror", isMirror);
                StringeeManager.getInstance().getRemoteViewOption().put(call2.getCallId(), remoteViewOptions);
            }
        }

        Utils.sendSuccessResponse("setMirror", null, result);
    }

    /**
     * Start capture screen
     */
    public void startCapture(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "startCapture", result)) {
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);

        stringeeManager.getCaptureManager().getActivityResult(new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                    stringeeManager.getCaptureManager().getScreenCapture().createCapture(data);
                }
                return false;
            }
        });

        call2.startCaptureScreen(stringeeManager.getCaptureManager().getScreenCapture(), REQUEST_CODE, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("startCapture", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("startCapture", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Stop capture screen
     */
    public void stopCapture(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "stopCapture", result)) {
            return;
        }

        call2.stopCaptureScreen(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("stopCapture", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("stopCapture", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Set auto send track media state change
     */
    public void setAutoSendTrackMediaStateChangeEvent(final boolean on, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "setAutoSendTrackMediaStateChangeEvent", result)) {
            return;
        }

        call2.setAutoSendTrackMediaStateChangeEvent(on);

        Utils.sendSuccessResponse("setAutoSendTrackMediaStateChangeEvent", null, result);
    }

    /**
     * Snap shot
     */
    public void snapshot(final Result result) {
        call2.snapshotLocal(new CallbackListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytearray = stream.toByteArray();
                        bitmap.recycle();
                        Utils.sendSuccessResponse("snapshot", "image", bytearray, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("snapshot", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didChangeSignalingState");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                map.put("body", bodyMap);
                if (isIncomingCall) {
                    if (signalingState != SignalingState.ANSWERED) {
                        Log.d(StringeeFlutterPlugin.TAG, "didChangeSignalingState: " + signalingState);
                        StringeeFlutterPlugin.eventSink.success(map);
                    }
                } else {
                    Log.d(StringeeFlutterPlugin.TAG, "didChangeSignalingState: " + signalingState);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onError(final StringeeCall2 stringeeCall, final int code, final String message) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onError2: code: " + code + " - message: " + message);
                if (makeCallResult != null) {
                    Utils.sendErrorResponse("makeCall", code, message, makeCallResult);
                    makeCallResult = null;
                }
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String description) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didHandleOnAnotherDevice: signalingState: " + signalingState + " - description: " + description);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didHandleOnAnotherDevice");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                bodyMap.put("description", description);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onMediaStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.MediaState mediaState) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(StringeeFlutterPlugin.TAG, "didChangeMediaState: " + mediaState);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didChangeMediaState");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);

                if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                    remoteStreamShowed = true;
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("nativeEventType", Call2Event.getValue());
                    map1.put("event", "didReceiveRemoteStream");
                    map1.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap1 = new HashMap<>();
                    bodyMap1.put("callId", stringeeCall.getCallId());
                    map1.put("body", bodyMap1);
                    StringeeFlutterPlugin.eventSink.success(map1);
                }
            }
        });
    }

    @Override
    public void onLocalStream(final StringeeCall2 stringeeCall) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveLocalStream");
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveLocalStream");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    if (isResumeVideo) {
                        Map<String, Object> localViewOptions = stringeeManager.getLocalViewOption().get(stringeeCall.getCallId());
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        boolean isMirror = (Boolean) localViewOptions.get("isMirror");
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                        localView.removeAllViews();
                        if (getLocalView().getParent() != null) {
                            ((FrameLayout) getLocalView().getParent()).removeView(getLocalView());
                        }
                        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER;
                        localView.addView(getLocalView(), layoutParams);
                        renderLocalView(scalingType);
                        getLocalView().setMirror(isMirror);

                        isResumeVideo = false;
                    }
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onRemoteStream(final StringeeCall2 stringeeCall) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveRemoteStream");
                    if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                        remoteStreamShowed = true;
                        Map<String, Object> map = new HashMap<>();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didReceiveRemoteStream");
                        map.put("uuid", clientWrapper.getId());
                        Map<String, Object> bodyMap = new HashMap<>();
                        bodyMap.put("callId", stringeeCall.getCallId());
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    } else {
                        hasRemoteStream = true;
                    }

                    Map<String, Object> remoteViewOptions = stringeeManager.getRemoteViewOption().get(stringeeCall.getCallId());
                    if (remoteViewOptions != null) {
                        FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                        boolean isMirror = (Boolean) remoteViewOptions.get("isMirror");
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
            }
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didAddVideoTrack");
                if (stringeeVideoTrack.isLocal()) {
                    shareId = Utils.createLocalId();
                }
                VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, stringeeVideoTrack.isLocal() ? shareId : "", true);
                stringeeManager.getTracksMap().put(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId(), videoTrackManager);

                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didAddVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didRemoveVideoTrack");
                VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId());

                if (videoTrackManager != null) {
                    StringeeVideoTrack videoTrack = videoTrackManager.getVideoTrack();
                    if (videoTrack != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didRemoveVideoTrack");
                        map.put("uuid", clientWrapper.getId());
                        Map<String, Object> bodyMap = new HashMap<>();
                        bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    }
                }
            }
        });
    }

    @Override
    public void onCallInfo(final StringeeCall2 stringeeCall, final JSONObject jsonObject) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveCallInfo: " + jsonObject.toString());
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveCallInfo");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTrackMediaStateChange(String from, MediaType mediaType, boolean enable) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didTrackMediaStateChange: mediaType: " + mediaType.name() + " - enable: " + enable);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didTrackMediaStateChange");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("callId", call2.getCallId());
                bodyMap.put("from", from);
                switch (mediaType) {
                    case AUDIO:
                        bodyMap.put("mediaType", 0);
                        break;
                    case VIDEO:
                        bodyMap.put("mediaType", 1);
                        break;
                }
                bodyMap.put("enable", enable);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }
}