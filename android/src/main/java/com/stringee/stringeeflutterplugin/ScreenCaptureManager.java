package com.stringee.stringeeflutterplugin;

import android.annotation.SuppressLint;

import com.stringee.video.StringeeScreenCapture;
import com.stringee.video.StringeeScreenCapture.Builder;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

@SuppressLint("NewApi")
public class ScreenCaptureManager {
    private static ScreenCaptureManager instance;
    private final StringeeScreenCapture screenCapture;
    private ActivityResultListener listener;

    private ScreenCaptureManager(ActivityPluginBinding binding) {
        binding.addActivityResultListener((requestCode, resultCode, data) -> {
            if (listener != null) {
                listener.onActivityResult(requestCode, resultCode, data);
            }
            return false;
        });

        screenCapture = new Builder().buildWithActivity(binding.getActivity());
    }

    public static ScreenCaptureManager getInstance(ActivityPluginBinding binding) {
        if (instance == null) {
            instance = new ScreenCaptureManager(binding);
        }
        return instance;
    }

    public void getActivityResult(ActivityResultListener listener) {
        this.listener = listener;
    }

    public StringeeScreenCapture getScreenCapture() {
        return screenCapture;
    }
}
