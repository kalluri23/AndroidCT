package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.activity.CTDeviceComboActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.asynctask.CalculationAsyncTask;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by kommisu on 7/14/2016.
 */
public class SetupModel {

    private static final String TAG = SetupModel.class.getName();


    private Activity activity;
    private PowerManager.WakeLock mWakeLock;
    private static SetupModel instance;


    private boolean deviceWifiConnStatus;
    private Intent nextIntent;
    private String connectedMacAddress = null;
    private String backupSSID = null;
    private List<WifiConfiguration> BkupWifiConfigurationlist = null;

    public void initModel(Activity activity ) {

        this.activity = activity;
    }

    public static SetupModel getInstance() {

        if (instance == null) {
            instance = new SetupModel();
        }
        return instance;

    }

    public void reset(){
        instance = null;
    }

    public boolean isDeviceWifiConnStatus() {
        return deviceWifiConnStatus;
    }
    public String getBackupSSID() {return backupSSID; }
    public String getConnectedMacAddress() {
        return connectedMacAddress;
    }

    public String getEula( ) {
        String eula = "";
        try {
            eula =  readFileFromAssetsAsString(activity.getApplicationContext(), VZTransferConstants.EULA_FILE);
        } catch ( IOException ioe ) {
            //LogUtil.e(TAG, "Failed to read EULA file. --" + ioe.getMessage() );
            ioe.printStackTrace();
        }

        return eula;
    }

    private String readFileFromAssetsAsString( Context context, String filename ) throws IOException {

        InputStream eulaStream = context.getAssets().open( filename );
        int size = eulaStream.available();

        byte[] buffer = new byte[size];
        eulaStream.read(buffer);
        eulaStream.close();

        String eula = new String(buffer);
        return eula;
    }

    public void proceedToAppication(String text) {
        nextIntent = new Intent(activity, CTDeviceComboActivity.class);

        if(text.equals(VZTransferConstants.OLD_PHONE)) {
            LogUtil.d(TAG, "sang- clicked old");
            nextIntent.putExtra("isNew", false);
        }
        else if(text.equals(VZTransferConstants.NEW_PHONE)){
            LogUtil.d(TAG, "sang- clicked new");
            nextIntent.putExtra("isNew", true);
            Utils.cleanUpReceiverMediaFilesFromVZTransfer();
        }
        runStartupTasks();
        LogUtil.d(TAG, "Run stat up tasks.");
    }

    public void releaseWakelock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            LogUtil.d(TAG,"Release wake lock...");
            mWakeLock.release();
        }else {
            LogUtil.e(TAG,"Device wake lock already released.");
        }
    }
    public static CalculationAsyncTask calculationAsyncTask =null;


    private BroadcastReceiver mWifiEnable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            // If app crash, we don't need to do any action on broadcast receiver.
            LogUtil.d(TAG,"received broadcast :"+intent.getAction());
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mWifiEnable);

            activity.startActivity(nextIntent);
        }

    };

    public void runStartupTasks(){

        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        LogUtil.d(TAG, "Wifi Status " + wifiManager.isWifiEnabled() + System.currentTimeMillis());
        backupConnectionStatus(wifiManager);

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mWifiEnable, new IntentFilter(VZTransferConstants.ENABLE_WIFI_BROADCAST));

        WifiManagerControl.configuringWifiConnection(activity);

        acquireWakeLock(activity);
    }

    public void acquireWakeLock(Activity activity) {
        try {
            if ( mWakeLock == null || !mWakeLock.isHeld()) {
                PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "VZTRANSFER");
                mWakeLock.acquire();
            }else {
                LogUtil.d(TAG,"Device wake lock already acquired.");
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "Exception on FULL_WAKE_LOCK :" + e.getMessage());
        }
    }

    private void backupConnectionStatus(WifiManager wifiManager) {
        deviceWifiConnStatus = wifiManager.isWifiEnabled();
        connectedMacAddress = wifiManager.getConnectionInfo().getBSSID();
        backupSSID = wifiManager.getConnectionInfo().getSSID();
        BkupWifiConfigurationlist = wifiManager.getConfiguredNetworks();

        if ( !HotSpotInfo.isDeviceHotspot() && ! wifiManager.isWifiEnabled() ) {
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }
    }
}
