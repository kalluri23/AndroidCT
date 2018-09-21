package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTDeviceComboListener;
import com.verizon.contenttransfer.model.CTDeviceComboModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTDeviceComboView;

/**
 * Created by rahiahm on 3/18/2016.
 */
public class CTDeviceComboActivity extends BaseActivity{

    public static Activity activity;
    private static String TAG = CTDeviceComboActivity.class.getName();
    Boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        isNew = bundle.getBoolean("isNew");
        activity = CTDeviceComboActivity.this;
        new CTDeviceComboView(activity, isNew);
        new CTDeviceComboModel(this, isNew);

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(VZTransferConstants.CONTENT_TRANSFER_STARTED));
    }

    protected void onResume(){
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTDeviceComboActivity");
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
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTDeviceComboActivity");
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
        if (requestCode == CTDeviceComboListener.DEFAULT_SMS_APP) {
            // Make sure the request was successful

            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            LogUtil.d(TAG, "onActivityResult ContinueTransfer");
            CTDeviceComboModel.getInstance().continueContentTransfer();
        }
    }
}
