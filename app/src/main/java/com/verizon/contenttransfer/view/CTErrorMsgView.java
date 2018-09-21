package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTErrorMsgListener;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/14/2016.
 */
public class CTErrorMsgView {

    public CTErrorMsgView(Activity act){
        initView(act);
    }

    private void initView(Activity act){
        act.setContentView(R.layout.ct_error_msg_layout);

        Bundle bundle = act.getIntent().getExtras();
        String screen = bundle.getString("screen");
        String callingActivity = bundle.getString("activity");
        String errorHeading = "";
        String errorDesc = "";

        MessageUtil.resetDefaultSMSApp(act);

        View.OnClickListener listener = new CTErrorMsgListener(act, screen, callingActivity);

        if(screen.equals(VZTransferConstants.LOW_BATTERY)){
            errorHeading = act.getString(R.string.ct_error_heading);
            errorDesc = act.getString(R.string.ct_error_desc);
        }
        else if(screen.equals(VZTransferConstants.WIDI_ERROR)){
            errorHeading = act.getString(R.string.ct_error_imv);
           if(QRCodeUtil.getInstance().isUsingQRCode()){
               errorDesc = act.getString(R.string.ct_qr_error_title) + ". " + act.getString(R.string.ct_qr_error_desc1);

           }
           else{
               errorDesc = act.getString(R.string.ct_error_desc1);
           }
            QRCodeUtil.getInstance().reset();
        }
        else if(screen.equals(VZTransferConstants.INSUFFICIENT_STORAGE))
        {
            String thingsOfBytes = bundle.getString("thingsOfBytes");
            if(thingsOfBytes== null){
                thingsOfBytes = "";
            }
            String availableBytes = bundle.getString("availableBytes");
            if(availableBytes== null){
                availableBytes = "";
            }

            errorHeading = act.getString(R.string.heading_ctnot_enough_storage);
            String part1 = act.getString(R.string.text_ctnot_enough_storage_part_1);
            String part2 = act.getString(R.string.text_ctnot_enough_storage_part_2);
            String part3 = act.getString(R.string.text_ctnot_enough_storage_part_3);
            errorDesc = part1+ thingsOfBytes +part2+availableBytes+part3;
        }

        ((TextView)act.findViewById(R.id.ct_error_heading)).setText(errorHeading);
        ((TextView)act.findViewById(R.id.ct_error_desc)).setText(errorDesc);

        ((TextView)act.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_error);

        if(Utils.isStandAloneBuild()){
            act.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            act.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            act.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)act.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_error);

        act.findViewById(R.id.search_icon).setOnClickListener(listener);
        act.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        act.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
        act.findViewById(R.id.ct_got_it_btn).setOnClickListener(listener);
    }
}
