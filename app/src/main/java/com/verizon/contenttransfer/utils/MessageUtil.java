package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.util.Base64;

import com.verizon.contenttransfer.sms.ComposeSmsActivity;
import com.verizon.contenttransfer.sms.MmsReceiver;

import java.io.ByteArrayOutputStream;

/**
 * Created by c0bissh on 12/15/2016.
 */
public class MessageUtil {
    private static String TAG = MessageUtil.class.getName();
    public static String TO_TYPE = "151"; // To get only TO address.
    public static String FROM_TYPE = "137"; //insert-address-token

    public static String INSERT_ADDRESS_TOKEN = "insert-address-token"; //insert-address-token
    public static String MESSAGE_TYPE_ALL    = "0";
    public static String MESSAGE_TYPE_INBOX  = "1"; // Telephony.Mms.MESSAGE_BOX_INBOX.  To get only FROM address.
    public static String MESSAGE_TYPE_SENT   = "2"; //To get only TO address.
    public static String MESSAGE_TYPE_DRAFT  = "3";
    public static String MESSAGE_TYPE_OUTBOX = "4"; // To get only PENDING MSG, FAILED MSG, .
    public static String MESSAGE_TYPE_FAILED = "5"; // for failed outgoing messages
    public static String MESSAGE_TYPE_QUEUED = "6"; // for messages to send later

    public static String getEncodedStringFromByteArray(byte[] rawdata ) {
        if(null == rawdata){
            return null;
        }
        System.gc();
        String encoded = Base64.encodeToString(rawdata, Base64.URL_SAFE);
        return encoded;
    }

    public static String getEncodedStringFromImage( Bitmap mmsBitmap) {
        if(null == mmsBitmap){
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mmsBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        //Since it we will be marshalling JSON use URL_SAFE
        String encoded = Base64.encodeToString(byteArrayOutputStream .toByteArray(), Base64.URL_SAFE );

        return encoded;
    }

    public static byte[] getByteArrayFromEncodedString( String encodedString ) {
        if(null == encodedString){
            return null;
        }
        byte[] decodedByteArray = Base64.decode(encodedString, Base64.URL_SAFE);
        return decodedByteArray;
    }
    public static void setDefaultSmsApp(Activity activity, int activityResultCode) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            String myPackageName = activity.getPackageName();
            //Log.d(TAG, "myPackageName :" + myPackageName);

            try {
                Intent intent1 = new Intent( Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT );
                intent1.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivityForResult(intent1, activityResultCode);
                //Log.d(TAG, "ACTION_CHANGE_DEFAULT - is started.");
            } catch (Exception e) {
                //Log.d(TAG, "ACTION_CHANGE_DEFAULT - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static void resetDefaultSMSApp(Context ctxt) {
        //Start .. Disable Default SMS after SMS restore.
        try {
            ComponentName component = new ComponentName(ctxt, MmsReceiver.class);
            ctxt.getPackageManager().setComponentEnabledSetting(component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            ComponentName component1 = new ComponentName(ctxt, ComposeSmsActivity.class);
            ctxt.getPackageManager().setComponentEnabledSetting(component1,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }catch (Exception e){
            e.printStackTrace();
        }
        //End .. Disable Default SMS after SMS restore.
    }


    public static boolean isMediaInBitmap(String mediaType){
        if(mediaType.contains( "image" )){
            return true;
        }
        return false;
    }
    public static boolean isMediaInByteArray(String mediaType){
        if(mediaType.contains( "video" )
                || mediaType.contains( "text/x-vCalendar" )
                || mediaType.contains( "audio" )
                || mediaType.contains( "text/x-vcard" )){
            return true;
        }
        return false;
    }

    public static String stripHtml(String body) {
        //return body.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("/", "&frasl;");
        return Utils.getEncodedString(body);
    }

    public static boolean isMMSExist(ContentValues values, Context context) {
        boolean isFound = false;
        String selection = "";
        String [] selectionArg = new String[values.size()];
        int index = 0;


        if (values.get("thread_id") != null) {
            selection += "thread_id = ?";
            selectionArg[index++] = values.get("thread_id").toString();
        }
        if ( values.get("date") != null) {
            selection += " and date = ?";
            selectionArg[index++] = values.get("date").toString();
        }
        if (values.get("msg_box") != null) {
            selection += " and msg_box = ?";
            selectionArg[index++] = values.get("msg_box").toString();
        }
        if (values.get("read") != null) {
            selection += " and read = ?";
            selectionArg[index++] = values.get("read").toString();
        }
        if (values.get("sub") != null) {
            selection += " and sub = ?";
            selectionArg[index++] = values.get("sub").toString();
        }
        if (values.get("sub_cs") != null) {
            selection += " and sub_cs = ?";
            selectionArg[index++] = values.get("sub_cs").toString();
        }
        if (values.get("ct_t") != null) {
            selection += " and ct_t = ?";
            selectionArg[index++] = values.get("ct_t").toString();
        }
        if (values.get("m_cls") != null) {
            selection += " and m_cls = ?";
            selectionArg[index++] = values.get("m_cls").toString();
        }


        if (values.get("m_type") != null) {
            selection += " and m_type = ?";
            selectionArg[index++] = values.get("m_type").toString();
        }
        if (values.get("v") != null) {
            selection += " and v = ?";
            selectionArg[index++] = values.get("v").toString();
        }
        if (values.get("pri") != null) {
            selection += " and pri = ?";
            selectionArg[index++] = values.get("pri").toString();
        }

        if (values.get("resp_st") != null) {
            selection += " and resp_st = ?";
            selectionArg[index++] = values.get("resp_st").toString();
        }

        if(selection.length()==0){
            selection = null;
            selectionArg = null;
        }

        Uri uri = Uri.parse("content://mms");
        //final String[] projection = {"COUNT(1)"};
        final String[] projection = {"*"};
        //final String selection = null;//Telephony.Sms.TYPE + "=" + Telephony.Sms.MESSAGE_TYPE_SENT;
        LogUtil.d(TAG,"selection str:"+selection);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    projection,
                    selection,
                    selectionArg,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                LogUtil.d(TAG,"Duplicate message check - mms found.");
                isFound = true;
            }else {
                LogUtil.d(TAG,"Duplicate message check - mms not found.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isFound;
    }

    public static boolean isSMSExist(ContentValues values, Context context) {
        boolean isFound = false;
        String selection = "";
        String [] selectionArg = new String[values.size()];
        int index = 0;

        if (values.get("address") != null) {
            selection += "address = ?";
            selectionArg[index++] = values.get("address").toString();
        }
        if ( values.get("person") != null) {
            selection += " and person = ?";
            selectionArg[index++] = values.get("person").toString();
        }
        if (values.get("date") != null) {
            selection += " and date = ?";
            selectionArg[index++] = values.get("date").toString();
        }
        if (values.get("body") != null) {
            selection += " and body = ?";
            selectionArg[index++] = values.get("body").toString();
        }
        if (values.get("type") != null) {
            selection += " and type = ?";
            selectionArg[index++] = values.get("type").toString();
        }
        if (values.get("read") != null) {
            selection += " and read = ?";
            selectionArg[index++] = values.get("read").toString();
        }
        if (values.get("thread_id") != null) {
            selection += " and thread_id = ?";
            selectionArg[index++] = values.get("thread_id").toString();
        }

        if(selection.length()==0){
            selection = null;
            selectionArg = null;
        }

        Uri uri = Uri.parse("content://sms");
        //final String[] projection = {"COUNT(1)"};
        final String[] projection = {"*"};
        //final String selection = null;//Telephony.Sms.TYPE + "=" + Telephony.Sms.MESSAGE_TYPE_SENT;
        LogUtil.d(TAG,"selection str:"+selection);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    projection,
                    selection,
                    selectionArg,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                LogUtil.d(TAG,"Duplicate message check - sms found.");
                isFound = true;
            }else {
                LogUtil.d(TAG,"Duplicate message check - sms not found.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isFound;
    }
    public static void displayCursorProperties(Cursor query, String msgStr) {
        String[] col = query.getColumnNames();
        String str = "";
        for (int i = 0; i < col.length; i++) {
            str = str + col[i] + ": " + query.getString(i) + ",\n ";
        }
        LogUtil.e(TAG, "*** print cursor properties: "+msgStr + str);
    }
    public static Long get_thread_id(Uri uri, Activity activity) {
        long threadId = 0;
        Cursor cursor = activity.getContentResolver().query(uri, new String[] { "thread_id" },
                null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    threadId = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }
        return threadId;
    }
}
