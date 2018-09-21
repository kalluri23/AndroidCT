package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTWifiSetupModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTWifiSetupView;


public class CTWifiSetupActivity extends BaseActivity {
	public static final String TAG = CTWifiSetupActivity.class.getName();

	boolean enableWifi = false;
	public static Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CTErrorReporter.getInstance().Init(this);
		LogUtil.d(TAG, "wifi activity create");
		activity = this;

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			enableWifi = bundle.getBoolean("enableWifi");
		}

		CTWifiSetupView.getInstance().initView(this);
		CTWifiSetupModel.getInstance().initModel(this);
		LogUtil.d(TAG,"On create check is Open camera.");
		if(CTGlobal.getInstance().isWiDiFallback() &&
				!Utils.isThisServer() &&
				QRCodeUtil.getInstance().isUsingQRCode()){
			QRCodeUtil.getInstance().launchQRCodeActivity(activity);
		}
	}

	@Override
	protected void onResume() {
	    super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTWifiSetupActivity - returned from qr code activity :"+QRCodeUtil.getInstance().isReturnedFromQRActivity());

		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}

		CTWifiSetupModel.getInstance().registerBroadcastReceivers();
		CTWifiSetupModel.getInstance().handleResume(enableWifi);


		if (QRCodeUtil.getInstance().isReturnedFromQRActivity()) {
			if(QRCodeUtil.getInstance().isUsingQRCode()) {
				Handler handler2 = new Handler();
				handler2.postDelayed(new Runnable() {
					@Override
					public void run() {
						CTWifiSetupModel.getInstance().processQRCode();
					}

				}, 0);
			}
		}



	}

	@Override
	protected void onPause() {
		super.onPause();

		if(CTGlobal.getInstance().getExitApp()){
			return;
		}


		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onPause - CTWifiSetupActivity");
		CTWifiSetupModel.getInstance().unregisterReceivers();
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.d(TAG, "wifi activity stop");
	}

	@Override
	protected void onDestroy() {
		WifiAccessPoint.getInstance().Stop();
		CTWifiSetupModel.getInstance().unregisterReceivers();
		CTWifiSetupView.killInstance();
		CTWifiSetupModel.getInstance().killInstance();
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTWifiSetupActivity");
	}

	@Override
	public void onBackPressed() {
	}

}
