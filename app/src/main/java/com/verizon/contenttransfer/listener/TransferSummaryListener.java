package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.utils.LogUtil;

/**
 * Created by c0bissh on 9/20/2016.
 */
public class TransferSummaryListener implements View.OnClickListener {
    private Activity activity;

    public TransferSummaryListener(Activity act){
        this.activity = act;
    }

    @Override
    public void onClick(View v) {
        LogUtil.d("VIEW", "v click listener.."+v.toString()+"   v="+v);

        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "TransferSummaryScreen");
        }
        if(v == activity.findViewById(R.id.ct_tr_summ_done_tv)){
            activity.finish();
        }
        if(v == activity.findViewById(R.id.ct_tr_summ_desc)){

            TextView totTime = (TextView) activity.findViewById(R.id.ct_tr_tot_time);
            TextView avgSpeed = (TextView) activity.findViewById(R.id.ct_tr_avg_speed);
            if(totTime.getVisibility() != View.VISIBLE){
                totTime.setVisibility(View.VISIBLE);
                avgSpeed.setVisibility(View.VISIBLE);

            }else{
                totTime.setVisibility(View.GONE);
                avgSpeed.setVisibility(View.GONE);
            }
        }

    }
}