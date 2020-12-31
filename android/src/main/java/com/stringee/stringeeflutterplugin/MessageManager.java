package com.stringee.stringeeflutterplugin;

import android.os.Handler;

import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.Message;
import com.stringee.messaging.listeners.CallbackListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class MessageManager {
    private static MessageManager _messageManager;
    private static StringeeManager _stringeeManager;
    private static Handler _handler;

    public static synchronized MessageManager getInstance(StringeeManager stringeeManager, Handler handler) {
        if (_messageManager == null) {
            _messageManager = new MessageManager();
            _stringeeManager = stringeeManager;
            _handler = handler;
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (msgId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Message id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        final StringeeClient _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
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
        if (convId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Conversation id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        if (msgId == null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -2);
                    map.put("message", "Message id can not be null");
                    result.success(map);
                }
            });
            return;
        }

        final StringeeClient _client = _stringeeManager.getClient();
        if (_client == null || !_client.isConnected()) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", -1);
                    map.put("message", "StringeeClient is not initialized or disconnected");
                    result.success(map);
                }
            });
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
