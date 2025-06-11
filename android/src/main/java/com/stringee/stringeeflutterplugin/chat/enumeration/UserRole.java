package com.stringee.stringeeflutterplugin.chat.enumeration;

public enum UserRole {
    ADMIN(0),
    MEMBER(1);

    public final short value;

    UserRole(int value) {
        this.value = (short) value;
    }

    public short getValue() {
        return this.value;
    }
}
