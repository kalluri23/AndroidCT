package com.verizon.contenttransfer.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.verizon.contenttransfer.activity.CTDeviceComboActivity;
import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.activity.CTGettingReadyReceiverActivity;
import com.verizon.contenttransfer.activity.CTLandingActivity;
import com.verizon.contenttransfer.activity.CTMultiPhoneTransferActivity;
import com.verizon.contenttransfer.activity.CTReceiverActivity;
import com.verizon.contenttransfer.activity.CTReceiverPinActivity;
import com.verizon.contenttransfer.activity.CTSenderActivity;
import com.verizon.contenttransfer.activity.CTSenderPinActivity;
import com.verizon.contenttransfer.activity.CTWifiSetupActivity;
import com.verizon.contenttransfer.activity.P2PSetupActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.activity.TNCActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.List;

/**
 * Created by duggipr on 2/26/2016.
 */
public class CTBatteryLevelReceiver extends BroadcastReceiver {
    private static final String TAG = CTBatteryLevelReceiver.class.getName();

    public static boolean exitForLowBattery = false;
    public static boolean isBatteryLevelAlertDialogDisplay = false;
    @Override
    public void onReceive(Context context, Intent intent) {


/*        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        if(usbCharge || acCharge){*/

/*        }else{*/
            LogUtil.d(TAG, "CTBatteryLevelReceiver onReceive.  -- isBatteryLevelAlertDialogDisplay:"+isBatteryLevelAlertDialogDisplay);
        try{
            if(!isBatteryLevelAlertDialogDisplay) {
                float batteryPct = getBatteryLevel();

                LogUtil.d(TAG,"batteryPct ="+batteryPct);
                if (batteryPct < VZTransferConstants.BATTERY_LEVEL) {
                    LogUtil.d(TAG, "CTBatteryLevelReceiver 1");
                    if (!isCharging()) {

                        LogUtil.d(TAG, "CTBatteryLevelReceiver 2");

                        Activity tempActivity = getTopActivity();
                        LogUtil.d(TAG,"Temp activity = "+tempActivity);
                        if(tempActivity != null){
                            boolean isExitApp = false;
                            if(tempActivity.getLocalClassName().contains(VZTransferConstants.landingActivity)
                                    || tempActivity.getLocalClassName().contains(VZTransferConstants.TncActvity)){
                                isExitApp = true;
                            }
                            isLowBattery(tempActivity,isExitApp);
                        }
                        LogUtil.d(TAG, "CTBatteryLevelReceiver 3");
                    }
                }
            }
        }catch (Exception e){
            LogUtil.d(TAG,"Battery Level exception :"+e.getMessage());
            e.printStackTrace();
        }
    }

    public static Activity getTopActivity(){
        Activity activity = null;
        try {
            String activityName = getTopActivityName();

            if(activityName.contains(VZTransferConstants.p2pSetupActivity)){
                activity = P2PSetupActivity.activity;
            }else if(activityName.contains(VZTransferConstants.p2pStartupActivity)){
                activity = P2PStartupActivity.activity;
            }else if(activityName.contains(VZTransferConstants.wiFiDirectActivity)){
                activity = WiFiDirectActivity.activity;
            }else if(activityName.contains(VZTransferConstants.p2PWifiSetupActivity)){
                activity = CTWifiSetupActivity.activity;
            }else if(activityName.contains(VZTransferConstants.activityDiscoveryNew)){
                activity = CTSenderPinActivity.activity;
            }else if(activityName.contains(VZTransferConstants.p2PReceiverActivity)){
                activity = CTReceiverPinActivity.activity;
            }else if(activityName.contains(VZTransferConstants.iOSTransferActivity)){
                activity = CTSelectContentModel.getInstance().getActivity();
            }else if(activityName.contains(VZTransferConstants.iOSSenderActivity)){
                activity = CTSenderActivity.activity;
            }else if(activityName.contains(VZTransferConstants.iOSReceiverActivity)){
                activity = CTReceiverActivity.activity;
            }else if(activityName.contains(VZTransferConstants.iOSGettingReadyReceiverActivity)){
                activity = CTGettingReadyReceiverActivity.activity;
            }else if(activityName.contains(VZTransferConstants.p2PFinishActivity)){
                activity = P2PFinishModel.getInstance().getActivity();
            }else if(activityName.contains(VZTransferConstants.CT_DEVICE_COMBO_ACTIVITY)){
                activity = CTDeviceComboActivity.activity;
            }else if(activityName.contains(VZTransferConstants.TncActvity)){
                activity =  TNCActivity.getActivity();
            }else if(activityName.contains(VZTransferConstants.landingActivity)){
                activity =  CTLandingActivity.activity;
            }else if(activityName.contains(VZTransferConstants.transferInterruptActivity)){
                activity =  CTLandingActivity.activity;
            }else if(activityName.contains(VZTransferConstants.ctMultiPhoneTransferActivity)){
                activity =  CTMultiPhoneTransferActivity.activity;
            }
            else {
                LogUtil.d(TAG, "Top Activity name ["+activityName+"] not found .");
            }
            LogUtil.d(TAG,"Activity initiated getLocalClassName :"+activity.getLocalClassName());
            LogUtil.d(TAG,"Activity initiated getPackageName :"+activity.getPackageName());

        }catch (Exception e){
            LogUtil.d(TAG,"getTopActivityName exception = "+e.getMessage());
        }
        return activity;
    }

    public static String getTopActivityName() {
        if(null == P2PStartupActivity.contentTransferContext){
            return "";
        }
        ActivityManager am = (ActivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.ACTIVITY_SERVICE);
        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        String topActivityName = taskInfo.get(0).topActivity.getClassName();
        return topActivityName;
    }

    public static float getBatteryLevel() {
        float batLevel = 50.0f;
        try {
            if(P2PStartupActivity.activity != null) {
                Intent batteryIntent = P2PStartupActivity.activity.registerReceiver(
                        null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                // Error checking that probably isn't needed but I added just in case.
                if (level == -1 || scale == -1) {
                    return batLevel;
                }

                batLevel = ((float) level / (float) scale) * 100.0f;
            }
        }catch (Exception e){
            LogUtil.d(TAG,"getBatteryLevel exception :"+e.getMessage());
        }
        return batLevel;
    }

    public static boolean isCharging()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = CTGlobal.getInstance().getContentTransferContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean bCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        LogUtil.d(TAG, "is charging.."+bCharging);
        return bCharging;
    }
    public static void isLowBattery(Activity actv,boolean isExitApp){
        LogUtil.d(TAG,"isLowBattery...");
        if(!isBatteryLevelAlertDialogDisplay){
            String currentActivityName = actv.getLocalClassName();
            LogUtil.d(TAG,"Current activity name ="+currentActivityName);
            if (!currentActivityName.contains(VZTransferConstants.p2PFinishActivity)
                    && !currentActivityName.contains(VZTransferConstants.transferSummaryActivity)
                    && !currentActivityName.contains(VZTransferConstants.ctTransferStatusActivity)
                    && !currentActivityName.contains(VZTransferConstants.transferInterruptActivity)
                    && !currentActivityName.contains(VZTransferConstants.ctErrorMsgActivity)) {
                float batteryPct = getBatteryLevel();
                LogUtil.d(TAG,"Battery percentage ="+batteryPct);
                if (batteryPct < VZTransferConstants.BATTERY_LEVEL) {
                    if(!isCharging()) {
                        isBatteryLevelAlertDialogDisplay = true;
                        LogUtil.d(TAG, "Exiting CT app.. because of low battery");
                        Intent intent = new Intent(actv, CTErrorMsgActivity.class);
                        intent.putExtra("activity", actv.getLocalClassName());
                        intent.putExtra("screen", VZTransferConstants.LOW_BATTERY);
                        intent.putExtra("isExitApp",isExitApp);
                        actv.startActivity(intent);
                    }
                }
            }
        }
    }
}
