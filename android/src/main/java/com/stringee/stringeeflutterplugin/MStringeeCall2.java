package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.Call2Event;
import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.CallEvent;

public class MStringeeCall2 implements StringeeCall2.StringeeCallListener {
    private StringeeClient _client;
    private StringeeCall2 _call;
    private StringeeManager _manager;
    private Result _makeCallResult;
    private android.os.Handler _handler;
    private StringeeCall2.MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;

    private static final String TAG = "StringeeSDK";

    public MStringeeCall2(StringeeCall2 call) {
        _call = call;
        _manager = StringeeManager.getInstance();
        _client = _manager.getClient();
        _handler = _manager.getHandler();
    }

    public MStringeeCall2(StringeeCall2 call, Result result) {
        _call = call;
        _makeCallResult = result;
        _manager = StringeeManager.getInstance();
        _client = _manager.getClient();
        _handler = _manager.getHandler();
    }

    public StringeeCall2 getCall() {
        return _call;
    }

    private void setCallListener() {
        _call.setCallListener(this);
    }

    public void prepareCall() {
        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        setCallListener();
    }

    public void createAudioManager(Context context) {
        _manager.startAudioManager(context, new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final StringeeAudioManager.AudioDevice selectedAudioDevice, final Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<StringeeAudioManager.AudioDevice> audioDeviceList = new ArrayList<StringeeAudioManager.AudioDevice>();
                        audioDeviceList.addAll(availableAudioDevices);
                        List<Integer> codeList = new java.util.ArrayList<Integer>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).ordinal());
                        }
                        Map map = new HashMap();
                        map.put("nativeEventType", CallEvent.getValue());
                        map.put("event", "didChangeAudioDevice");
                        Map bodyMap = new HashMap();
                        bodyMap.put("code", selectedAudioDevice.ordinal());
                        bodyMap.put("codeList", codeList);
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin._eventSink.success(map);
                    }
                });
            }
        });
    }

    public void makeCall() {
        _call.makeCall();
    }

    public void ringing(StatusListener statusListener) {
        _call.ringing(statusListener);
    }

    public void answer() {
        _call.answer();
    }

    public void hangup() {
        _manager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        _call.hangup();
    }

    public void reject() {
        _manager.stopAudioManager();

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        _call.reject();
    }

    public void sendCallInfo(JSONObject jsonObject, StatusListener statusListener) {
        _call.sendCallInfo(jsonObject, statusListener);
    }

    public void mute(boolean isMute) {
        _call.mute(isMute);
    }

    public void enableVideo(boolean enable) {
        _call.enableVideo(enable);
    }

    public void switchCamera(StatusListener statusListener) {
        _call.switchCamera(statusListener);
    }

    public void resumeVideo() {
        isResumeVideo = true;
        _call.resumeVideo();
    }

    public void getStats(StringeeCall2.CallStatsListener listener) {
        _call.getStats(listener);
    }

    public SurfaceViewRenderer getLocalView() {
        return _call.getLocalView();
    }

    public SurfaceViewRenderer getRemoteView() {
        return _call.getRemoteView();
    }

    public void renderLocalView(boolean isOverlay) {
        _call.renderLocalView(isOverlay);
    }

    public void renderRemoteView(boolean isOverlay) {
        _call.renderRemoteView(isOverlay);
    }

    @Override
    public void onSignalingStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String s, final int i, final String s1) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (signalingState == StringeeCall2.SignalingState.CALLING) {
                    Log.d(TAG, "makeCall: success");
                    _manager.getCall2sMap().put(stringeeCall.getCallId(), MStringeeCall2.this);
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
                    if (!stringeeCall.getFrom().equals(_client.getUserId())) {
                        callType = 1;
                    }
                    callInfoMap.put("callType", callType);
                    callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                    callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                    map.put("callInfo", callInfoMap);
                    if (_makeCallResult != null) {
                        _makeCallResult.success(map);
                        _makeCallResult = null;
                    }
                }

                Log.d(TAG, "onSignalingStateChange2: " + signalingState);
                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didChangeSignalingState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onError(final StringeeCall2 stringeeCall, final int code, final String message) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError2: code: " + code + " -message: " + message);
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", code);
                map.put("message", message);
                if (_makeCallResult != null) {
                    _makeCallResult.success(map);
                    _makeCallResult = null;
                }
            }
        });
    }

    @Override
    public void onHandledOnAnotherDevice(final StringeeCall2 stringeeCall, final StringeeCall2.SignalingState signalingState, final String description) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onHandledOnAnotherDevice2:" + "\nsignalingState: " + signalingState + " - description: " + description);
                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didHandleOnAnotherDevice");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", signalingState.getValue());
                bodyMap.put("description", description);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onMediaStateChange(final StringeeCall2 stringeeCall, final StringeeCall2.MediaState mediaState) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(TAG, "onMediaStateChange2: " + mediaState);
                Map map = new HashMap();
                map.put("nativeEventType", Call2Event.getValue());
                map.put("event", "didChangeMediaState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);

                if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                    remoteStreamShowed = true;
                    Map map1 = new HashMap();
                    map1.put("nativeEventType", Call2Event.getValue());
                    map1.put("event", "didReceiveRemoteStream");
                    Map bodyMap1 = new HashMap();
                    bodyMap1.put("callId", stringeeCall.getCallId());
                    map1.put("body", bodyMap1);
                    StringeeFlutterPlugin._eventSink.success(map1);
                }
            }
        });
    }

    @Override
    public void onLocalStream(final StringeeCall2 stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onLocalStream2");
                    Map map = new HashMap();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveLocalStream");
                    Map bodyMap = new HashMap();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    if (isResumeVideo) {
                        Map<String, Object> localViewOptions = _manager.getLocalViewOptions().get(stringeeCall.getCallId());
                        FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                        boolean isMirror = (Boolean) localViewOptions.get("isMirror");
                        boolean isOverlay = (Boolean) localViewOptions.get("isOverlay");
                        ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                        localView.removeAllViews();
                        stringeeCall.getLocalView().setScalingType(scalingType);
                        localView.addView(stringeeCall.getLocalView());
                        stringeeCall.renderLocalView(isOverlay);
                        stringeeCall.getLocalView().setMirror(isMirror);

                        isResumeVideo = false;
                    }
                    StringeeFlutterPlugin._eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onRemoteStream(final StringeeCall2 stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onRemoteStream2");
                    if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                        remoteStreamShowed = true;
                        Map map = new HashMap();
                        map.put("nativeEventType", Call2Event.getValue());
                        map.put("event", "didReceiveRemoteStream");
                        Map bodyMap = new HashMap();
                        bodyMap.put("callId", stringeeCall.getCallId());
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin._eventSink.success(map);
                    } else {
                        hasRemoteStream = true;
                    }
                }
            }
        });
    }

    @Override
    public void onCallInfo(final StringeeCall2 stringeeCall, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCallInfo2: " + jsonObject.toString());
                    Map map = new HashMap();
                    map.put("nativeEventType", Call2Event.getValue());
                    map.put("event", "didReceiveCallInfo");
                    Map bodyMap = new HashMap();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin._eventSink.success(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}