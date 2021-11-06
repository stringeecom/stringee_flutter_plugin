package com.stringee.stringeeflutterplugin.notification;

public class NotificationAction {
    private int id;
    private String icon;
    private int sourceFrom;
    private String title;
    private boolean isOpenApp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getSourceFrom() {
        return sourceFrom;
    }

    public void setSourceFrom(int sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isOpenApp() {
        return isOpenApp;
    }

    public void setIsOpenApp(boolean isOpenApp) {
        this.isOpenApp = isOpenApp;
    }
}
