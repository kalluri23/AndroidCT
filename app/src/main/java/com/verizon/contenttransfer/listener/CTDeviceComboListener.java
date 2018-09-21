package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.model.CTDeviceComboModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.ContentTransferAnalyticsMap;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.util.HashMap;

/**
 * Created by rahiahm on 7/20/2016.
 */
public class CTDeviceComboListener implements View.OnClickListener{
    private static CTDeviceComboListener instance;
    private Activity activity;
    private boolean isNew;

    private Dialog defaultSmsDialog;
    public static int DEFAULT_SMS_APP = 10;

    public CTDeviceComboListener() {

    }

    public static CTDeviceComboListener getInstance() {
        if (instance == null) {
            instance = new CTDeviceComboListener();
        }
        return instance;
    }
    public void init(Activity act, boolean isNew){
        this.activity = act;
        this.isNew = isNew;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "SelectPhoneCombinationScreen");
        }

        if(v.getId() == R.id.ct_option_one_iv
                || v.getId() == R.id.ct_option_one_tv){
            CTDeviceComboModel.getInstance().setIsCross(false);
            getTransferInRedColor();

            ((ImageView)activity.findViewById(R.id.ct_option_one_iv)).setImageResource(R.mipmap.radio_checked_new);
            ((ImageView)activity.findViewById(R.id.ct_option_two_iv)).setImageResource(R.mipmap.icon_ct_check_grey);
            activity.findViewById(R.id.ct_device_select_next_button_tv).setEnabled(true);
            HashMap<String, Object> eventMap = new HashMap<String, Object>();

            eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
            eventMap.put(ContentTransferAnalyticsMap.PHONE_COMBINATION, "ANDROID-ANDROID");
            Utils.logEvent(v, eventMap, "SelectPhoneCombinationScreen");

            CTDeviceComboModel.getInstance().enableMMSReceiver();
        }

        if(v.getId() == R.id.ct_option_two_iv
                || v.getId() == R.id.ct_option_two_tv){
            CTDeviceComboModel.getInstance().setIsCross(true);
            getTransferInRedColor();
            ((ImageView)activity.findViewById(R.id.ct_option_two_iv)).setImageResource(R.mipmap.radio_checked_new);
            ((ImageView)activity.findViewById(R.id.ct_option_one_iv)).setImageResource(R.mipmap.icon_ct_check_grey);
            activity.findViewById(R.id.ct_device_select_next_button_tv).setEnabled(true);
            HashMap<String, Object> eventMap = new HashMap<String, Object>();

            eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
            eventMap.put(ContentTransferAnalyticsMap.PHONE_COMBINATION, "ANDROID-IPHONE");

            Utils.logEvent(v, eventMap, "SelectPhoneCombinationScreen");

            CTDeviceComboModel.getInstance().enableMMSReceiver();
        }

        if(v.getId() == R.id.ct_settings){
            CTDeviceComboModel.getInstance().onClickSettingsIcon();
            CTDeviceComboModel.getInstance().setIsCross(false);
        }

        if(v.getId() == R.id.ct_device_select_next_button_tv){

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
               if(Utils.shouldShowSmsPrompt(activity)) {
                   defaultSmsDialog = CustomDialogs.createDialog(activity,
                           activity.getString(R.string.dialog_title), activity.getString(R.string.default_sms_setup_message), true, null, false, null, null, true, activity.getString(R.string.msg_ok), new View.OnClickListener() {

                       @Override
                       public void onClick(View v) {
                           defaultSmsDialog.dismiss();
                           MessageUtil.setDefaultSmsApp(activity, DEFAULT_SMS_APP);
                           Utils.enableDefaultSmsApp(activity);
                       }
                   });
               }else{
                   CTDeviceComboModel.getInstance().continueContentTransfer();
               }
            }else{
                CTDeviceComboModel.getInstance().continueContentTransfer();
            }
        }
    }

    private void getTransferInRedColor(){
        (activity.findViewById(R.id.ct_device_select_next_button_tv)).setBackgroundResource(R.drawable.ct_button_solid_black_bg); //changing to black with rebranding
        ((TextView)activity.findViewById(R.id.ct_device_select_next_button_tv)).setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
    }

}
