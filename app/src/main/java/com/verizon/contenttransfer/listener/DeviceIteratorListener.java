package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by kommisu on 7/12/2016.
 */
public class DeviceIteratorListener implements View.OnClickListener {

    private Activity activity;

    public DeviceIteratorListener(Activity act) {
        this.activity = act;

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "PairingScreen");
        }
    }

}
