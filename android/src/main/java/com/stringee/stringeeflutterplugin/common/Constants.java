package com.stringee.stringeeflutterplugin.common;

public class Constants {
    public static final String TAG = "Stringee native";

    public static String methodChannel = "com.stringee.flutter.methodchannel";
    public static String eventChannel = "com.stringee.flutter.eventchannel";

    public static String audioMethodChannel = "com.stringee.flutter.audio.method_channel";
    public static String audioEventChannel = "com.stringee.flutter.audio.event_channel";

    public static String notificationMethodChannel =
            "com.stringee.flutter.notification.method_channel";
    public static String notificationEventChannel =
            "com.stringee.flutter.notification.event_channel";

    public static String gsmMethodChannel = "com.stringee.flutter.gsm_call_state.method_channel";
    public static String gsmEventChannel = "com.stringee.flutter.gsm_call_state.event_channel";

    public static final String PREF_BASE = "com.stringee.flutter.";
    public static final String PREF_ACCESS_TOKEN = PREF_BASE + "access_token.";
    public static final String PREF_SERVER_ADDRESS = PREF_BASE + "server_address.";
    public static final String PREF_BASE_API_URL = PREF_BASE + "base_API_url.";

    public static final String PARAM_BASE = "com.stringee.flutter.param.";
    public static final String PARAM_UUID = PARAM_BASE + "uuid";
    public static final String PARAM_CALL_ID = PARAM_BASE + "call_id";
    public static final String PARAM_NOTIFICATION_ID = PARAM_BASE + "notification_id";

    public static final String ACTION_BASE = "com.stringee.flutter.action.";
    public static final String ACTION_START_FOREGROUND_SERVICE =
            ACTION_BASE + "start_foreground_service";
    public static final String ACTION_FULL_SCREEN_INTENT = ACTION_BASE + "full_screen_intent";
    public static final String ACTION_CLICK_NOTIFICATION = ACTION_BASE + "click_notification";
    public static final String ACTION_ANSWER_CALL = ACTION_BASE + "answer_call";
    public static final String ACTION_REJECT_CALL = ACTION_BASE + "reject_call";
    public static final String ACTION_HANG_UP_CALL = ACTION_BASE + "hang_up_call";
}
