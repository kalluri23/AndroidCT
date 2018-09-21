/*
 *  -------------------------------------------------------------------------
 *     PROPRIETARY INFORMATION.  Not for use or disclosure outside Verizon
 *     Wireless, Inc. and its affiliates except under written
 *     agreement.
 *
 *     This is an unpublished, proprietary work of Verizon Wireless,
 *     Inc. that is protected by United States copyright laws.  Disclosure,
 *     copying, reproduction, merger, translation,modification,enhancement,
 *     or use by anyone other than authorized employees or licensees of
 *     Verizon Wireless, Inc. without the prior written consent of
 *     Verizon Wireless, Inc. is prohibited.
 *
 *     Copyright (c) 2016 Verizon Wireless, Inc.  All rights reserved.
 *  -------------------------------------------------------------------------
 */

package com.verizon.contenttransfer.adobe;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.exceptions.SiteCatLogException;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/*
 *  Created by c0bissh on 7/26/2016.
 *
 */
public class CTSiteCatImpl implements CTSiteCatInterface {


    private static final String TAG = CTSiteCatImpl.class.getSimpleName();
    private static CTSiteCatImpl instance;
    private static Map<String, Object> globalData;

    public CTSiteCatImpl() {

    }

    public CTSiteCatImpl getInstance() {
        if (instance == null) {
            instance = new CTSiteCatImpl();
            init();
        }
        return instance;
    }

	/**
	 * Initialize global Data
     */
    private void init() {
        globalData = new HashMap<>();
        globalData.put(CTSiteCatConstants.SITECAT_GLOBAL_KEY_CATEGORY,CTSiteCatConstants.SITECAT_GLOBAL_VALUE_CT);
        globalData.put(CTSiteCatConstants.SITECAT_GLOBAL_KEY_LOB,CTSiteCatConstants.SITECAT_GLOBAL_VALUE_CONSUMER);
        globalData.put(CTSiteCatConstants.SITECAT_GLOBAL_KEY_LANGUAGE,CTSiteCatConstants.SITECAT_GLOBAL_VALUE_ENGLISH);
        globalData.put(CTSiteCatConstants.SITECAT_GLOBAL_KEY_APPVERSION, BuildConfig.VERSION_NAME);
        globalData.put(CTSiteCatConstants.SITECAT_GLOBAL_KEY_SDKVERSION, Config.getVersion());
        //rahiahm - Transaction ID not needed as Global Data
    }


    /**
     * Wrapper method used to append generic page data along with dynamic data
     *
     * @param pageName
     * @param contextData
     */
    public final void trackState( final String pageName, final Map<String, Object> contextData) throws SiteCatLogException {
        if(VZTransferConstants.SITECAT_LOGGING) {

            Map<String, Object> finalMap = new HashMap<String, Object>();

            if (null != pageName && pageName.trim().length() != 0) {
                finalMap.put(CTSiteCatConstants.SITECAT_KEY_PAGENAME, pageName);
                if (contextData != null) {
                    finalMap.putAll(contextData);
                }

                finalMap.putAll(globalData);
                LogUtil.d(TAG, "trackState Final Map :" + finalMap.toString());
                Analytics.trackState(pageName, finalMap);

            } else {
                throw new SiteCatLogException("Failed to log Site Catalyst data");
            }
        }else {
            LogUtil.d(TAG, "Site cat logging is not enabled.");
        }
    }

    public final void trackStateGlobal(final String pageName, final Map<String, Object> contextData) throws SiteCatLogException {
        if(VZTransferConstants.SITECAT_LOGGING) {
            trackState(pageName, contextData);
        }else {
            LogUtil.d(TAG, "Site cat logging is not enabled.");
        }
    }

    /**
     * Wrapper method used to track actions
     *
     * @param action
     * @param contextData
     */
    public final void trackAction(final String action, final Map<String, Object> contextData) throws SiteCatLogException {
        if(VZTransferConstants.SITECAT_LOGGING) {
            Map<String, Object> finalMap = new HashMap<String, Object>();

            if (null != action && action.trim().length() != 0) {
                finalMap.put(CTSiteCatConstants.SITECAT_KEY_LINKNAME, action);
                if (contextData != null) {
                    finalMap.putAll(contextData);
                }
		//rahiahm - Global data not needed for track action
                LogUtil.d(TAG, "trackAction Final Map :" + finalMap.toString());
                Analytics.trackAction(convert(action), finalMap);

            } else {
                throw new SiteCatLogException("Failed to log Site Catalyst data");
            }
        }else {
            LogUtil.d(TAG, "Site cat logging is not enabled.");
        }
    }

    private String convert(String action) {
        String transformedText = action != null ? action.trim().toLowerCase() : null;
        return transformedText;
    }
}
