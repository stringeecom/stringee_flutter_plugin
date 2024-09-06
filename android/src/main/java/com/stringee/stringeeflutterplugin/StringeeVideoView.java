package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;
import com.stringee.video.TextureViewRenderer;

import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

public class StringeeVideoView implements PlatformView {
    private FrameLayout frameLayout;

    StringeeVideoView(@NonNull Context context, @Nullable Map<String, Object> creationParams) {
        try {
            frameLayout = new FrameLayout(context);
            if (creationParams != null) {
                Object forCallObj = creationParams.get("forCall");
                boolean forCall = forCallObj instanceof Boolean && (boolean) forCallObj;
                if (forCall) {
                    String callId = (String) creationParams.get("callId");
                    if (!Utils.isStringEmpty(callId)) {
                        renderView(frameLayout, callId, creationParams);
                    }
                } else {
                    String trackId = (String) creationParams.get("trackId");
                    if (!Utils.isStringEmpty(trackId)) {
                        renderView(context, frameLayout, trackId, creationParams);
                    }
                }
            }
        } catch (Exception e) {
            Logging.e(StringeeVideoView.class, e);
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
        Utils.post(() -> {
            CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
            Call2Wrapper call2 = StringeeManager.getInstance().getCall2sMap().get(callId);

            if (call == null && call2 == null) {
                return;
            }

            Object isLocalObj = creationParams.get("isLocal");
            boolean isLocal = isLocalObj instanceof Boolean && (boolean) isLocalObj;
            boolean isMirror = false;

            ScalingType scalingType = ScalingType.SCALE_ASPECT_FILL;
            String scalingTypeStr = (String) creationParams.get("scalingType");
            if (!Utils.isStringEmpty(scalingTypeStr)) {
                if (scalingTypeStr.equals("BALANCED")) {
                    scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                } else if (scalingTypeStr.equals("FIT")) {
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                }
            }

            if (creationParams.containsKey("isMirror")) {
                Object isMirrorObj = creationParams.get("isMirror");
                isMirror = isMirrorObj instanceof Boolean && (boolean) isMirrorObj;
            }

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            layout.removeAllViews();
            layout.setBackgroundColor(Color.BLACK);
            if (isLocal) {
                TextureViewRenderer localView;
                if (call != null) {
                    localView = call.getLocalView();
                    if (localView.getParent() != null) {
                        ((FrameLayout) localView.getParent()).removeView(localView);
                    }

                    layout.addView(localView, layoutParams);
                    call.renderLocalView(scalingType);
                } else {

                    localView = call2.getLocalView();
                    if (localView.getParent() != null) {
                        ((FrameLayout) localView.getParent()).removeView(localView);
                    }

                    layout.addView(localView, layoutParams);
                    call2.renderLocalView(scalingType);
                }
                localView.setMirror(isMirror);

                //save localView option
                Map<String, Object> localViewOptions = new HashMap<>();
                localViewOptions.put("isMirror", isMirror);
                localViewOptions.put("scalingType", scalingType);
                localViewOptions.put("layout", layout);
                StringeeManager.getInstance().getLocalViewOptions().put(callId, localViewOptions);

            } else {
                TextureViewRenderer remoteView;
                if (call != null) {
                    remoteView = call.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((FrameLayout) remoteView.getParent()).removeView(remoteView);
                    }

                    layout.addView(remoteView, layoutParams);
                    call.renderRemoteView(scalingType);
                } else {
                    remoteView = call2.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((FrameLayout) remoteView.getParent()).removeView(remoteView);
                    }

                    layout.addView(remoteView, layoutParams);
                    call2.renderRemoteView(scalingType);
                }
                remoteView.setMirror(isMirror);

                //save remoteView option
                Map<String, Object> remoteViewOptions = new HashMap<>();
                remoteViewOptions.put("isMirror", isMirror);
                remoteViewOptions.put("scalingType", scalingType);
                remoteViewOptions.put("layout", layout);
                StringeeManager.getInstance().getRemoteViewOptions().put(callId, remoteViewOptions);
            }
        }, 500);
    }

    private void renderView(final Context context, final FrameLayout layout, final String trackId, final Map<String, Object> creationParams) {
        Utils.post(() -> {
            VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(trackId);

            if (videoTrackManager == null) {
                return;
            }

            ScalingType scalingType;
            String scalingTypeStr = (String) creationParams.get("scalingType");
            if (!Utils.isStringEmpty(scalingTypeStr)) {
                if (scalingTypeStr.equals("FIT")) {
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                } else if (scalingTypeStr.equals("BALANCED")) {
                    scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                } else {
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                }
            } else {
                scalingType = ScalingType.SCALE_ASPECT_FILL;
            }

            boolean isMirror;
            if (creationParams.containsKey("isMirror")) {
                Object isMirrorObj = creationParams.get("isMirror");
                isMirror = isMirrorObj instanceof Boolean && (boolean) isMirrorObj;
            } else {
                isMirror = false;
            }

            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            layout.removeAllViews();
            layout.setBackgroundColor(Color.BLACK);

            videoTrackManager.setListener(new Listener() {
                @Override
                public void onMediaAvailable() {
                    Utils.post(() -> {
                        TextureViewRenderer trackView = videoTrackManager.getVideoTrack().getView2(context);
                        if (trackView.getParent() != null) {
                            ((FrameLayout) trackView.getParent()).removeView(trackView);
                        }

                        layout.addView(trackView, layoutParams);
                        videoTrackManager.getVideoTrack().renderView2(scalingType);
                        trackView.setMirror(isMirror);
                    });
                }

                @Override
                public void onMediaStateChange(MediaState mediaState) {

                }
            });
        });
    }

}