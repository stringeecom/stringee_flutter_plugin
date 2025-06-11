package com.stringee.stringeeflutterplugin.notification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.notification.model.InCallServiceInfo;
import com.stringee.stringeeflutterplugin.notification.model.IncomingCallNotiInfo;
import com.stringee.stringeeflutterplugin.notification.model.ScreenCaptureServiceInfo;

import java.util.List;
import java.util.Map;

public class NotificationUtils {

    public static IncomingCallNotiInfo getIncomingCallNotiInfo(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        IncomingCallNotiInfo info = new IncomingCallNotiInfo();
        info.setChannelId((String) map.get("channelId"));
        info.setChannelName((String) map.get("channelName"));
        info.setChannelDescription((String) map.get("description"));
        info.setEnableLights(Boolean.TRUE.equals(map.get("enableLights")));
        info.setEnableVibration(Boolean.TRUE.equals(map.get("enableVibration")));
        try {
            List<Integer> vibrationPatternList = (List<Integer>) map.get("vibrationPattern");
            if (!Utils.isEmpty(vibrationPatternList)) {
                long[] vibrationPattern = new long[vibrationPatternList.size()];
                for (int i = 0; i < vibrationPatternList.size(); i++) {
                    vibrationPattern[i] = vibrationPatternList.get(i).longValue();
                }
                info.setVibrationPattern(vibrationPattern);
            }
        } catch (Exception e) {
            Utils.reportException(NotificationUtils.class, e);
        }
        try {
            info.setLockscreenVisibility((Integer) map.get("lockscreenVisibility"));
        } catch (Exception e) {
            Utils.reportException(NotificationUtils.class, e);
        }
        Map<String,Object> notificationSound = (Map<String,Object>) map.get("notificationSound");
        if (notificationSound != null) {
            info.setSoundSource((String) notificationSound.get("source"));
            info.setSourceType((Integer) notificationSound.get("sourceType"));
        }
        info.setId((Integer) map.get("id"));
        info.setContentTitle((String) map.get("contentTitle"));
        info.setContentText((String) map.get("contentText"));
        Map<String,Object> iconMap = (Map) map.get("icon");
        if (iconMap != null) {
            info.setIconSource((String) iconMap.get("source"));
            info.setSourceFrom((Integer) iconMap.get("sourceFrom"));
        }
        info.setFromName((String) map.get("fromName"));
        info.setFromNumber((String) map.get("fromNumber"));
        info.setFromAvatarUrl((String) map.get("fromAvatarUrl"));
        info.setClientId((String) map.get("uuid"));
        info.setCallId((String) map.get("callId"));
        return info;
    }

    public static InCallServiceInfo getInCallServiceInfo(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        InCallServiceInfo info = new InCallServiceInfo();
        info.setChannelId((String) map.get("channelId"));
        info.setChannelName((String) map.get("channelName"));
        info.setChannelDescription((String) map.get("description"));
        try {
            info.setLockscreenVisibility((Integer) map.get("lockscreenVisibility"));
        } catch (Exception e) {
            Utils.reportException(NotificationUtils.class, e);
        }
        info.setId((Integer) map.get("id"));
        info.setContentTitle((String) map.get("contentTitle"));
        info.setContentText((String) map.get("contentText"));
        Map<String,Object> iconMap = (Map) map.get("icon");
        if (iconMap != null) {
            info.setIconSource((String) iconMap.get("source"));
            info.setSourceFrom((Integer) iconMap.get("sourceFrom"));
        }
        info.setName((String) map.get("name"));
        info.setNumber((String) map.get("number"));
        info.setAvatarUrl((String) map.get("avatarUrl"));
        info.setClientId((String) map.get("uuid"));
        info.setCallId((String) map.get("callId"));
        return info;
    }

    public static ScreenCaptureServiceInfo getScreenCaptureServiceInfo(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        ScreenCaptureServiceInfo info = new ScreenCaptureServiceInfo();
        info.setChannelId((String) map.get("channelId"));
        info.setChannelName((String) map.get("channelName"));
        info.setChannelDescription((String) map.get("description"));
        try {
            info.setLockscreenVisibility((Integer) map.get("lockscreenVisibility"));
        } catch (Exception e) {
            Utils.reportException(NotificationUtils.class, e);
        }
        info.setId((Integer) map.get("id"));
        info.setContentTitle((String) map.get("contentTitle"));
        info.setContentText((String) map.get("contentText"));
        Map<String,Object> iconMap = (Map) map.get("icon");
        if (iconMap != null) {
            info.setIconSource((String) iconMap.get("source"));
            info.setSourceFrom((Integer) iconMap.get("sourceFrom"));
        }
        info.setClientId((String) map.get("uuid"));
        info.setCallId((String) map.get("callId"));
        return info;
    }

    public static boolean isResourceAvailable(Context context, String source,
                                              String sourceFrom) throws RuntimeException {
        try {
            int id = context.getResources()
                    .getIdentifier(source, sourceFrom, context.getPackageName());
            return id != 0;
        } catch (Exception e) {
            Utils.reportException(NotificationUtils.class, e);
            return false;
        }
    }

    public static int getIconResourceId(Context context, String source, String sourceFrom) {
        return context.getResources().getIdentifier(source, sourceFrom, context.getPackageName());
    }

    public static int getDefaultIconResourceId(Context context) {
        return context.getResources()
                .getIdentifier("ic_launcher", "mipmap", context.getPackageName());
    }

    public static Intent getLaunchIntent(Context context) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        return packageManager.getLaunchIntentForPackage(packageName);
    }

    public static boolean canShowNotification(Context context) {
        boolean canShowNotification = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            canShowNotification = ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        }
        return canShowNotification;
    }
}
