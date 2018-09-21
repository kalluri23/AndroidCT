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
 *      Created by c0bissh on 12/12/2016.
 */

package com.verizon.contenttransfer.p2p.model;

import android.graphics.Bitmap;

import com.verizon.contenttransfer.utils.MessageUtil;

import org.json.simple.JSONArray;
/**
 * Created by c0bissh on 12/16/2016.
 */
public class MMSMessageVO {


    private String id;
    private String t_id;
    private String date;
    private JSONArray addrAry;
    private String addrAryStr;
    private String body;
    private Bitmap img;

    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";
    public static final String IMAGE = "image";
    public static final String TEXT = "text/plain";
    public static final String SMIL = "application/smil";
    private byte[] media;

    private String m_cls;
    private String resp_st;
    private String pri;
    private String v;
    private String sub_cs;
    private String mediaType;
    private byte[] decodedMedia;
    private boolean bData;
    private String mmsId;
    private String sub;
    private String read;
    private String type;
    private String mediaFileName;

    private String m_type;
    private String ct_t;
    private String msg_box;

    private JSONArray parts;

    public String getPartsStr() {
        return partsStr;
    }

    public void setPartsStr(String partsStr) {
        this.partsStr = partsStr;
    }

    public String getAddrAryStr() {
        return addrAryStr;
    }

    public void setAddrAryStr(String addrAryStr) {
        this.addrAryStr = addrAryStr;
    }

    private String partsStr;

    public MMSMessageVO() {

    }



    public byte[] getDecodedMedia() {
        return decodedMedia;
    }

    public void setDecodedMedia(byte[] decodedMedia) {
        this.decodedMedia = decodedMedia;
    }

    public String getMediaType() {
        return mediaType;
    }


    public String getMmsId() {
        return mmsId;
    }

    public void setMmsId(String mmsId) {
        this.mmsId = mmsId;
    }


    public String getMediaFileName() {
        return mediaFileName;
    }

    public void setMediaFileName(String mediaFileName) {
        this.mediaFileName = mediaFileName;
    }


    public String getMsg_box() {
        return msg_box;
    }

    public void setMsg_box(String msg_box) {
        this.msg_box = msg_box;
    }

    public String getCt_t() {
        return ct_t;
    }

    public void setCt_t(String ct_t) {
        this.ct_t = ct_t;
    }


    public String getM_type() {
        return m_type;
    }

    public void setM_type(String m_type) {
        this.m_type = m_type;
    }

    public String getM_cls() {
        return m_cls;
    }

    public void setM_cls(String m_cls) {
        this.m_cls = m_cls;
    }

    public String getResp_st() {
        return resp_st;
    }

    public void setResp_st(String resp_st) {
        this.resp_st = resp_st;
    }

    public String getPri() {
        return pri;
    }

    public void setPri(String pri) {
        this.pri = pri;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getSub_cs() {
        return sub_cs;
    }

    public void setSub_cs(String sub_cs) {
        this.sub_cs = sub_cs;
    }

    public byte[] getMedia() {
        return media;
    }
    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }


    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getId(){
        return id;
    }
    public void setId(String ID){
        id = ID;
        body = "";
    }
    public void setDate(String d) {
        date = d;
    }

    public void setThread(String d) { t_id = d; }


    public JSONArray getAddrAry() {
        return addrAry;
    }

    public void setAddrAry(JSONArray addrAry) {
        this.addrAry = addrAry;
    }


    public void setBody(String b) {
        body = b;
        bData = false;
    }

    public void setImg(Bitmap bm) {
        img = bm;
        if (bm != null) {
            bData = true;
        } else {
            bData = false;
        }
    }

    public void setMedia( byte[] med ) {
        media = med;
        if (media != null) {
            bData = true;
        } else {
            bData = false;
        }
    }

    public void setMediaType( String type) {
        mediaType = type;
    }

    public String getDate() {
        return date;
    }

    public String getThread() { return t_id; }
    public String getID() { return id; }
    public String getBody() { return body; }
    public Bitmap getImg() { return img; }

    public String toString() {

        String s = id + ": "  + body;
        if (bData) {
            //s = s + "\nData: " + img;
            if ( mediaType.contains( VIDEO) ) {
                s = s + "\nVideo: " + MessageUtil.getEncodedStringFromByteArray( media );
            }else if ( mediaType.contains( AUDIO) ) {
                s = s + "\nAudio: " + MessageUtil.getEncodedStringFromByteArray( media );
            } else if ( mediaType.contains( IMAGE) ){
                s = s + "\nImage: " + MessageUtil.getEncodedStringFromImage(img);
            }
        }
        return s;
    }
    public JSONArray getParts() {
        return parts;
    }
    public void setParts(JSONArray parts) {
        this.parts = parts;
    }
}
