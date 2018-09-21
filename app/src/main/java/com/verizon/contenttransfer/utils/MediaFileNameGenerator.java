package com.verizon.contenttransfer.utils;

import android.os.Build;
import android.os.Environment;

import com.verizon.contenttransfer.base.VZTransferConstants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.util.HashMap;

/**
 * Created by c0bissh on 3/7/2016.
 */
public class MediaFileNameGenerator {
    public MediaFileNameGenerator() {
        LogUtil.d(TAG,"mediaFileNameCreator class created.");
    }

    private static final String TAG = "MediaFileNameGenerator";
    private static HashMap<String, Boolean> fileMap = new HashMap<String, Boolean>();

    public static File generateFileName(String media, JSONObject jsonObject){
        LogUtil.d(TAG,"generateFileName - Json Object ="+jsonObject);
        File downloadFile = null;
        try {
            downloadFile = createFileAndRegisterInHashMap(media, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadFile;
    }

    private static File createFileAndRegisterInHashMap(String media, JSONObject jsonObject) {
        String filePath = (String) jsonObject.get("Path");

        String dirPath = "";
        File downloadFile = null;
        try {

            LogUtil.d(TAG, "Media file name =" + filePath); //   /storage/emulated/0/DCIM/Camera/20160307_140402.jpg

            if (media.equalsIgnoreCase(VZTransferConstants.CALENDAR)) {
                dirPath = VZTransferConstants.tempCalendarStoragePath + File.separator;
            }
            else{
                JSONParser parser = new JSONParser();
                filePath = Utils.getDecodedString((String) jsonObject.get("Path"));
                String albumArrayStr="";
                if(null != jsonObject.get("AlbumName")){
                    JSONArray albumArray = null;
                    try {
                        albumArray = (JSONArray) parser.parse(jsonObject.get("AlbumName").toString());
                        LogUtil.d(TAG,"albumArray = "+albumArray);
                        albumArrayStr = albumArray.get(albumArray.size() - 1).toString();
                        String[] tempAlbumArray = albumArrayStr.split(",");
                        LogUtil.d(TAG,"AlbumArray size :"+tempAlbumArray.length);
                        if(tempAlbumArray.length > 0){
                            albumArrayStr = tempAlbumArray[tempAlbumArray.length-1];
                        }
                        albumArrayStr =  Utils.getDecodedString(albumArrayStr);
                    } catch (Exception e) {
                        LogUtil.e(TAG,"Exception on parsing album name to create map :"+e.getMessage());
                        //e.printStackTrace();
                    }

                }
                LogUtil.d(TAG, "albumArrayStr =" + albumArrayStr);
                dirPath = getMediaStoragePath(media,albumArrayStr);
            }
            //dirPath = "/storage/emulated/0/Documents/log/acore/0_dumpacore_3rd_com.android.settings.txt/static";
            LogUtil.d(TAG,"Dir path ="+dirPath);
            File transferFolder = new File( dirPath );
            if(!transferFolder.exists()) {
                transferFolder.mkdirs();
            }
            String mFileName="";
            if(media.equalsIgnoreCase(VZTransferConstants.APPS)){
                String fileName = Utils.getDecodedString((String) jsonObject.get("name"));
                mFileName=fileName + ".apk";
            }else{
                mFileName=filePath.substring(filePath.lastIndexOf("/")+1);
            }

            LogUtil.d(TAG,"generateFile path ="+mFileName);
            downloadFile = new File( dirPath, mFileName );
            if(downloadFile.exists() && (downloadFile.length()>0)){
                fileMap.put(filePath, true);
            }else{
                fileMap.put(filePath, false);
            }
        }catch (Exception e){
            LogUtil.d(TAG, "Exception .. =" + e.getMessage());
        }

        return downloadFile;
    }

    private static String getMediaStoragePath(String media,String albumArrayStr) {
        JSONParser parser = new JSONParser();
        String mediaPath = "";
        String rootDir = Environment.getExternalStorageDirectory().toString();
        String dcim = "/dcim/";
        String cameraPath = "/dcim/camera/";
        String pictures = "/pictures/";
        String movies = "/movies/";
        String music = "/music/";
        String documents = "/documents/";
        JSONArray albumArray = null;
        try {
            albumArray = (JSONArray) parser.parse(albumArrayStr);
            LogUtil.d(TAG,"albumArray.size():"+albumArray.size());
            albumArrayStr = albumArray.get(albumArray.size() - 1).toString();
            LogUtil.d(TAG,"albumArrayStr:"+albumArrayStr);
        } catch (Exception e) {
            LogUtil.d(TAG,"Exception on parsing album name :"+e.getMessage());
            //e.printStackTrace();
        }
        String[] tempAlbumArray = albumArrayStr.split(",");
        LogUtil.d(TAG,"AlbumArray size :"+tempAlbumArray.length);
        if(tempAlbumArray.length > 0){
            albumArrayStr = tempAlbumArray[tempAlbumArray.length-1];
        }
        LogUtil.d(TAG, "albumArrayStr = " + albumArrayStr);
        LogUtil.d(TAG, "media = " + media);
        if (media.equalsIgnoreCase(VZTransferConstants.PHOTOS)) {
            if (null!=albumArrayStr && albumArrayStr.length() > 0) {
                LogUtil.d(TAG,"DCIM PATH="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());

                if(albumArrayStr.equalsIgnoreCase("/dcim/camera")){
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                    String folder = albumArrayStr.substring(dcim.length()-1,albumArrayStr.length());
                    folder=getGooglePixelFolder( folder);
                    LogUtil.d(TAG,"Directory /DCIM/Camera path 0= "+mediaPath);
                    mediaPath = mediaPath + folder;
                }else if(albumArrayStr.toLowerCase().contains("camera roll") && CTGlobal.getInstance().isCross() ){// if android device has camera roll folder...
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                    LogUtil.d(TAG,"Directory /DCIM/camera roll 1= "+mediaPath);
                    String folder = "/Camera";
                    folder=getGooglePixelFolder( folder);
                    LogUtil.d(TAG,"Directory /DCIM/camera roll 2= "+mediaPath);
                    mediaPath = mediaPath + folder;
                }else if(albumArrayStr.toLowerCase().startsWith(dcim)){
                    if(albumArrayStr.toLowerCase().startsWith(cameraPath)){
                        String folder = "/Camera";
                        folder=getGooglePixelFolder(folder);
                        folder += albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(dcim) + cameraPath.length()-1, albumArrayStr.length());
                        LogUtil.d(TAG,"Pixel camera path ="+folder);
                        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                        mediaPath = mediaPath + folder;
                        LogUtil.d(TAG, "Directory /DCIM/Camera path with folder 0= " + mediaPath);
                    }else {
                        String folder = albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(dcim) + dcim.length() - 1, albumArrayStr.length());
                        //String folder = albumArrayStr.substring(dcim.length()-1,albumArrayStr.length());
                        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                        mediaPath = mediaPath + folder;
                        LogUtil.d(TAG, "Directory /DCIM/Camera path with folder 0= " + mediaPath);
                    }
                }
                else{
                    if(albumArrayStr.equalsIgnoreCase("/pictures")){
                        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                        LogUtil.d(TAG,"Directory pictute path = "+mediaPath);

                    }else if(albumArrayStr.toLowerCase().startsWith(pictures)){
                        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                        LogUtil.d(TAG,"Directory pictute path = "+mediaPath);
                        String folder = albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(pictures)+pictures.length()-1,albumArrayStr.length());
                        //String folder = albumArrayStr.substring(pictures.length()-1,albumArrayStr.length());
                        mediaPath = mediaPath + folder;
                        LogUtil.d(TAG,"Directory pictute path with folder 1 = "+mediaPath);
                    }else {
                        mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                        String defaultPicture = albumArrayStr;//albumArray.get(albumArray.size() - 1).toString();
                        if(!defaultPicture.startsWith("/")){
                            defaultPicture = "/"+defaultPicture;
                        }
                        mediaPath = mediaPath + defaultPicture;
                        LogUtil.d(TAG,"Directory pictute path with folder 2= "+mediaPath);
                    }
                }
            }else{
                mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            }

        } else if (media.equalsIgnoreCase(VZTransferConstants.VIDEOS)) {
            if (null!=albumArrayStr && albumArrayStr.length() > 0) {
                LogUtil.d(TAG,"VIDEO DIRECTORY_MOVIES PATH ="+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
                if(albumArrayStr.equalsIgnoreCase("/Movies")){
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
                    LogUtil.d(TAG,"VIDEO Directory DIRECTORY_MOVIES path 0= "+mediaPath);
                }else if(albumArrayStr.equalsIgnoreCase("/DCIM/Camera")){
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                    String folder = albumArrayStr.substring(dcim.length()-1,albumArrayStr.length());
                    folder=getGooglePixelFolder( folder);
                    mediaPath = mediaPath + folder;
                    LogUtil.d(TAG,"Directory /DCIM/Camera path 0= "+mediaPath);
                }else if(albumArrayStr.toLowerCase().contains("camera roll") && CTGlobal.getInstance().isCross()){ // if android device has camera roll folder...
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                    LogUtil.d(TAG,"Directory /DCIM/camera roll 1= "+mediaPath);
                    String folder = "/Camera";
                    folder=getGooglePixelFolder( folder);
                    LogUtil.d(TAG,"Directory /DCIM/camera roll 2= "+mediaPath);
                    mediaPath = mediaPath + folder;
                }else if(albumArrayStr.toLowerCase().startsWith(movies)){
                    String folder = albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(movies)+movies.length()-1,albumArrayStr.length());
                    //String folder = albumArrayStr.substring(movies.length()-1,albumArrayStr.length());
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
                    mediaPath = mediaPath + folder;
                    LogUtil.d(TAG,"VIDEO Directory DIRECTORY_MOVIES path with folder 0= "+mediaPath);
                }
                else{
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
                    String defaultVideo = albumArrayStr;//albumArray.get(albumArray.size() - 1).toString();
                    if(!defaultVideo.startsWith("/")){
                        defaultVideo = "/"+defaultVideo;
                    }
                    mediaPath = mediaPath + defaultVideo;
                    LogUtil.d(TAG,"VIDEO DIRECTORY_DCIM path with folder 2= "+mediaPath);
                }
            }else{
                mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            }
        } else if (media.equalsIgnoreCase(VZTransferConstants.MUSICS)) {
            if (null!=albumArrayStr && albumArrayStr.length() > 0) {
                LogUtil.d(TAG,"MUSIC DIRECTORY_MUSIC PATH+"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());

                if(albumArrayStr.equalsIgnoreCase("/Music")){
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
                    LogUtil.d(TAG,"MUSIC Directory DIRECTORY_MUSIC path 0= "+mediaPath);
                }else if(albumArrayStr.toLowerCase().startsWith(music)){
                    String folder = albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(music)+music.length()-1,albumArrayStr.length());
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
                    mediaPath = mediaPath + folder;
                    LogUtil.d(TAG,"MUSIC Directory DIRECTORY_MUSIC path with folder 0= "+mediaPath);
                }
                else{
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
                    String defaultMusic = albumArrayStr;//albumArray.get(albumArray.size() - 1).toString();
                    if(!defaultMusic.startsWith("/")){
                        defaultMusic = "/"+defaultMusic;
                    }
                    mediaPath = mediaPath +defaultMusic ;
                    LogUtil.d(TAG,"MUSIC DIRECTORY_DCIM path with folder 2= "+mediaPath);
                }
            }else{
                mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            }
        }
        else if (media.equalsIgnoreCase(VZTransferConstants.APPS)) {
            mediaPath = VZTransferConstants.tempAppsStoragePath ;
            //open file with
            LogUtil.d(TAG,"APPS Directory apps path 0= "+mediaPath);
        }
        else if (media.equalsIgnoreCase(VZTransferConstants.DOCUMENTS)) {
        ////
            if (null!=albumArrayStr && albumArrayStr.length() > 0) {
                //LogUtil.d(TAG,"DOCUMENT albumArray.get(0) ="+albumArray.get(0));
                LogUtil.d(TAG, "DOCUMENT DIRECTORY_DOCUMENTS PATH+" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString());

                if(albumArrayStr.equalsIgnoreCase("/Documents")){
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
                    LogUtil.d(TAG,"DOCUMENT Directory DIRECTORY_DOCUMENTS path 0= "+mediaPath);
                }else if(albumArrayStr.toLowerCase().startsWith(documents)){
                    String folder = albumArrayStr.substring(albumArrayStr.toLowerCase().indexOf(documents)+documents.length()-1,albumArrayStr.length());
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
                    mediaPath = mediaPath + folder;
                    LogUtil.d(TAG,"DOCUMENTS Directory DIRECTORY_DOCUMENTS path with folder 0= "+mediaPath);
                }
                else{
                    mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
                    String defaultDocs = albumArrayStr;//albumArray.get(albumArray.size() - 1).toString();
                    if(!defaultDocs.startsWith("/")){
                        defaultDocs = "/"+defaultDocs;
                    }
                    mediaPath = mediaPath +defaultDocs ;
                    LogUtil.d(TAG,"DOCUMENT DIRECTORY_DOCUMENTS path with folder 2= "+mediaPath);
                }
            }

            else{
                mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            }
        }

        LogUtil.d(TAG,"getMediaStoragePath - mediaPath ="+mediaPath);
        return mediaPath;
    }

    private static String getGooglePixelFolder(String folder){

        if (Build.BRAND.equalsIgnoreCase(VZTransferConstants.GOOGLE_PIXEL)){
            folder = folder + VZTransferConstants.CT_DIR;
        }
        return folder;
    }

    /*public static File generateDefaultFileName(String media, JSONObject jsonObject){
        return generateFileName(media,jsonObject);
    }*/

    public static String getAlbumPath(String filePath) {
        String externalSDCardDCIM = "/DCIM/";
        String storagePath = "/storage/";
        String mFilePath = "";
        try{
            String pubStorageDir = String.valueOf(Environment.getExternalStorageDirectory());
            if(filePath.startsWith(pubStorageDir)){
                mFilePath = filePath.substring(filePath.toLowerCase().indexOf(pubStorageDir)+pubStorageDir.length(),filePath.lastIndexOf("/")); //   /storage/emulated/0
            }else if(filePath.contains(externalSDCardDCIM)){
                mFilePath = filePath.substring(filePath.indexOf(externalSDCardDCIM),filePath.lastIndexOf("/")); //  /storage/4B9A-562F/DCIM/Camera/20160518_134843.jpg
            }else {
                String externalStoragePath = filePath.substring(storagePath.length(),filePath.lastIndexOf("/")); //  /storage/4B9A-562F/RootSD2.JPG
                LogUtil.d(TAG,"externalStoragePath ="+externalStoragePath);
                if(externalStoragePath.indexOf("/")>0) {
                    mFilePath = externalStoragePath.substring(externalStoragePath.indexOf("/"), externalStoragePath.length());
                }
            }
        }catch (Exception e){
            LogUtil.e(TAG, "getAlbumPath exception :" + e.getMessage());
        }

        if(mFilePath.equals("") && CTGlobal.getInstance().isCross()){
            mFilePath = "/DCIM/Camera"; // default path for IOS.
        }
        LogUtil.d(TAG,"mFilePath = "+mFilePath);
        return mFilePath;
    }



    public static boolean isDuplicate(String media, JSONObject jsonObject) {
        String filePath = Utils.getDecodedString((String) jsonObject.get("Path"));
        LogUtil.d(TAG,"Fatching "+media+" file path :"+filePath);

        Boolean checkFile = fileMap.get(filePath);
        if ( null!= checkFile ) {
            return checkFile.booleanValue();
        } else {
            createFileAndRegisterInHashMap(media, jsonObject);
            checkFile = fileMap.get(filePath);
            return checkFile.booleanValue();
        }

    }

    public static void resetVariables(){
        fileMap.clear();
       //fileMap = new HashMap<String, Boolean>();
    }
}
