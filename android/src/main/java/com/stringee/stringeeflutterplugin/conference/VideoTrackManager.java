package com.stringee.stringeeflutterplugin.conference;

import android.util.Log;

import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.StringeeFlutterPlugin;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.common.StringeeEventType;
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

    public VideoTrackManager(ClientWrapper clientWrapper, StringeeVideoTrack videoTrack,
                             String localId, boolean forCall) {
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

            Log.d(Constants.TAG,
                    "trackReadyToPlay: " + (videoTrack.isLocal() ? localId : videoTrack.getId()));
            if (!forCall) {
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
                map.put("event", "trackReadyToPlay");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", videoTrack.getRoomId());
                bodyMap.put("track", ConferenceUtils.convertVideoTrackToMap(getThis()));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onMediaStateChange(MediaState mediaState) {

    }
}
