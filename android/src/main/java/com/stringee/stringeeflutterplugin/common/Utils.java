package com.stringee.stringeeflutterplugin.common;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class Utils {
    public static void post(Runnable runnable) {
        post(runnable, 0);
    }

    public static void post(Runnable runnable, long delayMillis) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, delayMillis);
    }

    public static boolean isEmpty(@Nullable Object object) {
        if (object != null) {
            if (object instanceof JSONArray) {
                return ((JSONArray) object).length() == 0;
            }
            if (object instanceof List) {
                return ((List<?>) object).isEmpty();
            }
            if (object instanceof CharSequence) {
                CharSequence charSequence = (CharSequence) object;
                if (charSequence.toString().equalsIgnoreCase("null")) {
                    return true;
                } else {
                    return charSequence.toString().trim().isEmpty();
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static <T> void reportException(@NonNull Class<T> clazz, Exception exception) {
        Log.e("Stringee exception", clazz.getName(), exception);
    }

    public static Map<String, Object> convertJsonToMap(JSONObject object) throws JSONException {
        if (object != null) {
            Map<String, Object> map = new HashMap<>();
            Iterator<String> keysItr = object.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = object.get(key);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = convertJsonToMap((JSONObject) value);
                }
                map.put(key, value);
            }
            return map;
        } else {
            return null;
        }
    }

    public static JSONObject convertMapToJson(Map<String, Object> map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = convertJsonToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
