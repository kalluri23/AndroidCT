package com.verizon.contenttransfer.p2p.asynctask;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.util.List;


public class ClosePendingAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = ClosePendingAsyncTask.class.getName();
    private boolean isWifiEnable = false;
    private boolean forceWifiReset = false;
    public ClosePendingAsyncTask(boolean isWifiOn, boolean forceWifiReset){
        this.isWifiEnable = isWifiOn;
        this.forceWifiReset = forceWifiReset;
    }


    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Starting uploading data file in background task.....");
        try {
            WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
            if(wifiManager != null){
                LogUtil.d(TAG,"Is WiFi Enabled : "+wifiManager.isWifiEnabled());
                LogUtil.d(TAG,"HotSpotInfo.isDeviceHotspot(): "+HotSpotInfo.isDeviceHotspot());
                try {
                    if(!HotSpotInfo.isDeviceHotspot() || forceWifiReset){
                        //this resetWifiEnable is required to reset wifi direct icon on the screen.
                        resetWifiEnable();

                        //getWifiEnabled is used to enable wifi after transfer finish.
                        //if(P2PStartupActivity.deviceWifiConnStatus) {
                            //enableWifi();
                        //}
                        //Enable all wifi access point after wifi direct transfer complete.
                        enableAllNetwork();

                        LogUtil.d(TAG, "P2PStartupActivity.deviceWifiConnStatus  : " + isWifiEnable);
                        wifiManager.setWifiEnabled(isWifiEnable);
                    }
                    if(BuildConfig.ENABLE_ANALYTICS)   {
                        int count = 0;
                        try {
                            while(count < 3) {
                                if(ConnectionManager.isConnectedToInternet()) {
                                    P2PFinishUtil.getInstance().uploadAppAnalyticFile();
                                    P2PFinishUtil.getInstance().uploadVzcloudBannerAnalytics();
                                    break;
                                }else {
                                    count ++;
                                    Thread.sleep(1000);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    LogUtil.d(TAG,"restoreWifiConnection :"+e.getMessage());
                }
            }
        } catch (Exception e1) {
            LogUtil.d(TAG, "restoreWifiConnection Exception =" + e1.getStackTrace());
        }
        // restoreWifiConnection end.
        return null;
    }

    private void enableAllNetwork() {
        WifiManager wifiManagerObj = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        LogUtil.d(TAG, "enable access point ... isWifiEnabled =" + wifiManagerObj.isWifiEnabled());
        if (!wifiManagerObj.isWifiEnabled()){
            enableWifi();
        }
        List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
        if (null != wifiManagerObj && null != list && !list.isEmpty()) {
            for (WifiConfiguration w : list) {
                wifiManagerObj.enableNetwork(w.networkId, false);
                //LogUtil.d(TAG, "enable network SSID: " + w.SSID);
            }
            wifiManagerObj.saveConfiguration();
        }
    }

    private void enableWifi() {
        WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        int count = 0;
        while(!wifiManager.isWifiEnabled() && count<20){
            count ++;
            try {
                LogUtil.d(TAG, "Waiting to WifiEnabled ");
                Thread.sleep(1000);
            }catch (Exception e){
                LogUtil.d(TAG,e.getMessage());
            }
        }
    }

    private void resetWifiEnable() {
        WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            int count = 0;
            while(wifiManager.isWifiEnabled() && count < 10){
                try {
                    count++;
                    LogUtil.d(TAG, "Waiting to WifiDisable ");
                    Thread.sleep(1000);

                }catch (Exception e){
                    LogUtil.d(TAG,e.getMessage());
                }

            }
        }else{
            wifiManager.setWifiEnabled(true);
            int count = 0;
            while(!wifiManager.isWifiEnabled() && count < 10){
                try {
                    count++;

                    LogUtil.d(TAG,"Waiting to WifiEnable while reseting");
                    Thread.sleep(1000);

                }catch (Exception e){
                    LogUtil.d(TAG,e.getMessage());
                }

            }

        }

    }


        @Override
    protected void onPostExecute(String result) {

        LogUtil.d(TAG, "ClosePendingAsyncTask  onPostExecute");

            Intent intent = new Intent("wifireset");
            LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
            CTGlobal.getInstance().setWifiReset(true);
    }

    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG,"ClosePendingAsyncTask  onPreExecute");
    }
}
