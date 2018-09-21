package com.verizon.contenttransfer.utils;

//import android.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTReceiverAppsListActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatConstants;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.p2p.model.ContentRecapVO;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class P2PFinishUtil {

    private static final String TAG = P2PFinishUtil.class.getName();
    private static P2PFinishUtil instance;
    private List<ContentRecapVO> contentRecapVOs = new ArrayList<ContentRecapVO>();

    public String getTotalPayload() {
        return totalPayload;
    }

    public void setTotalPayload(String totalPayload) {
        this.totalPayload = totalPayload;
    }

    public String getTotalTransferredData() {
        return totalTransferredData;
    }

    public void setTotalTransferredData(String totalTransferredData) {
        this.totalTransferredData = totalTransferredData;
    }

    private String totalPayload = VZTransferConstants.DEFAULT_TRANSFERRED_BYTES; // default value.
    private String totalTransferredData = VZTransferConstants.DEFAULT_TRANSFERRED_BYTES; // default value.

    public String getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(String avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    private String avgSpeed = "";
    private String totalTime = "";

    public static P2PFinishUtil getInstance() {
        if (instance == null) {
            instance = new P2PFinishUtil();
        }
        return instance;
    }

    public List<ContentRecapVO> getContentRecapVOs() {
        return contentRecapVOs;
    }

    public void addToTransferSummaryAdapter(Activity activity,String media, String transferred, String totalItem) {
        int totTransferred = 0;
        int totItem = 0;
        boolean checkStatus = true;
        boolean mediaPermission = false;
        String uiMedia="";
        try {
            totTransferred = Integer.parseInt(transferred);
            totItem = Integer.parseInt(totalItem);
        } catch (Exception e) {
            LogUtil.e(TAG, "addToTransferSummaryAdapter :" + e.getMessage());
            e.printStackTrace();
        }
        if (totTransferred != totItem) {
            checkStatus = false;
        }
        if (media.equalsIgnoreCase(VZTransferConstants.CONTACTS_STR)) {
            mediaPermission = CTGlobal.getInstance().isContactsPermitted();
            uiMedia=activity.getString(R.string.contacts_str);
        } else if (media.equalsIgnoreCase(VZTransferConstants.CALENDAR_STR)) {
            mediaPermission = CTGlobal.getInstance().isCalendarPermitted();
            uiMedia=activity.getString(R.string.calendars_str);

        } else if (media.equalsIgnoreCase(VZTransferConstants.SMS_STR)) {
            mediaPermission = CTGlobal.getInstance().isSmsPermitted();
            uiMedia=activity.getString(R.string.messages_str);

        } else if (media.equalsIgnoreCase(VZTransferConstants.CALLLOG_STR)) {
            mediaPermission = CTGlobal.getInstance().isCalllogsPermitted();
            uiMedia=activity.getString(R.string.callLogs_str);

        } else {
            if (media.equalsIgnoreCase(VZTransferConstants.PHOTOS_STR)) {
                uiMedia=activity.getString(R.string.photos_str);
            }else if(media.equalsIgnoreCase(VZTransferConstants.VIDEOS_STR)){
                uiMedia=activity.getString(R.string.videos_str);
            }else if (media.equalsIgnoreCase(VZTransferConstants.AUDIO_STR) ){
                uiMedia=activity.getString(R.string.musics_str);
            }else if (media.equalsIgnoreCase(VZTransferConstants.DOCUMENTS_STR)){
                uiMedia=activity.getString(R.string.documents_str);
            }else if (media.equalsIgnoreCase(VZTransferConstants.APPS_STR)) {
                uiMedia=activity.getString(R.string.apps_str);
            }
            mediaPermission = true;
        }

        contentRecapVOs.add(new ContentRecapVO(media, totTransferred, totItem, checkStatus, mediaPermission,uiMedia));
    }

    public void resetContentRecapVOs() {
        this.contentRecapVOs = new ArrayList<ContentRecapVO>();
    }

    private CTAnalyticUtil ctAnalyticUtil = new CTAnalyticUtil();

    public void generateAppAnalyticsFile(String clientIp, String description) {
        ctAnalyticUtil.setDescription(description);
        generateAppAnalyticsFile(clientIp);
    }

    public void generateAppAnalyticsFile(String clientIp) {

        if (SocketUtil.getCtAnalyticUtil(clientIp) != null) {
            ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(clientIp);
        }
        String transferStatus = ctAnalyticUtil.getDataTransferStatusMsg();
        JSONObject log = new JSONObject();
        String errorMsg = "";
        //Print duplicate file / reading error file count for future use.
/*        getNotTransferredCount(ctAnalyticUtil.getNotTransferredPhotoList());
        getNotTransferredCount(ctAnalyticUtil.getNotTransferredVideoList());
        getNotTransferredCount(ctAnalyticUtil.getNotTransferredMusicList());
        getNotTransferredCount(ctAnalyticUtil.getNotTransferredDocumentList());
        getNotTransferredCount(ctAnalyticUtil.getNotTransferredAppsList());*/

        int photoCount = ctAnalyticUtil.getPhotoCount() + ctAnalyticUtil.getDuplicatePhotoCount();
        int videoCount = ctAnalyticUtil.getVideoCount() + ctAnalyticUtil.getDuplicateVideoCount();
        int audioCount = ctAnalyticUtil.getMusicCount() + ctAnalyticUtil.getDuplicateMusicCount();
        int documentCount = ctAnalyticUtil.getDocCount() + ctAnalyticUtil.getDuplicateDocCount();
        int appCount = ctAnalyticUtil.getAppCount();

        int contactCount = (P2PFinishModel.getInstance().isInterrupted() ? 0 : ctAnalyticUtil.getContactsCount());
        int callLogCount = (P2PFinishModel.getInstance().isInterrupted() ? 0 : ctAnalyticUtil.getCallLogCount());
        int messageCount = (P2PFinishModel.getInstance().isInterrupted() ? 0 : ctAnalyticUtil.getSmsCount());
        int calendarCount = (P2PFinishModel.getInstance().isInterrupted() ? 0 : ctAnalyticUtil.getCalendarCount());

        String scanType = QRCodeUtil.getInstance().isUsingQRCode() ? "QR" : "Manual";
        String globalUUID = ContentPreference.getStringValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.GLOBAL_UUID, null);
        long connectedDeviceCount = (CTGlobal.getInstance().isDoingOneToMany()) ? CTGlobal.getInstance().getDeviceCount() : -1;

        LogUtil.i(TAG, "Application status =" + CTGlobal.getInstance().getApplicationStatus());
        if (!CTGlobal.getInstance().isVztransferStarted()) {
            transferStatus = VZTransferConstants.DATA_TRANSFER_NOT_STARTED;
            // If user cancel before connection establish.
            scanType = "";
        }

        LogUtil.d(TAG, "transferStatus =" + transferStatus);
        String transferSpeed = CustomDialogs.averageSpeed() + " Mbps";

        long totDataTransferred = (ctAnalyticUtil.getTransferredBytes() - (ctAnalyticUtil.getTotDuplicateBytesTransferred() + ctAnalyticUtil.getTotFailedBytesTransferred()));
        String dataTransferred = DataSpeedAnalyzer.convertBytesToMegString(totDataTransferred);
        if (dataTransferred.equals(VZTransferConstants.DEFAULT_TRANSFERRED_BYTES)) {
            if (totDataTransferred > 0) {
                dataTransferred = "0.1";
            } else {
                dataTransferred = "0";
            }
            transferSpeed = "1 Mbps";
        }
        if (null != transferStatus) {
            if (!transferStatus.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
                errorMsg = CTGlobal.getInstance().getDbErrorMessage();
            }
            if (!CTGlobal.getInstance().isVztransferStarted()) {
                dataTransferred = "0";
                transferSpeed = "0 Mbps";
            }
        }


        try {
            log.put("mode", BuildConfig.BUILD_TYPE);
            log.put("deviceId", CTGlobal.getInstance().getDeviceUUID());
            log.put("pairingDeviceId", CTGlobal.getInstance().getParingDeviceID());
            log.put("deviceModel", Build.MODEL);
            log.put("pairingDeviceModel", CTGlobal.getInstance().getParingDeviceModel());
            log.put("deviceOsVersion", Utils.getAndroidVersion());
            log.put("pairingDeviceOsVersion", CTGlobal.getInstance().getParingDeviceOSVersion());
            log.put("deviceType", Build.MANUFACTURER);
            log.put("pairingDeviceType", CTGlobal.getInstance().getParingDeviceDeviceType());
            log.put("pairingType", CTGlobal.getInstance().getPairingType());
            log.put("status", transferStatus);
            log.put("errorMessage", errorMsg);
            log.put("contacts", contactCount);
            log.put("photos", photoCount);
            log.put("videos", videoCount);
            log.put("sms", messageCount);
            log.put("audio", audioCount);
            log.put("callLogs", callLogCount);
            log.put("documents", documentCount);
            log.put("calendars", calendarCount);
            log.put("reminders", 0);
            log.put("wifiSettings", 0);
            log.put("deviceSettings", 0);
            log.put("deviceApps", appCount);
            log.put("alarms", 0);
            log.put("wallpapers", 0);
            log.put("voiceRecordings", 0);
            log.put("ringtones", 0);
            log.put("sNotes", 0);
            log.put("transferType", CTGlobal.getInstance().getPhoneSelection());
            log.put("wifiAccessPoint", CTGlobal.getInstance().getHotspotName());
            log.put("dataTransferred", dataTransferred);
            log.put("transferSpeed", transferSpeed);
            log.put("duration", ctAnalyticUtil.getTransferDuration());
            log.put("buildVersion", BuildConfig.VERSION_NAME);
            log.put("description", ctAnalyticUtil.getDescription());
            // log.put("transferDate", String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date())));
            log.put("transferDate", String.valueOf(System.currentTimeMillis()));
            log.put("transferStart", CTGlobal.getInstance().getTransferStart());
            log.put("scanType", scanType);
            log.put("appType", CTGlobal.getInstance().getAppType());
            log.put("mdn", CTGlobal.getInstance().getMdn());
            log.put("locationRadioId", CTGlobal.getInstance().getStoreMacId());
            log.put("bluetoothBeaconMajorId", CTGlobal.getInstance().getBluetoothBeaconMajorId());
            log.put("bluetoothBeaconMinorId", CTGlobal.getInstance().getBluetoothBeaconMinorId());
            log.put("storeId", CTGlobal.getInstance().getStoreId());
            log.put("location", CTGlobal.getInstance().getLocation());
            log.put("state", CTGlobal.getInstance().getState());
            log.put("region", CTGlobal.getInstance().getRegion());
            log.put("deviceCount", connectedDeviceCount);
            log.put("globalUUID", globalUUID);

        } catch (Exception e) {
            LogUtil.d(TAG, e.getMessage());
        }
        String jsonAppAnalyticsLog = log.toString();
        LogUtil.d(TAG, "jsonAppAnalyticsLog = " + jsonAppAnalyticsLog);
        LogUtil.d(TAG, "isconnected via wifi :" + WifiManagerControl.isConnectedViaWifi());
        LogUtil.d(TAG, "getConnectionType :" + CTGlobal.getInstance().getConnectionType());

        //AppAnalyticsService();
        MediaFileListGenerator.writeAppAnalyticsToFile(jsonAppAnalyticsLog, VZTransferConstants.APP_ANALYTICS);

        //check for Router connection in case of wifi-direct and hotspot transfer
        Utils.uploadAppAnalyticsFile();
        Utils.uploadCrashErrorReportFile();
    }

    public void generateVzcloudAnalyticsFile(String event) {
        JSONObject log = new JSONObject();
        String globalUUID = ContentPreference.getStringValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.GLOBAL_UUID, null);
        int clicked = 1;
        try {
             log.put("globalUUID", globalUUID);
             log.put("deviceId", CTGlobal.getInstance().getDeviceUUID());
             log.put("didClickImage", clicked);
             log.put("buildVersion", BuildConfig.VERSION_NAME);
             log.put("description",event);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String jsonAnalyticsVzbanner = log.toString();
            LogUtil.d(TAG, "json banner analytics :" + jsonAnalyticsVzbanner);
        MediaFileListGenerator.writeAppAnalyticsToFile(jsonAnalyticsVzbanner, VZTransferConstants.VZCLOUD_CLICKS);
        Utils.uploadVzcloudBannerJSON();
    }


        private void getNotTransferredCount(ArrayList<TransferSummaryStatus> transferSummaryStatusList) {
        long duplicateCount = 0;
        long errorFileCount = 0;
        String mediaType = "";
        if (transferSummaryStatusList != null && transferSummaryStatusList.size() > 0) {
            for (TransferSummaryStatus item : transferSummaryStatusList) {
                mediaType = item.getMedia();
                if (item.getMessage().equals(VZTransferConstants.TRANSFER_SUMMARY_DUPLICATE_FILE)) {
                    duplicateCount++;
                } else if (item.getMessage().equals(VZTransferConstants.TRANSFER_SUMMARY_ERROR_FILE)) {
                    errorFileCount++;
                }
            }
            LogUtil.d(TAG, "Media type :[" + mediaType + "] Duplicate file count :" + duplicateCount + " and reading error file count :" + errorFileCount);
        }
    }

    public String getErrorMsg(String dataTransferStatusMsg) {
        String errorMsg = "";
        if (dataTransferStatusMsg.contains(VZTransferConstants.DATA_TRANSFER_INTERRUPTED)) {
            errorMsg = CTSiteCatConstants.SITECAT_VALUE_ACTION_TRANSFERINTERRUPT;
        } else if (dataTransferStatusMsg.contains(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER)) {
            errorMsg = CTSiteCatConstants.SITECAT_VALUE_ACTION_CANCELTRANSFER;
        } else {
            errorMsg = CTSiteCatConstants.SITECAT_VALUE_ACTION_TRANSFERFAILED;
        }
        return errorMsg;
    }

    public void createContentRecapVO(Activity activity) {
        CTAnalyticUtil ctAnalyticUtil = new CTAnalyticUtil();
        if (SocketUtil.getCtAnalyticUtil(null) != null) {
            ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
        }
        boolean receiver = Utils.isReceiverDevice();
        String totDataTransferred = DataSpeedAnalyzer.convertBytesToMegString(ctAnalyticUtil.getTransferredBytes() - ctAnalyticUtil.getTotFailedBytesTransferred());
        P2PFinishUtil.getInstance().setTotalTransferredData(totDataTransferred);
        LogUtil.d(TAG, "tempTotalDataTransfered .. = " + totDataTransferred);
        P2PFinishUtil.getInstance().setTotalTime(CustomDialogs.getSamLatestTimeElapsed());
        P2PFinishUtil.getInstance().setAvgSpeed(CustomDialogs.averageSpeed());

        P2PFinishUtil.getInstance().resetContentRecapVOs();
        if (receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            String receivedContactCount = "0";
            if (MediaFetchingService.isContactReceived && !P2PFinishModel.getInstance().isInterrupted()) {
                receivedContactCount = String.valueOf(ctAnalyticUtil.getContactsCount());
            }
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.CONTACTS_STR, receivedContactCount,
                    String.valueOf(ctAnalyticUtil.getContactsCount())
            );
        } else if (!receiver && Utils.getContentSelection(VZTransferConstants.CONTACTS_STR).getContentflag()) {
            String totContactCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.CONTACTS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.CONTACTS_STR, String.valueOf(ctAnalyticUtil.getContactsCount()), totContactCount);
        }

        if (receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getPhotosState().toLowerCase().trim().equalsIgnoreCase("true")) {
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.PHOTOS_STR, getTotalCount(ctAnalyticUtil.getPhotoCount(), ctAnalyticUtil.getDuplicatePhotoCount()), String.valueOf(ReceiveMetadata.TOTAL_PHOTO_COUNT));
        } else if (!receiver && Utils.getContentSelection(VZTransferConstants.PHOTOS_STR).getContentflag()) {
            String totPhotoCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.PHOTOS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.PHOTOS_STR,
                    getTotalCount((ctAnalyticUtil.getPhotoCount() - ctAnalyticUtil.getFailedPhotoCount()), ctAnalyticUtil.getDuplicatePhotoCount()),
                    totPhotoCount);
        }


        if (receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getVideosState().toLowerCase().trim().equalsIgnoreCase("true")) {
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.VIDEOS_STR, getTotalCount(ctAnalyticUtil.getVideoCount(), ctAnalyticUtil.getDuplicateVideoCount()), String.valueOf(ReceiveMetadata.TOTAL_VIDEO_COUNT));
        } else if (!receiver && Utils.getContentSelection(VZTransferConstants.VIDEOS_STR).getContentflag()) {
            String totVideoCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.VIDEOS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.VIDEOS_STR,
                    getTotalCount((ctAnalyticUtil.getVideoCount() - ctAnalyticUtil.getFailedVideoCount()), ctAnalyticUtil.getDuplicateVideoCount()), totVideoCount);
        }

        if (Utils.isSupportMusic() && receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getMusicsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.AUDIO_STR, getTotalCount(ctAnalyticUtil.getMusicCount(), ctAnalyticUtil.getDuplicateMusicCount()), String.valueOf(ReceiveMetadata.TOTAL_AUDIO_COUNT));
        } else if (!CTGlobal.getInstance().isCross() && !receiver && Utils.getContentSelection(VZTransferConstants.AUDIO_STR).getContentflag()) {
            String totMusicCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.AUDIO_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.AUDIO_STR,
                    getTotalCount((ctAnalyticUtil.getMusicCount() - ctAnalyticUtil.getFailedMusicCount()), ctAnalyticUtil.getDuplicateMusicCount()), totMusicCount);
        }
        if (receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true")) {
            String receivedCalendarCount = "0";
            if (!P2PFinishModel.getInstance().isInterrupted()) {
                receivedCalendarCount = getTotalCount(ctAnalyticUtil.getCalendarCount(), ctAnalyticUtil.getDuplicateCalendarCount());
            }
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,
                    VZTransferConstants.CALENDAR_STR,
                    receivedCalendarCount,
                    String.valueOf(ReceiveMetadata.TOTAL_CALENDAR_COUNT));
        } else if (!receiver && Utils.getContentSelection(VZTransferConstants.CALENDAR_STR).getContentflag()) {
            String totCalendarCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.CALENDAR_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.CALENDAR_STR, getTotalCount((ctAnalyticUtil.getCalendarCount() - ctAnalyticUtil.getFailedCalendarCount()), ctAnalyticUtil.getDuplicateCalendarCount()), totCalendarCount);
        }
        if (!CTGlobal.getInstance().isCross() && receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            String smsReceivedCount = "0";
            int smsCount = ctAnalyticUtil.getSmsCount();
            LogUtil.d(TAG, "Total sms count =" + smsCount);
            if (CTGlobal.getInstance().isProcessSMS() && !P2PFinishModel.getInstance().isInterrupted()) {
                smsReceivedCount = String.valueOf(smsCount);
            }
            LogUtil.d(TAG, "SMS received count :" + smsReceivedCount + "  CTGlobal.getInstance().isProcessSMS()=" + CTGlobal.getInstance().isProcessSMS());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.SMS_STR, smsReceivedCount, String.valueOf(smsCount));
        } else if (!CTGlobal.getInstance().isCross() && !receiver && Utils.getContentSelection(VZTransferConstants.SMS_STR).getContentflag()) {
            String totSmsCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.SMS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.SMS_STR, String.valueOf(ctAnalyticUtil.getSmsCount()), totSmsCount);
        }

        if (!CTGlobal.getInstance().isCross() && receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            String receivedCalllogsCount = "0";
            if (MediaFetchingService.isCalllogsReceived && !P2PFinishModel.getInstance().isInterrupted()) {
                receivedCalllogsCount = String.valueOf(ctAnalyticUtil.getCallLogCount());
            }
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,
                    VZTransferConstants.CALLLOG_STR,
                    receivedCalllogsCount,
                    String.valueOf(ctAnalyticUtil.getCallLogCount())
            );
        } else if (!CTGlobal.getInstance().isCross() && !receiver && Utils.getContentSelection(VZTransferConstants.CALLLOG_STR).getContentflag()) {

            String totCalllogCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.CALLLOG_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.CALLLOG_STR, String.valueOf(ctAnalyticUtil.getCallLogCount()), totCalllogCount);
        }

        if (!CTGlobal.getInstance().isCross() && receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getDocumentsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.DOCUMENTS_STR, getTotalCount(ctAnalyticUtil.getDocCount(), ctAnalyticUtil.getDuplicateDocCount()), String.valueOf(ReceiveMetadata.TOTAL_DOCUMENT_COUNT));
        } else if (!CTGlobal.getInstance().isCross() && !receiver && Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR).getContentflag()) {
            String totDocumentCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.DOCUMENTS_STR,
                    getTotalCount((ctAnalyticUtil.getDocCount() - ctAnalyticUtil.getFailedDocCount()), ctAnalyticUtil.getDuplicateDocCount()), totDocumentCount);
        }

        if (receiver && null != ReceiveMetadata.mediaStateObject && ReceiveMetadata.mediaStateObject.getAppsState().toLowerCase().trim().equalsIgnoreCase("true")) {
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.APPS_STR, String.valueOf(ctAnalyticUtil.getAppCount()), String.valueOf(ReceiveMetadata.TOTAL_APP_COUNT));
        } else if (!receiver && Utils.getContentSelection(VZTransferConstants.APPS_STR).getContentflag()) {
            String totAppCount = String.valueOf(Utils.getContentSelection(VZTransferConstants.APPS_STR).getContentsize());
            P2PFinishUtil.getInstance().addToTransferSummaryAdapter(activity,VZTransferConstants.APPS_STR, String.valueOf(ctAnalyticUtil.getAppCount() - ctAnalyticUtil.getFailedAppCount()), totAppCount);
        }
    }

    private String getTotalCount(String count, String dupCount) {
        String totalMediaCount = "";
        try {
            long mediaCount = 0;
            if (null != count && !count.equals("")) {
                mediaCount = Long.parseLong(count);
            }
            long dupMediaCount = 0;
            if (null != dupCount && !dupCount.equals("")) {
                dupMediaCount = Long.parseLong(dupCount);
            }
            totalMediaCount = String.valueOf(mediaCount + dupMediaCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalMediaCount;
    }

    private String getTotalCount(long mediaCount, long dupCount) {
        String totalMediaCount = "";
        try {
            totalMediaCount = String.valueOf(mediaCount + dupCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalMediaCount;
    }

    public void uploadAppAnalyticFile() {
        if (CTGlobal.getInstance().isAppAnalyticsAysncTaskRunning()) {
            return;
        }

        try {
            fileUploadwithBoundary( VZTransferConstants.APP_ANALYTICS_FILE ,"------VZTXFR");
            CTGlobal.getInstance().setAppAnalyticsAysncTaskRunning(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CTGlobal.getInstance().setAppAnalyticsAysncTaskRunning(false);
        }
    }

    public void uploadVzcloudBannerAnalytics() {
        if (CTGlobal.getInstance().isVzcloudClickAnalyticsRunning()) {
            return;
        }

        try {
            LogUtil.d(TAG,"vzcloud analytics generated");
            fileUploadwithBoundary( VZTransferConstants.VZCLOUD_CLICKS_FILE ,"------TCCLK");
            CTGlobal.getInstance().setVzcloudClickAnalyticsRunning(true);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            CTGlobal.getInstance().setVzcloudClickAnalyticsRunning(false);
        }
    }

    public void fileUploadwithBoundary(String fileName,String boundaryName){

        String filePath = VZTransferConstants.VZTRANSFER_DIR + fileName;

        File sourceFile = new File(filePath);
        LogUtil.d(TAG, fileName + " File.exists()= " + sourceFile.exists());

        if (!sourceFile.isFile() && !sourceFile.exists()) {
            LogUtil.d(TAG, "Source File not exist :" + sourceFile.getAbsolutePath());//FullPath);
            return;
        }
        LogUtil.d(TAG, "upload Analytic File");
        int serverResponseCode = 0;

        String VZ_AUDIT_HTTP_URL = VZTransferConstants.APP_ANALYTICS_FILE_URL;

        if (BuildConfig.LOCAL_DB_PROD) {
            VZ_AUDIT_HTTP_URL = VZTransferConstants.APP_ANALYTICS_FILE_PROD_URL;
        } else {
            VZ_AUDIT_HTTP_URL = VZTransferConstants.APP_ANALYTICS_FILE_URL;
        }

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = boundaryName;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {

            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            URL url = new URL(VZ_AUDIT_HTTP_URL);
            LogUtil.d(TAG, url.toString());

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copys
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", filePath);
            //conn.setRequestProperty("user", "vztransfer") );

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + filePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                LogUtil.i(TAG, "->");
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage().toString();
            LogUtil.i(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            // ------------------ read the SERVER RESPONSE
            DataInputStream inStream;
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    LogUtil.e(TAG, "SOF Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                LogUtil.e(TAG, "SOF error: " + ioex.getMessage());
            }
            //close the streams
            fileInputStream.close();
            dos.flush();
            dos.close();

            if (serverResponseCode == 200) {
                LogUtil.d(TAG, "Audit file upload was successful. Delete file.");
                sourceFile.delete();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            LogUtil.e(TAG, "URL error: " + e1.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
            LogUtil.e(TAG, "Upload file to server Exception - Exception : " + e1.getMessage());
        }
    }
    public void completeTransferFinishProcess(String statusMsg) {
        if(Utils.isReceiverDevice()
                && !CTGlobal.getInstance().isCross()
                && VZTransferConstants.SUPPORT_APPS
                && SocketUtil.getCtAnalyticUtil(null).getAppCount()>0
                && statusMsg.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)){
            Intent intent = new Intent( CTGlobal.getInstance().getContentTransferContext(), CTReceiverAppsListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(VZTransferConstants.MESSAGE_KEY, VZTransferConstants.TRANSFER_FINISH_HEADER);
            CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
        }else {
            CTReceiverModel.getInstance().launchFinishActivity();
        }
    }
}
