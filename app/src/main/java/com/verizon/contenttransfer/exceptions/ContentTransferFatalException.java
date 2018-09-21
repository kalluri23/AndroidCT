package com.verizon.contenttransfer.exceptions;

/**
 * Created by kommisu on 6/29/2016.
 */
public class ContentTransferFatalException extends Exception {
    private static final long serialVersionUID = 0L;

    public ContentTransferFatalException() {
    }

    public ContentTransferFatalException(String message) {
        super(message);
    }

    public ContentTransferFatalException(Throwable cause) {
        super(cause);
    }

    public ContentTransferFatalException(String message, Throwable cause) {
        super(message, cause);
    }

}
