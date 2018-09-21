package com.verizon.contenttransfer.p2p.model;

/**
 * Created by gur36379 on 9/15/2016.
 */
public class ContentRecapVO {
    String contentType;
    String UImedia;
    int contentSize;
    int transferSize;
    boolean checkStatus;
    boolean mediaPermitted;


    public ContentRecapVO(String contentType, int transferSize, int contentSize,boolean checkStatus,boolean mediaPermitted,String uiMedia) {
        this.contentType = contentType;
        this.contentSize = contentSize;
        this.transferSize = transferSize;
        this.checkStatus =checkStatus;
        this.mediaPermitted =mediaPermitted;
        this.UImedia =uiMedia;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    public int getTransferSize() {
        return transferSize;
    }

    public void setTransferSize(int transferSize) {
        this.transferSize = transferSize;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }

    public boolean isMediaPermitted() {
        return mediaPermitted;
    }

    public void setMediaPermitted(boolean mediaPermitted) {
        this.mediaPermitted = mediaPermitted;
    }

    public String getUImedia() {
        return UImedia;
    }

    public void setUImedia(String UImedia) {
        this.UImedia = UImedia;
    }


}
