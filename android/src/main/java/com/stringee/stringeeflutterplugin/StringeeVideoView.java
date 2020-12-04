package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.call.StringeeCall;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

public class StringeeVideoView implements PlatformView {
    private FrameLayout frameLayout;
    private static final String TAG = "Stringee";

    StringeeVideoView(@NonNull Context context, int id, @Nullable Map<String, Object> creationParams) {
        try {
            frameLayout = new FrameLayout(context);
            String callId = (String) creationParams.get("callId");
            boolean isLocal = (Boolean) creationParams.get("isLocal");
            boolean isOverlay = (Boolean) creationParams.get("isOverlay");
            if (creationParams.containsKey("isMirror")) {
                boolean isMirror = (Boolean) creationParams.get("isMirror");
                renderView(frameLayout, callId, isLocal, isOverlay, isMirror);
            } else {
                renderView(frameLayout, callId, isLocal, isOverlay, false);
            }
        } catch (Exception e) {
            Log.d(TAG, "StringeeVideoView render error: " + e.getMessage());
        }

    }

    @Override
    public View getView() {
        return frameLayout;
    }

    @Override
    public void dispose() {
        frameLayout.removeAllViews();
    }

    private void renderView(FrameLayout frameLayout, String callId, boolean isLocal, boolean isOverlay, boolean isMirror) {
        Log.d(TAG, "renderView: callId" + callId);
        if (callId == null || callId.length() == 0) {
            return;
        }

        StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            return;
        }

        frameLayout.removeAllViews();
        frameLayout.setBackgroundColor(Color.BLACK);
        if (isLocal) {
            call.getLocalView().setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            frameLayout.addView(call.getLocalView());
            call.renderLocalView(isOverlay);
            call.getLocalView().setMirror(isMirror);
        } else {
            call.getRemoteView().setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            frameLayout.addView(call.getRemoteView());
            call.renderRemoteView(isOverlay);
            call.getRemoteView().setMirror(isMirror);
        }
    }
}
