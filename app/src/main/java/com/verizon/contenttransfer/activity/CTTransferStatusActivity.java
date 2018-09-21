package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTTransferStatusView;

/**
 * Created by rahiahm on 9/20/2016.
 */
public class CTTransferStatusActivity extends BaseActivity {

    private static Activity activity;
    private static String TAG = CTTransferStatusActivity.class.getName();
    private boolean status;

    public static Activity getActivity(){return activity;}

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTTransferStatusActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTTransferStatusActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        Bundle bundle = getIntent().getExtras();
        status = bundle.getBoolean("isComplete");
        String media= bundle.getString("media");
        String UImedia= bundle.getString("UImedia");
        boolean mediaPermission= bundle.getBoolean("mediaPermission");
        LogUtil.d(TAG,"media clicked :"+media);
        LogUtil.d(TAG,"mediaPermission :"+mediaPermission);
        new CTTransferStatusView(this, status, media,mediaPermission,UImedia);
    }
}
