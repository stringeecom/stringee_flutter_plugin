package com.stringee.stringeeflutterplugin.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.stringee.common.StringeeAudioManager;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.Utils;

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

public class AudioManager implements MethodCallHandler, EventChannel.StreamHandler, StringeeAudioManager.AudioManagerEvents {
    private static volatile AudioManager instance;
    private EventSink eventSink;
    private final Context context;
    private final List<StringeeAudioManager.AudioDevice> audioDevices = new ArrayList<>();

    private StringeeAudioManager audioManager;

    public AudioManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static AudioManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager(context);
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
                    result.success(FlutterResult.success("startAudioManager").getMap());
                });
                break;
            case "stop":
                Utils.post(() -> {
                    if (audioManager != null) {
                        audioManager.stop();
                    }
                    audioManager = null;
                    result.success(FlutterResult.success("stopAudioManager").getMap());
                });
                break;
            case "selectDevice":
                Utils.post(() -> {
                    if (audioManager != null) {
                        Map<String, Object> deviceMap = call.argument("device");
                        if (deviceMap == null) {
                            result.success(FlutterResult.error("selectDevice", -1, "Invalid device")
                                    .getMap());
                            return;
                        }
                        Integer device = (Integer) deviceMap.get("type");
                        if (device != null && device < 4 && device >= 0) {
                            if (device == 0) {
                                audioManager.setSpeakerphoneOn(true);
                                audioManager.setBluetoothScoOn(false);
                            } else if (device == 3) {
                                audioManager.setSpeakerphoneOn(false);
                                audioManager.setBluetoothScoOn(true);
                            } else {
                                audioManager.setSpeakerphoneOn(false);
                                audioManager.setBluetoothScoOn(false);
                            }
                            result.success(FlutterResult.success("selectDevice").getMap());
                        } else {
                            result.success(
                                    FlutterResult.error("selectDevice", -1, "Invalid device type")
                                            .getMap());
                        }
                    } else {
                        result.success(FlutterResult.error("selectDevice", -2,
                                "Audio manager is not started").getMap());
                    }
                });
                break;
        }
    }

    @Override
    public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice,
                                     Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
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
        Log.d(Constants.TAG, "onAudioManagerDevicesChanged: " + audioDevices + ", " + "selected: " +
                selectedAudioDevice);
        List<Map<String, Object>> devices = new ArrayList<>();
        for (int i = 0; i < audioDevices.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", audioDevices.get(i).ordinal());
            devices.add(map);
        }

        Map<String, Object> device = new HashMap<>();
        device.put("type", selectedAudioDevice.ordinal());

        Map<String, Object> map = new HashMap<>();
        map.put("device", device);
        map.put("devices", devices);
        eventSink.success(map);
    }

    @SuppressLint("NewApi")
    private boolean hasEarpiece() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
}
