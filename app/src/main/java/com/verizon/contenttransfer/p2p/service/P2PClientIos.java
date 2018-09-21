package com.verizon.contenttransfer.p2p.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.sender.AppSender;
import com.verizon.contenttransfer.p2p.sender.CalendarSender;
import com.verizon.contenttransfer.p2p.sender.CallLogSender;
import com.verizon.contenttransfer.p2p.sender.ContactsSender;
import com.verizon.contenttransfer.p2p.sender.DocumentSender;
import com.verizon.contenttransfer.p2p.sender.PhotoSender;
import com.verizon.contenttransfer.p2p.sender.SMSSender;
import com.verizon.contenttransfer.p2p.sender.VideoSender;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifidirect.DeviceIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class P2PClientIos extends AsyncTask<Void, Void, String> {

    private static final String TAG = P2PClientIos.class.getName();
    public Socket iosClientSocket = null;
    private String host;
    private String status="";
    private CTAnalyticUtil ctAnalyticUtil;
    private boolean isFinishedTransfer = false;
    private boolean isTransferStarted = false;

    public P2PClientIos(String host) {
        this.host = host;
    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "P2p client ios started.");
        String loggingCurrentStatus = "";
        try {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            try {
                LogUtil.d(TAG, "Opening client socket - to host " + host);
                iosClientSocket = SocketUtil.getClientSocket(host);
                ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(host);
                LogUtil.d(TAG, " iosClientSocket=" + iosClientSocket);

                if(iosClientSocket == null){
                    LogUtil.e(TAG,"Socket is null.");
                    throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Socket is null");
                }
                if(ctAnalyticUtil == null){
                    LogUtil.e(TAG,"CTAnalyticUtil is not found.");
                    throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"CTAnalyticUtil is null");
                }
                loggingCurrentStatus = "Socket connection success";
                CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_CONNECTION_ESTABLISHED);
                LogUtil.d(TAG, "iosClientSocket.isConnected()=" + iosClientSocket.isConnected());


                iosClientSocket.setTcpNoDelay(true);

                BufferedReader in = new BufferedReader(new InputStreamReader(iosClientSocket.getInputStream()));
                LogUtil.d(TAG, "Sending Message to Server host : " + host);//+ "#" + secretCode);


                String fromServer = "DUMB MESSAGE";

                try {


                    LogUtil.d(TAG,"Buffered reader in= :"+in);
                    MediaTransferService service = new MediaTransferService();
                    service.setTimedOutRecorded(Utils.isConnectionTimedOutRecorded());
                    do {
                        if((fromServer = in.readLine()) != null){
                            LogUtil.d(TAG, "Form server : "+fromServer);
                            String statusMsg = "";
                            if(!isTransferStarted) {
                                CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_TRANSFER_STARTED);
                                isTransferStarted = true;
                            }
                            LogUtil.d(TAG, "Received from server -- " + host + " : " + fromServer);

                            if (fromServer.equalsIgnoreCase("Bye")) {
                                break;
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.VCARD_REQUEST_HEADER)) {
                                //Launch Contacts Request
                                loggingCurrentStatus = "VCARD HEADER";
                                ContactsSender.sendContacts(iosClientSocket,ctAnalyticUtil);
                                ctAnalyticUtil.setContactsCount(MediaFileListGenerator.TOT_CONTACTS);
                                LogUtil.d(TAG, "Total Contacts sent:" + ctAnalyticUtil.getContactsCount());
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.SMS_REQUEST_HEADER)) {
                                //Launch Contacts Request
                                loggingCurrentStatus = "SMS HEADER";
                                SMSSender.sendSMS(iosClientSocket);
                                ctAnalyticUtil.setSmsCount( MediaFileListGenerator.TOT_MESSAGES);
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.CALLLOG_REQUEST_HEADER)) {
                                //Launch Contacts Request
                                loggingCurrentStatus = "CALLLOG HEADER";
                                CallLogSender.sendCalllogs(iosClientSocket);
                                ctAnalyticUtil.setCallLogCount(MediaFileListGenerator.TOT_CALLLOGS);
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.PHOTO_LOG_REQUEST_HEADER)) {
                                //Launch Photo Log send request
                                loggingCurrentStatus = "PHOTO LOG HEADER";
                                PhotoSender.sendPhotosFileList(iosClientSocket);
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.APPS_LOG_REQUEST_HEADER)) {
                                if(VZTransferConstants.SUPPORT_APPS){
                                    //Launch App Log send request
                                    loggingCurrentStatus = "APP LOG HEADER";
                                    AppSender.sendAppsFileList(iosClientSocket);
                                }
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.VIDEO_LOG_REQUEST_HEADER)) {
                                //Launch Video Log send request
                                loggingCurrentStatus = "VIDEO LOG HEADER";
                                VideoSender.sendVideosFileList(iosClientSocket);
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.CALENDAR_LOG_REQUEST_HEADER)) {
                                //Launch calendar Log send request
                                loggingCurrentStatus = "CALENDAR LOG HEADER";
                                CalendarSender.sendCalendarFileList(iosClientSocket);
                            } else if (fromServer.equalsIgnoreCase(VZTransferConstants.DOCUMENT_LOG_REQUEST_HEADER)) {
                                if(VZTransferConstants.SUPPORT_DOCS) {
                                    //Lauch Documents Request
                                    loggingCurrentStatus = "DOCUMENT LOG HEADER";
                                    DocumentSender.sendDocumentsFileList(iosClientSocket);
                                }

                            } else if (fromServer.contains(VZTransferConstants.PHOTO_REQUEST_HEADER)) {

                                //Launch Photo request
                                loggingCurrentStatus = "PHOTO HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.PHOTO_REQUEST_HEADER.length());
                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                mediaFileName = Utils.getDecodedString(mediaFileName);
                                if (mediaFileName.startsWith(VZTransferConstants.DUPLICATE)) {


                                    setDuplicateFileProperties(mediaFileName, VZTransferConstants.PHOTOS_STR);

                                    ctAnalyticUtil.addDuplicatePhotoCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicatePhotoCount() + ctAnalyticUtil.getPhotoCount()) + " / " + MediaFileListGenerator.TOT_PHOTOS;
                                    updateStatusMessage(statusMsg);
                                    service.sendDuplicateAck(iosClientSocket);
                                } else {
                                    DataSpeedAnalyzer.setDuplicateFile(false);
                                    DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.PHOTOS_STR);
                                    //LogUtil.d(TAG, "Data Downloaded =" + DataSpeedAnalyzer.getDataDownloaded());
                                    ctAnalyticUtil.addPhotoCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicatePhotoCount() + ctAnalyticUtil.getPhotoCount()) + " / " + MediaFileListGenerator.TOT_PHOTOS;

                                    updateStatusMessage(statusMsg);
                                    service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.PHOTOS, mediaFileName, "0000000000");
                                }
                            } else if (fromServer.contains(VZTransferConstants.APPS_REQUEST_HEADER)) {
                                //Launch Apps request
                                loggingCurrentStatus = "APPS HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.APPS_REQUEST_HEADER.length());

                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                //if(!CTGlobal.getInstance().isCross()){
                                if(!ctAnalyticUtil.isCross()){
                                    mediaFileName = Utils.getDecodedString(mediaFileName);}
                                else{
                                    mediaFileName= CTAppUtil.getInstance().getIconPath(Utils.getDecodedString(mediaFileName));
                                }
                                DataSpeedAnalyzer.setDuplicateFile(false);
                                DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.APPS_STR);
                                LogUtil.d(TAG, "Data Downloaded =" + ctAnalyticUtil.getTransferredBytes());
                                ctAnalyticUtil.addAppCount(1);
                                statusMsg = ctAnalyticUtil.getAppCount() + " / " + MediaFileListGenerator.TOT_APPS;

                                updateStatusMessage(statusMsg);

                                service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.APPS, mediaFileName, "0000000000");

                            } else if (fromServer.contains(VZTransferConstants.VIDEO_REQUEST_HEADER)) {
                                //Launch Video request
                                loggingCurrentStatus = "VIDEO HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.VIDEO_REQUEST_HEADER.length());
                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                mediaFileName = Utils.getDecodedString(mediaFileName);
                                if (mediaFileName.startsWith(VZTransferConstants.DUPLICATE)) {
                                    setDuplicateFileProperties(mediaFileName, VZTransferConstants.VIDEOS_STR);
                                    ctAnalyticUtil.addDuplicateVideoCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateVideoCount() + ctAnalyticUtil.getVideoCount()) + " / " + MediaFileListGenerator.TOT_VIDEOS;
                                    updateStatusMessage(statusMsg);
                                    service.sendDuplicateAck(iosClientSocket);
                                } else {
                                    DataSpeedAnalyzer.setDuplicateFile(false);
                                    DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.VIDEOS_STR);
                                    ctAnalyticUtil.addVideoCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateVideoCount() + ctAnalyticUtil.getVideoCount()) + " / " + MediaFileListGenerator.TOT_VIDEOS;
                                    updateStatusMessage(statusMsg);
                                    service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.VIDEOS, mediaFileName, "0000000000");
                                }
                            } else if (fromServer.contains(VZTransferConstants.CALENDAR_REQUEST_HEADER)) {
                                //Launch calendar request
                                loggingCurrentStatus = "CALENDAR HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.CALENDAR_REQUEST_HEADER.length());
                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                DataSpeedAnalyzer.setDuplicateFile(false);
                                DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.CALENDAR_STR);
                                ctAnalyticUtil.addCalendarCount(1);
                                statusMsg = ctAnalyticUtil.getCalendarCount() + " / " + MediaFileListGenerator.TOT_CALENDAR;
                                updateStatusMessage(statusMsg);
                                service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.CALENDAR, mediaFileName, "0000000000");

                            } else if (fromServer.contains(VZTransferConstants.MUSIC_REQUEST_HEADER)) {
                                //Launch Video request
                                loggingCurrentStatus = "MUSIC HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.MUSIC_REQUEST_HEADER.length());
                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                mediaFileName = Utils.getDecodedString(mediaFileName);
                                if (mediaFileName.startsWith(VZTransferConstants.DUPLICATE)) {
                                    setDuplicateFileProperties(mediaFileName, VZTransferConstants.AUDIO_STR);
                                    ctAnalyticUtil.addDuplicateMusicCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateMusicCount() + ctAnalyticUtil.getMusicCount()) + " / " + MediaFileListGenerator.TOT_MUSICS;
                                    updateStatusMessage(statusMsg);
                                    service.sendDuplicateAck(iosClientSocket);
                                } else {
                                    DataSpeedAnalyzer.setDuplicateFile(false);
                                    DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.AUDIO_STR);
                                    ctAnalyticUtil.addMusicCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateMusicCount() + ctAnalyticUtil.getMusicCount()) + " / " + MediaFileListGenerator.TOT_MUSICS;
                                    updateStatusMessage(statusMsg);
                                    service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.MUSICS, mediaFileName, "0000000000");
                                }
                            } else if (fromServer.contains(VZTransferConstants.DOCUMENTS_REQUEST_HEADER)) {
                                //Launch Documents request
                                loggingCurrentStatus = "DOCUMENT HEADER";
                                String mediaFileName = fromServer.substring(VZTransferConstants.DOCUMENTS_REQUEST_HEADER.length());
                                LogUtil.d(TAG, "Media File name to be transferred : " + mediaFileName);
                                mediaFileName = Utils.getDecodedString(mediaFileName);
                                if (mediaFileName.startsWith(VZTransferConstants.DUPLICATE)) {
                                    setDuplicateFileProperties(mediaFileName,VZTransferConstants.DOCUMENTS_STR);
                                    ctAnalyticUtil.addDuplicateDocCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateDocCount() + ctAnalyticUtil.getDocCount()) + " / " + MediaFileListGenerator.TOT_DOCS;
                                    updateStatusMessage(statusMsg);
                                    service.sendDuplicateAck(iosClientSocket);
                                } else {
                                    DataSpeedAnalyzer.setDuplicateFile(false);
                                    DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.DOCUMENTS_STR);
                                    ctAnalyticUtil.addDocCount(1);
                                    statusMsg = (ctAnalyticUtil.getDuplicateDocCount() + ctAnalyticUtil.getDocCount()) + " / " + MediaFileListGenerator.TOT_DOCS;
                                    updateStatusMessage(statusMsg);
                                    service.transferMedia(ctAnalyticUtil,iosClientSocket, VZTransferConstants.DOCUMENTS, mediaFileName, "0000000000");
                                }
                            } else if (fromServer.contains(VZTransferConstants.TRANSFER_FINISH_HEADER)) {
                                //Launch Finish Activity
                                loggingCurrentStatus = "FINISHED HEADER";
                                LogUtil.d(TAG, "Received Transfer finish header");
                                isFinishedTransfer = true;
                                status = VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED;
                            } else {
                                loggingCurrentStatus = "UNKNOWN HEADER "+fromServer;
                                LogUtil.d(TAG, "Unknown Message received from Server." + fromServer);
                            }
                        }else {
                            if(!isFinishedTransfer ){
                                if(null == fromServer){
                                    LogUtil.d(TAG, "During transfer we receive null Message from Server... fromServer =" + fromServer);
                                    throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+loggingCurrentStatus+"> Received null from server.");
                                }
                            }else if(ctAnalyticUtil.isVztransferCancelled()){
                                throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+loggingCurrentStatus+"> Transfer cancelled by user");
                            }
                            else if(CTGlobal.getInstance().getExitApp()){
                                throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+loggingCurrentStatus+"> Exit app");
                            }else {
                                Thread.sleep(1000);
                                LogUtil.d(TAG, "Waiting to start transfer.. receiving null : fromServer =" + fromServer);
                            }
                        }
                    }while (!isFinishedTransfer);
                } catch (Exception e) {
                    String msg = e.getMessage();
                    e.printStackTrace();
                    LogUtil.d(TAG, "Transfer Exception... : " + msg);
                    CTGlobal.getInstance().setDbErrorMessage(loggingCurrentStatus+"> Sending1-"+e.getMessage());

                    status = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
                }
                finally {
                    // review the transfer status.
                    LogUtil.d(TAG, "review the transfer status.");

                    try {
                        LogUtil.d(TAG, "stopping timer...");
                        DataSpeedAnalyzer.stopTimer();
                        LogUtil.d(TAG,"stopping timer completed...");
                    }catch (Exception e){
                        LogUtil.d(TAG,"Stop timer exception.."+e.getMessage());
                        e.printStackTrace();
                    }
                }

                //LogUtil.d(TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(TAG, "Failed iOS Client Socket connection : " + e.getMessage());
                CTGlobal.getInstance().setDbErrorMessage(loggingCurrentStatus+"> Sending2-"+e.getMessage());
                status = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
                e.printStackTrace();
            } finally {
                if (iosClientSocket != null) {
                    try {
                        iosClientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            LogUtil.d(TAG, "Exception from p2p client ios : "+e.getMessage());
            e.printStackTrace();
        }
        return status;
    }
    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);
        LogUtil.d(TAG, "P2PClientIos onPostExecute..  status =" + status);

        launchFinishActivity(status);
        //local analytics
        P2PFinishUtil.getInstance().generateAppAnalyticsFile(host);
        Utils.deleteGeneratedPasswordManagerDbFile();
    }
    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG, "P2PClientIos onPreExecute...");
        status = "";
        isFinishedTransfer = false;
        isTransferStarted = false;
    }
    private void setDuplicateFileProperties(String duplicateFile, String media) {
        DataSpeedAnalyzer.setDuplicateFile(true);
        DataSpeedAnalyzer.setCurrentMediaType(media);
        try {
            Utils.updateTransferredBytes(ctAnalyticUtil, Long.parseLong(duplicateFile.substring(VZTransferConstants.DUPLICATE.length())));
        }catch (Exception e){
            LogUtil.d(TAG, "Duplicate file size invalid :"+duplicateFile);
        }
    }

    public void launchFinishActivity(String status) {

        LogUtil.d(TAG,"MediaFetchingService.isP2PFinishActivityLaunched ="+MediaFetchingService.isP2PFinishActivityLaunched);
        LogUtil.d(TAG,"DeviceIterator.goToMVMHome ="+DeviceIterator.goToMVMHome);
        if(MediaFetchingService.isP2PFinishActivityLaunched || DeviceIterator.goToMVMHome){
            return;
        }

        String finishStatusMsg = VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED;
        if(!status.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)){
            if(ctAnalyticUtil.isVztransferCancelled()
                    || !CTGlobal.getInstance().isVztransferStarted() ){
                finishStatusMsg = VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER;
            }else if(status.equals(VZTransferConstants.CONNECTION_FAILED)){
                finishStatusMsg = VZTransferConstants.CONNECTION_FAILED;
            }else{
                finishStatusMsg = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
            }
        }
        ctAnalyticUtil.setDataTransferStatusMsg(finishStatusMsg);
        CTGlobal.getInstance().setPairingType(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)?VZTransferConstants.WIFI_DIRECT_CONNECTION:
                CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)?VZTransferConstants.HOTSPOT_WIFI_CONNECTION:VZTransferConstants.PHONE_WIFI_CONNECTION);
        LogUtil.d(TAG, "setDataTransferStatusMsg =" + ctAnalyticUtil.getDataTransferStatusMsg());

        Intent intent = new Intent(VZTransferConstants.TRANSFER_FINISHED_MSG);
        intent.putExtra("host", host);
        intent.putExtra("finishstatusmsg",finishStatusMsg);
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
        LogUtil.d(TAG, "Finish transfer broadcast msg sent.");


        Intent intent1 = new Intent(VZTransferConstants.UPDATE_ONE_TO_MANY_PROGRESS);
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent1);

    }


    private void updateStatusMessage(final String statusMsg) {
        //UPDATE SENDING FILE STATUS.

        DataSpeedAnalyzer.setCurrentProgressStatus(statusMsg);
    }


}
