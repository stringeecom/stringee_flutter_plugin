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
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.TextureViewRenderer;

import org.webrtc.RendererCommon.ScalingType;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

/**
 * Android platform view that attaches a native Stringee call or conference video renderer.
 *
 * <p>Call view options are stored even when the SDK renderer is temporarily unavailable. The
 * corresponding call wrapper uses those pending options when a local or remote stream later
 * becomes ready. Disposing the platform view removes only the options owned by its container so a
 * replacement Flutter view remains active.</p>
 */
public class StringeeVideoView implements PlatformView {

    private FrameLayout frameLayout;
    private boolean disposed;
    private String callId;
    private boolean localCallView;

    StringeeVideoView(@NonNull Context context, @Nullable Map<String, Object> creationParams) {
        try {
            frameLayout = new FrameLayout(context);
            if (creationParams != null) {
                boolean forCall = Boolean.TRUE.equals(creationParams.get("forCall"));
                if (forCall) {
                    String callId = (String) creationParams.get("callId");
                    if (!Utils.isEmpty(callId)) {
                        this.callId = callId;
                        localCallView = Boolean.TRUE.equals(creationParams.get("isLocal"));
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
        disposed = true;
        removeCallViewOptions();
        frameLayout.removeAllViews();
    }

    /** Removes pending call-render options owned by this platform view. */
    private void removeCallViewOptions() {
        if (Utils.isEmpty(callId)) {
            return;
        }

        Map<String, Map<String, Object>> viewOptions = localCallView
                ? StringeeManager.getInstance().getLocalViewOptions()
                : StringeeManager.getInstance().getRemoteViewOptions();
        Map<String, Object> options = viewOptions.get(callId);
        if (options != null && options.get("layout") == frameLayout) {
            viewOptions.remove(callId);
        }
    }

    /**
     * Records call-view options and attaches the renderer immediately when it is already ready.
     *
     * <p>If attachment is deferred, {@link CallWrapper} or {@link Call2Wrapper} completes it when
     * the relevant native stream callback arrives.</p>
     */
    private void renderView(
            final FrameLayout layout, final String callId,
            @NonNull final Map<String, Object> creationParams
    ) {
        Utils.post(
                () -> {
                    if (disposed) {
                        return;
                    }

                    CallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
                    Call2Wrapper call2 = StringeeManager.getInstance().getCall2sMap().get(callId);

                    if (call == null && call2 == null) {
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

                    LayoutParams layoutParams = new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;

                    layout.removeAllViews();
                    layout.setBackgroundColor(Color.BLACK);
                    if (isLocal) {
                        Map<String, Object> localViewOptions = new HashMap<>();
                        localViewOptions.put("isMirror", isMirror);
                        localViewOptions.put("scalingType", scalingType);
                        localViewOptions.put("layout", layout);
                        StringeeManager.getInstance().getLocalViewOptions().put(
                                callId, localViewOptions);

                        TextureViewRenderer localView = call != null
                                ? call.getLocalView() : call2.getLocalView();
                        if (!Utils.attachVideoRenderer(layout, localView, layoutParams)) {
                            return;
                        }

                        if (call != null) {
                            call.renderLocalView(scalingType);
                        } else {
                            call2.renderLocalView(scalingType);
                        }
                        localView.setMirror(isMirror);
                    } else {
                        Map<String, Object> remoteViewOptions = new HashMap<>();
                        remoteViewOptions.put("isMirror", isMirror);
                        remoteViewOptions.put("scalingType", scalingType);
                        remoteViewOptions.put("layout", layout);
                        StringeeManager.getInstance().getRemoteViewOptions().put(
                                callId, remoteViewOptions);

                        TextureViewRenderer remoteView = call != null
                                ? call.getRemoteView() : call2.getRemoteView();
                        if (!Utils.attachVideoRenderer(layout, remoteView, layoutParams)) {
                            return;
                        }

                        if (call != null) {
                            call.renderRemoteView(scalingType);
                        } else {
                            call2.renderRemoteView(scalingType);
                        }
                        remoteView.setMirror(isMirror);
                    }
                }, 500
        );
    }

    /** Attaches a conference track after its native media becomes available. */
    private void renderView(
            final Context context, final FrameLayout layout, final String trackId,
            final Map<String, Object> creationParams
    ) {
        Utils.post(() -> {
            VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                    trackId);

            if (videoTrackManager == null) {
                return;
            }

            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            layout.removeAllViews();
            layout.setBackgroundColor(Color.BLACK);

            videoTrackManager.setListener(new Listener() {
                @Override
                public void onMediaAvailable() {
                    Utils.post(() -> {
                        if (disposed) {
                            return;
                        }

                        StringeeVideoTrack videoTrack = videoTrackManager.getVideoTrack();
                        if (videoTrack == null) {
                            return;
                        }

                        TextureViewRenderer trackView = videoTrack.getView2(context);

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

                        if (!Utils.attachVideoRenderer(layout, trackView, layoutParams)) {
                            return;
                        }

                        videoTrack.renderView2(scalingType);
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
