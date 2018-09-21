package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.Intent;

import com.verizon.contenttransfer.activity.CTSavingMediaActivity;
import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.CleanUpSockets;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CTPermissonCheck;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;


/**
 * Created by duggipr on 9/14/2016.
 */
public class CTLandingModel {

    private Activity activity;
    private static final String TAG = CTLandingModel.class.getName();
    private static CTLandingModel instance;
    private String calendarstate = "false", smsstate = "false", calllogsstate = "false", contactsstate = "false";
    private int contactscount;

    public CTLandingModel(Activity act) {
        this.activity = act;
        instance = this;
        CTPermissonCheck.checkPermission(activity);

    }

    public static CTLandingModel getInstance() {
        return instance;
    }

    public void LandingPageOnResume() {
        resetSingletonInstanceVariables();
        if (ContentPreference.getStringValue(activity.getApplicationContext(), VZTransferConstants.GLOBAL_UUID, null) == null) {
            Utils.UniqueID();
        }

        CTBatteryLevelReceiver.isLowBattery(activity, true);
        MessageUtil.resetDefaultSMSApp(activity);
        CleanUpSockets.resetVariablesOnStartUp();
    }

    public void resetSingletonInstanceVariables() {
        // CTAnalyticUtil.getInstance().reset();
        CTGlobal.getInstance().reset();
    }

    public void savingMediaIntent(String contactsstate,String calendarstate,String calllogsstate,String smsstate,int contactscount){
        try {
            Intent intent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTSavingMediaActivity.class);
            intent.putExtra(VZTransferConstants.CONTACTS_STATE, contactsstate);
            intent.putExtra(VZTransferConstants.CALENDAR_STATE, calendarstate);
            intent.putExtra(VZTransferConstants.CALLLOGS_STATE, calllogsstate);
            intent.putExtra(VZTransferConstants.SMS_STATE, smsstate);
            intent.putExtra(VZTransferConstants.CONTACTS_COUNT, contactscount);
            intent.putExtra(VZTransferConstants.FLOW, VZTransferConstants.RELAUNCH_FLOW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String contentSavedState(String media) {
        return ContentPreference.getStringValue(CTGlobal.getInstance().getContentTransferContext(), media, null);
    }

    public boolean isSomeMediaNotSaved() {

        if (contentSavedState(VZTransferConstants.CALENDAR_STR) != null && !contentSavedState(VZTransferConstants.CALENDAR_STR).equals("")) {
            calendarstate = contentSavedState(VZTransferConstants.CALENDAR_STR);
        }

        if (contentSavedState(VZTransferConstants.CONTACTS_STR) != null && !contentSavedState(VZTransferConstants.CONTACTS_STR).equals("")) {
            String[] contacts=contentSavedState(VZTransferConstants.CONTACTS_STR).split("#");
            if(contacts.length>0){
                contactsstate = contacts[0];
            }
            if(contacts.length >1) {
                contactscount = Integer.parseInt(contacts[1]);
            }
        }
        if (contentSavedState(VZTransferConstants.SMS_STR) != null && !contentSavedState(VZTransferConstants.SMS_STR).equals("")) {
            smsstate = contentSavedState(VZTransferConstants.SMS_STR);
        }
        if (contentSavedState(VZTransferConstants.CALLLOG_STR) != null && !contentSavedState(VZTransferConstants.CALLLOG_STR).equals("")) {
            calllogsstate = contentSavedState(VZTransferConstants.CALLLOG_STR);
        }

        if (calendarstate.equalsIgnoreCase("true")
                || smsstate.equalsIgnoreCase("true")
                || calllogsstate.equalsIgnoreCase("true")
                || contactsstate.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    public void launchSavingMediaPage(){
        CTLandingModel.getInstance().savingMediaIntent(contactsstate,calendarstate,calllogsstate,smsstate,contactscount);
    }
}
