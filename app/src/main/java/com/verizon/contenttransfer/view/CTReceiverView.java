package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.listener.CTReceiverListener;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/8/2016.
 */
public class CTReceiverView {

    private Activity activity;
    private static CTReceiverView instance;
    private static final String TAG = CTReceiverView.class.getName();
    public CTReceiverView(Activity activity){
        this.activity = activity;
        instance = this;
        initView(activity);
        DataSpeedAnalyzer.displaySpeedDialog(activity);
    }

    public static CTReceiverView getInstance(){
        return instance;
    }

    private void initView(Activity activity){
        CTErrorReporter.getInstance().Init(activity);
        activity.setContentView(R.layout.ct_progress_receiver_layout);

        View.OnClickListener buttonListener = new CTReceiverListener(activity);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer);

        activity.findViewById(R.id.search_icon).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_cancel_txt).setOnClickListener(buttonListener);
    }


    public void updateStatusText(String msg){
        LogUtil.d(TAG," update status text ="+msg);

    }

    public void updateReceiverStatus(String msg){
        LogUtil.d(TAG, " update receiver status text =" + msg);
    }

    public void updateMediaProgressStatus(String timeLeftStatus,
                                          String dataReceived,
                                          String speed,
                                          String receivingMediaName,
                                          String receivingMediaStatus,
                                          int receivingMediaProgress){
        TextView timeLeftStatusTV = (TextView) activity.findViewById(R.id.ct_progress_rec_tv_time_left_status);
        TextView dataReceivedTV = (TextView) activity.findViewById(R.id.ct_progress_rec_tv_received_status);
        TextView speedTV = (TextView) activity.findViewById(R.id.ct_progress_rec_tv_speed_status);
        TextView receivingMediaStatusTV = (TextView) activity.findViewById(R.id.ct_progress_rec_tv_send_contacts_status);
        TextView receivingMediaNameTV = (TextView) activity.findViewById(R.id.ct_progress_rec_tv_send_contacts);
        ProgressBar receivingMediaProgressPB = (ProgressBar) activity.findViewById(R.id.ct_receiver_loadingPB);


        if(null != timeLeftStatusTV){
            timeLeftStatusTV.setText(timeLeftStatus);
            dataReceivedTV.setText(dataReceived);
            speedTV.setText(speed);

            receivingMediaNameTV.setText(activity.getString(R.string.receiving) + receivingMediaName);
            receivingMediaStatusTV.setText(receivingMediaStatus);
            receivingMediaProgressPB.setProgress(receivingMediaProgress);
        }
    }
}
