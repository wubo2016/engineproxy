package com.wyuansmart.phone.engine.common;

public class ApplicationException extends RuntimeException {
    private int code;

    public ApplicationException() {
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
