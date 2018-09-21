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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.MMSMessageVO;
import com.verizon.contenttransfer.p2p.model.MMSPartsVO;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MMSSender {

    private static final String TAG = MMSSender.class.getName();
    private Context activity = null;

    public MMSSender(Context ctxt) {
        this.activity = ctxt;
    }
    public int fetchMMSFromDevice(Context ctxt, String msgId , int totMsgCount, int currentMsgCount) {
        LogUtil.d(TAG, "==============================Scan MMS() for id :"+msgId+"==============================");
        ContentResolver cr = ctxt.getContentResolver();
        String[] projection = {"*"};
        String selection = "_id = "+msgId;
        Uri uri = Uri.parse("content://mms");
        Cursor cursor = null;
        try {
            cursor = ctxt.getContentResolver().query(uri,
                    projection,
                    selection,
                    null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                currentMsgCount += 1;
                DataSpeedAnalyzer.updateProgressMessage(ctxt.getString(R.string.PROCESSING_SMS_MMS)+ currentMsgCount +"/"+totMsgCount );

                MMSMessageVO msg = setMMSMessageVO(cursor);

                //LogUtil.d(TAG, "created MMS Json Object");

                boolean appendComma = (currentMsgCount < totMsgCount);

                MediaFileListGenerator.appendToFile(createMMSJsonObject(msg, setMMSPartsVO(msg)), VZTransferConstants.SMS, false, appendComma);

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

        return currentMsgCount;
    }

    private JSONObject createMMSJsonObject(MMSMessageVO msg, List<MMSPartsVO> mmsPartsVOs) {
        JSONObject mmsJsonObject = new JSONObject();
        try {
            mmsJsonObject.put("_id", msg.getId());
            mmsJsonObject.put("thread_id", msg.getThread());
            mmsJsonObject.put("date", msg.getDate());
            mmsJsonObject.put("address", msg.getAddrAry());
            mmsJsonObject.put("read", msg.getRead());
            mmsJsonObject.put("sub", msg.getSub());
            mmsJsonObject.put("m_type", msg.getM_type());
            mmsJsonObject.put("m_cls", msg.getM_cls());
            mmsJsonObject.put("resp_st", msg.getResp_st());
            mmsJsonObject.put("pri", msg.getPri());
            mmsJsonObject.put("v", msg.getV());
            mmsJsonObject.put("sub_cs", msg.getSub_cs());
            mmsJsonObject.put("ct_t", msg.getCt_t());
            mmsJsonObject.put("msg_box", msg.getMsg_box());



            JSONArray jsonMmsPartsArray = new JSONArray();

            if(mmsPartsVOs != null){
                for(int p=0;p<mmsPartsVOs.size();p++){
                    JSONObject partsJsonObject = new JSONObject();
                    partsJsonObject.put("_id", mmsPartsVOs.get(p).getPartId());
                    partsJsonObject.put("ct", mmsPartsVOs.get(p).getType());
                    partsJsonObject.put("text", mmsPartsVOs.get(p).getBody());
                    partsJsonObject.put("cl", mmsPartsVOs.get(p).getCl());
                    partsJsonObject.put("_data", mmsPartsVOs.get(p).getEncodedData());

                    partsJsonObject.put("ctt_t", mmsPartsVOs.get(p).getCtt_t());
                    partsJsonObject.put("ctt_s", mmsPartsVOs.get(p).getCtt_s());
                    partsJsonObject.put("cid", mmsPartsVOs.get(p).getCid());
                    partsJsonObject.put("fn", mmsPartsVOs.get(p).getFn());
                    partsJsonObject.put("cd", mmsPartsVOs.get(p).getCd());
                    partsJsonObject.put("chset", mmsPartsVOs.get(p).getChset());
                    partsJsonObject.put("name", mmsPartsVOs.get(p).getName());
                    partsJsonObject.put("seq", mmsPartsVOs.get(p).getSeq());
                    partsJsonObject.put("mid", mmsPartsVOs.get(p).getMid());

                    jsonMmsPartsArray.add(partsJsonObject);
                }
            }

            mmsJsonObject.put("parts", jsonMmsPartsArray);

        } catch (Exception e) {

        }
        return mmsJsonObject;
    }


    private MMSMessageVO setMMSMessageVO(Cursor c) {
        MMSMessageVO msg = new MMSMessageVO();
        msg.setId(c.getString(c.getColumnIndex("_id")));
        msg.setThread(c.getString(c.getColumnIndex("thread_id")));
        msg.setDate(c.getString(c.getColumnIndex("date")));
        msg.setSub(c.getString(c.getColumnIndex("sub")));
        msg.setRead(c.getString(c.getColumnIndex("read")));
        msg.setM_type(c.getString(c.getColumnIndex("m_type")));
        msg.setM_cls(c.getString(c.getColumnIndex("m_cls")));
        msg.setResp_st(c.getString(c.getColumnIndex("resp_st")));
        msg.setPri(c.getString(c.getColumnIndex("pri")));
        msg.setV(c.getString(c.getColumnIndex("v")));
        msg.setSub_cs(c.getString(c.getColumnIndex("sub_cs")));
        msg.setCt_t(c.getString(c.getColumnIndex("ct_t")));
        msg.setMsg_box(c.getString(c.getColumnIndex("msg_box")));
        msg.setAddrAry(getAddressAryNumber(msg.getId()));
        return msg;
    }

    private String getAddressNumberOfRecipient(int threadId) {
        String selectionAdd = Telephony.Threads._ID + "=" + threadId;
        String uriStr = MessageFormat.format("content://mms-sms/conversations/{0}/recipients", threadId);
        Uri uriAddress = Uri.parse(uriStr);
        String[] columns = {Telephony.Threads.RECIPIENT_IDS};
        Cursor cAdd = activity.getContentResolver().query(uriAddress, columns, selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                name = cAdd.getString(cAdd.getColumnIndex(Telephony.Threads.RECIPIENT_IDS));
                if (!TextUtils.isEmpty(name)) {
                    break;
                }
            }
            while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        LogUtil.d(TAG, " getAddressNumberOfRecipient = " + name);
        return TextUtils.isEmpty(name) ? "" : getCanonicalRecipient(Integer.parseInt(name.split(" ")[0]));
    }

    private String getCanonicalRecipient(int recipientId) {
        String selectionAdd = Telephony.CanonicalAddressesColumns._ID + "=" + recipientId;
        String uriStr = MessageFormat.format("content://mms-sms/canonical-address/{0}", recipientId);
        Uri uriAddress = Uri.parse(uriStr);
        String[] columns = {Telephony.CanonicalAddressesColumns.ADDRESS};
        Cursor cAdd = activity.getContentResolver().query(uriAddress, columns, selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                name = cAdd.getString(cAdd.getColumnIndex(Telephony.CanonicalAddressesColumns.ADDRESS));
                if (!TextUtils.isEmpty(name)) {
                    break;
                }
            }
            while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        LogUtil.d(TAG, " getCanonicalRecipient = " + name);
        //return TextUtils.isEmpty(name) ? "" : filterPhoneNumber(name);
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public List<MMSPartsVO> setMMSPartsVO(MMSMessageVO msg) {
        Uri uri = Uri.parse("content://mms/part");
        String mmsId = "mid = " + msg.getId();
        Cursor c = activity.getContentResolver().query(uri, null, mmsId, null, null);
        List<MMSPartsVO> mmsPartsVOs = new ArrayList<MMSPartsVO>();
        if (c == null) {
            LogUtil.d(TAG, "MMS part cursor is null");
            return mmsPartsVOs;
        }

        String encodedData = null;
        String data = null;
        String body = null;
        String cl = null;

        while (c.moveToNext()) {

            if(!Utils.shouldCollect(VZTransferConstants.SMS_STR)){
                break;
            }

/*            String[] col = c.getColumnNames();
            String str = "";
            for (int i = 0; i < col.length; i++) {
                str = str + col[i] + ": " + c.getString(i) + ", ";
            }
            LogUtil.d(TAG, "Parsing MMS Message : " + str);*/
            //LogUtil.d(TAG, "Parsing MMS Message parts");

            String type = c.getString(c.getColumnIndex("ct"));

            String partId = c.getString(c.getColumnIndex("_id"));
            //LogUtil.d(TAG, "Parsing MMS Message partId : " + partId);

            //LogUtil.d(TAG, "Parsing MMS Message type : " + type);
            cl = c.getString(c.getColumnIndex("cl"));
            msg.setMediaFileName(cl);
            msg.setMediaType(type);

            //_id: 342, mid: 135, seq: 0, ct: image/jpeg, name: null, chset: null, cd: null, fn: null, cid: <IMG_1648>,
            // cl: IMG_1648.JPG, ctt_s: null, ctt_t: null, _data: /data/data/com.android.providers.telephony/app_parts/PART_1483127540033_IMG_1648.JPG, text: null,
            String mid = c.getString(c.getColumnIndex("mid"));
            String seq = c.getString(c.getColumnIndex("seq"));
            String name = c.getString(c.getColumnIndex("name"));
            String chset = c.getString(c.getColumnIndex("chset"));
            String cd = c.getString(c.getColumnIndex("cd"));
            String fn = c.getString(c.getColumnIndex("fn"));
            String cid = c.getString(c.getColumnIndex("cid"));
            String ctt_s = c.getString(c.getColumnIndex("ctt_s"));
            String ctt_t = c.getString(c.getColumnIndex("ctt_t"));

            if ("text/plain".equals(type)) {
                //LogUtil.d(TAG, "text/plain ");
                data = c.getString(c.getColumnIndex("_data"));
                //LogUtil.d(TAG, "Data URI : " + data);

                if (data != null) {
                    body = getMmsText(partId);
                } else {
                    body = c.getString(c.getColumnIndex("text"));
                }
                //LogUtil.d(TAG, "Body : " + body);
                body = MessageUtil.stripHtml(body);
                msg.setBody(msg.getBody() + body);

            } else if (MessageUtil.isMediaInBitmap(type)) {
                //LogUtil.d(TAG, "Message has a image...");

                data = c.getString(c.getColumnIndex("_data"));

                //LogUtil.d(TAG, "Data URI : " + cl + " -- " + data);

                //msg.setImg(getMmsImg(partId));
                //encodedData = MMS Util.getEncodedStringFromImage(msg.getImg());

                encodedData = MessageUtil.getEncodedStringFromByteArray(getImageInBytes(partId));

            } else if (MessageUtil.isMediaInByteArray(type)) {
                //LogUtil.d(TAG, "Message has Video...");

                data = c.getString(c.getColumnIndex("_data"));
                //name = c.getString(c.getColumnIndex("cl"));
                //LogUtil.d(TAG, "Data URI : " + cl + " -- " + data);

                msg.setMedia(getMMSByte(partId));
                encodedData = MessageUtil.getEncodedStringFromByteArray(msg.getMedia());

            } else {
                //Audio test
                //LogUtil.d(TAG, "Message Type----: " + type.toString());
            }
            //LogUtil.d(TAG, "End of inserting mms image....getDecodedMedia of [" + type + "] " + msg.getDecodedMedia());
            try {
                MMSPartsVO mmsPartsVO = new MMSPartsVO();
                mmsPartsVO.setPartId(partId);
                mmsPartsVO.setType(type);
                mmsPartsVO.setBody(body);
                mmsPartsVO.setCl(cl);
                mmsPartsVO.setEncodedData(encodedData);
                mmsPartsVO.setCtt_t(ctt_t);
                mmsPartsVO.setCtt_s(ctt_s);
                mmsPartsVO.setCid(cid);
                mmsPartsVO.setFn(fn);
                mmsPartsVO.setCd(cd);
                mmsPartsVO.setChset(chset);
                mmsPartsVO.setName(name);
                mmsPartsVO.setSeq(seq);
                mmsPartsVO.setMid(mid);
                mmsPartsVOs.add(mmsPartsVO);
                //_id: 342, mid: 135, seq: 0, ct: image/jpeg, name: null, chset: null, cd: null, fn: null, cid: <IMG_1648>, cl: IMG_1648.JPG, ctt_s: null, ctt_t: null, _data: /data/data/com.android.providers.telephony/app_parts/PART_1483127540033_IMG_1648.JPG, text: null,


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        c.close();
        return mmsPartsVOs;
    }

    public String getMmsText(String id) {

        Uri partURI = Uri.parse("content://mms/part/" + id);

        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = activity.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }


    public byte[] getImageInBytes(String id){
        Uri uri = Uri.parse("content://mms/part/" + id);
        InputStream in = null;
        byte[] byteArray = null;
        try {
            System.gc();
            in = activity.getContentResolver().openInputStream(uri);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            if (in != null) {
                in.close();
            }

            buffer.flush();
            byteArray = buffer.toByteArray();
        }catch (Exception e){

        }
        return byteArray;
    }
    public Bitmap getMmsImg(String id) {
        Uri uri = Uri.parse("content://mms/part/" + id);
        InputStream in = null;
        Bitmap bitmap = null;
        try {
            System.gc();
            in = activity.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in);
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtil.d(TAG, "getMMSImg returning image bitmap --" + bitmap);


        return bitmap;
    }
    Bitmap ShrinkBitmap(String file, int width, int height){

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

        if (heightRatio > 1 || widthRatio > 1)
        {
            if (heightRatio > widthRatio)
            {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }
    public byte[] getMMSByte(String id) {
        Uri uri = Uri.parse("content://mms/part/" + id);
        byte[] rawData = LoadRawData(uri);

        return rawData;
    }

    public byte[] getMmsAudio(String id) {
        Uri uri = Uri.parse("content://mms/part/" + id);
        byte[] rawAudio = LoadRawData(uri);

        return rawAudio;
    }


    /**
     * Get senders address
     *
     * @param id
     * @return
     */
    private JSONArray getAddressAryNumber(String id) {
        //LogUtil.d(TAG, "getAddressAryNumber - id=" + id);
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = activity.getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String name = null;
        //List<String> addrAry = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray();
        if (cAdd.moveToFirst()) {
            do {
                String[] col = cAdd.getColumnNames();
/*                String str = "";
                for (int i = 0; i < col.length; i++) {
                    str = str + col[i] + ": " + cAdd.getString(i) + ", ";
                }
                LogUtil.d(TAG, "Scan MMS : " + str);*/

                JSONObject jsonObject = new JSONObject();
                String _id = cAdd.getString(cAdd.getColumnIndex("_id"));
                String msg_id = cAdd.getString(cAdd.getColumnIndex("msg_id"));
                String contact_id = cAdd.getString(cAdd.getColumnIndex("contact_id"));
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                String type = cAdd.getString(cAdd.getColumnIndex("type"));
                String charset = cAdd.getString(cAdd.getColumnIndex("charset"));
                LogUtil.d(TAG, "getAddressAryNumber -  _id =" + _id
                        + " msg_id =" + msg_id
                        + " contact_id = " + contact_id
                        + " type =" + type
                        + " charset =" + charset
                        + " number = " + number);

                if (number != null) {
                    //getAddressNumberOfRecipient(Integer.parseInt(_id),activity);
                    if (!number.equals("insert-address-token")) {
                        try {
                            //number = number.replace("-", "");
                            number = number.replaceAll("[\\- ()]", "");
                            //LogUtil.d(TAG, "clean address :" + number);
                            Long.parseLong(number);
                            name = number;

                            //LogUtil.d(TAG, "Sender Number :" + number);
                        } catch (NumberFormatException nfe) {
                            if (name == null) {
                                name = number;
                            }
                        }
                        try {
                            jsonObject.put("_id", _id);
                            jsonObject.put("msg_id", msg_id);
                            jsonObject.put("contact_id", contact_id);
                            jsonObject.put("address", name);
                            jsonObject.put("type", type);
                            jsonObject.put("charset", charset);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        jsonArray.add(jsonObject);
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }

        return jsonArray;
    }

    private static final int RAW_DATA_BLOCK_SIZE = 8192; //Set the block size to 8K, used to write a ByteArrayOutputStream to byte[]
    public static final int ERROR_IO_EXCEPTION = 1;
    public static final int ERROR_FILE_NOT_FOUND = 2;
    //private int Error= 0;

    public byte[] LoadRawData(Uri uri) {
        int Error = 0;
        InputStream inputStream = null;
        byte[] ret = new byte[0];

        //Open inputStream from the specified URI
        try {
            inputStream = activity.getContentResolver().openInputStream(uri);

            //Try read from the InputStream
            if (inputStream != null) {
                ret = InputStreamToByteArray(inputStream);
            }

        } catch (FileNotFoundException e1) {
            Error = ERROR_FILE_NOT_FOUND;
        } catch (IOException e) {
            Error = ERROR_IO_EXCEPTION;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Error = ERROR_IO_EXCEPTION;
                }
            }
        }

        return ret;
    }


    //Create a byte array from an open inputStream. Read blocks of RAW_DATA_BLOCK_SIZE byte
    private static byte[] InputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[RAW_DATA_BLOCK_SIZE];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
    //<!--------------       S M S    S C A N         ---------------------->


}
