package com.verizon.contenttransfer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;

/**
 * Created by duggipr on 5/27/2016.
 */
public class CTGlobal {

    private static String TAG = "CTGlobal";
    private static CTGlobal instance;

    // An instance attribute.
    private String phoneSelection=VZTransferConstants.NEW_PHONE;
    private boolean isCross = false;
    private boolean cellularDataStatus = false;
    private boolean autoConnectToWifiOnDetect = false;
    private boolean exitApp = false;
    private String buildVersion = "";
    private String buildDate = "";
    private Context ctContext;
    private String storeMacId="";
    private String deviceUUID ="";
    private String dataTransferInterruptedByUser = "";
    private String paringDeviceID = "";
    private String paringDeviceModel = "";
    private String paringDeviceOSVersion = "";
    private String paringDeviceDeviceType = "";
    private String hotspotName = "";
    private String dbErrorMessage = "";
    private String appType = "";// This app type is set in setupmodel. so, do not reset inside reset method.. reset method is called after setting this value.
    private String bluetoothBeaconMajorId = "";
    private String bluetoothBeaconMinorId = "";
    private String storeId = "";
    private String location = "";
    private String state  = "";
    private String region = "";
    private String transferStart= "";
    private String pairingType = "";
    private Long deviceCount =0L;
    private String connectionType = VZTransferConstants.HOTSPOT_WIFI_CONNECTION; // DEFAULT VALUE
    private boolean forceStop = false;
    private long availableSpaceAtReceiverEnd = 0L;
    private String applicationStatus = "";
    private boolean vztransferStarted = false;
    private boolean warnWifi = true;
    private boolean tryAgain = false;
    private boolean doingOneToMany = false;
    private boolean waitForNewDevice = false;
    private boolean isWiDiFallback = false;
    private boolean processSMS = false;
    private boolean isWidiDiscovering = false;
    private boolean savingComplete = false;
    private boolean isWifiReset = false;
    private boolean readyToConnect = false;
    private boolean readyToTransfer = false;
    private boolean connectionFailed = false;
    private boolean isWifiDirecct = false;
    private boolean tryAnotherWay = false;
    private boolean doingOneToManyComb=false;
    private boolean SmsPermitted = true;
    private boolean ContactsPermitted = true;
    private boolean CalllogsPermitted = true;
    private boolean CalendarPermitted = true;
    private boolean isAppAnalyticsAysncTaskRunning = false;
    private boolean isVzcloudClickAnalyticsRunning = false;


    private boolean passwordManagerDBCopied = false;


    /*
     * Cross platform AND to iOS not transfer Audio. so, AUDIO_STR not required to add here.
     */
    private String[] crossMediaTypeArray = {
        VZTransferConstants.CONTACTS_STR,
        VZTransferConstants.PHOTOS_STR,
        VZTransferConstants.VIDEOS_STR,
        VZTransferConstants.CALENDAR_STR,
        VZTransferConstants.APPS_STR
    };

    private String[] crossMediaTypeArrayforOneToMany = {
            VZTransferConstants.CONTACTS_STR,
            VZTransferConstants.CALENDAR_STR
    };

    private String[] AndroidMediaTypeArray = {
        VZTransferConstants.CONTACTS_STR,
        VZTransferConstants.PHOTOS_STR,
        VZTransferConstants.VIDEOS_STR,
        VZTransferConstants.AUDIO_STR,
        VZTransferConstants.CALENDAR_STR,
        VZTransferConstants.SMS_STR,
        VZTransferConstants.CALLLOG_STR,
        VZTransferConstants.DOCUMENTS_STR,
        VZTransferConstants.APPS_STR,
    };

    public static CTGlobal getInstance() {
        if (instance == null) {
            instance = new CTGlobal();
        }
        return instance;

    }

    public boolean isPasswordManagerDBCopied() {
        return passwordManagerDBCopied;
    }

    public void setPasswordManagerDBCopied(boolean passwordManagerDBCopied) {
        this.passwordManagerDBCopied = passwordManagerDBCopied;
    }

    public boolean isAppAnalyticsAysncTaskRunning() {
        return isAppAnalyticsAysncTaskRunning;
    }

    public void setAppAnalyticsAysncTaskRunning(boolean appAnalyticsAysncTaskRunning) {
        isAppAnalyticsAysncTaskRunning = appAnalyticsAysncTaskRunning;
    }

    public boolean isVzcloudClickAnalyticsRunning() {
        return isVzcloudClickAnalyticsRunning;
    }

    public void setVzcloudClickAnalyticsRunning(boolean vzcloudClickAnalyticsRunning) {
        isVzcloudClickAnalyticsRunning = vzcloudClickAnalyticsRunning;
    }

    public boolean isConnectionFailed() {
        return connectionFailed;
    }

    public void setConnectionFailed(boolean connectionFailed) {
        this.connectionFailed = connectionFailed;
    }
    public boolean isReadyToConnect() {
        return readyToConnect;
    }

    public void setReadyToConnect(boolean readyToConnect) {
        this.readyToConnect = readyToConnect;
    }

    public boolean isReadyToTransfer() {
        return readyToTransfer;
    }

    public void setReadyToTransfer(boolean readyToTransfer) {
        this.readyToTransfer = readyToTransfer;
    }
    public boolean isTryAnotherWay() {
        return tryAnotherWay;
    }

    public void setTryAnotherWay(boolean tryAnotherWay) {
        this.tryAnotherWay = tryAnotherWay;
    }

    public void reset(){
        LogUtil.d(TAG,"Reset CTGloal Variables.");
        this.applicationStatus = "";
        this.forceStop = false;
        this.isCross = false;
        this.phoneSelection= VZTransferConstants.NEW_PHONE;
        this.exitApp = false;
        this.tryAgain = false;
        this.deviceUUID = Utils.resetUUID();
        this.paringDeviceID = "";
        this.paringDeviceModel = "";
        this.paringDeviceOSVersion = "";
        this.paringDeviceDeviceType = "";
        this.connectionType = VZTransferConstants.HOTSPOT_WIFI_CONNECTION; // DEFAULT VALUE
        this.hotspotName = "";
        this.transferStart= String.valueOf(System.currentTimeMillis());
        this.pairingType= "";
        this.dbErrorMessage = "";
        this.isWidiDiscovering = false;
        this.isWiDiFallback = false;
        this.processSMS = false;
        this.doingOneToMany = false;
        this.readyToConnect = false;
        this.readyToTransfer = false;
        this.tryAnotherWay = false;
        this.vztransferStarted = false;
        this.connectionFailed = false;
        this.waitForNewDevice = false;
        this.deviceCount = 0L;
        this.isWifiDirecct = false;
        this.doingOneToManyComb =false;
        this.passwordManagerDBCopied = false;
    }

    public boolean isWifiDirecct() {
        return isWifiDirecct;
    }

    public void setIsWifiDirecct(boolean isWifiDirecct) {
        this.isWifiDirecct = isWifiDirecct;
    }
    public boolean isVztransferStarted() {
        return vztransferStarted;
    }

    public void setVztransferStarted(boolean vztransferStarted) {
        this.vztransferStarted = vztransferStarted;
    }
    public boolean isWiDiFallback() {
        return isWiDiFallback;
    }

    public void setIsWiDiFallback(boolean isWiDiFallback) {
        this.isWiDiFallback = isWiDiFallback;
    }
    public boolean isSavingComplete() {
        return savingComplete;
    }

    public void setSavingComplete(boolean complete) {
        savingComplete = complete;
    }

    public boolean isWifiReset() {
        return isWifiReset;
    }

    public void setWifiReset(boolean wifiReset) {
        isWifiReset = wifiReset;
    }
    public boolean isWidiDiscovering() {
        return isWidiDiscovering;
    }

    public void setIsWidiDiscovering(boolean isWidiDiscovering) {
        this.isWidiDiscovering = isWidiDiscovering;
    }
    public boolean isTryAgain() {
        return tryAgain;
    }

    public void setTryAgain(boolean tryAgain) {
        this.tryAgain = tryAgain;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus += applicationStatus+"->";
    }
    public boolean isForceStop() {
        return forceStop;
    }

    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
    }

    private boolean manualSetup = false;
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getBluetoothBeaconMinorId() {
        return bluetoothBeaconMinorId;
    }

    public void setBluetoothBeaconMinorId(String bluetoothBeaconMinorId) {
        this.bluetoothBeaconMinorId = bluetoothBeaconMinorId;
    }

    public String getBluetoothBeaconMajorId() {
        return bluetoothBeaconMajorId;
    }

    public void setBluetoothBeaconMajorId(String bluetoothBeaconMajorId) {
        this.bluetoothBeaconMajorId = bluetoothBeaconMajorId;
    }
    public String getMdn() {

        return ContentPreference.getStringValue(
                CTGlobal.getInstance().getContentTransferContext(),
                ContentPreference.CT_MDN,
                null);
    }

    public void setMdn(String mdn) {

        ContentPreference.putStringValue(
                CTGlobal.getInstance().getContentTransferContext(),
                ContentPreference.CT_MDN,
                mdn);
    }
    public String getTransferStart() {return transferStart;}

    public Long getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(Long deviceCount) {
        this.deviceCount = deviceCount;
    }

    public boolean isManualSetup() {
        return manualSetup;
    }

    public void setManualSetup(boolean manualSetup) {
        this.manualSetup = manualSetup;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public Context getContentTransferContext() {
        return ctContext;
    }

    public void setContentTransferContext(Context contentTransferContext) {
        this.ctContext = contentTransferContext;
    }

    public String getDeviceUUID() {return deviceUUID;}
    public String getStoreMacId() {return storeMacId;}

    public void setStoreMacId(String storeMacId) {
        this.storeMacId = storeMacId;
    }

    public boolean getExitApp() {
        return exitApp;
    }

    public void setExitApp(boolean exitApp) {this.exitApp = exitApp;}

    public String getPhoneSelection() {
        //VZTransferConstants.NEW_PHONE / VZTransferConstants.OLD_PHONE
        return phoneSelection;
    }

    public void setPhoneSelection(String phoneSelection) {
        this.phoneSelection = phoneSelection;
    }

    public boolean isCross() {
        return isCross;
    }

    public void setIsCross(boolean isCross) {
        this.isCross = isCross;
    }

    public boolean getWarnWifi() {return warnWifi;}

    public void setWarnWifi(boolean warnWifi) {this.warnWifi = warnWifi; }

    public boolean getCellularDataStatus() {
        return cellularDataStatus;
    }

    public void setCellularDataStatus(boolean cellularDataStatus) {
        this.cellularDataStatus = cellularDataStatus;
    }

    public boolean getAutoConnectToWifiOnDetect() {
        return autoConnectToWifiOnDetect;
    }

    public void setAutoConnectToWifiOnDetect(boolean autoConnectToWifiOnDetect) {
        this.autoConnectToWifiOnDetect = autoConnectToWifiOnDetect;
    }
    public String getParingDeviceID() {
        return paringDeviceID;
    }

    public void setParingDeviceID(String paringDeviceID) {
        this.paringDeviceID = paringDeviceID;
    }

    public String getParingDeviceModel() {
        return paringDeviceModel;
    }

    public void setParingDeviceModel(String paringDeviceModel) {
        this.paringDeviceModel = paringDeviceModel;
    }

    public String getParingDeviceOSVersion() {
        return paringDeviceOSVersion;
    }

    public void setParingDeviceOSVersion(String paringDeviceOSVersion) {
        this.paringDeviceOSVersion = paringDeviceOSVersion;
    }

    public String getParingDeviceDeviceType() {
        return paringDeviceDeviceType;
    }

    public void setParingDeviceDeviceType(String paringDeviceDeviceType) {
        this.paringDeviceDeviceType = paringDeviceDeviceType;
    }

    public void setHotspotName(String hotspotName) {
        if(null!=hotspotName) {
            hotspotName = hotspotName.trim().replaceAll("^\"|\"$", "");
        }
        LogUtil.d(TAG,"Hotspot name ="+hotspotName);
        this.hotspotName = hotspotName;
    }


    public String getHotspotName() {

        return (null == hotspotName?"":hotspotName);
    }

    public String getDbErrorMessage() {
        return (null == dbErrorMessage?"":dbErrorMessage);
    }

    public void setDbErrorMessage(String dbErrorMessage) {

        if(null != dbErrorMessage){
            this.dbErrorMessage += dbErrorMessage+",";
            if(this.dbErrorMessage.length()>2040) {
                this.dbErrorMessage = this.dbErrorMessage.substring(0, 2040);
            }
        }

    }
    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
    public long getAvailableSpaceAtReceiverEnd() {
        return availableSpaceAtReceiverEnd;
    }

    public void setAvailableSpaceAtReceiverEnd(long availableSpaceAtReceiverEnd) {
        this.availableSpaceAtReceiverEnd = availableSpaceAtReceiverEnd;
    }

    public boolean isConnectedViaWifi(){
        ConnectivityManager connectivityManager = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public void setDataTransferInterruptedByUserMsg(String dataTransferInterruptedByUser) {
        this.dataTransferInterruptedByUser = dataTransferInterruptedByUser;
    }
    public String getDataTransferInterruptedByUserMsg() {
        return dataTransferInterruptedByUser;
    }

    public void setConnectionType(String connType) {
        this.connectionType = connType;
    }
    public String getConnectionType() {
        return connectionType;
    }


    public boolean isDoingOneToMany() {
        return doingOneToMany;
    }

    public void setDoingOneToMany(boolean doingOneToMany) {
        this.doingOneToMany = doingOneToMany;
        this.waitForNewDevice = doingOneToMany;
    }

  /*  public void setMediaTypeArray(String[] mediaTypeArray) {
        this.mediaTypeArray = mediaTypeArray;
    }*/

    public String[] getMediaTypeArray() {
       // String media=getMediaToCollect();
        if(isCross()){
            return crossMediaTypeArray;
        }else if(isDoingOneToManyComb()){
            return crossMediaTypeArrayforOneToMany;
        }else {
            return AndroidMediaTypeArray;
        }
    }

    public boolean isProcessSMS() {
        return processSMS;
    }

    public void setProcessSMS(boolean processSMS) {
        this.processSMS = processSMS;
    }
    public boolean isWaitForNewDevice() {
        LogUtil.d(TAG,"Wait for new device :"+waitForNewDevice);

        return waitForNewDevice;
    }
    /*
     * Set false after all connection done.
     */
    public void setWaitForNewDevice(boolean waitForNewDevice) {
        this.waitForNewDevice = waitForNewDevice;
        LogUtil.d(TAG,"set is wait for new device :"+waitForNewDevice);
    }

    public String getPairingType() {
        return pairingType;
    }

    public void setPairingType(String pairingType) {
        this.pairingType = pairingType;
    }

    public boolean isCalllogsPermitted() {
        return CalllogsPermitted;
    }

    public void setCalllogsPermitted(boolean calllogsPermitted) {
        CalllogsPermitted = calllogsPermitted;
    }

    public boolean isSmsPermitted() {
        return SmsPermitted;
    }

    public void setSmsPermitted(boolean smsPermitted) {
        SmsPermitted = smsPermitted;
    }

    public boolean isContactsPermitted() {
        return ContactsPermitted;
    }

    public void setContactsPermitted(boolean contactsPermitted) {
        ContactsPermitted = contactsPermitted;
    }

    public boolean isCalendarPermitted() {
        return CalendarPermitted;
    }

    public void setCalendarPermitted(boolean calendarPermitted) {
        CalendarPermitted = calendarPermitted;
    }

    public boolean isDoingOneToManyComb() {
        return doingOneToManyComb;
    }

    public void setDoingOneToManyComb(boolean doingOneToManyComb) {
        this.doingOneToManyComb = doingOneToManyComb;
    }
}
