package com.stringee.stringeeflutterplugin.chat;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.Utils;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ChatRequestManager {
    private final ClientWrapper clientWrapper;

    public ChatRequestManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    /**
     * accept chat request
     *
     * @param convId
     * @param result
     */
    public void acceptChatRequest(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "acceptChatRequest: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "acceptChatRequest: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.accept(clientWrapper.getClient(), new CallbackListener<>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "acceptChatRequest: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "acceptChatRequest: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage());
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
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "acceptChatRequest: false - " + stringeeError.getCode() + " - " +
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

    /**
     * Reject chat request
     *
     * @param convId
     * @param result
     */
    public void rejectChatRequest(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(Constants.TAG, "rejectChatRequest: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isEmpty(convId)) {
            Log.d(Constants.TAG, "rejectChatRequest: false - -2 - convId is invalid");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        ChatUtils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.reject(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(Constants.TAG, "rejectChatRequest: success");
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", true);
                            map.put("code", 0);
                            map.put("message", "Success");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Log.d(Constants.TAG,
                                    "rejectChatRequest: false - " + stringeeError.getCode() +
                                            " - " + stringeeError.getMessage());
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
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG,
                            "rejectChatRequest: false - " + stringeeError.getCode() + " - " +
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
