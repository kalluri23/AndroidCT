package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.TransferSummaryActivity;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CTTransferInterruptListener implements View.OnClickListener {

    private Activity activity;

    public CTTransferInterruptListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == (R.id.search_icon)
                || view.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || view.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "TransferInterrupt");
        }
        if(view.getId() == R.id.ct_transfer_interrupt_try_again_button){
            CTGlobal.getInstance().setTryAgain(true);
            P2PFinishModel.getInstance().finishEvent();
        }
        if(view.getId() == R.id.ct_transfer_interrupt_recap_button){
            //P2PFinishModel.getInstance().transferSummaryEvent();
            //Transfer summary..
            P2PFinishModel.getInstance().setTransferSummaryLaunched(true);
            //P2PFinishModel.getInstance().transferSummaryEvent();
            Intent intent = new Intent(activity.getApplicationContext(), TransferSummaryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message", VZTransferConstants.DATA_TRANSFER_INTERRUPTED);
            activity.startActivity(intent);
        }
    }
}
