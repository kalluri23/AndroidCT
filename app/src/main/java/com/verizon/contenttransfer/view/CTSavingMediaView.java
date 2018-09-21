package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.SavingMediaAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTSavingMediaListener;
import com.verizon.contenttransfer.model.CTSavingMediaModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by yempasu on 8/14/2017.
 */
public class CTSavingMediaView {
    private static String TAG = CTSavingMediaView.class.getName();
    private static CTSavingMediaView instance;

    public SavingMediaAdapter getSavingMediaAdapter() {
        return savingMediaAdapter;
    }

    public void setSavingMediaAdapter(SavingMediaAdapter savingMediaAdapter) {
        this.savingMediaAdapter = savingMediaAdapter;
    }

    private SavingMediaAdapter savingMediaAdapter;
    public static int DEFAULT_SMS_APP = 10;


    public TextView backgroundBtn;
    private Activity activity;
    private String contactsState;
    private String calendarsState;
    private String calllogsState;
    public String smsState;
    private String flow;
    private int contactscount;
    public static CTSavingMediaView getInstance() {
        if (instance == null) {
            instance = new CTSavingMediaView();
        }
        return instance;

    }

    public void initView(final Activity activity) {
        this.activity = activity;
        Bundle bundle = activity.getIntent().getExtras();
        this.contactsState = bundle.getString(VZTransferConstants.CONTACTS_STATE);
        this.calendarsState = bundle.getString(VZTransferConstants.CALENDAR_STATE);
        this.calllogsState = bundle.getString(VZTransferConstants.CALLLOGS_STATE);
        this.smsState = bundle.getString(VZTransferConstants.SMS_STATE);
        this.contactscount = bundle.getInt(VZTransferConstants.CONTACTS_COUNT);
        this.flow = bundle.getString(VZTransferConstants.FLOW);

        CTSavingMediaModel.getInstance().initModel(activity,contactsState,calendarsState,calllogsState,smsState,contactscount,flow);
        activity.setContentView(R.layout.ct_receiver_saving_media_layout);



/*        CTSavingMediaModel.getInstance().setCtSavingMediaVOArrayList(CTSavingMediaModel.getInstance().getSavingMediaList());
        ListView listView = (ListView) activity.findViewById(R.id.ct_saving_media_lv);
        savingMediaAdapter = new SavingMediaAdapter(activity, R.layout.ct_receiver_saving_media_cellview, CTSavingMediaModel.getInstance().getCtSavingMediaVOArrayList());
        listView.setAdapter(savingMediaAdapter);*/

        //Removing the bottom banner TextView and spannable text. Needed when users find it difficult to stand in same page for long time.
    /*    SpannableString ss = new SpannableString(activity.getResources().getString(R.string.ct_click_here_background_save));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                String statusMsg = activity.getIntent().getStringExtra("statusMsg");
                P2PFinishUtil.getInstance().completeTransferFinishProcess(statusMsg);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.linkColor = activity.getResources().getColor(R.color.hyper_link);
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView clickHere=(TextView) activity.findViewById(R.id.ct_run_in_background_done_tv);
            clickHere.setVisibility(View.VISIBLE);
            clickHere.setText(ss);
            clickHere.setMovementMethod(LinkMovementMethod.getInstance());
            clickHere.setHighlightColor(Color.TRANSPARENT);*/

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_saving_background_media);

        View.OnClickListener buttonListener = new CTSavingMediaListener(activity);

        activity.findViewById(R.id.search_icon).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(buttonListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(buttonListener);
       /* backgroundBtn = (TextView) activity.findViewById(R.id.ct_run_in_background_done_tv);
        if(backgroundBtn!= null){
            backgroundBtn.setOnClickListener(buttonListener);
        }*/

        LogUtil.d(TAG,"Flow media view :"+flow);

        if(smsState.equalsIgnoreCase("true")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                String mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(activity);
                LogUtil.d(TAG, "mDefault sms app :" + mDefaultSmsApp);
                LogUtil.d(TAG, " CTGlobal.getInstance().isSmsPermitted():" + CTGlobal.getInstance().isSmsPermitted());
                if (!activity.getPackageName().equals(mDefaultSmsApp) && CTGlobal.getInstance().isSmsPermitted()) {
                    MessageUtil.setDefaultSmsApp(activity, DEFAULT_SMS_APP);
                    Utils.enableDefaultSmsApp(activity);
                } else {
                    CTSavingMediaModel.getInstance().bindNotificationService("true");
                }
            }
        } else{
                CTSavingMediaModel.getInstance().bindNotificationService("false");
            }

    }

    public void updateSavingMediaProgress(){
        if(null != savingMediaAdapter) {
            savingMediaAdapter.notifyDataSetChanged();
        }
    }
}