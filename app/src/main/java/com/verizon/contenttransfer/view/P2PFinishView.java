package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.P2PFinishListener;
import com.verizon.contenttransfer.model.P2PFinishModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 7/20/2016.
 */
public class P2PFinishView {
    private static P2PFinishView instance;
    private static final String TAG = P2PFinishView.class.getName();

    private Activity activity = null;
    private ImageView finishImageIV;
    private TextView finishHeading;
    private TextView finishFooter;

    public static P2PFinishView getInstance() {
        if (instance == null) {
            instance = new P2PFinishView();
        }
        return instance;
    }
    public void killInstance(){
        instance = null;
    }
    public Activity getActivity(){
        return activity;
    }
    public void updateToCongratsPage(){
       // finishImageIV = (ImageView) activity.findViewById(R.id.ct_mobile_iconIV); //brandrefresh
        finishHeading = (TextView) activity.findViewById(R.id.ct_complete_headingTV);
        finishFooter = (TextView) activity.findViewById(R.id.ct_complete_textTV);
        if(null != finishFooter) {
            //finishImageIV.setImageResource(R.mipmap.icon_ct_transfer_complete_sender); //brandrefresh
            finishHeading.setText(R.string.heading_ct_complete);
            finishFooter.setText(R.string.text_ct_complete);
        }
    }
    public void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_complete_layout);
        this.activity = activity;
        P2PFinishModel.getInstance().initModel(activity);
        View.OnClickListener p2pFinishListener = new P2PFinishListener(activity);
        TextView transferSummaryTV = (TextView) activity.findViewById(R.id.ct_recap);
        transferSummaryTV.setOnClickListener(p2pFinishListener);
        TextView finishBtn = (TextView) activity.findViewById(R.id.ct_done);
       // ImageView survey = (ImageView)activity.findViewById(R.id.ct_survey_layout); //brandrefresh
        LinearLayout survey = (LinearLayout) activity.findViewById(R.id.ct_survey_layout_new); //brandrefresh
        TextView learnmore = (TextView) activity.findViewById(R.id.learn_more_tv);
        finishBtn.setOnClickListener(p2pFinishListener);


      //  finishImageIV = (ImageView) activity.findViewById(R.id.ct_mobile_iconIV); //brandrefresh
        finishHeading = (TextView) activity.findViewById(R.id.ct_complete_headingTV);
        finishFooter = (TextView) activity.findViewById(R.id.ct_complete_textTV);

      /*  if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)
                && null!= ReceiveMetadata.mediaStateObject ) {
            if ((ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true")&& CTGlobal.getInstance().isContactsPermitted())
                    || (ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isSmsPermitted())
                    || (ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase("true")&& CTGlobal.getInstance().isCalllogsPermitted() )
                    || (ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true")&& CTGlobal.getInstance().isCalendarPermitted())){
                finishImageIV.setImageResource(R.mipmap.icon_ct_transfer_complete_receiver);
                finishHeading.setText(R.string.ct_finish_receiver_header);
                finishFooter.setText(R.string.ct_finish_receiver_footer);
            }
        }*/

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);

        }
        if(CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)){
           // activity.findViewById(R.id.ct_mobile_iconIV).setVisibility(View.GONE);//brandrefresh
            survey.setVisibility(View.VISIBLE); //brandrefresh
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer_complete);

        learnmore.setOnClickListener(p2pFinishListener); //brandrefresh
        activity.findViewById(R.id.search_icon).setOnClickListener(p2pFinishListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(p2pFinishListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(p2pFinishListener);


    }


    public void mvmbackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        TextView title = new TextView(activity);
        title.setText(activity.getString(R.string.warning_txt));
        title.setPadding(20, 20, 20, 20);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(22);
        AlertDialog alertbox = new AlertDialog.Builder(activity)
                .setCustomTitle(title)
                .setMessage(activity.getString(R.string.first_page_move_dialog))
                .setCancelable(false)
                        // left side button start
                .setNegativeButton(activity.getString(R.string.btnNo), new DialogInterface.OnClickListener() {  //sang changec Ok ->Yes    May2016

                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                        //Right Side button
                .setPositiveButton(activity.getString(R.string.btnYes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //TextView finishBtn = (TextView) activity.findViewById(R.id.finishbtn);
                        //finishBtn.performClick();
                    }

                })
                .show();
        TextView messageView = (TextView) alertbox.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }
}
