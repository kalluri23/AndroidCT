package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.utils.CustomDialogs;

/**
 * Created by rahiahm on 9/8/2016.
 */
public class CTReceiverListener implements View.OnClickListener {

    private Activity activity;
    private Dialog cancelTransferDialog;
    public CTReceiverListener(Activity act){
        this.activity = act;
    }

    @Override
    public void onClick(View v) {
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "ReceiveDataScreen");
        }
        if(v == activity.findViewById(R.id.ct_cancel_txt)){

            cancelTransferDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.ct_dialog_exit_title), activity.getString(R.string.ct_dialog_exit_msg), true, null,
                    true, activity.getString(R.string.ct_dialog_exit_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CTReceiverModel.getInstance().cancelConnection();
                        }
                    },
                    true, activity.getString(R.string.cancel_button), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancelTransferDialog.dismiss();
                        }
                    });
        }

    }
}
