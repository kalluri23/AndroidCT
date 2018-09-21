package com.verizon.contenttransfer.p2p.service;

import android.content.Intent;
import android.os.AsyncTask;

import com.verizon.contenttransfer.activity.CTReceiverActivity;
import com.verizon.contenttransfer.activity.CTSavingMediaActivity;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.p2p.receiver.AppReceiver;
import com.verizon.contenttransfer.p2p.receiver.CalendarReceiver;
import com.verizon.contenttransfer.p2p.receiver.CallLogReceiver;
import com.verizon.contenttransfer.p2p.receiver.ContactsReceiver;
import com.verizon.contenttransfer.p2p.receiver.DocumentReceiver;
import com.verizon.contenttransfer.p2p.receiver.MusicReceiver;
import com.verizon.contenttransfer.p2p.receiver.PhotoReceiver;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.p2p.receiver.SMSReceiver;
import com.verizon.contenttransfer.p2p.receiver.VideoReceiver;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.ContentTransferAnalyticsMap;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileNameGenerator;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifidirect.DeviceIterator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

//import android.util.Log;

public class
P2PServerIos extends AsyncTask<Void, String, String> {

    private static final String TAG = P2PServerIos.class.getName();
    private ServerSocket serverSocket = null;
    private Socket iosServerSocket = null;
    private long startTransferTime=0;
    private long endTransferTIme=0;
    private CTAnalyticUtil ctAnalyticUtil;
    private String status="";
    private String host = "";
    private boolean isFinishedPageLaunched = false;
    private boolean isTransferFinished = false;

    public P2PServerIos(String host) {
        this.host = host;
    }

    @Override
    protected String doInBackground(Void... params) {
        String loggingCurrentStatus = "";
        try {
            LogUtil.d(TAG, "Opening server socket -- host ip :"+ host);

            iosServerSocket = SocketUtil.getClientSocket(host);
            ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(host);

            if(!SocketUtil.isClient()){
                serverSocket = SocketUtil.getServerSocket();
            }

            if(iosServerSocket == null){
                LogUtil.e(TAG,"Socket is null.");
                throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Socket is null");
            }

            if(ctAnalyticUtil == null){
                LogUtil.e(TAG,"CTAnalyticUtil is not found.");
                throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"CTAnalyticUtil is null");
            }
            loggingCurrentStatus = "Socket connection success";
            iosServerSocket.setTcpNoDelay(true);

            CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_CONNECTION_ESTABLISHED);
            boolean isMetadataReceivedSuccessfully = ReceiveMetadata.receiveMetadata(iosServerSocket);
            if(ReceiveMetadata.isInsufficientstoragespace){
                return "";
            }
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_METADATA_RECEIVED + (isMetadataReceivedSuccessfully?":T":":F"));

            if(!isMetadataReceivedSuccessfully){
                //Invalid Header received... close socket connection.
                LogUtil.d(TAG,"Invalid Header received... throwing socket exception.");
                status = VZTransferConstants.INVALID_HEADER_RECEIVED;
                throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Invalid metadata received");
            }else {
                startTransferTime  = Utils.getTimeInMillis();
                while ( ReceiveMetadata.mediaStateObject == null ) {
                    try {
                        if( ReceiveMetadata.IS_HOME_PAGE){
                            //isDataTransferInterrupted = true;
                            status = VZTransferConstants.WAITING_FOR_MEDIA_STATE_UPDATE;
                            break;
                        }
                        LogUtil.d(TAG, "Waiting for Media state update..DeviceIterator.goToMVMHome="+DeviceIterator.goToMVMHome);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LogUtil.d(TAG,"InterruptedException");
                        e.printStackTrace();
                    }
                }
                // Fatching order -  video, Music, Photo, Contacts, SMS, CallLogs, WiFiSetting.
                if( ReceiveMetadata.IS_HOME_PAGE ){
                    status = VZTransferConstants.MVM_BACK_TO_EXIT;
                    return status;
                }

                LogUtil.d(TAG, "Start CTReceiverActivity");
                Intent intent = new Intent( CTGlobal.getInstance().getContentTransferContext(), CTReceiverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
                CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_TRANSFER_STARTED);

                if ( ReceiveMetadata.mediaStateObject.getVideosState().toLowerCase().trim().equalsIgnoreCase("true") ) {
                    loggingCurrentStatus = "RECEIVING VIDEO";
                    LogUtil.d(TAG,"startProcessingVideoList");
                    VideoReceiver.startProcessingVideoList(iosServerSocket);
                }

                if ( Utils.isSupportMusic() && ReceiveMetadata.mediaStateObject.getMusicsState().toLowerCase().trim().equalsIgnoreCase( "true") ) {
                    loggingCurrentStatus = "RECEIVING MUSIC";
                    LogUtil.d(TAG,"startProcessingMusicList");
                    MusicReceiver.startProcessingMusicList(iosServerSocket);
                }
                if(VZTransferConstants.SUPPORT_APPS) {
                    LogUtil.d(TAG,"ReceiveMetadata.mediaStateObject.getAppsState().toLowerCase() ="+ReceiveMetadata.mediaStateObject.getAppsState().toLowerCase());
                    if (ReceiveMetadata.mediaStateObject.getAppsState().toLowerCase().trim().equalsIgnoreCase("true")) {
                        loggingCurrentStatus = "RECEIVING APPS";
                        LogUtil.d(TAG, "startProcessingAppList");
                        AppReceiver.startProcessingAppsList(iosServerSocket);
                    }
                }
                if ( ReceiveMetadata.mediaStateObject.getPhotosState().toLowerCase().trim().equalsIgnoreCase("true") ) {
                    loggingCurrentStatus = "RECEIVING PHOTO";
                    LogUtil.d(TAG, "startProcessingPhotoList");
                    PhotoReceiver.startProcessingPhotoList(iosServerSocket);
                }

                if(VZTransferConstants.SUPPORT_DOCS) {
                    if (!CTGlobal.getInstance().isCross() && ReceiveMetadata.mediaStateObject.getDocumentsState().toLowerCase().trim().equalsIgnoreCase("true")) {
                        loggingCurrentStatus = "RECEIVING DOCUMENT";
                        DocumentReceiver.startProcessingDocumentList(iosServerSocket);
                    }
                }
                if (  ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase( "true")&& CTGlobal.getInstance().isContactsPermitted() ) {
                    LogUtil.d(TAG,"start receiveContacts");
                    loggingCurrentStatus = "RECEIVING CONTACTS";
                    ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA + VZTransferConstants.CONTACTS_STR);
                    ContactsReceiver.receiveContacts(iosServerSocket);
                    MediaFetchingService.isContactReceived = true;
                }

                if ( ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isCalendarPermitted()) {
                    loggingCurrentStatus = "RECEIVING CALENDAR";
                    LogUtil.d(TAG, "startProcessingCalendarList");
                    ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA + VZTransferConstants.CALENDAR_STR);
                    CalendarReceiver.startProcessingCalendarList(iosServerSocket);
                }
                if ( !CTGlobal.getInstance().isCross() && ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase( "true")&& CTGlobal.getInstance().isCalllogsPermitted()) {
                    loggingCurrentStatus = "RECEIVING CALLLOG";
                    ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA + VZTransferConstants.CALLLOG_STR);
                    CallLogReceiver.receiveCalllogs(iosServerSocket);
                    MediaFetchingService.isCalllogsReceived = true;
                }
                if (  !CTGlobal.getInstance().isCross() && ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase( "true") && CTGlobal.getInstance().isSmsPermitted()) {
                    loggingCurrentStatus = "RECEIVING SMS";
                    ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA + VZTransferConstants.SMS_STR);
                    SMSReceiver.receiveSMS(iosServerSocket);
                }

                endTransferTIme = Utils.getTimeInMillis();
                status = VZTransferConstants.SERVICE_EXITED;



                LogUtil.d(TAG, "Content Transfer Complete...");
                //This code is required bt IOS to close the sokcet and navigate to Finish Screen
                iosServerSocket.getOutputStream().write( "VZCONTENTTRANSFER_FINISHED".getBytes() );
                iosServerSocket.getOutputStream().write(VZTransferConstants.CRLF.getBytes());
                iosServerSocket.getOutputStream().flush();
                ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_FINISHED);
                loggingCurrentStatus = "RECEIVING FINISHED";
                isTransferFinished = true;
                setLogAnalytics();


                status = VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED;
                //iOS required some delay to close the socket.. so, push finish activity before delay.
                publishProgress(status);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketTimeoutException e1){
            LogUtil.e(TAG, "Socket SocketTimeoutException : " + e1.getMessage());
            e1.printStackTrace();
            status = VZTransferConstants.SOCKET_TIMEOUT_EXCEPTION;
            CTGlobal.getInstance().setConnectionFailed(true);
            CTGlobal.getInstance().setDbErrorMessage(loggingCurrentStatus+"> Receiving1-"+e1.getMessage());
        } catch (IOException e1) {
            String msg = e1.getMessage();
            LogUtil.e(TAG, "Socket IOException : " + msg);
            CTGlobal.getInstance().setDbErrorMessage(loggingCurrentStatus+"> Receiving2-"+msg);
            e1.printStackTrace();
            if(null == msg) {
                status = VZTransferConstants.NULL_POINTER_EXCEPTION;
            } else if(msg.contains("EPIPE")){
                status = VZTransferConstants.SOCKET_EXCEPTION_BROKEN_PIPE_STATUS;
            }
        }
        catch (NullPointerException ne){
            ne.getStackTrace();
            ne.printStackTrace();
            LogUtil.e(TAG, "Null pointer exception :" + ne.getMessage());
            status = VZTransferConstants.NULL_POINTER_EXCEPTION;
        }
        // Always close the streams, even if exceptions were thrown
        finally {
            try {
                LogUtil.d(TAG,"stopping timer...");

                DataSpeedAnalyzer.stopTimer();
                LogUtil.d(TAG,"stopping timer completed...");
            }catch (Exception e){
                LogUtil.d(TAG,"Stop timer exception.."+e.getMessage());
                e.printStackTrace();
            }
        }
        LogUtil.d(TAG, "Data Transfer status :" + status);

        return status;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        DataSpeedAnalyzer.stopTimer();

        if ( DataSpeedAnalyzer.progressDialog != null ) {

            try{
                DataSpeedAnalyzer.progressDialog.cancel();
                dismissProgressDialog();
                LogUtil.d(TAG, "dismissed Progress Dialog");
            }catch(Exception ex)
            {
                LogUtil.d(TAG,ex.getMessage());
            }
        }
        MediaFetchingService.resetVariables();
        if(ReceiveMetadata.IS_HOME_PAGE || ReceiveMetadata.isInsufficientstoragespace) {
            return;
        }

        LogUtil.d(TAG, "P2PServerIos onPostExecute..  status =" + status);

        if ( serverSocket != null ) {
            try {
                LogUtil.d(TAG, "Closing Server socket......");
                serverSocket.close();
                serverSocket = null;
                //LogUtil.d(TAG, " Server socket Closed.... serverSocket="+serverSocket);
            } catch (IOException e) {
                LogUtil.e(TAG, "Failed to close Server Socket IOException :"+e.getMessage());
                e.printStackTrace();
            } catch (Exception ex){
                LogUtil.e(TAG, "Failed to close Server Socket Exception :" + ex.getMessage());
            }
        }

        if(!isFinishedPageLaunched){
            setStatusForFinishActivitity();
        }

        Utils.resetConnectionOnTransferFinish(true);
        LogUtil.d(TAG, "onPostExecute completed....");
    }
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if(!isFinishedPageLaunched) {
            setStatusForFinishActivitity();
        }
        isFinishedPageLaunched = true;
    }
    private void setStatusForFinishActivitity() {
        String finishStatus = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
        LogUtil.d(TAG, "Launching finish - isTransferFinished:"+isTransferFinished);
        if(CTGlobal.getInstance().isVztransferStarted()
                && isTransferFinished){
            LogUtil.d(TAG, "Launching finish - on data transfer success.");
            finishStatus = VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED;
        } else if(ctAnalyticUtil.getDataTransferStatusMsg()
                .equals(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER)){
            finishStatus = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
        }

        if(!CTGlobal.getInstance().isVztransferStarted()
                && !ctAnalyticUtil.isVztransferCancelled()){
            LogUtil.d(TAG,"Send vztransfer cancel from setStatus for finish activity...2");
            Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL,host);
        }


        launchFinishActivity(finishStatus);
//*******************************

/*        if(CTGlobal.getInstance().isConnectionFailed()) {
            LogUtil.d(TAG, "Launching finish - on isConnectionFailed");
            launchFinishActivity(VZTransferConstants.CONNECTION_FAILED);
        }
        else if(!CTGlobal.getInstance().isVztransferStarted()
                || status.equals(VZTransferConstants.NULL_POINTER_EXCEPTION)){

            if(!ctAnalyticUtil.isVztransferCancelled()) {
                LogUtil.d(TAG,"Send vztransfer cancel from setStatus for finish activity...2");
                Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL,host);
            }
            LogUtil.d(TAG, "Launching finish - on meta data receive unsuccessful");
            launchFinishActivity(VZTransferConstants.DATA_TRANSFER_INTERRUPTED);
        } else if(SocketUtil.getCtAnalyticUtil(host).getDataTransferStatusMsg()
                .equals(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER)){
            launchFinishActivity(VZTransferConstants.DATA_TRANSFER_INTERRUPTED);
        } else if(status.equals(VZTransferConstants.SOCKET_TIMEOUT_EXCEPTION)) {
            LogUtil.d(TAG,"Send vztransfer cancel from setStatus for finish activity ...1");
            Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL, host);
            LogUtil.d(TAG, "Launching finish - on meta data receive unsuccessful");
            launchFinishActivity(VZTransferConstants.CONNECTION_FAILED);
        }
        else {
            LogUtil.d(TAG, "Launching finish - on data transfer success.");
            launchFinishActivity(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED);
        }*/
        LogUtil.d(TAG, "setStatusForFinishActivitity....");
    }





    public void launchFinishActivity(String statusMsg) {
        Utils.writeToCommSocketThread(VZTransferConstants.CLOSE_COMM_ON_FINISH_TRANSFER, host);

        if(MediaFetchingService.isP2PFinishActivityLaunched || DeviceIterator.goToMVMHome){
            return;
        }
        LogUtil.d(TAG,"launchFinishActivity - interruptedMsg="+statusMsg);
        MediaFetchingService.isP2PFinishActivityLaunched = true;
        String finishStatusMsg = "";
        if(statusMsg.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
            finishStatusMsg = VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED;
        }else {
            if(ctAnalyticUtil.isVztransferCancelled() && CTGlobal.getInstance().isVztransferStarted()){
                finishStatusMsg = VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER;
            } else {
                finishStatusMsg = VZTransferConstants.DATA_TRANSFER_INTERRUPTED;
            }
        }
        LogUtil.d(TAG, "finishStatusMsg == " + finishStatusMsg);
        P2PFinishModel.getInstance().setSocketExceptionStatus(status);
        ctAnalyticUtil.setDataTransferStatusMsg(finishStatusMsg);
        if (ctAnalyticUtil.getDataTransferStatusMsg().equals(VZTransferConstants.CONNECTION_FAILED)) {
            P2PFinishModel.getInstance().setIsInterrupted(true);
        } else if (ctAnalyticUtil.getDataTransferStatusMsg().contains(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER)
                || ctAnalyticUtil.getDataTransferStatusMsg().equals(VZTransferConstants.DATA_TRANSFER_INTERRUPTED)
                || P2PFinishModel.getInstance().getSocketExceptionStatus() != null && P2PFinishModel.getInstance().getSocketExceptionStatus().equals(VZTransferConstants.SOCKET_EXCEPTION_BROKEN_PIPE_STATUS)) {
            P2PFinishModel.getInstance().setIsInterrupted(true);
        }
        CTGlobal.getInstance().setPairingType(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)?VZTransferConstants.WIFI_DIRECT_CONNECTION:
                CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)?VZTransferConstants.HOTSPOT_WIFI_CONNECTION:VZTransferConstants.PHONE_WIFI_CONNECTION);
        boolean gotoSavingMedia=Utils.isGoToSavingMediaPage();
        if(Utils.isReceiverDevice() && statusMsg.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)&& gotoSavingMedia){
            Intent intent = new Intent( CTGlobal.getInstance().getContentTransferContext(), CTSavingMediaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(VZTransferConstants.MESSAGE_KEY, VZTransferConstants.TRANSFER_FINISH_HEADER);
            intent.putExtra(VZTransferConstants.STATUS_MSG, statusMsg);
            intent.putExtra(VZTransferConstants.FLOW,VZTransferConstants.STANDARD_FLOW);
            intent.putExtra(VZTransferConstants.CONTACTS_COUNT,0);
            intent.putExtra(VZTransferConstants.CONTACTS_STATE,ReceiveMetadata.mediaStateObject.getContactsState());
            intent.putExtra(VZTransferConstants.SMS_STATE,ReceiveMetadata.mediaStateObject.getSmsState());
            intent.putExtra(VZTransferConstants.CALENDAR_STATE,ReceiveMetadata.mediaStateObject.getCalendarState());
            intent.putExtra(VZTransferConstants.CALLLOGS_STATE,ReceiveMetadata.mediaStateObject.getCallLogsState());
            CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
        } else {
            P2PFinishUtil.getInstance().completeTransferFinishProcess(statusMsg);
        }

        ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);

        //local analytics
        P2PFinishUtil.getInstance().generateAppAnalyticsFile(host);
    }

    public static void resetVariables() {
        MediaFetchingService.resetReceivingVariables();
    }

    private static void dismissProgressDialog() {

        try {
            if (DataSpeedAnalyzer.progressDialog != null && DataSpeedAnalyzer.progressDialog.isShowing()) {
                DataSpeedAnalyzer.progressDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
            e.printStackTrace();
        } catch (Exception e) {
            // Handle or log or ignore
            e.printStackTrace();
        } finally {
            DataSpeedAnalyzer.progressDialog = null;
        }

    }
    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG, "P2PServerIos onPreExecute...");
        CTGlobal.getInstance().setConnectionFailed(false);
        status = "";
        isFinishedPageLaunched = false;
        MediaFileNameGenerator.resetVariables();
    }

/*	public static boolean copyFile(InputStream inputStream, OutputStream out) {  // unused method
		byte buf[] = new byte[1024];
		int len;
		long startTime = System.currentTimeMillis();

		try {
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			out.close();
			inputStream.close();
			long endTime = System.currentTimeMillis() - startTime;
			LogUtil.v(TAG, "Time taken to transfer all bytes is : " + endTime);

		} catch (IOException e) {
			LogUtil.d(TAG, e.toString());
			return false;
		}
		return true;
	}*/

/*	public static List getFileList( String mediaType ) { // unused method
		List fileList = new ArrayList();

		if ( mediaType.equalsIgnoreCase( "PHOTOS") ) {
			//What are we doing here?
		} else if ( mediaType.equalsIgnoreCase( "VIDEOS") ) {
			//What are we doing here?
		}

		return fileList;
	}*/

    public void  setLogAnalytics() {
        //log analytics

        HashMap<String, Object> eventMap = new HashMap<String, Object>();

        eventMap.put(ContentTransferAnalyticsMap.CONTACTS, ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true"));
        eventMap.put(ContentTransferAnalyticsMap.PHOTOS, ReceiveMetadata.mediaStateObject.getPhotosState().toLowerCase().trim().equalsIgnoreCase( "true"));
        eventMap.put(ContentTransferAnalyticsMap.VIDEOS, ReceiveMetadata.mediaStateObject.getVideosState().toLowerCase().trim().equalsIgnoreCase( "true"));
        eventMap.put(ContentTransferAnalyticsMap.APPS, ReceiveMetadata.mediaStateObject.getAppsState().toLowerCase().trim().equalsIgnoreCase("true"));
        eventMap.put(ContentTransferAnalyticsMap.CONTACT_COUNT, ctAnalyticUtil.getContactsCount());
        eventMap.put(ContentTransferAnalyticsMap.APP_COUNT, ctAnalyticUtil.getAppCount());
        eventMap.put(ContentTransferAnalyticsMap.PHOTO_COUNT,ctAnalyticUtil.getPhotoCount());
        eventMap.put(ContentTransferAnalyticsMap.DUPLICATE_PHOTO_COUNT, ctAnalyticUtil.getDuplicatePhotoCount());
        eventMap.put(ContentTransferAnalyticsMap.VIDEO_COUNT, ctAnalyticUtil.getVideoCount());
        eventMap.put(ContentTransferAnalyticsMap.DUPLICATE_VIDEO_COUNT, ctAnalyticUtil.getDuplicateVideoCount());
        eventMap.put(ContentTransferAnalyticsMap.CALENDAR,ReceiveMetadata.mediaStateObject.getCalendarState() .toLowerCase().trim().equalsIgnoreCase( "true"));
        eventMap.put(ContentTransferAnalyticsMap.CALENDAR_COUNT,ctAnalyticUtil.getCalendarCount());



        if(!CTGlobal.getInstance().isCross()){

            eventMap.put(ContentTransferAnalyticsMap.MUSIC,ReceiveMetadata.mediaStateObject.getMusicsState().toLowerCase().trim().equalsIgnoreCase( "true"));
            eventMap.put(ContentTransferAnalyticsMap.SMS,ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase( "true"));
            eventMap.put(ContentTransferAnalyticsMap.CALLLOGS,ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase( "true"));
            eventMap.put(ContentTransferAnalyticsMap.DOCUMENTS,ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase( "true"));
            eventMap.put(ContentTransferAnalyticsMap.MUSIC_COUNT,ctAnalyticUtil.getMusicCount());
            eventMap.put(ContentTransferAnalyticsMap.DUPLICATE_MUSIC_COUNT, ctAnalyticUtil.getDuplicateMusicCount());
            eventMap.put(ContentTransferAnalyticsMap.SMS_COUNT,ctAnalyticUtil.getSmsCount());
            eventMap.put(ContentTransferAnalyticsMap.CALLLOG_COUNT, ctAnalyticUtil.getCallLogCount());
            eventMap.put(ContentTransferAnalyticsMap.CALLLOG_COUNT, ReceiveMetadata.TOTAL_DOCUMENT_COUNT);

        }

        eventMap.put(ContentTransferAnalyticsMap.TRANSFER_START_TIME,startTransferTime);
        eventMap.put(ContentTransferAnalyticsMap.TRANSFER_END_TIME,endTransferTIme);

        //LogAnalytics.getInstance().logEvent(IOSReceiverActivity.receiveStatusTV, eventMap, "ReceiveDataScreen", LogAnalytics.EventType.CLICK, VZTransferConstants.APP_NAME, false);
        //Utils.logEvent(CTReceiverActivity.receiveStatusTV, eventMap, "ReceiveDataScreen");
    }
}
