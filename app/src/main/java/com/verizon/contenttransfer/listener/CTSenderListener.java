package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by duggipr on 9/9/2016.
 */
public class CTSenderListener   implements View.OnClickListener{

    private Activity activity;

    public CTSenderListener(Activity activity) {
        this.activity = activity;
    }


    @Override
    public void onClick(View v) {
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "CTSenderActivityScreen");
        }
        if(v == activity.findViewById(R.id.ct_cancel_txt)){

        }

    }
}
