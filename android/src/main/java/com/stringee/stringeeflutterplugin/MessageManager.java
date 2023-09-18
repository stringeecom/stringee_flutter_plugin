package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Message;
import com.stringee.messaging.listeners.CallbackListener;

import io.flutter.plugin.common.MethodChannel.Result;

public class MessageManager {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;

    public MessageManager(ClientWrapper clientWrapper) {
        this.stringeeManager = StringeeManager.getInstance();
        this.clientWrapper = clientWrapper;
    }

    /**
     * Edit message
     */
    public void edit(final String convId, final String msgId, final String content, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "edit", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("edit", -2, "convId is invalid", result);
            return;
        }

        if (Utils.isStringEmpty(msgId)) {
            Utils.sendErrorResponse("edit", -2, "msgId is invalid", result);
            return;
        }

        Utils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.edit(clientWrapper.getClient(), content, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("edit", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("edit", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("edit", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Pin/Unpin message
     */
    public void pinOrUnPin(final String convId, final String msgId, final boolean pinOrUnPin, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "pinOrUnPin", result)) {
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Utils.sendErrorResponse("pinOrUnPin", -2, "convId is invalid", result);
            return;
        }

        if (Utils.isStringEmpty(msgId)) {
            Utils.sendErrorResponse("pinOrUnPin", -2, "msgId is invalid", result);
            return;
        }

        Utils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.pinOrUnpin(clientWrapper.getClient(), pinOrUnPin, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendSuccessResponse("pinOrUnPin", null, result);
                            }
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.sendErrorResponse("pinOrUnPin", stringeeError.getCode(), stringeeError.getMessage(), result);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("pinOrUnPin", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }
}