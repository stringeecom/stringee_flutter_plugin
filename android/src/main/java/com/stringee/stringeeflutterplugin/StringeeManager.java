package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.stringee.StringeeClient;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import java.util.HashMap;
import java.util.Map;

public class StringeeManager {
    private static StringeeManager stringeeManager;
    private StringeeClient mClient;
    private Map<String, MStringeeCall> callsMap = new HashMap<>();
    private Map<String, MStringeeCall2> call2sMap = new HashMap<>();
    private Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private StringeeAudioManager audioManager;

    public enum StringeeEventType {
        ClientEvent(0),
        CallEvent(1),
        Call2Event(2);

        public final short value;

        StringeeEventType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public static synchronized StringeeManager getInstance() {
        if (stringeeManager == null) {
            stringeeManager = new StringeeManager();
        }

        return stringeeManager;
    }

    public StringeeClient getClient() {
        return mClient;
    }

    public void setClient(StringeeClient mClient) {
        this.mClient = mClient;
    }

    public Map<String, MStringeeCall2> getCall2sMap() {
        return call2sMap;
    }

    public Map<String, Map<String, Object>> getLocalViewOptions() {
        return localViewOption;
    }

    public Map<String, MStringeeCall> getCallsMap() {
        return callsMap;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
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

    public void setSpeakerphoneOn(boolean on, StatusListener listener) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
            listener.onSuccess();
        } else {
            listener.onError(new StringeeError(-2, "AudioManager is not found"));
        }
    }
}