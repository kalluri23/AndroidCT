package com.verizon.contenttransfer.p2p.service;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileNameGenerator;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.TransferSummaryStatus;
import com.verizon.contenttransfer.utils.Utils;

import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaFetchingService {

    //
    private static final String TAG = MediaFetchingService.class.getName();
    public static boolean isP2PFinishActivityLaunched = false;
    public static long datadownloadedsize=0;
    public static long duplicatedatasize=0;
    public static int DISP_PHOTO_COUNT = 0;
    public static int DISP_APP_COUNT = 0;
    public static int DISP_PASSWORDS_COUNT = 0;
    public static int DISP_VIDEO_COUNT = 0;
    public static int DISP_MUSIC_COUNT=0;
    public static int DISP_CALENDAR_COUNT = 0;
    public static int DISP_DOCUMENT_COUNT = 0;
    public static boolean isContactReceived = false;
    public static boolean isCalllogsReceived = false;
    List<Long> list = new ArrayList<Long>();
    private final String transferErrorMsg = VZTransferConstants.TRANSFER_SUMMARY_ERROR_FILE;
    private final String duplicateTransferMsg = VZTransferConstants.TRANSFER_SUMMARY_DUPLICATE_FILE;
    public static int WIFISETTING_COUNT=0;


    public void fetchMedia(Socket iosServerSocket, String media, JSONObject jsonObject, String mediaSize, boolean isDuplicate ) throws IOException {
        String clientIp = iosServerSocket.getInetAddress().getHostAddress();
        String encodedDuplicatedString = "";

        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(clientIp);
        String mediaFileName = null;
        try {
            mediaFileName = (String) jsonObject .get("Path");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String decodedMediaFilePath = "";

        String requestHeader ="";

        File tmp_file = null;
        String CURRENT_PROGRESS_STATUS = "";

        if(isDuplicate){
            encodedDuplicatedString = Utils.getEncodedString(VZTransferConstants.DUPLICATE+mediaSize);
        }
        //Send the header to Sender
        OutputStream clientOutputStream = iosServerSocket.getOutputStream();

        if ( media.equalsIgnoreCase(VZTransferConstants.PHOTOS) ) {

            LogUtil.d(TAG,"*****************FATCHING PHOTO ******************* isDuplicate="+isDuplicate);
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.PHOTOS_STR);
            if(isDuplicate){
                mediaFileName = encodedDuplicatedString;
            }
            decodedMediaFilePath = Utils.getDecodedString(mediaFileName);

            requestHeader = VZTransferConstants.PHOTO_REQUEST_HEADER+mediaFileName;

            CURRENT_PROGRESS_STATUS = ++DISP_PHOTO_COUNT + " / " + ReceiveMetadata.TOTAL_PHOTO_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_PHOTO_COUNT);
        } else if ( media.equalsIgnoreCase( VZTransferConstants.VIDEOS) ) {
            LogUtil.d(TAG,"*****************FATCHING VIDEO ******************* isDuplicate="+isDuplicate);
            LogUtil.d(TAG,"VIDEO MEDIA FILENAME"+mediaFileName);
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.VIDEOS_STR);
            if(isDuplicate){
                mediaFileName = encodedDuplicatedString;
            }
            decodedMediaFilePath = Utils.getDecodedString(mediaFileName);
            requestHeader = VZTransferConstants.VIDEO_REQUEST_HEADER+mediaFileName;
            CURRENT_PROGRESS_STATUS = ++DISP_VIDEO_COUNT + " / " + ReceiveMetadata.TOTAL_VIDEO_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_VIDEO_COUNT);
        }
        else if ( media.equalsIgnoreCase( VZTransferConstants.MUSICS) ) {
            LogUtil.d(TAG,"*****************FATCHING MUSIC ******************* isDuplicate="+isDuplicate );
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.AUDIO_STR);
            if(isDuplicate){

                mediaFileName = encodedDuplicatedString;
            }
            decodedMediaFilePath = Utils.getDecodedString(mediaFileName);
            requestHeader = VZTransferConstants.MUSIC_REQUEST_HEADER+mediaFileName;

            CURRENT_PROGRESS_STATUS = ++DISP_MUSIC_COUNT + " / " + ReceiveMetadata.TOTAL_AUDIO_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_MUSIC_COUNT);
        } else if ( media.equalsIgnoreCase( VZTransferConstants.CALENDAR) ) {
            LogUtil.d(TAG,"*****************FATCHING CALENDAER *******************isDuplicate="+isDuplicate);
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.CALENDAR_STR);
            LogUtil.d(TAG, "calendar file name == "+mediaFileName);
            if(CTGlobal.getInstance().isCross()){
                try{
                    mediaFileName = mediaFileName.substring(mediaFileName.lastIndexOf("/") + 1);
                }catch (Exception e){
                    LogUtil.d(TAG,"Exception on parsing calendar file name for Cross platform.");
                    e.printStackTrace();
                }
                LogUtil.d(TAG, "calendar file name === "+mediaFileName);
            }

            requestHeader = VZTransferConstants.CALENDAR_REQUEST_HEADER+mediaFileName;
            decodedMediaFilePath = mediaFileName;

            CURRENT_PROGRESS_STATUS = ++DISP_CALENDAR_COUNT + " / " + ReceiveMetadata.TOTAL_CALENDAR_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_CALENDAR_COUNT);
        } else if ( media.equalsIgnoreCase( VZTransferConstants.APPS) ) {
            LogUtil.d(TAG,"*****************FETCHING APP *******************");
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.APPS_STR);

            requestHeader = VZTransferConstants.APPS_REQUEST_HEADER+mediaFileName;
            decodedMediaFilePath = Environment.getExternalStorageDirectory() + "\\apps\\";

            CURRENT_PROGRESS_STATUS = ++DISP_APP_COUNT + " / " + ReceiveMetadata.TOTAL_APP_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_APP_COUNT);
        }

        else if ( media.equalsIgnoreCase( VZTransferConstants.DOCUMENTS) ) {
            LogUtil.d(TAG,"*****************FETCHING DOCUMENTS ******************* isDuplicate="+isDuplicate );
            decodedMediaFilePath = Utils.getDecodedString(mediaFileName);
            if(isDuplicate){
                mediaFileName = encodedDuplicatedString;
            }
            requestHeader = VZTransferConstants.DOCUMENTS_REQUEST_HEADER+mediaFileName;
            DataSpeedAnalyzer.setCurrentMediaType(VZTransferConstants.DOCUMENTS_STR);
            CURRENT_PROGRESS_STATUS = ++DISP_DOCUMENT_COUNT + " / " + ReceiveMetadata.TOTAL_DOCUMENT_COUNT;
            ctAnalyticUtil.setDescription(VZTransferConstants.CT_ANALYTIC_TRANSFER_MEDIA +media+" - "+DISP_DOCUMENT_COUNT);
        }
        else {
            LogUtil.e(TAG, "UNKNOWN Media type is being requested, quitting....");
            CURRENT_PROGRESS_STATUS = "";
            return;
        }

        clientOutputStream.write(requestHeader.getBytes());

        //Add CRLF for android
        clientOutputStream.write(VZTransferConstants.CRLF.getBytes());

        clientOutputStream.flush();
        DataSpeedAnalyzer.setDuplicateFile(isDuplicate);
        LogUtil.d(TAG, "Header Sent : " + requestHeader);
        DataSpeedAnalyzer.setCurrentFileName(Utils.getFileName(decodedMediaFilePath));

        DataSpeedAnalyzer.setCurrentProgressStatus(CURRENT_PROGRESS_STATUS);
        if(!isDuplicate){
            InputStream socketStream = iosServerSocket.getInputStream();
            BufferedInputStream bis = null;
            bis = new BufferedInputStream(  socketStream );
            LogUtil.d(TAG, "Created input stream to read data..");

            FileOutputStream fileOutputStream = null; // Stream to write to destination

            File transferFolder = new File( VZTransferConstants.VZTRANSFER_DIR );
            if( ! transferFolder.exists() ) {
                transferFolder.mkdirs();
            }
            long fileSize = 0;
            long readSize = 0;
            String receivedHeaderFor = "";
            try {

                //LogUtil.d(TAG, "Waiting to read data... Number of bytes available to read : " + bis.available() );

                byte[] headerArray = new byte[37];

                socketStream.read(headerArray);
                String headerStr = new String(ByteBuffer.wrap(headerArray).array());
                LogUtil.d(TAG, "Header : " + headerStr);
                if(Utils.isAlphaNumeric(headerStr)){
                    LogUtil.d(TAG, "Received valid header..");
                }else {
                    throw new SocketException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Received invalid header");
                }

                String mediaType = "";
                try{
                    mediaType = headerStr.substring(17, 22);
                    LogUtil.d(TAG, "Media Type : " + mediaType );

                    receivedHeaderFor = headerStr.substring(22, 27);// NOTE: if receivedHeaderFor is 'ERROR' then it is considered as reading file error at sender side.
                    LogUtil.d(TAG, "receivedHeaderFor : " + receivedHeaderFor );

                    String size = headerStr.substring(27, 37);
                    fileSize = Long.parseLong( size );
                    LogUtil.d(TAG, "Size to be received : " + fileSize );

                }catch (Exception e){
                    LogUtil.e(TAG, "Exception on parsing size : " + e.getMessage() );
                    e.printStackTrace();
                }

                LogUtil.d(TAG, "Received valid header..processing file");

                if(!CTGlobal.getInstance().getExitApp()){
                    if(receivedHeaderFor.equals(VZTransferConstants.ERROR)){

                        LogUtil.d(TAG, "Media fetching error.. mediaSize=" + mediaSize);
                        //DataSpeedAnalyzer.setDuplicateFile(true);
                        Utils.updateTransferredFailedBytes(ctAnalyticUtil, Long.parseLong(mediaSize));
                        processNonTransferredFile(media, jsonObject, mediaSize, ctAnalyticUtil, transferErrorMsg);
                        Utils.addFailedMediaCount(ctAnalyticUtil, media);
                    }else{
                        //byte[] bufferSize = new byte[ VZTransferConstants.BUFFER_SIZE_64K ];
                        byte[] bufferSize;
                        if(ReceiveMetadata.isConnectionTimedOutRecorded) {
                            bufferSize = new byte[VZTransferConstants.BUFFER_SIZE_8K];
                        }else {
                            bufferSize = new byte[VZTransferConstants.BUFFER_SIZE_64K];
                        }
                        int dataRead = 0;						// How many bytes in buffer

                        DataSpeedAnalyzer.updateProgressMessage(VZTransferConstants.RECEIVING_FILE_MSG
                                + Utils.initCapitalize(media));


                        tmp_file = MediaFileNameGenerator.generateFileName(media, jsonObject);
                        LogUtil.d(TAG, "getAbsolutePath of File to be processed : " + tmp_file.getAbsolutePath());
                        try {
                            fileOutputStream = new FileOutputStream(tmp_file); // Create output stream
                        }catch (FileNotFoundException fe) {
                            CTGlobal.getInstance().setDbErrorMessage("RECEIVING "+media+"> fetchMedia-"+fe.getMessage());
                            throw new FileNotFoundException(VZTransferConstants.CT_CUSTOM_EXCEPTION+fe.getMessage());
                        }
                        String receivedString = "";
                        while ( (dataRead = socketStream.read( bufferSize )) != -1 ) {
                            byte[] tempBuffer = new byte[dataRead];
                            tempBuffer = Arrays.copyOf(bufferSize, dataRead);

                            receivedString = new String(ByteBuffer.wrap(tempBuffer).array());
                            if(receivedString.indexOf(VZTransferConstants.VZ_CONTENTTRANSFER_MEDIA_ERROR)>=0){
                                String brokenFileSizeStr = receivedString.substring(receivedString.indexOf(VZTransferConstants.VZ_CONTENTTRANSFER_MEDIA_ERROR)
                                        +VZTransferConstants.VZ_CONTENTTRANSFER_MEDIA_ERROR.length());
                                Utils.updateTransferredFailedBytes(ctAnalyticUtil, Long.parseLong( brokenFileSizeStr ));
                                processNonTransferredFile(media, jsonObject, brokenFileSizeStr, ctAnalyticUtil, transferErrorMsg);
                                Arrays.fill( bufferSize, (byte) 0);
                                break;
                            }else {
                                fileOutputStream.write(tempBuffer);
                                fileOutputStream.flush();
                                readSize += dataRead;

                                datadownloadedsize = datadownloadedsize + dataRead;
                                Utils.updateTransferredBytes(ctAnalyticUtil, dataRead);

                                if (CTGlobal.getInstance().isCross()) {
                                    if ((readSize - 1024) > 0
                                            && (readSize - 1024) % (ReceiveMetadata.videoPacketSize * 1024 * 1024) == 0
                                            && fileSize != readSize) {
                                        // Shwarup -  (readSize - 1024) is used to handle 1KB extra data receiving from IOS. - Debugged with Prakash
                                        iosServerSocket.getOutputStream().write(VZTransferConstants.VZCONTENTTRANSFER_REQUEST_FOR_VPART.getBytes());
                                        iosServerSocket.getOutputStream().write(VZTransferConstants.CRLF.getBytes());
                                        iosServerSocket.getOutputStream().flush();
                                    }
                                }
                                if (fileSize == readSize) {
                                    LogUtil.d(TAG, tmp_file.getName() + " ********** transfer complete**********");
                                    Utils.addTransferredMediaCount(media, ctAnalyticUtil, isDuplicate);
                                    break;
                                }
                                Arrays.fill(bufferSize, (byte) 0);
                            }
                            //LogUtil.d(TAG, "*** receiving :" + readSize + " of " + fileSize+" bufferSize: "+tempBuffer.length);
                        }
                    }
                }else {
                    LogUtil.d(TAG,"P2PServerIos.activity is null");
                }
            }catch (NumberFormatException nf){
                nf.getStackTrace();
                LogUtil.d(TAG,"Header may not ready at this time...");
            }
            // Always close the streams, even if exceptions were thrown
            finally {
                if ( fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                        LogUtil.d(TAG, "file name-- "+ mediaFileName +" -- is closed...");
                    } catch (IOException e) {
                        LogUtil.e(TAG, "Exception closing photo file list");
                        e.printStackTrace();
                    }
                    if (null != tmp_file && tmp_file.exists() && fileSize != readSize ) {
                        LogUtil.e(TAG,"delete broken file :"+tmp_file.getAbsolutePath());
                        tmp_file.delete();
                    }
                    //scan the file to add it to media data base
                    if(null != tmp_file && tmp_file.exists()) {

                        Uri uri = Uri.fromFile(tmp_file);
                        Intent sintent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        CTReceiverModel.getInstance().getCtxt().sendBroadcast(sintent);
                    }
                }
            }
            LogUtil.d(TAG, "NUmber of bytes available to read : " + bis.available() );
        }else{
            if(!CTGlobal.getInstance().getExitApp()) {

                Utils.addTransferredMediaCount(media, ctAnalyticUtil,isDuplicate);
                Utils.updateTransferredDuplicateBytes(ctAnalyticUtil, Long.parseLong(mediaSize));
                processNonTransferredFile(media, jsonObject, mediaSize, ctAnalyticUtil, duplicateTransferMsg);
                waitForDuplicateFileAcknowledgement(iosServerSocket);
            }
        }
        if(ctAnalyticUtil.isVztransferCancelled()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    private void processNonTransferredFile(String media, JSONObject jsonObject, String mediaSize, CTAnalyticUtil ctAnalyticUtil, String message) throws IOException {
        Utils.updateTransferredBytes(ctAnalyticUtil, Long.parseLong(mediaSize));

        DataSpeedAnalyzer.updateProgressMessage(VZTransferConstants.PROCESSING_DUPLICATE
                + Utils.initCapitalize(media));
        File tmp_file = MediaFileNameGenerator.generateFileName(media, jsonObject);
        String path = tmp_file.getAbsolutePath();
        TransferSummaryStatus transferSummaryStatus = new TransferSummaryStatus(path, message, media);
        Utils.addNonTransferredFileStatus(ctAnalyticUtil, media, transferSummaryStatus);
    }

    private void waitForDuplicateFileAcknowledgement(Socket iosServerSocket) throws IOException {
        // Handle Duplicate file acknowledgement.
        InputStream socketStream = iosServerSocket.getInputStream();
        byte[] duplicateAckArray = new byte[29];
        LogUtil.d(TAG, "Waiting for duplicate file acknowledgement");
        socketStream.read(duplicateAckArray);
        String duplicateAckStr = new String(ByteBuffer.wrap(duplicateAckArray).array());
        LogUtil.d(TAG, "duplicateAckStr received : " + duplicateAckStr);
        if(!Utils.isAlphaNumeric(duplicateAckStr)){
            LogUtil.e(TAG, "Received invalid duplicateAckStr..");
            throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Received invalid duplicateAckStr");
        }
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(CTReceiverModel.getInstance().getCtxt(),
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        LogUtil.i("TAG", "Finished scanning " + path);
                    }
                });
    }


    public static void resetReceivingVariables() {

        DISP_PHOTO_COUNT = 0;
        DISP_APP_COUNT=0;
        DISP_PASSWORDS_COUNT=0;
        DISP_VIDEO_COUNT = 0;
        DISP_MUSIC_COUNT = 0;
        DISP_CALENDAR_COUNT = 0;
        DISP_DOCUMENT_COUNT = 0;

        WIFISETTING_COUNT=0;
    }

    public static void resetVariables() {
        datadownloadedsize = 0;
    }
}
