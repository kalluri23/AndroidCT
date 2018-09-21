package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.adapter.P2PContentListAdapter;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTSelectContentView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahiahm on 7/22/2016.
 */
public class CTSelectContentListener implements View.OnClickListener {

    private static String TAG = CTSelectContentListener.class.getName();
    private static final String INFO = "Content Transfer";
    private Activity activity;
    private Dialog cancelTransferDialog;

    public static CTSelectContentListener getCtSelectContentListener() {
        return ctSelectContentListener;
    }

    private static CTSelectContentListener ctSelectContentListener;
    private boolean checkFlag = false;
    public CTSelectContentListener(final Activity act) {
        this.activity = act;
        ctSelectContentListener = this;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ct_select_all) {
            if ( ! checkFlag ) {
                checkFlag = true;
                CTSelectContentModel.getInstance().samSelectAllChangeText(true, true); // text will be deselect all
            } else {
                checkFlag = false;
                CTSelectContentModel.getInstance().samSelectAllChangeText(false, true);  //sang change to the select all text
            }
            if (checkFlag) {
                for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
                    ContentSelectionVO selectedItemVO = (ContentSelectionVO) P2PContentListAdapter.contentselctionlist.get(i);
                    if (selectedItemVO.getContentsize() > 0
                            || selectedItemVO.getContentStorage().equals(activity.getString(R.string.ct_content_tv))) {
                        selectedItemVO.setContentflag(true);
                    }
                }
            } else {
                for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
                    ContentSelectionVO selectedItemVO = (ContentSelectionVO) P2PContentListAdapter.contentselctionlist.get(i);
                    selectedItemVO.setContentflag(false);
                }
            }
            CTSelectContentView.getInstance().callNotifyDataSetChanged();
        }
        if(v.getId() == R.id.ct_select_content_sender_transfer_btn){
            //rahiahm - disabling the sender side call. Want to collect this from Reciever side - Launch Sender activity
            if (CTSelectContentUtil.getInstance().isAnyItemSelected()) {
                //Validate available device storage.

                if (!CTSelectContentUtil.getInstance().isAnyZeroFileSelected(activity)) {
                    List<String> selectedMediaList =new ArrayList<String>();
                    String[] mediaTypeArray =  CTGlobal.getInstance().getMediaTypeArray();

                    for(int i=0;i<mediaTypeArray.length;i++) {
                        if(mediaTypeArray[i].equals(VZTransferConstants.CONTACTS_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.CONTACTS_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.CONTACTS_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.PHOTOS_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.PHOTOS_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.PHOTOS_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.VIDEOS_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.VIDEOS_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.VIDEOS_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.AUDIO_STR)){
                            if(Utils.getContentSelection(VZTransferConstants.AUDIO_STR).getContentflag()){
                                selectedMediaList.add(VZTransferConstants.AUDIO_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.CALENDAR_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.CALENDAR_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.CALENDAR_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.SMS_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.SMS_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.SMS_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.CALLLOG_STR)){
                            if (Utils.getContentSelection(VZTransferConstants.CALLLOG_STR).getContentflag()) {
                                selectedMediaList.add(VZTransferConstants.CALLLOG_STR);
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.DOCUMENTS_STR)){
                            if(VZTransferConstants.SUPPORT_DOCS) {
                                if (Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR).getContentflag()) {
                                    selectedMediaList.add(VZTransferConstants.DOCUMENTS_STR);
                                }
                            }
                        } else if(mediaTypeArray[i].equals(VZTransferConstants.APPS_STR)){
                            if (VZTransferConstants.SUPPORT_APPS) {
                                if (Utils.getContentSelection(VZTransferConstants.APPS_STR).getContentflag()) {
                                    selectedMediaList.add(VZTransferConstants.APPS_STR);
                                }
                            }
                        }
                    }

                    CTSelectContentModel.getInstance().continueTransferProcess(selectedMediaList);

                }else{
                    CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                            activity.getString(R.string.file_with_zero_count_msg),
                            activity,
                            activity.getString(R.string.msg_ok), -1).show();
                }
            } else {
                CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                        activity.getString(R.string.selectoneormore),
                        activity,
                        activity.getString(R.string.msg_ok), -1).show();
            }
            if(Utils.isWifiDirectSupported()) {
                WiFiDirectActivity.StopPeerDiscovery();
            }
        }
        if(v.getId() == R.id.ct_select_content_sender_cancel_btn){

             cancelTransferDialog = CustomDialogs.createDialog(activity,  activity.getString(R.string.ct_dialog_exit_title), activity.getString(R.string.ct_dialog_exit_msg), true, null,
                    true,  activity.getString(R.string.ct_dialog_exit_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CTSelectContentUtil.getInstance().handleCancelTransfer();
                        }
                    },
                    true,activity.getString(R.string.cancel_button), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancelTransferDialog.dismiss();
                        }
                    });
        }
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "TransferWhatScreen");
        }
    }



    public void setCheckFlag(boolean flag) {
        checkFlag = flag;
    }
}
