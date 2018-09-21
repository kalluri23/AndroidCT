package com.verizon.contenttransfer.wifip2p;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.p2p.asynctask.ClosePendingAsyncMvmTask;
import com.verizon.contenttransfer.p2p.asynctask.ClosePendingAsyncTask;
import com.verizon.contenttransfer.p2p.asynctask.ConnectToWifiAsyncTask;
import com.verizon.contenttransfer.p2p.asynctask.DisableAllNetworkAsyncTask;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by rahiahm on 3/21/2016.
 */
public class WifiManagerControl {
    private static final String TAG = "WifiManagerControl";
    private static Method setFrequency;
    private static Method setLinkSpeed;

    private static Method getFrequency;
    private static final String WPA = "WPA";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    // verify this method before use this class.
  /*  public boolean is5GHzBandSupported() { // unused method
        return (setFrequency != null && setLinkSpeed != null);
    }*/

    private WifiInfo info;

    private WifiManagerControl(WifiManager mgr) {
        info = mgr.getConnectionInfo();
        for (Method method : info.getClass().getDeclaredMethods()) {
            String methodName = method.getName();
            LogUtil.d(TAG,"Method name ="+methodName);
            if (methodName.equals("setFrequency")) {
                setFrequency = method;
            }else if( methodName.equals("setLinkSpeed") ) {
                setLinkSpeed = method;
            }else if( methodName.equals("getFrequency") ) {
                getFrequency = method;
            }

        }
    }
/*    // Initiate class.
    public static WifiManagerControl getWifiManagerControl(WifiManager mgr) { // unused method
        return new WifiManagerControl(mgr);
    }

    public void set5GHzBandFrequency() {
        try {
            setFrequency.invoke(info, 5300);
        } catch (Exception e) {
            LogUtil.e(TAG, "setFrequency :"+e.toString()); // shouldn't happen
        }
    }*/

/*    public void set5GHzBandLinkSpeed() { // unused method
        try {
            setLinkSpeed.invoke(info, 585);
        } catch (Exception e) {
            LogUtil.e(TAG, "setLinkSpeed :"+e.toString()); // shouldn't happen
        }
    }*/
/*    public int get5GHzBandFrequency() {
        try {
            return (Integer)getFrequency.invoke(info);
        } catch (Exception e) {
            LogUtil.e(TAG, "getFrequency :" + e.toString()); // shouldn't happen
            return -1;
        }
    }*/


/*    public static void restoreWifiConnection() {
        try {
            WifiManager wifiManager = (WifiManager) P2PStartupActivity.contentTransferContext.getSystemService(Context.WIFI_SERVICE);
            if(wifiManager != null){
                LogUtil.d(TAG,"Is WiFi Enabled : "+wifiManager.isWifiEnabled());
                try {
                    if(!HotSpotInfo.isDeviceHotspot()){
                        //if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                        wifiManager.setWifiEnabled(true);
                        //}
                        LogUtil.d(TAG, "P2PStartupActivity.deviceWifiConnStatus  : " + P2PStartupActivity.deviceWifiConnStatus);
                        wifiManager.setWifiEnabled(P2PStartupActivity.deviceWifiConnStatus);
                        LogUtil.d(TAG, "Restored Wifi Connection after data transfer complite over WifiDirect : " + wifiManager.isWifiEnabled());
                        List<ScanResult> wireless = wifiManager.getScanResults(); // Returns a <list> of scanResults
                        for(ScanResult scan : wireless)
                        {
                            LogUtil.d(TAG, "P2PStartupActivity.desiredMACAddress  : " + P2PStartupActivity.desiredMACAddress);
                            if(scan.BSSID.equals(P2PStartupActivity.desiredMACAddress))
                            {
                                for(WifiConfiguration w: P2PStartupActivity.BkupWifiConfigurationlist)
                                {
                                    wifiManager.enableNetwork(w.networkId, true);
                                    LogUtil.d(TAG, "wifiManager.enableNetwork  : " + w.SSID);
                                }
                                wifiManager.saveConfiguration();
                            }
                        }
                    }
                }catch (Exception e){
                    LogUtil.d(TAG,"restoreWifiConnection :"+e.getMessage());
                }
            }
        } catch (Exception e1) {
            LogUtil.d(TAG, "restoreWifiConnection Exception =" + e1.getStackTrace());
        }
    }*/

    public static void configuringWifiConnection(Activity activity) {
        DisableAllNetworkAsyncTask disableAllNetworkAsyncTask = new DisableAllNetworkAsyncTask(activity);
        LogUtil.d(TAG, "Launching Open Comm port async task.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            disableAllNetworkAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            disableAllNetworkAsyncTask.execute();
        }
    }

    public static void disableAllWifiConnections(Activity activity) {
        LogUtil.d(TAG, "Disable access point");
        WifiManager wifiManagerObj = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
        if (null != wifiManagerObj && null != list && !list.isEmpty()) {
            for (WifiConfiguration w : wifiManagerObj.getConfiguredNetworks()) {
                wifiManagerObj.disableNetwork(w.networkId);
                LogUtil.d(TAG, "disable network " + w.SSID);

            }
            wifiManagerObj.saveConfiguration();
        }
    }

    public static void connectToWifiAsyncTask(Activity activity) {

        ConnectToWifiAsyncTask connectToWifiAsyncTask = new ConnectToWifiAsyncTask(activity);
        //p2pserver.execute();

        LogUtil.d(TAG, "Launching connect To Wifi AsyncTask....");
        if (Build.VERSION.SDK_INT >= 11) {
            LogUtil.d(TAG,"Launching single thread async task...");
            connectToWifiAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            LogUtil.d(TAG, "Launching single thread async task...");
            connectToWifiAsyncTask.execute();
        }
    }

    public static void closePendingAsyncTask(boolean isWifiOn, boolean forceWifiReset) {
        ClosePendingAsyncTask closePendingAsyncTask = new ClosePendingAsyncTask(isWifiOn,forceWifiReset);

        LogUtil.d(TAG, "Launching close Pending AsyncTask....");
        if (Build.VERSION.SDK_INT >= 11) {
            LogUtil.d(TAG,"Launching single thread async task...");
            closePendingAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            LogUtil.d(TAG, "Launching single thread async task...");
            closePendingAsyncTask.execute();
        }
    }
    public static void closePendingAsyncTaskMvm(Activity acvt, boolean forceWifiReset) {
        ClosePendingAsyncMvmTask closePendingAsyncTask = new ClosePendingAsyncMvmTask(acvt, forceWifiReset);
        //p2pserver.execute();

        LogUtil.d(TAG, "Launching ClosePendingAsyncMvmTask....");
        if (Build.VERSION.SDK_INT >= 11) {
            LogUtil.d(TAG,"Launching single thread async task...");
            closePendingAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            LogUtil.d(TAG, "Launching single thread async task...");
            closePendingAsyncTask.execute();
        }
    }

    public static boolean isConnectedViaWifi(){
        boolean isConnected = false;
        if(P2PStartupActivity.contentTransferContext != null) {
            ConnectivityManager connMgr = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            isConnected = wifi != null && wifi.isConnectedOrConnecting();
            LogUtil.d(TAG, "Is connected via wifi conn :" + isConnected);
        }
        return isConnected;
    }
    public static boolean isWifiEnabled(Activity activity){
        boolean isEnabled = false;
        if(P2PStartupActivity.contentTransferContext!= null){
            WifiManager wifiManagerObj = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            isEnabled =  wifiManagerObj.isWifiEnabled();
        }
        return isEnabled;
    }

    public static void disableOtherWifiAccesspoints(final Activity activity) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                LogUtil.d(TAG, "Disable access point");
                WifiManager wifiManagerObj = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo wifiInfo = wifiManagerObj.getConnectionInfo();
                String connectedSSID = wifiInfo.getSSID().replaceAll("\"","");
                List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
                if (null != wifiManagerObj && null != list && !list.isEmpty()) {
                    //LogUtil.d(TAG,"Wifi conf list size ="+list.size());
                    String ssid = "";
                    for (WifiConfiguration w : wifiManagerObj.getConfiguredNetworks()) {
                        //LogUtil.d(TAG,"network SSID" + w.SSID);
                        ssid = w.SSID.replaceAll("\"","");
                        if(!connectedSSID.equals(ssid)) {
                            wifiManagerObj.disableNetwork(w.networkId);
                            //LogUtil.d(TAG, "disable network " + w.SSID);
                        }else {
                            //LogUtil.d(TAG,"Connected SSID ="+w.SSID);
                        }
                    }
                    wifiManagerObj.saveConfiguration();
                }
            }
        }, 500);
    }

    public static void resetWifiConnectionOnFinish(boolean forceWifiReset) {
        LogUtil.d(TAG, "Wifi Connection flag " + CTGlobal.getInstance().isWifiDirecct());
        if(!CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.PHONE_WIFI_CONNECTION)
            && SetupModel.getInstance().isDeviceWifiConnStatus()) {
            CTGlobal.getInstance().setIsWifiDirecct(false);
            WifiManagerControl.closePendingAsyncTask(true,forceWifiReset);
        }else{
            CTGlobal.getInstance().setWifiReset(true);
        }
    }

   /* public static WifiConfiguration getWifiConfiguration(String ssidStr) {
        LogUtil.d(TAG,"get wifi configuration - ssid :"+ssidStr);
        if(ssidStr == null || ssidStr.length()==0){
            return null;
        }
        WifiConfiguration wifiConfiguration = null;
        WifiManager wifiManagerObj = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
        if(null != wifiManagerObj && null != list  && !list.isEmpty()){
            for(WifiConfiguration w: list)
            {
                if(ssidStr.equals(w.SSID)){
                    wifiConfiguration = w;
                    break;
                }
            }
        }
        return wifiConfiguration;
    }*/

   /* public String requestWIFIConnection(String networkSSID, String networkPass) {
        try {
            WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
            //Check ssid exists
            if (scanWifi(wifiManager, networkSSID)) {
                if (getCurrentSSID(wifiManager) != null && getCurrentSSID(wifiManager).equals("\"" + networkSSID + "\"")) {
                    //new ShowToast(context, "Already Connected With " + networkSSID);
                    return "ALREADY_CONNECTED";
                }
                //Security type detection
                String SECURE_TYPE = checkSecurity(wifiManager, networkSSID);
                if (SECURE_TYPE == null) {
                    //new ShowToast(context, "Unable to find Security type for " + networkSSID);
                    return "UNABLE_TO_FIND_SECURITY_TYPE";
                }
                if (SECURE_TYPE.equals(WPA)) {
                    //WPA(networkSSID, networkPass, wifiManager);
                } else if (SECURE_TYPE.equals(WEP)) {
                    //WEP(networkSSID, networkPass);
                } else {
                    //OPEN(wifiManager, networkSSID);
                }
                return "CONNECTION_REQUESTED";

            }

        } catch (Exception e) {
            LogUtil.e(TAG, "Error Connecting WIFI " + e.getMessage());
        }
        return "SSID_NOT_FOUND";
    }*/
   /* public String getCurrentSSID(WifiManager wifiManager) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }*/

   /* boolean scanWifi(WifiManager wifiManager, String networkSSID) {
        LogUtil.e(TAG, "scanWifi starts");
        List<ScanResult> scanList = wifiManager.getScanResults();
        for (ScanResult i : scanList) {
            if (i.SSID != null) {
                LogUtil.e(TAG, "SSID: " + i.SSID);
            }

            if (i.SSID != null && i.SSID.equals(networkSSID)) {
                LogUtil.e(TAG, "Found SSID: " + i.SSID);
                return true;
            }
        }
        //new ShowToast(context, "SSID " + networkSSID + " Not Found");
        return false;
    }*/

    public static String checkSecurity(WifiManager wifiManager, String ssid) {
        List<ScanResult> networkList = wifiManager.getScanResults();
        for (ScanResult network : networkList) {
            if (network.SSID.equals(ssid)) {
                String Capabilities = network.capabilities;
                if (Capabilities.contains("WPA")) {
                    return WPA;
                } else if (Capabilities.contains("WEP")) {
                    return WEP;
                } else {
                    return OPEN;
                }

            }
        }
        return "";
    }
    public static boolean isPersonalHotsoptOn(){
        final WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            LogUtil.d(TAG, "Ap State = "+apState);
            if (apState == 13) {
                // Ap Enabled
                return true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        return false;
    }
}
