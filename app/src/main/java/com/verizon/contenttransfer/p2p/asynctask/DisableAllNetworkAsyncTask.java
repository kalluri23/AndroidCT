package com.verizon.contenttransfer.p2p.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.List;


public class DisableAllNetworkAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DisableAllNetworkAsyncTask.class.getName();
    private Activity acvt;
    private ProgressDialog pDialog = null;


    public DisableAllNetworkAsyncTask(Activity acvt) {
        this.acvt = acvt;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            acvt.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog = new ProgressDialog(acvt,R.style.AppCompatAlertDialogStyle);
                    pDialog.setMessage(acvt.getString(R.string.configuring_device));
                    pDialog.setCancelable(false);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();
                }
            });

            disableAllNetwork();

        } catch (Exception e1) {
            LogUtil.d(TAG, "restoreWifiConnection Exception =" + e1.getStackTrace());
        }
        // restoreWifiConnection end.
        return null;
    }

    private void disableAllNetwork() {

        WifiManager wifiManagerObj = (WifiManager) acvt.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        LogUtil.d(TAG, "enable access point ... isWifiEnabled =" + wifiManagerObj.isWifiEnabled());
        if (!wifiManagerObj.isWifiEnabled()) {
            wifiManagerObj.setWifiEnabled(true);
            int count = 0;
            while (!wifiManagerObj.isWifiEnabled() && count < 20) {
                count++;
                try {
                    LogUtil.d(TAG, "Waiting to WifiEnabled ");
                    Thread.sleep(2000);
                } catch (Exception e) {
                    LogUtil.d(TAG, e.getMessage());
                }
            }
        }


        //DISABLE ALL KNOWN WIFI CONNECTION.
        LogUtil.d(TAG, "wifiManagerObj.isWifiEnabled():"+wifiManagerObj.isWifiEnabled());

        List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
        boolean isDisabled = false;

        if (null != wifiManagerObj && null != list && !list.isEmpty()) {
            LogUtil.d(TAG, "wifiManagerObj - list :" + list.size());
            for (WifiConfiguration w : wifiManagerObj.getConfiguredNetworks()) {
                isDisabled = wifiManagerObj.disableNetwork(w.networkId);
                LogUtil.d(TAG, "disable network " + w.SSID+" isDisabled: "+isDisabled);
            }
            wifiManagerObj.saveConfiguration();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG, "DisableAllNetworkAsyncTask  onPostExecute");
        if (null != acvt) {
            acvt.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    } catch (Exception e) {
                        LogUtil.d(TAG, "Exception on close dialog :" + e.getMessage());
                    }
                }
            });
        }
        Intent updateSSID = new Intent(VZTransferConstants.ENABLE_WIFI_BROADCAST);
        LocalBroadcastManager.getInstance(acvt).sendBroadcast(updateSSID);
    }

    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG, "DisableAllNetworkAsyncTask  onPreExecute");
    }
}
