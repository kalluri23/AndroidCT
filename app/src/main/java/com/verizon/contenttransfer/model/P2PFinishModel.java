package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatConstants;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.CleanUpSockets;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.exceptions.SiteCatLogException;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.P2PFinishView;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.util.HashMap;

/**
 * Created by kommisu on 7/15/2016.
 */
public class P2PFinishModel {
    private static final String TAG = P2PFinishModel.class.getName();
    private static final String INFO = "Content Transfer";
    public boolean transferSummaryLaunched = false;
    private boolean isInterrupted = false;
    private Activity activity;
    private static P2PFinishModel instance;
    private Dialog defaultSmsDialog, wifiDialog, review3ButtonDialog;

    private CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();

    private String socketExceptionStatus = "";

    private String dataTransferStatusMsg = "";
    private String mIdFromA = "";
    private boolean receiver = false;


    private boolean wifiReset, reviewInitiated, dataWithoutNotification, dataWithNotification, reviewShown;
    public static P2PFinishModel getInstance() {
        if (instance == null) {
            instance = new P2PFinishModel();
        }
        return instance;
    }

    public void initModel(final Activity activity) {
        this.activity = activity;
        wifiReset=CTGlobal.getInstance().isWifiReset();
        dataWithNotification=CTGlobal.getInstance().isSavingComplete();
        CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_TRANSFER_FINISH);
        initializeVariables(activity);
        receiver = Utils.isReceiverDevice();
        if(receiver && ReceiveMetadata.mediaStateObject != null ) {
            if (!CTGlobal.getInstance().isCross() &&
                    (ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true") ||
                    ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true") ||
                    ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase("true") ||
                    ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase("true"))) {
                dataWithoutNotification = true; //Documents, photos, video, audio
            }else if(ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true") ||
                    ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true")){
                dataWithoutNotification = true;
            }
        }

        if (null != mIdFromA && mIdFromA.equals(VZTransferConstants.DATA_TRANSFER_CANCELLED)) {
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CustomDialogs.MultiLineAlertDialogWithDismissBtn(
                            INFO,
                            activity.getString(R.string.data_transfer_cancelled),
                            activity, activity.getString(R.string.msg_ok), -1).show();
                }
            }, 300);
        }

        CTGlobal.getInstance().setApplicationStatus(VZTransferConstants.CT_CONNECTION_CLOSED);

        if (receiver) {
            //sang new phone side defect handle
            samTurnOnWifi(true); // this function will handle if user turned on wifi.
        } else {
            MessageUtil.resetDefaultSMSApp(activity.getApplicationContext());
        }

        // Creating Recap media item VO
        P2PFinishUtil.getInstance().createContentRecapVO(activity);

        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter("wifireset");
        LocalBroadcastManager.getInstance(activity).registerReceiver(notificationCompleted, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(VZTransferConstants.INIT_REVIEW);
        intentFilter2.setPriority(900);
        LocalBroadcastManager.getInstance(activity).registerReceiver(notificationCompleted, intentFilter2);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(VZTransferConstants.INIT_REVIEW));
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(VZTransferConstants.CONTENT_TRANSFER_STOPPED));
    }


    private void showReviewDialog() {

        if (!isInterrupted && receiver && CTGlobal.getInstance().getAppType().equals(VZTransferConstants.STANDALONE)) {
            if (!reviewShown && ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), "show_review", true)) {
                review3ButtonDialog = CustomDialogs.threeButtonDialog(activity, activity.getString(R.string.review_dialog_title), activity.getString(R.string.review_dialog_message), activity.getString(R.string.rate_app),
                        dialogListener,activity.getString(R.string.no_thanks), dialogListener, activity.getString(R.string.remind_later), dialogListener, null);
            }
        }



        if (dataWithNotification ) {
            LogUtil.d(TAG, " inside ALL done broadcast action");
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!CTGlobal.getInstance().isCross()) {
                    CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.default_sms_setup_message_reset), true, null, false, null, null, true, activity.getString(R.string.msg_ok), null);

                }
            }
            P2PFinishView.getInstance().updateToCongratsPage();

            dataWithNotification = false;
        }else {
            if (dataWithoutNotification && shouldDefaultMessagingAppAlertShow()) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    String mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(activity);

                    if (!CTGlobal.getInstance().isCross() && activity.getPackageName().equals(mDefaultSmsApp)) {

                        defaultSmsDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.default_sms_setup_message_reset), true, null, false, null, null, true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                defaultSmsDialog.dismiss();

                            }
                        });
                    }

                }

                dataWithoutNotification = false;
            }
        }

        reviewShown = true;

    }

    private void initializeVariables(Activity activity) {
        Intent intent = activity.getIntent();
        mIdFromA = intent.getStringExtra("finishmessage");
        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
        if(ctAnalyticUtil != null) {
            dataTransferStatusMsg = SocketUtil.getCtAnalyticUtil(null).getDataTransferStatusMsg();
        }
    }


    public void onResumeEvent() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                notificationCompleted, new IntentFilter(VZTransferConstants.ALL_DONE_BROADCAST));

        //Close socket connection on finish.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SocketUtil.disconnectAllSocket();
            }
        }, 1000);

    }


    private BroadcastReceiver notificationCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "calling updateToCongratsPage.");
            if (P2PStartupActivity.contentTransferContext == null || null == P2PFinishView.getInstance().getActivity()) {
                return;
            }
            final String action = intent.getAction();
            LogUtil.d(TAG, "Finish Page done broadcast msg received - action ="+action);
            if (action.equals(VZTransferConstants.ALL_DONE_BROADCAST)) {
                dataWithNotification = true; //calenders, contacts, sms/mms, call logs
                LogUtil.d(TAG,"show review dialog....step1");
                showReviewDialog();


            } else if (action.equals(VZTransferConstants.INIT_REVIEW) ) {
                reviewInitiated = true;
                if (wifiReset || CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.PHONE_WIFI_CONNECTION)
                       || CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.HOTSPOT_WIFI_CONNECTION)) {
                    LogUtil.d(TAG, "show review dialog....step2");
                    showReviewDialog();
                }

            } else if (action.equals("wifireset")) {
                wifiReset = true;
                if (reviewInitiated) {
                    LogUtil.d(TAG, "show review dialog....step3");
                    showReviewDialog();
                }
            } else {
                LogUtil.d(TAG,"no show review dialog condition executed.");
            }

        }
    };


    View.OnClickListener dialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            if(v.getId()==R.id.positiveButton) {
                try {

                    if (Utils.isConnectedViaWifi() || ConnectionManager.isMobileDataAvailable()) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.verizon.contenttransfer")));
                    } else {
                        wifiDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.wifi_not_connected_alert), true, null, false, null, null, true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                wifiDialog.dismiss();
                            }
                        });
                    }
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/com.verizon.contenttransfer")));
                }

                ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), "show_review", false);
            }
            if(v.getId()== R.id.negativeButton) {
                if (review3ButtonDialog != null)
                    review3ButtonDialog.dismiss();
                ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), "show_review", false);

            }
            if(v.getId()== R.id.neutralButton) {
                    if (review3ButtonDialog != null)
                        review3ButtonDialog.dismiss();
            }

        }
    };

    public final boolean isEmailValid(final CharSequence emailId) {
        if (null == emailId || emailId.toString().trim().length() == 0) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches();
        }
    }

    public final boolean isPhoneValid(final CharSequence phoneno) {
        if (null == phoneno || phoneno.toString().trim().length() != 10) {
            return false;
        } else {
            return Patterns.PHONE.matcher(phoneno).matches();
        }
    }

    public void killInstance() {
        if (null != notificationCompleted) {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(notificationCompleted);
        }

        if (!transferSummaryLaunched) {
            closeApplication();
            instance = null;
            P2PFinishView.getInstance().killInstance();
        }
    }

    public void closeApplication() {
        handleFinish();
        CleanUpSockets.cleanUpAllVariables();
        CTGlobal.getInstance().setExitApp(true);
        LogUtil.d(TAG, "Finish - finish activity.");
        if(activity != null) {
            activity.finish();
        }
    }

    private void handleFinish() {
        if (HotSpotInfo.isDeviceHotspot()) {
            WifiAccessPoint.getInstance().Stop();
        }
        CustomDialogs.resetValues(); //feb2016 sang elasped0 patch
        Utils.uploadAppAnalyticsFile();
    }

    private void closeAllActivites() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void disableWifiAccessPoint() {
        LogUtil.e(TAG, "Called disable access point");

        WifiAccessPoint.getInstance().Stop();
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should be received by the CTReceiverActivity.

    private void samTurnOnWifi(boolean status) {
        LogUtil.d(TAG, "sang- wifistaus:" + SetupModel.getInstance().isDeviceWifiConnStatus());

        if (SetupModel.getInstance().isDeviceWifiConnStatus()) {

            try {
                WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(status);
                LogUtil.d(TAG, "sang- wifi ON:" + "ON");
            } catch (Exception e) {
                LogUtil.d(TAG, e.getMessage());
            }
        }
    }

    public void setUserLeaveHint() {
        HashMap<String, Object> phoneSelectMap = new HashMap();
        phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_LINKNAME, CTSiteCatConstants.SITECAT_VALUE_CLICKHOMEBUTTON);
        phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_PAGELINK, CTSiteCatConstants.SITECAT_VALUE_PHONE_FINISH
                + CTSiteCatConstants.SITECAT_VALUE_DELIMITER
                + CTSiteCatConstants.SITECAT_VALUE_CLICKHOMEBUTTON);

        try {
            if (dataTransferStatusMsg.equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
                iCTSiteCat.getInstance().trackAction(CTSiteCatConstants.SITECAT_VALUE_ACTION_CLOSE, phoneSelectMap);
            } else {
                iCTSiteCat.getInstance().trackAction(CTSiteCatConstants.SITECAT_VALUE_ACTION_TRANSFERFAILED, phoneSelectMap);
            }

        } catch (SiteCatLogException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    public void finishEvent() {
        LogUtil.d(TAG, "FInish event...");
        closeApplication();
    }

    public String getSocketExceptionStatus() {
        return socketExceptionStatus;
    }
    public void setSocketExceptionStatus(String socketExceptionStatus) {
        this.socketExceptionStatus = socketExceptionStatus;
    }
    public String getDataTransferStatusMsg() {
        return dataTransferStatusMsg;
    }

    public void setDataTransferStatusMsg(String dataTransferStatusMsg) {
        this.dataTransferStatusMsg = dataTransferStatusMsg;
    }
    public boolean isInterrupted() {
        return isInterrupted;
    }


    public void setIsInterrupted(boolean isInterrupted) {
        this.isInterrupted = isInterrupted;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setTransferSummaryLaunched(boolean transferSummaryLaunched) {
        this.transferSummaryLaunched = transferSummaryLaunched;
    }

    private boolean shouldDefaultMessagingAppAlertShow() {
        // This method decide when we need to display Default app reset dialog from both sender and receiver side.
        boolean flag = true;
        if (receiver && ReceiveMetadata.mediaStateObject != null) {
            if (!CTGlobal.getInstance().isCross()) {
                if (ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true")
                        || ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true")
                        || ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase("true")
                        || ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase("true")) {
                    flag = false;
                }
            } else {
                if (ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true")
                        || ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true")) {
                    flag = false;
                }
            }
        }
        return flag;
    }
}
