package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.activity.CTSenderActivity;
import com.verizon.contenttransfer.activity.CTTransferInterruptActivity;
import com.verizon.contenttransfer.activity.P2PFinishActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.adapter.P2PContentListAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTSelectContentListener;
import com.verizon.contenttransfer.p2p.model.CollectionTaskVO;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTSelectContentView;

import java.util.List;

/**
 * Created by kommisu on 7/14/2016.
 */
public class CTSelectContentModel {

    private static final String TAG = CTSelectContentModel.class.getName();
    private static CTSelectContentModel instance;
    //public boolean isReadyToTransfer=false;
    public long totalPhotosBytes = 0L;
    public long totalAppsBytes = 0L;
    public long totalAppIconsBytes = 0L;
    public long totalMusicBytes = 0L;
    public long totalVideosBytes = 0L;
    private List<String> selectedMediaList;
    private int transferCount = 0;
    public long getTotalContactsBytes() {
        return totalContactsBytes;
    }

    public void setTotalContactsBytes(long totalContactsBytes) {
        this.totalContactsBytes = totalContactsBytes;
    }

    public long getTotalSMSBytes() {
        return totalSMSBytes;
    }

    public void setTotalSMSBytes(long totalSMSBytes) {
        this.totalSMSBytes = totalSMSBytes;
    }

    public long getTotalCalllogsBytes() {
        return totalCalllogsBytes;
    }

    public void setTotalCalllogsBytes(long totalCalllogsBytes) {
        this.totalCalllogsBytes = totalCalllogsBytes;
    }

    public long totalContactsBytes = 0L;
    public long totalSMSBytes = 0L;
    public long totalCalllogsBytes = 0L;
    public long totalCalendarBytes = 0L;
    public long totalDocumentsBytes = 0L;

    public long totalPasswordsBytes = 0L;
    private boolean isTransferBtnClicked  = false;
    private Dialog mediaCountZeroCustomDialogs;
    private Activity activity;

    public static CTSelectContentModel getInstance() {
        if (instance == null) {
            instance = new CTSelectContentModel();
        }
        return instance;
    }

    public void initModel(final Activity activity) {
        this.activity = activity;

        //STOP PEER DISCOVERY.
        WiFiDirectActivity.StopPeerDiscovery();
        enableTransferButton();

        updateMediaCountOnCreate();
    }
    private void updateMediaCountOnCreate(){
        String[] mediaArray = CTGlobal.getInstance().getMediaTypeArray();
        for(int i=0;i<mediaArray.length;i++) {
            CTSelectContentUtil.getInstance().updateMediaCount(mediaArray[i], !CollectionTaskVO.getInstance().isCollectionFinished(mediaArray[i]),activity);
            LogUtil.d(TAG,"Collection finished for media :"+mediaArray[i]);
        }
        CTSelectContentView.getInstance().callNotifyDataSetChanged();
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            String msg = intent.getStringExtra("MESSAGE");
            LogUtil.d(TAG, "Received message :" + msg);
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }

            if(msg.equals(VZTransferConstants.CALCULATE_DATA)){
                String mediatype = intent.getStringExtra("mediatype");
                LogUtil.d(TAG, "message received dismiss dialog");
                updateMediaCount(mediatype, false);
                LogUtil.d(TAG,"Tot media size ="+DataSpeedAnalyzer.getTotalSize());
                if (isTransferBtnClicked && CTSelectContentModel.getInstance().getSelectedMediaList() != null) {
                    if(CollectionTaskVO.getInstance().isCollectionFinished(CTSelectContentModel.getInstance().getSelectedMediaList())){
                        LogUtil.d(TAG, "dismiss Dialog calling 2");
                        dismissDialog();
                        proceedToTransfer();
                    }
                }
            }

            else if (msg.equals(VZTransferConstants.WIFIDIRECT_CONNECTION_DISCONNECTED)){
                cancelTransfer();
            } else if(null != msg && msg.equals(VZTransferConstants.BROADCAST_DATA_TRANSFER_INTERRUPTED)){
                LogUtil.d(TAG, "Connection interrupted.. closing sending side socket");
                SocketUtil.disconnectAllSocket();
            }
        }
    };

    private BroadcastReceiver transferFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        LogUtil.d(TAG, "One to many transfer finished broadcast receiver.");
        transferCount++;
        String hostReceived = intent.getStringExtra("host");
        String finishStatusMsg = intent.getStringExtra("finishstatusmsg");
        LogUtil.d(TAG, "transfer finished receiver - client exited before transfer start- host:" + hostReceived + " & Finish msg:" + finishStatusMsg);

        if(finishStatusMsg.equals(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER)
            || transferCount == SocketUtil.getConnectedClients().size()) {
            Intent senderIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), P2PFinishActivity.class);

            if(!Utils.isCTTransferSuccess()) {
                senderIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTTransferInterruptActivity.class);
            }
            senderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CTGlobal.getInstance().getContentTransferContext().startActivity(senderIntent);
            Utils.cleanUpOnTransferFinish();
        }
        }
    };
    private void updateMediaCount(String mediaType, boolean isCollecting) {
        CTSelectContentUtil.getInstance().updateMediaCount(mediaType, isCollecting,activity);
        CTSelectContentView.getInstance().callNotifyDataSetChanged();
    }

    private void dismissDialog() {
        try {
            if (DataSpeedAnalyzer.progressDialog != null && DataSpeedAnalyzer.progressDialog.isShowing()) {
                DataSpeedAnalyzer.progressDialog.dismiss();
                LogUtil.d(TAG,"Dismissed cal async dialog.");
            }
            DataSpeedAnalyzer.stopTimer();
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

    public void resetVariables(){
        totalPhotosBytes = 0L;
        totalAppsBytes = 0L;
        totalMusicBytes = 0L;
        totalVideosBytes = 0L;
        totalCalendarBytes = 0L;
        totalDocumentsBytes = 0L;
        totalCalllogsBytes = 0L;
        totalSMSBytes = 0L;
        totalContactsBytes = 0L;
        totalAppIconsBytes=0L;
        isTransferBtnClicked  = false;
        selectedMediaList = null;
        Utils.resetVariables();
    }
    public void cancelTransfer() {
        resetVariables();
        SocketUtil.disconnectAllSocket();
    }

    private void enableTransferButton() {
            CTSelectContentView.getInstance().enableTransferButton(true);
            CTSelectContentView.getInstance().getTransferInRedColor();
    }


    public void continueTransferProcess(List<String> selectedMediaList) {

        CollectionTaskVO.getInstance().cancelNonSelectedTask(selectedMediaList);
        CTSelectContentModel.getInstance().setSelectedMediaList(selectedMediaList);
        if(CollectionTaskVO.getInstance().isCollectionFinished(selectedMediaList)){
            proceedToTransfer();
        }else {
            isTransferBtnClicked = true;
            createCalcProgressDialogTimer();
        }
    }

    private void createCalcProgressDialogTimer() {
        LogUtil.d(TAG,"create call progress dialog timer.");
        String progressMsg = (DataSpeedAnalyzer.getProgressMessage() == "" ? activity.getString(R.string.calc_message) : DataSpeedAnalyzer.getProgressMessage());
        DataSpeedAnalyzer.showProgressDialog(progressMsg, activity);

        DataSpeedAnalyzer.calcTimer = null;
        DataSpeedAnalyzer.startCalcProgressDialogTimer(activity);
    }

    private void proceedToTransfer() {
        String zeroCountMediaName = CTSelectContentUtil.getInstance().getSelectedMediaNameWithZeroCount();
        if(zeroCountMediaName.equals(VZTransferConstants.NO_MEDIA_WITH_ZERO_COUNT_SELECTED)){
            transferData();
        }else {
            View.OnClickListener zeroMediaCountListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaCountZeroCustomDialogs.dismiss();
                    CTSelectContentView.getInstance().callNotifyDataSetChanged();
                }
            };
            mediaCountZeroCustomDialogs = CustomDialogs.createOneButtonDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.ct_media_count_zero_text), activity.getString(R.string.dialog_ok), zeroMediaCountListener);
        }
    }

    private void transferData() {
        long totalBytes = 0L;
        int totalFiles = 0;
        ContentSelectionVO contentSelectionVO = Utils.getContentSelection(VZTransferConstants.CONTACTS_STR);
        if (contentSelectionVO.getContentflag()) {
            totalBytes += CTSelectContentModel.getInstance().getTotalContactsBytes();
            totalFiles += 1;
        }
        contentSelectionVO = Utils.getContentSelection(VZTransferConstants.PHOTOS_STR);
        if (contentSelectionVO.getContentflag()) {
            totalBytes += CTSelectContentModel.getInstance().getTotalPhotosBytes();
            totalFiles += contentSelectionVO.getContentsize();
        }
        contentSelectionVO = Utils.getContentSelection(VZTransferConstants.VIDEOS_STR);
        if (contentSelectionVO.getContentflag()) {
            totalBytes += CTSelectContentModel.getInstance().getTotalVideosBytes();
            totalFiles += contentSelectionVO.getContentsize();
        }

        if(Utils.isSupportMusic()){
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.AUDIO_STR);
            if(contentSelectionVO.getContentflag()){
                totalBytes += CTSelectContentModel.getInstance().getTotalMusicBytes();
                totalFiles += contentSelectionVO.getContentsize();
            }
        }


        //if (!CTGlobal.getInstance().isCross()){
        if(Utils.isMediaSupported(VZTransferConstants.SMS_STR)){
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.SMS_STR);
            if (contentSelectionVO.getContentflag()) {
                totalBytes += CTSelectContentModel.getInstance().getTotalSMSBytes();
                totalFiles += 1;
            }
        }
        if(Utils.isMediaSupported(VZTransferConstants.CALLLOG_STR)) {
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.CALLLOG_STR);
            if (contentSelectionVO.getContentflag()) {
                totalBytes += CTSelectContentModel.getInstance().getTotalCalllogsBytes();
                totalFiles += 1;
            }
        }

        if (VZTransferConstants.SUPPORT_DOCS && Utils.isMediaSupported(VZTransferConstants.DOCUMENTS_STR)) {
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR);
            if (contentSelectionVO.getContentflag()) {
                totalBytes += CTSelectContentModel.getInstance().getTotalDocumentsBytes();
                totalFiles += contentSelectionVO.getContentsize();
            }
        }

       // }
        if(Utils.isMediaSupported(VZTransferConstants.CALENDAR_STR)) {
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.CALENDAR_STR);
            if (contentSelectionVO.getContentflag()) {
                totalBytes += CTSelectContentModel.getInstance().getTotalCalendarBytes();
                totalFiles += contentSelectionVO.getContentsize();
            }
        }
        if(Utils.isMediaSupported(VZTransferConstants.APPS_STR)) {
            contentSelectionVO = Utils.getContentSelection(VZTransferConstants.APPS_STR);
            if (contentSelectionVO.getContentflag()) {
                totalBytes += !CTGlobal.getInstance().isCross() ? (CTSelectContentModel.getInstance().getTotalAppsBytes()) : (CTSelectContentModel.getInstance().getTotalAppIconsBytes());
                totalFiles += contentSelectionVO.getContentsize();
            }
        }
        long availableSpace = CTGlobal.getInstance().getAvailableSpaceAtReceiverEnd();
        LogUtil.d(TAG, "availableSpace =" + availableSpace + " totalBytes =" + totalBytes);
        if(availableSpace > 0 && totalBytes > 0){
            if(CTGlobal.getInstance().isDoingOneToMany()){
                startSenderActivity(totalBytes, totalFiles);
            }else {
                if(availableSpace < (totalBytes + VZTransferConstants.RECEIVER_SIDE_BUFFER_BYTES)){
                    Intent storageIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTErrorMsgActivity.class);
                    storageIntent.putExtra("screen", VZTransferConstants.INSUFFICIENT_STORAGE);
                    storageIntent.putExtra("thingsOfBytes", String.valueOf(Utils.bytesToMeg(totalBytes)));
                    storageIntent.putExtra("availableBytes", String.valueOf(Utils.bytesToMeg(availableSpace)));
                    storageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CTGlobal.getInstance().getContentTransferContext().startActivity(storageIntent);
                }else {
                    startSenderActivity(totalBytes, totalFiles);
                }
            }

        }else {
            LogUtil.d(TAG, "No data to transfer.");
        }
    }

    private void startSenderActivity(long totalBytes, int totalFiles) {
        DataSpeedAnalyzer.setFileCounter(totalFiles);
        DataSpeedAnalyzer.setTotalsize(totalBytes);
        CTSelectContentView.getInstance().enableTransferButton(false);
        Intent intent1 = new Intent(activity, CTSenderActivity.class);
        activity.startActivity(intent1);
        CTSelectContentUtil.getInstance().setLogAnalytics();
    }

    public void handleOnResume(){
        LogUtil.d(TAG, "Register calcupdate broadcast receiver - isTransferBtnClicked :"+isTransferBtnClicked);
        if(isTransferBtnClicked && !CollectionTaskVO.getInstance().isCollectionFinished(CTSelectContentModel.getInstance().getSelectedMediaList())) {
            createCalcProgressDialogTimer();
        }

        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter("calcupdate"));
        LocalBroadcastManager.getInstance(activity).registerReceiver(transferFinishReceiver, new IntentFilter(VZTransferConstants.TRANSFER_FINISHED_MSG));
    }

    public void handleOnPause() {
        dismissDialog();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(transferFinishReceiver);

    }

    public void samSelectAllChangeText(boolean status,boolean selectTxtClicked) {
        if(!selectTxtClicked) {  //select each case
            int totConentCount = 0;
            int userHasContentCount=0;
            for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
                ContentSelectionVO cdata = (ContentSelectionVO) P2PContentListAdapter.contentselctionlist.get(i);
                if (cdata.getContentflag()) {
                    totConentCount++;
                }
                if(cdata.getContentsize() > 0 || cdata.getContentStorage().equals(activity.getString(R.string.ct_content_tv)))
                {
                    userHasContentCount++;
                }
            }
            if ( totConentCount == userHasContentCount
                    || totConentCount==0){
                if(status) {
                    samSelectAllChangeText(true, true);
                    CTSelectContentListener.getCtSelectContentListener().setCheckFlag(true);
                }
                else {
                    samSelectAllChangeText(false, true);
                }
            }
            else {  //more than totcontentCount clicked 1
                samSelectAllChangeText(false,true);  //change label
            }
        }
        else  //all  selection
        {
            if (status) {
                CTSelectContentView.getInstance().setSelectAllText(activity.getString(R.string.deselect_all));
            } else {
                CTSelectContentView.getInstance().setSelectAllText(activity.getString(R.string.select_all));
            }
        }
    }
    public long getTotalPhotosBytes() {
        return totalPhotosBytes;
    }

    public void setTotalPhotosBytes(long totalPhotosBytes) {
        this.totalPhotosBytes = totalPhotosBytes;
    }
    public long getTotalAppsBytes() {
        return totalAppsBytes;
    }

    public void setTotalAppsBytes(long totalAppsBytes) {
        this.totalAppsBytes = totalAppsBytes;
    }
    public long getTotalAppIconsBytes() {
        return totalAppIconsBytes;
    }

    public void setTotalAppIconsBytes(long totalAppIconsBytes) {
        this.totalAppIconsBytes = totalAppIconsBytes;
    }
    public long getTotalMusicBytes() {
        return totalMusicBytes;
    }

    public void setTotalMusicBytes(long totalMusicBytes) {
        this.totalMusicBytes = totalMusicBytes;
    }

    public long getTotalVideosBytes() {
        return totalVideosBytes;
    }

    public void setTotalVideosBytes(long totalVideosBytes) {
        this.totalVideosBytes = totalVideosBytes;
    }

    public long getTotalCalendarBytes() {
        return totalCalendarBytes;
    }

    public void setTotalCalendarBytes(long totalCalendarBytes) {
        this.totalCalendarBytes = totalCalendarBytes;
    }

    public long getTotalDocumentsBytes() {
        return totalDocumentsBytes;
    }

    public void setTotalDocumentsBytes(long totalDocumentsBytes) {
        this.totalDocumentsBytes = totalDocumentsBytes;
    }

    public long getTotalPasswordsBytes() {
        return totalPasswordsBytes;
    }

    public void setTotalPasswordsBytes(long totalPasswordsBytes) {
        this.totalPasswordsBytes = totalPasswordsBytes;
    }

/*    public boolean isReadyToTransfer() {
        return isReadyToTransfer;
    }*/

    public Activity getActivity() {
        return activity;
    }

    public List<String> getSelectedMediaList() {
        return selectedMediaList;
    }

    public void setSelectedMediaList(List<String> selectedMediaList) {
        this.selectedMediaList = selectedMediaList;
    }
}
