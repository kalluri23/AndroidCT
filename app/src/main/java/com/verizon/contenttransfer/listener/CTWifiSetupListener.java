package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.model.CTWifiSetupModel;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTWifiSetupListener implements View.OnClickListener{

    private final static String TAG = CTWifiSetupListener.class.getName();

    private Activity activity;
    private Dialog manualSetupDialog;

    public CTWifiSetupListener(Activity act) {
        this.activity = act;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "SelectPhoneCombinationScreen");
        }

        if(v.getId() == R.id.ct_same_wifi_network_btn_wifi_settings){
            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            intent.putExtra("only_access_points", true);
            intent.putExtra("extra_prefs_show_button_bar", true);
            intent.putExtra("wifi_enable_next_on_connect", true);
            activity.startActivityForResult(intent, 1);
        }
        if(v.getId() == R.id.ct_try_another_way_btn){
            LogUtil.d(TAG, "Try another way is clicked.");
            CTWifiSetupModel.getInstance().tryAnotherWay();
        }
        if(v.getId() == R.id.ct_one_to_many_next_btn){
            Utils.oneToManyNextButton(activity);
        }

        if(v.getId() == R.id.ct_same_wifi_network_btn_next){

            //rahiahm - Moved logic to below method 2/24
            //CTWifiSetupModel.getInstance().makeConnection();
            LogUtil.d(TAG, "pair has been detected: " + CTWifiSetupModel.getInstance().isPairDetected());

            //HashMap<String, Object> phoneSelectMap = new HashMap();

            if(!CTGlobal.getInstance().isConnectedViaWifi() && !HotSpotInfo.isDeviceHotspot())
            {
                CustomDialogs.MultiLineAlertDialogWithDismissBtn(activity.getString(R.string.content_transfer),
                        activity.getString(R.string.msg_p2p_no_wifi_connection),
                        activity,
                        activity.getString(R.string.msg_ok), -1).show();
            }
            else {
                LogUtil.d(TAG, "******** make connection 2");
                CTWifiSetupModel.getInstance().makeConnection();
            }
        }

        if(v.getId() == R.id.ct_manual_btn){
            //CTWifiSetupModel.getInstance().openNotificationTray();
            manualSetupDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.manual_setup_dialog_text), true, null,
                    true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.manualSetupExit(activity,manualSetupDialog);
                        }
                    },
                    true, activity.getString(R.string.cancel_button), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manualSetupDialog.dismiss();
                        }
                    });
        }
    }
}
