package com.stringee.stringeeflutterplugin;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeNotification implements MethodCallHandler, EventChannel.StreamHandler {
    private static StringeeNotification _instance;

    public static EventSink _eventSink;
    public static MethodChannel _channel;

    private StringeeManager _manager;
    private NotificationManager _notificationManager;
    private PacketSenderThread senderThread;
    public static Result startForegroundServiceResult;
    public static Result stopForegroundServiceResult;

    public static final String STRINGEE_NOTIFICATION_ACTION = "com.stringee.flutter.notification.action";
    public static final String STRINGEE_NOTIFICATION_ACTION_ID = "com.stringee.flutter.notification.action.id";
    public static final String STRINGEE_START_FOREGROUND_SERVICE = "con.stringee.flutter.foregroundservice.start";
    public static final String STRINGEE_STOP_FOREGROUND_SERVICE = "con.stringee.flutter.foregroundservice.stop";
    private static final String TAG = "StringeeSDK";

    public StringeeNotification() {
        _manager = StringeeManager.getInstance();

        senderThread = PacketSenderThread.getInstance();

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            _notificationManager = _manager.getContext().getSystemService(NotificationManager.class);
        } else {
            _notificationManager = (NotificationManager) _manager.getContext().getSystemService(NOTIFICATION_SERVICE);
        }
    }

    public static synchronized StringeeNotification getInstance() {
        if (_instance == null) {
            _instance = new StringeeNotification();
        }
        return _instance;
    }


    @Override
    public void onListen(Object arguments, EventSink events) {
        _eventSink = events;
        senderThread.setEventSink(_eventSink);
        if (!senderThread.isRunning()) {
            senderThread.start();
        }
        senderThread.sendAllPacketInQueue();

    }

    @Override
    public void onCancel(Object arguments) {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "createChannel":
                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                    createChannel(Utils.getChannelInfo(call.arguments()), result);
                } else {
                    Log.d(TAG, "createChannel: false - -5 - This feature requires android api level >= 26");
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -5);
                    map.put("message", "This feature requires android api level >= 26");
                    result.success(map);
                }
                break;
            case "showNotification":
                showNotification(Utils.getNotificationInfo(call.arguments()), result);
                break;
            case "cancel":
                cancel(call.arguments(), result);
                break;
            case "startForegroundService":
                startForegroundService(Utils.getNotificationInfo(call.arguments()), result);
                break;
            case "stopForegroundService":
                stopForegroundService(result);
                break;
        }
    }

    /**
     * Create notification channel
     *
     * @param channelInfo
     * @param result
     */
    @RequiresApi(api = VERSION_CODES.O)
    private void createChannel(ChannelInfo channelInfo, Result result) {
        NotificationChannel channel;
        channel = _notificationManager.getNotificationChannel(channelInfo.getChannelId());
        if (channel != null && channel.getImportance() != channelInfo.getImportance()) {
            _notificationManager.deleteNotificationChannel(channelInfo.getChannelId());
        } else {
            channel = new NotificationChannel(channelInfo.getChannelId(), channelInfo.getChannelName(), channelInfo.getImportance());
        }
        channel.setDescription(channelInfo.getDescription());
        channel.enableLights(channelInfo.isEnableLights());
        channel.enableVibration(channelInfo.isEnableVibration());
        channel.setImportance(channelInfo.getImportance());
        if (channelInfo.isEnableVibration()) {
            long[] vibrationPattern = channelInfo.getVibrationPattern();
            if (vibrationPattern != null) {
                if (vibrationPattern.length > 0) {
                    channel.setVibrationPattern(vibrationPattern);
                }
            }
        }
        channel.setLockscreenVisibility(channelInfo.getLockscreenVisibility());
        if (channelInfo.isPlaySound()) {
            AudioAttributes audioAttributes = new Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            int sourceType = channelInfo.getSourceType();
            switch (sourceType) {
                case 0:
                    if (Utils.isResourceAvailable(_manager.getContext(), channelInfo.getSoundSource(), "raw")) {
                        channel.setSound((Uri.parse("android.resource://" + _manager.getContext().getPackageName() + "/" + channelInfo.getSoundSource())), audioAttributes);
                    } else {
                        Log.d(TAG, "createChannel: false - -3 - No resource found");
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", -3);
                        map.put("message", "No resource found");
                        result.success(map);
                        return;
                    }
                    break;
                case 1:
                    channel.setSound(Uri.parse(channelInfo.getSoundSource()), audioAttributes);
                    break;
                case 2:
                    channel.setSound(Uri.parse(RingtoneManager.getDefaultUri(channelInfo.getRingtoneType()).toString()), audioAttributes);
                    break;
            }
        }
        channel.setBypassDnd(channelInfo.isBypassDnd());
        _notificationManager.createNotificationChannel(channel);

        Log.d(TAG, "createChannel: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Show notification
     *
     * @param notiInfo
     * @param result
     */
    private void showNotification(NotificationInfo notiInfo, Result result) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(_manager.getContext(), notiInfo.getChannelId());

        Intent intent = Utils.getLaunchIntent(_manager.getContext());
        intent.setAction(STRINGEE_NOTIFICATION_ACTION + "." + notiInfo.getId());
        intent.putExtra(STRINGEE_NOTIFICATION_ACTION_ID, String.valueOf(notiInfo.getId()));
        PendingIntent pendingIntent = PendingIntent.getActivity(_manager.getContext(), notiInfo.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        if (notiInfo.getContentTitle() != null) {
            builder.setContentTitle(notiInfo.getContentTitle());
        }

        if (notiInfo.getContentText() != null) {
            builder.setContentText(notiInfo.getContentText());
        }

        if (notiInfo.getSubText() != null) {
            builder.setSubText(notiInfo.getSubText());
        }

        if (notiInfo.getContentInfo() != null) {
            builder.setContentInfo(notiInfo.getContentInfo());
        }

        if (notiInfo.getNumber() > 0) {
            builder.setNumber(notiInfo.getNumber());
        }

        builder.setAutoCancel(notiInfo.isAutoCancel());

        if (notiInfo.isShowWhen()) {
            builder.setShowWhen(true);
            builder.setWhen(notiInfo.getWhen());
        }

        if (notiInfo.getIconSource() != null) {
            if (Utils.isResourceAvailable(_manager.getContext(), notiInfo.getIconSource(), notiInfo.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                builder.setSmallIcon(Utils.getIconResourceId(_manager.getContext(), notiInfo.getIconSource(), notiInfo.getSourceFrom()));
            } else {
                Log.d(TAG, "showNotification: false - -3 - No icon resource found");
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", -3);
                map.put("message", "No icon resource found");
                result.success(map);
                return;
            }
        } else {
            builder.setSmallIcon(Utils.getDefaultIconResourceId(_manager.getContext()));
        }

        if (notiInfo.isPlaySound()) {
            int sourceType = notiInfo.getSourceType();
            switch (sourceType) {
                case 0:
                    if (Utils.isResourceAvailable(_manager.getContext(), notiInfo.getSoundSource(), "raw")) {
                        builder.setSound((Uri.parse("android.resource://" + _manager.getContext().getPackageName() + "/" + notiInfo.getSoundSource())));
                    } else {
                        Log.d(TAG, "showNotification: false - -3 - No sound resource found");
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", -3);
                        map.put("message", "No sound resource found");
                        result.success(map);
                        return;
                    }
                    break;
                case 1:
                    builder.setSound(Uri.parse(notiInfo.getSoundSource()));
                    break;
                case 2:
                    builder.setSound(Uri.parse(RingtoneManager.getDefaultUri(notiInfo.getRingtoneType()).toString()));
                    break;
            }
        }

        if (notiInfo.getCategory() != null) {
            builder.setCategory(notiInfo.getCategory());
        }

        if (notiInfo.isFullScreenIntent()) {
            builder.setFullScreenIntent(pendingIntent, true);
        }

        long[] vibrationPattern = notiInfo.getVibrationPattern();
        if (vibrationPattern != null) {
            if (vibrationPattern.length > 0) {
                builder.setVibrate(vibrationPattern);
            }
        }

        builder.setOngoing(notiInfo.isOnGoing());

        builder.setOnlyAlertOnce(notiInfo.isOnlyAlertOnce());

        if (notiInfo.isTimeoutAfter()) {
            builder.setTimeoutAfter(notiInfo.getTimeoutAfter());
        }

        builder.setPriority(notiInfo.getPriority());

        if (notiInfo.getActions() != null) {
            List<NotificationAction> actions = notiInfo.getActions();
            for (int i = 0; i < actions.size(); i++) {
                NotificationAction action = actions.get(i);
                int iconResourceId;
                if (action.getIcon() != null) {
                    if (Utils.isResourceAvailable(_manager.getContext(), action.getIcon(), action.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                        iconResourceId = Utils.getIconResourceId(_manager.getContext(), action.getIcon(), action.getSourceFrom());
                    } else {
                        Log.d(TAG, "showNotification: false - -3 - No action icon resource found");
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", -3);
                        map.put("message", "No action icon resource found");
                        result.success(map);
                        return;
                    }
                } else {
                    iconResourceId = Utils.getDefaultIconResourceId(_manager.getContext());
                }
                Intent actionIntent = Utils.getLaunchIntent(_manager.getContext());
                actionIntent.setAction(STRINGEE_NOTIFICATION_ACTION + "." + action.getId());
                actionIntent.putExtra(STRINGEE_NOTIFICATION_ACTION_ID, action.getId());
                PendingIntent actionPendingIntent = PendingIntent.getActivity(_manager.getContext(), notiInfo.getId(), actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.addAction(iconResourceId, action.getTitle(), actionPendingIntent);
            }
        }

        _notificationManager.notify(notiInfo.getId(), builder.build());

        Log.d(TAG, "showNotification: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Cancel notification
     *
     * @param notificationId
     * @param result
     */
    private void cancel(int notificationId, Result result) {
        _notificationManager.cancel(notificationId);

        Log.d(TAG, "cancel: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * start foreground service
     *
     * @param notiInfo
     * @param result
     */
    private void startForegroundService(NotificationInfo notiInfo, Result result) {
        startForegroundServiceResult = result;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(_manager.getContext(), notiInfo.getChannelId());

        Intent contentIntent = Utils.getLaunchIntent(_manager.getContext());
        contentIntent.setAction(STRINGEE_NOTIFICATION_ACTION + "." + notiInfo.getId());
        contentIntent.putExtra(STRINGEE_NOTIFICATION_ACTION_ID, String.valueOf(notiInfo.getId()));
        PendingIntent pendingIntent = PendingIntent.getActivity(_manager.getContext(), notiInfo.getId(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        if (notiInfo.getContentTitle() != null) {
            builder.setContentTitle(notiInfo.getContentTitle());
        }

        if (notiInfo.getContentText() != null) {
            builder.setContentText(notiInfo.getContentText());
        }

        if (notiInfo.getSubText() != null) {
            builder.setSubText(notiInfo.getSubText());
        }

        if (notiInfo.getContentInfo() != null) {
            builder.setContentInfo(notiInfo.getContentInfo());
        }

        if (notiInfo.getNumber() > 0) {
            builder.setNumber(notiInfo.getNumber());
        }

        builder.setAutoCancel(notiInfo.isAutoCancel());

        if (notiInfo.isShowWhen()) {
            builder.setShowWhen(true);
            builder.setWhen(notiInfo.getWhen());
        }

        if (notiInfo.getIconSource() != null) {
            if (Utils.isResourceAvailable(_manager.getContext(), notiInfo.getIconSource(), notiInfo.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                builder.setSmallIcon(Utils.getIconResourceId(_manager.getContext(), notiInfo.getIconSource(), notiInfo.getSourceFrom()));
            } else {
                Log.d(TAG, "showNotification: false - -3 - No icon resource found");
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", -3);
                map.put("message", "No icon resource found");
                result.success(map);
                return;
            }
        } else {
            builder.setSmallIcon(Utils.getDefaultIconResourceId(_manager.getContext()));
        }

        if (notiInfo.isPlaySound()) {
            int sourceType = notiInfo.getSourceType();
            switch (sourceType) {
                case 0:
                    if (Utils.isResourceAvailable(_manager.getContext(), notiInfo.getSoundSource(), "raw")) {
                        builder.setSound((Uri.parse("android.resource://" + _manager.getContext().getPackageName() + "/" + notiInfo.getSoundSource())));
                    } else {
                        Log.d(TAG, "showNotification: false - -3 - No sound resource found");
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", -3);
                        map.put("message", "No sound resource found");
                        result.success(map);
                        return;
                    }
                    break;
                case 1:
                    builder.setSound(Uri.parse(notiInfo.getSoundSource()));
                    break;
                case 2:
                    builder.setSound(Uri.parse(RingtoneManager.getDefaultUri(notiInfo.getRingtoneType()).toString()));
                    break;
            }
        }

        if (notiInfo.getCategory() != null) {
            builder.setCategory(notiInfo.getCategory());
        }

        if (notiInfo.isFullScreenIntent()) {
            builder.setFullScreenIntent(pendingIntent, true);
        }

        long[] vibrationPattern = notiInfo.getVibrationPattern();
        if (vibrationPattern != null) {
            if (vibrationPattern.length > 0) {
                builder.setVibrate(vibrationPattern);
            }
        }

        builder.setOngoing(notiInfo.isOnGoing());

        builder.setOnlyAlertOnce(notiInfo.isOnlyAlertOnce());

        if (notiInfo.isTimeoutAfter()) {
            builder.setTimeoutAfter(notiInfo.getTimeoutAfter());
        }

        builder.setPriority(notiInfo.getPriority());

        if (notiInfo.getActions() != null) {
            List<NotificationAction> actions = notiInfo.getActions();
            for (int i = 0; i < actions.size(); i++) {
                NotificationAction action = actions.get(i);
                int iconResourceId;
                if (action.getIcon() != null) {
                    if (Utils.isResourceAvailable(_manager.getContext(), action.getIcon(), action.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                        iconResourceId = Utils.getIconResourceId(_manager.getContext(), action.getIcon(), action.getSourceFrom());
                    } else {
                        Log.d(TAG, "showNotification: false - -3 - No action icon resource found");
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", -3);
                        map.put("message", "No action icon resource found");
                        result.success(map);
                        return;
                    }
                } else {
                    iconResourceId = Utils.getDefaultIconResourceId(_manager.getContext());
                }
                Intent actionIntent = Utils.getLaunchIntent(_manager.getContext());
                actionIntent.setAction(STRINGEE_NOTIFICATION_ACTION + "." + action.getId());
                actionIntent.putExtra(STRINGEE_NOTIFICATION_ACTION_ID, action.getId());
                PendingIntent actionPendingIntent = PendingIntent.getActivity(_manager.getContext(), notiInfo.getId(), actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(iconResourceId, action.getTitle(), actionPendingIntent);
            }
        }

        Intent intent = new Intent(_manager.getContext(), StringeeForegroundService.class);
        intent.setAction(STRINGEE_START_FOREGROUND_SERVICE);
        intent.putExtra("notification", builder.build());
        intent.putExtra("id", notiInfo.getId());
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            _manager.getContext().startForegroundService(intent);
        } else {
            _manager.getContext().startService(intent);
        }
    }

    /**
     * Stop foreground service
     *
     * @param result
     */
    private void stopForegroundService(Result result) {
        stopForegroundServiceResult = result;
        Intent intent = new Intent(_manager.getContext(), StringeeForegroundService.class);
        intent.setAction(STRINGEE_STOP_FOREGROUND_SERVICE);
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            _manager.getContext().startForegroundService(intent);
        } else {
            _manager.getContext().startService(intent);
        }
    }
}
