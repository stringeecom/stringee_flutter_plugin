package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeManager {
    private static StringeeManager instance;
    private Context context;
    private Map<String, ClientWrapper> clientMap = new HashMap<>();
    private Map<String, CallWrapper> callsMap = new HashMap<>();
    private Map<String, Call2Wrapper> call2sMap = new HashMap<>();
    private Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private Map<String, Map<String, Object>> remoteViewOption = new HashMap<>();
    private Map<String, VideoTrackManager> tracksMap = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private StringeeAudioManager audioManager;
    private ScreenCaptureManager captureManager;

    private static final String TAG = "StringeeSDK";

    public enum StringeeCallType {
        AppToAppOutgoing(0),
        AppToAppIncoming(1),
        AppToPhone(2),
        PhoneToApp(3);

        public final short value;

        StringeeCallType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public enum StringeeEventType {
        ClientEvent(0),
        CallEvent(1),
        Call2Event(2),
        ChatEvent(3),
        RoomEvent(4);

        public final short value;

        StringeeEventType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public enum UserRole {
        Admin(0),
        Member(1);

        public final short value;

        UserRole(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public static synchronized StringeeManager getInstance() {
        if (instance == null) {
            instance = new StringeeManager();
        }

        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Map<String, ClientWrapper> getClientMap() {
        return clientMap;
    }

    public Map<String, CallWrapper> getCallsMap() {
        return callsMap;
    }

    public Map<String, Call2Wrapper> getCall2sMap() {
        return call2sMap;
    }

    public Map<String, Map<String, Object>> getLocalViewOptions() {
        return localViewOption;
    }

    public Map<String, Map<String, Object>> getRemoteViewOptions() {
        return remoteViewOption;
    }

    public Map<String, VideoTrackManager> getTracksMap() {
        return tracksMap;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void startAudioManager(short eventType, String clientId) {
        audioManager = StringeeAudioManager.create(context);
        audioManager.start(new AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                        List<AudioDevice> audioDeviceList = new ArrayList<>();
                        audioDeviceList.addAll(availableAudioDevices);
                        List<Integer> codeList = new ArrayList<>();
                        for (int i = 0; i < audioDeviceList.size(); i++) {
                            codeList.add(audioDeviceList.get(i).ordinal());
                        }
                        Map map = new HashMap();
                        map.put("nativeEventType", eventType);
                        map.put("event", "didChangeAudioDevice");
                        map.put("uuid", clientId);
                        Map bodyMap = new HashMap();
                        bodyMap.put("code", selectedAudioDevice.ordinal());
                        bodyMap.put("codeList", codeList);
                        map.put("body", bodyMap);
                        StringeeFlutterPlugin.eventSink.success(map);
                    }
                });
            }
        });
    }

    public void stopAudioManager() {
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
    }

    public ScreenCaptureManager getCaptureManager() {
        return captureManager;
    }

    public void setCaptureManager(ScreenCaptureManager captureManager) {
        this.captureManager = captureManager;
    }

    /**
     * Set speaker on/off
     *
     * @param on
     * @param result
     */
    public void setSpeakerphoneOn(boolean on, Result result) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
            Log.d("StringeeSDK", "setSpeakerphoneOn: success");
            Map map = new HashMap();
            map.put("status", true);
            map.put("code", 0);
            map.put("message", "Success");
            result.success(map);
        } else {
            Log.d("StringeeSDK", "setSpeakerphoneOn: false - -2 - AudioManager is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "AudioManager is not found");
            result.success(map);
        }
    }

    /**
     * Set speaker on/off
     *
     * @param on
     */
    public void setSpeakerphoneOn(boolean on) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
        }
    }
}