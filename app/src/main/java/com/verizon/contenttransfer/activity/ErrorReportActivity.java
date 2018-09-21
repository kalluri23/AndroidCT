package com.verizon.contenttransfer.activity;

import android.os.Bundle;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.ErrorReportView;

public class ErrorReportActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ct_layout_feedback);
        ErrorReportView.initView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - ErrorReportActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - ErrorReportActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
    }


}
