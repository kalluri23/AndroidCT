package com.verizon.contenttransfer.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DefaultSMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 Object[] rawMsgs=(Object[])intent.getExtras().get("pdus");
		 for (Object raw : rawMsgs) {
		      SmsMessage msg=SmsMessage.createFromPdu((byte[])raw);
		      Log.w("SMSReciever:"+msg.getOriginatingAddress(),msg.getMessageBody());
	    }*/
	}
	
}	
