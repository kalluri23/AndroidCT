package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;

/**
 * Created by rahiahm on 9/20/2016.
 */
public class CTTransferStatusListener implements View.OnClickListener {

    private Activity activity;
    public CTTransferStatusListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.search_icon
                || v.getId() == R.id.ct_toolbar_hamburger_menuIV
                || v.getId() == R.id.ct_toolbar_backIV) {
            ExitContentTransferDialog.showExitDialog(activity, "Transfer Summary Details screen");
        }

        if (v.getId() == R.id.ct_got_it_btn) {
            activity.finish();
        }

    }
}
