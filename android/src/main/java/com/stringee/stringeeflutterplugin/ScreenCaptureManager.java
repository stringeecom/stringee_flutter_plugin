package com.stringee.stringeeflutterplugin;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.stringee.video.StringeeScreenCapture;
import com.stringee.video.StringeeScreenCapture.Builder;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class ScreenCaptureManager {
    private static ScreenCaptureManager instance;
    private ActivityPluginBinding binding;
    private StringeeScreenCapture screenCapture;
    private ActivityResultListener listener;

    private ScreenCaptureManager(ActivityPluginBinding binding) {
        this.binding = binding;

        binding.addActivityResultListener(new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (listener != null) {
                    listener.onActivityResult(requestCode, resultCode, data);
                }
                return false;
            }
        });

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            screenCapture = new Builder().buildWithActivity(binding.getActivity());
        }
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
