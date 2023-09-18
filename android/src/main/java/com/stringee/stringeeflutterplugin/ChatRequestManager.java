package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.listeners.CallbackListener;

import io.flutter.plugin.common.MethodChannel.Result;

public class ChatRequestManager {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;

    public ChatRequestManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        this.stringeeManager = StringeeManager.getInstance();
    }

    /**
     * accept chat request
     */
    public void acceptChatRequest(String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "acceptChatRequest", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("acceptChatRequest", -2, "convId is invalid", result);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.accept(clientWrapper.getClient(), new CallbackListener<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("acceptChatRequest", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("acceptChatRequest", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("acceptChatRequest", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Reject chat request
     */
    public void rejectChatRequest(String convId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "rejectChatRequest", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("rejectChatRequest", -2, "convId is invalid", result);
            return;
        }

        Utils.getChatRequest(clientWrapper.getClient(), convId, new CallbackListener<ChatRequest>() {
            @Override
            public void onSuccess(ChatRequest chatRequest) {
                chatRequest.reject(clientWrapper.getClient(), new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("rejectChatRequest", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("rejectChatRequest", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("rejectChatRequest", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }
}
