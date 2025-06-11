package com.stringee.stringeeflutterplugin.call.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.call.StringeeCallWrapper;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.PacketSenderThread;
import com.stringee.stringeeflutterplugin.common.PrefUtils;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.notification.StringeeNotification;

public class ActionCallService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String uuid = intent.getStringExtra(Constants.PARAM_UUID);
            String callId = intent.getStringExtra(Constants.PARAM_CALL_ID);
            if (!Utils.isEmpty(action) && !Utils.isEmpty(uuid) && !Utils.isEmpty(callId)) {
                ClientWrapper clientWrapper =
                        StringeeManager.getInstance().getClientMap().get(uuid);
                if (clientWrapper != null) {
                    new Thread(() -> {
                        autoReconnectForCallIfNeed(clientWrapper, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                StringeeCallWrapper callWrapper =
                                        StringeeManager.getInstance().getCallsMap().get(callId);
                                if (callWrapper != null) {
                                    if (action.equals(Constants.ACTION_REJECT_CALL)) {
                                        callWrapper.reject(null);
                                    } else if (action.equals(Constants.ACTION_HANG_UP_CALL)) {
                                        callWrapper.hangup(null);
                                    }
                                }
                            }
                        });
                        int notificationId = intent.getIntExtra(Constants.PARAM_NOTIFICATION_ID, 0);
                        StringeeNotification.getInstance(getApplicationContext())
                                .cancelNotification(notificationId);
                    }).start();
                }
            }
            PacketSenderThread.getInstance().send(intent);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void autoReconnectForCallIfNeed(ClientWrapper clientWrapper,
                                           final StatusListener listener) {
        if (!clientWrapper.isConnected()) {
            PrefUtils prefUtils = PrefUtils.getInstance(this);
            String accessToken = prefUtils.getString(Constants.PREF_ACCESS_TOKEN, "");
            if (!Utils.isEmpty(accessToken)) {
                String addressString = prefUtils.getString(Constants.PREF_SERVER_ADDRESS, "");
                final Handler timeoutHandler = new Handler(Looper.getMainLooper());
                final Runnable timeoutRunnable = () -> {
                    if (listener != null) {
                        listener.onError(new StringeeError(408, "Reconnect timeout"));
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 3000);
                clientWrapper.connect(addressString, accessToken, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        if (listener != null) {
                            listener.onError(stringeeError);
                        }
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                    }
                });
            } else {
                if (listener != null) {
                    listener.onError(
                            new StringeeError(5, "Cannot reconnect because access token is empty"));
                }
            }
        } else {
            if (listener != null) {
                listener.onSuccess();
            }
        }
    }
}
