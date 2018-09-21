package com.verizon.contenttransfer.activity;

import android.os.Bundle;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTSelectContentUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTSelectContentView;

/**
 * This Activity captures the information regarding media selection to transfer
 *
 * @author kommisu
 *
 */
public class CTSelectContentActivity extends BaseActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CTErrorReporter.getInstance().Init(this);
		CTSelectContentView.getInstance().initView(this);
		CTSelectContentModel.getInstance().initModel(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTSelectContentActivity");
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
		//REGISTER RECEIVER.
		CTSelectContentModel.getInstance().handleOnResume();

	}



	@Override
	protected void onPause() {
		super.onPause();
		//UN REGISTER RECEIVER.
		if(CTGlobal.getInstance().getExitApp()){
			return;
		}
		CTSelectContentModel.getInstance().handleOnPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTSelectContentActivity");
		CTSelectContentUtil.getInstance().resetOndestroy();
	}

	@Override
	public void onBackPressed() {

	}
}
