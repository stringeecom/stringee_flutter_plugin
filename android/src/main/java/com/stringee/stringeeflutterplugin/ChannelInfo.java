package com.stringee.stringeeflutterplugin;

import android.app.Notification;
import android.app.NotificationManager;

public class ChannelInfo {
    private String channelId;
    private String channelName;
    private String description;
    private int importance;
    private boolean enableLights;
    private boolean enableVibration;
    private long[] vibrationPattern;
    private int lockscreenVisibility;
    private boolean playSound;
    private String soundSource;
    private int sourceType;
    private int ringtoneType;
    private boolean bypassDnd;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImportance() {
        switch (importance) {
            case 0:
                return NotificationManager.IMPORTANCE_NONE;
            case 1:
                return NotificationManager.IMPORTANCE_MIN;
            case 2:
                return NotificationManager.IMPORTANCE_LOW;
            case 4:
                return NotificationManager.IMPORTANCE_HIGH;
            case 5:
                return NotificationManager.IMPORTANCE_MAX;
            case 3:
            default:
                return NotificationManager.IMPORTANCE_DEFAULT;
        }
    }

    public void setImportance(int importance) {
        this.importance = importance;
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
            case 0:
            default:
                return Notification.VISIBILITY_PRIVATE;
            case 1:
                return Notification.VISIBILITY_PUBLIC;
        }
    }

    public void setLockscreenVisibility(int lockscreenVisibility) {
        this.lockscreenVisibility = lockscreenVisibility;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public String getSoundSource() {
        return soundSource;
    }

    public void setSoundSource(String soundSource) {
        this.soundSource = soundSource;
    }

    public int getRingtoneType() {
        return ringtoneType;
    }

    public void setRingtoneType(int ringtoneType) {
        this.ringtoneType = ringtoneType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isBypassDnd() {
        return bypassDnd;
    }

    public void setBypassDnd(boolean bypassDnd) {
        this.bypassDnd = bypassDnd;
    }
}
