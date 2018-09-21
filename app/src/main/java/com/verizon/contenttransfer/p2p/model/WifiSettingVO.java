package com.verizon.contenttransfer.p2p.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class WifiSettingVO implements Parcelable {
    String _networkid;
    String _ssid;
    String _bssid;
    String _hiddenSSID;
    String _status;
    String _priority;
    String _objToString;

    public WifiSettingVO() {
        //Blank Object
        _networkid= "";
        _ssid = "";
        _bssid = "";
        _hiddenSSID = "";
        _status = "";
        _priority = "";
        _objToString = "";
    }

    public WifiSettingVO(String networkid, String ssid, String bssid,String hiddenSSID, String status, String priority, String objToString) {

        _networkid= networkid;
        _ssid = ssid;
        _bssid = bssid;
        _hiddenSSID = hiddenSSID;
        _status = status;
        _priority = priority;
        _objToString = objToString;
    }
    public String getObjToString() {
        return _objToString;
    }

    public void setObjToString(String objToString) {
        this._objToString = objToString;
    }
    public String getNetworkid() {
        return _networkid;
    }

    public void setNetworkid(String _networkid) {
        this._networkid = _networkid;
    }

    public String getSsid() {
        return _ssid;
    }

    public void setSsid(String _ssid) {
        this._ssid = _ssid;
    }
    public String getBssid() {
        return _bssid;
    }

    public void setBssid(String _bssid) {
        this._bssid = _bssid;
    }

    public String getHiddenSSID() {
        return _hiddenSSID;
    }

    public void setHiddenSSID(String _hiddenSSID) {
        this._hiddenSSID = _hiddenSSID;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String _status) {
        this._status = _status;
    }

    public String getPriority() {
        return _priority;
    }

    public void setPriority(String _priority) {
        this._priority = _priority;
    }


    public String toString() {

        StringBuilder sb = new StringBuilder();
         //"CallLog string : "
        sb.append ( "networkid:" + getNetworkid() + ", " );
        sb.append ( "ssid:" + getSsid() + ", " );
        sb.append ( "bssid:" + getBssid() + ", " );
        sb.append ( "hiddenSSID:" + getHiddenSSID() + ", " );
        sb.append ( "status:" + getStatus() + ", " );
        sb.append ( "priority:" + getPriority() + ", " );
        sb.append ( "OBJTOSTRING" + getObjToString());


        return sb.toString();
    }

    public String toJson() {

        StringBuilder sb = new StringBuilder();

        sb.append( "{");
        sb.append(  toString() );
        sb.append( "}");

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // create a bundle for the key value pairs
        Bundle bundle = new Bundle();

        // insert the key value pairs to the bundle
        bundle.putString( "networkId", getNetworkid() );
        bundle.putString( "SSID", getSsid() );
        bundle.putString( "BSSID" , getBssid() );
        bundle.putString( "hiddenSSID", getHiddenSSID() );
        bundle.putString( "status", getStatus() );
        bundle.putString( "priority" , getPriority() );
        bundle.putString( "OBJTOSTRING",getObjToString());



        // write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Creator<WifiSettingVO> CREATOR = new Creator<WifiSettingVO>() {

        @Override
        public WifiSettingVO createFromParcel(Parcel source) {
            // read the bundle containing key value pairs from the parcel
            Bundle bundle = source.readBundle();

            // instantiate WifiSettingVO using values from the bundle
            return new WifiSettingVO(
                    bundle.getString("networkId"),
                    bundle.getString("SSID"),
                    bundle.getString("BSSID"),
                    bundle.getString("hiddenSSID"),
                    bundle.getString("status"),
                    bundle.getString("priority"),
                    bundle.getString("OBJTOSTRING")
            );
        }

        @Override
        public WifiSettingVO[] newArray(int size) {
            return new WifiSettingVO[size];
        }

    };


}
