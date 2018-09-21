package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by yempasu on 8/14/2017.
 */
public class CTSavingMediaListener implements View.OnClickListener {
    private Activity activity;

    public CTSavingMediaListener(Activity activity) {
        this.activity = activity;
    }


    @Override
    public void onClick(View v) {
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "CTSavingMedaiActivity");
        }

        /*if(v == activity.findViewById(R.id.ct_run_in_background_done_tv)){
            String statusMsg = activity.getIntent().getStringExtra("statusMsg");
            P2PFinishUtil.getInstance().completeTransferFinishProcess(statusMsg);
        }*/


    }
}
