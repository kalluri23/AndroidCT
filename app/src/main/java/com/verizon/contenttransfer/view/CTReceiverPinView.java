package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTReceiverPinListener;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/8/2016.
 */
public class CTReceiverPinView {
    private static String TAG = CTReceiverPinView.class.getName();
    private static CTReceiverPinView instance;
    private TextView networkName;
    private Activity activity;
    private TextView totConnected, oneToManyNextBtn;

    public CTReceiverPinView(Activity activity) {
        instance = this;
        this.activity = activity;
        initView(activity);
    }

    public static CTReceiverPinView getInstance(){return instance;}

    public void killInstance(){instance = null;}

    private void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_show_pin_layout);

        networkName = (TextView)activity.findViewById(R.id.ct_enter_pin_network);
        totConnected = (TextView) activity.findViewById(R.id.ct_total_conn_count);
        oneToManyNextBtn = (TextView) activity.findViewById(R.id.ct_one_to_many_next_btn);
        View.OnClickListener receiverPinListener = new CTReceiverPinListener(activity);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_pairing);

        activity.findViewById(R.id.search_icon).setOnClickListener(receiverPinListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(receiverPinListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(receiverPinListener);


        if(CTGlobal.getInstance().isDoingOneToMany()){
            oneToManyNextBtn.setOnClickListener(receiverPinListener);
            totConnected.setText(activity.getString(R.string.total_connected_devices) + "0");
            totConnected.setVisibility(View.VISIBLE);
            oneToManyNextBtn.setVisibility(View.VISIBLE);
            LogUtil.d(TAG, "conn count is displayed..");
            TextView maxConnection = (TextView) activity.findViewById(R.id.ct_one_to_many_max_device_connection);
            if(maxConnection != null) {
                maxConnection.setVisibility(View.VISIBLE);
            }

        }else {
            LogUtil.d(TAG, "is doing one to many false.");
        }
    }


    public void enableOneToManyNext(int count){
        totConnected.setText(activity.getString(R.string.total_connected_devices) + count);
        if (count > 0) {
            oneToManyNextBtn.setEnabled(true);
            oneToManyNextBtn.setBackgroundResource(R.drawable.vz_red_solid_round_button);
            oneToManyNextBtn.setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
        } else {
            oneToManyNextBtn.setEnabled(false);
            oneToManyNextBtn.setBackgroundResource(R.drawable.vz_gray_solid_round_button);
            oneToManyNextBtn.setTextColor(activity.getResources().getColor(R.color.ct_mf_light_grey_color));
        }
    }
    public void updateConnection(String ssid){
        networkName.setText(ssid);
    }
}
