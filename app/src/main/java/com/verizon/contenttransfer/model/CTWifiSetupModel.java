package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTGettingReadyReceiverActivity;
import com.verizon.contenttransfer.activity.CTReceiverPinActivity;
import com.verizon.contenttransfer.activity.CTSenderPinActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.MyClipboardManager;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.base.VersionCheckReceiver;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTRouterConnectionQRUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTWifiSetupView;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTWifiSetupModel {

    private static final String TAG = CTWifiSetupModel.class.getName();
    private static CTWifiSetupModel instance;
    private Activity activity;
    private BroadcastReceiver versionReceiver = null;
    private Dialog notSameNetworkDialog;
    private Dialog tryAnotherDialog;
    private Dialog qrErrorDialog;
    private boolean pairDetected = false;
    private boolean handshakeStarted = false;
    private static final String INFO = "Content Transfer";
    private boolean performNextEvent = false;
    private boolean isConnEstablished = false;
    private boolean showRescanDialog = false;
    private Dialog myDialog;

    public void initModel(Activity activity) {
        this.activity = activity;
        performNextEvent = false;
        isConnEstablished = false;
        showRescanDialog = false;

        versionReceiver = new VersionCheckReceiver(this.activity, CTWifiSetupModel.class.getName());
        pairDetected = false;
        handshakeStarted = false;
    }

    public static CTWifiSetupModel getInstance() {
        if (instance == null) {
            instance = new CTWifiSetupModel();
        }
        return instance;
    }

    public void killInstance() {
        instance = null;
    }

    public boolean isPairDetected() {
        return pairDetected;
    }

    public void registerBroadcastReceivers() {
        if (null != activity) {
            LocalBroadcastManager.getInstance(activity).registerReceiver(mServerReceiver, new IntentFilter("serverupdate"));
            LocalBroadcastManager.getInstance(activity).registerReceiver(mClientReceiver, new IntentFilter("clientupdate"));
            LocalBroadcastManager.getInstance(activity).registerReceiver(versionReceiver, new IntentFilter(VZTransferConstants.VERSION_CHECK_FAILED));
            LocalBroadcastManager.getInstance(activity).registerReceiver(mVersionCheckSuccess, new IntentFilter(VZTransferConstants.VERSION_CHECK_SUCCESS));
            LocalBroadcastManager.getInstance(activity).registerReceiver(wifiResetCompleted, new IntentFilter("wifireset"));
            LocalBroadcastManager.getInstance(activity).registerReceiver(readyToAcceptConnection, new IntentFilter(VZTransferConstants.READY_TO_ACCEPT_CONNECTION));
            LocalBroadcastManager.getInstance(activity).registerReceiver(transferFinishReceiver, new IntentFilter(VZTransferConstants.TRANSFER_FINISHED_MSG));
            LocalBroadcastManager.getInstance(activity).registerReceiver(totalConnectionCount, new IntentFilter(VZTransferConstants.UPDATE_TOTAL_CONNECTION_COUNT));
            IntentFilter wifiFilter = new IntentFilter();
            wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            wifiFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            wifiFilter.addAction("android.net.wifi.STATE_CHANGE");
            activity.registerReceiver(mWifiConnReceiver, wifiFilter);
            LogUtil.d(TAG, "Registered broadcast events.");
        } else {
            LogUtil.i(TAG, "register Broadcast Receivers - Activity is null");

        }
    }

    private BroadcastReceiver wifiResetCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtil.d(TAG, "Broadcast received ... action=" + action);
            if (action.equals("wifireset")) {
                Intent intent1 = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                intent1.putExtra("only_access_points", true);
                intent1.putExtra("extra_prefs_show_button_bar", true);
                intent1.putExtra("wifi_enable_next_on_connect", true);
                activity.startActivityForResult(intent1, 1);
            }
        }
    };
    private BroadcastReceiver readyToAcceptConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtil.d(TAG, "Broadcast received ... is waiting to accept connection. =" + action);
            if (action.equals(VZTransferConstants.READY_TO_ACCEPT_CONNECTION)) {
                CustomDialogs.dismissDefaultProgressDialog();
            }
        }
    };

    public void unregisterReceivers() {
        if (null != activity) {
            try {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(mClientReceiver);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(mServerReceiver);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(mVersionCheckSuccess);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(versionReceiver);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(wifiResetCompleted);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(readyToAcceptConnection);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(transferFinishReceiver);
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(totalConnectionCount);

                activity.unregisterReceiver(mWifiConnReceiver);
                LogUtil.d(TAG, "Un Registered broadcast events.");

            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            }
        } else {
            LogUtil.i(TAG, "unregister Broadcast Receivers - Activity is null");
        }
    }


    private BroadcastReceiver mServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pairDetected = true;
            CustomDialogs.dismissDefaultProgressDialog();
            // If app crash, we don't need to do any action on broadcast receiver.
            if (P2PStartupActivity.contentTransferContext == null) {
                return;
            }

            startP2PAsReceiver(activity);
        }
    };

    private BroadcastReceiver mClientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            pairDetected = true;
            //rahaiham Dialog should be dismissed when going to the Pin page, not before.
            // If app crash, we don't need to do any action on broadcast receiver.
            if (P2PStartupActivity.contentTransferContext == null) {
                return;
            }
            String host = intent.getStringExtra("clientIp");
            String name = intent.getStringExtra("clientName");

            LogUtil.d(TAG, "Client broadcast received.. start p2p as sender...");
            startP2PAsSender(activity, host, name);
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

    private BroadcastReceiver totalConnectionCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connCountLimitExit = false;
            CTWifiSetupView.getInstance().enableOneToManyNext(SocketUtil.getConnectedClients().size());
            if (HotSpotInfo.isDeviceHotspot()
                    && ServerConnectionObject.getInstance().getClients().size() >= Utils.getMaxConnectionCount(true)) {
                CTGlobal.getInstance().setWaitForNewDevice(false);
                connCountLimitExit = true;

            } else if (!HotSpotInfo.isDeviceHotspot()
                    && ServerConnectionObject.getInstance().getClients().size() >= Utils.getMaxConnectionCount(false)) {
                CTGlobal.getInstance().setWaitForNewDevice(false);
                connCountLimitExit = true;
            }
            if (connCountLimitExit) {
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


    private BroadcastReceiver mVersionCheckSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            pairDetected = true;

            String hostReceived = intent.getStringExtra("ipAddressOfServer");

            LogUtil.d(TAG, "Version check success .. pair detected :" + pairDetected + "   hostReceived =" + hostReceived);

            if (Utils.isReceiverDevice()) {
                LogUtil.d(TAG, "Version check success .. new phone. go to getting ready page.");
                Intent iOSIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTGettingReadyReceiverActivity.class);
                iOSIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isServer", true);
                bundle.putString("ipAddressOfServer", hostReceived);
                iOSIntent.putExtras(bundle);
                CTGlobal.getInstance().getContentTransferContext().startActivity(iOSIntent);
            } else {
                LogUtil.d(TAG, "Version check success .. old phone. go to select item page. CTGloble.getInstance().isWaitForNewDevice():" + CTGlobal.getInstance().isWaitForNewDevice());
                //Version is up to date, proceed with the transfer
                LogUtil.d(TAG, "TODO -- set this as required : CTGloble.getInstance().setWaitForNewDevice(false)");
                Utils.startSelectContentActivity();
                LogUtil.d(TAG, "Launching the transfer activity -- P2PClient Host:" + hostReceived);
            }


        }
    };

    private BroadcastReceiver mWifiConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If app crash, we don't need to do any action on broadcast receiver.
            if (P2PStartupActivity.contentTransferContext == null) {
                return;
            }
            LogUtil.d(TAG, " -- mWifiConnReceiver intent.getAction() --- " + intent.getAction());
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                    || intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")
                    || intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {


                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        networkInfo.isConnected()) {
                    crossWifiAccessPointReset();
                } else {
                    if (!WifiManagerControl.isWifiEnabled(activity)) {
                        crossWifiAccessPointReset();
                    }
                }
                String ssid = ConnectionManager.getSSID(CTGlobal.getInstance().getContentTransferContext());
                LogUtil.d(TAG, "SSID :" + ssid);
                if (Utils.isConnectedViaWifi() && ssid.length() > 0
                        && !ssid.toUpperCase().startsWith(VZTransferConstants.DIRECT_WIFI)) {
                    CustomDialogs.dismissDefaultProgressDialog();


                }
                LogUtil.d(TAG, "Handle broadcast receiver ..update wifi conn status.");
                updateWifiConnectionStatus();


            }


            if(!CTGlobal.getInstance().isReadyToConnect()
                    && Utils.isThisServer()){
                if(Utils.isAPILevelAndroidO()){
                    LogUtil.d(TAG, "Android O - not supporting hotspot.");
                    CustomDialogs.dismissDefaultProgressDialog();
                }else {
                    LogUtil.d(TAG, "Show dialog - isReadyToConnect . : " + CTGlobal.getInstance().isReadyToConnect());
                    CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.getting_ready_please_wait), activity, false);
                }
            }

        }
    };

    public void handleResume(boolean enableWifi) {
        if (CTGlobal.getInstance().getExitApp()) {
            return;
        }

        CTWifiSetupView.getInstance().toggleConnectionCounterView(CTGlobal.getInstance().isDoingOneToMany() && !Utils.isReceiverDevice());


        if (enableWifi) {
            if (QRCodeUtil.getInstance().getQrCodeVO() != null
                    && QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)) {
                LogUtil.d(TAG, "create default progress dialog 2..");
                CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.connecting_wifi_please_wait), activity, false);
                //Dismiss continuous connecting to wifi hotspot dialog : Some devcie dont connect to 5G networks
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomDialogs.dismissDefaultProgressDialog();
                    }
                }, 60000);
            }
        }
        LogUtil.d(TAG, "Handle on resume..update wifi conn status.");
        updateWifiConnectionStatus();

        if (!handshakeStarted && HotSpotInfo.isDeviceHotspot()) {
            handshakeStarted = true;
            try {
                Utils.createServerConnectionObject(InetAddress.getByName(VZTransferConstants.HOTSPOT_IP));
            } catch (UnknownHostException e) {
                LogUtil.d(TAG, "UnknownHostException :" + e.getMessage());
            }
        }


        if (!CTGlobal.getInstance().isReadyToConnect()
                && Utils.isThisServer()) {
            LogUtil.d(TAG, "Show dialog - isReadyToConnect... : " + CTGlobal.getInstance().isReadyToConnect());
            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device), activity, false);
        }
    }


    private void crossWifiAccessPointReset() {
        WifiManager wifiManager1 = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager1.getConnectionInfo().getSSID();
        LogUtil.d(TAG, "Cross wifi access point reset =" + ssid);
        if (HotSpotInfo.isDeviceHotspot()) {
            HotSpotInfo.setIsDeviceHotspot(false);
            WifiAccessPoint.getInstance().Stop();
            LogUtil.d(TAG, "Cross wifi access point reset for wifi hotspot =" + ssid);
            CTWifiSetupView.getInstance().wifiUIReset();
        }
    }

    public void startP2PAsReceiver(Context context) {
        if (HotSpotInfo.isDeviceHotspot()) {
            LogUtil.d(TAG, "Start ct receiver pin activity.- start P2PServerTask");
            Utils.startP2PServerTask(activity);
        } else {
            LogUtil.d(TAG, "Start ct receiver pin activity. else part.");
            processPinFlow(true);
        }
    }

    private void processPinFlow(boolean isServer) {
        Intent intent;
        if (isServer) {
            intent = new Intent(activity, CTReceiverPinActivity.class);
        } else {
            intent = new Intent(activity, CTSenderPinActivity.class);
        }
        activity.startActivity(intent);
    }

    public void startP2PAsSender(Context context, String host, String name) {
        if (HotSpotInfo.isDeviceHotspot()) {
            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device), activity, false);
            Utils.CreateClientConnectionObject(VZTransferConstants.PHONE_WIFI_CONNECTION, host, null, activity);
        } else {
            processPinFlow(false);
        }
    }

    public void updateWifiConnectionStatus() {
        LogUtil.d(TAG, "update wifi conn status.");
        if (P2PStartupActivity.contentTransferContext != null) {
            String ssid = ConnectionManager.getSSID(CTGlobal.getInstance().getContentTransferContext());

            LogUtil.d(TAG, "SSID === " + ssid);
            boolean displayQrCode = false;
            if (QRCodeUtil.getInstance().isUsingQRCode()
                    && Utils.isThisServer()) {
                displayQrCode = true;
            }

            LogUtil.d(TAG, "UPDATE UI - Display qr code :" + displayQrCode);
            CTWifiSetupView.getInstance().updateConnectionUI(ssid, displayQrCode);

            if (HotSpotInfo.isDeviceHotspot() || ssid.toUpperCase().startsWith(VZTransferConstants.DIRECT_WIFI)) {
                CTGlobal.getInstance().setConnectionType(VZTransferConstants.HOTSPOT_WIFI_CONNECTION);
                if (Utils.isThisServer()) {
                    ssid = HotSpotInfo.getHotspotSSID();
                }
            } else {
                CTGlobal.getInstance().setConnectionType(VZTransferConstants.PHONE_WIFI_CONNECTION);
            }
            LogUtil.d(TAG, "ssid.....:" + ssid);
            if (!CTGlobal.getInstance().isDoingOneToMany()) {
                if (Utils.isReceiverDevice()) {
                    LogUtil.d(TAG, "handle new Phone Flow - ssid.. :" + ssid);
                    handleServerFlow(ssid);
                } else {//old phone
                    LogUtil.d(TAG, "old phone - ssid.....:" + ssid);
                    if (!displayQrCode && !QRCodeUtil.getInstance().isReturnedFromQRActivity()) {

                        LogUtil.d(TAG, "handle Old Phone Flow - ssid ..:" + ssid);
                        handleClientFlow(ssid);
                    }
                }
            } else {
                if (Utils.isReceiverDevice()) {
                    if (!displayQrCode && !QRCodeUtil.getInstance().isReturnedFromQRActivity()) {

                        LogUtil.d(TAG, "handle new Phone Flow - ssid :" + ssid);
                        handleClientFlow(ssid);
                    }
                } else {//old phone

                    LogUtil.d(TAG, "handle Old Phone Flow - ssid :" + ssid);
                    handleServerFlow(ssid);
                }
            }
        }
    }

    private void handleClientFlow(String ssid) {
        LogUtil.d(TAG, "handleClientFlow - SSID :[" + ssid + "] is connected via wifi :[" + Utils.isConnectedViaWifi() + "] isConnectingWifi : " + QRCodeUtil.getInstance().isConnectingWifi());
        if (ssid != null
                && ssid.length() > 0
                && !QRCodeUtil.getInstance().isConnectingWifi()) {
            if (Utils.isConnectedViaWifi()) {
                LogUtil.d(TAG, "wifi is connected..");
                CTWifiSetupView.getInstance().updateNoInternetAlert(false);
                if (CTGlobal.getInstance().isCross()
                        && HotSpotInfo.isDeviceHotspot()) {
                    LogUtil.d(TAG, "handle Old Phone Flow - if cross and hotspot");
                    CustomDialogs.dismissDefaultProgressDialog();
                } else {
                    LogUtil.d(TAG, "handle Old Phone Flow - else part");
                    if (QRCodeUtil.getInstance().isUsingQRCode()
                            && QRCodeUtil.getInstance().getQrCodeVO() != null) {
                        if (ssid.equals(QRCodeUtil.getInstance().getQrCodeVO().getSsid())) {
                            LogUtil.d(TAG, "Same network found.. performNextEvent=" + performNextEvent);
                            if (!performNextEvent) {
                                performNextEvent = true;
                                if (null != mWifiConnReceiver) {
                                    activity.unregisterReceiver(mWifiConnReceiver);
                                }
                                CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device), activity, false);
                                LogUtil.d(TAG, "******** make connection 3");
                                makeConnection();
                            }
                        } else {
                            LogUtil.d(TAG, "Not connected to Same network..");

                            if (!showRescanDialog) {
                                CustomDialogs.dismissDefaultProgressDialog();
                                showRescanDialog = true;
                                showWifiSettings();
                            }
                        }
                    } else {
                        LogUtil.d(TAG, "QR code VO is null");
                    }
                }
            } else {
                if (ssid != null
                        && !ssid.equals(VZTransferConstants.UNKNOWN_SSID)
                        && Utils.isGooglePhone()) {
                    LogUtil.d(TAG, "wifi is connected - google phone.." + ssid);
                    CustomDialogs.dismissDefaultProgressDialog();
                    CTWifiSetupView.getInstance().updateNoInternetAlert(true);

                }
            }

        } else {
            LogUtil.d(TAG, "Invalid SSID :" + ssid);
        }
    }

    public void openNotificationTray() {
        try {
            Object sbservice = activity.getSystemService("statusbar");

            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleServerFlow(String ssid) {
        LogUtil.d(TAG, "handle server flow - ssid :" + ssid);
        if (ssid != null && ssid.length() > 0) {
            CustomDialogs.dismissDefaultProgressDialog();
            if (Utils.isConnectedViaWifi()) {
                if (QRCodeUtil.getInstance().isUsingQRCode()
                        && !ssid.toUpperCase().startsWith(VZTransferConstants.DIRECT_WIFI)
                        && !isConnEstablished) {
                    if (QRCodeUtil.getInstance().getQrCodeVO() != null) {
                        if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.PHONE_WIFI_CONNECTION)) {
                            LogUtil.d(TAG, "Start p2p server task...");
                            CTWifiSetupView.getInstance().updateNoInternetAlert(false);
                            Utils.startP2PServerTask(activity);
                            isConnEstablished = true;
                        }
                    }
                }
            } else {
                if (!HotSpotInfo.isDeviceHotspot()) {
                    if (ssid != null
                            && !ssid.equals(VZTransferConstants.UNKNOWN_SSID)
                            && Utils.isGooglePhone()) {
                        LogUtil.d(TAG, "wifi is connected - google phone.." + ssid);
                        CustomDialogs.dismissDefaultProgressDialog();
                        CTWifiSetupView.getInstance().updateNoInternetAlert(true);
                    }
                }
            }
            LogUtil.d(TAG, "Start p2p server task.");
        }

        if (CTGlobal.getInstance().getStoreMacId() != null
                && CTGlobal.getInstance().getWarnWifi()
                && ssid.equals(VZTransferConstants.VERIZON_GUEST_WIFI)) {
            //yempasu - check if prompt should be shown
            CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,

                    activity.getString(R.string.wifi_warning),
                    activity,
                    activity.getString(R.string.msg_ok), -1).show();
            CTGlobal.getInstance().setWarnWifi(false);
            LogUtil.d(TAG, "Start p2p server task...");
        }
    }

    public void processQRCode() {
        //rahiahm - prevent from calling this after returning from wifi Settings
        QRCodeUtil.getInstance().setReturnedFromQRActivity(false);
        LogUtil.d(TAG, "set returned from qr act..1 :" + QRCodeUtil.getInstance().isReturnedFromQRActivity());
        LogUtil.d(TAG, "Process QR code - scanned code :" + QRCodeUtil.getInstance().getScannedQRCode());
        QRCodeUtil.getInstance().setQrCodeVO(QRCodeUtil.getInstance().resultQRCodeVO(activity));
        if (QRCodeUtil.getInstance().getQrCodeVO() != null) {

            if(VZTransferConstants.INVALID_QR_CODE.equals(QRCodeUtil.getInstance().getQrCodeVO().getStatus())){
                LogUtil.d(TAG,"show error dialog.");
            }else{
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
                    if (null != QRCodeUtil.getInstance().getQrCodeVO()) {
                        LogUtil.d(TAG, "******** QRCodeUtil.getInstance().getQrCodeVO().getConnectionType() =" + QRCodeUtil.getInstance().getQrCodeVO().getConnectionType());
                        if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)) {
                            //scanned a wifi direct code, not applicable here
                            qrErrorDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.ct_qr_error_title),
                                    activity.getString(R.string.ct_qr_error_desc2),
                                    false, null, false, null, null,
                                    true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            qrErrorDialog.dismiss();
                                            QRCodeUtil.getInstance().launchQRCodeActivity(activity);
                                        }
                                    });
                            return;
                        } else if (QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)
                                && !Utils.isGooglePhone()) {
                            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.connecting_hostspot_please_wait), activity, false);
                            //rahiahm - why do we have this dialog?
                            //QRCodeUtil.getInstance().setConnectingWifi(true);
                            LogUtil.e(TAG, "connect To WifiAsyncTask called");
                            WifiManagerControl.connectToWifiAsyncTask(activity);
                        } else if (QRCodeUtil.getInstance().getQrCodeVO().getCombinationType().toLowerCase().contains("cross")) {
                            //Cross platform router
                            if (notSameNetworkDialog == null || (notSameNetworkDialog != null && !notSameNetworkDialog.isShowing())) {
                                showWifiSettings();
                            }
                        } else {
                            //same platform router
                            LogUtil.d(TAG, "qr code vo ssid :" + QRCodeUtil.getInstance().getQrCodeVO().getSsid() +
                                    "   notSameNetworkDialog =" + notSameNetworkDialog);
                            if (QRCodeUtil.getInstance().getQrCodeVO().getSsid().contains("DIRECT")
                                    && Utils.isGooglePhone()) { //Not Hotspot
                                if (notSameNetworkDialog == null ||
                                        (notSameNetworkDialog != null &&
                                                !notSameNetworkDialog.isShowing())) {
                                    //copy password into clipboard.
                                    boolean isPswCopiedToClipboard = MyClipboardManager.copyToClipboard(activity, QRCodeUtil.getInstance().getQrCodeVO().getPassword());
                                    LogUtil.d(TAG, "isPswCopiedToClipboard =" + isPswCopiedToClipboard);

                                    String msg = Utils.getPassCopiedToClipboardMsg(activity);
                                    LogUtil.d(TAG, "wifi password copied text :" + msg);
                                    CustomDialogs.dismissDefaultProgressDialog();
                                    CTWifiSetupView.getInstance().updateNoInternetAlert(false);
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                        showWifiSettings(Html.fromHtml(msg));
                                    } else {
                                        showWifiSettings(Html.fromHtml(msg, Html.FROM_HTML_MODE_COMPACT));
                                    }

                                }
                            } else if (!QRCodeUtil.getInstance().getQrCodeVO().getSsid().contains("DIRECT")) { //Not Hotspot
                                if (notSameNetworkDialog == null ||
                                        (notSameNetworkDialog != null &&
                                                !notSameNetworkDialog.isShowing())) {
                                    showWifiSettings();
                                }
                            } else {
                                LogUtil.d(TAG, "******** make connection 1");
                                makeConnection();
                            }
                        }
                    }
                } else {
                    ExitContentTransferDialog.alertToExitDialog(activity, activity.getString(R.string.ct_qr_error_title), activity.getString(R.string.invalid_device_combo_selection),activity.getString(R.string.start_over) );
                }
            }
        } else {
            qrErrorDialog = CustomDialogs.createDialog(activity,
                    activity.getString(R.string.ct_qr_error_title),
                    activity.getString(R.string.ct_qr_error_desc1),
                    false, null, false, null, null,
                    true,activity.getString(R.string.msg_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            qrErrorDialog.dismiss();
                            QRCodeUtil.getInstance().launchQRCodeActivity(activity);
                        }
                    });
        }
    }

    private void showWifiSettings() {
        notSameNetworkDialog = CustomDialogs.createDialog(activity,
                activity.getString(R.string.dialog_title),
                activity.getString(R.string.not_same_wifi_network_warning) + "\"" + QRCodeUtil.getInstance().getQrCodeVO().getSsid() + "\" "+
                        activity.getString(R.string.ortext) +" "+activity.getString(R.string.button_re_scan_text),
                true,
                null,
                true,
                activity.getString(R.string.button_wifi_settings_text),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG, "Wifi Setting clicked.");
                        notSameNetworkDialog.dismiss();
                        CTWifiSetupModel.getInstance().setShowRescanDialog(false);
                        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                        intent.putExtra("only_access_points", true);
                        intent.putExtra("extra_prefs_show_button_bar", true);
                        intent.putExtra("wifi_enable_next_on_connect", true);
                        activity.startActivityForResult(intent, 1);
                    }
                },
                true,
                activity.getString(R.string.button_re_scan_text),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG, "Re-Scan clicked...");
                        notSameNetworkDialog.dismiss();
                        CTWifiSetupModel.getInstance().setShowRescanDialog(false);
                        WifiManagerControl.disableAllWifiConnections(activity);
                        QRCodeUtil.getInstance().launchQRCodeActivity(activity);
                    }
                });
        notSameNetworkDialog.setCancelable(false);
        notSameNetworkDialog.setCanceledOnTouchOutside(false);
    }

    private void showWifiSettings(Spanned msg) {
        notSameNetworkDialog = CustomDialogs.createHtmlDialog(activity,
                activity.getString(R.string.dialog_title),
                msg,
                true,
                null,
                true,
                activity.getString(R.string.button_wifi_settings_text),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG, "Wifi Setting clicked.");
                        notSameNetworkDialog.dismiss();
                        CTWifiSetupModel.getInstance().setShowRescanDialog(false);
                        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                        intent.putExtra("only_access_points", true);
                        intent.putExtra("extra_prefs_show_button_bar", true);
                        intent.putExtra("wifi_enable_next_on_connect", true);
                        activity.startActivityForResult(intent, 1);
                    }
                },
                true,
                activity.getString(R.string.button_re_scan_text),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG, "Re-Scan clicked...");
                        notSameNetworkDialog.dismiss();
                        CTWifiSetupModel.getInstance().setShowRescanDialog(false);
                        WifiManagerControl.disableAllWifiConnections(activity);
                        QRCodeUtil.getInstance().launchQRCodeActivity(activity);
                    }
                });
        notSameNetworkDialog.setCancelable(false);
        notSameNetworkDialog.setCanceledOnTouchOutside(false);
    }

    public void tryAnotherWay() {
        CTGlobal.getInstance().setTryAnotherWay(true);
        if (HotSpotInfo.isDeviceHotspot()) {
            WifiAccessPoint.getInstance().Stop();
            LogUtil.d(TAG, "Stopped hotspot server.");
        }
        HotSpotInfo.resetHotspotInfo();

        if (!Utils.isThisServer()) {
            tryAnotherDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.try_another_way_on_cross_old_router_transfer),
                    false, null, false, null, null, true, activity.getString(R.string.msg_ok), new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            QRCodeUtil.getInstance().setReturnedFromQRActivity(false);
                            LogUtil.d(TAG, "set returned from qr act..2 :" + QRCodeUtil.getInstance().isReturnedFromQRActivity());
                            QRCodeUtil.getInstance().launchQRCodeActivity(activity);
                            tryAnotherDialog.dismiss();
                        }
                    });

        } else {

            myDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.wifi_setup_instruct), false, null,
                    false, null, null,
                    true,activity.getString(R.string.msg_ok), new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                            LogUtil.d(TAG, "wait to reset wifi connection and go to wifi setting window.");

                            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                            intent.putExtra("only_access_points", true);
                            intent.putExtra("extra_prefs_show_button_bar", true);
                            intent.putExtra("wifi_enable_next_on_connect", true);
                            activity.startActivityForResult(intent, 1);

                        }
                    });
        }

        crossWifiAccessPointReset();
        CTWifiSetupView.getInstance().setupQRRouterUI();

    }

    public void makeConnection() {
        LogUtil.d(TAG, "pair has been detected: " + CTWifiSetupModel.getInstance().isPairDetected());

        if (Utils.isThisServer() && HotSpotInfo.isDeviceHotspot()) {
            if (!CTWifiSetupModel.getInstance().isPairDetected()) {
                CustomDialogs.createDefaultProgressDialog("Waiting for the other device to connect. Please wait.", activity, false);
            }
        } else {
            if (CTGlobal.getInstance().isDoingOneToMany()) {
                if (!Utils.isReceiverDevice()) {
                    startP2PAsReceiver(activity);
                } else {
                    makeSenderConnection();
                }
            } else {
                if (Utils.isReceiverDevice()) {
                    startP2PAsReceiver(activity);
                } else {
                    makeSenderConnection();
                }
            }
            WifiManagerControl.disableOtherWifiAccesspoints(activity);
        }
    }

    private void makeSenderConnection() {
        //Check if connected to the Android Hotspot
        //If we can connect, go to the Transfer What screen
        LogUtil.d(TAG, "making sender connection.");
        if (CTGlobal.getInstance().getHotspotName().startsWith("DIRECT")
                || (QRCodeUtil.getInstance().isUsingQRCode() && QRCodeUtil.getInstance().getQrCodeVO().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION))) {
            LogUtil.d(TAG, "making sender hotspot connection.");
            Utils.CreateClientConnectionObject(VZTransferConstants.HOTSPOT_WIFI_CONNECTION, VZTransferConstants.HOTSPOT_IP, null, activity);
            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device_please_wait), activity, false);
        } else {
            LogUtil.d(TAG, "making sender router connection.");
            CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device_please_wait), activity, false);
            if (QRCodeUtil.getInstance().isUsingQRCode()) {
                CTRouterConnectionQRUtil.getInstance().connectRouter(activity, QRCodeUtil.getInstance().getQrCodeVO());
            } else {
                CTWifiSetupModel.getInstance().startP2PAsSender(activity, null, null);
            }
        }
    }

    public void setShowRescanDialog(boolean showRescanDialog) {
        this.showRescanDialog = showRescanDialog;
    }
}
