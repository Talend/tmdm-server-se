package com.amalto.core.storage.exception;

public class RecordNotExistedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public RecordNotExistedException(String message) {
        super(message);
    }
}
