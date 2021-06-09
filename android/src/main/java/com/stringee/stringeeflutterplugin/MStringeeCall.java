package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.CallStatsListener;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.call.StringeeCall.SignalingState;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
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

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.CallEvent;

public class MStringeeCall implements StringeeCall.StringeeCallListener {
    private StringeeClient _client;
    private StringeeCall _call;
    private StringeeManager _manager;
    private Result _makeCallResult;
    private Handler _handler;
    private MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;
    private SignalingState _signalingState;

    private static final String TAG = "StringeeSDK";

    public MStringeeCall(StringeeCall call) {
        _call = call;
        _manager = StringeeManager.getInstance();
        _client = _manager.getClient();
        _handler = _manager.getHandler();
    }

    public MStringeeCall(StringeeCall call, Result result) {
        _call = call;
        _makeCallResult = result;
        _manager = StringeeManager.getInstance();
        _client = _manager.getClient();
        _handler = _manager.getHandler();
    }

    public StringeeCall getCall() {
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
            public void onAudioDeviceChanged(final StringeeAudioManager.AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                _handler.post(new Runnable() {
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

    public void sendDTMF(String dtmf, StatusListener statusListener) {
        _call.sendDTMF(dtmf, statusListener);
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

    public void getStats(CallStatsListener listener) {
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
    public void onSignalingStateChange(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String s, int i, String s1) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _signalingState = signalingState;
                _call = stringeeCall;
                if (_signalingState == StringeeCall.SignalingState.CALLING) {
                    Log.d(TAG, "makeCall: success");
                    _manager.getCallsMap().put(_call.getCallId(), MStringeeCall.this);
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    Map callInfoMap = new HashMap();
                    callInfoMap.put("callId", _call.getCallId());
                    callInfoMap.put("from", _call.getFrom());
                    callInfoMap.put("to", _call.getTo());
                    callInfoMap.put("fromAlias", _call.getFromAlias());
                    callInfoMap.put("toAlias", _call.getToAlias());
                    callInfoMap.put("isVideocall", _call.isVideoCall());
                    int callType = 0;
                    if (!_call.getFrom().equals(_client.getUserId())) {
                        callType = 1;
                    }
                    if (_call.isAppToPhoneCall()) {
                        callType = 2;
                    } else if (_call.isPhoneToAppCall()) {
                        callType = 3;
                    }
                    callInfoMap.put("callType", callType);
                    callInfoMap.put("isVideoCall", _call.isVideoCall());
                    callInfoMap.put("customDataFromYourServer", _call.getCustomDataFromYourServer());
                    map.put("callInfo", callInfoMap);
                    if (_makeCallResult != null) {
                        _makeCallResult.success(map);
                        _makeCallResult = null;
                    }
                }

                Log.d(TAG, "onSignalingStateChange: " + _signalingState);
                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
                map.put("event", "didChangeSignalingState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", _call.getCallId());
                bodyMap.put("code", _signalingState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onError(StringeeCall stringeeCall, int code, String message) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onError: code: " + code + " -message: " + message);
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
    public void onHandledOnAnotherDevice(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String description) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onHandledOnAnotherDevice:" + "\nsignalingState: " + signalingState + " - description: " + description);
                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
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
    public void onMediaStateChange(StringeeCall stringeeCall, StringeeCall.MediaState mediaState) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(TAG, "onMediaStateChange: " + mediaState);
                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
                map.put("event", "didChangeMediaState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);

                if (_mediaState == StringeeCall.MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
                    remoteStreamShowed = true;
                    Map map1 = new HashMap();
                    map1.put("nativeEventType", CallEvent.getValue());
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
    public void onLocalStream(StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onLocalStream");
                    Map map = new HashMap();
                    map.put("nativeEventType", CallEvent.getValue());
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
    public void onRemoteStream(StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "onRemoteStream");
                    if (_mediaState == StringeeCall.MediaState.CONNECTED && !remoteStreamShowed) {
                        remoteStreamShowed = true;
                        Map map = new HashMap();
                        map.put("nativeEventType", CallEvent.getValue());
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
    public void onCallInfo(StringeeCall stringeeCall, JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onCallInfo: " + jsonObject.toString());
                    Map map = new HashMap();
                    map.put("nativeEventType", CallEvent.getValue());
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
