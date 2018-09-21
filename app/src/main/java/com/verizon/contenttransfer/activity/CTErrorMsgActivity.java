package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTErrorMsgView;


/**
 * Created by rahiahm on 9/14/2016.
 */
public class CTErrorMsgActivity extends BaseActivity{

    private static Activity activity = null;
    public static Activity getActivity() {return activity;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        new CTErrorMsgView(activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTErrorMsgActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTErrorMsgActivity");
    }

    @Override
    public void onBackPressed() {
    }
}
