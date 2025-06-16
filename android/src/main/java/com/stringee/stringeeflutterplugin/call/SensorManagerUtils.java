package com.stringee.stringeeflutterplugin.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import com.stringee.stringeeflutterplugin.common.FlutterResult;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class SensorManagerUtils implements SensorEventListener, MethodChannel.MethodCallHandler {
    private static volatile SensorManagerUtils instance;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private final Context context;

    public static SensorManagerUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SensorManagerUtils.class) {
                if (instance == null) {
                    instance = new SensorManagerUtils(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private SensorManagerUtils(Context context) {
        this.context = context.getApplicationContext();
    }

    public SensorManagerUtils initialize() {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (mProximity == null) {
                mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            }
            mSensorManager.registerListener(instance, mProximity,
                    android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (powerManager == null) {
            powerManager = ((PowerManager) context.getSystemService(Context.POWER_SERVICE));

            int screenLockValue;

            screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(screenLockValue, context.getPackageName());
            }
        }
        return instance;
    }

    public void turnOn() {
        if (wakeLock != null) {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    public void turnOff() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    public void releaseSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "turn_on_sensor": {
                initialize().turnOn();
                result.success(FlutterResult.success("turnOnSensor").getMap());
                break;
            }
            case "turn_off_sensor": {
                initialize().turnOff();
                result.success(FlutterResult.success("turnOffSensor").getMap());
                break;
            }
            case "release_sensor": {
                initialize().releaseSensor();
                result.success(FlutterResult.success("releaseSensor").getMap());
                break;
            }
        }
    }
}
