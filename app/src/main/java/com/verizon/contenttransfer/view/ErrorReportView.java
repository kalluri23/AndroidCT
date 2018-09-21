package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.ErrorReportListener;
import com.verizon.contenttransfer.utils.LogUtil;

/**
 * Created by kommisu on 7/12/2016.
 */
public class ErrorReportView {
    public  static final  String TAG = ErrorReportView.class.getName();
    public static void initView(final Activity activity ) {

        View.OnClickListener buttonListener = new ErrorReportListener( activity );
        String data = activity.getIntent().getStringExtra("ERROR_DATA");
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(VZTransferConstants.CONTENT_TRANSFER_STOPPED));
        TextView tv = (TextView)activity.findViewById(R.id.layout_dialog_tvRightLink);
        tv.setOnClickListener(buttonListener);
        LogUtil.d(TAG, "Error:" + data);

        /*Test to view the crash on UI
        *
        */

        //TextView tv1 = (TextView)activity.findViewById(R.id.layout_dialog_tvMsg);
       // tv1.setText(data);

    }
}
