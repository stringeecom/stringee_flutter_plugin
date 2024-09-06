package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.listeners.CallbackListener;

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
     * @param convId convId
     * @param result result
     */
    public void acceptChatRequest(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("acceptChatRequest");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("acceptChatRequest", "convId");
            result.success(map);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.accept(clientWrapper.getClient(), new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("acceptChatRequest");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("acceptChatRequest", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("acceptChatRequest", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Reject chat request
     *
     * @param convId convId
     * @param result result
     */
    public void rejectChatRequest(String convId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("rejectChatRequest");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("rejectChatRequest", "convId");
            result.success(map);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.reject(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("rejectChatRequest");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("rejectChatRequest", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("rejectChatRequest", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}
