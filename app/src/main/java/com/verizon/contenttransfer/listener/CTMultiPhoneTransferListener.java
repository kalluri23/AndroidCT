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
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;


/**
 * Created by yempasu on 5/23/2017.
 */
public class CTMultiPhoneTransferListener implements View.OnClickListener {
    private static CTMultiPhoneTransferListener instance;
    private Activity activity;
    private Dialog defaultSmsDialog;
    public static int DEFAULT_SMS_APP = 10;

    public CTMultiPhoneTransferListener() {

    }

    public static CTMultiPhoneTransferListener getInstance() {
        if (instance == null) {
            instance = new CTMultiPhoneTransferListener();
        }
        return instance;
    }

    public void init(Activity act){
        this.activity = act;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "SelectPhoneCombinationScreen");
        }

        if(v.getId() == R.id.ct_option_one_iv || v.getId() == R.id.ct_option_one_ll){
            getTransferInRedColor();
            ((ImageView)activity.findViewById(R.id.ct_option_one_iv)).setImageResource(R.mipmap.radio_checked_new);
            ((ImageView)activity.findViewById(R.id.ct_option_two_iv)).setImageResource(R.mipmap.icon_ct_check_grey);
            activity.findViewById(R.id.ct_device_select_next_button_tv).setEnabled(true);
            CTGlobal.getInstance().setDoingOneToManyComb(false);
        }

        if(v.getId() == R.id.ct_option_two_iv || v.getId() == R.id.ct_option_two_ll){
            getTransferInRedColor();
            ((ImageView)activity.findViewById(R.id.ct_option_two_iv)).setImageResource(R.mipmap.radio_checked_new);
            ((ImageView)activity.findViewById(R.id.ct_option_one_iv)).setImageResource(R.mipmap.icon_ct_check_grey);
            activity.findViewById(R.id.ct_device_select_next_button_tv).setEnabled(true);
            CTGlobal.getInstance().setDoingOneToManyComb(true);
        }

        if(v.getId() == R.id.ct_device_select_next_button_tv){
            CTGlobal.getInstance().setDoingOneToMany(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                if(Utils.shouldShowSmsPrompt(activity)) {
                    defaultSmsDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title),
                            activity.getString(R.string.default_sms_setup_message), true, null, false, null, null, true, activity.getString(R.string.msg_ok), new View.OnClickListener() {

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

        if(v.getId() == R.id.back_button){
            activity.finish();
            CTGlobal.getInstance().setDoingOneToManyComb(false);
        }
    }

    private void getTransferInRedColor(){
        (activity.findViewById(R.id.ct_device_select_next_button_tv)).setBackgroundResource(R.drawable.ct_button_solid_black_bg);
        ((TextView)activity.findViewById(R.id.ct_device_select_next_button_tv)).setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
        activity.findViewById(R.id.ct_device_select_next_button_tv).setOnClickListener(this);
    }
}
