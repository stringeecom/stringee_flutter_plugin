package com.stringee.stringeeflutterplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import io.flutter.plugin.common.EventChannel.EventSink;

/**
 * @author Alex
 */

public class PacketSenderThread extends Thread {
    private static PacketSenderThread instance;
    private final LinkedBlockingQueue packetQueue = new LinkedBlockingQueue();
    private final LinkedBlockingQueue queueNotLogin = new LinkedBlockingQueue();
    private boolean running = false;
    private EventSink eventSink;

    public static PacketSenderThread getInstance() {
        if (instance == null) {
            instance = new PacketSenderThread();
        }
        return instance;
    }

    public void send(String actionId) {
        try {
            packetQueue.put(actionId);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void sendAllPacketInQueue() {
        while (!queueNotLogin.isEmpty()) {
            try {
                String actionId = (String) queueNotLogin.take();
                packetQueue.put(actionId);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
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
                String actionId = (String) packetQueue.take();
                if (eventSink != null) {
                    StringeeManager.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Map actionMap = new HashMap();
                            actionMap.put("actionId", actionId);
                            eventSink.success(actionMap);
                        }
                    });
                } else {
                    queueNotLogin.put(actionId);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
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
