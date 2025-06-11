package com.stringee.stringeeflutterplugin.common;

import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.call.Call2Wrapper;
import com.stringee.stringeeflutterplugin.call.CallWrapper;
import com.stringee.stringeeflutterplugin.call.StringeeCallWrapper;
import com.stringee.stringeeflutterplugin.conference.ScreenCaptureManager;
import com.stringee.stringeeflutterplugin.conference.VideoTrackManager;

import java.util.HashMap;
import java.util.Map;

public class StringeeManager {
    private static StringeeManager instance;
    private final Map<String, ClientWrapper> clientMap = new HashMap<>();
    private final Map<String, StringeeCallWrapper> callsMap = new HashMap<>();
    private final Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private final Map<String, Map<String, Object>> remoteViewOption = new HashMap<>();
    private final Map<String, VideoTrackManager> tracksMap = new HashMap<>();

    private ScreenCaptureManager captureManager;

    public static synchronized StringeeManager getInstance() {
        if (instance == null) {
            instance = new StringeeManager();
        }

        return instance;
    }

    public Map<String, ClientWrapper> getClientMap() {
        return clientMap;
    }

    public Map<String, StringeeCallWrapper> getCallsMap() {
        return callsMap;
    }

    public Map<String, Map<String, Object>> getLocalViewOptions() {
        return localViewOption;
    }

    public Map<String, Map<String, Object>> getRemoteViewOptions() {
        return remoteViewOption;
    }

    public Map<String, VideoTrackManager> getTracksMap() {
        return tracksMap;
    }

    public ScreenCaptureManager getCaptureManager() {
        return captureManager;
    }

    public void setCaptureManager(ScreenCaptureManager captureManager) {
        this.captureManager = captureManager;
    }
}
