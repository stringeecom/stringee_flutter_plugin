package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;

import org.webrtc.RendererCommon.ScalingType;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

public class StringeeVideoView implements PlatformView {
    private FrameLayout frameLayout;
    private static final String TAG = "Stringee";
    private Handler handler = new Handler(Looper.getMainLooper());

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

    private void renderView(final FrameLayout frameLayout, final String callId, final boolean isLocal, final boolean isOverlay, final boolean isMirror) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "renderView: callId" + callId);
                if (callId == null || callId.length() == 0) {
                    return;
                }

                StringeeCall call = StringeeManager.getInstance().getCallsMap().get(callId);
                StringeeCall2 call2 = StringeeManager.getInstance().getCall2sMap().get(callId);
                if (call == null && call2 == null) {
                    return;
                }

                frameLayout.removeAllViews();
                frameLayout.setBackgroundColor(Color.BLACK);
                if (isLocal) {
                    if (call != null) {
                        call.getLocalView().setScalingType(ScalingType.SCALE_ASPECT_FIT);
                        frameLayout.addView(call.getLocalView());
                        call.renderLocalView(isOverlay);
                        call.getLocalView().setMirror(isMirror);
                    } else {
                        call2.getLocalView().setScalingType(ScalingType.SCALE_ASPECT_FIT);
                        frameLayout.addView(call2.getLocalView());
                        call2.renderLocalView(isOverlay);
                        call2.getLocalView().setMirror(isMirror);
                    }
                } else {
                    if (call != null) {
                        call.getRemoteView().setScalingType(ScalingType.SCALE_ASPECT_FIT);
                        frameLayout.addView(call.getRemoteView());
                        call.renderRemoteView(isOverlay);
                        call.getRemoteView().setMirror(isMirror);
                    } else {
                        call2.getRemoteView().setScalingType(ScalingType.SCALE_ASPECT_FIT);
                        frameLayout.addView(call2.getRemoteView());
                        call2.renderRemoteView(isOverlay);
                        call2.getRemoteView().setMirror(isMirror);
                    }
                }
            }
        });
    }
}
