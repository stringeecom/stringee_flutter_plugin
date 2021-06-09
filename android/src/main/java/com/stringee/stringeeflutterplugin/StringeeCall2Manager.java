package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.CallStatsListener;
import com.stringee.call.StringeeCall2.StringeeCallStats;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeCall2Manager {
    private static StringeeCall2Manager _callManager;
    private static StringeeClient _client;
    private static StringeeManager _manager;
    private static Context _context;
    private static Handler _handler;

    private static final String TAG = "StringeeSDK";

    public static synchronized StringeeCall2Manager getInstance(Context context) {
        if (_callManager == null) {
            _callManager = new StringeeCall2Manager();
            _context = context;
            _manager = StringeeManager.getInstance();
            _handler = _manager.getHandler();
            _client = _manager.getClient();
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
    public void makeCall(String from, String to, final boolean isVideoCall, String customData, String videoResolution, final Result result) {
        _client = _manager.getClient();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "makeCall: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        StringeeCall2 call = new StringeeCall2(_client, from, to);
        call.setVideoCall(isVideoCall);
        if (customData != null) {
            call.setCustom(customData);
        }

        MStringeeCall2 mCall2 = new MStringeeCall2(call, result);
        mCall2.createAudioManager(_context);
        mCall2.prepareCall();
        mCall2.makeCall();
        _manager.getCall2sMap().put(call.getCallId(), mCall2);
    }

    /**
     * Init an answer
     *
     * @param callId
     * @param result
     */
    public void initAnswer(String callId, final Result result) {
        _client = _manager.getClient();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "initAnswer: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }
        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "initAnswer: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "initAnswer: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.createAudioManager(_context);
        call.prepareCall();
        call.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
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
     * @param callId
     * @param result
     */
    public void answer(String callId, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "answer: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Map map = new HashMap();
            Log.d(TAG, "answer: false - -2 - callId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Map map = new HashMap();
            Log.d(TAG, "answer: false - -3 - StringeeCall2 is not found");
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.answer();
        Log.d(TAG, "answer: success");
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
    public void hangup(String callId, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "hangup: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "hangup: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "hangup: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.hangup();
        _manager.getCallsMap().put(callId, null);
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
     * @param callId
     * @param result
     */
    public void reject(String callId, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "reject: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "reject: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "reject: false - -3 - StringeeCall is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.reject();
        _manager.getCallsMap().put(callId, null);
        Log.d(TAG, "reject: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Send call info
     *
     * @param callId
     * @param callInfo
     * @param result
     */
    public void sendCallInfo(String callId, Map callInfo, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "sendCallInfo: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "sendCallInfo: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "sendCallInfo: false - -3 - StringeeCall is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        try {
            JSONObject jsonObject = Utils.convertMapToJson(callInfo);
            call.sendCallInfo(jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    _handler.post(new Runnable() {
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
                    _handler.post(new Runnable() {
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
            _handler.post(new Runnable() {
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
     * @param callId
     * @param mute
     * @param result
     */
    public void mute(String callId, boolean mute, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "mute: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "mute: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "mute: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.mute(mute);
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
     * @param callId
     * @param isVideoEnable
     * @param result
     */
    public void enableVideo(String callId, boolean isVideoEnable, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "enableVideo: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "enableVideo: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "enableVideo: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.enableVideo(isVideoEnable);
        Log.d(TAG, "enableVideo: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Set speaker on/off
     *
     * @param on
     * @param result
     */
    public void setSpeakerphoneOn(boolean on, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        _manager.setSpeakerphoneOn(on, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "setSpeakerphoneOn: success");
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
                        Log.d(TAG, "setSpeakerphoneOn: false - " + error.getCode() + " - " + error.getMessage());
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
     * Switch Camera
     *
     * @param callId
     * @param result
     */
    public void switchCamera(String callId, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "switchCamera: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "switchCamera: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
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
     * Resume Video
     *
     * @param callId
     * @param result
     */
    public void resumeVideo(String callId, Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "resumeVideo: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "resumeVideo: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "resumeVideo: false - -3 - StringeeCall2 is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.resumeVideo();
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
     * @param callId
     * @param result
     */
    public void getCallStats(String callId, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "getCallStats: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "getCallStats: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "getCallStats: false - -3 - StringeeCall is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall2 is not found");
            result.success(map);
            return;
        }

        call.getStats(new CallStatsListener() {
            @Override
            public void onCallStats(final StringeeCallStats stringeeCallStats) {
                _handler.post(new Runnable() {
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

    public void setMirror(String callId, boolean isLocal, boolean isMirror, final Result result) {
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "setMirror: false - -1 - StringeeClient is not initialized or disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (callId == null || callId.isEmpty()) {
            Log.d(TAG, "setMirror: false - -2 - callId is invalid");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "callId is invalid");
            result.success(map);
            return;
        }

        MStringeeCall2 call = _manager.getCall2sMap().get(callId);
        if (call == null) {
            Log.d(TAG, "setMirror: false - -3 - StringeeCall is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "StringeeCall is not found");
            result.success(map);
            return;
        }

        if (isLocal) {
            call.getLocalView().setMirror(isMirror);
        } else {
            call.getRemoteView().setMirror(isMirror);
        }

        Log.d(TAG, "setMirror: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }
}