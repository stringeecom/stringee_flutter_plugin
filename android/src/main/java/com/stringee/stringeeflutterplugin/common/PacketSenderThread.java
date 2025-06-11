package com.stringee.stringeeflutterplugin.common;

import android.content.Intent;

import com.stringee.stringeeflutterplugin.common.StringeeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import io.flutter.plugin.common.EventChannel.EventSink;

public class PacketSenderThread extends Thread {
    private static PacketSenderThread instance;
    private final LinkedBlockingQueue<Intent> packetQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Intent> queueNotLogin = new LinkedBlockingQueue<>();
    private boolean running = false;
    private EventSink eventSink;

    public static PacketSenderThread getInstance() {
        if (instance == null) {
            instance = new PacketSenderThread();
        }
        return instance;
    }

    public void send(Intent intent) {
        try {
            packetQueue.put(intent);
        } catch (InterruptedException ex) {
            Utils.reportException(PacketSenderThread.class,ex);
        }
    }

    public void sendAllPacketInQueue() {
        while (!queueNotLogin.isEmpty()) {
            try {
                packetQueue.put(queueNotLogin.take());
            } catch (InterruptedException ex) {
               Utils.reportException(PacketSenderThread.class,ex);
            }
        }
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;
        while (running) {
            try {
                Intent intent = packetQueue.take();
                Map<String, Object> map = new HashMap<>();
                map.put("action", intent.getAction());
                if (intent.getExtras() != null) {
                    for (String key : intent.getExtras().keySet()) {
                        map.put(key, intent.getExtras().get(key));
                    }
                }
                if (eventSink != null) {
                    Utils.post(() -> eventSink.success(map));
                } else {
                    queueNotLogin.put(intent);
                }
            } catch (InterruptedException ex) {
                Utils.reportException(PacketSenderThread.class,ex);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setEventSink(EventSink eventSink) {
        this.eventSink = eventSink;
    }
}
