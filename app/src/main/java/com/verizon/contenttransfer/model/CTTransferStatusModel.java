package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yempasu on 8/7/2017.
 */
public class CTTransferStatusModel {
    private static final String TAG = CTTransferStatusModel.class.getName();
    private Activity activity;
    private static CTTransferStatusModel instance;

    public static CTTransferStatusModel getInstance() {
        if (instance == null) {
            instance = new CTTransferStatusModel();
        }
        return instance;
    }

    public void initModel(Activity activity) {
        this.activity = activity;
    }

    public void openIntent(String media){
        if (media.equals(VZTransferConstants.VIDEOS_STR) || media.equals(VZTransferConstants.PHOTOS_STR) ) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "content://media/internal/images/media"));
            activity.startActivity(intent);
        }

        if(media.equals(VZTransferConstants.AUDIO_STR)){
            try {
                Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,Intent.CATEGORY_APP_MUSIC);
                activity.startActivity(intent);
            }catch (Exception e){
                LogUtil.d(TAG,"music player not found");
                CustomDialogs.createInfoDialog(activity,activity.getString(R.string.music_player_not_found));
                e.printStackTrace();
            }
        }

        if (media.equals(VZTransferConstants.CALLLOG_STR) ) {

            try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.provider.CallLog.Calls.CONTENT_URI);
            activity.startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (media.equals(VZTransferConstants.CONTACTS_STR) ) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
                activity.startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (media.equals(VZTransferConstants.CALENDAR_STR) ) {

            try {
                long startMillis=0;
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                activity.startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (media.equals(VZTransferConstants.SMS_STR)  ) {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setType("vnd.android-dir/mms-sms");
                activity.startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean showContent(String media){
        List<String> mediaList= Arrays.asList(VZTransferConstants.PHOTOS_STR, VZTransferConstants.VIDEOS_STR, VZTransferConstants.AUDIO_STR,VZTransferConstants.CALLLOG_STR,
                VZTransferConstants.CONTACTS_STR,VZTransferConstants.CALENDAR_STR,VZTransferConstants.SMS_STR);
        LogUtil.d(TAG,"mediaList :"+ mediaList);
        if(mediaList.contains(media)){
            return true;
        }
        return false;
    }
}
