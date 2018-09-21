package com.verizon.contenttransfer.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.adobe.mobile.Config;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.StartupModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

public class P2PStartupActivity extends BaseActivity {

	public static final String TAG = "P2PStartupActivity";
	//Variable for existing wifi connection setting backup.
	public static Activity activity;
	public static Context contentTransferContext;

	private StartupModel startupModel;

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - P2PStartupActivity");
		LogUtil.d(TAG,"MODEL :"+Build.MODEL);
		LogUtil.d(TAG, "BRAND :" + Build.BRAND);



		if(CTGlobal.getInstance().getExitApp()){
			Utils.resetExitAppFlags(false);
			finish();
			return;
		}
		startupModel.collectLifeCylce();
		startupModel.stopSensor();
		// We are checking wifi status to restore after app crash.
		startupModel.resetConnection();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activity = this;

		LogUtil.d(TAG,"Launching P2PStartupActivity = "+VZTransferConstants.CRASH_LOGGING);

		Intent intent = getIntent();

		// Allow the SDK access to the application context
		contentTransferContext = this.getApplicationContext();
		Config.setContext(this.getApplicationContext());

		startupModel = new StartupModel(this, intent);
	}

	@Override
	protected void onDestroy() {

		startupModel.cleanUp();
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - P2PStartupActivity");
	}


}
