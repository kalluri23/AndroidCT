package com.verizon.contenttransfer.activity;

import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.TransferSummaryView;

public class TransferSummaryActivity extends BaseActivity{

    public  static final  String TAG = "TransferSummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CTErrorReporter.getInstance().Init(this);
        TransferSummaryView transferSummaryView = new TransferSummaryView();
        transferSummaryView.initView(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - TransferSummaryActivity");
        if(CTGlobal.getInstance().getExitApp()){
            LogUtil.d(TAG,"Transfer summary on resume finish");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, "Transfer summary on stop");
        P2PFinishModel.getInstance().setTransferSummaryLaunched(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - TransferSummaryActivity");
    }

    @Override
    public void onBackPressed() {
    }
}
