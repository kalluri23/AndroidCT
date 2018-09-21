/*
 *  -------------------------------------------------------------------------
 *     PROPRIETARY INFORMATION. Not for use or disclosure outside Verizon Wireless, Inc.
 *     and its affiliates except under written agreement.
 *
 *     This is an unpublished, proprietary work of Verizon Wireless, Inc.
 *     that is protected by United States copyright laws.  Disclosure,
 *     copying, reproduction, merger, translation,modification,enhancement,
 *     or use by anyone other than authorized employees or licensees of
 *     Verizon Wireless, Inc. without the prior written consent of
 *     Verizon Wireless, Inc. is prohibited.
 *
 *     Copyright (c) 2016 Verizon Wireless, Inc.  All rights reserved.
 *  -------------------------------------------------------------------------
 *
 *
 *      Created by c0bissh on 12/12/2016.
 */

package com.verizon.contenttransfer.p2p.model;

import com.verizon.contenttransfer.base.VZTransferConstants;

/**
 * Created by c0bissh on 12/16/2016.
 */
public class QRCodeVO {
    private String ssid;
    private String ipaddress;
    private String versionName;
    private String securityType;
    private String password;
    private String platform;
    private String connectionType;
    private String setupType;
    private String combinationType;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getCombinationType() {
        return combinationType;
    }

    public void setCombinationType(String combinationType) {
        this.combinationType = combinationType;
    }

    public String getSetupType() {
        return setupType;
    }

    public void setSetupType(String setupType) {
        this.setupType = setupType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }


    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = (null == password?"":password);
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getString(){
        StringBuilder builder = new StringBuilder();

        builder.append(versionName + VZTransferConstants.QR_CODE_DELIMITER); //

        builder.append(securityType + VZTransferConstants.QR_CODE_DELIMITER);

        builder.append(combinationType + VZTransferConstants.QR_CODE_DELIMITER);// VZTransferConstants.CROSS_PLATFORM : VZTransferConstants.SAME_PLATFORM

        builder.append(ssid +  VZTransferConstants.QR_CODE_DELIMITER); // Name of device shows on WifiDirect / Hotspot page / SSID - Ex: DIRECT-Dk-SM-N910

        builder.append(ipaddress + VZTransferConstants.QR_CODE_DELIMITER);

        builder.append(password + VZTransferConstants.QR_CODE_DELIMITER); // Hotspot password.

        builder.append(connectionType + VZTransferConstants.QR_CODE_DELIMITER); //  WifiDirect / Hotspot page / SSID.

        builder.append(setupType); // Receiver/Sender



        return builder.toString();
    }
}
