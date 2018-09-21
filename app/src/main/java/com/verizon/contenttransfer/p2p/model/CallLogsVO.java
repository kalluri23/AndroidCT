package com.verizon.contenttransfer.p2p.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CallLogsVO implements Parcelable {
    String _id;
    String _name;
    String _date;
    String _duration;
    String _type;
    String _number;
    String _new;
    String _cachedNumberType;
    String _cachedNumberLabel;

    public CallLogsVO() {
        //Blank Object
        _id= "";
        _name = "";
        _date = "";
        _duration = "";
        _type = "";
        _number = "";
        _new = "" ;
        _cachedNumberLabel = "";
        _cachedNumberType = "";
    }

    public CallLogsVO(String id, String name, String date, String duration, String type, String number,
            String neww, String cachedNumberType, String cachedNumberLabel) {

        _id= id;
        _name = name;
        _date = date;
        _duration = duration;
        _type = type;
        _number = number;
        _new = neww ;
        _cachedNumberLabel = cachedNumberLabel;
        _cachedNumberType = cachedNumberType;

    }

    public String getCachedNumberType() {
        return _cachedNumberType;
    }

    public void setCachedNumberType(String _cachedNumberType) {
        this._cachedNumberType = _cachedNumberType;
    }

    public String getCachedNumberLabel() {
        return _cachedNumberLabel;
    }

    public void setCachedNumberLabel(String _cachedNumberLabel) {
        this._cachedNumberLabel = _cachedNumberLabel;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getDuration() {
        return this._duration;
    }


    public String getName() {
        return this._name;
    }

    public String getNew() {
        return this._new;
    }

    public String getNumber() {
        return this._number;
    }

    public String getType() {
        return this._type;
    }

    public String getDate() {
        return this._date;
    }

    public void setDate(String paramString) {
        this._date = paramString;
    }

    public void setDuration(String paramString) {
        this._duration = paramString;
    }

    public void setName(String paramString) {
        this._name = paramString;
    }

    public void setNew(String paramString) {
        this._new = paramString;
    }

    public void setNumber(String paramString) {
        this._number = paramString;
    }

    public void setType(String paramString) {
        this._type = paramString;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
         //"CallLog string : "
        sb.append ( "Id:" + getId() + ", " );
        sb.append ( "Name:" + getName() + ", " );
        sb.append ( "Date:" + getDate() + ", " );
        sb.append ( "Duration:" + getDuration() + ", " );
        sb.append ( "Number:" + getNumber() + ", " );
        sb.append ( "New:" + getNew() + ", " );
        sb.append ( "CachedNumberType:" + getCachedNumberType() + ", " );
        sb.append ( "CachedNumberLabel:" + getCachedNumberLabel() + ", " );
        sb.append ( "Type:" + getType() );

        return sb.toString();
    }

/*    public String toJson() { // unused method

        StringBuilder sb = new StringBuilder();

        sb.append( "{");
        sb.append(  toString() );
        sb.append( "}");

        return sb.toString();
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // create a bundle for the key value pairs
        Bundle bundle = new Bundle();

        // insert the key value pairs to the bundle
        bundle.putString ( "Id", getId() );
        bundle.putString( "Name", getName() );
        bundle.putString( "Date" , getDate() );
        bundle.putString( "Duration" , getDuration() );
        bundle.putString( "Number" , getNumber() );
        bundle.putString( "New" , getNew() );
        bundle.putString( "CachedNumberType" , getCachedNumberType() );
        bundle.putString( "CachedNumberLabel" , getCachedNumberLabel() );
        bundle.putString( "Type" , getType() );

        // write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<CallLogsVO> CREATOR = new Creator<CallLogsVO>() {

        @Override
        public CallLogsVO createFromParcel(Parcel source) {
            // read the bundle containing key value pairs from the parcel
            Bundle bundle = source.readBundle();

            // instantiate CallLogsVO using values from the bundle
            return new CallLogsVO (
                    bundle.getString("Id"),
                    bundle.getString("Name"),
                    bundle.getString("Date"),
                    bundle.getString("Duration"),
                    bundle.getString("Number"),
                    bundle.getString("New"),
                    bundle.getString("CachedNumberType"),
                    bundle.getString("CachedNumberLabel"),
                    bundle.getString("Type")
            );
        }

        @Override
        public CallLogsVO[] newArray(int size) {
            return new CallLogsVO[size];
        }

    };
}
