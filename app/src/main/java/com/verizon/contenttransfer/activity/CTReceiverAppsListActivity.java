package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;

/**
 * Created by yempasu on 4/7/2017.
 */
public class CTReceiverAppsListActivity extends BaseActivity{
    private static final String TAG = CTReceiverAppsListActivity.class.getName();
    public static Activity activity;
    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTReceiverAppsList Activity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
        CTReceiverAppsListView.getInstance().handleResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CTReceiverAppsListView(this);
    }
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy - CTReceiver Apps ListActivity");
    }
}
