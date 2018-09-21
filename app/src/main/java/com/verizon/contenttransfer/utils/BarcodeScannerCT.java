package com.verizon.contenttransfer.utils;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.zxing.client.android.CaptureActivityCT;


/**
 * This calls out to the ZXing barcode reader and returns the result.
 */
public class BarcodeScannerCT {

    public static final int REQUEST_CODE = 0x0ba7c0de;

    public String callback;
    
    public Activity ctx = null;

    /**
     * Constructor.
     */
    public BarcodeScannerCT() {
    }


    /**
     * Starts an intent to scan and decode a barcode.
     */
    public void scan() {
        Log.d("BaseActivity", "In ScanBarCode -Scanner");

        Intent intentScan = new Intent(this.ctx, CaptureActivityCT.class);
        this.ctx.startActivity(intentScan);


/*        Intent intentScan = new Intent("CT.BARCODESCANNER.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        this.ctx.startActivity(intentScan);*/
    }


	public void setContext(Activity baseActivity) {
		ctx = baseActivity;
		// TODO Auto-generated method stub
		
	}
	
}