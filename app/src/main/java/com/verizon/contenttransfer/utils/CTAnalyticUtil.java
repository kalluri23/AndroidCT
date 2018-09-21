package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.base.VZTransferConstants;

import java.util.ArrayList;

/**
 * Created by duggipr on 5/27/2016.
 */
public class CTAnalyticUtil {

    private static String TAG = CTAnalyticUtil.class.getName();
    private int contactsCount=0;
    private int photoCount=0;
    private int appCount=0;
    private int passwordsCount=0;
    private int videoCount=0;
    private int smsCount=0;
    private int musicCount=0;
    private int callLogCount=0;
    private int docCount=0;
    private int calendarCount=0;
    private int duplicateContactsCount=0;
    private int duplicatePhotoCount=0;
    private int duplicateVideoCount=0;
    private int duplicateSmsCount=0;
    private int duplicateMusicCount=0;
    private int duplicateCallLogCount=0;
    private int duplicateDocCount=0;
    private int duplicateCalendarCount=0;
    private String description = "";
    private String transferDuration = VZTransferConstants.DEFAULT_TRANSFER_DURATION;
    private String dataTransferStatusMsg = "";
    private long transferredBytes = 0;
    private long totDuplicateBytesTransferred = 0;
    private long totFailedBytesTransferred = 0;
    private long fileTransferredCount = 0L;
    private boolean vztransferCancelled = false;
    private boolean isCross = false;
    private int failedPhotoCount=0;
    private int failedVideoCount=0;
    private int failedMusicCount=0;
    private int failedDocCount=0;
    private int failedAppCount=0;
    private int failedPasswordsCount=0;
    private int failedCalendarCount=0;

    public ArrayList<TransferSummaryStatus> notTransferredPhotoList = new ArrayList<TransferSummaryStatus>();
    public ArrayList<TransferSummaryStatus> notTransferredVideoList = new ArrayList<TransferSummaryStatus>();
    public ArrayList<TransferSummaryStatus> notTransferredMusicList = new ArrayList<TransferSummaryStatus>();
    public ArrayList<TransferSummaryStatus> notTransferredDocumentList = new ArrayList<TransferSummaryStatus>();

    public ArrayList<TransferSummaryStatus> notTransferredAppsList = new ArrayList<TransferSummaryStatus>();
    public CTAnalyticUtil(){

    }
    public boolean isCross() {
        return isCross;
    }

    public void setIsCross(boolean isCross) {
        this.isCross = isCross;
    }


    public int getPasswordsCount() {
        return passwordsCount;
    }

    public void setPasswordsCount(int passwordsCount) {
        this.passwordsCount = passwordsCount;
    }

    public void addPasswordsCount(int passwordsCount) {
        this.passwordsCount += passwordsCount;
    }

    public int getFailedPasswordsCount() {
        return failedPasswordsCount;
    }

    public void setFailedPasswordsCount(int failedPasswordsCount) {
        this.failedPasswordsCount = failedPasswordsCount;
    }

    public void addFailedPasswordsCount(int failedPasswordsCount) {
        this.failedPasswordsCount += failedPasswordsCount;
    }

    public int getFailedCalendarCount() {
        return failedCalendarCount;
    }

    public void addFailedCalendarCount(int failedCalendarCount) {
        this.failedCalendarCount = failedCalendarCount;
    }
    public int getFailedPhotoCount() {
        return failedPhotoCount;
    }

    public void addFailedPhotoCount(int failedPhotoCount) {
        this.failedPhotoCount += failedPhotoCount;
    }

    public int getFailedVideoCount() {
        return failedVideoCount;
    }

    public void addFailedVideoCount(int failedVideoCount) {
        this.failedVideoCount += failedVideoCount;
    }

    public int getFailedMusicCount() {
        return failedMusicCount;
    }

    public void addFailedMusicCount(int failedMusicCount) {
        this.failedMusicCount += failedMusicCount;
    }

    public int getFailedDocCount() {
        return failedDocCount;
    }

    public void addFailedDocCount(int failedDocCount) {
        this.failedDocCount += failedDocCount;
    }


    public int getFailedAppCount() {
        return failedAppCount;
    }

    public void addFailedAppCount(int failedAppCount) {
        this.failedAppCount += failedAppCount;
    }
    public long getTotFailedBytesTransferred() {
        return totFailedBytesTransferred;
    }

    public void setTotFailedBytesTransferred(long totFailedBytesTransferred) {
        this.totFailedBytesTransferred = totFailedBytesTransferred;
    }
    public long getTotDuplicateBytesTransferred() {
        return totDuplicateBytesTransferred;
    }

    public void setTotDuplicateBytesTransferred(long totDuplicateBytesTransferred) {
        this.totDuplicateBytesTransferred = totDuplicateBytesTransferred;
    }
    /*
     *  transferredBytes - has actual transferred and duplicate transferred bytes.
     *  Also duplicate bytes saved separately in 'totDuplicateBytesTransferred' to find the actual transferred bytes.
     */
    public long getTransferredBytes() {
        return transferredBytes;
    }

    public long getOnlyTransferredBytes() {
        return getTransferredBytes() - getTotFailedBytesTransferred() - getTotDuplicateBytesTransferred();
    }

    public void setTransferredBytes(long transferredBytes) {
        this.transferredBytes = transferredBytes;
    }
    public long getFileTransferredCount() {
        long totFiles = fileTransferredCount +
                getPhotoCount() +
                getDuplicatePhotoCount() +
                getVideoCount() +
                getDuplicateVideoCount() +
                getMusicCount() +
                getDuplicateMusicCount() +
                getAppCount() +
                getPasswordsCount() +
                getDocCount() +
                getDuplicateDocCount() +
                getCalendarCount();
        return totFiles;
    }
    public long getTotalFailedCount() {
        long totFiles = getFailedAppCount() +
                getFailedPasswordsCount() +
                getFailedDocCount() +
                getFailedMusicCount() +
                getFailedPhotoCount() +
                getFailedCalendarCount() +
                getFailedVideoCount();

        return totFiles;
    }
    public ArrayList<TransferSummaryStatus> getNotTransferredPhotoList() {
        return notTransferredPhotoList;
    }

    public ArrayList<TransferSummaryStatus> getNotTransferredVideoList() {
        return notTransferredVideoList;
    }

    public ArrayList<TransferSummaryStatus> getNotTransferredMusicList() {
        return notTransferredMusicList;
    }

    public ArrayList<TransferSummaryStatus> getNotTransferredDocumentList() {
        return notTransferredDocumentList;
    }

    public ArrayList<TransferSummaryStatus> getNotTransferredAppsList() {
        return notTransferredAppsList;
    }

    public void setFileTransferredCount(long fileTransferredCount) {
        this.fileTransferredCount = fileTransferredCount;
    }
    public void addFileTransferredCount(int count) {
        this.fileTransferredCount += count;
    }
    public String getDataTransferStatusMsg() {
        return dataTransferStatusMsg;
    }

    public void setDataTransferStatusMsg(String dataTransferStatusMsg) {
        this.dataTransferStatusMsg = dataTransferStatusMsg;
    }

    public int getDuplicateContactsCount() {
        return duplicateContactsCount;
    }

    public void setDuplicateContactsCount(int duplicateContactsCount) {
        this.duplicateContactsCount = duplicateContactsCount;
    }

    public int getDuplicatePhotoCount() {
        return duplicatePhotoCount;
    }

    public void setDuplicatePhotoCount(int duplicatePhotoCount) {
        this.duplicatePhotoCount = duplicatePhotoCount;
    }

    public void addDuplicatePhotoCount(int count) {
        this.duplicatePhotoCount += count;
    }

    public int getDuplicateVideoCount() {
        return duplicateVideoCount;
    }

    public void setDuplicateVideoCount(int duplicateVideoCount) {
        this.duplicateVideoCount = duplicateVideoCount;
    }
    public void addDuplicateVideoCount(int count) {
        this.duplicateVideoCount += count;
    }

    public int getDuplicateSmsCount() {
        return duplicateSmsCount;
    }

    public void setDuplicateSmsCount(int duplicateSmsCount) {
        this.duplicateSmsCount = duplicateSmsCount;
    }
    public void addDuplicateSmsCount(int count) {
        this.duplicateSmsCount += count;
    }
    public int getDuplicateMusicCount() {
        return duplicateMusicCount;
    }

    public void setDuplicateMusicCount(int duplicateMusicCount) {
        this.duplicateMusicCount = duplicateMusicCount;
    }
    public void addDuplicateMusicCount(int count) {
        this.duplicateMusicCount += count;
    }
    public int getDuplicateCallLogCount() {
        return duplicateCallLogCount;
    }

    public void setDuplicateCallLogCount(int duplicateCallLogCount) {
        this.duplicateCallLogCount = duplicateCallLogCount;
    }
    public void addDuplicateCallLogCount(int count) {
        this.duplicateCallLogCount += count;
    }
    public int getDuplicateDocCount() {
        return duplicateDocCount;
    }

    public void setDuplicateDocCount(int duplicateDocCount) {
        this.duplicateDocCount = duplicateDocCount;
    }
    public void addDuplicateDocCount(int count) {
        this.duplicateDocCount += count;
    }
    public int getDuplicateCalendarCount() {
        return duplicateCalendarCount;
    }

    public void setDuplicateCalendarCount(int duplicateCalendarCount) {
        this.duplicateCalendarCount = duplicateCalendarCount;
    }
    public void addDuplicateCalendarCount(int count) {
        this.duplicateCalendarCount += count;
    }
    public int getContactsCount() {
        return contactsCount;
    }

    public void setContactsCount(int contactsCount) {
        this.contactsCount = contactsCount;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public void addPhotoCount(int count) {
        this.photoCount += count;
    }

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int photoCount) {
        this.appCount = appCount;
    }

    public void addAppCount(int count) {
        this.appCount += count;
    }


    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }
    public void addVideoCount(int count) {
        this.videoCount += count;
    }
    public int getSmsCount() {
        return smsCount;
    }

    public void setSmsCount(int smsCount) {
        this.smsCount = smsCount;
    }
    public void addSmsCount(int count) {
        this.smsCount += count;
    }
    public int getMusicCount() {
        return musicCount;
    }

    public void setMusicCount(int musicCount) {
        this.musicCount = musicCount;
    }

    public void addMusicCount(int count) {
        this.musicCount += count;
    }

    public int getCallLogCount() {
        return callLogCount;
    }

    public void setCallLogCount(int callLogCount) {
        this.callLogCount = callLogCount;
    }
    public void addCallLogCount(int count) {
        this.callLogCount += count;
    }
    public int getDocCount() {
        return docCount;
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }
    public void addDocCount(int count) {
        this.docCount += count;
    }
    public int getCalendarCount() {
        return calendarCount;
    }

    public void setCalendarCount(int calendarCount) {
        this.calendarCount = calendarCount;
    }
    public void addCalendarCount(int count) {
        this.calendarCount += count;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getTransferDuration() {
        return transferDuration;
    }

    public void setTransferDuration(long transferDuration) {
        this.transferDuration = String.valueOf(transferDuration);
    }
    public boolean isVztransferCancelled() {
        return vztransferCancelled;
    }

    public void setVztransferCancelled(boolean vztransferCancelled) {
        this.vztransferCancelled = vztransferCancelled;
    }
    public int getProgressUpdate(){
        int receivingMediaProgress = 1;
        if(DataSpeedAnalyzer.getTotalSize() > 0) {
            long totalDownloaded = getTransferredBytes();
            if (totalDownloaded > DataSpeedAnalyzer.getTotalSize()) {
                totalDownloaded = DataSpeedAnalyzer.getTotalSize();
            }
            receivingMediaProgress = (int) ((totalDownloaded*100)/DataSpeedAnalyzer.getTotalSize());
        } else {
            receivingMediaProgress = 1;
        }
        return receivingMediaProgress;
    }
    public void showAnalytic(){
        LogUtil.d(TAG,
                "contactsCount = "+contactsCount+
                "photoCount = "+photoCount+
                "appCount = "+appCount+
                "passwordsCount = "+passwordsCount+
                "videoCount = "+videoCount+
                "smsCount = "+smsCount+
                "musicCount = "+musicCount+
                "callLogCount = "+callLogCount+
                "docCount = "+docCount+
                "calendarCount = "+calendarCount+
                "duplicateContactsCount = "+duplicateContactsCount+
                "duplicatePhotoCount = "+duplicatePhotoCount+
                "duplicateVideoCount = "+duplicateVideoCount+
                "duplicateSmsCount = "+duplicateSmsCount+
                "duplicateMusicCount = "+duplicateMusicCount+
                "duplicateCallLogCount = "+duplicateCallLogCount+
                "duplicateDocCount = "+duplicateDocCount+
                "duplicateCalendarCount = "+duplicateCalendarCount+
                "description = "+description+
                "tot BytesTransfered = "+ transferredBytes +
                "tot DuplicateBytesTransfered = "+ totDuplicateBytesTransferred +
                "tot FailedBytesTransfered = "+ totFailedBytesTransferred +
                "transferDuration = "+transferDuration+
                "dataTransferStatusMsg = "+dataTransferStatusMsg);
    }
}
