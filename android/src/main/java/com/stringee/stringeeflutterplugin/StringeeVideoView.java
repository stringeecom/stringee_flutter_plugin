package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_BALANCED;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT;

public class StringeeVideoView implements PlatformView {
    private FrameLayout frameLayout;
    private static final String TAG = "Stringee sdk";
    private Handler handler = new Handler(Looper.getMainLooper());

    StringeeVideoView(@NonNull Context context, int id, @Nullable Map<String, Object> creationParams) {
        try {
            frameLayout = new FrameLayout(context);
            String callId = (String) creationParams.get("callId");

            if (callId == null || callId.length() == 0) {
                return;
            }

            renderView(frameLayout, callId, creationParams);
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

    private void renderView(final FrameLayout layout, final String callId, final Map<String, Object> creationParams) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
                Call2Wrapper call2 = StringeeManager.getInstance().getCall2sMap().get(callId);

                if (call == null && call2 == null) {
                    return;
                }

                boolean isLocal = (Boolean) creationParams.get("isLocal");
                boolean isMirror = false;

                ScalingType scalingType = null;
                if (creationParams.get("scalingType").equals("FILL")) {
                    scalingType = SCALE_ASPECT_FILL;
                } else if (creationParams.get("scalingType").equals("FIT")) {
                    scalingType = SCALE_ASPECT_FIT;
                } else if (creationParams.get("scalingType").equals("BALANCED")) {
                    scalingType = SCALE_ASPECT_BALANCED;
                }

                if (creationParams.containsKey("isMirror")) {
                    isMirror = (Boolean) creationParams.get("isMirror");
                }

                boolean isOverlay = (Boolean) creationParams.get("isOverlay");
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                    isOverlay = true;
                }

                layout.removeAllViews();
                layout.setBackgroundColor(Color.BLACK);
                if (isLocal) {
                    if (call != null) {
                        call.getLocalView().setScalingType(scalingType);
                        layout.addView(call.getLocalView());
                        call.renderLocalView(isOverlay);
                        call.getLocalView().setMirror(isMirror);
                    } else {
                        call2.getLocalView().setScalingType(scalingType);
                        layout.addView(call2.getLocalView());
                        call2.renderLocalView(isOverlay);
                        call2.getLocalView().setMirror(isMirror);
                    }

                    //save localView option
                    Map<String, Object> localViewOptions = new HashMap<>();
                    localViewOptions.put("isMirror", isMirror);
                    localViewOptions.put("isOverlay", isOverlay);
                    localViewOptions.put("scalingType", scalingType);
                    localViewOptions.put("layout", layout);
                    StringeeManager.getInstance().getLocalViewOptions().put(callId, localViewOptions);

                } else {
                    if (call != null) {
                        call.getRemoteView().setScalingType(scalingType);
                        layout.addView(call.getRemoteView());
                        call.renderRemoteView(isOverlay);
                        call.getRemoteView().setMirror(isMirror);
                    } else {
                        call2.getRemoteView().setScalingType(scalingType);
                        layout.addView(call2.getRemoteView());
                        call2.renderRemoteView(isOverlay);
                        call2.getRemoteView().setMirror(isMirror);
                    }
                }
            }
        });
    }
}