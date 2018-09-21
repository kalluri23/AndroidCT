package com.verizon.contenttransfer.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

/**
 * Created by duggipr on 2/26/2016.
 */

// NOTE: This class is used to for sending App analytic report.. and now it is not used.

public class CTWifiConnBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = CTWifiConnBroadcastReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "CTWifiConnBroadcastReceiver onReceive.  -- context:" + context);
        try{

            if (WifiManagerControl.isConnectedViaWifi()) {
                LogUtil.d(TAG, "Wifi Network is Available ");

                Utils.uploadAppAnalyticsFile();
                Utils.uploadCrashErrorReportFile();
                Intent updateSSID = new Intent("update-ssid");
                LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(updateSSID);
            } else {
                LogUtil.d(TAG, "Wifi Network is not Available ");
            }
        }catch (Exception e){
            LogUtil.d(TAG,"Battery Level exception :"+e.getMessage());
            e.printStackTrace();
        }
    }

}
