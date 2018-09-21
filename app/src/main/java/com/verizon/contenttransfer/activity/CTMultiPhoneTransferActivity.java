package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTMultiPhoneTransferListener;
import com.verizon.contenttransfer.model.CTDeviceComboModel;
import com.verizon.contenttransfer.model.CTMultiPhoneTransferModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTMultiPhoneTransferView;

/**
 * Created by yempasu on 5/23/2017.
 */
public class CTMultiPhoneTransferActivity extends BaseActivity {
    public static Activity activity;
    private static String TAG = CTMultiPhoneTransferActivity.class.getName();
    boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        activity = CTMultiPhoneTransferActivity.this;
        new CTMultiPhoneTransferView(activity);
        new CTMultiPhoneTransferModel(this);
    }

    protected void onResume(){
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTMultiPhoneTransferActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
        CTDeviceComboModel.getInstance().registerBroadcastReceiver();
    }

    protected void onPause() {
        super.onPause();
        if(CTGlobal.getInstance().getExitApp()){
            return;
        }
        CTDeviceComboModel.getInstance().unregisterReciever();
    }

    protected void onDestroy() {
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTMultiPhoneTransferActivity");
        WifiAccessPoint.getInstance().Stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        LogUtil.d(TAG,"onActivityResult - resultCode="+resultCode+"  RESULT_OK="+RESULT_OK);
        if (requestCode == CTMultiPhoneTransferListener.DEFAULT_SMS_APP) {
            // Make sure the request was successful

            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            LogUtil.d(TAG, "onActivityResult ContinueTransfer");
            CTDeviceComboModel.getInstance().continueContentTransfer();
        }
    }
}
