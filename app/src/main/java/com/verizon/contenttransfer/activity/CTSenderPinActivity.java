package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTSenderPinModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTSenderPinView;


public class CTSenderPinActivity extends BaseActivity {

    public static final String TAG = CTSenderPinActivity.class.getName();
    private static final String TITLE = "Content Transfer";
    private CTSenderPinModel ctSenderPinModel;
    public static Activity activity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CTErrorReporter.getInstance().Init(this);
        CTSenderPinView.getInstance().initView(this);
        ctSenderPinModel = new CTSenderPinModel(this);
        activity = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTSenderPinActivity");
        if (CTGlobal.getInstance().getExitApp()) {
            finish();
            return;
        }

        ctSenderPinModel.registerBroadcastReceiver();
        ctSenderPinModel.updateWifiConnectionStatus();
        LogUtil.d(TAG, "onResume - CtGlobal.getInstance().getExitApp() =" + CTGlobal.getInstance().getExitApp());

        CTSenderPinView.getInstance().enableConnect(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (CTGlobal.getInstance().getExitApp()) {
            return;
        }

        ctSenderPinModel.unregisterReciever();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {

        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTSenderPinActivity");
        CTSenderPinView.getInstance().killInstance();
        super.onDestroy();
    }
}
