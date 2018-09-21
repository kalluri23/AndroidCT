package com.verizon.contenttransfer.p2p.receiver;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import com.verizon.contenttransfer.p2p.model.MMSMessageVO;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by c0bissh on 12/8/2016.
 */
public class InsertMMS {
    private static String TAG = InsertMMS.class.getName();

    //private HashMap<String,byte[]> bytesMap = new HashMap<String,byte[]>();
    public InsertMMS() {
    }

    public synchronized long insertMms(Context context, final MMSMessageVO msg)
    {
        long thread_id = -1;
        Log.d(TAG,"***************insertMms START*****************");
        try
        {
            //String msgPartsArray  = msg.getPartsStr();
/*            if(bytesMap!=null){
                bytesMap.clear();
            }*/
            ContentResolver resolver = context.getContentResolver();
            String[] toAddrOnly = getReceipentsAddress(msg.getAddrAryStr(), msg.getMsg_box());
            //long dataBytesLength = getBytesLength(msgPartsArray);
            // Get thread id
            Set<String> recipients = new HashSet<String>();

            recipients.addAll(Arrays.asList(toAddrOnly));
            if(msg.getThread() != null){
                thread_id = Long.parseLong(msg.getThread());
            }else {
                thread_id = getOrCreateThreadId(context, recipients);
            }
            LogUtil.d(TAG, "*** Thread ID is ****** MMS: " + msg.getThread());
            // Create a dummy sms
/*            ContentValues dummyValues = new ContentValues();
            dummyValues.put("thread_id", thread_id);
            dummyValues.put("body", "Dummy SMS body.");
            Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);*/


            // Create a new message entry
            long dateLong = (msg.getDate() == null ? System.currentTimeMillis() : Long.parseLong(msg.getDate()));
            Log.e(TAG, "dateLong is " + dateLong);
            ContentValues mmsValues = new ContentValues();
            //mmsValues.put("_id",msg.getId());
            mmsValues.put("thread_id", thread_id);

            mmsValues.put("date", dateLong);
            if(null != msg.getMsg_box()) {
                mmsValues.put("msg_box", Integer.parseInt(msg.getMsg_box()));
            }
            mmsValues.put("read", 1);
            if(msg.getSub() != null) {
                mmsValues.put("sub", msg.getSub());
            }
            if(null != msg.getSub_cs()) {
                mmsValues.put("sub_cs", Integer.parseInt(msg.getSub_cs()));
            }
            if(msg.getCt_t() != null) {
                mmsValues.put("ct_t", msg.getCt_t());
            }
            //mmsValues.put("exp", dataBytesLength);
            if(null != msg.getM_cls()) {
                mmsValues.put("m_cls", msg.getM_cls());
            }
            if(msg.getM_type()!=null) {
                mmsValues.put("m_type", Integer.parseInt(msg.getM_type())); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
            }
            if(msg.getV() != null) {
                mmsValues.put("v", msg.getV());
            }
            if(null != msg.getPri()) {
                mmsValues.put("pri", Integer.parseInt(msg.getPri()));
            }
            //mmsValues.put("tr_id", "T"+ Long.toHexString(dateLong));
            if(null != msg.getResp_st()) {
                mmsValues.put("resp_st", Integer.parseInt(msg.getResp_st()));
            }

            boolean isMmsFound = MessageUtil.isMMSExist(mmsValues,context);


            LogUtil.d(TAG, "inserting mms - is Mms already exist :"+isMmsFound);
            Uri res = null;

            if(!isMmsFound){
                // Insert message

                res = resolver.insert(Uri.parse("content://mms"), mmsValues);
                String messageId = res.getLastPathSegment().trim();
                //msg.setMmsId(messageId);
                LogUtil.d(TAG, "messageId =" + messageId + " , Message saved as " + res);
                JSONArray partsArray = msg.getParts();
                if(partsArray != null) {
                    createMMSParts(context, partsArray, messageId);
                }
                // Create addresses
                if(msg.getAddrAryStr()!=null) {
                    CreateMMSAddress(context, msg.getAddrAryStr(), messageId);
                }
            }

            // Delete dummy sms
/*            context.getContentResolver().delete(dummySms, null, null);*/

            return thread_id;
        }
        catch (Exception e)
        {
            LogUtil.d(TAG,"Exception : "+e.getMessage());
            e.printStackTrace();
        }
        Log.d(TAG,"***************insertMms END*****************");
        return thread_id;
    }

    private void CreateMMSAddress(Context context, String addrArrayStr, String messageId) {
        try {
            if(addrArrayStr != null){
                JSONParser parser = new JSONParser();

                JSONArray jsonAddrArray = (JSONArray) parser.parse(addrArrayStr);
                LogUtil.d(TAG,"Create mms address - size: "+jsonAddrArray.size());
                for (int i = 0; i < jsonAddrArray.size(); i++) {
                    createMmsPartsAddr(context, messageId, (JSONObject)jsonAddrArray.get(i));
                }
            }
        }catch (Exception je){
            LogUtil.d(TAG,"Exception on MMS address insert :"+je.getMessage());
        }
    }

    private void createMMSParts(Context context, JSONArray partsArray, String messageId){


            try{
                JSONParser jparser = new JSONParser();
                //JSONArray partsArray = (JSONArray) jparser.parse(msgPartsArray);
                // Create part
                if(messageId!=null && partsArray != null && partsArray.size()>0){

                    String type = null;
                    byte[] dataBytes = null;

                    LogUtil.d(TAG, "parts size =" + partsArray.size());
                    ContentValues mmsPartValue = new ContentValues();
                    for(int p=0;p<partsArray.size();p++){
                        JSONObject jsonObject = (JSONObject) partsArray.get(p);
                        LogUtil.d(TAG, "Parts json string =" + jsonObject.toString());
                        type = jsonObject.get("ct").toString();
                        mmsPartValue.clear();
                        if(!type.contains( "application/smil" )) {
                            mmsPartValue.put("mid", messageId);
                            mmsPartValue.put("ct", type);
                            if(jsonObject.get("cl") != null) {
                                mmsPartValue.put("cl", jsonObject.get("cl").toString());
                            }
                            if(jsonObject.get("fn")!=null) {
                                mmsPartValue.put("fn", jsonObject.get("fn").toString());
                            }
                            if(jsonObject.get("text")!=null) {
                                mmsPartValue.put("text", Utils.getDecodedString(jsonObject.get("text").toString()));
                            }
                            mmsPartValue.put("cid", jsonObject.get("cid").toString());
                            Uri partUri = Uri.parse("content://mms/" + messageId + "/part");
                            Uri partRes = context.getContentResolver().insert(partUri, mmsPartValue);
                            Log.d(TAG, "Part uri is " + partRes.toString());

                            if (MessageUtil.isMediaInBitmap(type)
                                    || MessageUtil.isMediaInByteArray(type)){

                                // Add data to part

                                dataBytes = MessageUtil.getByteArrayFromEncodedString(jsonObject.get("_data").toString());

                                if (dataBytes != null) {

                                    OutputStream os = context.getContentResolver().openOutputStream(partRes);
                                    if (null == os) {
                                        LogUtil.d(TAG, "Failed to open media output stream.");
                                    } else {
                                        LogUtil.d(TAG, "current data byte length =" + dataBytes.length);
                                        ByteArrayInputStream is = new ByteArrayInputStream(dataBytes);
                                        byte[] buffer = new byte[256];
                                        for (int len = 0; (len = is.read(buffer)) != -1; ) {
                                            os.write(buffer, 0, len);
                                        }
                                        os.close();
                                        is.close();
                                    }
                                }
                            }
                        }
/*                        else {

                            mmsPartValue.put("seq", -1);
                            mmsPartValue.put("text", jsonObject.get("text").toString());
                            Uri partUri = Uri.parse("content://mms/" + messageId + "/part");
                            Uri partRes = context.getContentResolver().insert(partUri, mmsPartValue);
                        }*/
                    }
                }
            }catch(Exception e){
                LogUtil.d(TAG,"Insert mms parts exception :"+e.getMessage());
            }


    }


    private String[] getReceipentsAddress(String jsonArrayStr, String msg_box) {
        if(jsonArrayStr == null ){
            return null;
        }
        List<String> toNumber = new ArrayList<String>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(jsonArrayStr);
            if(jsonArray!=null && jsonArray.size()>0) {
                for (int i = 0; i <jsonArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject)jsonArray.get(i);

                    //jsonObject = new JSONObject(jsonArray.get(i).toString());
                    LogUtil.d(TAG,"msg type :"+jsonObject.get("type")+  "   msg_box ="+msg_box);

                    if(msg_box.equals(MessageUtil.MESSAGE_TYPE_INBOX)){
                        if (jsonObject.get("type").toString().equals(MessageUtil.FROM_TYPE) && !jsonObject.get("address").equals(MessageUtil.INSERT_ADDRESS_TOKEN)) {
                            Log.d(TAG, "TO NUMBER :" + jsonObject.get("address").toString());
                            toNumber.add(jsonObject.get("address").toString());
                        }
                    }/*else if (msg_box.equals(toMsgBox)) */{
                        if (jsonObject.get("type").toString().equals(MessageUtil.TO_TYPE)) {
                            Log.d(TAG, "TO NUMBER :" + jsonObject.get("address").toString());
                            toNumber.add(jsonObject.get("address").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        String[] addrArr = new String[toNumber.size()];
        addrArr = toNumber.toArray(addrArr);

        return addrArr;
    }

    private  Uri createMmsPartsAddr(Context context, String mid, JSONObject jsonObject)
    {
        if(mid != null) {
            try {
                Log.d(TAG,"create addr : "+jsonObject.toString());
                ContentValues addrValues = new ContentValues();
                LogUtil.d(TAG, "Create address  == "+jsonObject.get("address"));
                addrValues.put("address", (null!=jsonObject.get("address")?jsonObject.get("address").toString():null));
                if(null != jsonObject.get("charset")) {
                    addrValues.put("charset", jsonObject.get("charset").toString());
                }
                if(null != jsonObject.get("type")) {
                    addrValues.put("type", Integer.parseInt(jsonObject.get("type").toString())); // TO
                }
                addrValues.put("msg_id", (null != jsonObject.get("msg_id")?jsonObject.get("msg_id").toString():null));
                Uri addrUri = Uri.parse("content://mms/"+ mid +"/addr");
                Uri res = context.getContentResolver().insert(addrUri, addrValues);
                Log.d(TAG, "Addr uri is " + res.toString());
                return res;
            }catch (Exception e){
                LogUtil.d(TAG,"createMmsPartsAddr exception :"+e.getMessage());
            }
        }

        LogUtil.d(TAG,"createMmsPartsAddr finished.");
        return null;
    }
    @SuppressLint("NewApi")
    private long getOrCreateThreadId(Context context, Set<String> recipients) throws Exception
    {
        return Telephony.Threads.getOrCreateThreadId(context, recipients);
        //return 11;
    }

}
