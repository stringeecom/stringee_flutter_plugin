package com.stringee.stringeeflutterplugin.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Handler handler = new Handler(Looper.getMainLooper());
        int actionId = intent.getIntExtra(StringeeNotification.STRINGEE_NOTIFICATION_ACTION_ID, 0);
        if (actionId != 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Map actionMap = new HashMap();
                    actionMap.put("actionId", actionId);
                    StringeeNotification._eventSink.success(actionMap);
                }
            });
        }
    }
}
