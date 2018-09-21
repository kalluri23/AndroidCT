package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTGettingReadyReceiverView;

/**
 * Created by c0bissh on 9/15/2016.
 */
public class CTGettingReadyReceiverActivity extends BaseActivity {
    public static Activity activity;
    private static final String TAG = CTGettingReadyReceiverActivity.class.getName();

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTGettingReadyReceiverActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        new CTGettingReadyReceiverView(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTGettingReadyReceiverActivity");
    }
}
