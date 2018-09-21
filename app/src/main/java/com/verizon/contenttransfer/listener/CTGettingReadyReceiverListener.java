package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by c0bissh on 9/15/2016.
 */
public class CTGettingReadyReceiverListener implements View.OnClickListener{
    private Activity activity;

    public CTGettingReadyReceiverListener(Activity act){
        this.activity = act;

    }

    @Override
    public void onClick(View v) {
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "GettingReadyReceiveDataScreen");
        }
    }
}
