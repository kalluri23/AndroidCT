package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTSavingMediaModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTSavingMediaView;

/**
 * Created by yempasu on 8/14/2017.
 */
public class CTSavingMediaActivity extends BaseActivity {
    private static final String TAG = CTSavingMediaActivity.class.getName();
    public static Activity activity;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "Launchng saving media activity...");
        super.onCreate(savedInstanceState);
        CTErrorReporter.getInstance().Init(this);
        activity = this;
        CTSavingMediaView.getInstance().initView(activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTSavingMediaActivity");
        if (CTGlobal.getInstance().getExitApp()) {
            finish();
            return;
        }
        CTSavingMediaModel.getInstance().handleOnResume();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "sang- Physical Back Disabled ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTSavingMediaActivity");
        CTSavingMediaModel.getInstance().handleOnDestroy();
        if (CTGlobal.getInstance().getExitApp()) {
            finish();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        LogUtil.d(TAG, "onActivityResult - resultCode=" + resultCode + "  RESULT_OK=" + RESULT_OK);
        if (requestCode == CTSavingMediaView.DEFAULT_SMS_APP) {
            if (resultCode == -1) {
                LogUtil.d(TAG, "onActivityResult default sms accepted");
                CTSavingMediaModel.getInstance().bindNotificationService(CTSavingMediaModel.getInstance().smsState);
            } else {
                LogUtil.d(TAG, "onActivityResult default sms not accepted");
                CTSavingMediaModel.getInstance().bindNotificationService("false"); //ignoring saving SMS process
            }
        }
    }

}
