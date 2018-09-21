package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.activity.CTTransferInterruptActivity;
import com.verizon.contenttransfer.activity.P2PFinishActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/8/2016.
 */
public class CTReceiverModel {

    private Activity activity;
    private static final String TAG = CTReceiverModel.class.getName();
    private static CTReceiverModel instance;
    CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();

    public static CTReceiverModel getInstance(){
        if (instance == null) {
            instance = new CTReceiverModel();
            LogUtil.d(TAG,"New Instance created ="+instance);
        }
        return instance;
    }

    public void initModel(Activity act){
        this.activity = act;

    }
    public Context getCtxt(){
        return instance.activity.getApplicationContext();
    }

    public void cancelConnection(){

        // FOR MVM exit. We will use this message for log analytic.
        CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
        if (null != ctAnalyticUtil) {
            ctAnalyticUtil.setDataTransferStatusMsg(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER);
            ctAnalyticUtil.setVztransferCancelled(true);
        }
        //P2PServerIos.activity = null;
        LogUtil.d(TAG, "Before Closing P2PClientIos socket- top activity name =" + CTBatteryLevelReceiver.getTopActivityName());

        LogUtil.d(TAG, "Sending cancel message.");
        Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "Closing Sockets...");
                SocketUtil.disconnectAllSocket();

                Intent intent = new Intent("restore-wifi-connection");
                // You can also include some extra data.
                intent.putExtra("message", "restorewifi");
                LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
                LogUtil.d(TAG, "restore-wifi-connection - Broadcasting message sent");
            }
        }, 2000);
    }


    public void launchFinishActivity() {
        Intent mIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), P2PFinishActivity.class);
        if(null != SocketUtil.getCtAnalyticUtil(null)
            && !SocketUtil.getCtAnalyticUtil(null).getDataTransferStatusMsg().equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)){
            mIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTTransferInterruptActivity.class);
        }
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CTGlobal.getInstance().getContentTransferContext().startActivity(mIntent);
        LogUtil.d(TAG, "launched finish activity..");
    }

}
