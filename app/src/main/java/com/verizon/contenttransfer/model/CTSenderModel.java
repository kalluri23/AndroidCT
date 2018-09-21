package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.activity.CTTransferInterruptActivity;
import com.verizon.contenttransfer.activity.P2PFinishActivity;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.asynctask.SendMetadataAsyncTask;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTSenderView;

import java.util.concurrent.Executors;

/**
 * Created by duggipr on 9/9/2016.
 */
public class CTSenderModel {

    private Activity activity;
    private static final String TAG = CTSenderModel.class.getName();
    private static CTSenderModel instance;
    private int transferCount = 0;

    public void initModel(Activity activity) {
        this.activity = activity;
        transferCount = 0;

        SendMetadataAsyncTask sendMetadataAsyncTask = new SendMetadataAsyncTask( activity );
        LogUtil.d(TAG, "Launching send metadata async task.");
        if (Build.VERSION.SDK_INT >= 11) {
            LogUtil.d(TAG, "Starting status update task");
            //calculationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            sendMetadataAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            LogUtil.d(TAG,"less than honeycomb");
            sendMetadataAsyncTask.execute();
        }
        //CTSenderView.getInstance().startSendAnimation();
    }

    public static CTSenderModel getInstance(){
        if (instance == null) {
            instance = new CTSenderModel();
            LogUtil.d(TAG, "New Instance created =" + instance);
        }
        return instance;
    }

    public void cancelConnectionOnMVMExit(){
        // FOR MVM exit. We will use this message for log analytic.
        LogUtil.d(TAG, "Before Closing P2PClientIos socket- top activity name =" + CTBatteryLevelReceiver.getTopActivityName());

        LogUtil.d(TAG, "Sending cancel message.");
        Utils.writeToAllCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "Closing Sockets...");
                SocketUtil.disconnectAllSocket();
            }
        }, 2000);
    }

    public void handleOnPause() {

    }

    public void handleOnResume() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(transferFinishReceiver, new IntentFilter(VZTransferConstants.TRANSFER_FINISHED_MSG));
        LocalBroadcastManager.getInstance(activity).registerReceiver(updateOneToManyProgress, new IntentFilter(VZTransferConstants.UPDATE_ONE_TO_MANY_PROGRESS));
    }
    private BroadcastReceiver transferFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            transferCount++;
            LogUtil.d(TAG, "One to many transfer finished broadcast receiver. transferCount=" + transferCount);
            String hostReceived = intent.getStringExtra("host");
            String finishStatusMsg = intent.getStringExtra("finishstatusmsg");
            LogUtil.d(TAG, "transfer finished receiver - client exited before transfer start- host:" + hostReceived + " & Finish msg:" + finishStatusMsg);

            if(transferCount == SocketUtil.getConnectedClients().size()){
                Utils.cleanUpOnTransferFinish();

                Intent senderIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), P2PFinishActivity.class);
                if(!Utils.isCTTransferSuccess()) {
                    senderIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTTransferInterruptActivity.class);
                }
                senderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CTGlobal.getInstance().getContentTransferContext().startActivity(senderIntent);
            }else {
                LogUtil.d(TAG,"Waiting for more client "+transferCount+"/"+SocketUtil.getConnectedClients().size()+" received.");
            }
        }
    };
    private BroadcastReceiver updateOneToManyProgress = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //LogUtil.d(TAG, "update One to many progress :" + intent.getAction());
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CTSenderView.getInstance().updateOneToManyMediaProgressStatus();
                }
            }, 10);
        }
    };
    public void handleOnDestroy() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(transferFinishReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(updateOneToManyProgress);
    }
}
