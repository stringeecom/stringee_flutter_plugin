package com.stringee.stringeeflutterplugin.common.enumeration;

public enum StringeeEventType {
    CLIENT_EVENT(0),
    CALL_EVENT(1),
    CALL2_EVENT(2),
    CHAT_EVENT(3),
    ROOM_EVENT(4);

    public final short value;

    StringeeEventType(int value) {
        this.value = (short) value;
    }

    public short getValue() {
        return this.value;
    }
}