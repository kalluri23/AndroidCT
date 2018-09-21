package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTReceiverPinModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTReceiverPinView;


public class CTReceiverPinActivity extends BaseActivity {

	private static final String TAG = "CTReceiverPinActivity";
	public static final String iosAckMessagePrefix = "VZCONTENTTRANSFERSECURITYKEYFROMRECAND#";
	public static Activity activity;
	private CTReceiverPinView ctReceiverPinView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CTErrorReporter.getInstance().Init(this);
		ctReceiverPinView = new CTReceiverPinView(this);
		CTReceiverPinModel.getInstance().initReceiverPinModel(this);
		activity = this;
	}

	@Override
	public void onBackPressed() {
	}

	@Override
	protected void onDestroy() {

		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTReceiverPinActivity");
		ctReceiverPinView.killInstance();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTReceiverPinActivity");
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
		CTReceiverPinModel.getInstance().registerBroadcastReceiver();
		CTReceiverPinModel.getInstance().updateWifiConnectionStatus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.d(TAG, "onPause - CTGlobal.getInstance().getExitApp() =" + CTGlobal.getInstance().getExitApp());
		if(CTGlobal.getInstance().getExitApp()){
			return;
		}
		CTReceiverPinModel.getInstance().unregisterReciever();
		CTReceiverPinModel.getInstance().cancelPinTimer();
	}

}
