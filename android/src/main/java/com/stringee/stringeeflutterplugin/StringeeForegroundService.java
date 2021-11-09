package com.stringee.stringeeflutterplugin;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StringeeForegroundService extends Service {
    private static final String TAG = "StringeeSDK";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(StringeeNotification.STRINGEE_START_FOREGROUND_SERVICE)) {
                Notification notification = intent.getParcelableExtra("notification");
                int id = intent.getIntExtra("id", 0);
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "startForegroundService: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        StringeeNotification.startForegroundServiceResult.success(map);
                    }
                });
                startForeground(id, notification);
            } else if (action.equals(StringeeNotification.STRINGEE_STOP_FOREGROUND_SERVICE)) {
                StringeeManager.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "stopForegroundService: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        StringeeNotification.stopForegroundServiceResult.success(map);
                    }
                });
                stopForeground(true);
                stopSelf();
            }
        }

        return START_STICKY;
    }
}
