package com.stringee.stringeeflutterplugin.call.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.notification.StringeeNotification;

public class InCallService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!Utils.isEmpty(action)) {
                if (action.equals(Constants.ACTION_START_FOREGROUND_SERVICE)) {
                    StringeeNotification.getInstance(this).startInCallService(this);
                }
            }
        }
        return START_STICKY;
    }

    public void stopService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
