package com.stringee.stringeeflutterplugin.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.ServiceCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stringee.stringeeflutterplugin.call.service.ActionCallService;
import com.stringee.stringeeflutterplugin.call.service.InCallService;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.PacketSenderThread;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.conference.service.ScreenCaptureService;
import com.stringee.stringeeflutterplugin.notification.model.InCallServiceInfo;
import com.stringee.stringeeflutterplugin.notification.model.IncomingCallNotiInfo;
import com.stringee.stringeeflutterplugin.notification.model.ScreenCaptureServiceInfo;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StringeeNotification implements MethodCallHandler, EventChannel.StreamHandler {
    private static volatile StringeeNotification instance;
    private final Context context;

    private InCallService inCallService;
    private Result inCallServiceResult;
    private InCallServiceInfo inCallServiceInfo;
    private ScreenCaptureService screenCaptureService;
    private Result screenCaptureServiceResult;
    private ScreenCaptureServiceInfo screenCaptureServiceInfo;

    public StringeeNotification(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized StringeeNotification getInstance(Context context) {
        if (instance == null) {
            synchronized (StringeeNotification.class) {
                if (instance == null && context != null) {
                    instance = new StringeeNotification(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onListen(Object arguments, EventSink events) {
        PacketSenderThread senderThread = PacketSenderThread.getInstance();
        senderThread.setEventSink(events);
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
            case "notify_incoming_call": {
                if (!NotificationUtils.canShowNotification(context)) {
                    result.success(FlutterResult.error("notifyIncomingCall", -4,
                            "Notification permission is not granted").getMap());
                    return;
                }
                try {
                    IncomingCallNotiInfo notiInfo =
                            NotificationUtils.getIncomingCallNotiInfo(call.arguments());
                    notifyIncomingCall(notiInfo, result);
                } catch (Exception e) {
                    result.success(FlutterResult.error("notifyIncomingCall", -101, e.getMessage())
                            .getMap());
                    Utils.reportException(StringeeNotification.class, e);
                }
                break;
            }
            case "cancel_notification":
                try {
                    cancelNotification(call.arguments());
                    result.success(FlutterResult.success("cancelNotification").getMap());
                } catch (Exception e) {
                    result.success(FlutterResult.error("cancelNotification", -101, e.getMessage())
                            .getMap());
                    Utils.reportException(StringeeNotification.class, e);
                }
                break;
            case "start_in_call_service": {
                if (!NotificationUtils.canShowNotification(context)) {
                    result.success(FlutterResult.error("startInCallService", -4,
                            "Notification permission is not granted").getMap());
                    return;
                }
                try {
                    inCallServiceInfo = NotificationUtils.getInCallServiceInfo(call.arguments());
                    inCallServiceResult = result;
                    Intent intent = new Intent(context, InCallService.class);
                    intent.setAction(Constants.ACTION_START_FOREGROUND_SERVICE);
                    if (VERSION.SDK_INT >= VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeNotification.class, e);
                    result.success(FlutterResult.error("startInCallService", -101, e.getMessage())
                            .getMap());
                }
                break;
            }
            case "stop_in_call_service":
                if (inCallService != null) {
                    inCallService.stopService();
                    inCallService = null;
                }
                result.success(FlutterResult.success("stopInCallService").getMap());
                break;
            case "start_screen_capture_service": {
                if (!NotificationUtils.canShowNotification(context)) {
                    result.success(FlutterResult.error("startScreenCaptureService", -4,
                            "Notification permission is not granted").getMap());
                    return;
                }
                try {
                    screenCaptureServiceInfo =
                            NotificationUtils.getScreenCaptureServiceInfo(call.arguments());
                    screenCaptureServiceResult = result;
                    Intent intent = new Intent(context, ScreenCaptureService.class);
                    intent.setAction(Constants.ACTION_START_FOREGROUND_SERVICE);
                    if (VERSION.SDK_INT >= VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                } catch (Exception e) {
                    Utils.reportException(StringeeNotification.class, e);
                    result.success(
                            FlutterResult.error("startScreenCaptureService", -101, e.getMessage())
                                    .getMap());
                }
                break;
            }
            case "stop_screen_capture_service":
                if (screenCaptureService != null) {
                    screenCaptureService.stopService();
                    screenCaptureService = null;
                }
                result.success(FlutterResult.success("stopScreenCaptureService").getMap());
                break;
        }
    }

    /**
     * Notify incoming call
     *
     * @param info   IncomingCallNotiInfo
     * @param result Result
     */
    private void notifyIncomingCall(IncomingCallNotiInfo info, Result result) {
        NotificationManager nm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm = context.getSystemService(NotificationManager.class);
        } else {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel = nm.getNotificationChannel(info.getChannelId());
            if (channel != null &&
                    !(channel.getImportance() == NotificationManager.IMPORTANCE_MAX ||
                            channel.getImportance() == NotificationManager.IMPORTANCE_HIGH)) {
                nm.deleteNotificationChannel(info.getChannelId());
            }
            channel = new NotificationChannel(info.getChannelId(), info.getChannelName(),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(info.getChannelDescription());
            channel.enableLights(info.isEnableLights());
            channel.enableVibration(info.isEnableVibration());
            if (info.isEnableVibration()) {
                long[] vibrationPattern = info.getVibrationPattern();
                if (vibrationPattern != null) {
                    if (vibrationPattern.length > 0) {
                        channel.setVibrationPattern(vibrationPattern);
                    }
                }
            }
            channel.setLockscreenVisibility(info.getLockscreenVisibility());
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(
                            AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            int sourceType = info.getSourceType();
            if (sourceType == 0) {
                if (NotificationUtils.isResourceAvailable(context, info.getSoundSource(), "raw")) {
                    soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" +
                            info.getSoundSource());
                } else {
                    Log.d(Constants.TAG, "No sound resource found");
                }
            } else if (sourceType == 1) {
                soundUri = Uri.parse(info.getSoundSource());
            }
            channel.setSound(soundUri, audioAttributes);
            channel.setBypassDnd(true);
            nm.createNotificationChannel(channel);
        }
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        Intent fullScreenIntent = NotificationUtils.getLaunchIntent(context);
        fullScreenIntent.setAction(Constants.ACTION_CLICK_NOTIFICATION);
        fullScreenIntent.putExtra(Constants.PARAM_CALL_ID, info.getCallId());
        fullScreenIntent.putExtra(Constants.PARAM_UUID, info.getClientId());
        fullScreenIntent.putExtra(Constants.PARAM_NOTIFICATION_ID, info.getId());
        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        fullScreenIntent, flag);

        Intent rejectIntent = new Intent(context, ActionCallService.class);
        rejectIntent.setAction(Constants.ACTION_REJECT_CALL);
        rejectIntent.putExtra(Constants.PARAM_CALL_ID, info.getCallId());
        rejectIntent.putExtra(Constants.PARAM_UUID, info.getClientId());
        rejectIntent.putExtra(Constants.PARAM_NOTIFICATION_ID, info.getId());
        PendingIntent rejectPendingIntent =
                PendingIntent.getService(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        rejectIntent, flag);

        Intent answerIntent = NotificationUtils.getLaunchIntent(context);
        answerIntent.setAction(Constants.ACTION_ANSWER_CALL);
        answerIntent.putExtra(Constants.PARAM_CALL_ID, info.getCallId());
        answerIntent.putExtra(Constants.PARAM_UUID, info.getClientId());
        answerIntent.putExtra(Constants.PARAM_NOTIFICATION_ID, info.getId());
        PendingIntent answerPendingIntent =
                PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        answerIntent, flag);

        String name = info.getFromName();
        String number = info.getFromNumber();
        String avatarUrl = info.getFromAvatarUrl();
        if (Utils.isEmpty(name)) {
            name = number;
        }
        IconCompat avatar = null;
        if (!Utils.isEmpty(avatarUrl)) {
            RequestOptions options = new RequestOptions().circleCrop();
            Callable<Bitmap> callable = () -> Glide.with(context)
                    .asBitmap()
                    .apply(options)
                    .load(avatarUrl)
                    .submit()
                    .get();

            FutureTask<Bitmap> futureTask = new FutureTask<>(callable);
            Thread thread = new Thread(futureTask);
            thread.start();

            try {
                Bitmap avatarBitmap = futureTask.get();
                if (avatarBitmap != null) {
                    avatar = IconCompat.createWithBitmap(avatarBitmap);
                }
            } catch (Exception e) {
                Utils.reportException(StringeeNotification.class, e);
            }
        }

        Person.Builder personBuilder = new Person.Builder();
        personBuilder.setName(name);
        personBuilder.setImportant(true);
        if (avatar != null) {
            personBuilder.setIcon(avatar);
        }
        Person person = personBuilder.build();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, info.getChannelId());
        // Style for incoming call
        builder.setStyle(NotificationCompat.CallStyle.forIncomingCall(person, rejectPendingIntent,
                answerPendingIntent).setIsVideo(false));
        // Set person for notification
        builder.addPerson(person);
        // Small icon
        int iconResourceId = NotificationUtils.getDefaultIconResourceId(context);
        if (!Utils.isEmpty(info.getIconSource())) {
            if (NotificationUtils.isResourceAvailable(context, info.getIconSource(),
                    info.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                iconResourceId = NotificationUtils.getIconResourceId(context, info.getIconSource(),
                        info.getSourceFrom() == 0 ? "drawable" : "mipmap");
            }
        }
        builder.setSmallIcon(iconResourceId);
        // ContentTitle
        builder.setContentTitle(
                Utils.isEmpty(info.getContentTitle()) ? name : info.getContentTitle());
        // ContentText
        builder.setContentText(
                Utils.isEmpty(info.getContentText()) ? number : info.getContentText());
        // Auto cancel
        builder.setAutoCancel(true);
        // Show when
        builder.setShowWhen(false);
        // Ongoing
        builder.setOngoing(true);
        // Content intent
        builder.setContentIntent(fullScreenPendingIntent);
        // Priority
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        // Category
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        // Full screen intent
        builder.setFullScreenIntent(fullScreenPendingIntent, true);

        Notification notification = builder.build();
        nm.notify(info.getId(), notification);

        result.success(FlutterResult.success("notifyIncomingCall").getMap());
    }

    /**
     * Cancel notification by id
     *
     * @param notificationId Notification id
     */
    public void cancelNotification(int notificationId) {
        NotificationManager nm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm = context.getSystemService(NotificationManager.class);
        } else {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        nm.cancel(notificationId);
    }

    /**
     * Start InCallService
     * Add on Manifest:
     * <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE"/>
     * <service
     * android:name="com.stringee.stringeeflutterplugin.call.service.InCallService"
     * android:exported="false"
     * android:foregroundServiceType="microphone"/>
     */
    public void startInCallService(InCallService inCallService) {
        this.inCallService = inCallService;
        NotificationManager nm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm = context.getSystemService(NotificationManager.class);
        } else {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(inCallServiceInfo.getChannelId(),
                    inCallServiceInfo.getChannelName(), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(inCallServiceInfo.getLockscreenVisibility());
            channel.setDescription(inCallServiceInfo.getChannelDescription());
            channel.setBypassDnd(true);
            channel.enableVibration(false);
            channel.setSound(null, null);
            nm.createNotificationChannel(channel);
        }

        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        Intent fullScreenIntent = NotificationUtils.getLaunchIntent(context);
        fullScreenIntent.setAction(Constants.ACTION_CLICK_NOTIFICATION);
        fullScreenIntent.putExtra(Constants.PARAM_CALL_ID, inCallServiceInfo.getCallId());
        fullScreenIntent.putExtra(Constants.PARAM_UUID, inCallServiceInfo.getClientId());
        fullScreenIntent.putExtra(Constants.PARAM_NOTIFICATION_ID, inCallServiceInfo.getId());
        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        fullScreenIntent, flag);

        Intent endIntent = new Intent(context, ActionCallService.class);
        endIntent.setAction(Constants.ACTION_HANG_UP_CALL);
        endIntent.putExtra(Constants.PARAM_CALL_ID, inCallServiceInfo.getCallId());
        endIntent.putExtra(Constants.PARAM_UUID, inCallServiceInfo.getClientId());
        endIntent.putExtra(Constants.PARAM_NOTIFICATION_ID, inCallServiceInfo.getId());
        PendingIntent endPendingIntent =
                PendingIntent.getService(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        endIntent, flag);

        String name = inCallServiceInfo.getName();
        String number = inCallServiceInfo.getNumber();
        String avatarUrl = inCallServiceInfo.getAvatarUrl();
        if (Utils.isEmpty(name)) {
            name = number;
        }
        IconCompat avatar = null;
        if (!Utils.isEmpty(avatarUrl)) {
            RequestOptions options = new RequestOptions().circleCrop();
            Callable<Bitmap> callable = () -> Glide.with(context)
                    .asBitmap()
                    .apply(options)
                    .load(avatarUrl)
                    .submit()
                    .get();

            FutureTask<Bitmap> futureTask = new FutureTask<>(callable);
            Thread thread = new Thread(futureTask);
            thread.start();

            try {
                Bitmap avatarBitmap = futureTask.get();
                if (avatarBitmap != null) {
                    avatar = IconCompat.createWithBitmap(avatarBitmap);
                }
            } catch (Exception e) {
                Utils.reportException(StringeeNotification.class, e);
            }
        }

        Person.Builder personBuilder = new Person.Builder();
        personBuilder.setName(name);
        personBuilder.setImportant(true);
        if (avatar != null) {
            personBuilder.setIcon(avatar);
        }
        Person person = personBuilder.build();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, inCallServiceInfo.getChannelId());
        // Style for in call
        builder.setStyle(NotificationCompat.CallStyle.forOngoingCall(person, endPendingIntent)
                .setIsVideo(false));
        // Set person for notification
        builder.addPerson(person);
        // Small icon
        int iconResourceId = NotificationUtils.getDefaultIconResourceId(context);
        if (!Utils.isEmpty(inCallServiceInfo.getIconSource())) {
            if (NotificationUtils.isResourceAvailable(context, inCallServiceInfo.getIconSource(),
                    inCallServiceInfo.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                iconResourceId = NotificationUtils.getIconResourceId(context,
                        inCallServiceInfo.getIconSource(),
                        inCallServiceInfo.getSourceFrom() == 0 ? "drawable" : "mipmap");
            }
        }
        builder.setSmallIcon(iconResourceId);
        // ContentTitle
        builder.setContentTitle(Utils.isEmpty(
                inCallServiceInfo.getContentTitle()) ? name : inCallServiceInfo.getContentTitle());
        // ContentText
        builder.setContentText(Utils.isEmpty(
                inCallServiceInfo.getContentText()) ? number : inCallServiceInfo.getContentText());
        // Auto cancel
        builder.setAutoCancel(false);
        // Show when
        builder.setShowWhen(false);
        // Ongoing
        builder.setOngoing(true);
        // Content intent
        builder.setContentIntent(fullScreenPendingIntent);
        // Priority
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Category
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        // Full screen intent
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
        Notification notification = builder.build();

        int type = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
        }
        try {
            ServiceCompat.startForeground(inCallService, inCallServiceInfo.getId(), notification,
                    type);
            if (inCallServiceResult != null) {
                inCallServiceResult.success(FlutterResult.success("startInCallService").getMap());
            }
        } catch (Exception exception) {
            if (inCallServiceResult != null) {
                inCallServiceResult.success(
                        FlutterResult.error("startInCallService", -1, exception.getMessage())
                                .getMap());
            }
            Utils.reportException(InCallService.class, exception);
        }
    }

    /**
     * Start ScreenCaptureService
     * Add on Manifest:
     * <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"/>
     * <service
     * android:name="com.stringee.stringeeflutterplugin.call.service.ScreenCaptureService"
     * android:exported="false"
     * android:foregroundServiceType="mediaProjection"/>
     */
    public void startScreenCaptureService(ScreenCaptureService screenCaptureService) {
        this.screenCaptureService = screenCaptureService;
        NotificationManager nm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm = context.getSystemService(NotificationManager.class);
        } else {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(screenCaptureServiceInfo.getChannelId(),
                            screenCaptureServiceInfo.getChannelName(),
                            NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(screenCaptureServiceInfo.getChannelDescription());
            channel.setLockscreenVisibility(screenCaptureServiceInfo.getLockscreenVisibility());
            channel.setBypassDnd(true);
            channel.enableVibration(false);
            channel.setSound(null, null);
            nm.createNotificationChannel(channel);
        }

        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }

        Intent intent = NotificationUtils.getLaunchIntent(context);
        intent.setAction(Constants.ACTION_CLICK_NOTIFICATION);
        intent.putExtra(Constants.PARAM_CALL_ID, screenCaptureServiceInfo.getCallId());
        intent.putExtra(Constants.PARAM_UUID, screenCaptureServiceInfo.getClientId());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                        intent, flag);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, screenCaptureServiceInfo.getChannelId());
        // Small icon
        int iconResourceId = NotificationUtils.getDefaultIconResourceId(context);
        if (!Utils.isEmpty(screenCaptureServiceInfo.getIconSource())) {
            if (NotificationUtils.isResourceAvailable(context,
                    screenCaptureServiceInfo.getIconSource(),
                    screenCaptureServiceInfo.getSourceFrom() == 0 ? "drawable" : "mipmap")) {
                iconResourceId = NotificationUtils.getIconResourceId(context,
                        screenCaptureServiceInfo.getIconSource(),
                        screenCaptureServiceInfo.getSourceFrom() == 0 ? "drawable" : "mipmap");
            }
        }
        builder.setSmallIcon(iconResourceId);
        // ContentTitle
        builder.setContentTitle(screenCaptureServiceInfo.getContentTitle());
        // ContentText
        builder.setContentText(screenCaptureServiceInfo.getContentText());
        // Ongoing
        builder.setOngoing(true);
        // Content intent
        builder.setContentIntent(pendingIntent);
        // Priority
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.build();

        int type = 0;
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
        }
        try {
            ServiceCompat.startForeground(screenCaptureService, screenCaptureServiceInfo.getId(),
                    notification, type);
            if (screenCaptureServiceResult != null) {
                screenCaptureServiceResult.success(
                        FlutterResult.success("startScreenCaptureService").getMap());
            }
        } catch (Exception exception) {
            if (screenCaptureServiceResult != null) {
                screenCaptureServiceResult.success(
                        FlutterResult.error("startScreenCaptureService", -1, exception.getMessage())
                                .getMap());
            }
            Utils.reportException(InCallService.class, exception);
        }
    }
}
