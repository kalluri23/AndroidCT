package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CaptureListenerCT implements View.OnClickListener {

    private Activity activity;
    private static final String INFO = "Content Transfer";
    private Dialog manualSetupDialog;
    public CaptureListenerCT(Activity act){
        this.activity = act;
    }
    @Override
    public void onClick(View view) {

        if(view.getId() == (R.id.search_icon)
                || view.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || view.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "Capture Activity");
        }

        if(view.getId() == R.id.ct_manual_btn){
            manualSetupDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.manual_setup_dialog_text), true, null,
                    true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
/*                            manualSetupDialog.dismiss();
                            VZTransferConstants.SUPPORT_QR = false;
                            CleanUpSockets.cleanUpAllVariables();
                            activity.finish();
                            CTGlobal.getInstance().setExitApp(true);*/

                            Utils.manualSetupExit(activity, manualSetupDialog);
                        }
                    },
                    true, activity.getString(R.string.cancel_button), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manualSetupDialog.dismiss();
                        }
                    });

        }
/*         if(view.getId() == R.id.ct_info_iv) {

            PackageInfo pInfo;
            String space = "            ";
            String aboutInfo = "";

            aboutInfo += space + "Build Version: " + CTGlobal.getInstance().getBuildVersion();
            aboutInfo += "\n";
            aboutInfo += space + "Date: " + CTGlobal.getInstance().getBuildDate();

            CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                    aboutInfo,
                    activity,
                    activity.getString(R.string.msg_ok), -1).show();
        }*/



    }
}
