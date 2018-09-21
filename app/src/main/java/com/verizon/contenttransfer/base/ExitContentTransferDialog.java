package com.verizon.contenttransfer.base;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.model.CTSenderModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 3/17/2016.
 */
public class ExitContentTransferDialog {

    private static final String TAG = ExitContentTransferDialog.class.getName();
    private static Dialog exitDialog;
    private static Dialog mvmExit;

    public static void showExitDialog(final Activity acvt, final String screenName){

        mvmExit= CustomDialogs.createDialog(acvt, acvt.getString(R.string.dialog_title),acvt.getString(R.string.first_page_move_dialog) , false, null, true,acvt.getString(R.string.btnYes), new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                LogUtil.d(TAG,"Clicked on Yes");
                String localClassName = acvt.getLocalClassName();
                LogUtil.d(TAG, "localClassName =" + localClassName);
                if (!CTGlobal.getInstance().getApplicationStatus().contains(VZTransferConstants.CT_TRANSFER_FINISH)) {
                    LogUtil.d(TAG,"logging MVM exit analytic - selected phone="+CTGlobal.getInstance().getPhoneSelection());
                    // Write comm message.
                    if(CTGlobal.getInstance().getApplicationStatus().contains(VZTransferConstants.CT_CONNECTION_ESTABLISHED)
                            && !CTGlobal.getInstance().getApplicationStatus().contains(VZTransferConstants.CT_TRANSFER_FINISH)) {
                        if(Utils.isReceiverDevice()) {
                            LogUtil.d(TAG,"logging MVM exit analytic - app status="+CTGlobal.getInstance().getApplicationStatus());
                            CTReceiverModel.getInstance().cancelConnection();
                        }else {
                            LogUtil.d(TAG, "logging MVM exit analytic - old phone");
                            if(localClassName.contains(VZTransferConstants.iOSTransferActivity)){
                                CTSelectContentUtil.getInstance().handleCancelTransfer();
                            }else {
                                CTSenderModel.getInstance().cancelConnectionOnMVMExit();
                            }
                        }
                    }

                    Utils.setCtAnalyticUtil(localClassName,VZTransferConstants.MF_MVM_EXIT);
                    String desc=VZTransferConstants.MF_MVM_EXIT + ">" + screenName;
                    P2PFinishUtil.getInstance().generateAppAnalyticsFile(null,desc);
                }
                CTGlobal.getInstance().setExitApp(true);
                CTGlobal.getInstance().setManualSetup(false);
                //continueExit();
                //CustomDialogs.createDefaultProgressDialog(VZTransferConstants.PLEASE_WAIT, acvt, false);
                mvmExit.dismiss();
                ExitHandler.performExitTasks(acvt, screenName);
                }
             },true,acvt.getString(R.string.btnNo),new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG,"Clicked on No");
                        mvmExit.dismiss();
                    }
        });


    }
    public static void alertToExitDialog(final Activity activity, final String titleText, final String msg, final String btnText) {

        exitDialog=CustomDialogs.createOneButtonDialog(activity, titleText, msg, btnText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "msg =" + msg);
                CTGlobal.getInstance().setExitApp(true);
                exitDialog.cancel();
                activity.finish();
            }
        });

    }
}
