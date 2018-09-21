package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/8/2016.
 */
public class CTReceiverPinListener implements View.OnClickListener{
    private static String TAG = CTReceiverPinListener.class.getName();
    private Activity activity;

    public CTReceiverPinListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.ctlayout_toolbar_ivBack){
            ExitContentTransferDialog.showExitDialog(activity, "DisplayPinScreen");
        }

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "ShowPinScreen");
        }
        if(v.getId() == R.id.ct_one_to_many_next_btn){
            Utils.oneToManyNextButton(activity);
        }
    }
}
