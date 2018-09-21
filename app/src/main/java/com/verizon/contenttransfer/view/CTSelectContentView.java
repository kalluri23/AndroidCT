package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.P2PContentListAdapter;
import com.verizon.contenttransfer.listener.CTSelectContentListener;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 7/20/2016.
 */
public class CTSelectContentView {

    private static final String TAG= CTSelectContentView.class.getName();
    private static CTSelectContentView instance;
    private ListView mediaListView;
    private P2PContentListAdapter deviceAdapter;
    private TextView transferdata;
    private TextView selectAll;
    private TextView videoFormatTV;
    private TextView cancelbtn;
    private Activity activity;
    private View.OnClickListener ctSelectContentListener;

    public static CTSelectContentView getInstance() {
        if (instance == null) {
            instance = new CTSelectContentView();
        }
        return instance;
    }
    public void killInstance(){
        instance = null;
    }
    public void initView(final Activity activity) {
        this.activity = activity;
        activity.setContentView(R.layout.ct_select_content_sender_layout);
        ctSelectContentListener = new CTSelectContentListener(activity);
        
        mediaListView = (ListView) activity.findViewById(R.id.ct_content_container);
        deviceAdapter = new P2PContentListAdapter(activity.getApplicationContext(), R.layout.ct_select_content_cell, CTSelectContentUtil.getInstance().getContentList(activity));
        mediaListView.setAdapter(deviceAdapter);
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
            }
        });
        // When transfer button is clicked Launch Sender activity to begin transfer
        transferdata = (TextView) activity.findViewById(R.id.ct_select_content_sender_transfer_btn);
        transferdata.setEnabled(false);
        transferdata.setOnClickListener(ctSelectContentListener);
        cancelbtn = (TextView) activity.findViewById(R.id.ct_select_content_sender_cancel_btn);
        cancelbtn.setOnClickListener(ctSelectContentListener);

        selectAll = (TextView) activity.findViewById(R.id.ct_select_all);
        selectAll.setOnClickListener(ctSelectContentListener);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }
        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer);

        activity.findViewById(R.id.search_icon).setOnClickListener(ctSelectContentListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(ctSelectContentListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(ctSelectContentListener);
    }
    public void  displayVideoFormatMessage(boolean display) {
        LogUtil.d(TAG, "Checking to display video format message" + display);

        if(CTGlobal.getInstance().isCross()) 	{
            if ( display ) {
                videoFormatTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.supporting_video_file_formats));
            }
        } else {
            videoFormatTV.setText("");//sang May2016	videoFormatTV.setText(P2PStartupActivity.contentTransferContext.getString(R.string.zero_lenth_files));
            setVideoFormatTVVisible(true);
        }
    }

    public void setVideoFormatTVVisible(boolean isVisible) {
        if(isVisible) {
            videoFormatTV.setVisibility(View.VISIBLE);
        }else {
            videoFormatTV.setVisibility(View.GONE);
        }
    }

    public void enableTransferButton(boolean isEnable) {
        LogUtil.d(TAG, "Enable the transfer button");
        if(isEnable) {
            transferdata.setEnabled(true);
        }else {
            transferdata.setEnabled(false);
        }
    }

    public  void getTransferInRedColor(){
        (activity.findViewById(R.id.ct_select_content_sender_transfer_btn)).setBackgroundResource(R.drawable.ct_button_solid_black_bg);
        ((TextView)activity.findViewById(R.id.ct_select_content_sender_transfer_btn)).setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
    }



    public void callNotifyDataSetChanged(){
        if(null != deviceAdapter) {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    public TextView getTransferdata() {
        return transferdata;
    }

    public void setSelectAllText(String text) {
        selectAll.setText(text);
    }
}
