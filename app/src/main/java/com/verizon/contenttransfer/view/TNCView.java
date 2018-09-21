package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.listener.TNCListener;
import com.verizon.contenttransfer.model.TNCModel;

/**
 * Created by kommisu on 7/12/2016.
 */
public class TNCView {

    public static void initView(final Activity activity ) {

        TextView eulaTV = (TextView) activity.findViewById( R.id.eulaTV );

        eulaTV.setText( Html.fromHtml( new TNCModel( activity ).getEula() ) );

        eulaTV.setMovementMethod(LinkMovementMethod.getInstance());

        View.OnClickListener buttonListener = new TNCListener( activity );

        TextView acceptTV = (TextView) activity.findViewById( R.id.acceptTncTV);
        acceptTV.setOnClickListener( buttonListener );

        ImageView toolbarBack = (ImageView) activity.findViewById(R.id.ctlayout_toolbar_ivBack);
        toolbarBack.setVisibility(View.GONE);

    }
}
