package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.util.Log;
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
            Log.d(StringeeFlutterPlugin.TAG, "StringeeVideoView render error: " + e.getMessage());
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

                String scalingTypeString = (String) creationParams.get("scalingType");
                ScalingType scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                if (scalingTypeString != null) {
                    switch (scalingTypeString) {
                        case "FILL":
                            scalingType = ScalingType.SCALE_ASPECT_FILL;
                            break;
                        case "FIT":
                            scalingType = ScalingType.SCALE_ASPECT_FIT;
                            break;
                    }
                }

                boolean isMirror = false;
                if (creationParams.containsKey("isMirror")) {
                    isMirror = (Boolean) creationParams.get("isMirror");
                }

                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;

                layout.removeAllViews();
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
                    Map<String, Object> localViewOption = new HashMap<>();
                    localViewOption.put("isMirror", isMirror);
                    localViewOption.put("scalingType", scalingType);
                    localViewOption.put("layout", layout);
                    StringeeManager.getInstance().getLocalViewOption().put(callId, localViewOption);

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
                    Map<String, Object> remoteViewOption = new HashMap<>();
                    remoteViewOption.put("isMirror", isMirror);
                    remoteViewOption.put("scalingType", scalingType);
                    remoteViewOption.put("layout", layout);
                    StringeeManager.getInstance().getRemoteViewOption().put(callId, remoteViewOption);
                }
            }
        }, 500);
    }

    private void renderView(final Context context, final FrameLayout layout, final String trackId, final Map<String, Object> creationParams) {
        StringeeManager.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(trackId);

                if (videoTrackManager == null) {
                    return;
                }
                videoTrackManager.setListener(new Listener() {
                    @Override
                    public void onMediaAvailable() {
                        StringeeManager.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                String scalingTypeString = (String) creationParams.get("scalingType");
                                ScalingType scalingType = ScalingType.SCALE_ASPECT_BALANCED;
                                if (scalingTypeString != null) {
                                    switch (scalingTypeString) {
                                        case "FILL":
                                            scalingType = ScalingType.SCALE_ASPECT_FILL;
                                            break;
                                        case "FIT":
                                            scalingType = ScalingType.SCALE_ASPECT_FIT;
                                            break;
                                    }
                                }

                                boolean isMirror = false;
                                if (creationParams.containsKey("isMirror")) {
                                    isMirror = (Boolean) creationParams.get("isMirror");
                                }

                                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                                layoutParams.gravity = Gravity.CENTER;

                                layout.removeAllViews();

                                TextureViewRenderer trackView = videoTrackManager.getVideoTrack().getView2(context);
                                if (trackView.getParent() != null) {
                                    ((FrameLayout) trackView.getParent()).removeView(trackView);
                                }

                                layout.addView(trackView, layoutParams);
                                videoTrackManager.getVideoTrack().renderView2(scalingType);
                                trackView.setMirror(isMirror);

                                //save track view option
                                videoTrackManager.getViewOptions().put("isMirror", isMirror);
                                videoTrackManager.getViewOptions().put("scalingType", scalingType);
                                videoTrackManager.getViewOptions().put("layout", layout);
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