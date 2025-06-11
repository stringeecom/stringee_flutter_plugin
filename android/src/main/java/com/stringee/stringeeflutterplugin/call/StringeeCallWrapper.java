package com.stringee.stringeeflutterplugin.call;

import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.video.TextureViewRenderer;

import org.webrtc.RendererCommon.ScalingType;

import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public abstract class StringeeCallWrapper {
    protected final ClientWrapper clientWrapper;
    protected Result makeCallResult;
    protected boolean localStreamShowed;
    protected boolean hasRemoteStream;
    protected boolean remoteStreamShowed;
    protected boolean isIncomingCall;

    public StringeeCallWrapper(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    protected void prepareCall() {
        hasRemoteStream = false;
        remoteStreamShowed = false;
    }

    protected void makeCall(final Result result) {
        this.makeCallResult = result;
    }

    abstract public void initAnswer(final Result result);

    abstract public void answer(final Result result);

    abstract public void hangup(final Result result);

    abstract public void reject(final Result result);

    abstract public void sendDTMF(final String dtmf, final Result result);

    abstract public void sendCallInfo(final Map<String, Object> callInfo, final Result result);

    abstract public void mute(final boolean mute, final Result result);

    abstract public void enableVideo(final boolean enable, final Result result);

    abstract public void switchCamera(final Result result);

    abstract public void switchCamera(String cameraName, final Result result);

    abstract public void resumeVideo(final Result result);

    abstract public void getCallStats(final Result result);

    abstract public void setMirror(final boolean isLocal, final boolean isMirror,
                                   final Result result);

    abstract public void startCapture(final Result result);

    abstract public void stopCapture(final Result result);

    abstract public TextureViewRenderer getLocalView();

    abstract public TextureViewRenderer getRemoteView();

    abstract public void renderLocalView(ScalingType scalingType);

    abstract public void renderRemoteView(ScalingType scalingType);
}
