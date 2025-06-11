package com.stringee.stringeeflutterplugin.chat;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Message;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.Utils;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class MessageManager {
    private final ClientWrapper clientWrapper;

    public MessageManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    /**
     * Edit message
     *
     * @param msgId
     * @param content
     * @param result
     */
    public void edit(String convId, String msgId, final String content, final Result result) {
        Map<String, Object> map = new HashMap<>();
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "edit: false - -1 - StringeeClient is disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "edit: false - -2 - convId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(msgId)) {
            Log.d(Constants.TAG, "edit: false - -2 - msgId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "msgId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId},
                new CallbackListener<>() {
                    @Override
                    public void onSuccess(Message message) {
                        message.edit(clientWrapper.getClient(), content, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Utils.post(() -> {
                                    Log.d(Constants.TAG, "edit: success");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("status", true);
                                    map.put("code", 0);
                                    map.put("message", "Success");
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(final StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Log.d(Constants.TAG,
                                            "edit: false - " + stringeeError.getCode() + " - " +
                                                    stringeeError.getMessage());
                                    Map<String, Object> map1 = new HashMap<>();
                                    map1.put("status", false);
                                    map1.put("code", stringeeError.getCode());
                                    map1.put("message", stringeeError.getMessage());
                                    result.success(map1);
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "edit: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("status", false);
                            map2.put("code", stringeeError.getCode());
                            map2.put("message", stringeeError.getMessage());
                            result.success(map2);
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
    public void pinOrUnPin(String convId, String msgId, final boolean pinOrUnPin,
                           final Result result) {
        Map<String, Object> map = new HashMap<>();
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "pinOrUnPin: false - -1 - StringeeClient is disconnected");
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "pinOrUnPin: false - -2 - convId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(msgId)) {
            Log.d(Constants.TAG, "pinOrUnPin: false - -2 - msgId is invalid");
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "msgId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId},
                new CallbackListener<>() {
                    @Override
                    public void onSuccess(Message message) {
                        message.pinOrUnpin(clientWrapper.getClient(), pinOrUnPin,
                                new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG, "pinOrUnPin: success");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", true);
                                            map.put("code", 0);
                                            map.put("message", "Success");
                                            result.success(map);
                                        });
                                    }

                                    @Override
                                    public void onError(final StringeeError stringeeError) {
                                        super.onError(stringeeError);
                                        Utils.post(() -> {
                                            Log.d(Constants.TAG, "pinOrUnPin: false - " +
                                                    stringeeError.getCode() + " - " +
                                                    stringeeError.getMessage());
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("status", false);
                                            map.put("code", stringeeError.getCode());
                                            map.put("message", stringeeError.getMessage());
                                            result.success(map);
                                        });
                                    }
                                });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "pinOrUnPin: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage());
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
    }
}
