package com.verizon.contenttransfer.adobe;

import com.verizon.contenttransfer.exceptions.SiteCatLogException;

import java.util.Map;

/**
 * Created by c0bissh on 7/26/2016.
 */
public interface CTSiteCatInterface {
    public CTSiteCatImpl getInstance();
    public void trackState(final String state, final Map<String, Object> contextData) throws SiteCatLogException;
    public void trackAction(final String action, final Map<String, Object> contextData) throws SiteCatLogException;
    public void trackStateGlobal(final String state, final Map<String, Object> contextData) throws SiteCatLogException;
}
