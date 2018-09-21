package com.verizon.contenttransfer.wifip2p;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by rahiahm on 3/21/2016.
 */
public class ConnectionManager {
    private static final String TAG = "ConnectionManager";

    ConnectionManager() {
    }

    public static void setMobileDataState(boolean mobileDataEnabled) {
        try {
            TelephonyManager telephonyService = (TelephonyManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error setting mobile data state", ex);
        }
    }

    public static boolean getMobileDataState() {
        try {
            TelephonyManager telephonyService = (TelephonyManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod) {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error getting mobile data state", ex);
        }

        return false;
    }
/*    private static final int REQUEST_SCAN_ALWAYS_AVAILABLE = 10;
    public void isScanAlwaysAvailable (boolean flag){
        WifiManager wifiManager = (WifiManager) P2PStartupActivity.contentTransferContext.getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE, REQUEST_SCAN_ALWAYS_AVAILABLE));
        }
    }*/

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public static boolean isMobileDataAvailable() {
        boolean isAvailable = false;
        if (P2PStartupActivity.contentTransferContext != null) {
            ConnectivityManager connMgr = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            isAvailable = mobile != null && mobile.isAvailable();
            LogUtil.d(TAG, "Is mobile conn available :" + isAvailable);
        }
        return isAvailable;
    }

    /*    public void setFlightMode() {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    try {
                        // No root permission, just show Airplane / Flight mode setting screen.
                        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        P2PStartupActivity.contentTransferContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Setting screen not found due to: " + e.fillInStackTrace());
                    }

            } else {
                // API 16 and earlier.
                boolean enabled = isFlightModeEnabled();
                Settings.System.putInt(P2PStartupActivity.contentTransferContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 0 : 1);
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", !enabled);
                P2PStartupActivity.contentTransferContext.sendBroadcast(intent);
            }
        }*/
    public static boolean isSimSupport(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);

    }

    /**
     * Gets the resources of another installed application
     */
    private static Resources getExternalResources(Context ctx, String namespace) {
        PackageManager pm = ctx.getPackageManager();
        try {
            return (pm == null) ? null : pm.getResourcesForApplication(namespace);
        } catch (PackageManager.NameNotFoundException ex) {
            return null;
        }
    }

    /**
     * Gets a resource ID from another installed application
     */
    private static int getExternalIdentifier(Context ctx, String namespace,
                                             String key, String type) {
        Resources res = getExternalResources(ctx, namespace);
        return (res == null) ? 0 : res.getIdentifier(key, type, namespace);
    }

    /**
     * Gets a String resource from another installed application
     */
    public static String getExternalString(Context ctx, String namespace,
                                           String key, String defVal) {
        int resId = getExternalIdentifier(ctx, namespace, key, "string");
        if (resId != 0) {
            Resources res = getExternalResources(ctx, namespace);
            return res.getString(resId);
        } else {
            return defVal;
        }
    }

    /**
     * Ensure that an Activity is available to receive the given Intent
     */
    public static boolean activityExists(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }

    /*
     *  launch "Smart network switch" setting activity.
     *  backup for further investigation.
     */
    public static void showAdvancedWifiIfAvailable(Context ctx) {
        final Intent i = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
        if (activityExists(ctx, i)) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        }
    }

    /**
     * Gets the service set identifier (SSID) of the current 802.11 network. If
     * the SSID can be decoded as UTF-8, it will be returned surrounded by
     * double quotation marks. Otherwise, it is returned as a string of hex
     * digits. The SSID may be null if there is no network currently connected.
     *
     * @param context
     * @return string the SSID
     * @author Seiwon Lee
     */
    public static String getSSID(Context context) {

        if (null == context) {
            LogUtil.d(TAG, "Invalid argument: context is null.");
            return null;
        }
        WifiManager wifiMgr = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return (wifiInfo.getSSID() == null ? "" : wifiInfo.getSSID().replaceAll("^\"|\"$", ""));
    }

    public static void resetWifiConnectionOnlyAfterCrash() {
        if (ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_WIFI_CONNECTED, false)) {
            ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_WIFI_CONNECTED, false);
            String ssid = ConnectionManager.getSSID(CTGlobal.getInstance().getContentTransferContext());
            LogUtil.d(TAG, "SSID on restart :[" + ssid + "]");
            if (ssid != null && ssid.length() == 0) {
                WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    wifiManager.setWifiEnabled(true);
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    if (null != wifiManager && null != list && !list.isEmpty()) {
                        for (WifiConfiguration w : list) {
                            wifiManager.enableNetwork(w.networkId, false);
                            LogUtil.d(TAG, "enable network SSID on app restart: " + w.SSID);
                        }
                        wifiManager.saveConfiguration();
                    }
                }
            }
        }
    }

    public static String getLocalIpAddress() {
        WifiManager wifiMan = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();

        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }

    public static boolean isConnectedToInternet() {
        boolean isConnected = false;
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
        }
        LogUtil.d(TAG, "is connected to internet :" + isConnected);
        return isConnected;

    }
}
