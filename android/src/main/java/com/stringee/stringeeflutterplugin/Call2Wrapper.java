package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.Call2Event;
import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.CallEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.CallStatsListener;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.call.StringeeCall2.StringeeCallStats;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideo.ScalingType;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class Call2Wrapper implements StringeeCall2.StringeeCallListener {
    private ClientWrapper clientWrapper;
    private StringeeCall2 call2;
    private StringeeManager stringeeManager;
    private Result makeCallResult;
    private Handler handler;
    private StringeeCall2.MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;
    private boolean isIncomingCall = false;
    private String shareId = "";

    private static final String TAG = "StringeeSDK";

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.stringeeManager = StringeeManager.getInstance();
        this.handler = stringeeManager.getHandler();
        this.isIncomingCall = true;
    }

    public Call2Wrapper(ClientWrapper clientWrapper, StringeeCall2 call, Result result) {
        this.call2 = call;
        this.clientWrapper = clientWrapper;
        this.makeCallResult = result;
        this.stringeeManager = StringeeManager.getInstance();
        this.handler = stringeeManager.getHandler();
        this.isIncomingCall = false;
    }

    public void prepareCall() {
        stringeeManager.startAudioManager(stringeeManager.getContext(), new AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<AudioDevice> audioDeviceList = new ArrayList<AudioDevice>();
                        audioDeviceList.addAll(availableAudioDevices);
                        List<Integer> codeList = new ArrayList<Integer>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).ordinal());
                        }
                        Map map = new HashMap();
                        map.put("nativeEventType", CallEvent.getValue());
                        map.put("event", "didChangeAudioDevice");
                        map.put("uuid", clientWrapper.getId());
                        Map bodyMap = new HashMap();
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
    public void makeCall() {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "makeCall: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            makeCallResult.success(map);
            return;
        }

        prepareCall();
        call2.makeCall();
    }

    /**
     * Init an answer
     *
     * @param result
     */
    public void initAnswer(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "initAnswer: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "initAnswer: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "initAnswer: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.answer();
        Log.d(TAG, "answer: success");
        Map map = new HashMap();
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
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.hangup();
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
        Log.d(TAG, "hangup: success");
        Map map = new HashMap();
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
        stringeeManager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        call2.reject();
        stringeeManager.getCall2sMap().put(call2.getCallId(), null);
        Log.d(TAG, "hangup: success");
        Map map = new HashMap();
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
            Map map = new HashMap();
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "sendCallInfo: success");
                            Map map = new HashMap();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        }
                    });
                }

                @Override
                public void onError(final StringeeError stringeeError) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "sendCallInfo: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                            Map map = new HashMap();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        }
                    });
                }
            });
        } catch (final JSONException e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "sendCallInfo: false - -2 - " + e.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", e.getMessage());
                    result.success(map);
                }
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.mute(mute);
        Log.d(TAG, "mute: success");
        Map map = new HashMap();
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.enableVideo(enable);
        Log.d(TAG, "enableVideo: success");
        Map map = new HashMap();
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: false - code: " + stringeeError.getCode() + " - message: " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Switch Camera
     *
     * @param result
     * @param cameraId
     */
    public void switchCamera(int cameraId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: false - code: " + stringeeError.getCode() + " - message: " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        }, cameraId);
    }

    /**
     * Resume Video
     *
     * @param result
     */
    public void resumeVideo(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "resumeVideo: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        isResumeVideo = true;
        call2.resumeVideo();
        Log.d(TAG, "resumeVideo: success");
        Map map = new HashMap();
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
        call2.getStats(new CallStatsListener() {
            @Override
            public void onCallStats(final StringeeCallStats stringeeCallStats) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "getCallStats: callBytesReceived: " + stringeeCallStats.callBytesReceived +
                                " - callPacketsLost: " + stringeeCallStats.callPacketsLost +
                                " - callPacketsReceived: " + stringeeCallStats.callPacketsReceived +
                                " - timeStamp: " + stringeeCallStats.timeStamp);
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
        });
    }

    /**
     * Set local stream is mirror or not
     *
     * @param result
     */
    public void setMirror(final boolean isLocal, final boolean isMirror, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "setMirror: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (isLocal) {
            call2.getLocalView().setMirror(isMirror);
        } else {
            call2.getRemoteView().setMirror(isMirror);
        }

        Log.d(TAG, "setMirror: success");
        Map map = new HashMap();
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "startCapture: success");
                            Map map = new HashMap();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        }
                    });
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "startCapture: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                            Map map = new HashMap();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        }
                    });
                }
            });

        } else {
            Log.d(TAG, "startCapture: false - -5 - This feature requires android api level >= 21");
            Map map = new HashMap();
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
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        call2.stopCaptureScreen(new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "stopCapture: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "stopCapture: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }


    public SurfaceViewRenderer getLocalView() {
        return call2.getLocalView();
    }

    public SurfaceViewRenderer getRemoteView() {
        return call2.getRemoteView();
    }

    public void renderLocalView(boolean isOverlay, StringeeVideo.ScalingType scalingType) {
        call2.renderLocalView(isOverlay, scalingType);
    }

    public void renderRemoteView(boolean isOverlay, StringeeVideo.ScalingType scalingType) {
        call2.renderRemoteView(isOverlay, scalingType);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String s, final int i, final String s1) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (signalingState == SignalingState.CALLING) {
                    Log.d(TAG, "makeCall: success");
                    stringeeManager.getCall2sMap().put(stringeeCall.getCallId(), Call2Wrapper.this);
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
                    callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                    int callType = 0;
                    if (!stringeeCall.getFrom().equals(clientWrapper.getClient().getUserId())) {
                        callType = 1;
                    }
                    callInfoMap.put("callType", callType);
                    callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                    callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                    map.put("callInfo", callInfoMap);
                    if (makeCallResult != null) {
                        makeCallResult.success(map);
                        makeCallResult = null;
                    }
                }

                if (isIncomingCall) {
                    if (signalingState != SignalingState.ANSWERED) {
                        Log.d(TAG, "onSignalingStateChange2: " + signalingState);
                        Map map = new HashMap();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didChangeSignalingState");
                        map.put("uuid", clientWrapper.getId());
                        Map bodyMap = new HashMap();
                        bodyMap.put("callId", stringeeCall.getCallId());
                        bodyMap.put("code", signalingState.getValue());
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    }
                } else {
                    Log.d(TAG, "onSignalingStateChange2: " + signalingState);
                    Map map = new HashMap();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didChangeSignalingState");
                    map.put("uuid", clientWrapper.getId());
                    Map bodyMap = new HashMap();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    bodyMap.put("code", signalingState.getValue());
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onError(final StringeeCall2 stringeeCall, final int code, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError2: code: " + code + " -message: " + message);
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", code);
                map.put("message", message);
                if (makeCallResult != null) {
                    makeCallResult.success(map);
                    makeCallResult = null;
                }
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String description) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onHandledOnAnotherDevice2:" + "\nsignalingState: " + signalingState + " - description: " + description);
                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didHandleOnAnotherDevice");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(TAG, "onMediaStateChange2: " + mediaState);
                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didChangeMediaState");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);

                if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                    remoteStreamShowed = true;
                    Map map1 = new HashMap();
                    map1.put("nativeEventType", Call2Event.getValue());
                    map1.put("event", "didReceiveRemoteStream");
                    map1.put("uuid", clientWrapper.getId());
                    Map bodyMap1 = new HashMap();
                    bodyMap1.put("callId", stringeeCall.getCallId());
                    map1.put("body", bodyMap1);
                    StringeeFlutterPlugin.eventSink.success(map1);
                }
            }
        });
    }

    @Override
    public void onLocalStream(final StringeeCall2 stringeeCall) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onLocalStream2");
                    Map map = new HashMap();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveLocalStream");
                    map.put("uuid", clientWrapper.getId());
                    Map bodyMap = new HashMap();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    if (isResumeVideo) {
                        Map<String, Object> localViewOptions = stringeeManager.getLocalViewOptions().get(stringeeCall.getCallId());
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        boolean isMirror = (Boolean) localViewOptions.get("isMirror");
                        boolean isOverlay = (Boolean) localViewOptions.get("isOverlay");
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                        localView.removeAllViews();
                        if (stringeeCall.getLocalView().getParent() != null) {
                            ((FrameLayout) stringeeCall.getLocalView().getParent()).removeView(stringeeCall.getLocalView());
                        }
                        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER;
                        localView.addView(stringeeCall.getLocalView(), layoutParams);
                        stringeeCall.renderLocalView(isOverlay, scalingType);
                        stringeeCall.getLocalView().setMirror(isMirror);

                        isResumeVideo = false;
                    }
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onRemoteStream(final StringeeCall2 stringeeCall) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onRemoteStream2");
                    if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                        remoteStreamShowed = true;
                        Map map = new HashMap();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didReceiveRemoteStream");
                        map.put("uuid", clientWrapper.getId());
                        Map bodyMap = new HashMap();
                        bodyMap.put("callId", stringeeCall.getCallId());
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    } else {
                        hasRemoteStream = true;
                    }

                    Map<String, Object> remoteViewOptions = stringeeManager.getRemoteViewOptions().get(stringeeCall.getCallId());
                    if (remoteViewOptions != null) {
                        FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                        boolean isMirror = (Boolean) remoteViewOptions.get("isMirror");
                        boolean isOverlay = (Boolean) remoteViewOptions.get("isOverlay");
                        ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");

                        if (remoteView != null) {
                            remoteView.removeAllViews();
                            if (stringeeCall.getRemoteView().getParent() != null) {
                                ((FrameLayout) stringeeCall.getRemoteView().getParent()).removeView(stringeeCall.getRemoteView());
                            }
                            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER;
                            remoteView.addView(stringeeCall.getRemoteView(), layoutParams);
                            stringeeCall.renderRemoteView(isOverlay, scalingType);
                            stringeeCall.getRemoteView().setMirror(isMirror);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didAddVideoTrack");
                if (stringeeVideoTrack.isLocal()) {
                    shareId = Utils.createLocalId();
                }
                VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, stringeeVideoTrack.isLocal() ? shareId : "", true);
                stringeeManager.getTracksMap().put(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId(), videoTrackManager);

                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didAddVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didRemoveVideoTrack");
                VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(stringeeVideoTrack.isLocal() ? shareId : stringeeVideoTrack.getId());

                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didRemoveVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(videoTrackManager));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onCallInfo(final StringeeCall2 stringeeCall, final JSONObject jsonObject) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCallInfo2: " + jsonObject.toString());
                    Map map = new HashMap();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveCallInfo");
                    map.put("uuid", clientWrapper.getId());
                    Map bodyMap = new HashMap();
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