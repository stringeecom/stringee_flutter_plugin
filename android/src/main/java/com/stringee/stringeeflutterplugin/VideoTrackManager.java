package com.stringee.stringeeflutterplugin;

import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;

import java.util.HashMap;
import java.util.Map;

public class VideoTrackManager implements Listener {
    private final ClientWrapper clientWrapper;
    private final String localId;
    private final StringeeVideoTrack videoTrack;
    private boolean mediaAvailable = false;
    private final boolean forCall;
    private Listener listener;

    public VideoTrackManager(ClientWrapper clientWrapper, StringeeVideoTrack videoTrack, String localId, boolean forCall) {
        this.clientWrapper = clientWrapper;
        this.videoTrack = videoTrack;
        this.localId = localId;
        this.forCall = forCall;
        videoTrack.setListener(this);
        if (forCall) {
            mediaAvailable = true;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (mediaAvailable) {
            if (listener != null) {
                listener.onMediaAvailable();
            }
        }
    }

    public StringeeVideoTrack getVideoTrack() {
        return videoTrack;
    }

    public String getLocalId() {
        return localId;
    }

    public VideoTrackManager getThis() {
        return this;
    }

    @Override
    public void onMediaAvailable() {
        Utils.post(() -> {
            mediaAvailable = true;
            if (listener != null) {
                listener.onMediaAvailable();
            }

            Map<String, Object> map = Utils.createEventMap("trackReadyToPlay", clientWrapper.getId(), StringeeManager.StringeeEventType.RoomEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", videoTrack.getRoomId());
            bodyMap.put("track", Utils.convertVideoTrackToMap(getThis()));
            Logging.d("track: " + bodyMap);
            map.put("body", bodyMap);
            if (!forCall) {
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onMediaStateChange(MediaState mediaState) {

    }
}
