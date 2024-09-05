package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import com.stringee.video.StringeeScreenCapture;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class ScreenCaptureManager {
    private static ScreenCaptureManager instance;
    private final StringeeScreenCapture screenCapture;
    private final MediaProjectionManager manager;
    private final ActivityPluginBinding binding;
    private ActivityResultListener listener;

    private ScreenCaptureManager(ActivityPluginBinding binding) {
        this.binding = binding;
        this.binding.addActivityResultListener((requestCode, resultCode, data) -> {
            if (listener != null) {
                listener.onActivityResult(requestCode, resultCode, data);
            }
            return false;
        });

        screenCapture = new StringeeScreenCapture(this.binding.getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = this.binding.getActivity().getApplication().getSystemService(MediaProjectionManager.class);
        } else {
            manager = (MediaProjectionManager) this.binding.getActivity().getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
    }

    public static ScreenCaptureManager create(ActivityPluginBinding binding) {
        if (instance == null) {
            instance = new ScreenCaptureManager(binding);
        }
        return instance;
    }

    public void createCapture(int requestCode, ActivityResultListener listener) {
        this.binding.getActivity().startActivityForResult(manager.createScreenCaptureIntent(), requestCode);
        this.listener = listener;
    }

    public StringeeScreenCapture getScreenCapture() {
        return screenCapture;
    }
}
