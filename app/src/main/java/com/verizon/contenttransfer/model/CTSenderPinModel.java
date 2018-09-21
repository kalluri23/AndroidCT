package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTGettingReadyReceiverActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatConstants;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.base.VersionCheckReceiver;
import com.verizon.contenttransfer.exceptions.SiteCatLogException;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.ContentTransferAnalyticsMap;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTSenderPinView;

import java.util.HashMap;

/**
 * Created by duggipr on 9/6/2016.
 */
public class CTSenderPinModel {

    private static final String TAG = CTSenderPinModel.class.getName();

    private static final String INFO = "Content Transfer";

    private Activity activity;

    //public static ProgressDialog p2pconnectionDiaLogUtil=null;

    private BroadcastReceiver versionReceiver = null;
    private CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();


    public CTSenderPinModel(Activity activity){
        this.activity = activity;
        versionReceiver = new VersionCheckReceiver(activity, "Enter pin screen");
        try {
            iCTSiteCat.getInstance().trackStateGlobal(CTSiteCatConstants.SITECAT_VALUE_PHONE_PIN, null);
        } catch (SiteCatLogException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }


    public void updateWifiConnectionStatus() {
        try{
            if(P2PStartupActivity.contentTransferContext != null) {
                WifiManager wifiManager1 = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = "";
                //rahiaham - When the device acts as a hotspot, don't get the ssid. Network will be VZTransfer

                ssid = wifiManager1.getConnectionInfo().getSSID().replaceAll("\"","");
                LogUtil.d(TAG,"updateWifiConnectionStatus 2.. ssid="+ssid);

                if (CTGlobal.getInstance().getStoreMacId() != null) {
                    if (Utils.isConnectedViaWifi()) {
                        CTSenderPinView.getInstance().updateConnection(ssid);
                    } else {
                        CTSenderPinView.getInstance().updateConnection(activity.getString(R.string.ct_hotspot_find_conn_status));
                    }
                }


                CTGlobal.getInstance().setHotspotName(ssid);
            }
        }catch (Exception e){
            LogUtil.e(TAG,"updateWifiConnectionStatus exception :"+e.getMessage());
        }

    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }

            String message = intent.getStringExtra("message");
           /* (activity.findViewById(R.id.connectbtn)).setEnabled(true);
            (activity.findViewById(R.id.connectbtn)).setBackgroundResource(R.drawable.vz_red_solid_rect_button);*/

            LogUtil.d(TAG, "Got message: " + message);
            if(null != message){

                if(message.equals(VZTransferConstants.CLIENT_KEY_UPDATE_BROADCAST)){
                    HashMap<String,Object> eventMap = new HashMap<String, Object>();
                    eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
                    eventMap.put(ContentTransferAnalyticsMap.PAIRINGSTATUS,"Conntection Failed");
                    CTSenderPinView.getInstance().enableConnect(true);
                    CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                            activity.getString(R.string.conntectionfailed),
                            activity,
                            activity.getString(R.string.msg_ok), -1).show();
                }
            }
        }
    };

    private BroadcastReceiver mUpdateSSIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            //update SSID.
            updateWifiConnectionStatus();
        }
    };

    private BroadcastReceiver mVersionCheckSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "Broadcast received .. Version check success .. ");

            //pairDetected = true;
            LogUtil.d(TAG, "Version check success ..");
            String hostReceived = intent.getStringExtra("ipAddressOfServer");

            if(Utils.isReceiverDevice()){
                LogUtil.d(TAG, "Version check success .. new phone. go to getting ready page.");

                Intent iOSIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTGettingReadyReceiverActivity.class);
                iOSIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isServer", true);
                bundle.putString("ipAddressOfServer", hostReceived);
                iOSIntent.putExtras(bundle);
                CTGlobal.getInstance().getContentTransferContext().startActivity(iOSIntent);
            }else {
                LogUtil.d(TAG, "Version check success .. old phone. go to select item page.");

                //Version is up to date, proceed with the transfer
                CustomDialogs.dismissDefaultProgressDialog();
/*                if (!CTGlobal.getInstance().isCross()) {
                    Utils.isSendingDevice = true;

                    Utils.startP2PClientIos(hostReceived);

                    Intent intent1 = new Intent(activity, CTSelectContentActivity.class);
                    //Intent intent1 = new Intent(context, OneTouchTransferActivity.class);

                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isServer", false);
                    bundle.putString("ipAddressOfServer", hostReceived);
                    intent1.putExtras(bundle);
                    context.startActivity(intent1);


                } else {*/
                    //Utils.isSendingDevice = true;

                    //Utils.startP2PClientIos(hostReceived);
                Utils.startSelectContentActivity();
/*                    Intent intent1 = new Intent(context, CTSelectContentActivity.class);

                    //intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();

                    bundle.putBoolean("isServer", false);
                    bundle.putString("ipAddressOfServer", hostReceived);
                    intent1.putExtras(bundle);
                    context.startActivity(intent1);*/

                    //Dont Close the client socket here we are going to use this for datatransfer in P2PClientIOS class
/*                }*/

                LogUtil.d(TAG, "Launching the transfer activity");
            }
        }
    };
/*    private BroadcastReceiver mNewClientConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "Broadcast received .. new client is connected. Start version check.");
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            //handshakeStarted = true;

            String host = intent.getStringExtra("host");
            String connType = intent.getStringExtra("conntype");
            LogUtil.d(TAG, "Broadcast received .. new client is connected. Start version check.");

            Utils.startClientVersionCheckAsyncTask(host, connType);


        }
    };*/
/*    private BroadcastReceiver mNewServerAccepted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "Broadcast received .. new client is accepted. Start version check.");
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            //handshakeStarted = true;

            String clientIp = intent.getStringExtra("clientip");
            String connType = intent.getStringExtra("conntype");
            LogUtil.d(TAG, "Broadcast received .. new client is accepted. Start version check.");
            LogUtil.d(TAG, "Client ip :"+clientIp);
            LogUtil.d(TAG, "Conn Type :"+connType);
            Utils.startServerVersionCheckAsyncTask(clientIp, connType);

        }
    };*/

    public void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter(VZTransferConstants.CLIENT_KEY_UPDATE_BROADCAST));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mUpdateSSIDReceiver, new IntentFilter("update-ssid"));


        LocalBroadcastManager.getInstance(activity).registerReceiver(versionReceiver, new IntentFilter(VZTransferConstants.VERSION_CHECK_FAILED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mVersionCheckSuccess, new IntentFilter(VZTransferConstants.VERSION_CHECK_SUCCESS));
        /*LocalBroadcastManager.getInstance(activity).registerReceiver(mNewServerAccepted, new IntentFilter(VZTransferConstants.SERVER_ACCEPTED_CLIENT));*/
/*        LocalBroadcastManager.getInstance(activity).registerReceiver(mNewClientConnected, new IntentFilter(VZTransferConstants.CLIENT_CONNECTED_TO_SERVER));*/

    }

    public void unregisterReciever() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mUpdateSSIDReceiver);

       /* LocalBroadcastManager.getInstance(activity).unregisterReceiver(mNewServerAccepted);*/
/*        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mNewClientConnected);*/
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mVersionCheckSuccess);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(versionReceiver);
    }
}
