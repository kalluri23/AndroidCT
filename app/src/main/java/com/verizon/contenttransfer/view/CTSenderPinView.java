package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTSenderPinListener;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/6/2016.
 */
public class CTSenderPinView {

    private TextView connectbtn;
    private TextView networkName;

    private static CTSenderPinView instance;

    public static ProgressDialog p2pconnectionDiaLogUtil=null;

    public static CTSenderPinView getInstance() {
        if (instance == null) {
            instance = new CTSenderPinView();
        }
        return instance;
    }
    public void killInstance(){
        instance = null;
    }

    public void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_enter_pin_layout);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_pairing);

        View.OnClickListener p2pSenderListener = new CTSenderPinListener(activity);

        activity.findViewById(R.id.ct_enter_pin_cancel_button_tv).setOnClickListener(p2pSenderListener);
		networkName = (TextView) activity.findViewById(R.id.ct_enter_pin_network);
        connectbtn = (TextView) activity.findViewById(R.id.ct_enter_pin_next_button_tv);
        enableConnect(true);
        connectbtn.setOnClickListener(p2pSenderListener);

        if(null != p2pconnectionDiaLogUtil && p2pconnectionDiaLogUtil.isShowing()){
            p2pconnectionDiaLogUtil.dismiss();
            p2pconnectionDiaLogUtil = null;
        }

        activity.findViewById(R.id.search_icon).setOnClickListener(p2pSenderListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(p2pSenderListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(p2pSenderListener);
    }

    public void enableConnect(boolean flag){

        if(connectbtn !=null) {
            connectbtn.setEnabled(flag);
            connectbtn.setClickable(flag);
        }
    }

    public void updateConnection(String ssid){
        networkName.setText(ssid);
    }
}
