package com.verizon.contenttransfer.p2p.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SMSMessageVO implements Parcelable {

	String id;
	String body;
	String date;
	String dateSent;
	String deliveryDate;
	String person;
	String read;
	String threadId;
	String type;
	String address;

	public SMSMessageVO() {
		//Blank Object
		id = "";
		body = "";
		date = "";
		dateSent = "";
		deliveryDate = "";
		person = "";
		read = "";
		threadId = "";
		type = "";
		address = "";
	}

	public SMSMessageVO( String _id, String _body, String _date, String _dateSent, String _deliveryDate,
					  String _person, String _read, String _threadId, String _type, String _address) {
		id = _id;
		body = _body;
		date = _date;
		dateSent = _dateSent;
		deliveryDate = _deliveryDate;
		person = _person;
		read = _read;
		threadId = _threadId;
		type = _type;
		address = _address;
	}

	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDateSent() {
		return dateSent;
	}
	public void setDateSent(String date_sent) {
		this.dateSent = date_sent;
	}
	public String getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(String delivery_date) {
		this.deliveryDate = delivery_date;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPerson() {
		return person;
	}
	public void setPerson(String person) {
		this.person = person;
	}
	public String getRead() {
		return read;
	}
	public void setRead(String read) {
		this.read = read;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String thread_id) {
		this.threadId = thread_id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public String toString() {

		StringBuilder sb = new StringBuilder();
		//"CallLog string : "
		sb.append ( "Id:" + getId()+ ", " );
		sb.append ( "Body:" + getBody()+ ", " );
		sb.append ( "Date:" + getDate() + ", " );
		sb.append ( "DateSent:" + getDateSent()+ ", " );
		sb.append ( "DeliveryDate:" + getDeliveryDate() + ", " );
		sb.append ( "Person:" + getPerson()+ ", " );
		sb.append ( "Read:" + getRead() + ", " );
		sb.append ( "ThreadID:" + getThreadId() + ", " );
		sb.append ( "Type:" + getType() + "," );
		sb.append ( "Address:" + getAddress() );

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
		bundle.putString ( "Id", getId() );
		bundle.putString( "Body", getBody() );
		bundle.putString( "Date" , getDate() );
		bundle.putString( "DateSent" , getDateSent() );
		bundle.putString( "DeliveryDate" , getDeliveryDate() );
		bundle.putString( "Person" , getPerson() );
		bundle.putString( "Read" , getRead() );
		bundle.putString( "ThreadId" , getThreadId() );
		bundle.putString( "Type" , getType() );
		bundle.putString( "Address" , getAddress() );

		// write the key value pairs to the parcel
		dest.writeBundle(bundle);
	}

	/**
	 * Creator required for class implementing the parcelable interface.
	 */
	public static final Parcelable.Creator<SMSMessageVO> CREATOR = new Parcelable.Creator<SMSMessageVO>() {

		@Override
		public SMSMessageVO createFromParcel(Parcel source) {
			// read the bundle containing key value pairs from the parcel
			Bundle bundle = source.readBundle();

			// instantiate SMSMessageVO using values from the bundle
			return new SMSMessageVO (
					bundle.getString("Id"),
					bundle.getString("Body"),
					bundle.getString("Date"),
					bundle.getString("DateSent"),
					bundle.getString("DeliveryDate"),
					bundle.getString("Person"),
					bundle.getString("Read"),
					bundle.getString("ThreadId"),
					bundle.getString("Type"),
					bundle.getString("Address")
			);
		}

		@Override
		public SMSMessageVO[] newArray(int size) {
			return new SMSMessageVO[size];
		}

	};

}
