package com.minelittlepony.hdskins.common.upload;

public class UploaderException extends Exception {

    public UploaderException(String message) {
        super(message);
    }

    public UploaderException(Throwable throwable) {
        super(throwable);
    }

    public UploaderException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
