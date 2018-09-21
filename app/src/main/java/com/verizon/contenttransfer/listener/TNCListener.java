package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by kommisu on 7/12/2016.
 */
public class TNCListener implements View.OnClickListener {

    private Activity activity;

    public TNCListener( Activity act ) {
        this.activity = act;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.acceptTncTV) {
            activity.finish();
        }
        else if (v.getId() == R.id.ctlayout_toolbar_ivBack) {
            ExitContentTransferDialog.showExitDialog(activity, "EULAScreen");
        }

    }

}
