package com.stringee.stringeeflutterplugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Process-wide registry for client, call, track, renderer, and screen-capture bridge objects.
 */
public class StringeeManager {

    private static StringeeManager instance;
    private final Map<String, ClientWrapper> clientMap = new HashMap<>();
    private final Map<String, CallWrapper> callsMap = new HashMap<>();
    private final Map<String, Call2Wrapper> call2sMap = new HashMap<>();
    private final Map<String, Map<String, Object>> localViewOption = new HashMap<>();
    private final Map<String, Map<String, Object>> remoteViewOption = new HashMap<>();
    private final Map<String, VideoTrackManager> tracksMap = new HashMap<>();

    private ScreenCaptureManager captureManager;

    /** Returns the lazily created registry singleton. */
    public static synchronized StringeeManager getInstance() {
        if (instance == null) {
            instance = new StringeeManager();
        }

        return instance;
    }

    /** Returns clients keyed by Dart UUID. */
    public Map<String, ClientWrapper> getClientMap() {
        return clientMap;
    }

    /** Returns first-generation calls keyed by call ID. */
    public Map<String, CallWrapper> getCallsMap() {
        return callsMap;
    }

    /** Returns Call2 instances keyed by call ID. */
    public Map<String, Call2Wrapper> getCall2sMap() {
        return call2sMap;
    }

    /** Returns pending local-renderer options keyed by call ID. */
    public Map<String, Map<String, Object>> getLocalViewOptions() {
        return localViewOption;
    }

    /** Returns pending remote-renderer options keyed by call ID. */
    public Map<String, Map<String, Object>> getRemoteViewOptions() {
        return remoteViewOption;
    }

    /** Returns conference tracks keyed by local or server track ID. */
    public Map<String, VideoTrackManager> getTracksMap() {
        return tracksMap;
    }

    /** Returns the active screen-capture manager, or {@code null}. */
    public ScreenCaptureManager getCaptureManager() {
        return captureManager;
    }

    /** Stores the active {@code captureManager}. */
    public void setCaptureManager(ScreenCaptureManager captureManager) {
        this.captureManager = captureManager;
    }
}
