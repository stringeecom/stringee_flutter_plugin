package com.stringee.stringeeflutterplugin;

import android.content.Intent;

import com.stringee.video.StringeeScreenCapture;
import com.stringee.video.StringeeScreenCapture.Builder;

import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class ScreenCaptureManager {
    private static ScreenCaptureManager instance;
    private StringeeScreenCapture screenCapture;
    private ActivityResultListener listener;

    public static ScreenCaptureManager getInstance() {
        if (instance == null) {
            instance = new ScreenCaptureManager();
        }
        return instance;
    }

    public void initialize() {
        StringeeManager.getInstance().getBinding().addActivityResultListener(new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (listener != null) {
                    listener.onActivityResult(requestCode, resultCode, data);
                }
                return false;
            }
        });

        screenCapture = new Builder().buildWithActivity(StringeeManager.getInstance().getActivity());
    }

    public void getActivityResult(ActivityResultListener listener) {
        this.listener = listener;
    }

    public StringeeScreenCapture getScreenCapture() {
        return screenCapture;
    }
}
