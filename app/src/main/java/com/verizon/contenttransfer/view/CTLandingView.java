package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.TNCActivity;
import com.verizon.contenttransfer.listener.CTLandingListener;
import com.verizon.contenttransfer.model.TNCModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CTLandingView {
    private Activity activity;
    private static CTLandingView instance;
    private TextView privacyPolicy;
    private static String TAG = CTLandingView.class.getName();

    public CTLandingView(Activity activity) {
        this.activity = activity;
        instance = this;
        initView(activity);
    }
    //new test1

    public static CTLandingView getInstance(){
        return instance;
    }

    private void initView(final Activity activity) {

        activity.setContentView(R.layout.ct_landing_layout);

        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);

            ((TextView)activity.findViewById(R.id.message_below_header)).setText(activity.getString(R.string.text_ctlanding_standalone));
        }else{
            ((TextView)activity.findViewById(R.id.message_below_header)).setText(R.string.text_ctlanding);
            ((TextView)activity.findViewById(R.id.terms_tv)).setVisibility(View.INVISIBLE);
        }

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_ctlanding);
        ((TextView)activity.findViewById(R.id.ct_header_tv)).setText(R.string.ct_landing_page_header);

        View.OnClickListener landingListener = new CTLandingListener(activity);

        activity.findViewById(R.id.search_icon).setOnClickListener(landingListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(landingListener);
        activity.findViewById(R.id.ct_get_started_button_tv).setOnClickListener(landingListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(landingListener);
        activity.findViewById(R.id.aboutTV).setOnClickListener(landingListener);

        privacyPolicy = (TextView) activity.findViewById(R.id.privacyTV);

        if(Utils.isStandAloneBuild()){
            privacyPolicy.setOnClickListener(landingListener);
            privacyPolicy.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.pipe).setVisibility(View.VISIBLE);
        }

        int startSpannableLength=activity.getResources().getString(R.string.terms_message).indexOf(activity.getResources().getString(R.string.terms_conditions_str));
        int endSpannableLength=startSpannableLength+activity.getResources().getString(R.string.terms_conditions_str).length();

        LogUtil.d(TAG,"startSpannableLength :"+startSpannableLength);
        LogUtil.d(TAG,"endSpannableLength :"+endSpannableLength);

        SpannableString ss = new SpannableString(activity.getResources().getString(R.string.terms_message));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                TNCModel model = new TNCModel(activity);
                Intent tncIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), TNCActivity.class);
                tncIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(tncIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.linkColor = activity.getResources().getColor(R.color.hyper_link);
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan,startSpannableLength, endSpannableLength,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView termsText = (TextView) activity.findViewById(R.id.terms_tv);
        termsText.setText(ss);
        termsText.setMovementMethod(LinkMovementMethod.getInstance());
        termsText.setHighlightColor(Color.TRANSPARENT);
    }

}
