package com.stringee.stringeeflutterplugin;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.listeners.CallbackListener;

import io.flutter.plugin.common.MethodChannel.Result;

public class ChatRequestManager {
    private ClientWrapper _clientWrapper;
    private StringeeManager _manager;
    private android.os.Handler _handler;
    private static final String TAG = "StringeeSDK";

    public ChatRequestManager(ClientWrapper clientWrapper) {
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _clientWrapper = clientWrapper;
    }

    /**
     * accept chat request
     *
     * @param convId
     * @param result
     */
    public void acceptChatRequest(String convId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "acceptChatRequest: false - -1 - StringeeClient is disconnected");
            java.util.Map map = new java.util.HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "acceptChatRequest: false - -2 - convId is invalid");
            java.util.Map map = new java.util.HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getChatRequest(_clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.accept(_clientWrapper.getClient(), new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "acceptChatRequest: success");
                                java.util.Map map = new java.util.HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "acceptChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                java.util.Map map = new java.util.HashMap();
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
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "acceptChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        java.util.Map map = new java.util.HashMap();
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
     * Reject chat request
     *
     * @param convId
     * @param result
     */
    public void rejectChatRequest(String convId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "rejectChatRequest: false - -1 - StringeeClient is disconnected");
            java.util.Map map = new java.util.HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (convId == null || convId.isEmpty()) {
            Log.d(TAG, "rejectChatRequest: false - -2 - convId is invalid");
            java.util.Map map = new java.util.HashMap();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getChatRequest(_clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.reject(_clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "rejectChatRequest: success");
                                java.util.Map map = new java.util.HashMap();
                                map.put("status", true);
                                map.put("code", 0);
                                map.put("message", "Success");
                                result.success(map);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "rejectChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                java.util.Map map = new java.util.HashMap();
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
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "rejectChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        java.util.Map map = new java.util.HashMap();
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
