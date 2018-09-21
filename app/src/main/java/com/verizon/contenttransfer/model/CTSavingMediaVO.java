package com.verizon.contenttransfer.model;

/**
 * Created by yempasu on 8/14/2017.
 */

public class CTSavingMediaVO {
    private String mediaType;

    public String getUImediaType() {
        return UImediaType;
    }

    public void setUImediaType(String UImediaType) {
        this.UImediaType = UImediaType;
    }

    private String UImediaType;
    private int percentageSaved=0;

    public String getProgressShowStatus() {
        return progressShowStatus;
    }

    public void setProgressShowStatus(String status) {
        this.progressShowStatus = status;
    }

    private String progressShowStatus;
    private String insideText;

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public int getPercentageSaved() {
        return percentageSaved;
    }

    public void setPercentageSaved(int percentageSaved) {
        this.percentageSaved = percentageSaved;
    }

    public String getInsideText() {
        return insideText;
    }

    public void setInsideText(String insideText) {
        this.insideText = insideText;
    }
}
