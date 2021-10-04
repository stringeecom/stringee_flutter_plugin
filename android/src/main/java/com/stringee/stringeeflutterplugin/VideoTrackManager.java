package com.stringee.stringeeflutterplugin;

import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;

public class VideoTrackManager implements Listener {
    private StringeeVideoTrack videoTrack;
    private boolean mediaAvailable = false;
    private Listener listener;

    public VideoTrackManager(StringeeVideoTrack videoTrack, boolean forCall) {
        this.videoTrack = videoTrack;
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

    @Override
    public void onMediaAvailable() {
        StringeeManager.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                mediaAvailable = true;
                listener.onMediaAvailable();
            }
        });
    }

    @Override
    public void onMediaStateChange(MediaState mediaState) {

    }
}
