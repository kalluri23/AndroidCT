package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTReceiverActivity;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.view.CTReceiverView;
import com.verizon.contenttransfer.view.CTSenderView;

public class CustomDialogs {
    public static final String TAG = CustomDialogs.class.getName();
    private static Dialog dialog;
    private static Dialog tempDialog;
    public static ProgressBar bar = null;
    public static ProgressBar br_round_bar = null;
    static TextView progressOkButton;
    public static TextView progressNegButton;
    private static int timeCounter = 0;
    private static int eta = 0;
    private static int eta_hrs = 0;
    private static int eta_min = 0;
    private static int eta_sec = 0;
    private static int etl = 0;
    private static int etl_hrs = 0;
    private static int etl_min = 0;
    private static int etl_sec = 0;
    private static long avgSpeedinMbps = 1;
    public static String totalTransfered = null;
    public static Dialog customProgressDialog = null;
    public static LinearLayout br_loader_specs_text = null;
    private static ProgressDialog pDialog;
    public static String DownloadedFilesRtnTxt="";

    public static Dialog createProgressDialog(String title, String msg, final Context context) {
        //Animation splashAnimation = AnimationUtils.loadAnimation(context, R.anim.br_rotate);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        (dialog.getWindow().getAttributes()).dimAmount = 0.0f;
        if(VZTransferConstants.USE_NEW_DIALOG) {
            dialog.setContentView(R.layout.br_loader2);
        }else{
            dialog.setContentView(R.layout.br_loader2_existing);
        }
        dialog.setCanceledOnTouchOutside(false);
        br_loader_specs_text = (LinearLayout) dialog.findViewById(R.id.br_loader_specs_text);
        //splashspinner = (ImageView) dialog.findViewById(R.id.splashspinner);
        bar = (ProgressBar) dialog.findViewById(R.id.progressBar1);
        bar.setIndeterminate(false);
        br_round_bar = (ProgressBar) dialog.findViewById(R.id.br_round_progress);
        progressOkButton = (TextView) dialog.findViewById(R.id.progressok_button);
        progressNegButton = (TextView) dialog.findViewById(R.id.progressNegBtn);
        TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTextView);

        if (title == null) {
            titleTextView.setText(context.getString(R.string.content_transfer));
        } else {
            titleTextView.setText(title);

        }
        updateProgressMessage( msg);


/*      splashspinner.startAnimation(splashAnimation);

        splashAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub

            }
        });*/

        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                try {
                    if (null != dialog && ((Dialog) dialog).isShowing()) {
                        dialog.dismiss();
                        //splashspinner.clearAnimation();
                    }
                } catch (Exception e) {
                    LogUtil.d(TAG, "Exception " + e.toString());
                }
            }
        });
        // handled dialog back button click dismiss issue
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        progressOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                ((CTReceiverActivity) context).finish();

            }
        });

        progressNegButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
                ctAnalyticUtil.setVztransferCancelled(true);
                Utils.writeToCommSocketThread(VZTransferConstants.VZTRANSFER_CANCEL, null);
                cancelConnection();
            }
        });

        return dialog;
    }


    public static void cancelConnection() {
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }

        LogUtil.d( TAG, "Broadcasting message , top activity name ="+ CTBatteryLevelReceiver.getTopActivityName());
        Intent intent = new Intent("restore-wifi-connection");
        intent.putExtra("message", "restorewifi");
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
        LogUtil.d(TAG, "Broadcasting message sent");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "Closing Sockets...");
                SocketUtil.disconnectAllSocket();
            }
        }, 100);  // commMessageAsyncTask wait 3 sec to check the msg, so made more than 3 sec here

    }


    public static Dialog showProgressBar(Context context, String title, String message,
                                         boolean showProgressCircle, boolean indeterminate, boolean cancelable,
                                         OnCancelListener cancelListener,
                                         boolean posButton, String posBtnText, OnClickListener posClick,
                                         boolean negButton, String negBtnText, OnClickListener negClick){
        try {
            if (customProgressDialog == null) {
                customProgressDialog = new Dialog(context);
                customProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //kommisu
                customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
            customProgressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            (customProgressDialog.getWindow().getAttributes()).dimAmount = 0.0f;
            customProgressDialog.setOnCancelListener(cancelListener);
            if(VZTransferConstants.USE_NEW_DIALOG) {
                customProgressDialog.setContentView(R.layout.customdialog1);
            }else {
                customProgressDialog.setContentView(R.layout.customdialog1_existing);
            }
            customProgressDialog.setCancelable(cancelable);
            customProgressDialog.setCanceledOnTouchOutside(false);

            if ( showProgressCircle ) {
                ((ProgressBar) customProgressDialog.findViewById(R.id.customDialog_round_progress)).setVisibility(View.VISIBLE);
                ((ProgressBar) customProgressDialog.findViewById(R.id.customDialog_round_progress)).setIndeterminate(indeterminate);
            } else {
                ((ProgressBar) customProgressDialog.findViewById(R.id.customDialog_round_progress)).setVisibility(View.GONE);
            }

            if (title != null) {
                ((TextView) customProgressDialog.findViewById(R.id.titletext)).setText(title);
            } else {
                ((TextView) customProgressDialog.findViewById(R.id.titletext)).setVisibility(View.GONE);
            }

            if (message != null) {
                ((TextView) customProgressDialog.findViewById(R.id.messagetext)).setText(message);
            } else {
                ((TextView) customProgressDialog.findViewById(R.id.titletext)).setVisibility(View.GONE);
            }

            if (negButton) {
                ((TextView) customProgressDialog.findViewById(R.id.negBtn)).setVisibility(View.VISIBLE);
                if (negBtnText != null) {
                    ((TextView) customProgressDialog.findViewById(R.id.negBtn)).setText(negBtnText);
                }
                if (negClick != null) {
                    ((TextView) customProgressDialog.findViewById(R.id.negBtn)).setOnClickListener(negClick);
                }else{
                    ((TextView) customProgressDialog.findViewById(R.id.negBtn)).setOnClickListener(
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    customProgressDialog.dismiss();
                                }
                            }
                    );
                }
            } else {
                ((TextView) customProgressDialog.findViewById(R.id.negBtn)).setVisibility(View.GONE);
            }

            if (posButton) {
                ((TextView) customProgressDialog.findViewById(R.id.posBtn)).setVisibility(View.VISIBLE);
                if (posBtnText != null) {
                    ((TextView) customProgressDialog.findViewById(R.id.posBtn)).setText(posBtnText);
                }
                if (posClick != null) {
                    ((TextView) customProgressDialog.findViewById(R.id.posBtn)).setOnClickListener(posClick);
                }
            }  else {
                ((TextView) customProgressDialog.findViewById(R.id.posBtn)).setVisibility(View.GONE);
            }

            customProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customProgressDialog;
    }


    public static void hideProgressBar(){
        try {
            if (customProgressDialog != null) {
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.cancel();
                    customProgressDialog.dismiss();
                }
                customProgressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Dialog MultiLineAlertDialogWithDismissBtn(String title, String msg, Context context, String btnText, final int requestCode) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        (dialog.getWindow().getAttributes()).dimAmount = 0.0f;
        if(VZTransferConstants.USE_NEW_DIALOG) {
            dialog.setContentView(R.layout.br_dialog);
        }else{
            dialog.setContentView(R.layout.br_dialog_existing);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTextView);

        if (title == null) {
            titleTextView.setText(context.getString(R.string.dialog_title));
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        }

        TextView messageTextView = (TextView) dialog
                .findViewById(R.id.messageTextView);

        messageTextView.setText(msg);


        final TextView btn1 = (TextView) dialog.findViewById(R.id.negativeButton);
        btn1.setText(btnText);

        TextView btn2 = (TextView) dialog.findViewById(R.id.positiveButton);
        btn2.setVisibility(View.GONE);
        // if button is clicked, close the custom dialog
        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        return dialog;
    }

    public static void updateMFTransferRate( long TotalSize,
                                             long dataDownloaded,
                                             long time,
                                             long startTime,
                                             boolean isDuplicateFile,
                                             String currentMediaType,
                                             String currentProgressStatus) {


        long speedinMbps = 0;
        int receivingMediaProgress = 1;
        BytesConversion conversion = Utils.bytesToMegDisplay(TotalSize);
        if(TextUtils.isEmpty(conversion.getStringValue())){
            totalTransfered = String.valueOf(Utils.bytesToMeg(TotalSize))+VZTransferConstants.MB;
        }else{
            totalTransfered = conversion.getStringValue()+VZTransferConstants.MB;
        }
        try {

            if (dataDownloaded > TotalSize) {
                dataDownloaded = TotalSize;
            }
            if(TotalSize > 0) {
                long totalDownloaded = dataDownloaded;
                if (totalDownloaded > TotalSize) {
                    totalDownloaded = TotalSize;
                }
                receivingMediaProgress = (int) ((totalDownloaded*100)/TotalSize);
            } else {
                receivingMediaProgress = 1;
            }
            long speed=0;

            if(dataDownloaded > 0 && !isDuplicateFile ) {
                try {
                    speed = ((dataDownloaded) / ((time - startTime) / (1000)) * 8);
                    avgSpeedinMbps =  (speed / 1000000);
                }catch (Exception e){
                    LogUtil.d(TAG,e.getMessage());
                }
            } else {

                speed = 1000000;
            }
            if ( speed > 0 ) {
                speedinMbps = (speed / 1000000);
                eta = (int) (((TotalSize - (dataDownloaded)) * 8) / speed);
                eta_hrs = eta / 3600;
                eta_min = (eta / 60) - (eta_hrs * 60);
                eta_sec = (eta - (eta_hrs * 3600)) - (eta_min * 60);
                if (eta_sec < 0) {
                    eta_sec = 0;
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"Exception calculating speed" + e.getMessage());
            e.printStackTrace();
        }

        BytesConversion convDownloadObj = Utils.bytesToMegDisplay(dataDownloaded);
        BytesConversion convSizeObj = Utils.bytesToMegDisplay(TotalSize);
        String downloadStr = "";
        String sizeStr = "";

        LogUtil.d(TAG,"test....1-"+convDownloadObj.getStringValue());
        if(TextUtils.isEmpty(convDownloadObj.getStringValue())){
            downloadStr = String.valueOf(convDownloadObj.getNumericValue());
        }else{
            downloadStr = convDownloadObj.getStringValue();
        }
        LogUtil.d(TAG,"test....2-"+downloadStr);
        LogUtil.d(TAG,"test....3-"+convSizeObj.getStringValue());
        if(TextUtils.isEmpty(convSizeObj.getStringValue())){
            sizeStr = String.valueOf(convSizeObj.getNumericValue());
        }else{
            sizeStr = convSizeObj.getStringValue();
        }
        LogUtil.d(TAG,"test....4-"+sizeStr);
        double totSize =  Utils.bytesToMeg(TotalSize);
        if(TotalSize > 0){
            P2PFinishUtil.getInstance().setTotalPayload(String.valueOf(totSize));
        }else {
            P2PFinishUtil.getInstance().setTotalPayload(CTGlobal.getInstance().getContentTransferContext().getString(R.string.less_than_1));
        }
        String timeLeft = eta_hrs+":"+eta_min+":"+eta_sec;
        String totalTransfered = downloadStr+" "+ CTGlobal.getInstance().getContentTransferContext().getString(R.string.of)+" "+sizeStr + VZTransferConstants.MB;
        String currentSpeed  = speedinMbps+" Mbps";
        String mediaType = currentMediaType;
        String progressStatus = currentProgressStatus;

        try {
            if (Utils.isReceiverDevice()) {
                CTReceiverView.getInstance().updateMediaProgressStatus(timeLeft,
                        totalTransfered,
                        currentSpeed,
                        mediaType,
                        progressStatus,
                        receivingMediaProgress);
            } else {
                CTSenderView.getInstance().updateMediaProgressStatus(timeLeft,
                        totalTransfered,
                        currentSpeed,
                        mediaType,
                        progressStatus,
                        receivingMediaProgress);


            }
        }catch (Exception e){
            LogUtil.d(TAG,"Exception on updating view on - "+CTGlobal.getInstance().getPhoneSelection());
        }
        if( convDownloadObj.getNumericValue()==0 ||    convSizeObj.getNumericValue()==0)
        {
            DownloadedFilesRtnTxt = CTGlobal.getInstance().getContentTransferContext().getString(R.string.less_than_1);
        }
        else {
            DownloadedFilesRtnTxt = String.valueOf(convDownloadObj.getNumericValue());
        }
    }

    public static String getSamLatestTimeElapsed(){

        if (etl_hrs==0 && etl_min==0 && etl_sec==0  )
        {
            return "0 Hr:" +"0 Min:"  + "1 Sec";
        }
        else {
            return String.valueOf(etl_hrs) + " Hr:" + String.valueOf(etl_min) + " Min:" + String.valueOf(etl_sec) + " Sec";
        }

    }
    public static String averageSpeed(){

        return String.valueOf(avgSpeedinMbps);
    }
    public static String getTimeElapsedInSeconds(){

        if (etl_hrs==0 && etl_min==0 && etl_sec==0  )
        {
            return "0";
        }
        else {
            int totSec = etl_hrs*60*60;
            totSec += etl_min*60;
            totSec += etl_sec;
            return String.valueOf(totSec);
        }
    }

    public static void updateProgressMessage(String msg){
        if (dialog != null) {
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);
            if (msg == null) {
                messageTextView.setVisibility(View.GONE);

            } else {
                if (msg.length()> 30) {
                    messageTextView.setText(msg.substring(0, 26)+"...");
                }else{
                    messageTextView.setText(msg);
                }
            }
        }
    }
    public static void updateTimeElapsed(){
        etl_sec ++;
        if (etl_sec == 60) {
            etl_sec = 0;
            etl_min++;
            if (etl_min == 60) {
                etl_min = 0;
                etl_hrs++;
            }
        }
        String elapsedTime ="Time Elapsed: "+etl_hrs+":"+etl_min+":"+etl_sec;

        if (dialog != null) {

            TextView view = (TextView) dialog.findViewById(R.id.titletxt);
            view.setText(elapsedTime);
        }
    }
    public static void updateMFTimeElapsed(){
        etl_sec ++;
        if (etl_sec == 60) {
            etl_sec = 0;
            etl_min++;
            if (etl_min == 60) {
                etl_min = 0;
                etl_hrs++;
            }
        }
        String elapsedTime = "Time Elapsed: "+etl_hrs+":"+etl_min+":"+etl_sec;

    }
    public static void updateCurrentFileName(String msg){
        LogUtil.d(TAG, "Current file name :"+msg);
        if (dialog != null) {
            TextView currentNameTextView = (TextView) dialog.findViewById(R.id.currentFileNameTextView);
            if (msg == null) {
                currentNameTextView.setVisibility(View.GONE);
            } else {
                currentNameTextView.setVisibility(View.VISIBLE);
                currentNameTextView.setText(msg);
            }
        }
    }


    public static void resetValues() {
        timeCounter = 0;
        eta = 0;
        eta_hrs = 0;
        eta_min = 0;
        eta_sec = 0;
        etl = 0;
        etl_hrs = 0;
        etl_min = 0;
        etl_sec = 0;
        avgSpeedinMbps = 1;
        totalTransfered = null;
        DownloadedFilesRtnTxt="";
    }

    public static void createInfoDialog(Context context, String message) {
        tempDialog = createDialog(context, context.getString(R.string.dialog_title),  message,false, null, false, null, null,true, context.getString(R.string.dialog_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempDialog.dismiss();
            }
        });
    }

    public static Dialog createOneButtonDialog(Context context, String title, String message, String btnText, View.OnClickListener onClickListener) {
        return createDialog(context, title,  message,false, null, false, null, null,true, btnText, onClickListener);
    }


    public static Dialog createDialog(String title, String message, String cancelButtonText) {

        Context context = CTGlobal.getInstance().getContentTransferContext();

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if(VZTransferConstants.USE_NEW_DIALOG) {
            dialog.setContentView(R.layout.br_dialog);
        }else{
            dialog.setContentView(R.layout.br_dialog_existing);
        }

        TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTextView);

        if (title == null) {
            titleTextView.setText(context.getString(R.string.dialog_title));
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        }

        TextView messageTextView = (TextView) dialog
                .findViewById(R.id.messageTextView);

        messageTextView.setText(message);

        final Button btn1 = (Button) dialog.findViewById(R.id.negativeButton);
        btn1.setText(cancelButtonText);

        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static Dialog createDialog(Context context, String title, String message, String cancelButtonText) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if(VZTransferConstants.USE_NEW_DIALOG) {
            dialog.setContentView(R.layout.br_dialog);
        }else{
            dialog.setContentView(R.layout.br_dialog_existing);
        }

        TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTextView);

        if (title == null) {
            titleTextView.setText(context.getString(R.string.dialog_title));
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        }

        TextView messageTextView = (TextView) dialog
                .findViewById(R.id.messageTextView);

        messageTextView.setText(message);

        final TextView btn1 = (TextView) dialog.findViewById(R.id.negativeButton);
        dialog.findViewById(R.id.positiveButton).setVisibility(View.GONE);
        btn1.setText(cancelButtonText);

        btn1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }



    public static Dialog createHtmlDialog(Context context, String title, Spanned message,
                                      boolean cancelable,
                                      OnCancelListener cancelListener,
                                      boolean posButton, String posBtnText, OnClickListener posClick,
                                      boolean negButton, String negBtnText, OnClickListener negClick) {
        try {
            final Dialog tempDialog = new Dialog(context);
            tempDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            tempDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            tempDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            (tempDialog.getWindow().getAttributes()).dimAmount = 0.0f;
            tempDialog.setOnCancelListener(cancelListener);
            if(VZTransferConstants.USE_NEW_DIALOG) {
                tempDialog.setContentView(R.layout.br_dialog);
            }else{
                tempDialog.setContentView(R.layout.br_dialog_existing);
            }

            tempDialog.setCancelable(cancelable);
            tempDialog.setCanceledOnTouchOutside(false);

            if (title != null) {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setText(title);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            if (message != null) {
                ((TextView) tempDialog.findViewById(R.id.messageTextView)).setText(message);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            if (negButton) {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setVisibility(View.VISIBLE);
                if (negBtnText != null) {
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setText(negBtnText);
                }
                if (negClick != null) {
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(negClick);
                }else{
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    tempDialog.dismiss();
                                }
                            }
                    );
                }
            } else {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setVisibility(View.GONE);
            }

            if (posButton) {
                ((TextView) tempDialog.findViewById(R.id.positiveButton)).setVisibility(View.VISIBLE);
                if (posBtnText != null) {
                    ((TextView) tempDialog.findViewById(R.id.positiveButton)).setText(posBtnText);
                }
                if (posClick != null) {
                    ((TextView) tempDialog.findViewById(R.id.positiveButton)).setOnClickListener(posClick);
                }
            }  else {
                ((TextView) tempDialog.findViewById(R.id.positiveButton)).setVisibility(View.GONE);
            }

            tempDialog.show();
            return tempDialog;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static Dialog createDialog(Context context, String title, String message,
                                                   boolean cancelable,
                                                   OnCancelListener cancelListener,
                                      boolean posButton, String posBtnText, OnClickListener posClick,
                                      boolean negButton, String negBtnText, OnClickListener negClick) {
        try {
            final Dialog tempDialog = new Dialog(context);
            tempDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            tempDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            tempDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            (tempDialog.getWindow().getAttributes()).dimAmount = 0.0f;
            tempDialog.setOnCancelListener(cancelListener);
            if(VZTransferConstants.USE_NEW_DIALOG) {
                tempDialog.setContentView(R.layout.br_dialog);
            }else{
                tempDialog.setContentView(R.layout.br_dialog_existing);
            }

            tempDialog.setCancelable(cancelable);
            tempDialog.setCanceledOnTouchOutside(false);

            if(title!=null) {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setText(title);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            if (message != null) {
                ((TextView) tempDialog.findViewById(R.id.messageTextView)).setText(message);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            if (negButton) {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setVisibility(View.VISIBLE);
                if (negBtnText != null) {
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setText(negBtnText);
                }
                if (negClick != null) {
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(negClick);
                }else{
                    ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    tempDialog.dismiss();
                                }
                            }
                    );
                }
            } else {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setVisibility(View.GONE);
            }

            if (posButton) {
                ((TextView) tempDialog.findViewById(R.id.positiveButton)).setVisibility(View.VISIBLE);
                if (posBtnText != null) {
                    ((TextView) tempDialog.findViewById(R.id.positiveButton)).setText(posBtnText);
                }
                if (posClick != null) {
                    ((TextView) tempDialog.findViewById(R.id.positiveButton)).setOnClickListener(posClick);
                }
            }  else {
                ((TextView) tempDialog.findViewById(R.id.positiveButton)).setVisibility(View.GONE);
            }
            
            tempDialog.show();
            return tempDialog;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void createDefaultProgressDialog(String msg, Activity activity, boolean cancellable){
        try {

            if (pDialog != null && pDialog.isShowing()) {
                LogUtil.d(TAG,"Creating default progress..");
                dismissDefaultProgressDialog();
            }
            pDialog = new ProgressDialog(activity,R.style.AppCompatAlertDialogStyle);
            pDialog.setMessage(msg);
            pDialog.setCanceledOnTouchOutside(cancellable);
            pDialog.setCancelable(false);
            pDialog.show();

        }catch (Exception e){
            LogUtil.d(TAG, e.getMessage());
        }
    }

    public static void dismissDefaultProgressDialog(){
        try {
            if (pDialog != null && pDialog.isShowing()) {
                LogUtil.d(TAG, "Dismiss default progress dialog...");
                pDialog.dismiss();
                pDialog = null;
            }
        }catch (Exception e){
            LogUtil.d(TAG, e.getMessage());
        }
    }
    public static Dialog threeButtonDialog(Context context, String title, String message, String positiveButtonText, OnClickListener positiveClickListener,
                                           String negativeButtonText, OnClickListener negativeClickListener,
                                           String neutralButtonText, OnClickListener neutralClickListener, OnCancelListener cancelListener) {


        try {
            final Dialog tempDialog = new Dialog(context);
            tempDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            tempDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            tempDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            (tempDialog.getWindow().getAttributes()).dimAmount = 0.0f;
            tempDialog.setOnCancelListener(cancelListener);
            if(VZTransferConstants.USE_NEW_DIALOG) {
                tempDialog.setContentView(R.layout.dialog_3button);
            }else{
                tempDialog.setContentView(R.layout.dialog_3button_existing);
            }
            tempDialog.setCanceledOnTouchOutside(false);
            tempDialog.setCancelable(false);

            if (title != null) {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setText(title);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            if (message != null) {
                ((TextView) tempDialog.findViewById(R.id.messageTextView)).setText(message);
            } else {
                ((TextView) tempDialog.findViewById(R.id.titleTextView)).setVisibility(View.GONE);
            }

            TextView positiveButton = ((TextView) tempDialog.findViewById(R.id.positiveButton));
            positiveButton.setVisibility(View.VISIBLE);
            if (positiveButtonText != null) {
                positiveButton.setText(positiveButtonText);
            }
            if (positiveClickListener != null) {
                positiveButton.setOnClickListener(positiveClickListener);
            }

            ((TextView) tempDialog.findViewById(R.id.negativeButton)).setVisibility(View.VISIBLE);
            if (negativeButtonText != null) {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setText(negativeButtonText);
            }
            if (negativeClickListener != null) {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(negativeClickListener);
            } else {
                ((TextView) tempDialog.findViewById(R.id.negativeButton)).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tempDialog.dismiss();
                            }
                        }
                );
            }

            ((TextView) tempDialog.findViewById(R.id.neutralButton)).setVisibility(View.VISIBLE);
            if (negativeButtonText != null) {
                ((TextView) tempDialog.findViewById(R.id.neutralButton)).setText(neutralButtonText);
            }
            if (negativeClickListener != null) {
                ((TextView) tempDialog.findViewById(R.id.neutralButton)).setOnClickListener(neutralClickListener);
            } else {
                ((TextView) tempDialog.findViewById(R.id.neutralButton)).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tempDialog.dismiss();
                            }
                        }
                );
            }

            tempDialog.show();
            return tempDialog;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Dialog createNativeDialog (Context context, String title, String message,
                                            boolean cancelable,
                                            OnCancelListener cancelListener,
                                            boolean posButton, String posBtnText, OnClickListener posClick,
                                            boolean negButton, String negBtnText, OnClickListener negClick) {
        try {

            final AlertDialog.Builder tempDialog = new AlertDialog.Builder(context);
            tempDialog.setOnCancelListener(cancelListener);

            tempDialog.setCancelable(cancelable);

            if(title!=null) {
                tempDialog.setTitle(title);
            }else{
                tempDialog.setTitle("");
            }

            if (message != null) {
                tempDialog.setMessage(message);
            } else {
                tempDialog.setMessage("");
            }

            if (negButton) {
               /* if (negBtnText != null) {
                    tempDialog.setNegativeButton(negBtnText, (DialogInterface.OnClickListener) negClick);
                }*/
                if (negClick != null) {
                    tempDialog.setNegativeButton(negBtnText, (DialogInterface.OnClickListener) negClick);
                }else{
                   tempDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                       }
                   });
                }
            }
            if (posButton) {
              /*  if (posBtnText != null) {
                    tempDialog.setPositiveButton(posBtnText, (DialogInterface.OnClickListener) posClick);
                }*/
                if (negClick != null) {
                    tempDialog.setPositiveButton(posBtnText, (DialogInterface.OnClickListener) posClick);
                }
            }
            tempDialog.show();
            return tempDialog.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
