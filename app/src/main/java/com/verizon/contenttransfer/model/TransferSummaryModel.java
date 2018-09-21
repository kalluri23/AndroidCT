package com.verizon.contenttransfer.model;

import android.app.Activity;

import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.TransferSummaryStatus;

import java.util.ArrayList;

/**
 * Created by kommisu on 7/13/2016.
 */
public class TransferSummaryModel {

    private Activity activity;
    private  ArrayList<TransferSummaryStatus> duplicatePhotoArray = new ArrayList<TransferSummaryStatus>();
    private  ArrayList<TransferSummaryStatus>  duplicateVideoArray = new ArrayList<TransferSummaryStatus>();
    private  ArrayList<TransferSummaryStatus>  duplicateMusicArray = new ArrayList<TransferSummaryStatus>();
    private  ArrayList<TransferSummaryStatus>  duplicateDocumentArray = new ArrayList<TransferSummaryStatus>();
    private  ArrayList<TransferSummaryStatus>  duplicateAppsArray = new ArrayList<TransferSummaryStatus>();

    private long getDuplicateVideoArraySize = 0;
    private long getDuplicatePhotoArraySize = 0;
    private long getDuplicateMusicArraySize = 0;
    private long getDuplicateDocumentArraySize = 0;
    private long getDuplicateAppsArraySize = 0;

    public long getGetDuplicateVideoArraySize() {
        return getDuplicateVideoArraySize;
    }

    public long getGetDuplicatePhotoArraySize() {
        return getDuplicatePhotoArraySize;
    }

    public long getGetDuplicateMusicArraySize() {
        return getDuplicateMusicArraySize;
    }

    public long getGetDuplicateDocumentArraySize() {
        return getDuplicateDocumentArraySize;
    }

    public long getGetDuplicateAppsArraySize() {
        return getDuplicateAppsArraySize;
    }

    private TransferSummaryModel() {
        //Not supported
    }

    public TransferSummaryModel(Activity activity) {
        this.activity = activity;
    }


    public ArrayList<TransferSummaryStatus> getDuplicatePhotoArray() {
        return duplicatePhotoArray;
    }

    public ArrayList<TransferSummaryStatus> getDuplicateVideoArray() {
        return duplicateVideoArray;
    }

    public ArrayList<TransferSummaryStatus> getDuplicateMusicArray() {
        return duplicateMusicArray;
    }
    public ArrayList<TransferSummaryStatus> getDuplicateDocumentArray() {
        return duplicateDocumentArray;
    }

    public ArrayList<TransferSummaryStatus> getDuplicateAppsArray() {
        return duplicateAppsArray;
    }
    public void initValue() {
        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
        duplicatePhotoArray = ctAnalyticUtil.notTransferredPhotoList;
        duplicateVideoArray = ctAnalyticUtil.notTransferredVideoList;
        duplicateMusicArray = ctAnalyticUtil.notTransferredMusicList;
        duplicateDocumentArray = ctAnalyticUtil.notTransferredDocumentList;
        duplicateAppsArray = ctAnalyticUtil.notTransferredAppsList;
        if(duplicatePhotoArray != null ){
            getDuplicatePhotoArraySize = duplicatePhotoArray.size();
        }
        if(duplicateVideoArray != null ){
            getDuplicateVideoArraySize = duplicateVideoArray.size();
        }
        if(duplicateMusicArray != null ){
            getDuplicateMusicArraySize = duplicateMusicArray.size();
        }
        if(duplicateDocumentArray != null ){
            getDuplicateDocumentArraySize = duplicateDocumentArray.size();
        }
        if(duplicateAppsArray != null ){
            getDuplicateAppsArraySize = duplicateAppsArray.size();
        }
    }

}
