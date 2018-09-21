/*
 *  -------------------------------------------------------------------------
 *     PROPRIETARY INFORMATION. Not for use or disclosure outside Verizon Wireless, Inc. 
 *     and its affiliates except under written agreement.
 *
 *     This is an unpublished, proprietary work of Verizon Wireless, Inc.
 *     that is protected by United States copyright laws.  Disclosure,
 *     copying, reproduction, merger, translation,modification,enhancement,
 *     or use by anyone other than authorized employees or licensees of
 *     Verizon Wireless, Inc. without the prior written consent of
 *     Verizon Wireless, Inc. is prohibited.
 *
 *     Copyright (c) 2016 Verizon Wireless, Inc.  All rights reserved.
 *  -------------------------------------------------------------------------
 *
 *
 *      Created by kommisu on 10/3/2016.
 */
package com.verizon.contenttransfer.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CTButtonTextView extends TextView {

	public static Typeface FONT_NAME;
	// Font path
	private String fontPath = "fonts/NHaasGroteskDSStd-55Rg.otf";

	public CTButtonTextView(Context context) {
		super(context);
		initMainHeaderTextView( context );
	}

	public CTButtonTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMainHeaderTextView( context );
	}

	public CTButtonTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMainHeaderTextView( context );
	}

	private void initMainHeaderTextView(Context context) {
		if(FONT_NAME == null) FONT_NAME = Typeface.createFromAsset(context.getAssets(), fontPath );
		this.setTypeface(FONT_NAME);
		this.setTypeface(Typeface.DEFAULT_BOLD);
		//this.setTextSize( getResources().getDimension( R.dimen.ct_mf_button_text_font ) );
	}
}
