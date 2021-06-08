package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Message;
import com.stringee.messaging.listeners.CallbackListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class MessageManager {
    private static MessageManager _messageManager;
    private static StringeeManager _manager;
    private static Handler _handler;

    private static final String TAG = "StringeeSDK";

    public static synchronized MessageManager getInstance() {
        if (_messageManager == null) {
            _messageManager = new MessageManager();
            _manager = StringeeManager.getInstance();
            _handler = _manager.getHandler();
        }
        return _messageManager;
    }

    /**
     * Edit message
     *
     * @param msgId
     * @param content
     * @param result
     */
    public void edit(String convId, String msgId, final String content, final Result result) {
        Map map = new HashMap();
        com.stringee.StringeeClient _client = _manager.getClient();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "edit: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "edit: false - -2 - convId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        if (msgId == null || msgId.isEmpty()) {
            Log.d(TAG, "edit: false - -2 - msgId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "msgId is invalid");
            result.success(map);
            return;
        }

        Utils.getMessage(_client, convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.edit(_client, content, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "edit: success");
                                Map map = new HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "edit: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "edit: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    /**
     * Pin/Unpin message
     *
     * @param msgId
     * @param pinOrUnPin
     * @param result
     */
    public void pinOrUnPin(String convId, String msgId, final boolean pinOrUnPin, final Result result) {
        Map map = new HashMap();
        com.stringee.StringeeClient _client = _manager.getClient();
        if (_client == null || !_client.isConnected()) {
            Log.d(TAG, "pinOrUnPin: false - -1 - StringeeClient is not initialized or disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is not initialized or disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "pinOrUnPin: false - -2 - convId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        if (msgId == null || msgId.isEmpty()) {
            Log.d(TAG, "pinOrUnPin: false - -2 - msgId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "msgId is invalid");
            result.success(map);
            return;
        }

        Utils.getMessage(_client, convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.pinOrUnpin(_client, pinOrUnPin, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "pinOrUnPin: success");
                                Map map = new HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "pinOrUnPin: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map map = new HashMap();
                                map.put("status", false);
                                map.put("code", stringeeError.getCode());
                                map.put("message", stringeeError.getMessage());
                                result.success(map);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "pinOrUnPin: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }
}