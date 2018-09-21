package com.verizon.contenttransfer.p2p.asynctask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.QRCodeVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;


public class ConnectToWifiAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = ConnectToWifiAsyncTask.class.getName();
    private Activity activity;
    private ProgressDialog pDialog = null;
    public ConnectToWifiAsyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        //WifiManagerControl.restoreWifiConnection();
        // restoreWifiConnection start.
        try {

            WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
            if(wifiManager != null){
                LogUtil.d(TAG,"Is WiFi Enabled : "+wifiManager.isWifiEnabled());
                try {
                    //this resetWifiEnable is required to reset wifi direct icon on the screen.
                    resetWifiEnable();

                    //Enable all wifi access point after wifi direct transfer complete.
                    enableWifi();
                    if(QRCodeUtil.getInstance().getQrCodeVO() != null) {
                        if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)) {
                            connectToNetwork(QRCodeUtil.getInstance().getQrCodeVO());
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


    private void enableWifi() {
        WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            int count = 0;
            while (!wifiManager.isWifiEnabled() && count < 10) {
                count++;
                //wifiManager.setWifiEnabled(true);
                try {
                    LogUtil.d(TAG, "Waiting to WifiEnabled ");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    LogUtil.d(TAG, e.getMessage());
                }
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
    /*
     * Connect network with password.
     */
    private void connectToNetwork(QRCodeVO qrCodeVO) {
        try{

            String ssid = qrCodeVO.getSsid();
            String key = qrCodeVO.getPassword();
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", key);
            WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(CTGlobal.getInstance().getContentTransferContext().WIFI_SERVICE);
            int networkId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.removeNetwork(networkId);
            wifiManager.saveConfiguration();
            //remember id
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();

            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        }catch (Exception e){
            LogUtil.d(TAG,e.getMessage());
        }
    }

    /*
     * Connect network with no password.
     */
    private void connectToNetworkRouter(QRCodeVO qrCodeVO) {
        try{

            Log.e("TAG", "Called open network code");
            String ssid = qrCodeVO.getSsid();
            String key = qrCodeVO.getPassword();
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", ssid);

            WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(CTGlobal.getInstance().getContentTransferContext().WIFI_SERVICE);

            Log.e("TAG","connection type = "+(getSecurity(wifiConfig)));
            if(getSecurity(wifiConfig) == 1) {
                Log.e("TAG", "Detected open network");
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
            else
                wifiConfig.preSharedKey = String.format("\"%s\"", key);

            //remember id
            int netId = wifiManager.addNetwork(wifiConfig);

            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        }catch (Exception e){
            LogUtil.d(TAG,e.getMessage());
        }
    }

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return 0;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return -1;
        }
        return (config.wepKeys[0] != null) ? 2 : 1;
    }

    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG, "Connect to wifi async task  onPostExecute");
        QRCodeUtil.getInstance().setConnectingWifi(false);
    }

    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG, "Connect to wifi async task  onPreExecute");
        QRCodeUtil.getInstance().setConnectingWifi(true);

/*        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog = new ProgressDialog(activity);
                    pDialog.setMessage("Please wait.");

                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();
                }
            });
        } catch (Exception e) {
            LogUtil.d(TAG, "Exception on close dialog :" + e.getMessage());
        }*/



    }
}
