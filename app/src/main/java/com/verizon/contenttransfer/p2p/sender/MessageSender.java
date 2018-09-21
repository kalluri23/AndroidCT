/*
 *  -------------------------------------------------------------------------
 *     PROPRIETARY INFORMATION. Not for use or disclosure outside Verizon Wireless, Inc. 
 *     and its affiliates except under written agreement.
 *
 *     This is an unpublished, proprietary work of Verizon Wireless, Inc.
 *     that is protected by United States copyright laws.  Disclosure,
 *     copying, reproduction, merger, translation,modification,enhancement,
 *     or use by anyone other than authorized employees or licensees of
 *     Verizon Wireless, Inc. without the prior written consent of
 *     Verizon Wireless, Inc. is prohibited.
 *
 *     Copyright (c) 2016 Verizon Wireless, Inc.  All rights reserved.
 *  -------------------------------------------------------------------------
 *
 *
 *      Created by kommisu on 10/13/2016.
 */
package com.verizon.contenttransfer.p2p.sender;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.verizon.contenttransfer.utils.LogUtil;

public class MessageSender {

    private static final String TAG = MessageSender.class.getName();
    private Context context = null;

    public MessageSender(Context ctxt) {
        this.context = ctxt;
    }


    public int fetchMMSSMSFromDevice() {
        int totMsgCount = 0;
        LogUtil.d(TAG, "==============================Scan MMS and SMS==============================");
        //Initialize Box
        ContentResolver cr = context.getContentResolver();

        final String[] projection = new String[]{"_id","ct_t"};
        Uri uriboth = Uri.parse("content://mms-sms/complete-conversations?simple=true");
        Cursor query = cr.query(uriboth, projection, null, null, null);
        LogUtil.d(TAG, "query = " + query);
        totMsgCount = query.getCount();

        LogUtil.d(TAG, "sms mms thread count on inbox = " + totMsgCount);
        int currentMsgCount = 0;
        if (query.moveToFirst()) {
            MMSSender mmsSender = new MMSSender(context);
            do {
                //printCursorProperties(query,uriboth.toString());
                String _id = query.getString(query.getColumnIndex("_id"));
                LogUtil.d(TAG,"******** _id ="+_id);
                //Msg msg;
                String msgData = query.getString(query.getColumnIndex("ct_t"));
                if("application/vnd.wap.multipart.related".equals(msgData)) {
                    LogUtil.d(TAG,"It's MMS.");
                    currentMsgCount = mmsSender.fetchMMSFromDevice(context,_id, totMsgCount,currentMsgCount);
                } else {
                    LogUtil.d(TAG,"It's SMS.");
                    //getThreadSentCount(_id);
                    currentMsgCount = SMSSender.fetchSMSFromDevice(context,_id, totMsgCount,currentMsgCount);
                }
            } while (query.moveToNext());
        }


        return totMsgCount;
    }
}
