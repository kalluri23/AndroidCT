package com.verizon.contenttransfer.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;


//import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;
    public static final String TAG = "WiFiDirectBroadRec";
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (P2PStartupActivity.contentTransferContext == null
                    || CTGlobal.getInstance().getExitApp()) {
                return;
            }
            String action = intent.getAction();
            LogUtil.d(TAG,"Wifi direct broadcast receiver - action="+action);
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                LogUtil.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Utils.setIsWifiP2pEnabled(true);
                } else {
                    Utils.setIsWifiP2pEnabled(false);
                    String activityName = CTBatteryLevelReceiver.getTopActivityName();
                    if(null != Utils.getListener() && activityName.contains(VZTransferConstants.wiFiDirectActivity)){
                        Utils.getListener().resetData();
                    }


                }
                LogUtil.d(TAG, "P2P state changed - " + state);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                LogUtil.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
                if (manager != null) {
                    manager.requestPeers(channel, (WifiP2pManager.PeerListListener) activity.getFragmentManager()
                            .findFragmentById(R.id.frag_list));
                }else {
                    LogUtil.d(TAG,"manager is null");
                }

                LogUtil.d(TAG, "P2P peers changed");

                DeviceIterator fragment = (DeviceIterator) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                if(null != fragment) {
                    fragment.peerDiscoverStateChanged((WifiP2pDevice) intent.getParcelableExtra(
                            WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                }else {
                    LogUtil.d(TAG,"fragment is null");
                }

            }

            else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                LogUtil.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                if (manager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    Log.d(TAG, "Connected to p2p network. Requesting network details");
                    // You can also include some extra data.
                    LogUtil.d(TAG,"network info getState :"+networkInfo.getState());

                    LogUtil.d(TAG, "Stoping peer discovery after wifi direct connection establish.");
                    WiFiDirectActivity.StopPeerDiscovery();
                    QRCodeUtil.getInstance().cancelTimeoutTimer();

                    manager.requestConnectionInfo(channel, Utils.getListener());
                } else {

                    Log.d(TAG, "isConnected is false , top activity =" + CTBatteryLevelReceiver.getTopActivityName());


                    if (WiFiDirectActivity.selectedListitemPosition != -1){
                        Log.d(TAG, "It's a dis connect");


                        if(CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.iOSSenderActivity)){
                            LogUtil.d(TAG, "Closing all sockets...");
                            SocketUtil.disconnectAllSocket();
                        }else if(CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.iOSTransferActivity)){
                            Intent p2pTransferActivityReceiver = new Intent("calcupdate");
                            p2pTransferActivityReceiver.putExtra("MESSAGE", VZTransferConstants.WIFIDIRECT_CONNECTION_DISCONNECTED);
                            LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(p2pTransferActivityReceiver);
                        }else if(CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.wiFiDirectActivity)){
                            if(QRCodeUtil.getInstance().getQrCodeVO() != null
                                    && QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)){
                                QRCodeUtil.getInstance().cancelTimeoutTimer();


                                Intent widiErrorIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTErrorMsgActivity.class);
                                widiErrorIntent.putExtra("screen", VZTransferConstants.WIDI_ERROR);
                                widiErrorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                CTGlobal.getInstance().getContentTransferContext().startActivity(widiErrorIntent);
                            }
                        }


                    }else if(CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.iOSReceiverActivity)){
                        LogUtil.e(TAG, "wifi direct connection dropped at sender side.");
                        if(SocketUtil.getClientSocket(null) != null){
                            try{
                                LogUtil.e(TAG, "Closing iosServerSocket.");
                                CTGlobal.getInstance().setConnectionFailed(true);
                                SocketUtil.getClientSocket(null).close();
                            }catch (Exception e){
                                LogUtil.d(TAG, "ios Server socket close exception.");
                                e.printStackTrace();
                            }
                        }
                    }else if(CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.wiFiDirectActivity)){
                        LogUtil.d(TAG,"wifi direct connection dropped, try again..");
                        sendBroadcastToRestartWiDiDiscovery();
                    }
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                LogUtil.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                DeviceIterator fragment = (DeviceIterator) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                WiFiDirectActivity.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));


                WifiP2pDevice device = (WifiP2pDevice) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                LogUtil.d(TAG, "Device status -" + device.status);

            }else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
                LogUtil.d(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION");
                int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);

                if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    LogUtil.d(TAG, "WIFI_P2P_DISCOVERY_STARTED");
                } else if( discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED ) {
                    LogUtil.d(TAG, "WIFI_P2P_DISCOVERY_STOPPED");
                    DeviceIterator fragment = (DeviceIterator) activity.getFragmentManager()
                            .findFragmentById(R.id.frag_list);
                    if(null != fragment) {
                        fragment.peerDiscoverStateChanged((WifiP2pDevice) intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                    }
                    sendBroadcastToRestartWiDiDiscovery();
                }
            }
        }catch (Exception e){
            LogUtil.d(TAG,e.getMessage());
        }
    }

    private void sendBroadcastToRestartWiDiDiscovery() {
        String activityName = CTBatteryLevelReceiver.getTopActivityName();
        LogUtil.d(TAG,"Top Activity name ="+activityName);
        if(activityName.contains(VZTransferConstants.wiFiDirectActivity)){
            LogUtil.d(TAG, "Req to rediscover wifi direct peer.");

            //Restarting Wifi Direct discovery for user second try.
            Intent resetWiDiManager = new Intent("restore-wifi-connection");
            resetWiDiManager.putExtra("message", "restart-widi-discovery");
            LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(resetWiDiManager);
        }

    }

}
