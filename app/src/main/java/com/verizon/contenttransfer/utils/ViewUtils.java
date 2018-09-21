package com.verizon.contenttransfer.utils;

import android.content.Context;

import com.verizon.contenttransfer.R;

/**
 * Created by c0bissh on 9/7/2017.
 */

public class ViewUtils {
    public static boolean isTablet(Context context){
        return context.getResources().getBoolean(R.bool.isTablet);
    }
}
