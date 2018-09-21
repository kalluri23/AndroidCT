package com.verizon.contenttransfer.feedback;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by desaivi on 9/24/2015.
 */
public class CTCrashReportBean implements Serializable {

    //@SerializedName("deviceModel")
    private String deviceModel;
    //@SerializedName("deviceName")
    private String deviceName;
    //@SerializedName("osVersion")
    private String osVersion;

    //@SerializedName("SessionCookie")
    private String sessionCookie;

    //@SerializedName("mdn")
    private String mdn;
    //@SerializedName("sourceid")
    private String sourceid;
    //@SerializedName("timestamp")
    private Date timestamp;
    //@SerializedName("exceptionReason")
    private String exceptionReason;
    //@SerializedName("crashLocation")
    private String crashLocation;
    //@SerializedName("crashStack")
    private List<String> crashStack;
    //@SerializedName("appVersion")
    private String appVersion;

    public String getCrashLocation() {
        return crashLocation;
    }

    public void setCrashLocation(String crashLocation) {
        this.crashLocation = crashLocation;
    }

    public List<String> getCrashStack() {
        return crashStack;
    }

    public void setCrashStack(List<String> crashStack) {
        this.crashStack = crashStack;
    }

    public void setCrashStack(StackTraceElement[] stackTraceElements){

        if(stackTraceElements != null && stackTraceElements.length > 0 ){
            crashStack = new ArrayList<String>();
            for(StackTraceElement element : stackTraceElements){
                if(element != null)
                    crashStack.add(element.toString());
            }
        }
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public String getMdn() {
        return mdn;
    }

    public void setMdn(String mdn) {
        this.mdn = mdn;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }}
