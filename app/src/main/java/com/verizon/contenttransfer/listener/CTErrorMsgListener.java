package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.ExitHandler;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/14/2016.
 */
public class CTErrorMsgListener implements View.OnClickListener {
    private static String TAG = CTErrorMsgListener.class.getName();
    private Activity activity;
    String context;
    String callingActv;

    public CTErrorMsgListener(final Activity act, String context, String callingActv){

        this.activity = act;
        this.context = context;
        this.callingActv = callingActv;


    }

    @Override
    public void onClick(View v) {

        if (v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)) {
            ExitContentTransferDialog.showExitDialog(activity, context + "_screen");
            return;
        }

        if (v == activity.findViewById(R.id.ct_got_it_btn)) {
            if (context.equals(VZTransferConstants.LOW_BATTERY)) {

                CTBatteryLevelReceiver.isBatteryLevelAlertDialogDisplay = false;
                Intent intent = activity.getIntent();
                boolean isExitApp = intent.getBooleanExtra("isExitApp", false);
                if (isExitApp && !CTBatteryLevelReceiver.isCharging()) {
                    Utils.resetExitAppFlags(true);
                    ExitHandler.performExitTasks(activity, "low_battery_screen");
                    return;
                }

            }else if(context.equals(VZTransferConstants.WIDI_ERROR)){
                CTGlobal.getInstance().setIsWiDiFallback(true);
                CTDeviceIteratorModel.getInstance().iDontSeeItFunction(activity);

            }else if(context.equals(VZTransferConstants.INSUFFICIENT_STORAGE)){
                if(Utils.isReceiverDevice()){
                    P2PFinishModel.getInstance().disableWifiAccessPoint();
                    Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL, null);
                    CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.PLEASE_WAIT), activity, false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != activity) {
                                LogUtil.d(TAG, "Closing Sockets...");
                                SocketUtil.disconnectAllSocket();
                                CustomDialogs.dismissDefaultProgressDialog();
                                CTGlobal.getInstance().setExitApp(true);
                                activity.finish();
                            }
                        }
                    }, 2000);
                    return; // NOT required to pop current activity and sending cancel msg via comm socket.
                }
            }
        }
        activity.finish();
    }
}
