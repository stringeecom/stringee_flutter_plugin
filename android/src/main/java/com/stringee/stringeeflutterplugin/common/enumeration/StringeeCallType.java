package com.stringee.stringeeflutterplugin.common.enumeration;

public enum StringeeCallType {
    APP_TO_APP_OUTGOING(0),
    APP_TO_APP_Incoming(1),
    APP_TO_PHONE(2),
    PHONE_TO_APP(3);

    public final short value;

    StringeeCallType(int value) {
        this.value = (short) value;
    }

    public short getValue() {
        return this.value;
    }
}
