package com.stringee.stringeeflutterplugin.video_view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.stringeeflutterplugin.call.StringeeCallWrapper;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.conference.VideoTrackManager;
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
                boolean forCall = Boolean.TRUE.equals(creationParams.get("forCall"));
                if (forCall) {
                    String callId = (String) creationParams.get("callId");
                    if (!Utils.isEmpty(callId)) {
                        renderView(frameLayout, callId, creationParams);
                    }
                } else {
                    String trackId = (String) creationParams.get("trackId");
                    if (!Utils.isEmpty(trackId)) {
                        renderView(context, frameLayout, trackId, creationParams);
                    }
                }
            }
        } catch (Exception e) {
            Utils.reportException(StringeeVideoView.class, e);
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

    private void renderView(final FrameLayout layout, final String callId,
                            @NonNull final Map<String, Object> creationParams) {
        Utils.post(() -> {
            StringeeCallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);

            if (call == null) {
                return;
            }

            boolean isLocal = Boolean.TRUE.equals(creationParams.get("isLocal"));
            boolean isMirror = false;

            String scalingTypeStr = (String) creationParams.get("scalingType");
            ScalingType scalingType = ScalingType.SCALE_ASPECT_BALANCED;
            if (!Utils.isEmpty(scalingTypeStr)) {
                if (scalingTypeStr.equals("FILL")) {
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                } else if (scalingTypeStr.equals("FIT")) {
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                }
            }

            if (creationParams.containsKey("isMirror")) {
                isMirror = Boolean.TRUE.equals(creationParams.get("isMirror"));
            }

            LayoutParams layoutParams =
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            layout.removeAllViews();
            layout.setBackgroundColor(Color.BLACK);
            if (isLocal) {
                TextureViewRenderer localView;
                localView = call.getLocalView();
                if (localView.getParent() != null) {
                    ((FrameLayout) localView.getParent()).removeView(localView);
                }

                layout.addView(localView, layoutParams);
                call.renderLocalView(scalingType);
                localView.setMirror(isMirror);

                //save localView option
                Map<String, Object> localViewOptions = new HashMap<>();
                localViewOptions.put("isMirror", isMirror);
                localViewOptions.put("scalingType", scalingType);
                localViewOptions.put("layout", layout);
                StringeeManager.getInstance().getLocalViewOptions().put(callId, localViewOptions);
            } else {
                TextureViewRenderer remoteView;
                remoteView = call.getRemoteView();
                if (remoteView.getParent() != null) {
                    ((FrameLayout) remoteView.getParent()).removeView(remoteView);
                }

                layout.addView(remoteView, layoutParams);
                call.renderRemoteView(scalingType);
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

    private void renderView(final Context context, final FrameLayout layout, final String trackId,
                            final Map<String, Object> creationParams) {
        Utils.post(() -> {
            VideoTrackManager videoTrackManager =
                    StringeeManager.getInstance().getTracksMap().get(trackId);

            if (videoTrackManager == null) {
                return;
            }

            LayoutParams layoutParams =
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            layout.removeAllViews();
            layout.setBackgroundColor(Color.BLACK);

            videoTrackManager.setListener(new Listener() {
                @Override
                public void onMediaAvailable() {
                    Utils.post(() -> {
                        TextureViewRenderer trackView =
                                videoTrackManager.getVideoTrack().getView2(context);
                        if (trackView.getParent() != null) {
                            ((FrameLayout) trackView.getParent()).removeView(trackView);
                        }

                        String scalingTypeStr = (String) creationParams.get("scalingType");
                        ScalingType scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                        if (!Utils.isEmpty(scalingTypeStr)) {
                            if (scalingTypeStr.equals("FILL")) {
                                scalingType = ScalingType.SCALE_ASPECT_FILL;
                            } else if (scalingTypeStr.equals("FIT")) {
                                scalingType = ScalingType.SCALE_ASPECT_FIT;
                            }
                        }

                        boolean isMirror = false;
                        if (creationParams.containsKey("isMirror")) {
                            isMirror = Boolean.TRUE.equals(creationParams.get("isMirror"));
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
