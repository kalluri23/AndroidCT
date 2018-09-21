package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

/**
 * Created by rahiahm on 7/22/2016.
 */
public class SetupListener implements View.OnClickListener {

    private static String TAG = SetupListener.class.getName();
    private static final String INFO = "Content Transfer";
    private Activity activity;
    //SetupModel setupModel;
   private Dialog wifiAlertDialog;

    public SetupListener(Activity act) {
        this.activity = act;
        SetupModel.getInstance().initModel(act);

    }

    @Override
    public void onClick(final View v) {
        if (v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)) {
            ExitContentTransferDialog.showExitDialog(activity, "PhoneSelectionScreen");
        }

        if (v.getId() == R.id.ct_option_one_iv
                || v.getId() == R.id.ct_option_one_ll) {

            ImageView imageView = (ImageView) activity.findViewById(R.id.ct_option_one_iv);
            getTransferInRedColor();
            imageView.setImageResource(R.mipmap.radio_checked_new);
            CTGlobal.getInstance().setPhoneSelection(VZTransferConstants.OLD_PHONE);
            ImageView newCheck = ((ImageView) activity.findViewById(R.id.ct_option_two_iv));
            newCheck.setImageResource(R.mipmap.icon_ct_check_grey);
        }
        if (v.getId() == R.id.ct_option_two_iv
                || v.getId() == R.id.ct_option_two_ll) {
            ImageView imageView = (ImageView) activity.findViewById(R.id.ct_option_two_iv);
            getTransferInRedColor();
            imageView.setImageResource(R.mipmap.radio_checked_new);
            CTGlobal.getInstance().setPhoneSelection(VZTransferConstants.NEW_PHONE);
            ImageView oldCheck = ((ImageView) activity.findViewById(R.id.ct_option_one_iv));
            oldCheck.setImageResource(R.mipmap.icon_ct_check_grey);
        }

        if (v.getId() == R.id.ct_device_select_next_button_tv) {

            wifiAlertDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.wifi_disconnected_alert), true, null, false, null, null, true, activity.getString(R.string.i_understand), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wifiAlertDialog.dismiss();
                    if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)) {
                        LogUtil.d(TAG, "New phone selected.");
                        Utils.cleanUpReceiverMediaFilesFromVZTransfer();
                        boolean cellularDataFlag = ConnectionManager.getMobileDataState();
                        LogUtil.d(TAG, "cellularDataFlag =" + cellularDataFlag
                                + "  IsAirPlaneModeOn =" + ConnectionManager.isAirplaneModeOn(activity.getApplicationContext())
                                + "  isConnectedViaMobileData =" + ConnectionManager.isMobileDataAvailable()
                                + "  isSimSupport =" + ConnectionManager.isSimSupport(activity.getApplicationContext()));
                        logSmartNetworkSwitch();
                        if (!ConnectionManager.isAirplaneModeOn(activity.getApplicationContext())
                                && ConnectionManager.isSimSupport(activity.getApplicationContext())
                                && ConnectionManager.isMobileDataAvailable()
                                && cellularDataFlag) {
                            showDialogForLaunchDataUsageSummaryActivity();
                        } else {
                            v.setClickable(false);
                            SetupModel.getInstance().proceedToAppication(CTGlobal.getInstance().getPhoneSelection());
                        }

                    } else if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.OLD_PHONE)) {
                        LogUtil.d(TAG, "Old phone selected.");
                        boolean cellularDataFlag = ConnectionManager.getMobileDataState();
                        LogUtil.d(TAG, "cellularDataFlag =" + cellularDataFlag
                                + "  IsAirPlaneModeOn =" + ConnectionManager.isAirplaneModeOn(activity.getApplicationContext())
                                + "  isMobileDataAvailable =" + ConnectionManager.isMobileDataAvailable()
                                + "  isSimSupport =" + ConnectionManager.isSimSupport(activity.getApplicationContext()));

                        logSmartNetworkSwitch();
                        if (!ConnectionManager.isAirplaneModeOn(activity.getApplicationContext())
                                && ConnectionManager.isSimSupport(activity.getApplicationContext())
                                && ConnectionManager.isMobileDataAvailable()
                                && cellularDataFlag) {
                            showDialogForLaunchDataUsageSummaryActivity();
                        } else {
                            v.setClickable(false);
                            SetupModel.getInstance().proceedToAppication(CTGlobal.getInstance().getPhoneSelection());
                        }
                    }
                }

            });
        }
    }

    private void getTransferInRedColor() {
        (activity.findViewById(R.id.ct_device_select_next_button_tv)).setBackgroundResource(R.drawable.ct_button_solid_black_bg);
        ((TextView) activity.findViewById(R.id.ct_device_select_next_button_tv)).setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
        //rahiahm - button is only active after the UI changes to signify it's enabled
        activity.findViewById(R.id.ct_device_select_next_button_tv).setOnClickListener(this);

    }

    private void showDialogForLaunchDataUsageSummaryActivity() {
        final TextView title = new TextView(activity);
        title.setText(INFO);
        title.setPadding(20, 20, 20, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);

        SetupModel.getInstance().proceedToAppication(CTGlobal.getInstance().getPhoneSelection());
    }


    private void logSmartNetworkSwitch() {
        try {
            String autoNetworkSwitch = ConnectionManager.getExternalString(activity.getApplicationContext(), "com.android.settings",
                    "wifi_watchdog_connectivity_check", "Unknown");
            LogUtil.d(TAG, "autoNetworkSwitch =" + autoNetworkSwitch);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
