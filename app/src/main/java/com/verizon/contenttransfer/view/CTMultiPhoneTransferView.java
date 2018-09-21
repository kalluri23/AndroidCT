package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTMultiPhoneTransferListener;
import com.verizon.contenttransfer.listener.SetupListener;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by yempasu on 5/23/2017.
 */
public class CTMultiPhoneTransferView {

    public CTMultiPhoneTransferView(Activity activity){
        initView(activity);
    }

   /* public void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_devicecombo_onetomany);

        ImageView multiPhoneTransferIV = (ImageView) activity.findViewById(R.id.ct_multiphone_check_iv);
        TextView multiPhoneTransferTV = (TextView)activity.findViewById(R.id.ct_multiphone_transfer_tv);

        ImageView multiPhoneTransferMixIV = (ImageView) activity.findViewById(R.id.ct_multiphone_check_mix_iv);
        TextView multiPhoneTransferMixTV = (TextView)activity.findViewById(R.id.ct_multiphone_mix_transfer_tv);

        //Disable Next button until a selection is made
        activity.findViewById(R.id.ct_multiphone_next).setEnabled(false);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_setup);
        CTMultiPhoneTransferListener.getInstance().init(activity);
        View.OnClickListener listener = CTMultiPhoneTransferListener.getInstance();

        multiPhoneTransferIV.setOnClickListener(listener);
        multiPhoneTransferTV.setOnClickListener(listener);
        multiPhoneTransferMixIV.setOnClickListener(listener);
        multiPhoneTransferMixTV.setOnClickListener(listener);
        activity.findViewById(R.id.ct_multiphone_next).setOnClickListener(listener);
        activity.findViewById(R.id.ct_multiphone_back).setOnClickListener(listener);
        activity.findViewById(R.id.search_icon).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
    }
*/
    public void initView(final Activity activity){

        activity.setContentView(R.layout.ct_devicecombo_onetomany);

        ((TextView)activity.findViewById(R.id.ct_header_tv)).setText(R.string.ct_advanced_options);

       TextView multiPhoneTransferTV=(TextView)activity.findViewById(R.id.ct_option_one_tv);
        multiPhoneTransferTV.setText(R.string.ct_multiphone_transfer);
        ((TextView)activity.findViewById(R.id.ct_option_one_desc)).setText(R.string.android_devices_only);

       TextView multiPhoneTransferMixTV=((TextView)activity.findViewById(R.id.ct_option_two_tv));
        multiPhoneTransferMixTV.setText(R.string.ct_multiphone_transfer);
        ((TextView)activity.findViewById(R.id.ct_option_two_desc)).setText(R.string.combination);


        ImageView multiPhoneTransferIV=(ImageView) activity.findViewById(R.id.ct_option_one_iv);

        ImageView multiPhoneTransferMixIV=(ImageView) activity.findViewById(R.id.ct_option_two_iv);
        //rahiahm - don't assign the "Next" listener until old/new phone selection has been made

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_setup);
        CTMultiPhoneTransferListener.getInstance().init(activity);
        View.OnClickListener listener = CTMultiPhoneTransferListener.getInstance();

        multiPhoneTransferIV.setOnClickListener(listener);
        multiPhoneTransferTV.setOnClickListener(listener);
        multiPhoneTransferMixIV.setOnClickListener(listener);
        multiPhoneTransferMixTV.setOnClickListener(listener);

        activity.findViewById(R.id.back_button).setOnClickListener(listener);
        activity.findViewById(R.id.search_icon).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);

        activity.findViewById(R.id.ct_option_two_ll).setOnClickListener(listener);
        activity.findViewById(R.id.ct_option_one_ll).setOnClickListener(listener);


    }
}
