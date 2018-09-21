package com.verizon.contenttransfer.exceptions;

/**
 * Created by kommisu on 7/27/2016.
 */
public class SiteCatLogException extends Exception {
    private static final long serialVersionUID = 0L;

    public SiteCatLogException() {
    }

    public SiteCatLogException(String message) {
        super(message);
    }

    public SiteCatLogException(Throwable cause) {
        super(cause);
    }

    public SiteCatLogException(String message, Throwable cause) {
        super(message, cause);
    }

}
