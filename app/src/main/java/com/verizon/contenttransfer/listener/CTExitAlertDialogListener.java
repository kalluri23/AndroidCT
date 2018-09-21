package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.content.DialogInterface;

import com.verizon.contenttransfer.base.ExitHandler;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.model.CTSenderModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by c0bissh on 3/22/2017.
 */
public class CTExitAlertDialogListener implements DialogInterface.OnClickListener {

    private Activity acvt;
    private String screenName;
    private static final String TAG = CTExitAlertDialogListener.class.getName();
    public CTExitAlertDialogListener(Activity act, String screenName) {
        this.acvt = act;
        this.screenName = screenName;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        LogUtil.d(TAG,"Which :"+which);
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                // int which = -2
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                // int which = -3
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                // int which = -1
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
                    P2PFinishUtil.getInstance().generateAppAnalyticsFile(null);
                }
                CTGlobal.getInstance().setExitApp(true);
                CTGlobal.getInstance().setManualSetup(false);
                //continueExit();

                //CustomDialogs.createDefaultProgressDialog(VZTransferConstants.PLEASE_WAIT, acvt, false);
                dialog.dismiss();
                ExitHandler.performExitTasks(acvt, screenName);
                break;
        }
    }

/*    private void continueExit() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //acvt.finish();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LogUtil.d(TAG,"Exception on continue exit function.");
                    e.printStackTrace();
                }


                LogUtil.d(TAG, "Dismissing exit dialog");
            }
        });
        thread.start();



    }*/
}
