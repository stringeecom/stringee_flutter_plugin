package com.stringee.stringeeflutterplugin;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;

import java.util.HashMap;
import java.util.Map;

public class StringeeManager {
    private static StringeeManager stringeeManager;
    private StringeeClient mClient;
    private com.stringee.stringeeflutterplugin.StringeeAudioManager audioManager;
    private Map<String, StringeeCall> callsMap = new HashMap<>();

    public static synchronized StringeeManager getInstance() {
        if (stringeeManager == null) {
            stringeeManager = new StringeeManager();
        }

        return stringeeManager;
    }

    public StringeeClient getClient() {
        return mClient;
    }

    public void setClient(StringeeClient mClient) {
        this.mClient = mClient;
    }

    public Map<String, StringeeCall> getCallsMap() {
        return callsMap;
    }

    public com.stringee.stringeeflutterplugin.StringeeAudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(com.stringee.stringeeflutterplugin.StringeeAudioManager audioManager) {
        this.audioManager = audioManager;
    }
}
