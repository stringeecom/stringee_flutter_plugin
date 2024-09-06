package com.stringee.stringeeflutterplugin;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Message;
import com.stringee.messaging.listeners.CallbackListener;

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
     * @param msgId   message id
     * @param content new content
     * @param result  result
     */
    public void edit(String convId, String msgId, final String content, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("edit");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("edit", "convId");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(msgId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("edit", "msgId");
            result.success(map);
            return;
        }

        Utils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.edit(clientWrapper.getClient(), content, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("edit");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("edit", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("edit", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Pin/Unpin message
     *
     * @param msgId      message id
     * @param pinOrUnPin true: pin, false: unpin
     * @param result     result
     */
    public void pinOrUnPin(String convId, String msgId, final boolean pinOrUnPin, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("pinOrUnPin");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(convId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("pinOrUnPin", "convId");
            result.success(map);
            return;
        }

        if (Utils.isStringEmpty(msgId)) {
            Map<String, Object> map = Utils.createInvalidErrorMap("pinOrUnPin", "msgId");
            result.success(map);
            return;
        }

        Utils.getMessage(clientWrapper.getClient(), convId, new String[]{msgId}, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
                message.pinOrUnpin(clientWrapper.getClient(), pinOrUnPin, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createSuccessMap("pinOrUnPin");
                            result.success(map);
                        });
                    }

                    @Override
                    public void onError(final StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Utils.post(() -> {
                            Map<String, Object> map = Utils.createErrorMap("pinOrUnPin", stringeeError.getCode(), stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                });
            }

            @Override
            public void onError(final StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("pinOrUnPin", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }
}