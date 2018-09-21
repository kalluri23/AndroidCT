package com.verizon.contenttransfer.model;

import android.app.Activity;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTWifiDirectModel {

    private static final String TAG = CTWifiDirectModel.class.getName();
    private static CTWifiDirectModel instance;
    private Activity activity;


    public void initModel(Activity activity){
        this.activity = activity;

    }

    public static CTWifiDirectModel getInstance(){
        if(instance == null){
            instance = new CTWifiDirectModel();
        }
        return instance;
    }

    public void killInstance(){
        instance = null;
    }


}
