package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.Context;

import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kommisu on 7/13/2016.
 */
public class TNCModel {

    private static final String TAG = TNCModel.class.getName();

    private Activity activity;

    private TNCModel() {
        //Not supported
    }

    public TNCModel(Activity activity) {
        this.activity = activity;
    }

    public boolean getStatus() {
        return getFromSharedPreference();
    }

    private boolean getFromSharedPreference() {
        boolean status = ContentPreference.getBooleanValue(activity.getApplicationContext(), VZTransferConstants.TNC_STATUS, VZTransferConstants.FALSE );
        return status;
    }

    private boolean getFromLocalStorage() {
        return false;
    }

    public String getEula( ) {
        String eula = "";
        try {
            eula =  readFileFromAssetsAsString(activity.getApplicationContext(), VZTransferConstants.EULA_FILE);
        } catch ( IOException ioe ) {
            LogUtil.e(TAG, "Failed to read EULA file. --" + ioe.getMessage());
            ioe.printStackTrace();
        }

        return eula;
    }

    private String readFileFromAssetsAsString( Context context, String filename ) throws IOException {

        InputStream eulaStream = context.getAssets().open( filename );
        int size = eulaStream.available();

        byte[] buffer = new byte[size];
        eulaStream.read(buffer);
        eulaStream.close();

        String eula = new String(buffer);
        LogUtil.d(TAG, "E U L A  \n" + eula );

        return eula;

    }

}
