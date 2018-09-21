package com.verizon.contenttransfer.activity;

import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.P2PFinishView;

public class P2PFinishActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CTErrorReporter.getInstance().Init(this);
        P2PFinishView.getInstance().initView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - P2PFinishActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
        P2PFinishModel.getInstance().onResumeEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();
        P2PFinishModel.getInstance().killInstance();
    }

    @Override
    protected void onUserLeaveHint(){
        P2PFinishModel.getInstance().setUserLeaveHint();
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        try {
            LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - P2PFinishActivity");
            SetupModel.getInstance().releaseWakelock();

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }
}
