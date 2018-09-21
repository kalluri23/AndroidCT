package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.SenderProgressAdapterOneToMany;
import com.verizon.contenttransfer.listener.CTSenderListener;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/9/2016.
 */
public class CTSenderView {
    private static String TAG = CTSenderView.class.getName();
    private static Activity activity;
    private static CTSenderView instance;
    private SenderProgressAdapterOneToMany senderAdapterOneToMany;

    public CTSenderView(Activity activity) {
        this.activity = activity;
        instance = this;
        initView(activity);
        DataSpeedAnalyzer.displaySpeedDialog(activity);
    }

    public static Activity getActivity() {
        return activity;
    }
    public static CTSenderView getInstance(){
        return instance;
    }

    private void initView(Activity activity) {

        if(!CTGlobal.getInstance().isDoingOneToMany()){
            activity.setContentView(R.layout.ct_progress_sender_layout);}
        else{
            activity.setContentView(R.layout.ct_progress_sender_onetomany);
            TextView summary = (TextView) activity.findViewById(R.id.summary);
            summary.setText(DataSpeedAnalyzer.getFileCounter()+activity.getString(R.string.files_with_size)+ Utils.bytesToMeg(DataSpeedAnalyzer.getTotalSize()) +" MB");
            ListView listView = (ListView) activity.findViewById(R.id.ct_connected_one_to_many_devices_progress_lv);
            senderAdapterOneToMany = new SenderProgressAdapterOneToMany(activity, R.layout.ct_progress_sender_onetomany_cell, ServerConnectionObject.getInstance().getClients());
            listView.setAdapter(senderAdapterOneToMany);
        }

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer);

        View.OnClickListener buttonListener = new CTSenderListener(activity);

        activity.findViewById(R.id.search_icon).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(buttonListener);

    }

    public void updateMediaProgressStatus(String timeLeftStatus,
                                          String dataReceived,
                                          String speed,
                                          String receivingMediaName,
                                          String receivingMediaStatus,
                                          int receivingMediaProgress){
        TextView timeLeftStatusTV = (TextView) activity.findViewById(R.id.ct_progress_send_tv_time_left_status);
        TextView dataReceivedTV = (TextView) activity.findViewById(R.id.ct_progress_send_tv_received_status);
        TextView speedTV = (TextView) activity.findViewById(R.id.ct_progress_send_tv_speed_status);
        TextView sendingMediaStatusTV = (TextView) activity.findViewById(R.id.ct_progress_send_tv_send_contacts_status);
        TextView sendingMediaNameTV = (TextView) activity.findViewById(R.id.ct_progress_send_tv_send_contacts);
        ProgressBar sendingMediaProgressPB = (ProgressBar) activity.findViewById(R.id.ct_sender_loadingPB);


        if(null != sendingMediaStatusTV){
            timeLeftStatusTV .setText(timeLeftStatus);
            dataReceivedTV.setText(dataReceived);
            speedTV.setText(speed);

            sendingMediaNameTV.setText(activity.getString(R.string.sending_to) + receivingMediaName);
            sendingMediaStatusTV.setText(receivingMediaStatus);
            sendingMediaProgressPB.setProgress(receivingMediaProgress);
        }
    }
    public void updateOneToManyMediaProgressStatus(){
        if(null != senderAdapterOneToMany) {
            senderAdapterOneToMany.notifyDataSetChanged();
        }
    }
}
