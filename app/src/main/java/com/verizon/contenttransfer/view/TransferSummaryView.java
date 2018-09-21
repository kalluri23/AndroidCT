package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.ContentRecapAdapter;
import com.verizon.contenttransfer.adapter.ContentRecapAdapterOneToMany;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.TransferSummaryListener;
import com.verizon.contenttransfer.model.TransferSummaryModel;
import com.verizon.contenttransfer.p2p.model.ContentRecapVO;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.util.List;

/**
 * Created by kommisu on 7/12/2016.
 */
public class TransferSummaryView {
    private  static final  String TAG = TransferSummaryView.class.getName();
    private Activity activity = null;
    private TransferSummaryModel transferSummaryModel = null;
    private ContentRecapAdapterOneToMany recapAdapterOneToMany;

    public void initView(final Activity act ) {
        this.activity = act;
        View.OnClickListener transferSummaryListener = new TransferSummaryListener(activity);
        if(Utils.isReceiverDevice()|| !CTGlobal.getInstance().isDoingOneToMany()){
            activity.setContentView(R.layout.ct_transfer_summary_layout);
            transferSummaryModel = new TransferSummaryModel(activity);
            transferSummaryModel.initValue();

            String mediaType = activity.getIntent().getStringExtra("mediaType");
            LogUtil.d(TAG, "Transfer summary - media type =" + mediaType);

            activity.findViewById(R.id.ct_tr_summ_desc).setOnClickListener(transferSummaryListener);
            LogUtil.d(TAG, "P2PFinishUtil.getInstance().getTotalPayload()...=" + P2PFinishUtil.getInstance().getTotalPayload());

            TextView totTimeTV = (TextView) activity.findViewById(R.id.ct_tr_tot_time);
            String timeStr = activity.getApplicationContext().getString(R.string.TOTAL_TIME) +P2PFinishUtil.getInstance().getTotalTime();
            totTimeTV.setText(timeStr);

            TextView avgSpeedTV = (TextView) activity.findViewById(R.id.ct_tr_avg_speed);
            String avgSpeed = activity.getApplicationContext().getString(R.string.AVERAGE_SPEED)+P2PFinishUtil.getInstance().getAvgSpeed()+ VZTransferConstants.MBPS;
            avgSpeedTV.setText(avgSpeed);

            ListView contentContainer = (ListView) activity.findViewById(R.id.ct_tr_summ_content_container);
            List<ContentRecapVO> listOfDevice = P2PFinishUtil.getInstance().getContentRecapVOs();
            LogUtil.d(TAG, "listOfDevice =" + listOfDevice);

            TextView summTV = (TextView) activity.findViewById(R.id.ct_tr_summ_desc);
            String summDesc = setTransferRecapStatus();
            if(listOfDevice.size()==0){
                summTV.setText(R.string.no_media_transferred_msg);
            }else {
                summTV.setText(summDesc);
                ContentRecapAdapter contentRecapAdapter = new ContentRecapAdapter(activity, R.id.ct_tr_summ_content_container, listOfDevice);
                contentContainer.setAdapter(contentRecapAdapter);
            }
        }else{
            activity.setContentView(R.layout.ct_transfer_summary_layout_onetomany);
            ListView listView = (ListView) activity.findViewById(R.id.ct_tr_summ_content_container_lv);
            TextView summary=(TextView) activity.findViewById(R.id.summary);
            recapAdapterOneToMany = new ContentRecapAdapterOneToMany(activity, R.layout.ct_transfer_summary_cell_onetomany, ServerConnectionObject.getInstance().getClients());
            listView.setAdapter(recapAdapterOneToMany);
            summary.setText(DataSpeedAnalyzer.getFileCounter()+activity.getString(R.string.files_with_size)+ Utils.bytesToMeg(DataSpeedAnalyzer.getTotalSize()) +" MB");
        }

        handleHeaderButtons(activity, transferSummaryListener);
        activity.findViewById(R.id.ct_tr_summ_done_tv).setOnClickListener(transferSummaryListener);
        activity.findViewById(R.id.ct_transfer_btn).setOnClickListener(transferSummaryListener);
    }

    @NonNull
    private String setTransferRecapStatus() {
        Intent intent = activity.getIntent();
        String finishStatus  = intent.getStringExtra("message");
        String summDesc = "";
        boolean useTransferredBytes=false;
        //if condition --Excluding out the sender one-to-many scenario, we have a different view.
        if(!CTGlobal.getInstance().isDoingOneToMany() || Utils.isReceiverDevice()) {
            List<ContentRecapVO> contentRecapVOList=P2PFinishUtil.getInstance().getContentRecapVOs();
            if(contentRecapVOList!=null) {
                LogUtil.d(TAG, "contentRecapVOList size =" + contentRecapVOList.size());
                for (int i = 0; i < contentRecapVOList.size(); i++) {
                    // NOTE: check status if any media type is interrupted or permission denied.
                    if (!contentRecapVOList.get(i).isCheckStatus()) {
                        useTransferredBytes = true;
                        break;
                    }
                }
            }
        }
        //NOTE: if transfer is success, then show total pay load at both side and if interrupted or permission denied then show only transferred bytes as transferred.
        if(finishStatus.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED) && !useTransferredBytes){

            summDesc = activity.getString(R.string.see_how)+P2PFinishUtil.getInstance().getTotalPayload()+" MB "+activity.getString(R.string.of_your)+P2PFinishUtil.getInstance().getTotalPayload()+" MB "+activity.getString(R.string.transferred);
        }else{
            summDesc = activity.getString(R.string.see_how)+P2PFinishUtil.getInstance().getTotalTransferredData()+" MB "+activity.getString(R.string.of_your)+P2PFinishUtil.getInstance().getTotalPayload()+" MB "+activity.getString(R.string.transferred);
        }
        return summDesc;
    }

    private void handleHeaderButtons(Activity activity, View.OnClickListener transferSummaryListener) {
        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.transfer_summary_header);

        activity.findViewById(R.id.search_icon).setOnClickListener(transferSummaryListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(transferSummaryListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(transferSummaryListener);
    }
}
