package com.verizon.contenttransfer.model;

import android.app.Activity;

import com.verizon.contenttransfer.feedback.CTErrorReporter;

/**
 * Created by yempasu on 5/23/2017.
 */
public class CTMultiPhoneTransferModel {
    private static final String TAG = CTMultiPhoneTransferModel.class.getName();
    Activity activity;
    private static CTMultiPhoneTransferModel instance;

    public CTMultiPhoneTransferModel(Activity activity) {
        this.activity = activity;
        instance = this;
        CTErrorReporter.getInstance().Init(activity);
    }

    public static CTMultiPhoneTransferModel getInstance(){
        return instance;
    }
}
