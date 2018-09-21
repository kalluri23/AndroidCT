package com.verizon.contenttransfer.utils;

/**
 * Created by c0bissh on 8/24/2016.
 */
public class TransferSummaryStatus {
    private String path = "";
    private String message = "Test Message";
    private String media = "";
    public TransferSummaryStatus(String path, String msg,String media) {
        this.path = path;
        this.message = msg;
        this.media = media;
    }
    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }
    public String getMedia() {
        return media;
    }
}
