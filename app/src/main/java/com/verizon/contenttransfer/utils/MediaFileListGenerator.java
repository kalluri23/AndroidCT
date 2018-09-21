package com.verizon.contenttransfer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.sender.CalendarSender;
import com.verizon.contenttransfer.p2p.sender.MessageSender;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import android.util.Log;

@SuppressWarnings("ALL")
public class MediaFileListGenerator {

    private static final String TAG = "MediaFileListGenerator";

    private static StringBuilder photosJsonList = new StringBuilder();
    private static StringBuilder appsJsonList = new StringBuilder();
    private static StringBuilder videosJsonList = new StringBuilder();
    private static StringBuilder musicsJsonList = new StringBuilder();
    private static StringBuilder calendarJsonList = new StringBuilder();
    private static StringBuilder documentJsonList = new StringBuilder();
    private static StringBuilder passwordsJsonList = new StringBuilder();

    public static int TOT_PHOTOS = 0;
    public static int TOT_VIDEOS = 0;
    public static int TOT_MUSICS = 0;
    public static int TOT_APPS = 0;
    public static int TOT_CALLLOGS = 0;
    public static int TOT_MESSAGES = 0;
    public static int TOT_CALENDAR = 0;
    public static int TOT_CONTACTS = 0;
    public static int TOT_CLOUD_CONTACTS = 0;
    public static int TOT_DOCS = 0;
    public static int TOT_PASSWORDS = 0;
    //public static long TOT_MEDIA_SIZE = 0L;
    //public static int TOT_DOC=0;

    public static void resetVariables() {
        photosJsonList = new StringBuilder();
        videosJsonList = new StringBuilder();
        musicsJsonList = new StringBuilder();
        appsJsonList = new StringBuilder();
        TOT_PHOTOS = 0;
        TOT_VIDEOS = 0;
        TOT_APPS = 0;
        TOT_MUSICS = 0;
        TOT_APPS = 0;
        //CTAnalyticUtil.getInstance().setCallLogCount(0);
        //CTAnalyticUtil.getInstance().setSmsCount(0);
        //CTAnalyticUtil.getInstance().setContactsCount(0);
        TOT_CONTACTS = 0;
        TOT_CLOUD_CONTACTS = 0;
        TOT_CALLLOGS = 0;
        TOT_MESSAGES = 0;
        TOT_DOCS = 0;
        TOT_PASSWORDS = 0;
        //TOT_MEDIA_SIZE = 0L;
    }

/*    public int getSMSFileList(Context ctxt, int mmsCount) {
        return SMSSender.fetchSMSFromDevice(ctxt, mmsCount);
    }*/

/*    public int getMMSFileList(Context ctxt) {
        MMSSender mmsSender = new MMSSender(ctxt);
        return mmsSender.fetchMMSFromDevice();
    }*/

    public int getMessageFileList(Context ctxt) {
        MessageSender messageSender = new MessageSender(ctxt);
        MediaFileListGenerator.TOT_MESSAGES = messageSender.fetchMMSSMSFromDevice();
        return MediaFileListGenerator.TOT_MESSAGES;
    }

    public Map<String, String> getCallLogsFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();

        // reading all data in descending order according to DATE
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";


        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, strOrder);

        int callLogCount = cursor.getCount();
        LogUtil.d(TAG, "Number of callLogCount to transfer : " + callLogCount);
        TOT_CALLLOGS = callLogCount;
        HashMap<String, String> result = new HashMap<String, String>(cursor.getCount());
        JSONArray callLogsArray = new JSONArray();
        //callLogsJsonList.append("[");

        if (callLogCount > 0) {
            int count = 0;
            if (cursor.moveToFirst()) {

                do {
                    DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_CALLLOG) + count++ + "/" + callLogCount);
                    JSONObject log = new JSONObject();
                    //++TOT_CALLLOGS;
                    String id = cursor.getString((cursor.getColumnIndex(CallLog.Calls._ID)));
                    String callNumber = cursor.getString(cursor
                            .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                    String callName = cursor
                            .getString(cursor
                                    .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));
                    String callDate = cursor.getString(cursor
                            .getColumnIndex(android.provider.CallLog.Calls.DATE));
                    //SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                    //String dateString = formatter.format(new Date(Long.parseLong(callDate)));
                    String callType = cursor.getString(cursor
                            .getColumnIndex(android.provider.CallLog.Calls.TYPE));
                    String isCallNew = cursor.getString(cursor
                            .getColumnIndex(android.provider.CallLog.Calls.NEW));
                    String duration = cursor.getString(cursor
                            .getColumnIndex(android.provider.CallLog.Calls.DURATION));

                    String cashedNumberType = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
                    String cashedNumberLabel = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                    try {
                        if (cashedNumberType == null) {
                            cashedNumberType = "0";
                        }
                        log.put("cashedNumberType", cashedNumberType);


                        if (cashedNumberLabel == null) {
                            cashedNumberLabel = "";
                        }
                        log.put("cashedNumberLabel", cashedNumberLabel);


                        if (id == null) {
                            id = "";
                        }
                        log.put("id", id);
                        //LogUtil.d(TAG,"callName  = "+callName);
                        if (callName == null) {
                            callName = "";
                        }
                        log.put("callName", callName);
                        if (callDate == null) {
                            callDate = "0";
                        }
                        log.put("dateString", callDate);
                        if (duration == null) {
                            duration = "";
                        }
                        log.put("duration", duration);
                        if (callType == null) {
                            callType = "";
                        }
                        log.put("callType", callType);
                        if (callNumber == null) {
                            callNumber = "";
                        }
                        log.put("callNumber", callNumber);
                        if (isCallNew == null) {
                            isCallNew = "";
                        }
                        log.put("isCallNew", isCallNew);
                        //result.put( "callName:"+callName, "callNumber:"+callNumber);

                        result.put("Path:" + callName, "Size:" + 1);


                        //LogUtil.d(TAG, "Each Call log = " + log);

                        callLogsArray.add(log);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if(!Utils.shouldCollect(VZTransferConstants.CALLLOG_STR)){
                        break;
                    }
                } while (cursor.moveToNext());

                LogUtil.d(TAG, "TOTAL call logs :" + callLogsArray.toString());
            }

            //LogUtil.d(TAG, "Call logs = " + callLogsArray);
            cursor.close();

        }
        writeToFile(callLogsArray.toString(), VZTransferConstants.CALLLOG);

        //reset the musicsJsonList
        //callLogsJsonList.setLength(0);
        return result;
    }

    public Map<String, String> getMusicsFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();

        String[] columns = new String[]{
                ImageColumns._ID,
                ImageColumns.TITLE,
                ImageColumns.DATA,
                ImageColumns.MIME_TYPE,
                ImageColumns.SIZE};

        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);

        int musicCount = cursor.getCount();
        LogUtil.d(TAG, "Number of Musics to transfer : " + musicCount);
        TOT_MUSICS = musicCount;
        //ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        HashMap<String, String> result = new HashMap<String, String>(cursor.getCount());

        musicsJsonList.append("[");

        if (musicCount > 0) {
            int count = 0;
            if (cursor.moveToFirst()) {
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                final int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                final int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                final String title = cursor.getString(titleColumn);
                final String mimeType = cursor.getString(mimeTypeColumn);
                //final int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                do {
                   DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_MUSIC) + count++ + "/" + musicCount);
                    final String musicPath = cursor.getString(dataColumn);
                    File file = new File(musicPath);
                    final String musicName = musicPath.substring(musicPath.lastIndexOf("/") + 1);
                    final String musicSize = Long.toString(file.length());
                    final String albumName = MediaFileNameGenerator.getAlbumPath(musicPath);
                    LogUtil.d(TAG, "Music file Name =" + musicName);
                    long eachMusicSize = Integer.parseInt(musicSize);
                    if (eachMusicSize > 0 &&
                            Utils.isValidFile(file) &&
                            !musicName.endsWith(VZTransferConstants.HANGOUT_MESSAGE) &&
                            !musicName.startsWith(VZTransferConstants.AMAZON_MP3) &&
                            !musicName.startsWith(VZTransferConstants.GOOGLE_MP3)) {
                        CTSelectContentModel.getInstance().setTotalMusicBytes(CTSelectContentModel.getInstance().getTotalMusicBytes() + eachMusicSize);
                        result.put("Path:" + musicName, "Size:" + musicSize);
                        musicsJsonList.append(Utils.getRequestedFileDetails(musicPath, musicSize, albumName));
                        //LogUtil.d(TAG, "Name: "+ musicName + " --Size : " + musicSize );
                    } else {
                        --TOT_MUSICS;
                    }
                    if(!Utils.shouldCollect(VZTransferConstants.AUDIO_STR)){
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();


            LogUtil.d(TAG, "Music List : " + musicsJsonList.toString());

            //remove last comma
            int lastIndex = musicsJsonList.toString().lastIndexOf(",");
            LogUtil.d(TAG, "last index =" + lastIndex);
            if (lastIndex > -1) {
                musicsJsonList = musicsJsonList.deleteCharAt(lastIndex);
            }
        }
        musicsJsonList.append("]");
        writeToFile(musicsJsonList.toString(), "musics");

        //reset the musicsJsonList
        musicsJsonList.setLength(0);
        return result;
    }

    public Map<String, String> getPhotosFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();

        String[] columns = new String[]{
                ImageColumns._ID,
                ImageColumns.TITLE,
                ImageColumns.DATA,
                ImageColumns.MIME_TYPE,
                ImageColumns.SIZE};
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);

        int photoCount = cursor.getCount();
        LogUtil.d(TAG, "Number of Photos to transfer : " + photoCount);
        TOT_PHOTOS = photoCount;
        //ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        HashMap<String, String> result = new HashMap<String, String>(cursor.getCount());

        photosJsonList.append("[");

        if (photoCount > 0) {
            int count = 0;
            if (cursor.moveToFirst()) {
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                final int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                final int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                final String title = cursor.getString(titleColumn);
                final String mimeType = cursor.getString(mimeTypeColumn);
                //final int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                do {
                    DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_PHOTO) + count++ + "/" + photoCount);
                    final String photoPath = cursor.getString(dataColumn);
                    final String photoName = photoPath.substring(photoPath.lastIndexOf("/") + 1);
                    File file = new File(photoPath);
                    final String photoSize = Long.toString(file.length());

                    final String albumName = MediaFileNameGenerator.getAlbumPath(photoPath);
                    //result.add(data);
                    long eachPhotoSize = Integer.parseInt(photoSize);
                    if (eachPhotoSize > 0 && Utils.isValidFile(file)) {
                        result.put("Path:" + photoName, "Size:" + photoSize);
                        CTSelectContentModel.getInstance().setTotalPhotosBytes(CTSelectContentModel.getInstance().getTotalPhotosBytes() + eachPhotoSize);
                        photosJsonList.append(Utils.getRequestedFileDetails(photoPath, photoSize, albumName));

                        LogUtil.d(TAG, "Name: " + photoName + " --Size : " + photoSize);
                    } else {
                        LogUtil.d(TAG, "File " + photoName + " size is 0");
                        --TOT_PHOTOS;
                    }
                    if(!Utils.shouldCollect(VZTransferConstants.PHOTOS_STR)){
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            LogUtil.d(TAG, "Photo List json: " + photosJsonList.toString());

            //remove last comma
            int lastIndex = photosJsonList.toString().lastIndexOf(",");
            if (lastIndex > -1) {
                photosJsonList = photosJsonList.deleteCharAt(lastIndex);
            }
        }
        photosJsonList.append("]");
        //writeToFile( result, "photos" );
        writeToFile(photosJsonList.toString(), "photos");

        //reset the photosJsonList

        photosJsonList.setLength(0);
        return result;

    }


    public Map<String, String> getDocFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

// every column, although that is huge waste, you probably need
// BaseColumns.DATA (the path) only.
        String[] projection = {MediaStore.Files.FileColumns.DATA};

// exclude media files, they would be here also.
/*
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + '='
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        String[] selectionArgs = null; // there is no ? in selection so null here

        String sortOrder = null; // unordered
        Cursor allNonMediaFiles = cr.query(uri, projection, selection, selectionArgs, sortOrder);
*/

        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String txtType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt");
        String pdfType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        String docType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc");
        String xlsType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls");
        String pptType = //MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt");
                "application/vnd.ms-powerpoint";
        String docxType = //MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx");
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        String xlsxType = //MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx");
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String pptxType = //MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx");
                "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        String xmlType = "text/xml";

        String[] selectionArgs = new String[]{txtType, pdfType, docType, xlsType, pptType, docxType, xlsxType, pptxType, xmlType};

        //Cursor allPdfFiles = null;
        Cursor allPdfFiles = null;

        HashMap<String, String> docFileList = new HashMap<String, String>();

        documentJsonList.append("[");

        for (String type : selectionArgs) {
            String[] typeargs = new String[]{type};
            try {
                allPdfFiles = cr.query(uri, projection, selectionMimeType, typeargs, null);
                //allPdfFiles = cr.query(uri, columns, null, null, null);
            } catch (Exception e) {
                LogUtil.d(TAG, e.getMessage());
            }

            int docCount = allPdfFiles.getCount();
            TOT_DOCS += docCount;
            LogUtil.d(TAG, "Number of Documents to transfer : " + docCount);

            if (docCount > 0) {
                int count = 0;
                if (allPdfFiles.moveToFirst()) {
                    final int dataColumn = allPdfFiles.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    do {
                        final String docPath = allPdfFiles.getString(dataColumn);
                        File myFile = new File(docPath);
                        final String docName = docPath.substring(docPath.lastIndexOf("/") + 1);
                        final String docSize = Long.toString(myFile.length());
                        final String albumName = MediaFileNameGenerator.getAlbumPath(docPath);

                        long size = Integer.parseInt(docSize);
                        LogUtil.d(TAG,"Document file validation :"+docPath);
                        if (size > 0 && Utils.isValidFile(myFile)) {

                            docFileList.put("Path:" + docName, "Size:" + size);

                            CTSelectContentModel.getInstance().setTotalDocumentsBytes(CTSelectContentModel.getInstance().getTotalDocumentsBytes() + size);

                            documentJsonList.append(Utils.getRequestedFileDetails(docPath, docSize, albumName));

                            LogUtil.d(TAG, "document Name....: " + docName + " --Size : " + docSize);
                        } else {
                            LogUtil.d(TAG, "File " + docName + " size is 0");
                            --TOT_DOCS;
                        }
                        if(!Utils.shouldCollect(VZTransferConstants.DOCUMENTS_STR)){
                            break;
                        }
                    } while (allPdfFiles.moveToNext());
                }

                docFileList.putAll(new HashMap<String, String>((allPdfFiles.getCount())));


            }
        }

        allPdfFiles.close();

        LogUtil.d(TAG, "Document List : " + documentJsonList.toString());

        //remove last comma
        int lastIndex = documentJsonList.toString().lastIndexOf(",");
        if (lastIndex > -1) {
            documentJsonList = documentJsonList.deleteCharAt(lastIndex);
        }
        documentJsonList.append("]");
        writeToFile(documentJsonList.toString(), "documents");

        //reset the photosJsonList
        documentJsonList.setLength(0);

        return docFileList;
    }

    public Map<String, String> getAppsList(Context ctxt) {


        PackageManager packageManager = ctxt.getPackageManager();
        List<ApplicationInfo> appsList = Utils.getInstalledApps(ctxt);

        appsJsonList.append("[");
        final PackageManager pm = ctxt.getPackageManager();
        TOT_APPS = appsList.size();
        for (ApplicationInfo appInfo : appsList) {
            String path = appInfo.sourceDir;
            String name = appInfo.loadLabel(ctxt.getPackageManager()).toString();
            name = Utils.stringWithNoSpecialCharacter(name);
            /*
             * TODO - after 3.5.4 release
             *
             * name = Utils.getEncodedString(name);
             *
             * ios - need to decode if we send it encoded.
             */
            LogUtil.d(TAG,"app file path:"+path+" - name :"+name);
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
                File file = new File(applicationInfo.publicSourceDir);
                if (Utils.isValidFile(file)) {
                    String size = "0";
                    if(!CTGlobal.getInstance().isCross()){

                        size = String.valueOf(file.length());
                        CTSelectContentModel.getInstance().setTotalAppsBytes(CTSelectContentModel.getInstance().getTotalAppsBytes() + (file.length()));
                    }else{
                        Drawable apkIcon= appInfo.loadIcon(pm);
                        Utils.saveImage(apkIcon, name);
                        String icon=VZTransferConstants.tempAppsIconsStoragePath+"/"+name+".png";
                        CTAppUtil.getInstance().addIconPath(path,icon);
                        File iconsFile= new File(icon);
                        String iconsSize= String.valueOf(iconsFile.length());
                        CTSelectContentModel.getInstance().setTotalAppIconsBytes(CTSelectContentModel.getInstance().getTotalAppIconsBytes() + (iconsFile.length()));
                    }
                    appsJsonList.append(Utils.getAppsFileDetails(path, name, size));
                }else {
                    TOT_APPS --;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if(!Utils.shouldCollect(VZTransferConstants.APPS_STR)){
                LogUtil.e(TAG, "Collecting APPS  - not selected by user");
                break;
            }
        }
//remove last comma
        int lastIndex = appsJsonList.toString().lastIndexOf(",");
        if (lastIndex > -1) {
            appsJsonList = appsJsonList.deleteCharAt(lastIndex);
        }
        appsJsonList.append("]");

        //appsJsonList = new StringBuilder(jsonArray.toString());
        LogUtil.d("TAG", "Apps json = " + appsJsonList.toString());


        writeToFile(appsJsonList.toString(), "apps");
        appsJsonList.setLength(0);

        return null;
    }

    public Map<String, String> getVideosFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();

        String[] columns = new String[]{
                VideoColumns._ID,
                VideoColumns.TITLE,
                VideoColumns.DATA,
                VideoColumns.MIME_TYPE,
                VideoColumns.SIZE};
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);

        int videoCount = cursor.getCount();
        LogUtil.d(TAG, "Number of Videos to transfer : " + videoCount);
        TOT_VIDEOS = videoCount;
        //ArrayList<String> videoFileList = new ArrayList<String>(cursor.getCount());
        HashMap<String, String> videoFileList = new HashMap<String, String>(cursor.getCount());
        //ArrayList<String> displayName = new ArrayList<String>(cursor.getCount());

        videosJsonList.append("[");

        if (videoCount > 0) {
            int count = 0;
            if (cursor.moveToFirst()) {
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                //final int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                //final int fileNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

                do {
                    final String videoPath = cursor.getString(dataColumn);
                    final String videoName = videoPath.substring(videoPath.lastIndexOf("/") + 1);
                    File file = new File(videoPath);
                    final String videoSize = Long.toString(file.length());
                    final String albumName = MediaFileNameGenerator.getAlbumPath(videoPath);
                    //final String dispName = cursor.getString( fileNameColumn );
                    //videoFileList.add( videoName );
                    DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_VIDEO) + count++ + "/" + videoCount);
                    long eachVideoSize = Long.parseLong(videoSize);
                    if (eachVideoSize > 0
                            && Utils.isValidFile(file)) {
                        if (CTGlobal.getInstance().isCross()) {
                            LogUtil.d(TAG, "Video file name : " + videoName);

                            if (isSupportedFileFormat(videoName)) {
                                LogUtil.d(TAG, "Cross supported file format  : true");
                                CTSelectContentModel.getInstance().setTotalVideosBytes(CTSelectContentModel.getInstance().getTotalVideosBytes() + eachVideoSize);
                                videoFileList.put("Path:" + videoName, "Size:" + videoSize);
                                videosJsonList.append(Utils.getRequestedFileDetails(videoPath, videoSize, albumName));
                            } else {
                                LogUtil.d(TAG, "Cross supported file format  : false");
                                --TOT_VIDEOS;
                            }
                        } else {
                            LogUtil.d(TAG, "Is cross platform : false");
                            CTSelectContentModel.getInstance().setTotalVideosBytes(CTSelectContentModel.getInstance().getTotalVideosBytes() + eachVideoSize);
                            videoFileList.put("Path:" + videoName, "Size:" + videoSize);
                            videosJsonList.append(Utils.getRequestedFileDetails(videoPath, videoSize, albumName));
                        }
                    } else {
                        --TOT_VIDEOS;
                    }
                    if(!Utils.shouldCollect(VZTransferConstants.VIDEOS_STR)){
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();


            LogUtil.d(TAG, "Video List : " + videosJsonList.toString());


            int lastIndex = videosJsonList.toString().lastIndexOf(",");
            if (lastIndex > -1) {
                videosJsonList = videosJsonList.deleteCharAt(lastIndex);
            }


            //remove last comma

        }
        videosJsonList.append("]");
        LogUtil.d(TAG, "Video file List is created : ");

        //writeToFile( videoFileList, "videos" );
        writeToFile(videosJsonList.toString(), "videos");

        //reset the videosJsonList

        videosJsonList.setLength(0);

        return videoFileList;
    }

    private boolean isSupportedFileFormat(String videoName) {
        //if ([[type lowercaseString] isEqualToString:@"m4v"] || [[type lowercaseString] isEqualToString:@"mp4"] || [[type lowercaseString] isEqualToString:@"mov"]) {
        if (videoName != null &&
                (videoName.toLowerCase().endsWith("m4v") || videoName.toLowerCase().endsWith("mp4")
                        || videoName.toLowerCase().endsWith("mov"))) {
            return true;
        }
        return false;
    }

    public List<String> getCalendarFileList(Context ctxt) {
        ContentResolver cr = ctxt.getContentResolver();
        List<String> calendarFileList = new ArrayList<String>();

        //Generate ics files here
        LogUtil.d(TAG, "Generate ICS files here....");
        Map<String, String> calMap = CalendarSender.exportCalendars();

        calendarJsonList.append("[");

        //get only local calendars from sending device
        int count = 0;

        if (null != calMap && !calMap.isEmpty()) {
            int i = 0;
            for (Map.Entry<String, String> entry : calMap.entrySet()) {
                DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_CALENDAR) + count++);
                CTSelectContentModel.getInstance().setTotalCalendarBytes(CTSelectContentModel.getInstance().getTotalCalendarBytes() + Integer.parseInt(entry.getValue()));
                calendarFileList.add("Path:" + entry.getKey());
                calendarJsonList.append("{\"Path\":\"");
                calendarJsonList.append(VZTransferConstants.VZTRANSFER_DIR + "calendar" + File.separator + entry.getKey() + ".ics");
                calendarJsonList.append("\",");
                calendarJsonList.append("\"Size\":\"");
                calendarJsonList.append(entry.getValue());
                calendarJsonList.append("\"");
                calendarJsonList.append("},");
                LogUtil.d(TAG,"Collecting calendar : "+ i++);
                if(!Utils.shouldCollect(VZTransferConstants.CALENDAR_STR)){
                    break;
                }
            }

            LogUtil.d(TAG, "Calendar List : " + calendarJsonList.toString());
            int lastIndex = calendarJsonList.toString().lastIndexOf(",");
            if (lastIndex > -1) {
                calendarJsonList = calendarJsonList.deleteCharAt(lastIndex);
            }
        }

        TOT_CALENDAR = calendarFileList.size();
        calendarJsonList.append("]");


        LogUtil.d(TAG, "Calendar file List is created : ");

        writeToFile(calendarJsonList.toString(), "CALENDAR");
        calendarJsonList.setLength(0);

        return calendarFileList;
    }

    private boolean writeToFile(HashMap<String, String> fileList, String media) {

        //JSONObject filelist = new JSONObject( fileList );
        //JSONArray jarray = JSONArray.fromObject( fileList );
        //JSONArray jarray = new JSONArray( fileList.toString() );
        String jsonText = JSONValue.toJSONString(fileList);


        FileOutputStream fos = null;
        String mediaFileName = "none";

        if (media.equalsIgnoreCase("PHOTOS")) {
            mediaFileName = VZTransferConstants.PHOTOS_FILE;
        } else if (media.equalsIgnoreCase("VIDEOS")) {
            mediaFileName = VZTransferConstants.VIDEOS_FILE;
        } else {
            LogUtil.e(TAG, "Media format specified ---" + media + " is not listed for processing");
        }

        try {
            //fos = new FileOutputStream ( new File (Constants.vzTransferDirectory+mediaFileName) );
            fos = new FileOutputStream(new File(VZTransferConstants.VZTRANSFER_DIR + mediaFileName));
            //fos.write( filelist.toJSONString().getBytes() );
            fos.write(jsonText.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;

    }

    private boolean writeToFile(List<String> fileList, String media) {
        FileOutputStream fos = null;
        String mediaFileName = "none";

        if (media.equalsIgnoreCase("PHOTOS")) {
            mediaFileName = VZTransferConstants.PHOTOS_FILE;
        } else if (media.equalsIgnoreCase("VIDEOS")) {
            mediaFileName = VZTransferConstants.VIDEOS_FILE;
        } else {
            LogUtil.e(TAG, "Media format specified ---" + media + " is not listed for processing");
        }

        if (!mediaFileName.equalsIgnoreCase("NONE")) {
            try {
                fos = new FileOutputStream(new File(VZTransferConstants.VZTRANSFER_DIR + mediaFileName));
                for (String fileName : fileList) {
                    fos.write(fileName.getBytes());
                    fos.write("\n".getBytes());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    /*
     * This method is used to create .txt file. and the .txt file will hold a json array.
     *
     *@jsonObject : Is a json object need to write in file.
     *@media : Is a String value of media type to select file name.
     *@closeFile:  boolean value to finish file with "]" character.
     *@appendComma : boolean value used to decide where the need to append a comma.
     */
    public static boolean appendToFile(JSONObject jsonObject, String media, boolean closeFile, boolean appendComma) {

        String mediaFileName = "none";
        BufferedWriter writer = null;
        try {
            if (media.equalsIgnoreCase(VZTransferConstants.SMS)) {
                mediaFileName = VZTransferConstants.SMS_FILE;

            }
            if (!mediaFileName.equalsIgnoreCase("NONE")) {
                File dir = new File(VZTransferConstants.VZTRANSFER_DIR.substring(0, VZTransferConstants.VZTRANSFER_DIR.length() - 1));
                if (dir.exists() == false)
                    dir.mkdir();
                File f = new File(dir, mediaFileName);
                if (!f.exists()) {
                    f.createNewFile();
                }

                if (jsonObject != null) {
                    FileWriter file = new FileWriter(f, true);
                    writer = new BufferedWriter(file);
                    try {

                        writer.append(jsonObject.toString());

                        if (appendComma) {
                            writer.append(VZTransferConstants.LF);
                        }

                    } catch (IOException exception) {

                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                    }

                }

                //LogUtil.d(TAG, media+" file wrote successfully.");
            }
        } catch (Exception exception) {

        }
        return true;
    }

    public static boolean appendToFile(JSONArray jsonArray, String media) {
        if (jsonArray.size() == 0) {
            return false;
        }
        String mediaFileName = "none";
        BufferedWriter writer = null;
        try {
            if (media.equalsIgnoreCase(VZTransferConstants.SMS)) {
                mediaFileName = VZTransferConstants.SMS_FILE;

            }
            if (!mediaFileName.equalsIgnoreCase("NONE")) {
                File transferDir = new File(VZTransferConstants.VZTRANSFER_DIR);

                if (transferDir.exists()) {
                    LogUtil.d(TAG, "Transfer Directory already exists..");
                    if (!transferDir.isDirectory()) {
                        transferDir.delete();
                        transferDir.mkdirs();
                    }
                } else {
                    LogUtil.d(TAG, "Transfer directory is not there, creating ");
                    //If directory does not exists then crete it
                    transferDir.mkdirs();
                }
                File dir = new File(VZTransferConstants.VZTRANSFER_DIR.substring(0, VZTransferConstants.VZTRANSFER_DIR.length() - 1));
                if (dir.exists() == false)
                    dir.mkdir();
                File f = new File(dir, mediaFileName);
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();

                FileWriter file = new FileWriter(f, true);
                writer = new BufferedWriter(file);
                if (f.length() == 0) {
                    writer.append("[");
                }
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);
                    writer.append(jsonObject.toString());
                    if (i < jsonArray.size() - 1) {
                        writer.append(",");
                    }
                }

                writer.append("]");

                LogUtil.d(TAG, "CT_Logs file wrote successfully.");
            }
        } catch (IOException exception) {

        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
        }
        return true;
    }


    private static String getMediaFileName(String media) {
        if (media.equalsIgnoreCase(VZTransferConstants.PHOTOS)) {
            return VZTransferConstants.PHOTOS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.SMS)) {
            return VZTransferConstants.SMS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.VIDEOS)) {
            return VZTransferConstants.VIDEOS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.MUSICS)) {
            return VZTransferConstants.MUSICS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.CALLLOG)) {
            return VZTransferConstants.CALLLOGS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.CALENDAR)) {
            return VZTransferConstants.CALENDAR_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.DOCUMENTS)) {
            return VZTransferConstants.DOCUMENTS_FILE;
        } else if (media.equalsIgnoreCase(VZTransferConstants.APPS)) {
            return VZTransferConstants.APPS_FILE;
        } else {
            LogUtil.e(TAG, "Media format specified ---" + media + " is not listed for processing");
            return "none";
        }
    }

    public static boolean writeToFile(String fileJsonArray, String media) {
        FileOutputStream fos = null;
        String mediaFileName = getMediaFileName(media);
        if (!mediaFileName.equalsIgnoreCase("NONE")) {
            try {
                fos = new FileOutputStream(new File(VZTransferConstants.VZTRANSFER_DIR + mediaFileName));
                fos.write(fileJsonArray.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    public static boolean writeAppAnalyticsToFile(String fileJsonArray, String media) {
        FileOutputStream fos = null;
        String mediaFileName = "none";
        String lineEnd = "\r\n";
        LogUtil.d(TAG, "Writing " + media + " file list to file.");

        if (media.equalsIgnoreCase(VZTransferConstants.APP_ANALYTICS)) {
            mediaFileName = VZTransferConstants.APP_ANALYTICS_FILE;
        }else if (media.equalsIgnoreCase(VZTransferConstants.VZCLOUD_CLICKS)){
            mediaFileName = VZTransferConstants.VZCLOUD_CLICKS_FILE;
        } else {
            LogUtil.e(TAG, "Media format specified ---" + media + " is not listed for processing");
        }

        File transferDir = new File(VZTransferConstants.VZTRANSFER_DIR);

        if (transferDir.exists()) {
            LogUtil.d(TAG, "Transfer Directory already exists..");
            if (!transferDir.isDirectory()) {
                transferDir.delete();
                transferDir.mkdirs();
            }
        } else {
            LogUtil.d(TAG, "Transfer directory is not there, creating ");
            //If directory does not exists then crete it
            transferDir.mkdirs();
        }

        File file = new File(VZTransferConstants.VZTRANSFER_DIR + mediaFileName);


        if (!mediaFileName.equalsIgnoreCase("NONE")) {
            try {

                if (file.exists() && file.length() > 0) {
                    try {
                        fos = new FileOutputStream(file, true);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fos);
                        myOutWriter.append(lineEnd);
                        myOutWriter.append(fileJsonArray);
                        myOutWriter.close();

                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                } else {
                    fos = new FileOutputStream(file);
                    fos.write(fileJsonArray.getBytes());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //LogUtil.d(TAG,"File content ="+ReadAppAnalyticsFile(file));
        return true;
    }

    public static String ReadAppAnalyticsFile(File file) {
        //reading text from file

        final int READ_BLOCK_SIZE = 100;
        try {
            FileInputStream fileIn = new FileInputStream(file);
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            //Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
            LogUtil.d(TAG, "App Analytics file content =" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
