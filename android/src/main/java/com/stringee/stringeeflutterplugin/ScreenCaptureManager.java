package com.stringee.stringeeflutterplugin;

import android.annotation.SuppressLint;

import com.stringee.video.StringeeScreenCapture;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

/** Coordinates Android screen-capture permission results with Stringee screen capture. */
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

        screenCapture = new StringeeScreenCapture(binding.getActivity());
    }

    /** Returns the singleton associated with the current Flutter activity binding. */
    public static ScreenCaptureManager getInstance(ActivityPluginBinding binding) {
        if (instance == null) {
            instance = new ScreenCaptureManager(binding);
        }
        return instance;
    }

    /** Sets the listener that receives the next screen-capture permission result. */
    public void getActivityResult(ActivityResultListener listener) {
        this.listener = listener;
    }

    /** Returns the native Stringee screen-capture source. */
    public StringeeScreenCapture getScreenCapture() {
        return screenCapture;
    }
}
