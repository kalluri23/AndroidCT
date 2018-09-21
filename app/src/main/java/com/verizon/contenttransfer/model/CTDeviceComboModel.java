package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTMultiPhoneTransferActivity;
import com.verizon.contenttransfer.activity.CTWifiSetupActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.asynctask.CalculationAsyncTask;
import com.verizon.contenttransfer.p2p.model.CollectionTaskVO;
import com.verizon.contenttransfer.sms.ComposeSmsActivity;
import com.verizon.contenttransfer.sms.MmsReceiver;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.passwordmanager.BindPasswordManagerService;
import com.verizon.contenttransfer.wifiscan.WifiScanner;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by rahiahm on 7/20/2016.
 */
public class CTDeviceComboModel {

    private static final String TAG = CTDeviceComboModel.class.getName();
    private boolean isCross;
    private boolean isNew;
    private Activity activity;
    Intent nextIntent;

    //private ProgressDialog pDialog;
    //public static WifiAccessPoint wifiAccessPoint;
    private static CTDeviceComboModel instance;

    public CTDeviceComboModel(Activity activity, boolean isNew){
        this.activity = activity;
        this.isNew = isNew;
        instance = this;
        CTErrorReporter.getInstance().Init(activity);
        launchWifiScan();
    }

    public static CTDeviceComboModel getInstance(){
        return instance;
    }

    public boolean isCross() {
        return isCross;
    }

    public void setIsCross(boolean isCross) {
        this.isCross = isCross;
    }

/*    public static void stopAccessPoint(){
        if(wifiAccessPoint != null) {
            wifiAccessPoint.Stop();
        }
        if(DeviceIterator.wifiAccessPoint != null){
            DeviceIterator.wifiAccessPoint.Stop();
        }
    }*/

    public void enableMMSReceiver(){
        ComponentName component=new ComponentName(activity ,MmsReceiver.class);
        activity.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        ComponentName component1=new ComponentName(activity ,ComposeSmsActivity.class);
        activity.getPackageManager().setComponentEnabledSetting(component1,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


    public void registerBroadcastReceiver() {
        IntentFilter wifiFilter=new IntentFilter();
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mMessageReceiver, new IntentFilter(VZTransferConstants.ACCESS_POINT_UPDATE));
    }

    public void unregisterReciever() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mMessageReceiver);
    }

    public void ContinueTransfer (){
        if(!Utils.isReceiverDevice()) {
            LogUtil.d(TAG, "Launch Data collection service...");
            launchDataCollectionService();
        }
        if(Utils.getServerName().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)){
            startWiDiFlow();
        }else {
            if(!Utils.isThisServer())
            {
                CTGlobal.getInstance().setIsWiDiFallback(true);
            }

            Intent intent = new Intent(activity, CTWifiSetupActivity.class);
            activity.startActivity(intent);
        }
    }

    private void startWiDiFlow() {
        Intent intent = new Intent(activity, WiFiDirectActivity.class);
        LogUtil.d(TAG,"on launching WifiDirectActivity ");
        Bundle bundle = new Bundle();
        if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)) {
            int packetSize = 1024;
            int numberOfSockets = 0;
            bundle.putInt("PACKET_SIZE", packetSize);
            bundle.putInt("NUMBER_OF_SOCKETS", numberOfSockets);
            bundle.putInt(VZTransferConstants.SERVER_EXTRA_KEY, 1);
            intent.putExtras(bundle);
        } else {
            bundle.putInt(VZTransferConstants.SERVER_EXTRA_KEY, 0);
        }

        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //dismissDialog();
            CustomDialogs.dismissDefaultProgressDialog();
            // If app crash, we don't need to do any action on broadcast receiver.
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            LogUtil.d(TAG, "broadcast received... continue transfer");
            ContinueTransfer();
        }
    };


    private void launchDataCollectionService() {
        File transferDir = new File( VZTransferConstants.VZTRANSFER_DIR);

        if ( transferDir.exists() ) {
            LogUtil.d(TAG, "Transfer Directory already exists..");
            if ( ! transferDir.isDirectory() ) {
                transferDir.delete();
                transferDir.mkdirs();
            }
        } else {
            LogUtil.d(TAG, "Transfer directory is not there, creating ");
            //If directory does not exists then create it
            transferDir.mkdirs();
        }
        Utils.cleanupSenderMediaFilesFromVZTransferDir();

        String[] mediaTypeArray =  CTGlobal.getInstance().getMediaTypeArray();
        for(int i=0;i<mediaTypeArray.length;i++) {

            CalculationAsyncTask calculationAsyncTask = new CalculationAsyncTask(activity,mediaTypeArray[i]);

            LogUtil.d(TAG, "Launching calculator async task....");
            if (Build.VERSION.SDK_INT >= 11) {
                LogUtil.d(TAG, "Launching single thread async task...");
                calculationAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
            } else {
                LogUtil.d(TAG, "Launching single thread async task...");
                calculationAsyncTask.execute();
            }
            CollectionTaskVO.getInstance().registerTask(mediaTypeArray[i],calculationAsyncTask);
        }
        if(VZTransferConstants.SUPPORT_PASSWORDS_DATABASE) {
            new BindPasswordManagerService(activity.getApplicationContext());
        }
    }


    private void launchWifiScan() {
        WifiScanner wifiAsyncTask = new WifiScanner(activity);

        LogUtil.d(TAG, "Launching launchWifiScan....");
        if (Build.VERSION.SDK_INT >= 11) {
            LogUtil.d(TAG,"Launching single thread async task...");
            wifiAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            LogUtil.d(TAG,"Launching single thread async task...");
            wifiAsyncTask.execute();
        }
    }

    public void continueContentTransfer() {
        if(CTGlobal.getInstance().isDoingOneToMany()){
            CTGlobal.getInstance().setIsCross(false);
        }else{
            CTGlobal.getInstance().setIsCross(CTDeviceComboModel.getInstance().isCross());
        }

        if(Utils.isThisServer()
                && Utils.getServerName().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)){
            WifiAccessPoint.getInstance().init(activity);
            WifiAccessPoint.getInstance().Start();

            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.settingup_wifi_hotspot), activity, false);
        }
        else {//rahiahm - No AP needed. Resume normal flow
            CTDeviceComboModel.getInstance().ContinueTransfer();
        }

        ContentPreference.putBooleanValue(activity.getApplicationContext(), VZTransferConstants.IS_CT_FLOW_STARTED, true);
    }

    public void onClickSettingsIcon() {
        nextIntent = new Intent(activity, CTMultiPhoneTransferActivity.class);
        activity.startActivity(nextIntent);
    }

}
