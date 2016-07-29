package com.amalto.core.storage.exception;

public class MissingPrimaryKeyException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public MissingPrimaryKeyException(String message) {
        super(message);
    }
}
