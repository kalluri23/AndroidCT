package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.view.WindowManager;

import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.utils.LogUtil;

/**
 * Created by c0bissh on 9/15/2016.
 */
public class CTGettingReadyReceiverModel {
    private Activity activity;
    public static int DEFAULT_SMS_APP = 20;

    private static final String TAG = CTGettingReadyReceiverModel.class.getName();


    public CTGettingReadyReceiverModel(Activity act) {
        this.activity = act;
        initModel();
    }
    private void initModel() {
        CTErrorReporter.getInstance().Init(activity);
        LogUtil.d(TAG, "getting ready Activity going to p2pserverios....");

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // always wakeup

        //STOP PEER DISCOVERY.
        WiFiDirectActivity.StopPeerDiscovery();

    }


}
