package com.stringee.stringeeflutterplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEnventType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

import static com.stringee.stringeeflutterplugin.StringeeClientManager.StringeeCallType.*;

/**
 * com.stringee.stringeeflutterplugin.StringeeClientManager
 */
public class StringeeClientManager implements StringeeConnectionListener {
    private static StringeeClientManager _clientManager;
    private static Context _context;
    private static StringeeClient _client;
    private static StringeeManager _stringeeManager;
    private static Handler _handler;
    private static final String TAG = "Stringee";

    /**
     * @param context
     * @param stringeeManager
     * @param handler
     *
     * @return
     */
    public static synchronized StringeeClientManager getInstance(Context context, StringeeManager stringeeManager, Handler handler) {
        if (_clientManager == null) {
            _clientManager = new StringeeClientManager();
            _context = context;
            _stringeeManager = stringeeManager;
            _handler = handler;
        }
        return _clientManager;
    }

    public enum StringeeCallType {
        AppToAppOutgoing(0),
        AppToAppIncoming(1),
        AppToPhone(2),
        PhoneToApp(3);

        public final short value;

        StringeeCallType(int value) {
            this.value = (short) value;
        }

        public short getValue() {
            return this.value;
        }
    }

    /**
     * Connect to Stringee server
     *
     * @param token
     */
    public void connect(final String token) {
        _client = _stringeeManager.getClient();
        if (_client == null) {
            _client = new StringeeClient(_context);
            _stringeeManager.setClient(_client);
        }
        _client.setConnectionListener(this);
        _client.connect(token);
    }

    /**
     * Disconnect from Stringee server
     */
    public void disconnect() {
        if (_client != null) {
            _client.disconnect();
        }
    }

    /**
     * Register push notification
     *
     * @param registrationToken
     */
    public void registerPush(String registrationToken, final MethodChannel.Result result) {
        if (_client != null) {
            _client.registerPushToken(registrationToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        }
                    });
                }

                @Override
                public void onError(final StringeeError error) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        }
                    });
                }
            });
        }
    }

    /**
     * Unregister push notification
     *
     * @param registrationToken
     */
    public void unregisterPush(String registrationToken, final MethodChannel.Result result) {
        if (_client != null) {
            _client.unregisterPushToken(registrationToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        }
                    });
                }

                @Override
                public void onError(final StringeeError error) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        }
                    });
                }
            });
        }
    }

    /**
     * Send a custom message
     *
     * @param toUserId
     * @param jsonObject
     */
    public void sendCustomMessage(String toUserId, JSONObject jsonObject, final MethodChannel.Result result) {
        if (_client != null) {
            _client.sendCustomMessage(toUserId, jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        }
                    });
                }

                @Override
                public void onError(final StringeeError error) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Map map = new HashMap();
                            map.put("status", false);
                            map.put("code", error.getCode());
                            map.put("message", error.getMessage());
                            result.success(map);
                        }
                    });
                }
            });
        }
    }

    //listener
    @Override
    public void onConnectionConnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========Connected==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didConnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionDisconnected(final StringeeClient stringeeClient, final boolean b) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========Disconnected==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didDisconnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall(final StringeeCall stringeeCall) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========IncomingCall==========");
                _stringeeManager.getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "incomingCall");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                int callType = AppToAppOutgoing.getValue();
                if (!stringeeCall.getFrom().equals(_client.getUserId())) {
                    callType = AppToAppIncoming.getValue();
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = AppToPhone.getValue();
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = PhoneToApp.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall2(final StringeeCall2 stringeeCall2) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========IncomingCall2==========");
                _stringeeManager.getCall2sMap().put(stringeeCall2.getCallId(), stringeeCall2);
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "incomingCall2");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall2.getCallId());
                callInfoMap.put("from", stringeeCall2.getFrom());
                callInfoMap.put("to", stringeeCall2.getTo());
                callInfoMap.put("fromAlias", stringeeCall2.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall2.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall2.isVideoCall());
                int callType = AppToAppOutgoing.getValue();
                if (!stringeeCall2.getFrom().equals(_client.getUserId())) {
                    callType = AppToAppIncoming.getValue();
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall2.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ConnectionError==========\n" + "code: " + stringeeError.getCode() + " -message: " + stringeeError.getMessage());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didFailWithError");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("code", stringeeError.getCode());
                bodyMap.put("message", stringeeError.getMessage());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onRequestNewToken(final StringeeClient stringeeClient) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========RequestNewToken==========");
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "requestAccessToken");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onCustomMessage(final String from, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveCustomMessage==========\n" + jsonObject.toString());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didReceiveCustomMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onTopicMessage(final String from, final JSONObject jsonObject) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ReceiveTopicMessage==========\n" + jsonObject.toString());
                Map map = new HashMap();
                map.put("typeEvent", StringeeEnventType.ClientEvent.getValue());
                map.put("event", "didReceiveTopicMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
