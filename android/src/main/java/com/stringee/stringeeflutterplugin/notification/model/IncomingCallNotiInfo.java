package com.stringee.stringeeflutterplugin.notification.model;

import android.app.Notification;

import java.io.Serializable;

public class IncomingCallNotiInfo implements Serializable {
    // Channel info
    private String channelId;
    private String channelName;
    private String channelDescription;
    private boolean enableLights = false;
    private boolean enableVibration = true;
    private long[] vibrationPattern = new long[]{0, 350, 500};
    private int lockscreenVisibility = Notification.VISIBILITY_PRIVATE;
    private String soundSource;
    private int sourceType;

    // Notification info
    private int id;
    private String fromName;
    private String fromNumber;
    private String fromAvatarUrl;
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

    public boolean isEnableLights() {
        return enableLights;
    }

    public void setEnableLights(boolean enableLights) {
        this.enableLights = enableLights;
    }

    public boolean isEnableVibration() {
        return enableVibration;
    }

    public void setEnableVibration(boolean enableVibration) {
        this.enableVibration = enableVibration;
    }

    public long[] getVibrationPattern() {
        return vibrationPattern;
    }

    public void setVibrationPattern(long[] vibrationPattern) {
        this.vibrationPattern = vibrationPattern;
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

    public String getSoundSource() {
        return soundSource;
    }

    public void setSoundSource(String soundSource) {
        this.soundSource = soundSource;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public String getFromAvatarUrl() {
        return fromAvatarUrl;
    }

    public void setFromAvatarUrl(String fromAvatarUrl) {
        this.fromAvatarUrl = fromAvatarUrl;
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
