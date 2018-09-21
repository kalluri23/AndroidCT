package com.verizon.contenttransfer.p2p.receiver;

import android.content.Intent;

import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.MediaTransferStateVO;
import com.verizon.contenttransfer.p2p.model.WifiSettingVO;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.UtilsFromApacheLib.CTFileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

//import android.util.Log;

public class ReceiveMetadata {
    

    private static final String TAG = ReceiveMetadata.class.getName();

    private static String metadataReceived = "";
    private static int NUM_ATTEMPTS = 3;
    private static long mediaSize = 0;
    public static boolean isInsufficientstoragespace = false;
    public static ArrayList<String> photosList = new ArrayList<String>();
    public static ArrayList<String> videosList = new ArrayList<String>();
    public static ArrayList<String> calendarList = new ArrayList<String>();
    public static ArrayList<String> musicsList = new ArrayList<String>();
    public static ArrayList<WifiSettingVO> wifiSettingList = new ArrayList<WifiSettingVO>();
    public static ArrayList<String> documentsList = new ArrayList<String>();
    public static ArrayList<String> appsList = new ArrayList<String>();

    public static int TOTAL_PHOTO_COUNT = 0;
    public static int TOTAL_VIDEO_COUNT = 0;
    public static int TOTAL_AUDIO_COUNT = 0;
    public static int TOTAL_CALENDAR_COUNT = 0;
    public static int TOTAL_WIFISETTING_COUNT = 0;
    public static int TOTAL_DOCUMENT_COUNT = 0;
    public static int TOTAL_PASSWORDS_COUNT = 0;
    public static int TOTAL_APP_COUNT = 0;

    public static MediaTransferStateVO mediaStateObject = null;
    public static boolean IS_HOME_PAGE = false;
    public static long getMediaSize() {
        return mediaSize;
    }
    public static int videoPacketSize=0;
    public static boolean isConnectionTimedOutRecorded = false;
    public static void resetValues(){
        mediaSize=0;
        NUM_ATTEMPTS = 0;
        isConnectionTimedOutRecorded = false;
    }
    public static void resetVariables(){
        photosList = new ArrayList<String>();
        videosList = new ArrayList<String>();
        musicsList = new ArrayList<String>();
        appsList = new ArrayList<String>();
        calendarList = new ArrayList<String>();
        wifiSettingList = new ArrayList<WifiSettingVO>();
        documentsList = new ArrayList<String>();
        mediaStateObject = null;
    }
    public static boolean receiveMetadata(Socket iosServerSocket) {
        boolean isALLFLFlag = false;
        resetValues();
        String clientIp = iosServerSocket.getInetAddress().getHostAddress();
        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(clientIp);
        while(!isALLFLFlag &&  NUM_ATTEMPTS++ < 3 ) {
            FileOutputStream fileOutputStream = null; // Stream to write to destination
            try {
                LogUtil.d(TAG, "NUM_ATTEMPTS =" + NUM_ATTEMPTS);
                LogUtil.d(TAG, "P2PServerIos.iosServerSocket = " + iosServerSocket);
                InputStream socketStream = iosServerSocket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(socketStream);
                byte[] headerArray = new byte[37];
                LogUtil.d(TAG, "Waiting to Read from socket....");
                while (socketStream.available()<=0 && !CTGlobal.getInstance().isConnectionFailed()){
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(!CTGlobal.getInstance().isConnectionFailed()) {
                    socketStream.read(headerArray);
                }
                LogUtil.d(TAG, "headerArray...." + headerArray);
                if (headerArray == null || headerArray.length == 0) {

                    LogUtil.e(TAG, "Header is null");
                    return false;
                }
                CTGlobal.getInstance().setVztransferStarted(true);
                LogUtil.d(TAG, "headerArray.length..." + headerArray.length);
                String headerStr = new String(ByteBuffer.wrap(headerArray).array());

                if(Utils.isAlphaNumeric(headerStr)){
                    LogUtil.d(TAG, "Received valid header..");

                }else {
                    LogUtil.e(TAG, "Received invalid header..");
                    return false;
                }
                LogUtil.d(TAG, "Header : " + headerStr);

                String mediaType = headerStr.substring(17, 22);
                LogUtil.d(TAG, "Media Type : " + mediaType);

                String FILE_NAME = VZTransferConstants.CLIENT_PAYLOAD;        //"contacts";

                File transferFolder = new File(VZTransferConstants.VZTRANSFER_DIR);
                transferFolder.mkdirs();

                File tmp_file = new File(VZTransferConstants.VZTRANSFER_DIR, FILE_NAME);

                fileOutputStream = new FileOutputStream(tmp_file); // Create output stream

                //ALLFL	- 	"All File Log" Dont change this string
                if (mediaType.equalsIgnoreCase("ALLFL")) {
                    isALLFLFlag = true;
                    String size = headerStr.substring(27, 37);
                    int imageSize = Integer.parseInt(size);
                    LogUtil.d(TAG, "Size to be received : " + imageSize);

                    //byte[] imageAr = new byte[ 2048 ];		// To hold file contents
                    byte[] imageAr = new byte[VZTransferConstants.BUFFER_SIZE_16K];    // To hold file contents

                    int dataRead = 0;                        // How many bytes in buffer
                    int readSize = 0;

                    if (imageSize > 0) {
                        LogUtil.d(TAG, "Reading Stream....");
                        while ((dataRead = socketStream.read(imageAr)) != -1) {
                            LogUtil.d(TAG, "Copying Stream....");
                            //There is a long delay when using IOUtils
                            LogUtil.d(TAG,"dataRead ="+dataRead);
                            LogUtil.d(TAG,"imageAr length ="+imageAr.length);
                            fileOutputStream.write(imageAr, 0, dataRead);
                            fileOutputStream.flush();

                            readSize += dataRead;
                            LogUtil.d(TAG, "Reading ..... --dataRead: " + dataRead + "  --readSize: " + readSize);

                            if (imageSize == readSize) {
                                LogUtil.d(TAG, "Last Data Receved ");
                                break;
                            }
                        }

                        LogUtil.d(TAG, "Reading complete...");
                    } else {
                        LogUtil.d(TAG, "There are no Data to be read.");
                    }

                    metadataReceived = CTFileUtils.readFileToString(new File(VZTransferConstants.VZTRANSFER_DIR, FILE_NAME), "UTF-8");

                    metadataReceived = metadataReceived.trim();
                    LogUtil.d(TAG, "Received metadata json : " + metadataReceived);

                    JSONParser parser = new JSONParser();

                    JSONObject metaData = (JSONObject) parser.parse(metadataReceived);
                    Long deviceCount = (Long) metaData.get("deviceCount");

                    if(deviceCount!=null) {
                        //This is for One-to-Many to send the devices connected.
                        CTGlobal.getInstance().setDeviceCount(deviceCount);
                    }

                    JSONObject mediaState = (JSONObject) metaData.get("itemList");
                    LogUtil.d(TAG, "itemList Json : " + mediaState.toJSONString());
                    mediaStateObject = new MediaTransferStateVO(mediaState);

                    //Calculate Photos File Size
                    if (mediaStateObject.getPhotosState().equalsIgnoreCase("true")) {
                        JSONArray photosList = (JSONArray) metaData.get("photoFileList");
                        createMediaListFile("photos", VZTransferConstants.CLIENT_PHOTOS_LIST_FILE, photosList);
                        TOTAL_PHOTO_COUNT = Integer.parseInt(mediaStateObject.getPhotosCount());
                        mediaSize += calculateSize(VZTransferConstants.PHOTOS, photosList);
                    }
                    if(VZTransferConstants.SUPPORT_APPS) {
                        //Calculate Apps File Size
                        if (mediaStateObject.getAppsState().equalsIgnoreCase("true")) {
                            JSONArray appsList = (JSONArray) metaData.get("appsFileList");
                            createMediaListFile("apps", VZTransferConstants.CLIENT_APPS_LIST_FILE, appsList);
                            TOTAL_APP_COUNT = Integer.parseInt(mediaStateObject.getAppsCount());
                            mediaSize += calculateSize(VZTransferConstants.APPS, appsList);
                        }
                    }
                    //Calculate Videos File Size
                    if (mediaStateObject.getVideosState().equalsIgnoreCase("true")) {
                        JSONArray videosList = (JSONArray) metaData.get("videoFileList");
                        createMediaListFile("videos", VZTransferConstants.CLIENT_VIDEOS_LIST_FILE, videosList);
                        TOTAL_VIDEO_COUNT = Integer.parseInt(mediaStateObject.getVideosCount());
                        mediaSize += calculateSize(VZTransferConstants.VIDEOS, videosList);
                    }
                    if(CTGlobal.getInstance().isCross()) {
                        videoPacketSize = 300;
                        try {
                            videoPacketSize = Integer.parseInt(metaData.get("videoPkgSize").toString());
                        }catch(Exception e){
                            videoPacketSize = 300;
                        }
                        LogUtil.d(TAG, "Video Packet Size " + videoPacketSize);

                    }
                    if(!CTGlobal.getInstance().isCross()) {
                        // parsing additional meta data.
                        JSONObject additoanalDataObj = (JSONObject) metaData.get("data");
                        if(additoanalDataObj != null
                                && additoanalDataObj.get(VZTransferConstants.CONNECTION_TIMED_OUT_DURING_TRANSFER) != null) {
                            isConnectionTimedOutRecorded = Boolean.parseBoolean(additoanalDataObj.get(VZTransferConstants.CONNECTION_TIMED_OUT_DURING_TRANSFER).toString());
                        }
                        // End of parsing additional meta data.
                    }

                    //Calculate Calendar File Size
                    if (mediaStateObject.getCalendarState().equalsIgnoreCase("true")) {
                        JSONArray calendarList = (JSONArray) metaData.get("calendarFileList");
                        createMediaListFile("calendar", VZTransferConstants.CLIENT_CALENDAR_FILE, calendarList);
                        TOTAL_CALENDAR_COUNT = Integer.parseInt(mediaStateObject.getCalendarCount());
                        mediaSize += calculateSize(VZTransferConstants.CALENDAR, calendarList);
                    }

                    //Calculate Musics File Size
                    if (Utils.isSupportMusic() && mediaStateObject.getMusicsState().equalsIgnoreCase("true")) {
                        JSONArray musicsList = (JSONArray) metaData.get("musicFileList");
                        createMediaListFile("musics", VZTransferConstants.CLIENT_MUSIC_LIST_FILE, musicsList);
                        TOTAL_AUDIO_COUNT = Integer.parseInt(mediaStateObject.getMusicCount());
                        mediaSize += calculateSize(VZTransferConstants.MUSICS, musicsList);
                    }

                    //Calculate Contact File Size
                    if (mediaStateObject.getContactsState().equalsIgnoreCase("true")) {
                        String contactsCount = mediaStateObject.getContactsCount();
                        ctAnalyticUtil.setContactsCount(Integer.parseInt(contactsCount));
                        LogUtil.d(TAG, "Total Contacts received:" + ctAnalyticUtil.getContactsCount());
                        mediaSize += Long.parseLong(mediaStateObject.getContactsSize());
                        LogUtil.d(TAG, "Long.parseLong(mediaStateObject.getContactsSize() ="+Long.parseLong(mediaStateObject.getContactsSize()));
                    }
                    //Calculate CallLog File Size
                    if (!CTGlobal.getInstance().isCross() && mediaStateObject.getCallLogsState().equalsIgnoreCase("true")) {
                        String calllogsCount = mediaStateObject.getCalllogsCount();
                        ctAnalyticUtil.setCallLogCount(Integer.parseInt(calllogsCount));
                        LogUtil.d(TAG, "Total Calllogs:" + ctAnalyticUtil.getCallLogCount());
                        mediaSize += Long.parseLong(mediaStateObject.getCallLogsSize());
                        LogUtil.d(TAG, "Long.parseLong(mediaStateObject.getCallLogsSize() ="+Long.parseLong(mediaStateObject.getCallLogsSize()));
                    }
                    //Calculate SMS File Size
                    if (!CTGlobal.getInstance().isCross() && mediaStateObject.getSmsState().equalsIgnoreCase("true")) {
                        String smsCount = mediaStateObject.getSmsCount();
                        ctAnalyticUtil.setSmsCount(Integer.parseInt(smsCount));
                        LogUtil.d(TAG, "Total SMS:" + ctAnalyticUtil.getSmsCount());
                        mediaSize += Long.parseLong(mediaStateObject.getSmsSize());
                        LogUtil.d(TAG, "Long.parseLong(mediaStateObject.getSmsSize() ="+Long.parseLong(mediaStateObject.getSmsSize()));
                    }

                    if(VZTransferConstants.SUPPORT_DOCS) {
                        //Calculate Documents File Size
                        if (!CTGlobal.getInstance().isCross() && mediaStateObject.getDocumentsState().equalsIgnoreCase("true")) {
                            JSONArray docsList = (JSONArray) metaData.get("documentFileList");
                            createMediaListFile("documents", VZTransferConstants.CLIENT_DOCUMENTS_FILE, docsList);
                            TOTAL_DOCUMENT_COUNT = Integer.parseInt(mediaStateObject.getDocumentsCount());
                            mediaSize += calculateSize(VZTransferConstants.DOCUMENTS, docsList);
                        }
                    }

                    isInsufficientstoragespace = false;
                    long availableSpace = Utils.bytesAvailable();
                    if(mediaSize > availableSpace)
                    {
                        LogUtil.e(TAG, "Not enough storage space");
                        isInsufficientstoragespace = true;
                        Intent storageIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTErrorMsgActivity.class);
                        storageIntent.putExtra("screen", VZTransferConstants.INSUFFICIENT_STORAGE);
                        storageIntent.putExtra("thingsOfBytes", String.valueOf(Utils.bytesToMeg(mediaSize)));
                        storageIntent.putExtra("availableBytes", String.valueOf(Utils.bytesToMeg(availableSpace)));
                        storageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        CTGlobal.getInstance().getContentTransferContext().startActivity(storageIntent);
                        return true;
                    }
                    DataSpeedAnalyzer.setTotalsize(mediaSize);

                    DataSpeedAnalyzer.setStartTime(System.currentTimeMillis());

                }
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();

                LogUtil.e(TAG, "IOException=" + e.getMessage());
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, "Exception=" + e.getMessage());
                return false;
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private static long calculateSize( String mediaType, JSONArray mediaList ) {
        long size = 0;

        if(mediaList.size()>0){
            if ( mediaType.equalsIgnoreCase( VZTransferConstants.PHOTOS ) ) {
                size = Long.parseLong(mediaStateObject.getPhotosSize());
            } else if ( mediaType.equalsIgnoreCase( VZTransferConstants.VIDEOS ) ) {
                size = Long.parseLong(mediaStateObject.getVideosSize());
            } else if ( mediaType.equalsIgnoreCase( VZTransferConstants.APPS ) ) {
                if (VZTransferConstants.SUPPORT_APPS) {
                    size = Long.parseLong(mediaStateObject.getAppsSize());
                }
            }else if ( mediaType.equalsIgnoreCase( VZTransferConstants.MUSICS ) ) {
                size = Long.parseLong(mediaStateObject.getMusicsSize());
            } else if ( mediaType.equalsIgnoreCase( VZTransferConstants.CALENDAR ) ) {
                size = Long.parseLong(mediaStateObject.getCalendarSize());
            } else if ( mediaType.equalsIgnoreCase( VZTransferConstants.DOCUMENTS ) ) {
                if (VZTransferConstants.SUPPORT_DOCS) {
                    size = Long.parseLong(mediaStateObject.getDocumentsSize());
                }
            }
        }
        LogUtil.d(TAG, "Media Size : " + size );
        return size;
    }

    private static void createMediaListFile(String mediaType, String mediaFileName, JSONArray mediaList ) {
        LogUtil.d(TAG, "Creating "+mediaType+" Media List File" );
        FileOutputStream mediaOutputStream = null; // Stream to write to destination

        File transferFolder = new File( VZTransferConstants.VZTRANSFER_DIR );
        transferFolder.mkdirs();

        File client_media_file = new File( VZTransferConstants.VZTRANSFER_DIR, mediaFileName );

        if ( client_media_file.exists() ) {
            LogUtil.d(TAG, "Media List file exists, deleting...");
            client_media_file.delete();
        }

        try {
            mediaOutputStream = new FileOutputStream( client_media_file ); // Create output stream
            mediaOutputStream.write( mediaList.toJSONString().getBytes() );
        }catch (Exception e) {
            e.printStackTrace();
        }
        // Always close the streams, even if exceptions were thrown
        finally {
            if ( mediaOutputStream != null) {
                try {
                    mediaOutputStream.close();
                    LogUtil.d(TAG, "Media file list from Client is closed...");
                } catch (IOException e) {
                    LogUtil.e(TAG, "Exception closing Media file list");
                    e.printStackTrace();
                }
            }
        }
    }
}
