package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTDeviceComboListener;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.Utils;


/**
 * Created by rahiahm on 7/20/2016.
 */
public class CTDeviceComboView {


    public CTDeviceComboView(Activity activity, boolean isNew){
        initView(activity, isNew);
    }

    public void initView(final Activity activity, boolean isNew) {

        activity.setContentView(R.layout.ct_device_combo_layout);

        ((TextView)activity.findViewById(R.id.ct_header_tv)).setText(R.string.ct_device_combo_hd);

        ImageView samePlatform = (ImageView)activity.findViewById(R.id.ct_option_one_iv);
        TextView samePlatformTV = (TextView)activity.findViewById(R.id.ct_option_one_tv);
        samePlatformTV.setText((R.string.ct_android_to_android));
        samePlatformTV.setTypeface(Typeface.DEFAULT);

        ((TextView)activity.findViewById(R.id.ct_option_one_desc)).setVisibility(View.GONE);
        ((TextView)activity.findViewById(R.id.ct_option_two_desc)).setVisibility(View.GONE);
        ((TextView)activity.findViewById(R.id.back_button)).setVisibility(View.GONE);

        ImageView crossPlatform = (ImageView)activity.findViewById(R.id.ct_option_two_iv);
        TextView crossText = (TextView)activity.findViewById(R.id.ct_option_two_tv);
        crossText.setTypeface(Typeface.DEFAULT);

        ImageView settings = (ImageView)activity.findViewById(R.id.ct_settings);

        if(isNew) {
            crossText.setText(R.string.iphone_to_android_text);
        } else{
            crossText.setText(R.string.ct_android_to_iphone);
        }

        if(CTGlobal.getInstance().isManualSetup() || !Utils.isStandAloneBuild()){
            settings.setVisibility(View.GONE);
        }else {
            settings.setVisibility(View.VISIBLE);
        }
        //Disable Next button until a selection is made
        activity.findViewById(R.id.ct_device_select_next_button_tv).setEnabled(false);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_setup);
        CTDeviceComboListener.getInstance().init(activity, isNew);
        View.OnClickListener listener = CTDeviceComboListener.getInstance();

        samePlatform.setOnClickListener(listener);
        samePlatformTV.setOnClickListener(listener);
        crossPlatform.setOnClickListener(listener);
        crossText.setOnClickListener(listener);
        settings.setOnClickListener(listener);
        activity.findViewById(R.id.ct_device_select_next_button_tv).setOnClickListener(listener);
        activity.findViewById(R.id.search_icon).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
    }
}
