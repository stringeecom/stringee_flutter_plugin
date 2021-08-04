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
    private ClientWrapper _clientWrapper;
    private StringeeManager _manager;
    private Handler _handler;

    private static final String TAG = "StringeeSDK";

    public MessageManager(ClientWrapper clientWrapper) {
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _clientWrapper = clientWrapper;
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

        Utils.getMessage(_clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.edit(_clientWrapper.getClient(), content, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "edit: success");
                                Map map1 = new HashMap();
                                map1.put("status", true);
                                map1.put("code", 0);
                                map1.put("message", "Success");
                                result.success(map1);
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
                                Map map12 = new HashMap();
                                map12.put("status", false);
                                map12.put("code", stringeeError.getCode());
                                map12.put("message", stringeeError.getMessage());
                                result.success(map12);
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
                        Map map13 = new HashMap();
                        map13.put("status", false);
                        map13.put("code", stringeeError.getCode());
                        map13.put("message", stringeeError.getMessage());
                        result.success(map13);
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

        Utils.getMessage(_clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.pinOrUnpin(_clientWrapper.getClient(), pinOrUnPin, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "pinOrUnPin: success");
                                Map map1 = new HashMap();
                                map1.put("status", true);
                                map1.put("code", 0);
                                map1.put("message", "Success");
                                result.success(map1);
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
                                Map map12 = new HashMap();
                                map12.put("status", false);
                                map12.put("code", stringeeError.getCode());
                                map12.put("message", stringeeError.getMessage());
                                result.success(map12);
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
                        Map map13 = new HashMap();
                        map13.put("status", false);
                        map13.put("code", stringeeError.getCode());
                        map13.put("message", stringeeError.getMessage());
                        result.success(map13);
                    }
                });
            }
        });
    }
}