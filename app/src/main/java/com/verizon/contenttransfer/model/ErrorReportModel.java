package com.verizon.contenttransfer.model;

import android.app.Activity;

import com.verizon.contenttransfer.adobe.CTSiteCatConstants;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.exceptions.SiteCatLogException;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.HashMap;

/**
 * Created by kommisu on 7/13/2016.
 */
public class ErrorReportModel {

    private static final String TAG = ErrorReportModel.class.getName();

    private Activity activity;
    private CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();
    public ErrorReportModel(Activity activity) {
        this.activity = activity;

        //SITE CAT ANALYTIC
        HashMap<String, Object> stateMap = new HashMap<>();
        stateMap.put(CTSiteCatConstants.SITECAT_KEY_ERROR_MESSAGE, CTSiteCatConstants.SITECAT_VALUE_ACTION_APPCRASHED);
        try {
            iCTSiteCat.getInstance().trackStateGlobal(CTSiteCatConstants.SITECAT_VALUE_TRANSFER_CRASHED, stateMap);
        } catch (SiteCatLogException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

}
