package com.verizon.contenttransfer.p2p.service;


import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MediaTransferService {
    private static final String TAG = MediaTransferService.class.getName();
    private boolean isTimedOutRecorded = false;
    public void sendDuplicateAck(Socket iosClientSocket) throws IOException {
        String ackHeader = VZTransferConstants.VZTRANSFER_DUPLICATE_RECEIVED;
        //Send the ack header to Sender

        OutputStream clientOutputStream = iosClientSocket.getOutputStream();
        BufferedOutputStream socketBOS = new BufferedOutputStream( clientOutputStream );
        LogUtil.d(TAG, "Sending duplicate ack header...="  + ackHeader );
        //We dont need to append CRLF here as we are reading 37 bytes only before processing the file
        socketBOS.write( ackHeader.trim().getBytes() );
        socketBOS.flush();
    }

    public boolean isTimedOutRecorded() {
        return isTimedOutRecorded;
    }

    public void setTimedOutRecorded(boolean timedOutRecorded) {
        isTimedOutRecorded = timedOutRecorded;
    }
    public boolean transferMedia(CTAnalyticUtil ctAnalyticUtil, Socket iosClientSocket, String media, String mediaFileName, String mediaSize ) throws IOException {
        InputStream mediaStream = null;
        File mediaFile = null;

        boolean isTransferSuccess = false;
        try {
            boolean readingFailed = false;
            String hostIp = iosClientSocket.getInetAddress().getHostAddress();
            String requestHeader = null;
            try{
                mediaFile = new File ( mediaFileName );
                if ( mediaFile.exists() ) {
                    LogUtil.d(TAG, "Media :"+media+" - Reading file ..." + mediaFile.getAbsolutePath() );
                    mediaStream = new FileInputStream( mediaFile );

                }
            }catch (Exception e){
                readingFailed  = true;
                LogUtil.e(TAG,"Exception on fetching file :"+e.getMessage());
                e.printStackTrace();
            }

            if ( mediaFile != null && mediaFile.exists() ) {

                mediaSize = createTenDigitMediaSize(mediaFile.length() );
                LogUtil.d(TAG, "Translated Media Size : " + mediaSize );


                //Send the header to Sender
                OutputStream clientOutputStream = iosClientSocket.getOutputStream();
                BufferedOutputStream socketBOS = new BufferedOutputStream( clientOutputStream );
                LogUtil.d(TAG, "Media Type : " + media);
                if(readingFailed){
                    requestHeader = VZTransferConstants.VZ_CONTENTTRANSFER_MEDIA_ERROR + mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.PHOTOS ) ) {
                    requestHeader = VZTransferConstants.PHOTO_TRANSFER_REQUEST_HEADER + mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.VIDEOS ) ) {
                    requestHeader = VZTransferConstants.VIDEO_TRANSFER_REQUEST_HEADER+mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.APPS ) ) {
                    requestHeader = VZTransferConstants.APP_TRANSFER_REQUEST_HEADER + mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.MUSICS ) ) {
                    requestHeader = VZTransferConstants.MUSIC_TRANSFER_REQUEST_HEADER+mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.DOCUMENTS ) ) {
                    requestHeader = VZTransferConstants.DOCUMENT_TRANSFER_REQUEST_HEADER+mediaSize;
                } else if ( media.equalsIgnoreCase( VZTransferConstants.CALENDAR ) ) {
                    requestHeader = VZTransferConstants.CALENDAR_TRANSFER_REQUEST_HEADER+mediaSize;
                }

                if(requestHeader == null) {
                    LogUtil.e(TAG, "UNKNOWN Media type is being requested, quitting....");
                }else {
                    LogUtil.d(TAG, "Sending transfer header...="  + requestHeader );
                    //We dont need to append CRLF here as we are reading 37 bytes only before processing the file
                    socketBOS.write( requestHeader.trim().getBytes() );
                    socketBOS.flush();
                    int readSize = 0;
                    long mediaSizeLong = Long.parseLong(mediaSize);
                    if ( mediaStream != null &&  !readingFailed) {
                        byte[] buffer;
                        if(isTimedOutRecorded()){
                            buffer = new byte[ VZTransferConstants.BUFFER_SIZE_8K ];
                        }else {
                            buffer = new byte[ VZTransferConstants.BUFFER_SIZE_64K ];
                        }

                        int len = 0;
                        try {
                                while ((len = mediaStream.read(buffer)) != -1) {
                                    socketBOS.write(buffer, 0, len);
                                    socketBOS.flush();

                                    Utils.updateTransferredBytes(ctAnalyticUtil, len);

                                    readSize += len;
                                    //LogUtil.d(TAG, "*** transferring :" + readSize + " of " + mediaSizeLong+" bufferSize: "+buffer.length);
                                    //"Moto" to "note 3" connection time out solved with this delay...still need to find a better way to handle.
                                    if(isTimedOutRecorded()) {
                                        Thread.sleep(5);
                                    }
                                }
                            LogUtil.d(TAG, mediaFile.getName()+" ********** bytes transfer complete**********");
                            isTransferSuccess = true;
                        }catch (Exception e){
                            readingFailed = true;
                            LogUtil.d(TAG,"caught exception :"+e.getMessage()+", isTimedOutRecorded() "+isTimedOutRecorded());
                            if(e.getMessage().contains(VZTransferConstants.CONNECTION_TIMED_OUT)){
                                Utils.setConnectionTimedOutRecorded();
                            }else {
                                try {
                                    requestHeader = VZTransferConstants.VZ_CONTENTTRANSFER_MEDIA_ERROR + createTenDigitMediaSize(mediaSizeLong - readSize);
                                    socketBOS.write(requestHeader.trim().getBytes());
                                    socketBOS.flush();
                                }catch (Exception e1){
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    if(readingFailed){
                        LogUtil.d(TAG, media + " type File transfer error");
                        //DataSpeedAnalyzer.setDuplicateFile(true);
                        Utils.updateTransferredBytes(ctAnalyticUtil, mediaSizeLong);
                        Utils.updateTransferredFailedBytes(ctAnalyticUtil, mediaSizeLong);
                        Utils.addFailedMediaCount(ctAnalyticUtil, media);
                    }
                }
            } else { // end of file check
                LogUtil.e(TAG, mediaFileName + " to be transferred is not found");
            }
        }
        finally {
            //Close the streams if they are open

            if ( mediaStream != null ) {
                try {
                    mediaStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isTransferSuccess;
    }


    private String createTenDigitMediaSize(long fileLength) {
        String mediaSize = "0";
        mediaSize = Long.toString( fileLength );
        int numSize = mediaSize.length();
        LogUtil.d(TAG, "Media Size : " + mediaSize  + " ---- numSize: " + numSize );

        if ( numSize < 10 ) {
            for( int i = 0; i < (10 - numSize); i++ ) {
                mediaSize = "0"+ mediaSize;
            }
        }
        return mediaSize;
    }
}
