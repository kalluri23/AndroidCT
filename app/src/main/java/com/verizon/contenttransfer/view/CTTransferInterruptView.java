package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.CTTransferInterruptListener;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CTTransferInterruptView {

    private Activity activity;
    private static CTTransferInterruptView instance;
    private ImageView toolbarBack;


    public CTTransferInterruptView(Activity activity) {
        this.activity = activity;
        instance = this;
        initView(activity);

    }

    public static CTTransferInterruptView getInstance(){
        return instance;
    }

    private void initView(Activity activity) {

        activity.setContentView(R.layout.ct_transfer_interrupt_error);
        P2PFinishModel.getInstance().initModel(activity);
        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        View.OnClickListener buttonListener = new CTTransferInterruptListener(activity);
        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_error);
        activity.findViewById(R.id.search_icon).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(buttonListener);

        activity.findViewById(R.id.ct_transfer_interrupt_recap_button).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_transfer_interrupt_try_again_button).setOnClickListener(buttonListener);

    }
}
