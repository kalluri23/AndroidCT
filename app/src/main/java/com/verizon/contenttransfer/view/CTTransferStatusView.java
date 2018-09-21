package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTTransferStatusListener;
import com.verizon.contenttransfer.model.CTTransferStatusModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/20/2016.
 */
public class CTTransferStatusView{

    private static final String TAG = CTTransferStatusView.class.getName();

    public CTTransferStatusView(Activity activity, boolean status, String media, boolean mediaPermission,String UImedia) {
        initView(activity, status, media, mediaPermission,UImedia);
    }

    private void initView(final Activity activity, boolean status, final String media, boolean mediaPermission,String UImedia){
        CTTransferStatusModel.getInstance().initModel(activity);
        activity.setContentView(R.layout.ct_content_transfer_status);
        boolean receiver = CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE);
        boolean showContent= CTTransferStatusModel.getInstance().showContent(media);
        LogUtil.d(TAG,"showcontent :"+ showContent);

        int startSpannableLength=activity.getResources().getString(R.string.ct_click_here_spannable).indexOf(activity.getResources().getString(R.string.here_text));
        int endSpannableLength=startSpannableLength+activity.getResources().getString(R.string.here_text).length();

        LogUtil.d(TAG,"startSpannableLength :"+startSpannableLength);
        LogUtil.d(TAG,"endSpannableLength :"+endSpannableLength);

        SpannableString ss = new SpannableString((activity.getResources().getString(R.string.ct_click_here_spannable))+UImedia);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                LogUtil.d(TAG,"media in clickable span :"+media);
                CTTransferStatusModel.getInstance().openIntent(media);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.linkColor = activity.getResources().getColor(R.color.hyper_link);
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, startSpannableLength, endSpannableLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView openContent=(TextView) activity.findViewById(R.id.ct_open_content);
        if(receiver && showContent ){
            openContent.setVisibility(View.VISIBLE);
            openContent.setText(ss);
            openContent.setMovementMethod(LinkMovementMethod.getInstance());
            openContent.setHighlightColor(Color.TRANSPARENT);

        }

        if(status){
          //  ((ImageView)activity.findViewById(R.id.ct_error_imv)).setImageResource(R.mipmap.icon_ct_black_tick_big); //brand refresh
            ((TextView)activity.findViewById(R.id.ct_error_heading)).setText(R.string.heading_cttransfer_complete);
            activity.findViewById(R.id.ct_error_desc).setVisibility(View.GONE);
        }else{
            if(receiver && !mediaPermission){
               // ((ImageView) activity.findViewById(R.id.ct_error_imv)).setImageResource(R.mipmap.icon_ct_mobile_yellow_big); //brand refresh
                ((TextView) activity.findViewById(R.id.ct_error_heading)).setText(R.string.heading_ctpartial_content_transfer_warning);
                activity.findViewById(R.id.ct_error_desc).setVisibility(View.VISIBLE);
                ((TextView) activity.findViewById(R.id.ct_error_desc)).setText(R.string.user_denied_permission_for +media);
            } else {
               // ((ImageView) activity.findViewById(R.id.ct_error_imv)).setImageResource(R.mipmap.icon_ct_mobile_yellow_big); //brand refresh
                ((TextView) activity.findViewById(R.id.ct_error_heading)).setText(R.string.heading_ctpartial_content_transfer_warning);
                activity.findViewById(R.id.ct_error_desc).setVisibility(View.VISIBLE);
            }

        }
        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_transfer_summary_detail);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        View.OnClickListener listener = new CTTransferStatusListener(activity);

        activity.findViewById(R.id.ct_got_it_btn).setOnClickListener(listener);
        activity.findViewById(R.id.search_icon).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
    }

}
