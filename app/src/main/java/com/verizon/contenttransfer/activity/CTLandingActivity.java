package com.verizon.contenttransfer.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTLandingModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTPermissonCheck;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTLandingView;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CTLandingActivity extends BaseActivity {

    private static final String TAG = "CTLandingActivity";
    public static Activity activity;

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - CTLandingActivity");
        if(CTGlobal.getInstance().isForceStop()){
            finish();
            return;
        }
        else if((CTGlobal.getInstance().getExitApp() && !Utils.isStandAloneBuild())){
            if(!CTGlobal.getInstance().isManualSetup() && !CTGlobal.getInstance().isTryAgain()) {
                finish();
                return;
            }
        }
        CTLandingModel.getInstance().LandingPageOnResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        CTErrorReporter.getInstance().Init(this);
        new CTLandingView(activity);
        new CTLandingModel(activity);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
        LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - CTLandingActivity");
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case CTPermissonCheck.MY_PERMISSIONS_REQUEST: {
                boolean deniedFlag = false;
                for(int i=0;i<grantResults.length;i++){
                    if (permissions[i].toString().equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedFlag = true;
                        break;
                    }else if(permissions[i].toString().equals(Manifest.permission.CAMERA) && grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        CTGlobal.getInstance().setManualSetup(true);
                    }else if ((permissions[i].toString().equals(Manifest.permission.RECEIVE_SMS)||permissions[i].toString().equals(Manifest.permission.SEND_SMS)
                             ||permissions[i].toString().equals(Manifest.permission.READ_SMS)||permissions[i].toString().equals(Manifest.permission.RECEIVE_MMS))
                            && grantResults[i] !=PackageManager.PERMISSION_GRANTED ){
                        CTGlobal.getInstance().setSmsPermitted(false);
                    }else if ((permissions[i].toString().equals(Manifest.permission.READ_CALENDAR) || permissions[i].toString().equals(Manifest.permission.WRITE_CALENDAR)) && grantResults[i] !=PackageManager.PERMISSION_GRANTED ){
                        CTGlobal.getInstance().setCalendarPermitted(false);
                    }else if ((permissions[i].toString().equals(Manifest.permission.READ_CALL_LOG) || permissions[i].toString().equals(Manifest.permission.WRITE_CALL_LOG)) && grantResults[i] !=PackageManager.PERMISSION_GRANTED ){
                        CTGlobal.getInstance().setCalllogsPermitted(false);
                    }else if ((permissions[i].toString().equals(Manifest.permission.READ_CONTACTS) || permissions[i].toString().equals(Manifest.permission.WRITE_CONTACTS)) && grantResults[i] !=PackageManager.PERMISSION_GRANTED ){
                        CTGlobal.getInstance().setContactsPermitted(false);
                    }
                }
                if(deniedFlag){
                    ExitContentTransferDialog.alertToExitDialog(activity, activity.getString(R.string.dialog_title) , this.getString(R.string.ct_external_permissions_text),activity.getString(R.string.msg_ok));
                }
            }
        }
    }
}
