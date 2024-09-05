package com.stringee.stringeeflutterplugin;

import android.util.Log;

import androidx.annotation.NonNull;

public class Logging {
    private static final String TAG = "Stringee native";

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String function, boolean status, int code, String msg) {
        Log.d(TAG, function + ": " + status + " - " + code + " - " + msg);
    }

    public static void e(@NonNull Class<?> clazz, Exception exception) {
        Log.e(TAG, clazz.getName(), exception);
    }
}