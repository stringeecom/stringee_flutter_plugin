package com.stringee.stringeeflutterplugin.call.receiver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.stringee.stringeeflutterplugin.common.FlutterResult;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class GSMCallStateReceiver implements MethodChannel.MethodCallHandler, EventChannel.StreamHandler {
    public enum PhoneState {
        IDLE("idle"),
        RINGING("ringing"),
        OFFHOOK("offHook");

        private final String value;

        PhoneState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PhoneState getState(int value) {
            switch (value) {
                case TelephonyManager.CALL_STATE_RINGING:
                    return RINGING;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    return OFFHOOK;
                default:
                    return IDLE;
            }
        }
    }

    private static volatile GSMCallStateReceiver instance = null;
    public static PhoneState phoneState = PhoneState.IDLE;
    private TelephonyManager telephonyManager = null;
    private PhoneStateListener phoneStateListener = null;
    private MyTelephonyCallback callback = null;
    private EventChannel.EventSink eventSink;
    private final Context context;

    public GSMCallStateReceiver(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized GSMCallStateReceiver getInstance(Context context) {
        if (instance == null) {
            synchronized (GSMCallStateReceiver.class) {
                if (instance == null) {
                    instance = new GSMCallStateReceiver(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        eventSink = events;
    }

    @Override
    public void onCancel(Object arguments) {
        eventSink = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "get_current_call_state":
                FlutterResult response = FlutterResult.success("getCurrentCallState");
                response.put("state", phoneState.getValue());
                result.success(response.getMap());
                break;
            case "start_listen_gms_call_state":
                startListening();
                result.success(FlutterResult.success("startListenGmsCallState").getMap());
                break;
            case "stop_listen_gms_call_state":
                stopListening();
                result.success(FlutterResult.success("stopListenGmsCallState").getMap());
                break;
        }
    }

    public void startListening() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (telephonyManager == null) {
            telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        if (telephonyManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (callback == null) {
                    callback = new MyTelephonyCallback(eventSink);
                    telephonyManager.registerTelephonyCallback(context.getMainExecutor(), callback);
                }
            } else {
                if (phoneStateListener == null) {
                    phoneStateListener = new PhoneStateListener() {
                        @Override
                        public void onCallStateChanged(int state, String phoneNumber) {
                            phoneState = PhoneState.getState(state);
                            if (eventSink != null) {
                                eventSink.success(phoneState.getValue());
                            }
                        }
                    };
                    telephonyManager.listen(phoneStateListener,
                            PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }

    public void stopListening() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (telephonyManager == null) {
            telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (callback != null) {
                telephonyManager.unregisterTelephonyCallback(callback);
                callback = null;
            }
        } else {
            if (phoneStateListener != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                phoneStateListener = null;
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    public static class MyTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        private final EventChannel.EventSink eventSink;

        public MyTelephonyCallback(EventChannel.EventSink eventSink) {
            this.eventSink = eventSink;
        }

        @Override
        public void onCallStateChanged(int state) {
            phoneState = PhoneState.getState(state);
            if (eventSink != null) {
                eventSink.success(phoneState.getValue());
            }
        }
    }
}
