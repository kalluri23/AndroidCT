package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTSenderModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTSenderView;



public class CTSenderActivity extends BaseActivity {

	private static final String TAG = "IOSSenderActivity";
	public static Activity activity;

	private CTSenderView ctSenderView;

	@Override
	protected void onPause() {
		super.onPause();
		CTSenderModel.getInstance().handleOnPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(TAG, "Launchng sender activity...");
		super.onCreate(savedInstanceState);
		CTErrorReporter.getInstance().Init(this);
		activity = this;
		ctSenderView = new CTSenderView(activity);

		CTSenderModel.getInstance().initModel(activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTSenderActivity");
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
		CTSenderModel.getInstance().handleOnResume();
	}

	@Override
	public void onBackPressed() {
		LogUtil.d(TAG, "sang- Physical Back Disabled ");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTSenderActivity");
		CTSenderModel.getInstance().handleOnDestroy();
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
	}
}
