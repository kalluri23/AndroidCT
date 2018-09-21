package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTGettingReadyReceiverListener;
import com.verizon.contenttransfer.model.CTGettingReadyReceiverModel;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by c0bissh on 9/15/2016.
 */
public class CTGettingReadyReceiverView{
    public CTGettingReadyReceiverView(Activity activity){
        initView(activity);
        new CTGettingReadyReceiverModel(activity);
    }
    private void initView(Activity activity){

        activity.setContentView(R.layout.ct_select_content_layout);

        View.OnClickListener buttonListener = new CTGettingReadyReceiverListener(activity);
        
        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer);

        activity.findViewById(R.id.search_icon).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(buttonListener);
    }
}
