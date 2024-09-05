package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.listeners.CallbackListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class ChatRequestManager {
    private ClientWrapper clientWrapper;
    private Handler handler;

    private static final String TAG = "StringeeSDK";

    public ChatRequestManager(ClientWrapper clientWrapper) {
        handler = StringeeManager.getInstance().getHandler();
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
            Log.d(TAG, "acceptChatRequest: false - -1 - StringeeClient is disconnected");
            Map<String,Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Log.d(TAG, "acceptChatRequest: false - -2 - convId is invalid");
            Map<String,Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.accept(clientWrapper.getClient(), new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "acceptChatRequest: success");
                                Map<String,Object> map = new HashMap<>();
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
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "acceptChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map<String,Object> map = new HashMap<>();
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "acceptChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map<String,Object> map = new HashMap<>();
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
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "rejectChatRequest: false - -1 - StringeeClient is disconnected");
            Map<String,Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Log.d(TAG, "rejectChatRequest: false - -2 - convId is invalid");
            Map<String,Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -2);
            map.put("message", "convId is invalid");
            result.success(map);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.reject(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "rejectChatRequest: success");
                                Map<String,Object> map = new HashMap<>();
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
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "rejectChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                                Map<String,Object> map = new HashMap<>();
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "rejectChatRequest: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map<String,Object> map = new HashMap<>();
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
