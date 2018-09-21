package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTTransferInterruptView;

public class CTTransferInterruptActivity extends BaseActivity {

    private static final String TAG = "CTLandingActivity";
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        CTErrorReporter.getInstance().Init(this);
        new CTTransferInterruptView(activity);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTTransferInterruptActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onStop() {
        super.onStop();
        P2PFinishModel.getInstance().killInstance();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTTransferInterruptActivity");
        activity = null;
    }

}
