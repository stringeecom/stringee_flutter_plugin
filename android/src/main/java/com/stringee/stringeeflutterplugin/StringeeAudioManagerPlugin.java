package com.stringee.stringeeflutterplugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.common.StringeeAudioManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeAudioManagerPlugin implements MethodCallHandler, EventChannel.StreamHandler, StringeeAudioManager.AudioManagerEvents {
    private static volatile StringeeAudioManagerPlugin instance;
    private EventSink eventSink;
    private Context context;
    private final List<StringeeAudioManager.AudioDevice> audioDevices = new ArrayList<>();

    private StringeeAudioManager audioManager;
    private static final String TAG = "StringeeSDK";

    public StringeeAudioManagerPlugin() {
    }

    static void initialize(Context context) {
        getInstance().setContext(context.getApplicationContext());
    }

    public static StringeeAudioManagerPlugin getInstance() {
        if (instance == null) {
            synchronized (StringeeAudioManagerPlugin.class) {
                if (instance == null) {
                    instance = new StringeeAudioManagerPlugin();
                }
            }
        }
        return instance;
    }

    @Override
    public void onListen(Object arguments, EventSink events) {
        this.eventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "start":
                Utils.post(() -> {
                    if (audioManager == null) {
                        audioManager = new StringeeAudioManager(context);
                    }
                    audioManager.start(this);
                    Log.d(TAG, "start: true - 0 - Start audio manager success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Start audio manager success");
                    result.success(map);
                });
                break;
            case "stop":
                Utils.post(() -> {
                    if (audioManager != null) {
                        audioManager.stop();
                    }
                    audioManager = null;
                    Log.d(TAG, "start: true - 0 - Stop audio manager success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Stop audio manager success");
                    result.success(map);
                });
                break;
            case "selectDevice":
                Utils.post(() -> {
                    Map<String, Object> map = new HashMap<>();
                    if (audioManager != null) {
                        Integer deviceType = call.argument("deviceType");
                        if (deviceType != null && deviceType != 0) {
                            if (deviceType == 1) {
                                audioManager.setSpeakerphoneOn(true);
                                audioManager.setBluetoothScoOn(false);
                            } else if (deviceType == 2 || deviceType == 3) {
                                audioManager.setSpeakerphoneOn(false);
                                audioManager.setBluetoothScoOn(false);
                            } else if (deviceType == 4) {
                                audioManager.setSpeakerphoneOn(false);
                                audioManager.setBluetoothScoOn(true);
                            }
                            Log.d(TAG, "selectDevice: true - 0 - Select audio device success");
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Select audio device success");
                        } else {
                            Log.d(TAG, "selectDevice: false - 1 - Invalid device type");
                            map.put("status", false);
                            map.put("code", -1);
                            map.put("message", "Invalid device type");
                        }
                    } else {
                        Log.d(TAG, "selectDevice: false - 2 - Audio manager is not started");
                        map.put("status", false);
                        map.put("code", -2);
                        map.put("message", "Audio manager is not started");
                    }
                    result.success(map);
                });
                break;
        }
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice, Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
        audioDevices.clear();
        audioDevices.add(StringeeAudioManager.AudioDevice.SPEAKER_PHONE);
        if (availableAudioDevices.contains(StringeeAudioManager.AudioDevice.BLUETOOTH)) {
            audioDevices.add(StringeeAudioManager.AudioDevice.BLUETOOTH);
        }
        if (availableAudioDevices.contains(StringeeAudioManager.AudioDevice.WIRED_HEADSET)) {
            audioDevices.add(StringeeAudioManager.AudioDevice.WIRED_HEADSET);
        } else {
            if (hasEarpiece()) {
                audioDevices.add(StringeeAudioManager.AudioDevice.EARPIECE);
            }
        }
        Log.d(TAG, "onAudioManagerDevicesChanged: " + audioDevices + ", " + "selected: " + selectedAudioDevice);
        List<Integer> codeList = new ArrayList<>();
        for (int i = 0; i < audioDevices.size(); i++) {
            codeList.add(audioDevices.get(i).ordinal());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("code", selectedAudioDevice.ordinal());
        map.put("codeList", codeList);
        eventSink.success(map);
    }

    @SuppressLint("NewApi")
    private boolean hasEarpiece() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
}
