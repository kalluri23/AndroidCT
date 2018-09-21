package com.verizon.contenttransfer.p2p.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.sender.AppSender;
import com.verizon.contenttransfer.p2p.sender.CalendarSender;
import com.verizon.contenttransfer.p2p.sender.CallLogSender;
import com.verizon.contenttransfer.p2p.sender.ContactsSender;
import com.verizon.contenttransfer.p2p.sender.DocumentSender;
import com.verizon.contenttransfer.p2p.sender.MusicSender;
import com.verizon.contenttransfer.p2p.sender.PhotoSender;
import com.verizon.contenttransfer.p2p.sender.SMSSender;
import com.verizon.contenttransfer.p2p.sender.VideoSender;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;

import java.io.File;


public class CalculationAsyncTask extends AsyncTask<Void, Void, String> {

    //
    private static final String TAG = CalculationAsyncTask.class.getName();

    public String GENERATION_STATUS = "";

    //public static Dialog p2pcalcutiondialog =null;

    Activity activity;
    Context ctxt;
    String mediaType = null;
    public CalculationAsyncTask(Context context, String mediaType) {
        this.ctxt = context;
        this.mediaType = mediaType;
    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Starting to collect data.....");

        if(mediaType.equals(VZTransferConstants.PHOTOS_STR)) {
            DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_PHOTO));
            LogUtil.d(TAG, "Generating Photo List....");
            PhotoSender.generatePhotosListFile();
        }else if(mediaType.equals(VZTransferConstants.VIDEOS_STR)) {
            DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_VIDEO));
            LogUtil.d(TAG, "Generating Video List....");
            VideoSender.generateVideoListFile();
        }else if(mediaType.equals(VZTransferConstants.CALENDAR_STR)) {
            if (VZTransferConstants.TRANSFER_CALENDAR) {
                if(CTGlobal.getInstance().isCalendarPermitted()) {
                    DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_CALENDAR));
                    LogUtil.d(TAG, "Generate Calendar List....");
                    CalendarSender.generateCalendarListFile();
                }
            }
        }
        //Collect Audio, SMS, CALL LOGS, DOCS only for Android platform
        else if(mediaType.equals(VZTransferConstants.AUDIO_STR)) {
            DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_MUSIC));
            LogUtil.d(TAG, "Generating Music List....");
            MusicSender.generateMusicsListFile();
        } else if(mediaType.equals(VZTransferConstants.CALLLOG_STR)) {
            if(CTGlobal.getInstance().isCalllogsPermitted()) {
                DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_CALLLOG));
                LogUtil.d(TAG, "Generating CallLog List....");
                CallLogSender.generateCallLogsFile();
                setMediaFileSize(VZTransferConstants.CALLLOG);
            }
        }else if(mediaType.equals(VZTransferConstants.SMS_STR)) {
            if(CTGlobal.getInstance().isSmsPermitted()) {
                DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_SMS_MMS));
                LogUtil.d(TAG, "Generating SMS List....");
                SMSSender.generateSMSMMSFileList();
                setMediaFileSize(VZTransferConstants.SMS);
            }
        }
        else if(mediaType.equals(VZTransferConstants.DOCUMENTS_STR)) {
            if (VZTransferConstants.SUPPORT_DOCS) {
                DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_DOCUMENT));
                LogUtil.d(TAG, "Generate Document List....");
                DocumentSender.generateDocumentsListFile();
            }
        }
        else if(mediaType.equals(VZTransferConstants.CONTACTS_STR)) {
            if(CTGlobal.getInstance().isContactsPermitted()) {
                DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_CONTACTS));
                LogUtil.d(TAG, "Exporting contacts list....");
                ContactsSender.exportContacts();
                setMediaFileSize(VZTransferConstants.CONTACTS);
            }
        }else if(mediaType.equals(VZTransferConstants.APPS_STR)) {
            if (VZTransferConstants.SUPPORT_APPS) {
                DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PROCESSING_APPS));
                LogUtil.d(TAG, "Generating Apps List....");
                AppSender.generateAppsListFile();
            }
        }

        DataSpeedAnalyzer.updateProgressMessage(CTGlobal.getInstance().getContentTransferContext().getString(R.string.PLEASE_WAIT));

        return null;
    }

    public static void setMediaFileSize(String media) {
        // calculate contact/ sms / calllog file size.
        if (media.equalsIgnoreCase(VZTransferConstants.SMS)) {
            File file = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.SMS_FILE);
            if (file.exists()) {
                LogUtil.d(TAG, "SMS length =" + file.length());
                CTSelectContentModel.getInstance().setTotalSMSBytes(file.length());
            }

        } else if (media.equalsIgnoreCase(VZTransferConstants.CALLLOG)) {
            File file = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CALLLOGS_FILE);
            if (file.exists()) {
                LogUtil.d(TAG, "CALLLOG length =" + file.length());
                CTSelectContentModel.getInstance().setTotalCalllogsBytes(file.length());
            }
        } else if (media.equalsIgnoreCase(VZTransferConstants.CONTACTS)) {
            File file = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CONTACTS_FILE);
            if (file.exists()) {
                LogUtil.d(TAG, "contacts length =" + file.length());
                CTSelectContentModel.getInstance().setTotalContactsBytes(file.length());
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG, "Cal async task onPostExecute mediatype :"+mediaType);

        Intent intent = new Intent("calcupdate");
        intent.putExtra("MESSAGE",VZTransferConstants.CALCULATE_DATA);
        intent.putExtra("mediatype",mediaType);
        LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);

    }

    @Override
    protected void onPreExecute() {

        GENERATION_STATUS = VZTransferConstants.IN_PROGRESS;
        LogUtil.d(TAG,"On PreExecute ... GENERATION_STATUS="+GENERATION_STATUS);
    }

}
