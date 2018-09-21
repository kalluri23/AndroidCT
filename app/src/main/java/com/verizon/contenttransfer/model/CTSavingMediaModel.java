package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ListView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTReceiverAppsListActivity;
import com.verizon.contenttransfer.adapter.SavingMediaAdapter;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.CallLogsVO;
import com.verizon.contenttransfer.p2p.model.MMSMessageVO;
import com.verizon.contenttransfer.p2p.receiver.InsertMMS;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CalendarUtil.AndroidCalendar;
import com.verizon.contenttransfer.utils.CalendarUtil.CalendarController;
import com.verizon.contenttransfer.utils.CalendarUtil.ProcessVEvent;
import com.verizon.contenttransfer.utils.ContactUtil.VCardIO;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.UtilsFromApacheLib.CTFilenameUtils;
import com.verizon.contenttransfer.view.CTSavingMediaView;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by yempasu on 8/14/2017.
 */
public class CTSavingMediaModel {
    private static Activity activity;
    private static final String TAG = CTSavingMediaModel.class.getName();
    private static CTSavingMediaModel instance;

    public String contactsState;
    public String calendarsState;
    public String calllogsState;

    public void setSmsState(String smsState) {
        this.smsState = smsState;
    }

    public String smsState;
    public String flow;
    public int contactscount;
    private Dialog savingDoneDialog;

//    private static final int callLogsId = VZTransferConstants.CALLLOG_NOTIFICATION_INDEX;
//    private static final int messageId = VZTransferConstants.SMS_NOTIFICATION_INDEX;
//    private static final int calendarId = VZTransferConstants.CALENDAR_NOTIFICATION_INDEX; //these 3 ids needed for Notification progress update

    private ArrayList<CTSavingMediaVO> ctSavingMediaVOArrayList = new ArrayList<CTSavingMediaVO>();
    private Dialog allDoneDialog;
    private Dialog appDialog;

    public void initModel(Activity activity, String contactsState, String calendarsState, String calllogsState, String smsState, int contactscount, String flow) {
        this.activity = activity;
        this.contactsState = contactsState;
        this.calendarsState = calendarsState;
        this.calllogsState = calllogsState;
        this.smsState = smsState;
        this.contactscount = contactscount;
        this.flow = flow;
    }

    public static CTSavingMediaModel getInstance() {
        if (instance == null) {
            instance = new CTSavingMediaModel();
            LogUtil.d(TAG, "New Instance created =" + instance);
        }
        return instance;
    }

    public void bindNotificationService(String smsState) {

        CTSavingMediaModel.getInstance().setSmsState(smsState);
        CTSavingMediaModel.getInstance().setCtSavingMediaVOArrayList(CTSavingMediaModel.getInstance().getSavingMediaList());
        ListView listView = (ListView) activity.findViewById(R.id.ct_saving_media_lv);
        CTSavingMediaView.getInstance().setSavingMediaAdapter(new SavingMediaAdapter(activity, R.layout.ct_receiver_saving_media_cellview, CTSavingMediaModel.getInstance().getCtSavingMediaVOArrayList()));

        //Pop the activity if default sms permission cancelled and only sms is present
        if(CTSavingMediaModel.getInstance().getCtSavingMediaVOArrayList().size()>0){
            listView.setAdapter(CTSavingMediaView.getInstance().getSavingMediaAdapter());
        }else{
            if(!flow.equals(VZTransferConstants.STANDARD_FLOW)) {
                LogUtil.d(TAG, "on relaunch cancelled default removing all preferences");
                Utils.clearAllPreferencesForSavingMedia();
                activity.finish();
            }else{
                String statusMsg = activity.getIntent().getStringExtra(VZTransferConstants.STATUS_MSG);
                P2PFinishUtil.getInstance().completeTransferFinishProcess(statusMsg);
                activity.finish();
            }
        }

        if (smsState.toLowerCase().trim().equalsIgnoreCase("true")) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                String mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(CTGlobal.getInstance().getContentTransferContext());
                LogUtil.d(TAG, "mDefaultSmsApp =" + mDefaultSmsApp);
                if (CTGlobal.getInstance().getContentTransferContext().getPackageName().equals(mDefaultSmsApp)) {
                    CTGlobal.getInstance().setProcessSMS(true);
                }
            } else {
                CTGlobal.getInstance().setProcessSMS(true);
            }
        }
        if (Utils.isReceiverDevice()) {
            VCardIO vCardIO = new VCardIO(activity, contactsState, calendarsState, smsState, calllogsState, contactscount);
            LogUtil.d(TAG, "Launching VCardIO async task....");
            if (Build.VERSION.SDK_INT >= 11) {
                LogUtil.d(TAG, "Launching single thread async task...");
                vCardIO.executeOnExecutor(Executors.newSingleThreadExecutor());
            } else {
                LogUtil.d(TAG, "Launching single thread async task...");
                vCardIO.execute();
            }
        } else {
            MessageUtil.resetDefaultSMSApp(activity.getApplicationContext());
        }

    }

    // Restore SMS Method.
    public void startProcessingSMSRestore(Context ctxt) {
        try {
            parseSMSMMSList(ctxt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restore SMS Method.
    private void parseSMSMMSList(Context ctxt) {

        File client_sms_file = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.CLIENT_SMS_LIST_FILE);
            try {
                BufferedReader smsReader = new BufferedReader(new FileReader(client_sms_file));
                if (smsReader != null) {
                    //NotificationUtil.createNotification(messageId, VZTransferConstants.SAVING_MESSAGES_NOTIFICATION);//disabling sending notification
                    ContentResolver resolver = ctxt.getContentResolver();
                    ContentValues values = null;
                    String smsFileName = null;
                    int size = 0;
                    int counter = 0;
                    while ((smsReader.readLine()) != null) {
                        size++;
                    }
                    smsReader = new BufferedReader(new FileReader(client_sms_file));
                    boolean isSmsFound = false;
                    JSONParser parser = new JSONParser();
                    long inserted_thread_id = -1;
                    InsertMMS insertMMS = new InsertMMS();
                    HashMap<String, Long> threadIdMap = new HashMap<String, Long>();
                    while ((smsFileName = smsReader.readLine()) != null) {
                        LogUtil.d(TAG, "SMS string read from file [ " + counter + "] =" + smsFileName + " length :" + smsFileName.length());
                        counter++;
                        if (smsFileName.length() > 0) {
                            try {
                                JSONObject jsonObject = (JSONObject) parser.parse(smsFileName);
                                if (null != jsonObject.get("parts")) {
                                    // MMS found.
/*                        if(!isMMSNotificationCreated) {
                            NotificationUtil.createNotification(mmsId, VZTransferConstants.SAVING_MMS_NOTIFICATION);
                            isMMSNotificationCreated = true;
                        }*/
                                    MMSMessageVO mmsmsg = new MMSMessageVO();

                                    if (null != jsonObject.get("thread_id")
                                            && null != threadIdMap.get(jsonObject.get("thread_id").toString())) {
                                        mmsmsg.setThread(threadIdMap.get(jsonObject.get("thread_id").toString()).toString());
                                    } else {
                                        mmsmsg.setThread(null);
                                    }
                                    mmsmsg.setId((null != jsonObject.get("_id") ? jsonObject.get("_id").toString() : null));

                                    //mmsmsg.setThread(current_thread_id_str);
                                    mmsmsg.setDate((null != jsonObject.get("date") ? jsonObject.get("date").toString() : null));
                                    mmsmsg.setSub((null != jsonObject.get("sub") ? jsonObject.get("sub").toString() : null));
                                    mmsmsg.setRead((null != jsonObject.get("read") ? jsonObject.get("read").toString() : null));
                                    mmsmsg.setM_type((null != jsonObject.get("m_type") ? jsonObject.get("m_type").toString() : null));
                                    mmsmsg.setM_cls((null != jsonObject.get("m_cls") ? jsonObject.get("m_cls").toString() : null));
                                    mmsmsg.setResp_st((null != jsonObject.get("resp_st") ? jsonObject.get("resp_st").toString() : null));
                                    mmsmsg.setPri((null != jsonObject.get("pri") ? jsonObject.get("pri").toString() : null));
                                    mmsmsg.setV((null != jsonObject.get("v") ? jsonObject.get("v").toString() : null));
                                    mmsmsg.setSub_cs((null != jsonObject.get("sub_cs") ? jsonObject.get("sub_cs").toString() : null));
                                    mmsmsg.setCt_t((null != jsonObject.get("ct_t") ? jsonObject.get("ct_t").toString() : null));
                                    mmsmsg.setMsg_box((null != jsonObject.get("msg_box") ? jsonObject.get("msg_box").toString() : null));
                                    mmsmsg.setAddrAryStr((null != jsonObject.get("address") ? jsonObject.get("address").toString() : null));
                                    mmsmsg.setParts((null != (JSONArray) jsonObject.get("parts") ? (JSONArray) jsonObject.get("parts") : null));

                                    LogUtil.d(TAG, "Inserting mms :" + counter);

                                    inserted_thread_id = insertMMS.insertMms(ctxt, mmsmsg);
                                    if (inserted_thread_id > -1
                                            && null != jsonObject.get("thread_id")
                                            && null == threadIdMap.get(jsonObject.get("thread_id").toString())) {
                                        threadIdMap.put(jsonObject.get("thread_id").toString(), inserted_thread_id);
                                        inserted_thread_id = -1;
                                    }
                                    LogUtil.d(TAG, "inserted SMS thread id :" + inserted_thread_id);
                                    //LogUtil.d(TAG,"previous thread id after insert :"+previous_thread_id+"  reUseThreadId ="+reUseThreadId);
                                    // updateNotificationProgressbar(VZTransferConstants.SMS_STR, messageId, VZTransferConstants.SAVING_MESSAGES_NOTIFICATION, size, counter);
                                    updateNotificationProgressbar(VZTransferConstants.SMS_STR, size, counter,activity.getString(R.string.messages_str));

                                } else {

                                    // SMS found.
                                    LogUtil.d(TAG, "*** Thread ID is ****** SMS: " + jsonObject.get("thread_id").toString());

                                    values = new ContentValues();
                                    if (jsonObject.get("address") != null) {
                                        values.put("address", jsonObject.get("address").toString());
                                    } else {
                                        LogUtil.e(TAG, "SMS address  is null..");
                                    }
                                    if (jsonObject.get("person") != null)
                                        values.put("person", jsonObject.get("person").toString());
                                    if (jsonObject.get("date") != null)
                                        values.put("date", jsonObject.get("date").toString());
                                    if (jsonObject.get("body") != null)
                                        values.put("body", Utils.getDecodedString(jsonObject.get("body").toString()));
                                    if (jsonObject.get("type") != null)
                                        values.put("type", jsonObject.get("type").toString());
                                    if (jsonObject.get("read") != null)
                                        values.put("read", jsonObject.get("read").toString());
                                    if (null != jsonObject.get("thread_id")
                                            && null != threadIdMap.get(jsonObject.get("thread_id").toString())) {
                                        values.put("thread_id", threadIdMap.get(jsonObject.get("thread_id").toString()));
                                    }

                                    isSmsFound = MessageUtil.isSMSExist(values, ctxt);
                                    LogUtil.d(TAG, "Is SMS Found : " + isSmsFound);
                                    if (!isSmsFound) {
                                        LogUtil.d(TAG, "Inserting sms :" + counter);
                                        Uri insertedSmsUri = resolver.insert(Uri.parse("content://sms/"), values);
                                        inserted_thread_id = MessageUtil.get_thread_id(insertedSmsUri, activity);
                                        if (inserted_thread_id > -1
                                                && null != jsonObject.get("thread_id")
                                                && null == threadIdMap.get(jsonObject.get("thread_id").toString())) {
                                            threadIdMap.put(jsonObject.get("thread_id").toString(), inserted_thread_id);
                                            inserted_thread_id = -1;
                                        }
                                        LogUtil.d(TAG, "inserted SMS thread id :" + inserted_thread_id);
                                        //updateNotificationProgressbar(VZTransferConstants.SMS_STR, messageId, VZTransferConstants.SAVING_MESSAGES_NOTIFICATION, size, counter);
                                    }
                                    updateNotificationProgressbar(VZTransferConstants.SMS_STR, size, counter,activity.getString(R.string.messages_str));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.d(TAG, "Exception :" + e.getMessage());
                            }
                        }
                    }
                    // NotificationUtil.complete(messageId, VZTransferConstants.SAVING_COMPLETED_MESSAGE_NOTIFICATION);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        LogUtil.e(TAG, "Saving SMS completed");
    }


    //Disabling Notifications and its progress, revert back this method when notifications needed
 /*   private static void updateNotificationProgressbar(String media, int notificationId, String msg, int size, int endIndex) {
        int perc = endIndex * 100 / size;
        if (perc % 2 == 0) {
            LogUtil.d(TAG, msg);
            NotificationUtil.updateProgress(notificationId, perc, msg + ": " + perc + "% completed");
            CTSavingMediaModel.getInstance().updateMediaSavingProgress(media, perc);
        }
    }*/

    private static void updateNotificationProgressbar(String media, int size, int endIndex,String uimedia) {
        int perc = endIndex * 100 / size;
        if (perc % 2 == 0) {
            //LogUtil.d(TAG, msg);
            // NotificationUtil.updateProgress(notificationId, perc, msg + ": " + perc + "% completed");
            CTSavingMediaModel.getInstance().updateMediaSavingProgress(media, perc, endIndex,uimedia);
        }
    }

    // Restore Calllogs Method.
    public void startProcessingCallLogRestore(Context ctxt) {

        //NotificationUtil.createNotification(callLogsId, VZTransferConstants.SAVING_CONTENT_NOTIFICATION);

        try {
            ArrayList<CallLogsVO> callLogList = parseCallLogList();
            if (callLogList.size() == 0) {
                return;
            }

            restoringCallLogs(ctxt, callLogList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restore Calllogs Method.
    private static ArrayList<CallLogsVO> parseCallLogList() {
        ArrayList<CallLogsVO> callLogList = new ArrayList<CallLogsVO>();
        JSONArray mediaList = parseCallLogFile();
        if (mediaList == null) {
            LogUtil.e(TAG, "calllog list is null.");
            return callLogList;
        }

        ListIterator<JSONObject> mediaIter = mediaList.listIterator();
        while (mediaIter.hasNext()) {
            JSONObject media = mediaIter.next();

            CallLogsVO callLogSetter = new CallLogsVO();
            if (null != media.get("id"))
                callLogSetter.setId(media.get("id").toString());
            if (null != media.get("callName"))
                callLogSetter.setName(media.get("callName").toString());
            if (null != media.get("dateString"))
                callLogSetter.setDate(media.get("dateString").toString());
            if (null != media.get("duration"))
                callLogSetter.setDuration(media.get("duration").toString());
            if (null != media.get("callType"))
                callLogSetter.setType(media.get("callType").toString());
            if (null != media.get("callNumber"))
                callLogSetter.setNumber(media.get("callNumber").toString());
            if (null != media.get("isCallNew"))
                callLogSetter.setNew(media.get("isCallNew").toString());
            if (null != media.get("cashedNumberType"))
                callLogSetter.setCachedNumberType(media.get("cashedNumberType").toString());
            if (null != media.get("cashedNumberLabel"))
                callLogSetter.setCachedNumberLabel(media.get("cashedNumberLabel").toString());
            callLogList.add(callLogSetter);
        }
        return callLogList;
    }

    // Restore Calllogs Method.
    private static void restoringCallLogs(Context ctxt, ArrayList<CallLogsVO> callLogList) {
        final int size = callLogList.size();

        LogUtil.d(TAG, "Restoring calllogs");

        int max = VZTransferConstants.MAX_BULK_INSERT_COUNT;
        if (max > size) {
            max = size;
        }
        final int x = size / max;
        final int r = (size % max); // remainder
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < x; i++) {
            endIndex = bulkCalllogsInsert(ctxt, max, startIndex, size, callLogList);
            startIndex = endIndex;
        }

        if (r > 0) {
            endIndex = bulkCalllogsInsert(ctxt, r, startIndex, size, callLogList);
        }
        //NotificationUtil.complete(callLogsId, "Saving call logs completed");
        LogUtil.d(TAG, "endIndex =" + endIndex);
    }

    // Restore Calllogs Method.
    public static JSONArray parseCallLogFile() {
        File client_calllogs_file = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.CLIENT_CALLLOG_LIST_FILE);

        JSONArray calllogListArray = new JSONArray();
        try {
            BufferedReader calllogsReader = new BufferedReader(new FileReader(client_calllogs_file));
            String calllogsFileName = null;
            while ((calllogsFileName = calllogsReader.readLine()) != null) {
                LogUtil.d(TAG, "Call log to be received : " + calllogsFileName);
            }

            JSONParser parser = new JSONParser();

            calllogListArray = (JSONArray) parser.parse(new FileReader(client_calllogs_file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calllogListArray;
    }

    // Restore Calllogs Method.
    private static int bulkCalllogsInsert(Context ctxt, int max, int startIndex, int size, ArrayList<CallLogsVO> callLogList) {
        int index = startIndex;

        try {
            ContentResolver resolver = ctxt.getContentResolver();
            ContentValues[] values = new ContentValues[max];

            for (int i = 0; i < max; ++i) {

                CallLogsVO callLogsGettersSetters = callLogList.get(index);
                values[i] = new ContentValues();
                values[i].put(CallLog.Calls._ID, Long.parseLong(callLogsGettersSetters.getId()));
                values[i].put(CallLog.Calls.CACHED_NAME, callLogsGettersSetters.getName());
                values[i].put(CallLog.Calls.NUMBER, callLogsGettersSetters.getNumber());
                values[i].put(CallLog.Calls.TYPE, callLogsGettersSetters.getType());
                values[i].put(CallLog.Calls.DATE, callLogsGettersSetters.getDate());
                values[i].put(CallLog.Calls.DURATION, callLogsGettersSetters.getDuration());
                index++;
            }
            LogUtil.d(TAG, "...Inserting bulk calllogs tot count is :" + max);
            int inserted = 0;
            try {
                inserted = resolver.bulkInsert(Uri.parse("content://call_log/calls"), values);
                LogUtil.d(TAG, "...Inserted bulk Call logs :" + inserted);
                //updateNotificationProgressbar(VZTransferConstants.CALLLOG_STR, callLogsId, "Saving call logs", size, index);
                updateNotificationProgressbar(VZTransferConstants.CALLLOG_STR, size, index,activity.getString(R.string.callLogs_str));
            } catch (Exception e) {
                LogUtil.e(TAG, "Calllogs bulk insert exception :" + e.getStackTrace());
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Calllog insert exception :" + e.getStackTrace());
        }

        return index;
    }

    public void processCalendars(Context ctx) {

        File trfrIcsFiles[] = null;

        try {

            // NotificationUtil.createNotification(calendarId, VZTransferConstants.SAVING_CALENDAR_NOTIFICATION); //disabling sending notification
            //get files
            //get only local calendars from receiving device
            //check calendar exist in receiving device
            //if cal doesnot exist
            //create a calendar...get it's id
            //else
            //insert the events into exisitng calendar

            List<String> trfrCalendarNames = new ArrayList<String>();
            Map<String, Long> rDeviceCalendarNames = new HashMap<String, Long>();
            List<String> createCalendarNames = new ArrayList<String>();
            //create calendars map for using on receiving device
            HashMap<Long, String> finalmap = new HashMap<Long, String>();

            //get files from storage
            File calendarFile = new File(VZTransferConstants.tempCalendarStoragePath);
            if (null != calendarFile && calendarFile.isDirectory()) {
                trfrIcsFiles = calendarFile.listFiles();
                if (null != trfrIcsFiles) {
                    for (int i = 0; i < trfrIcsFiles.length; i++) {
                        String fileNameWithOutExt = CTFilenameUtils.removeExtension(trfrIcsFiles[i].getName());
                        trfrCalendarNames.add(fileNameWithOutExt);
                    }
                }
            }

            //get only local calendars from receiving device
            List<AndroidCalendar> rDeviceCalendars = AndroidCalendar.loadAll(ctx.getContentResolver());
            if (null != rDeviceCalendars) {
                for (AndroidCalendar rcal : rDeviceCalendars) {
                    if ("LOCAL".equalsIgnoreCase(rcal.mAccountType)) {
                        rDeviceCalendarNames.put(rcal.mDisplayName, rcal.mId);
                    }
                }
            }

            //these calendars needs to be created on receiving device

            for (String name : trfrCalendarNames) {

                if (rDeviceCalendarNames.containsKey(name)) {
                    finalmap.put(rDeviceCalendarNames.get(name), name);
                } else {
                    createCalendarNames.add(name);
                }
            }


            //create the calendars on receiing device
            if (!createCalendarNames.isEmpty()) {
                for (String calname : createCalendarNames) {
                    finalmap = CalendarController.addCalendar(ctx, calname, 0, ctx.getContentResolver(), finalmap);
                }
            }


            if (null != calendarFile && calendarFile.isDirectory()) {
                trfrIcsFiles = calendarFile.listFiles();
                if (null != trfrIcsFiles) {
                    for (int i = 0; i < trfrIcsFiles.length; i++) {
                        String calendarFilePath = trfrIcsFiles[i].getPath();
                        LogUtil.d(TAG, "calendarFilePath =" + calendarFilePath);
                        URL url = new URL("file://" + calendarFilePath);
                        URLConnection c = url.openConnection();
                        InputStream in = c == null ? null : c.getInputStream();
                        Calendar mCalendar = in == null ? null : new CalendarBuilder().build(in); // this build method takes a lot of time to build.
                        long calId = -1;

                        for (Map.Entry<Long, String> entry : finalmap.entrySet()) {
                            long key = entry.getKey();
                            String value = entry.getValue();
                            String icsfileNameWithOutExt = CTFilenameUtils.removeExtension(trfrIcsFiles[i].getName());
                            if (value.equalsIgnoreCase(icsfileNameWithOutExt)) {
                                calId = key;
                            }
                        }
                        ProcessVEvent process = new ProcessVEvent(mCalendar, true, ctx, calId);
                        // updateNotificationProgressbar(VZTransferConstants.CALENDAR_STR, calendarId, "Saving calendar", trfrIcsFiles.length, (i + 1));
                        updateNotificationProgressbar(VZTransferConstants.CALENDAR_STR, trfrIcsFiles.length, (i + 1),activity.getString(R.string.calendars_str));
                    }
                }
            }

            //delete calendarTemp
            Utils.deleteTempCalendarFiles();
        } catch (java.net.MalformedURLException e) {
            LogUtil.d(TAG, "MalformedURLException =" + e.getMessage());
            CTGlobal.getInstance().setDbErrorMessage("processCalendars1-" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.d(TAG, "Calendar IOException =" + e.getMessage());
            CTGlobal.getInstance().setDbErrorMessage("processCalendars2-" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LogUtil.d(TAG, "Calendar Exception =" + e.getMessage());
            CTGlobal.getInstance().setDbErrorMessage("processCalendars3-" + e.getMessage());
            e.printStackTrace();
        } finally {
            //NotificationUtil.complete(calendarId, "Saving calendar completed");
        }
    }

    public ArrayList<CTSavingMediaVO> getSavingMediaList() {
        ArrayList<CTSavingMediaVO> savingList = new ArrayList<CTSavingMediaVO>();
        if (contactsState.toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isContactsPermitted()) {
            CTSavingMediaVO contacts = new CTSavingMediaVO();
            contacts.setMediaType(VZTransferConstants.CONTACTS_STR);
            contacts.setUImediaType(activity.getString(R.string.contacts_str));
            contacts.setInsideText("");
            contacts.setPercentageSaved(0);
            contacts.setProgressShowStatus(contactsState);
            savingList.add(contacts);
        }
        if (calllogsState.toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isCalllogsPermitted()) {
            CTSavingMediaVO calllogs = new CTSavingMediaVO();
            calllogs.setMediaType(VZTransferConstants.CALLLOG_STR);
            calllogs.setInsideText("");
            calllogs.setUImediaType(activity.getString(R.string.callLogs_str));
            calllogs.setPercentageSaved(0);
            calllogs.setProgressShowStatus(calllogsState);
            savingList.add(calllogs);
        }
        if (smsState.toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isSmsPermitted()) {
            CTSavingMediaVO messages = new CTSavingMediaVO();
            messages.setMediaType(VZTransferConstants.SMS_STR);
            messages.setUImediaType(activity.getString(R.string.messages_str));
            messages.setInsideText("");
            messages.setPercentageSaved(0);
            messages.setProgressShowStatus(smsState);
            savingList.add(messages);
        }
        if (calendarsState.toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isCalendarPermitted()) {
            CTSavingMediaVO calendar = new CTSavingMediaVO();
            calendar.setMediaType(VZTransferConstants.CALENDAR_STR);
            calendar.setUImediaType(activity.getString(R.string.calendars_str));
            calendar.setInsideText("");
            calendar.setPercentageSaved(0);
            calendar.setProgressShowStatus(calendarsState);
            savingList.add(calendar);
        }
        return savingList;
    }

    public ArrayList<CTSavingMediaVO> getCtSavingMediaVOArrayList() {
        return ctSavingMediaVOArrayList;
    }

    public void setCtSavingMediaVOArrayList(ArrayList<CTSavingMediaVO> ctSavingMediaVOArrayList) {
        this.ctSavingMediaVOArrayList = ctSavingMediaVOArrayList;
    }

    public void handleOnResume() {
        LogUtil.d(TAG, "handle on resume.");
        LocalBroadcastManager.getInstance(activity).registerReceiver(savingFinishedReceiver, new IntentFilter(VZTransferConstants.ALL_DONE_BROADCAST));
        LocalBroadcastManager.getInstance(activity).registerReceiver(updateSavingMediaProgress, new IntentFilter(VZTransferConstants.UPDATE_SAVING_MEDIA_PROGRESS));
        LogUtil.d(TAG, "handle on resume. flow :" + flow);
        if (null == flow || !flow.equals(VZTransferConstants.STANDARD_FLOW)) {
            LogUtil.d(TAG, "acquire device wake lock");
            SetupModel.getInstance().acquireWakeLock(activity);
        }
    }

    private BroadcastReceiver savingFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "All done broadcast received.");
          /*  if(CTSavingMediaView.getInstance().backgroundBtn != null){
                CTSavingMediaView.getInstance().backgroundBtn.setText(activity.getString(R.string.button_done_text));
            }*/
           /* allDoneDialog= CustomDialogs.createOneButtonDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.saving_media_complete_message), activity.getString(R.string.dialog_ok), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allDoneDialog.cancel();*/
            if (null != flow && flow.equals(VZTransferConstants.STANDARD_FLOW)) {
                String statusMsg = activity.getIntent().getStringExtra(VZTransferConstants.STATUS_MSG);
                P2PFinishUtil.getInstance().completeTransferFinishProcess(statusMsg);
                activity.finish();
            } else {
                SetupModel.getInstance().releaseWakelock();
                if (Utils.isDirectoryHasFileWithExtension(VZTransferConstants.tempAppsStoragePath, ".apk")) {
                    showAppsInstall();
                } else {
                    showSavingDoneDailog();
                    Utils.clearAllPreferencesForSavingMedia();
                }
            }

            // }
            //});
        }

    };

    private BroadcastReceiver updateSavingMediaProgress = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String media = intent.getStringExtra("media");
            int progress = intent.getIntExtra("progress", 0);
            int count = intent.getIntExtra("count", 0);
            LogUtil.d(TAG, "Current media :" + media + " and progress :" + progress);
            if (media == null) {
                return;
            }

            ArrayList<CTSavingMediaVO> tempArrayList = new ArrayList<CTSavingMediaVO>();
            for (CTSavingMediaVO savingMediaVO : getCtSavingMediaVOArrayList()) {
                LogUtil.d(TAG, "iterating saving media vo : media :" + savingMediaVO.getMediaType() + "  percentage :" + savingMediaVO.getPercentageSaved());
                if (savingMediaVO.getMediaType().equals(media)) {
                    savingMediaVO.setPercentageSaved(progress);
                }
                tempArrayList.add(savingMediaVO);

                if (savingMediaVO.getProgressShowStatus().equalsIgnoreCase("true")) {
                    if (savingMediaVO.getPercentageSaved() != 100) {
                        if (savingMediaVO.getMediaType().equalsIgnoreCase(VZTransferConstants.CONTACTS_STR)) {
                            ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(), savingMediaVO.getMediaType(), "true" + "#" + count);
                        } else {
                            ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(), savingMediaVO.getMediaType(), "true");
                        }
                    } else {
                        if (savingMediaVO.getMediaType().equalsIgnoreCase(VZTransferConstants.CONTACTS_STR)) {
                            ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(), savingMediaVO.getMediaType(), "false" + "#" + count);
                        } else {
                            ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(), savingMediaVO.getMediaType(), "false");
                        }
                    }
                }
            }
            //getCtSavingMediaVOArrayList().clear();
            //getCtSavingMediaVOArrayList().addAll(tempArrayList);
            setCtSavingMediaVOArrayList(tempArrayList);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "WE Are in Broadcast receiver update");
                    CTSavingMediaView.getInstance().updateSavingMediaProgress();
                }
            }, 10);
        }
    };

    public void handleOnDestroy() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(savingFinishedReceiver);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(updateSavingMediaProgress);
    }

    public void updateMediaSavingProgress(String media, int progress, int count,String uimedia) {

        if (activity != null) {
            Intent intent = new Intent(VZTransferConstants.UPDATE_SAVING_MEDIA_PROGRESS);
            intent.putExtra("media", media);
            intent.putExtra("progress", progress);
            intent.putExtra("count", count);
            intent.putExtra("uimedia", uimedia);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    private void showAppsInstall() {
        appDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.app_install_header),
                activity.getString(R.string.app_insatll_dialog_message),
                false, null,
                true, activity.getString(R.string.btnYes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appDialog.dismiss();
                        activity.finish();
                        Intent intent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTReceiverAppsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
                    }
                },
                true, activity.getString(R.string.btnNo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, false);
                        appDialog.dismiss();
                        activity.finish();
                    }
                }
        );
    }

    private void showSavingDoneDailog() {

        savingDoneDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.saving_media_done), false, null,
                false, null, null,
                true, activity.getString(R.string.msg_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        savingDoneDialog.dismiss();
                        activity.finish();
                    }
                });
    }
}
