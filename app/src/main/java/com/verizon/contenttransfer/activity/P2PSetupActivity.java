package com.verizon.contenttransfer.activity; 

import android.app.Activity;
import android.os.Bundle;

import com.adobe.mobile.Config;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.SetupView;

import java.util.HashMap;


public class P2PSetupActivity extends BaseActivity {
	private static final String TAG = "P2PSetupActivity";

	public static Activity activity;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - P2PSetupActivity");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CTErrorReporter.getInstance().Init(this);
		new SetupView(this);
		activity = this;
		ContentPreference.putBooleanValue(getApplicationContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);
	}

	@Override
	public void onBackPressed() {
	}

	@Override
	protected void onResume() {
		super.onResume();

		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - P2PSetupActivity");

		if (CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}

		HashMap<String, Object> contextData = new HashMap<String, Object>();
		contextData.put("content transfer", "transfer");
		Config.collectLifecycleData(this, contextData);
	}


}
