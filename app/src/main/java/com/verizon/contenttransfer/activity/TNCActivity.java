package com.verizon.contenttransfer.activity; 

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.TNCModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.TNCView;

/**
 * Created by kommisu on 7/12/2016.
 */
public class TNCActivity extends BaseActivity {

    public static Activity activity = null;

    public static Activity getActivity() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tnc_layout);
        activity = this;

        if ( ! new TNCModel( this ).getStatus() ) {
            TNCView.initView( this );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - TNCActivity");
        if(CTGlobal.getInstance().getExitApp()){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - TNCActivity");
    }

    @Override
    public void onBackPressed() {
    }

}
