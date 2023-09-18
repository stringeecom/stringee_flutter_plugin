package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeManager {
    private static volatile StringeeManager instance;
    private static final Object lock = new Object();
    private Context context;
    private Map<String, ClientWrapper> clientMap = new HashMap<>();
    private Map<String, CallWrapper> callsMap = new HashMap<>();
    private Map<String, Call2Wrapper> call2sMap = new HashMap<>();
    private Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private Map<String, Map<String, Object>> remoteViewOption = new HashMap<>();
    private Map<String, VideoTrackManager> tracksMap = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private StringeeAudioManager audioManager;
    private ActivityPluginBinding binding;

    public enum StringeeEventType {
        ClientEvent(0), CallEvent(1), Call2Event(2), ChatEvent(3), RoomEvent(4);

        public final short value;

        StringeeEventType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    public static synchronized StringeeManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
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

    public Map<String, Map<String, Object>> getLocalViewOption() {
        return localViewOption;
    }

    public Map<String, Map<String, Object>> getRemoteViewOption() {
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

    public ActivityPluginBinding getBinding() {
        return binding;
    }

    public void setBinding(ActivityPluginBinding binding) {
        this.binding = binding;
    }

    public Activity getActivity() {
        return binding.getActivity();
    }

    public Resources getResources() {
        return binding.getActivity().getResources();
    }

    public String getPackageName() {
        return binding.getActivity().getPackageName();
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
        return ScreenCaptureManager.getInstance();
    }

    /**
     * Set speaker on/off
     */
    public void setSpeakerphoneOn(boolean on, Result result) {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(on);
            Utils.sendSuccessResponse("setSpeakerphoneOn", null, result);
        } else {
            Utils.sendErrorResponse("setSpeakerphoneOn", -2, "AudioManager is not found", result);
        }
    }

    /**
     * Set bluetooth on/off
     */
    public void setBluetoothScoOn(boolean on, Result result) {
        if (audioManager != null) {
            audioManager.setBluetoothScoOn(on);
            Utils.sendSuccessResponse("setBluetoothScoOn", null, result);
        } else {
            Utils.sendErrorResponse("setBluetoothScoOn", -2, "AudioManager is not found", result);
        }
    }
}