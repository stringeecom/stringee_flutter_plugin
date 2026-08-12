package com.stringee.stringeeflutterplugin.common.enumeration;

/** Identifies the Dart object category that should receive a native event. */
public enum StringeeEventType {
    CLIENT_EVENT(0),
    CALL_EVENT(1),
    CALL2_EVENT(2),
    CHAT_EVENT(3),
    ROOM_EVENT(4);

    /** Platform-channel value. */
    public final short value;

    StringeeEventType(int value) {
        this.value = (short) value;
    }

    /** Returns the platform-channel value. */
    public short getValue() {
        return this.value;
    }
}
