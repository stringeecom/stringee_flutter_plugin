package com.stringee.stringeeflutterplugin;

import java.io.Serializable;
import java.util.List;

public class NotificationInfo implements Serializable {
    private int id;
    private String channelId;
    private String contentTitle;
    private String contentText;
    private String subText;
    private String contentInfo;
    private int number;
    private boolean autoCancel;
    private boolean showWhen;
    private int when;
    private String iconSource;
    private int sourceFrom;
    private boolean playSound;
    private String soundSource;
    private int sourceType;
    private int ringtoneType;
    private String category;
    private boolean fullScreenIntent;
    private long[] vibrationPattern;
    private boolean onGoing;
    private boolean onlyAlertOnce;
    private boolean isTimeoutAfter;
    private int timeoutAfter;
    private int priority;
    private List<NotificationAction> actions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public String getContentInfo() {
        return contentInfo;
    }

    public void setContentInfo(String contentInfo) {
        this.contentInfo = contentInfo;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isAutoCancel() {
        return autoCancel;
    }

    public void setAutoCancel(boolean autoCancel) {
        this.autoCancel = autoCancel;
    }

    public boolean isShowWhen() {
        return showWhen;
    }

    public void setShowWhen(boolean showWhen) {
        this.showWhen = showWhen;
    }

    public int getWhen() {
        return when;
    }

    public void setWhen(int when) {
        this.when = when;
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

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public int getRingtoneType() {
        return ringtoneType;
    }

    public void setRingtoneType(int ringtoneType) {
        this.ringtoneType = ringtoneType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFullScreenIntent() {
        return fullScreenIntent;
    }

    public void setFullScreenIntent(boolean fullScreenIntent) {
        this.fullScreenIntent = fullScreenIntent;
    }

    public long[] getVibrationPattern() {
        return vibrationPattern;
    }

    public void setVibrationPattern(long[] vibrationPattern) {
        this.vibrationPattern = vibrationPattern;
    }

    public boolean isOnGoing() {
        return onGoing;
    }

    public void setOnGoing(boolean onGoing) {
        this.onGoing = onGoing;
    }

    public boolean isOnlyAlertOnce() {
        return onlyAlertOnce;
    }

    public void setOnlyAlertOnce(boolean onlyAlertOnce) {
        this.onlyAlertOnce = onlyAlertOnce;
    }

    public boolean isTimeoutAfter() {
        return isTimeoutAfter;
    }

    public void setTimeoutAfter(boolean timeoutAfter) {
        isTimeoutAfter = timeoutAfter;
    }

    public int getTimeoutAfter() {
        return timeoutAfter;
    }

    public void setTimeoutAfter(int timeoutAfter) {
        this.timeoutAfter = timeoutAfter;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public java.util.List<NotificationAction> getActions() {
        return actions;
    }

    public void setActions(java.util.List<NotificationAction> actions) {
        this.actions = actions;
    }
}
