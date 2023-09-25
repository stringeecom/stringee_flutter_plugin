package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.CallEvent;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.CallStatsListener;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.call.StringeeCall.SignalingState;
import com.stringee.call.StringeeCall.StringeeCallStats;
import com.stringee.call.VideoQuality;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
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
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;

public class CallWrapper implements StringeeCall.StringeeCallListener {
    private ClientWrapper clientWrapper;
    private StringeeCall call;
    private StringeeManager stringeeManager;
    private Result makeCallResult;
    private MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;
    private boolean isIncomingCall = false;

    public CallWrapper(ClientWrapper clientWrapper, StringeeCall call, boolean isIncomingCall) {
        this.clientWrapper = clientWrapper;
        this.call = call;
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
                        map.put("nativeEventType", CallEvent.getValue());
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

        call.setCallListener(this);
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
        call.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(StringeeFlutterPlugin.TAG, "makeCall: success");
                        stringeeManager.getCallsMap().put(call.getCallId(), CallWrapper.this);
                        if (makeCallResult != null) {
                            Map<String, Object> callInfoMap = new HashMap<>();
                            callInfoMap.put("callId", call.getCallId());
                            callInfoMap.put("from", call.getFrom());
                            callInfoMap.put("to", call.getTo());
                            callInfoMap.put("fromAlias", call.getFromAlias());
                            callInfoMap.put("toAlias", call.getToAlias());
                            callInfoMap.put("callType", call.getCallType().getValue());
                            callInfoMap.put("isVideoCall", call.isVideoCall());
                            callInfoMap.put("customDataFromYourServer", call.getCustomDataFromYourServer());
                            int videoQuality = 0;
                            if (call.getVideoQuality() != null) {
                                VideoQuality quality = call.getVideoQuality();
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
        call.ringing(new StatusListener() {
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

        call.answer(new StatusListener() {
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

        call.hangup(new StatusListener() {
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
        stringeeManager.getCallsMap().put(call.getCallId(), null);
    }

    /**
     * Reject a call
     */
    public void reject(final Result result) {
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call.reject(new StatusListener() {
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
        stringeeManager.getCallsMap().put(call.getCallId(), null);
        Utils.sendSuccessResponse("reject", null, result);
    }

    /**
     * Send a DTMF
     */
    public void sendDTMF(final String dtmf, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendDTMF", result)) {
            return;
        }

        call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("sendDTMF", null, result);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendDtmf", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
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
            call.sendCallInfo(jsonObject, new StatusListener() {
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
     * Mute or un mute
     */
    public void mute(final boolean mute, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "mute", result)) {
            return;
        }

        call.mute(mute);
        Utils.sendSuccessResponse("mute", null, result);
    }

    /**
     * enable Video
     */
    public void enableVideo(final boolean enable, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "enableVideo", result)) {
            return;
        }

        call.enableVideo(enable);
        Utils.sendSuccessResponse("enableVideo", null, result);
    }

    /**
     * Switch Camera
     */
    public void switchCamera(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "switchCamera", result)) {
            return;
        }

        call.switchCamera(new StatusListener() {
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

        call.switchCamera(new StatusListener() {
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
        call.resumeVideo();
        Utils.sendSuccessResponse("resumeVideo", null, result);
    }

    /**
     * Get call statistic
     */
    public void getCallStats(final Result result) {
        call.getStats(new CallStatsListener() {
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
            call.getLocalView2().setMirror(isMirror);

            //save localView option
            Map<String, Object> localViewOptions = StringeeManager.getInstance().getLocalViewOption().get(call.getCallId());
            if (localViewOptions != null) {
                localViewOptions.put("isMirror", isMirror);
                StringeeManager.getInstance().getLocalViewOption().put(call.getCallId(), localViewOptions);
            }
        } else {
            call.getRemoteView2().setMirror(isMirror);

            //save remoteView option
            Map<String, Object> remoteViewOptions = StringeeManager.getInstance().getRemoteViewOption().get(call.getCallId());
            if (remoteViewOptions != null) {
                remoteViewOptions.put("isMirror", isMirror);
                StringeeManager.getInstance().getRemoteViewOption().put(call.getCallId(), remoteViewOptions);
            }
        }

        Utils.sendSuccessResponse("setMirror", null, result);
    }

    /**
     * Hold the call
     */
    public void hold(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "hold", result)) {
            return;
        }

        call.hold(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("hold", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("hold", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Un hold the call
     */
    public void unHold(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "unHold", result)) {
            return;
        }

        call.unHold(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("unHold", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("unHold", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Transfer the call to userId
     */
    public void transferToUserId(final String userId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "transferToUserId", result)) {
            return;
        }

        call.transferToUserId(userId, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("transferToUserId", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("transferToUserId", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Transfer the call to phone
     */
    public void transferToPhone(final String from, final String to, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "transferToPhone", result)) {
            return;
        }

        call.transferToPhone(from, to, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("transferToPhone", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("transferToPhone", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Snap shot
     */
    public void snapshot(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "snapshot", result)) {
            return;
        }

        call.snapShot(new CallbackListener<Bitmap>() {
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", CallEvent.getValue());
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
    public void onError(final StringeeCall stringeeCall, final int code, final String message) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "onError: code: " + code + " -message: " + message);
                if (makeCallResult != null) {
                    Utils.sendErrorResponse("makeCall", code, message, makeCallResult);
                    makeCallResult = null;
                }
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, final String description) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didHandleOnAnotherDevice: signalingState: " + signalingState + " - description: " + description);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", CallEvent.getValue());
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
    public void onMediaStateChange(final StringeeCall stringeeCall, final StringeeCall.MediaState mediaState) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(StringeeFlutterPlugin.TAG, "didChangeMediaState: " + mediaState);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", CallEvent.getValue());
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
                    map1.put("nativeEventType", CallEvent.getValue());
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
    public void onLocalStream(final StringeeCall stringeeCall) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveLocalStream");
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", CallEvent.getValue());
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
    public void onRemoteStream(final StringeeCall stringeeCall) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveRemoteStream");
                    if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                        remoteStreamShowed = true;
                        Map<String, Object> map = new HashMap<>();
                        map.put("nativeEventType", CallEvent.getValue());
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
    public void onCallInfo(final StringeeCall stringeeCall, final JSONObject jsonObject) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveCallInfo: " + jsonObject.toString());
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", CallEvent.getValue());
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
}