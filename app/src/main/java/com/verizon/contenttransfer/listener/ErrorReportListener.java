package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.model.ErrorReportModel;
import com.verizon.contenttransfer.utils.CTGlobal;

/**
 * Created by kommisu on 7/12/2016.
 */
public class ErrorReportListener implements View.OnClickListener {

    private Activity activity;

    public  static final  String TAG = ErrorReportListener.class.getName();

    public ErrorReportListener(Activity act) {
        this.activity = act;
        new ErrorReportModel(act);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_dialog_tvRightLink) {
            CTGlobal.getInstance().setExitApp(true);
            System.exit(0);
        }

    }

}
