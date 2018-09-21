package com.verizon.contenttransfer.wifiscan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.verizon.contenttransfer.utils.CTGlobal;

import java.util.Iterator;
import java.util.List;

/**
 * Created by duggipr on 9/29/2016.
 */
public class WifiScanner extends AsyncTask<Void, Void, String> {

    private static final String TAG = "WifiScanner";

    public static String MF_WIFI_SSIDA = "936F-A";
    public static String MF_WIFI_SSIDB = "936F-B";

    //public static String SCANTEST = "NETGEAR88";
    WifiManager wifiManager = null;
    private Context wifiCtxt;
    private Activity activity;
    private WifiScanReceiver mWifiScanReceiver;


    public WifiScanner(Activity act) {
        activity = act;
        wifiCtxt = activity.getApplicationContext();
    }

    @Override
    protected String doInBackground(Void... params) {
        scanForWifi(this.wifiCtxt);
        return null;
    }

    public void scanForWifi(Context ctxt) {
        //LogUtil.d(TAG, "Checking for WIFI...");
        wifiManager = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        //LogUtil.d(TAG, "Wifi Status : " + wifiManager.isWifiEnabled());
        //If WiFi not enabled, enable WiFi
        if (!wifiManager.isWifiEnabled()) {
            //LogUtil.d(TAG, "Enabling Wifi...");
            //wifiManager.setWifiEnabled(true);
        } else {
            //LogUtil.d(TAG, "Scanning for nearby wifi access points...");
            mWifiScanReceiver = new WifiScanReceiver(activity);
            activity.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {

    }


    class WifiScanReceiver extends BroadcastReceiver {
        Activity mActivity = null;

        WifiScanReceiver(Activity activity) {
            mActivity = activity;
        }

        public void onReceive(Context context, Intent intent) {

            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //LogUtil.d(TAG, intent.getAction());
            List<ScanResult> scanResults = wifiManager.getScanResults();
            //LogUtil.d(TAG, wifiManager.getScanResults().toString());
            //LogUtil.d(TAG, Integer.toString(scanResults.size()));


            if (scanResults != null) {

                for (Iterator<ScanResult> iterator = scanResults.iterator(); iterator.hasNext(); ) {
                    if(CTGlobal.getInstance().getExitApp()){
                        if(mWifiScanReceiver != null){
                            activity.unregisterReceiver(mWifiScanReceiver);
                            return;
                        }

                    }
                    ScanResult scanResult = (ScanResult) iterator.next();

                    String ssid = scanResult.SSID;

                    String macAddress = scanResult.BSSID;
                    //LogUtil.d(TAG, "scanResult SSID is: " + ssid+"  macAddress="+macAddress);
                    macAddress = macAddress.replaceAll(":", "");
                    //LogUtil.d(TAG, "Mac Address : " + macAddress);
                    if (ssid.equals(MF_WIFI_SSIDA) || ssid.equals(MF_WIFI_SSIDB)) {
                        //LogUtil.d(TAG, "Found the macaddress " + macAddress);
                        CTGlobal.getInstance().setStoreMacId(macAddress);
                        activity.unregisterReceiver(mWifiScanReceiver);
                        return;
                    }
                }
            }
        }
    }
}
