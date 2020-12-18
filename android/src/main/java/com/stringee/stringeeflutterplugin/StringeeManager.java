package com.stringee.stringeeflutterplugin;

import android.widget.FrameLayout;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;

import java.util.HashMap;
import java.util.Map;

public class StringeeManager {
    private static StringeeManager stringeeManager;
    private StringeeClient mClient;
    private Map<String, StringeeCall> callsMap = new HashMap<>();
    private Map<String, StringeeCall2> call2sMap = new HashMap<>();
    private Map<String, Map<String, Object>> localViewOption = new HashMap<>();

    public enum StringeeEnventType {
        ClientEvent(0),
        CallEvent(1),
        Call2Event(2);

        public final short value;

        StringeeEnventType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

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

    public Map<String, StringeeCall2> getCall2sMap() {
        return call2sMap;
    }

    public Map<String, Map<String, Object>> getLocalViewOptions() {
        return localViewOption;
    }
}
