package com.verizon.contenttransfer.base;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.ContentTransferAnalyticsMap;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifidirect.DeviceIterator;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.util.HashMap;

/**
 * Created by rahiahm on 7/8/2016.
 */
public class ExitHandler {

    //
    private static final String TAG = ExitHandler.class.getName();

    public static void performExitTasks(final Activity acvt, final String screenName){
        final TextView title = new TextView(acvt);
        boolean exitActivity = true;

        title.setText(acvt.getString(R.string.warning_txt));

        HashMap<String, Object> eventMap = new HashMap<String, Object>();

        eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
        eventMap.put(ContentTransferAnalyticsMap.DATA_TRANSFER_STATUS_MSG, screenName + " - " + CTGlobal.getInstance().getPhoneSelection() + "-" + "Application exited by user");
        Utils.logEvent(title, eventMap, "Application exited by user");

        DeviceIterator.goToMVMHome = true;
        CTGlobal.getInstance().setExitApp(true);
        if(!acvt.getLocalClassName().contains(VZTransferConstants.p2PFinishActivity)){
            MessageUtil.resetDefaultSMSApp(acvt.getApplicationContext());
        }
        CTGlobal.getInstance().setIsWifiDirecct(false);
        LogUtil.d(TAG, "getLocalClassName =" + acvt.getLocalClassName());
        LogUtil.d(TAG, "getPackageName =" + acvt.getPackageName());

        if (acvt.getLocalClassName().contains(VZTransferConstants.wiFiDirectActivity)) {
            LogUtil.d(TAG, "dis connect the wifidirect");
            if (Utils.getListener() != null) {
                Utils.getListener().disconnect();
            }
        }
        //WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        boolean isFirstActivities  = (acvt.getLocalClassName().contains(VZTransferConstants.landingActivity)
                                        || acvt.getLocalClassName().contains(VZTransferConstants.p2pSetupActivity));

        if(Utils.isStandAloneBuild()){
            WifiManagerControl.closePendingAsyncTask(SetupModel.getInstance().isDeviceWifiConnStatus(), !isFirstActivities);
        }
        else
        {
            WifiManagerControl.closePendingAsyncTaskMvm(acvt, !isFirstActivities);
            exitActivity = false;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LogUtil.d(TAG,"Exception on continue exit function.");
                    e.printStackTrace();
                }
                CleanUpSockets.cleanUpAllVariables();
                LogUtil.d(TAG, "Dismissing exit dialog");
            }
        });
        thread.start();
        LocalBroadcastManager.getInstance(acvt).sendBroadcast(new Intent(VZTransferConstants.CONTENT_TRANSFER_STOPPED));
        if(exitActivity) {
            acvt.finish();
        }

    }
}
