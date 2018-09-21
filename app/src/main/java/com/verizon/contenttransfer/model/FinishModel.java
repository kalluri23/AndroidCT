package com.verizon.contenttransfer.model;

import android.util.Patterns;

/**
 * Created by kommisu on 7/15/2016.
 */
public class FinishModel {

    public final static boolean isEmailValid(final CharSequence emailId ) {
        if ( null == emailId  || emailId.toString().trim().length() == 0 ) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher( emailId ).matches();
        }
    }

    public final static boolean isPhoneValid(final CharSequence phoneno ) {
        if ( null == phoneno  || phoneno.toString().trim().length() !=  10 ) {
            return false;
        } else {
            return Patterns.PHONE.matcher( phoneno ).matches();
        }
    }
}
