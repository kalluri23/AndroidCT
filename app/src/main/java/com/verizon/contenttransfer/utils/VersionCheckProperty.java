package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.base.VZTransferConstants;

/**
 * Created by c0bissh on 4/10/2017.
 */
public class VersionCheckProperty {
    private String TAG = VersionCheckProperty.class.getName();
    private String osFlag = VZTransferConstants.ANDROID;
    private String hostReceived = "";
    private String buildVersion = "";
    private String minSupported = "";
    private String availableSpace = "";

    public VersionCheckProperty(String inputMessage) {
        if(inputMessage == null){
            return;
        }
        if (inputMessage.trim().split("#").length > 0) {
            if (inputMessage.trim().split("#")[0] != null) {
                hostReceived = inputMessage.trim().split("#")[0];
                LogUtil.e(TAG, "Client Host received : " + hostReceived);

                osFlag = hostReceived.substring(35);
                LogUtil.e(TAG, "osFlag : " + osFlag);
            }
        }
        if (inputMessage.trim().split("#").length > 1) {
            if (inputMessage.trim().split("#")[1] != null) {
                buildVersion = inputMessage.trim().split("#")[1];
                LogUtil.d(TAG, "Client Version received 1: " + buildVersion);
            }
        }
        if (inputMessage.trim().split("#").length > 2) {
            if (inputMessage.trim().split("#")[2] != null) {
                minSupported = inputMessage.trim().split("#")[2];
            }
        }
        try {
            if (inputMessage.trim().split("#").length > 3) {
                availableSpace = inputMessage.trim().split("#")[3];
                LogUtil.d(TAG, "available space =" + availableSpace);
                Utils.setAvailableSpace(availableSpace);
            }
        }catch (Exception e){
            LogUtil.d(TAG,"parsing available space :"+e.getMessage());
        }
    }

    public String getHostReceived() {
        return hostReceived;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getMinSupported() {
        return minSupported;
    }

    public String getOsFlag() {
        return osFlag;
    }

    public String getAvailableSpace() {
        return availableSpace;
    }
}
