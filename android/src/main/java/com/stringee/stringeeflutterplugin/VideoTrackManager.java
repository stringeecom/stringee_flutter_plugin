package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.RoomEvent;

import android.util.Log;

import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;

import java.util.HashMap;
import java.util.Map;

public class VideoTrackManager implements Listener {
    private ClientWrapper clientWrapper;
    private String localId;
    private StringeeVideoTrack videoTrack;
    private boolean mediaAvailable = false;
    private boolean forCall = false;
    private Listener listener;
    private Map<String, Object> viewOptions = new HashMap<>();

    public VideoTrackManager(ClientWrapper clientWrapper, StringeeVideoTrack videoTrack, String localId, boolean forCall) {
        this.clientWrapper = clientWrapper;
        this.videoTrack = videoTrack;
        this.localId = localId;
        this.forCall = forCall;
        videoTrack.setListener(this);
        if (forCall) {
            this.mediaAvailable = true;
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

    public Map<String, Object> getViewOptions() {
        return viewOptions;
    }

    @Override
    public void onMediaAvailable() {
        StringeeManager.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                mediaAvailable = true;
                if (listener != null) {
                    listener.onMediaAvailable();
                }

                Log.d(StringeeFlutterPlugin.TAG, "trackReadyToPlay: " + (videoTrack.isLocal() ? localId : videoTrack.getId()));
                if (!forCall) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "trackReadyToPlay");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
                    bodyMap.put("roomId", videoTrack.getRoomId());
                    bodyMap.put("track", Utils.convertVideoTrackToMap(getThis()));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onMediaStateChange(MediaState mediaState) {

    }
}
