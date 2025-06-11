package com.stringee.stringeeflutterplugin.notification.model;

import android.app.Notification;

public class ScreenCaptureServiceInfo {
    // Channel info
    private String channelId;
    private String channelName;
    private String channelDescription;
    private int lockscreenVisibility = Notification.VISIBILITY_PUBLIC;

    // Notification info
    private int id;
    private String iconSource;
    private int sourceFrom;
    private String contentTitle;
    private String contentText;

    // Other information for call
    private String clientId;
    private String callId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelDescription() {
        return channelDescription;
    }

    public void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
    }

    public int getLockscreenVisibility() {
        switch (lockscreenVisibility) {
            case -1:
                return Notification.VISIBILITY_SECRET;
            case 1:
                return Notification.VISIBILITY_PUBLIC;
            default:
                return Notification.VISIBILITY_PRIVATE;
        }
    }

    public void setLockscreenVisibility(int lockscreenVisibility) {
        this.lockscreenVisibility = lockscreenVisibility;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIconSource() {
        return iconSource;
    }

    public void setIconSource(String iconSource) {
        this.iconSource = iconSource;
    }

    public int getSourceFrom() {
        return sourceFrom;
    }

    public void setSourceFrom(int sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }
}
