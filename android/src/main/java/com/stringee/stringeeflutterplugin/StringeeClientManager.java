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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

/**
 * com.stringee.stringeeflutterplugin.StringeeClientManager
 */
public class StringeeClientManager implements StringeeConnectionListener {
    private static StringeeClientManager _clientManager;
    private static Context _context;
    private static StringeeClient _client;
    private static com.stringee.stringeeflutterplugin.StringeeManager _stringeeManager;
    private static Handler _handler;
    private static final String TAG = "Stringee";

    /**
     * @param context
     * @param stringeeManager
     * @param handler
     *
     * @return
     */
    public static synchronized StringeeClientManager getInstance(Context context, com.stringee.stringeeflutterplugin.StringeeManager stringeeManager, Handler handler) {
        if (_clientManager == null) {
            _clientManager = new StringeeClientManager();
            _context = context;
            _stringeeManager = stringeeManager;
            _handler = handler;
        }
        return _clientManager;
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
                map.put("event", "didConnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
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
                map.put("event", "didDisconnect");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("projectId", String.valueOf(stringeeClient.getProjectId()));
                bodyMap.put("isReconnecting", b);
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
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
                map.put("event", "incomingCall");
                Map callInfoMap = new HashMap();
                callInfoMap.put("callId", stringeeCall.getCallId());
                callInfoMap.put("from", stringeeCall.getFrom());
                callInfoMap.put("to", stringeeCall.getTo());
                callInfoMap.put("fromAlias", stringeeCall.getFromAlias());
                callInfoMap.put("toAlias", stringeeCall.getToAlias());
                callInfoMap.put("isVideocall", stringeeCall.isVideoCall());
                int callType = 0;
                if (!stringeeCall.getFrom().equals(_client.getUserId())) {
                    callType = 1;
                }
                if (stringeeCall.isAppToPhoneCall()) {
                    callType = 2;
                } else if (stringeeCall.isPhoneToAppCall()) {
                    callType = 3;
                }
                callInfoMap.put("callType", callType);
                callInfoMap.put("isVideoCall", stringeeCall.isVideoCall());
                callInfoMap.put("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());
                map.put("body", callInfoMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onIncomingCall2(StringeeCall2 stringeeCall2) {

    }

    @Override
    public void onConnectionError(final StringeeClient stringeeClient, final StringeeError stringeeError) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "==========ConnectionError==========\n" + "code: " + stringeeError.getCode() + " -message: " + stringeeError.getMessage());
                Map map = new HashMap();
                map.put("event", "didFailWithError");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                bodyMap.put("code", stringeeError.getCode());
                bodyMap.put("message", stringeeError.getMessage());
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
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
                map.put("event", "requestAccessToken");
                Map bodyMap = new HashMap();
                bodyMap.put("userId", stringeeClient.getUserId());
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
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
                map.put("event", "didReceiveCustomMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
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
                map.put("event", "didReceiveTopicMessage");
                Map bodyMap = new HashMap();
                bodyMap.put("fromUserId", from);
                try {
                    bodyMap.put("info", Utils.convertJsonToMap(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("body", bodyMap);
                com.stringee.stringeeflutterplugin.StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
