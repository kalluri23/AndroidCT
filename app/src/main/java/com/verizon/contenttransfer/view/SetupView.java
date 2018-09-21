package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.SetupListener;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 7/20/2016.
 */
public class SetupView {

    private Activity activity;

    public SetupView(Activity activity){
        this.activity = activity;
        initView(activity);
    }

    /*private void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_device_select_layout);

        View.OnClickListener setupListener = new SetupListener(activity);

        activity.findViewById(R.id.ct_old_phone_iv).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_old_phone_desc_rl).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_new_phone_iv).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_new_phone_desc_rl).setOnClickListener(setupListener);
        //rahiahm - don't assign the "Next" listener until old/new phone selection has been made

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_setup);

        activity.findViewById(R.id.search_icon).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(setupListener);

    }*/
    private void initView(final Activity activity) {


        activity.setContentView(R.layout.ct_device_select_layout);

        ((TextView)activity.findViewById(R.id.ct_header_tv)).setText(R.string.ct_which_phone_header);
        ((TextView)activity.findViewById(R.id.ct_option_one_tv)).setText(R.string.ct_my_old_phone);
        ((TextView)activity.findViewById(R.id.ct_option_one_desc)).setText(R.string.ct_my_old_phone_desc);
        ((TextView)activity.findViewById(R.id.ct_option_two_tv)).setText(R.string.ct_my_new_phone);
        ((TextView)activity.findViewById(R.id.ct_option_two_desc)).setText(R.string.ct_my_new_phone_desc);
        ((TextView)activity.findViewById(R.id.back_button)).setVisibility(View.GONE);


        View.OnClickListener setupListener = new SetupListener(activity);

        activity.findViewById(R.id.ct_option_one_iv).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_option_one_ll).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_option_two_iv).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_option_two_ll).setOnClickListener(setupListener);
        //rahiahm - don't assign the "Next" listener until old/new phone selection has been made

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_setup);

        activity.findViewById(R.id.search_icon).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(setupListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(setupListener);

    }
}
