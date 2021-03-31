package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.common.StringeeConstant;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon.ScalingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel;

import static com.stringee.stringeeflutterplugin.StringeeAudioManager.AudioDevice;
import static com.stringee.stringeeflutterplugin.StringeeAudioManager.AudioManagerEvents;
import static com.stringee.stringeeflutterplugin.StringeeAudioManager.create;
import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.CallEvent;

public class StringeeCallManager implements StringeeCall.StringeeCallListener {
    private static StringeeCallManager _callManager;
    private static Context _context;
    private static StringeeCall _call;
    private static StringeeClient _client;
    private StringeeAudioManager _audioManager;
    private static StringeeManager _stringeeManager;
    private MethodChannel.Result makeCallResult;
    private static Handler _handler;
    private static final String TAG = "Stringee sdk";
    private MediaState _mediaState;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private boolean isResumeVideo = false;

    public static synchronized StringeeCallManager getInstance(Context context, StringeeManager stringeeManager, Handler handler) {
        if (_callManager == null) {
            _callManager = new StringeeCallManager();
            _context = context;
            _stringeeManager = stringeeManager;
            _handler = handler;
        }
        return _callManager;
    }

    /**
     * Make a call
     *
     * @param from
     * @param to
     * @param isVideoCall
     * @param customData
     * @param videoResolution
     */
    public void makeCall(String from, String to, final boolean isVideoCall, String customData, String videoResolution, MethodChannel.Result result) {
        _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected.");
            result.success(map);
            return;
        }
        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        _call = new StringeeCall(_client, from, to);
        _call.setVideoCall(isVideoCall);
        if (customData != null) {
            _call.setCustom(customData);
        }
        if (videoResolution != null) {
            if (videoResolution.equalsIgnoreCase("NORMAL")) {
                _call.setQuality(StringeeConstant.QUALITY_NORMAL);
            } else if (videoResolution.equalsIgnoreCase("HD")) {
                _call.setQuality(StringeeConstant.QUALITY_HD);
            } else if (videoResolution.equalsIgnoreCase("FULLHD")) {
                _call.setQuality(StringeeConstant.QUALITY_FULLHD);
            }
        }
        _call.setCallListener(this);
        _stringeeManager.getCallsMap().put(_call.getCallId(), _call);
        makeCallResult = result;

        _audioManager = create(_context);
        _audioManager.start(new AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<AudioDevice> audioDeviceList = new ArrayList<AudioDevice>();
                        audioDeviceList.addAll(availableAudioDevices);
                        List<Short> codeList = new ArrayList<Short>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).getValue());
                        }
                        Map map = new HashMap();
                        map.put("nativeEventType", CallEvent.getValue());
                        map.put("event", "didChangeAudioDevice");
                        Map bodyMap = new HashMap();
                        bodyMap.put("code", selectedAudioDevice.getValue());
                        bodyMap.put("codeList", codeList);
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin._eventSink.success(map);
                    }
                });
            }
        });
        _call.makeCall();
    }

    /**
     * Init an answer
     *
     * @param callId
     * @param result
     */
    public void initAnswer(String callId, MethodChannel.Result result) {
        _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }
        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        _call = _stringeeManager.getCallsMap().get(callId);
        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        _call.setCallListener(this);
        _call.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Send ringing success");
            }
        });

        _audioManager = create(_context);
        _audioManager.start(new AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<AudioDevice> audioDeviceList = new ArrayList<AudioDevice>();
                        audioDeviceList.addAll(availableAudioDevices);
                        List<Short> codeList = new ArrayList<Short>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).getValue());
                        }
                        Map map = new HashMap();
                        map.put("nativeEventType", CallEvent.getValue());
                        map.put("event", "didChangeAudioDevice");
                        Map bodyMap = new HashMap();
                        bodyMap.put("code", selectedAudioDevice.getValue());
                        bodyMap.put("codeList", codeList);
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin._eventSink.success(map);
                    }
                });
            }
        });

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Answer a call
     *
     * @param callId
     * @param result
     */
    public void answer(String callId, MethodChannel.Result result) {
        _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        _call.answer();

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * End call
     *
     * @param callId
     * @param result
     */
    public void hangup(String callId, MethodChannel.Result result) {
        _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        if (_audioManager != null) {
            _audioManager.stop();
            _audioManager = null;
        }


        _call.hangup();
        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Reject a call
     *
     * @param callId
     * @param result
     */
    public void reject(String callId, MethodChannel.Result result) {
        _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        if (_audioManager != null) {
            _audioManager.stop();
            _audioManager = null;
        }

        _call.reject();
        _mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Send a DTMF
     *
     * @param callId
     * @param dtmf
     * @param result
     */
    public void sendDtmf(String callId, String dtmf, final MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }
        _call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(final StringeeError error) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", error.getCode());
                        map.put("message", error.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Send call info
     *
     * @param callId
     * @param callInfo
     * @param result
     */
    public void sendCallInfo(String callId, Map callInfo, MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = Utils.convertMapToJson(callInfo);
            _call.sendCallInfo(jsonObject);
            Map map = new HashMap();
            map.put("status", true);
            map.put("code", 0);
            map.put("message", "Success");
            result.success(map);
        } catch (JSONException e) {
            e.printStackTrace();
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
        }
    }

    /**
     * Mute or unmute
     *
     * @param callId
     * @param mute
     * @param result
     */
    public void mute(String callId, boolean mute, MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }
        _call.mute(mute);
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * enable Video
     *
     * @param callId
     * @param isVideoEnable
     * @param result
     */
    public void enableVideo(String callId, boolean isVideoEnable, MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }
        _call.enableVideo(isVideoEnable);
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Set speaker on/off
     *
     * @param callId
     * @param on
     * @param result
     */
    public void setSpeakerphoneOn(String callId, boolean on, MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        if (_audioManager != null) {
            _audioManager.setSpeakerphoneOn(on);
        }

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Switch Camera
     *
     * @param callId
     * @param isMirror
     * @param result
     */
    public void switchCamera(String callId, final boolean isMirror, final MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        _call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _call.getLocalView().setMirror(isMirror);
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Resume Video
     *
     * @param callId
     * @param result
     */
    public void resumeVideo(String callId, MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        isResumeVideo = true;
        _call.resumeVideo();

        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Get call statistic
     *
     * @param callId
     * @param result
     */
    public void getCallStats(String callId, final MethodChannel.Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.length() == 0) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        if (_call == null) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -4);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        _call.getStats(new StringeeCall.CallStatsListener() {
            @Override
            public void onCallStats(StringeeCall.StringeeCallStats stringeeCallStats) {
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

    @Override
    public void onSignalingStateChange(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, String s, int i, String s1) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========SignalingStateChange==========\n" + "signalingState: " + signalingState);
                if (signalingState == StringeeCall.SignalingState.CALLING) {
                    _stringeeManager.getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
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
                    if (stringeeCall.isAppToPhoneCall()) {
                        callType = 2;
                    } else if (stringeeCall.isPhoneToAppCall()) {
                        callType = 3;
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

                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
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
    public void onError(StringeeCall stringeeCall, final int code, final String message) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========Error==========\n" + "code: " + code + " -message: " + message);
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
    public void onHandledOnAnotherDevice(final StringeeCall stringeeCall, final StringeeCall.SignalingState signalingState, final String description) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========HandledOnAnotherDevice==========\n" + "signalingState: " + signalingState + " -description: " + description);
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
    public void onMediaStateChange(final StringeeCall stringeeCall, final MediaState mediaState) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _mediaState = mediaState;
                Log.d(TAG, "==========MediaStateChange==========\n" + "mediaState: " + mediaState);
                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
                map.put("event", "didChangeMediaState");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                bodyMap.put("code", mediaState.getValue());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);

                if (_mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
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
    public void onLocalStream(final StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "==========ReceiveLocalStream==========");
                    Map map = new HashMap();
                    map.put("nativeEventType", CallEvent.getValue());
                    map.put("event", "didReceiveLocalStream");
                    Map bodyMap = new HashMap();
                    bodyMap.put("callId", stringeeCall.getCallId());
                    map.put("body", bodyMap);
                    if (isResumeVideo) {
                        Map<String, Object> localViewOptions = _stringeeManager.getLocalViewOptions().get(stringeeCall.getCallId());
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
    public void onRemoteStream(final StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (stringeeCall.isVideoCall()) {
                    Log.d(TAG, "==========ReceiveRemoteStream========== ");
                    if (_mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
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
    public void onCallInfo(final StringeeCall stringeeCall, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveCallInfo==========\n" + jsonObject.toString());
                Map map = new HashMap();
                map.put("nativeEventType", CallEvent.getValue());
                map.put("event", "didReceiveCallInfo");
                Map bodyMap = new HashMap();
                bodyMap.put("callId", stringeeCall.getCallId());
                try {
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
