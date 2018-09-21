package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.activity.CTWifiSetupActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.SensorService;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTDeviceIteratorView;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTDeviceIteratorModel {

    private static final String TAG = CTDeviceIteratorModel.class.getName();
    private static CTDeviceIteratorModel instance;
    private Activity activity;
    private Dialog qrErrorDialog;


    public WifiP2pDevice device;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();


    public WifiP2pConfig config = new WifiP2pConfig();

    public void initModel(Activity activity){
        this.activity = activity;

    }

    public static CTDeviceIteratorModel getInstance(){
        if(instance == null){
            instance = new CTDeviceIteratorModel();
        }
        return instance;
    }

    public void killInstance(){
        peers.clear();
        instance = null;
    }

    public WifiP2pConfig getConfig() {
        return config;
    }

    public void setConfig(WifiP2pConfig config) {
        this.config = config;
    }
    public List<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void setPeers(List<WifiP2pDevice> peers) {
        this.peers = peers;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
    public void onResume(){
        QRCodeUtil.getInstance().setQrCodeVO(null);
        CTDeviceIteratorView.getInstance().showInvitationLayout(false);
        LogUtil.d(TAG, "Device iterator... onresume - Start wifi direct discovery....");
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (QRCodeUtil.getInstance().isReturnedFromQRActivity()) {
                    LogUtil.d(TAG, "returned From QRActivity - ignore wifi direct discovery...");

                    processQRCode();
                } else {
                    WiFiDirectActivity.startWiDiDiscovery(activity);
                }
            }
        }, 0);
        CTDeviceIteratorModel.getInstance().setDevice(null);
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(timeoutReceiver, new IntentFilter(VZTransferConstants.TIMER_TIME_OUT));
    }

    private void processQRCode() {

        LogUtil.d(TAG, "Process QR code - scanned code :" + QRCodeUtil.getInstance().getScannedQRCode());
        QRCodeUtil.getInstance().setQrCodeVO(QRCodeUtil.getInstance().resultQRCodeVO(activity));
        if (QRCodeUtil.getInstance().getQrCodeVO() != null) {

            if(VZTransferConstants.INVALID_QR_CODE.equals(QRCodeUtil.getInstance().getQrCodeVO().getStatus())){
                LogUtil.d(TAG,"show error dialog.");
            }else{
                LogUtil.d(TAG,"getStatus :"+QRCodeUtil.getInstance().getQrCodeVO().getStatus());
                String combType = "";
                if (CTGlobal.getInstance().isCross()) {
                    combType = VZTransferConstants.CROSS_PLATFORM;
                } else if (CTGlobal.getInstance().isDoingOneToManyComb()) {
                    combType = VZTransferConstants.ONE_TO_MANY;
                } else {
                    combType = VZTransferConstants.SAME_PLATFORM;
                }
                LogUtil.d(TAG, "combtype :" + combType);
                if (QRCodeUtil.getInstance().getQrCodeVO().getCombinationType().equals(combType)) {
                    CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.qr_code_verify_message), activity, false);
                    String otherDeviceName = "";
                    LogUtil.d(TAG, "getConnectionType =" + QRCodeUtil.getInstance().getQrCodeVO().getConnectionType());
                    if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)) {
                        if (peers.size() == 1 && !CTGlobal.getInstance().isWidiDiscovering()) {
                            WiFiDirectActivity.startDiscoveryInBackground(activity);
                        }
                        QRCodeUtil.getInstance().setReturnedFromQRActivity(false);
                        LogUtil.d(TAG, "set returned from qr act..3 :" + QRCodeUtil.getInstance().isReturnedFromQRActivity());
                        otherDeviceName = QRCodeUtil.getInstance().getQrCodeVO().getSsid();
                        LogUtil.d(TAG, "other device name =" + otherDeviceName);
                        selectDeviceAndConnect(otherDeviceName);

                    } else if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)
                            || QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.PHONE_WIFI_CONNECTION)) {
                        //rahiahm - why are we doing any of this? Go straight to iDontSeeIt handler
                        //otherDeviceName = getActivity().getString(R.string.ct_dont_see_desc_tv);
                        //selectDeviceAndConnect(otherDeviceName);
                        LogUtil.d(TAG, "I don't see it function");
                        iDontSeeItFunction(activity);
                    }
                    LogUtil.d(TAG, "dismiss Default Progress Dialog");
                    CustomDialogs.dismissDefaultProgressDialog();
                } else {
                    ExitContentTransferDialog.alertToExitDialog(activity, activity.getString(R.string.ct_qr_error_title), activity.getString(R.string.invalid_device_combo_selection), activity.getString(R.string.start_over));
                }
            }


    }else {
            qrErrorDialog = CustomDialogs.createDialog(activity,
                    activity.getString(R.string.ct_qr_error_title),
                    activity.getString(R.string.ct_qr_error_desc1),
                    false, null, false, null, null,
                    true,activity.getString(R.string.msg_ok), new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            qrErrorDialog.dismiss();
                            WiFiDirectActivity.startWiDiDiscovery(activity);
                        }
                    });
        }
    }
    public void iDontSeeItFunction(Activity activity) {
        QRCodeUtil.getInstance().cancelTimeoutTimer();
        if (Utils.isWifiDirectSupported()) {
            //WiFiDirectActivity.StopPeerDiscovery();
            WiFiDirectActivity.cancelP2P();
        }

        LogUtil.d(TAG, "Broadcasting message");
        CTGlobal.getInstance().setIsWifiDirecct(false);
        Intent sensorIntent = new Intent(SensorService.STOP_SENSOR_SERVICE);

        activity.sendBroadcast(sensorIntent);
        LogUtil.d(TAG, "SensorService stoped");
        Intent wifiSetup = new Intent(activity, CTWifiSetupActivity.class);
        wifiSetup.putExtra("enableWifi", true);
        activity.startActivity(wifiSetup);
    }
    private void selectDeviceAndConnect(final String otherDeviceName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String deviceName = "";
                    boolean isDeviceFound = false;
                    int count = 0;
                    do {
                        LogUtil.d(TAG, "isWidi Discovering : " + CTGlobal.getInstance().isWidiDiscovering());
                        if(CTGlobal.getInstance().isWidiDiscovering()){
                            LogUtil.d(TAG,"Still discovering.. please wait.");
                            Thread.sleep(2000);
                        }

                        count++;
                        List<WifiP2pDevice> tempPeers = peers;
                        LogUtil.d(TAG, "Peers size =" + peers.size());
                        for (int i = 0; i < tempPeers.size(); i++) {
                            LogUtil.d(TAG, "Peers size... =" + tempPeers.size());

                            WifiP2pDevice wifiP2pDevice = tempPeers.get(i);

                            deviceName = wifiP2pDevice.deviceName;

                            LogUtil.d(TAG, "device name from peers : =" + deviceName);

                            if (deviceName.length() > 0 && otherDeviceName != null && otherDeviceName.length() > 0) {
                                if (deviceName.endsWith(otherDeviceName)) {
                                    isDeviceFound = true;
                                    LogUtil.d(TAG, "Device found, connecting to it.");

                                    CTDeviceIteratorView.getInstance().handleListItemClickEvent(i, wifiP2pDevice, null);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            executeNextEvent();
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                        LogUtil.d(TAG, "is device found : " + isDeviceFound);


                    } while (!isDeviceFound && count < 5);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void executeNextEvent() {
        if (device != null) {
            if (device.deviceName.equals(activity.getString(R.string.ct_dont_see_desc_tv))) {
                if(!QRCodeUtil.getInstance().isUsingQRCode()){
                    WifiManagerControl.closePendingAsyncTask(true, false);
                }
                CTDeviceIteratorModel.getInstance().iDontSeeItFunction(activity);
            } else {
                Utils.getListener().connect(config);
                CTDeviceIteratorView.getInstance().showInvitationLayout(true);
                LogUtil.d(TAG, "Sent WifiDirect Connection request.. and started timer. ");
                QRCodeUtil.getInstance().createTimer(VZTransferConstants.WIDI_CONNECTION_TIMEOUT);
            }
        }
    }

    public void onPause() {
        unregisterTimeoutListener();
    }

    public void unregisterTimeoutListener() {
        if(timeoutReceiver != null) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(timeoutReceiver);

        }
    }

    private BroadcastReceiver timeoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG,"Time out receiver receiver msg received.");
            if (Utils.isWifiDirectSupported()) {
                WiFiDirectActivity.cancelP2P();
            }
            Intent widiErrorIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTErrorMsgActivity.class);
            widiErrorIntent.putExtra("screen", VZTransferConstants.WIDI_ERROR);
            widiErrorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CTGlobal.getInstance().getContentTransferContext().startActivity(widiErrorIntent);
        }
    };
}
