package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.adobe.mobile.Config;
import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.activity.CTLandingActivity;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.p2p.service.SensorService;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by rahiahm on 7/19/2016.
 */
public class StartupModel {

    public static final String TAG = StartupModel.class.getName();

    private Activity activity;

    public StartupModel(Activity act, Intent i) {
        activity = act;
        CTErrorReporter.getInstance().Init(activity);




        CTGlobal.getInstance().setContentTransferContext(activity.getApplicationContext());
        readPropertyFile();
        Config.setDebugLogging(true);

        System.currentTimeMillis();
        String mdn = i.getStringExtra("mdn");
        String appType = i.getStringExtra("appType");
        if (mdn != null) {
            CTGlobal.getInstance().setMdn(mdn);
        }

        if(null != appType && appType.equals(VZTransferConstants.MVM)){
            CTGlobal.getInstance().setAppType(VZTransferConstants.MVM);
        }else {
            CTGlobal.getInstance().setAppType(VZTransferConstants.STANDALONE);
        }
        LogUtil.d(TAG,"Get app type :"+CTGlobal.getInstance().getAppType());
        activity.startActivity(new Intent(activity, CTLandingActivity.class));

    }


    private void readPropertyFile() {
        Properties props = new Properties();
        AssetManager assetManager = activity.getAssets();

        try {
            InputStream in = assetManager.open("version.properties");
            props.load(in);
            String localMinorVer = (props.getProperty("VERSION_MINOR")); //=1
            String localBuildVer = (props.getProperty("VERSION_CODE")); //=259
            String localMajorVer = (props.getProperty("VERSION_MAJOR")); //=2
            CTGlobal.getInstance().setBuildDate(props.getProperty("DATE"));
            LogUtil.DEBUG = BuildConfig.DEBUG_LOG;
            LogUtil.FILE_DEBUG = BuildConfig.FILE_DEBUG;
            CTGlobal.getInstance().setBuildVersion(localMajorVer + "." + localMinorVer + "." + localBuildVer);
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void resetWifiEnable() {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {

                WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    LogUtil.d(TAG, "Is WiFi Enabled : " + wifiManager.isWifiEnabled());
                    try {

                        wifiManager.setWifiEnabled(true);
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                LogUtil.d(TAG, "onDestroy called - resetWifiEnable");
            }
        }, 1);
    }

    public void cleanUp() {

        if (CTGlobal.getInstance().isWifiDirecct() && !CTBatteryLevelReceiver.exitForLowBattery) {
            LogUtil.d(TAG, "resetWifiEnable.. on onDestroy");
            resetWifiEnable();
        }
        CTBatteryLevelReceiver.exitForLowBattery = false;
    }

    public void collectLifeCylce() {
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("content transfer", "transfer");

        Config.collectLifecycleData(activity, contextData);
    }

    public void stopSensor(){
        Intent sensorIntent = new Intent(SensorService.STOP_SENSOR_SERVICE);
        activity.sendBroadcast(sensorIntent);
    }

    public void resetConnection(){
        ConnectionManager.resetWifiConnectionOnlyAfterCrash();
    }
}
