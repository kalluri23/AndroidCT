package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTGettingReadyReceiverActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.base.VersionCheckReceiver;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.PinManager;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTReceiverPinView;

/**
 * Created by duggipr on 9/8/2016.
 */
public class CTReceiverPinModel {

    private Activity activity;
    private static final String TAG = CTReceiverPinModel.class.getName();
    private PinManager pinManager = null;
    private BroadcastReceiver versionReceiver = null;
    private static String code = null;
    private String currIP;
    private CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();
    private static CTReceiverPinModel instance;
    private Handler pinHandler = new Handler();
    private static final String INFO = "Content Transfer";
    private Dialog myDialog;

    private Runnable recurringTask = new Runnable() {
        @Override
        public void run() {
            updatethePin();
            pinHandler.postDelayed(this, 30000);
        }
    };

    public static CTReceiverPinModel getInstance() {
        if (instance == null) {
            instance = new CTReceiverPinModel();
        }
        return instance;
    }

    public void initReceiverPinModel(Activity activity) {
        this.activity = activity;

        Utils.startP2PServerTask(activity);

        versionReceiver = new VersionCheckReceiver(activity, "Pin Screen");
        String localIP = getLocalIpAddress();
        String[] parts = localIP.split("\\.");
        code = parts[3];
        pinManager = new PinManager();
        String encodedPin = pinManager.encodePin( code );
        currIP = localIP;
        ((TextView) activity.findViewById(R.id.ct_pairing_code_tv)).setText(encodedPin);
    }

    public void updateWifiConnectionStatus() {
        if(P2PStartupActivity.contentTransferContext != null) {
            WifiManager wifiManager1 = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);

            String ssid = wifiManager1.getConnectionInfo().getSSID().replaceAll("\"","");

            if (CTGlobal.getInstance().getStoreMacId() != null) {

                if (Utils.isConnectedViaWifi()) {
                    CTReceiverPinView.getInstance().updateConnection(ssid);
                } else {
                    CTReceiverPinView.getInstance().updateConnection(activity.getString(R.string.ct_hotspot_find_conn_status));
                }
            }


            CTGlobal.getInstance().setHotspotName(ssid);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            updateLocalIpAddress();

        }
    };

    private void updateLocalIpAddress() {
        //IP update after wifi connection verified
        String localIP = getLocalIpAddress();
        LogUtil.d(TAG,"localIP ="+localIP);
        if(!localIP.equals(currIP)) {
            String[] parts = localIP.split("\\.");
            code = parts[3];
            String encodedPin = pinManager.encodePin(code);
            LogUtil.d(TAG, "Encoded pin during update of IP : " + encodedPin);
            ((TextView) activity.findViewById(R.id.ct_pairing_code_tv)).setText(encodedPin);
            currIP = localIP;
        }
    }

    private BroadcastReceiver mUpdateSSIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            if(P2PStartupActivity.contentTransferContext == null){
                return;
            }
            //update SSID.
            LogUtil.d(TAG, "Wifi ssid update broadcast received.");
            updateLocalIpAddress();
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

/*                if (!CTGlobal.getInstance().isCross()) {
                    Utils.isSendingDevice = true;

                    Utils.startP2PClientIos(hostReceived);

                    Intent intent1 = new Intent(activity, CTSelectContentActivity.class);

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
    private BroadcastReceiver totalConnectionCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connCountLimitExit = false;
            CTReceiverPinView.getInstance().enableOneToManyNext(SocketUtil.getConnectedClients().size());
            if (HotSpotInfo.isDeviceHotspot()
                    && ServerConnectionObject.getInstance().getClients().size() >= Utils.getMaxConnectionCount(true)) {
                CTGlobal.getInstance().setWaitForNewDevice(false);
                connCountLimitExit = true;

            }else if(!HotSpotInfo.isDeviceHotspot()
                    && ServerConnectionObject.getInstance().getClients().size() >= Utils.getMaxConnectionCount(false)) {
                CTGlobal.getInstance().setWaitForNewDevice(false);
                connCountLimitExit = true;
            }
            if(connCountLimitExit){
                myDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.one_to_many_max_conn_count_msg), false, null,
                        false, null, null,
                        true, activity.getString(R.string.msg_ok), new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                myDialog.dismiss();
                                Utils.oneToManyNextButton(activity);
                            }
                        });
            }
        }
    };
    private BroadcastReceiver transferFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String hostReceived = intent.getStringExtra("host");
            String finishStatusMsg = intent.getStringExtra("finishstatusmsg");
            LogUtil.d(TAG, "transfer finished receiver - client exited before transfer start- host:" + hostReceived + " & Finish msg:" + finishStatusMsg);
            ServerConnectionObject.getInstance().deleteClientOnDisconnectBeforeTransfer(hostReceived);

            Utils.startSelectContentActivity();
        }
    };

    private String getLocalIpAddress() {
        WifiManager wifiMan = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }

    public void updatethePin(){
        final String encodedPin = pinManager.encodePin(code);
        LogUtil.d(TAG, "Encoded pin during 30 sec refresh : " + encodedPin);
        ((TextView) activity.findViewById(R.id.ct_pairing_code_tv)).setText(encodedPin);

    }

    public void cancelPinTimer()
    {
        pinHandler.removeCallbacks(recurringTask);
    }

    public void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter("ipupdate"));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mUpdateSSIDReceiver, new IntentFilter("update-ssid"));
        LocalBroadcastManager.getInstance(activity).registerReceiver(transferFinishReceiver, new IntentFilter(VZTransferConstants.TRANSFER_FINISHED_MSG));
        LocalBroadcastManager.getInstance(activity).registerReceiver(totalConnectionCount, new IntentFilter(VZTransferConstants.UPDATE_TOTAL_CONNECTION_COUNT));
        LocalBroadcastManager.getInstance(activity).registerReceiver(versionReceiver, new IntentFilter(VZTransferConstants.VERSION_CHECK_FAILED));
        LocalBroadcastManager.getInstance(activity).registerReceiver(mVersionCheckSuccess, new IntentFilter(VZTransferConstants.VERSION_CHECK_SUCCESS));
        /*LocalBroadcastManager.getInstance(activity).registerReceiver(mNewServerAccepted, new IntentFilter(VZTransferConstants.SERVER_ACCEPTED_CLIENT));*/
/*        LocalBroadcastManager.getInstance(activity).registerReceiver(mNewClientConnected, new IntentFilter(VZTransferConstants.CLIENT_CONNECTED_TO_SERVER));*/
    }

    public void unregisterReciever() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mUpdateSSIDReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(totalConnectionCount);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(transferFinishReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mVersionCheckSuccess);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(versionReceiver);
    }
}
