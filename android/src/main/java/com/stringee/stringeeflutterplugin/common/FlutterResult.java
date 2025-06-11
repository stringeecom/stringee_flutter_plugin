package com.stringee.stringeeflutterplugin.common;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public abstract class FlutterResult {
    final Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public static Success success(String functionName) {
        Log.d(Constants.TAG, functionName + ": success");
        return new Success();
    }

    public static Error error(String functionName, int code, String message) {
        Log.d(Constants.TAG, functionName + ": false - " + code + " - " + message);
        return new Error(code, message);
    }

    public static class Success extends FlutterResult {
        public Success() {
            put("status", true);
            put("code", 0);
            put("message", "Success");
        }
    }

    public static class Error extends FlutterResult {
        public Error(int code, String message) {
            put("status", false);
            put("code", code);
            put("message", message);
        }
    }
}
