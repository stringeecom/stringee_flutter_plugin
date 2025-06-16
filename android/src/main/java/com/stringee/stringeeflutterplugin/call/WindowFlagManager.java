package com.stringee.stringeeflutterplugin.call;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.stringee.stringeeflutterplugin.common.FlutterResult;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class WindowFlagManager implements MethodChannel.MethodCallHandler, ActivityAware {
    private static volatile WindowFlagManager instance;
    private Activity activity;

    public static WindowFlagManager getInstance() {
        if (instance == null) {
            synchronized (WindowFlagManager.class) {
                if (instance == null) {
                    instance = new WindowFlagManager();
                }
            }
        }
        return instance;
    }

    public void addFlag(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true);
            activity.setTurnScreenOn(true);
        }

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void clearFlag(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(false);
            activity.setTurnScreenOn(false);
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "add_window_flag": {
                if (activity == null) {
                    result.success(
                            FlutterResult.error("addWindowFlag", -1, "Not attached to activity")
                                    .getMap());
                    return;
                }
                WindowFlagManager.getInstance().addFlag(activity);
                result.success(FlutterResult.success("addWindowFlag").getMap());
                break;
            }
            case "clear_window_flag": {
                if (activity == null) {
                    result.success(
                            FlutterResult.error("clearWindowFlag", -1, "Not attached to activity")
                                    .getMap());
                    return;
                }
                WindowFlagManager.getInstance().clearFlag(activity);
                result.success(FlutterResult.success("clearWindowFlag").getMap());
                break;
            }
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }
}
