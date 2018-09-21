package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.TransferSummaryActivity;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.P2PFinishUtil;

/**
 * Created by kommisu on 7/15/2016.
 */
public class P2PFinishListener implements View.OnClickListener, View.OnFocusChangeListener  {
    private static final String TAG = P2PFinishListener.class.getName();
    private Activity activity;
    private EditText surveyEmailET;
    //ICTSiteCat
    CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();
    public P2PFinishListener(Activity act) {
        this.activity = act;

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "Finish Page");
        }

        if(v.getId() == R.id.ct_done){
            P2PFinishModel.getInstance().finishEvent();
        }
        if(v.getId() == R.id.ct_recap){

            P2PFinishModel.getInstance().setTransferSummaryLaunched(true);
            Intent intent = new Intent(activity.getApplicationContext(), TransferSummaryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message", VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED);
            activity.startActivity(intent);
        }
       // if(v.getId() == R.id.ct_survey_layout){ //brand refresh
        if(v.getId() == R.id.learn_more_tv){
            P2PFinishUtil.getInstance().generateVzcloudAnalyticsFile(VZTransferConstants.VZ_CLOUD_EVENT);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.vcast.mediamanager&hl=en"));
            try{
                activity.startActivity(intent);
            }
            catch(Exception e){
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.vcast.mediamanager&hl=en"));
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        /*surveyEmailET = (EditText) activity.findViewById( R.id.surveyEmailET );

        if ( hasFocus ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                surveyEmailET.setTextColor(activity.getResources().getColor(R.color.vz_gray_color, activity.getTheme()));
            }else {
                surveyEmailET.setTextColor(activity.getResources().getColor(R.color.vz_gray_color));
            }
            surveyEmailET.setText("");

        } else {
            //do nothing
        }*/
    }
}
