package com.verizon.contenttransfer.exceptions;

/**
 * Created by kommisu on 6/29/2016.
 */
public class SocketInitializationException extends Exception {
    private static final long serialVersionUID = 0L;

    public SocketInitializationException() {
    }

    public SocketInitializationException(String message) {
        super(message);
    }

    public SocketInitializationException(Throwable cause) {
        super(cause);
    }

    public SocketInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
