package com.stringee.stringeeflutterpluginexample;


import android.content.Intent;

import androidx.annotation.NonNull;

import com.stringee.stringeeflutterplugin.notification.ActionReceiver;
import com.stringee.stringeeflutterplugin.notification.StringeeNotification;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;

public class MainActivity extends FlutterActivity {
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getAction().equals(StringeeNotification.STRINGEE_NOTIFICATION_ACTION)) {
            Intent intent = new Intent(this, ActionReceiver.class);
            intent.putExtra(StringeeNotification.STRINGEE_NOTIFICATION_ACTION_ID, getIntent().getIntExtra(StringeeNotification.STRINGEE_NOTIFICATION_ACTION_ID, 0));
            sendBroadcast(intent);
        }

    }
}
