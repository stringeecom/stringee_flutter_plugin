package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stringee.video.StringeeVideo.ScalingType;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;
import com.stringee.video.TextureViewRenderer;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

public class StringeeVideoView implements PlatformView {
    private FrameLayout frameLayout;
    private static final String TAG = "Stringee sdk";

    StringeeVideoView(@NonNull Context context, int id, @Nullable Map<String, Object> creationParams) {
        try {
            frameLayout = new FrameLayout(context);

            boolean forCall = (boolean) creationParams.get("forCall");
            if (forCall) {
                String callId = (String) creationParams.get("callId");
                if (!(callId == null || callId.length() == 0)) {
                    renderView(frameLayout, callId, creationParams);
                }
            } else {
                String trackId = (String) creationParams.get("trackId");
                if (!(trackId == null || trackId.length() == 0)) {
                    renderView(context, frameLayout, trackId, creationParams);
                }
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

    private void renderView(final FrameLayout layout, final String callId, final Map<String, Object> creationParams) {
        StringeeManager.getInstance().getHandler().postDelayed(new Runnable() {
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
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                } else if (creationParams.get("scalingType").equals("FIT")) {
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                } else if (creationParams.get("scalingType").equals("BALANCED")) {
                    scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                }

                if (creationParams.containsKey("isMirror")) {
                    isMirror = (Boolean) creationParams.get("isMirror");
                }

                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;

                layout.removeAllViews();
                layout.setBackgroundColor(Color.BLACK);
                if (isLocal) {
                    if (call != null) {
                        TextureViewRenderer localView = call.getLocalView();
                        if (localView.getParent() != null) {
                            ((FrameLayout) localView.getParent()).removeView(localView);
                        }

                        layout.addView(localView, layoutParams);
                        call.renderLocalView(scalingType);
                        localView.setMirror(isMirror);
                    } else {

                        TextureViewRenderer localView = call2.getLocalView();
                        if (localView.getParent() != null) {
                            ((FrameLayout) localView.getParent()).removeView(localView);
                        }

                        layout.addView(localView, layoutParams);
                        call2.renderLocalView(scalingType);
                        localView.setMirror(isMirror);
                    }

                    //save localView option
                    Map<String, Object> localViewOptions = new HashMap<>();
                    localViewOptions.put("isMirror", isMirror);
                    localViewOptions.put("scalingType", scalingType);
                    localViewOptions.put("layout", layout);
                    StringeeManager.getInstance().getLocalViewOptions().put(callId, localViewOptions);

                } else {
                    if (call != null) {
                        TextureViewRenderer remoteView = call.getRemoteView();
                        if (remoteView.getParent() != null) {
                            ((FrameLayout) remoteView.getParent()).removeView(remoteView);
                        }

                        layout.addView(remoteView, layoutParams);
                        call.renderRemoteView(scalingType);
                        remoteView.setMirror(isMirror);
                    } else {
                        TextureViewRenderer remoteView = call2.getRemoteView();
                        if (remoteView.getParent() != null) {
                            ((FrameLayout) remoteView.getParent()).removeView(remoteView);
                        }

                        layout.addView(remoteView, layoutParams);
                        call2.renderRemoteView(scalingType);
                        remoteView.setMirror(isMirror);
                    }

                    //save remoteView option
                    Map<String, Object> remoteViewOptions = new HashMap<>();
                    remoteViewOptions.put("isMirror", isMirror);
                    remoteViewOptions.put("scalingType", scalingType);
                    remoteViewOptions.put("layout", layout);
                    StringeeManager.getInstance().getRemoteViewOptions().put(callId, remoteViewOptions);
                }
            }
        },500);
    }

    private void renderView(final Context context, final FrameLayout layout, final String trackId, final Map<String, Object> creationParams) {
        StringeeManager.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(trackId);

                if (videoTrackManager == null) {
                    return;
                }

                ScalingType scalingType;
                if (creationParams.get("scalingType").equals("FILL")) {
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                } else if (creationParams.get("scalingType").equals("FIT")) {
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                } else if (creationParams.get("scalingType").equals("BALANCED")) {
                    scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                } else {
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                }

                boolean isMirror;
                if (creationParams.containsKey("isMirror")) {
                    isMirror = (Boolean) creationParams.get("isMirror");
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
                        StringeeManager.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                TextureViewRenderer trackView = videoTrackManager.getVideoTrack().getView2(context);
                                if (trackView.getParent() != null) {
                                    ((FrameLayout) trackView.getParent()).removeView(trackView);
                                }

                                layout.addView(trackView, layoutParams);
                                videoTrackManager.getVideoTrack().renderView2(scalingType);
                                trackView.setMirror(isMirror);
                            }
                        });
                    }

                    @Override
                    public void onMediaStateChange(MediaState mediaState) {

                    }
                });
            }
        });
    }

}