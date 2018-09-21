package com.verizon.contenttransfer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTReceiverView;

public class CTReceiverActivity extends BaseActivity {

	private static final String TAG = CTReceiverActivity.class.getName();
	public static Activity activity;

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTReceiverActivity");
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
	}
	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		if(CTGlobal.getInstance().getExitApp()){
			return;
		}
		new CTReceiverView(activity);
		CTReceiverModel.getInstance().initModel(this);
	}

	@Override
	public void onBackPressed() {
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		activity = null;
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTReceiverActivity");
	}
}
