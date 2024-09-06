package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.util.Log;

import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeManager {
    private static volatile StringeeManager instance;
    private Context context;
    private final Map<String, ClientWrapper> clientMap = new HashMap<>();
    private final Map<String, CallWrapper> callsMap = new HashMap<>();
    private final Map<String, Call2Wrapper> call2sMap = new HashMap<>();
    private final Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private final Map<String, Map<String, Object>> remoteViewOption = new HashMap<>();
    private final Map<String, VideoTrackManager> tracksMap = new HashMap<>();
    private StringeeAudioManager audioManager;
    private ScreenCaptureManager captureManager;

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
            synchronized (StringeeManager.class) {
                if (instance == null) {
                    instance = new StringeeManager();
                }
            }
        }

        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
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

    public void startAudioManager(Context context, AudioManagerEvents events) {
        audioManager = StringeeAudioManager.create(context);
        audioManager.start(events);
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
     * @param on     true: on, false: off
     * @param result result
     */
    public void setSpeakerphoneOn(boolean on, Result result) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
            Map<String, Object> map = Utils.createSuccessMap("setSpeakerphoneOn");
            result.success(map);
        } else {
            Map<String, Object> map = Utils.createNotFoundErrorMap("setSpeakerphoneOn", "AudioManager");
            result.success(map);
        }
    }

    /**
     * Set speaker on/off
     *
     * @param on true: on, false: off
     */
    public void setSpeakerphoneOn(boolean on) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
        }
    }
}