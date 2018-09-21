package com.verizon.contenttransfer.p2p.accesspoint;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;
/**
 * Created by rahiahm on 4/26/2016.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WifiAccessPoint implements WifiP2pManager.ConnectionInfoListener,WifiP2pManager.ChannelListener,WifiP2pManager.GroupInfoListener{

    /*static final public String DSS_WIFIAP_VALUES = "CT.MYWIFIMESH.DSS_WIFIAP_VALUES";
    static final public String DSS_WIFIAP_MESSAGE = "CT.MYWIFIMESH.DSS_WIFIAP_MESSAGE";
    */
    static final public String DSS_WIFIAP_SERVERADDRESS = "CT.MYWIFIMESH.DSS_WIFIAP_SERVERADDRESS";
    static final public String DSS_WIFIAP_INETADDRESS = "CT.MYWIFIMESH.DSS_WIFIAP_INETADDRESS";

    public static final String SERVICE_TYPE = "_wdm_p2p._tcp";
    private static final String TAG = "WifiAccessPoint";
    private static WifiAccessPoint instance;
    private static boolean serviceStarted = false;
    WifiAccessPoint that = this;
    LocalBroadcastManager broadcaster;
    Context context;

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel channel;
    private int timeoutCount = 10;
    String mNetworkName = "";
    String mPassphrase = "";
    String mInetAddress = "";

    private BroadcastReceiver receiver;
    private IntentFilter filter;

    public void init(Context Context) {
        this.context = Context;
        this.broadcaster = LocalBroadcastManager.getInstance(this.context);
    }
    public static WifiAccessPoint getInstance() {
        if (instance == null) {
            instance = new WifiAccessPoint();
        }
        return instance;
    }

    public static void killInstance() {
        instance = null;
    }


    public void Start() {
        LogUtil.d(TAG,"Start Wifi access point.");
        startTimeoutCounter();

        WiFiDirectActivity.cancelP2P();

        p2p = (WifiP2pManager) this.context.getSystemService(this.context.WIFI_P2P_SERVICE);
        if(Utils.isAPILevelAndroidO()){
            timeoutCount = 0;
        }else if (p2p == null) {
            LogUtil.e(TAG, "This device does not support WiFi Direct");
        } else {

            channel = p2p.initialize(this.context, this.context.getMainLooper(), this);

            receiver = new AccessPointReceiver();
            filter = new IntentFilter();
            filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
            filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
            this.context.registerReceiver(receiver, filter);

            p2p.createGroup(channel,new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    LogUtil.e(TAG, "Creating Local Group ");
                }

                public void onFailure(int reason) {
                    LogUtil.e(TAG, "Local Group failed, error code " + reason);
                }
            });
        }
    }

    private void startTimeoutCounter(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (!serviceStarted && ++count < timeoutCount) {
                    try {
                        LogUtil.e(TAG, "Waiting to start the hotspot service");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!serviceStarted) {
                    Stop();
                    Intent intent = new Intent(VZTransferConstants.ACCESS_POINT_UPDATE);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }
        });
    }

    public void Stop() {
        try{
            if(null != receiver) {
                try {
                    this.context.unregisterReceiver(receiver);
                } catch(Exception e){
                    //Don't do anything. Receiver was already unregisterd.
                    LogUtil.d(TAG, "Soft Access Point receiver already unregisterd");
                }
            }
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        stopLocalServices();
        removeGroup();
        LogUtil.d(TAG, "Stopped soft access point.");
    }

    public void removeGroup() {
        if(p2p != null && channel != null) {
            p2p.removeGroup(channel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    LogUtil.e(TAG, "Cleared Local Group ");
                }

                public void onFailure(int reason) {
                    LogUtil.e(TAG, "Clearing Local Group failed, error code " + reason);
                }
            });
        }
    }

    @Override
    public void onChannelDisconnected() {
        // see how we could avoid looping
        //     p2p = (WifiP2pManager) this.context.getSystemService(this.context.WIFI_P2P_SERVICE);
        //     channel = p2p.initialize(this.context, this.context.getMainLooper(), this);
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        LogUtil.d(TAG, "info available for group " + group.toString());
        try {
            Collection<WifiP2pDevice> devlist = group.getClientList();

            int numm = 0;
            for (WifiP2pDevice peer : group.getClientList()) {
                numm++;
                LogUtil.e(TAG, "Client " + numm + " : " + peer.deviceName + " " + peer.deviceAddress);
            }

            if(mNetworkName.equals(group.getNetworkName()) && mPassphrase.equals(group.getPassphrase())){
                LogUtil.e(TAG, "Already have local service for " + mNetworkName + " ," + mPassphrase);
            }else {
                mNetworkName = group.getNetworkName();
                mPassphrase = group.getPassphrase();
                startLocalService("NI:" + group.getNetworkName() + ":" + group.getPassphrase() + ":" + mInetAddress);
            }
            CTGlobal.getInstance().setHotspotName(group.getNetworkName());
        } catch(Exception e) {
            LogUtil.e(TAG, "onGroupInfoAvailable, error: " + e.toString());
        }
    }

    private void startLocalService(String instance) {

        Map<String, String> record = new HashMap<String, String>();
        record.put("available", "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance( instance, SERVICE_TYPE, record);

        LogUtil.e(TAG, "Add local service :" + instance);
        p2p.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                serviceStarted = true;
                LogUtil.d(TAG, "Added local service");
                HotSpotInfo.setHotspotPass(mPassphrase);
                HotSpotInfo.setHotspotSSID(mNetworkName);
                HotSpotInfo.setIsDeviceHotspot(true);

                Intent intent = new Intent(VZTransferConstants.ACCESS_POINT_UPDATE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            public void onFailure(int reason) {
                LogUtil.e(TAG, "Adding local service failed, error code " + reason);
            }
        });
    }

    private void stopLocalServices() {
        mNetworkName = "";
        mPassphrase = "";
        if(p2p != null && channel != null) {
            p2p.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    LogUtil.e(TAG, "Cleared local services");
                    HotSpotInfo.setIsDeviceHotspot(false);
                }

                public void onFailure(int reason) {
                    LogUtil.e(TAG, "Clearing local services failed, error code " + reason);
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        try {
            if (info.isGroupOwner) {
                if(broadcaster != null) {
                    mInetAddress = info.groupOwnerAddress.getHostAddress();
                    Intent intent = new Intent(DSS_WIFIAP_SERVERADDRESS);
                    intent.putExtra(DSS_WIFIAP_INETADDRESS, (Serializable)info.groupOwnerAddress);
                    broadcaster.sendBroadcast(intent);
                }
                p2p.requestGroupInfo(channel,this);
            } else {
                LogUtil.e(TAG, "we are client !! group owner address is: " + info.groupOwnerAddress.getHostAddress());
            }
        } catch(Exception e) {
            LogUtil.e(TAG, "onConnectionInfoAvailable, error: " + e.toString());
        }
    }

    private class AccessPointReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // startLocalService();
                } else {
                    //stopLocalService();
                    //Todo: Add the state monitoring in higher level, stop & re-start all when happening
                }
            }  else if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    LogUtil.e(TAG, "We are connected, will check info now");
                    p2p.requestConnectionInfo(channel, that);
                } else{
                    LogUtil.e(TAG, "We are DIS-connected");
                }
            }
        }
    }
}
