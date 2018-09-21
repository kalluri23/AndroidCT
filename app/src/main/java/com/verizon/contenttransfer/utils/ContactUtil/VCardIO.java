package com.verizon.contenttransfer.utils.ContactUtil;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSavingMediaModel;
import com.verizon.contenttransfer.model.CTSavingMediaVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DatabaseHelper;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.view.CTSavingMediaView;
import com.verizon.contenttransfer.view.P2PFinishView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class VCardIO extends AsyncTask<Void, CTSavingMediaVO, String> {
	//
	static final String TAG = "VCardIO";
	static final String DATABASE_NAME = "syncdata.db";
	static final String SYNCDATA_TABLE_NAME = "sync";
	static final String PERSONID = "person";
	static final String SYNCID = "syncid";
	private static final int DATABASE_VERSION = 1;
	private static boolean contactsPending = true;
	private static boolean callLogPending = true;
	private static boolean smsPending = true;
	private static boolean calendarPending = true;

	final Object syncMonitor = "SyncMonitor";
	String syncFileName;
	private Activity activity;
	private String contactsState;
	private String calendarsState;
	private String calllogsState;
	private String smsState;
	private int contactcount;

	public VCardIO(Activity activity,String contactsState,String calendarsState,String smsState,String calllogsState,int contactcount) {
		this.activity = activity;
		this.contactsState=contactsState;
		this.calendarsState=calendarsState;
		this.calllogsState=calllogsState;
		this.smsState=smsState;
		this.contactcount=contactcount;
	}

	enum Action {
		IDLE, IMPORT, EXPORT
	};

	private DatabaseHelper mOpenHelper;
	Action mAction;

	@Override
	protected String doInBackground(Void... params) {

		mOpenHelper = new DatabaseHelper(activity.getApplicationContext());
		mAction = Action.IDLE;
		doImport();
		return null;
	}


	@Override
	protected void onPostExecute(String result) {
		synchronized (syncMonitor) {
			switch (mAction) {
				case IMPORT:
					// Tell the user we stopped.
					//Toast.makeText(activity, "VCard import aborted ("+syncFileName +")", Toast.LENGTH_SHORT).show();
					break;
				case EXPORT:
					// Tell the user we stopped.
					//Toast.makeText(activity, "VCard export aborted ("+syncFileName +")", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
			mAction = Action.IDLE;
		}
	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected void onProgressUpdate(CTSavingMediaVO... values) {
		super.onProgressUpdate(values);
		if(values[0].getMediaType() != null) {
			CTSavingMediaView.getInstance().updateSavingMediaProgress();
		}
	}


	private void finishService() {
		if(!contactsPending && !callLogPending && !smsPending && !calendarPending){
			CTGlobal.getInstance().setSavingComplete(true);
			resetVariables();
			LogUtil.d(TAG, "Done. Stopping self");
			try {
				//LogUtil.d(TAG,"P2PStartupActivity.contentTransferContext ="+P2PStartupActivity.contentTransferContext);
				if(null != P2PStartupActivity.contentTransferContext){
					LogUtil.d(TAG,"P2PFinishView.getInstance().getActivity() ="+P2PFinishView.getInstance().getActivity());
					//if (null != P2PFinishView.getInstance().getActivity()){
					if(null!=activity){
						LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(new Intent(VZTransferConstants.ALL_DONE_BROADCAST));
					}
				}
			}catch (Exception e){
				LogUtil.d(TAG, "Updating congrats finish page :" + e.getMessage());
			}
			MessageUtil.resetDefaultSMSApp(activity.getApplicationContext());

		}
		else{
			LogUtil.d(TAG, "Waiting for other operations to finish");
		}
	}

	private void resetVariables() {
		contactsPending = true;
		callLogPending = true;
		smsPending = true;
		calendarPending = true;
	}

	public void  doImport() {

		LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(new Intent(VZTransferConstants.INIT_REVIEW));

		if (contactsState.toLowerCase().trim().equalsIgnoreCase( "true") && CTGlobal.getInstance().isContactsPermitted()) {
			new Thread(new Runnable() {
				public void run() {
					importContacts();
					contactsPending = false;
					finishService();

				}
			}).start();

		} else {
			contactsPending = false;
		}


		if (calendarsState.toLowerCase().trim().equalsIgnoreCase( "true") && CTGlobal.getInstance().isCalendarPermitted()) {
			new Thread(new Runnable() {
				public void run() {
					CTSavingMediaModel.getInstance().processCalendars(activity.getApplicationContext());
					calendarPending = false;
					finishService();
				}
			}).start();
		}
		else{
			calendarPending = false;
		}


	//	if (!CTGlobal.getInstance().isCross() && null != ReceiveMetadata.mediaStateObject ) {
		if (!CTGlobal.getInstance().isCross() ) {

			if (calllogsState.toLowerCase().trim().equalsIgnoreCase( "true") && CTGlobal.getInstance().isCalllogsPermitted()) {
				new Thread(new Runnable() {
					public void run() {
						CTSavingMediaModel.getInstance().startProcessingCallLogRestore(activity.getApplicationContext());
						callLogPending = false;
						finishService();
					}
				}).start();
			}
			else{
				callLogPending = false;
			}


			if (smsState.toLowerCase().trim().equalsIgnoreCase( "true") && CTGlobal.getInstance().isSmsPermitted()) {
				new Thread(new Runnable() {
					public void run() {
						CTSavingMediaModel.getInstance().startProcessingSMSRestore(activity.getApplicationContext());
						smsPending = false;
						finishService();
					}
				}).start();
			}
			else{
				smsPending = false;
				MessageUtil.resetDefaultSMSApp(activity.getApplicationContext());
			}
		}else{
			callLogPending = false;
			smsPending = false;
		}
	}

	private void importContacts() {

		final String fileName = VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CLIENT_CONTACTS_FILE;
		final boolean replace = false;
		// Start lengthy operation in a background thread

		//NotificationUtil.createNotification(VZTransferConstants.CONTACT_NOTIFICATION_INDEX, "Importing Contacts"); //disabling sending notification

		final BufferedReader vcfBuffer;
		try {
			vcfBuffer = new BufferedReader(new FileReader(fileName));
			synchronized (syncMonitor) {
				mAction = Action.IMPORT;
				syncFileName = fileName;
			}

			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			SQLiteStatement querySyncId = db.compileStatement("SELECT " + SYNCID + " FROM " + SYNCDATA_TABLE_NAME + " WHERE " + PERSONID + "=?");
			SQLiteStatement queryPersonId = db.compileStatement("SELECT " + PERSONID + " FROM " + SYNCDATA_TABLE_NAME + " WHERE " + SYNCID + "=?");
			SQLiteStatement insertSyncId = db.compileStatement("INSERT INTO  " + SYNCDATA_TABLE_NAME + " (" + PERSONID + "," + SYNCID + ") VALUES (?,?)");
			Contact parseContact = new Contact(querySyncId, queryPersonId, insertSyncId);
			try {
				int count=0;
				long ret = 0;
				int total= getVcardCount();
				LogUtil.d(TAG,"total contacts count :"+ total);
				do {
					ret = parseContact.parseVCard(vcfBuffer);
					CTSavingMediaVO ctSavingMediaVO = new CTSavingMediaVO();
					if (ret >= 0) {
						if(count>=contactcount) {
							parseContact.addContact(activity.getApplicationContext(), 0, replace);
						}
						count++;
						int perc = count * 100 / total;
						//if (perc % 10 == 0) {
							LogUtil.e(TAG, "Sending progress");
							//NotificationUtil.updateProgress(VZTransferConstants.CONTACT_NOTIFICATION_INDEX, perc, "Saving Contacts: " + perc + "% completed");
							ctSavingMediaVO.setMediaType(VZTransferConstants.CONTACTS_STR);
							ctSavingMediaVO.setPercentageSaved(perc);
							ctSavingMediaVO.setInsideText(""+perc);
						    ctSavingMediaVO.setUImediaType(activity.getString(R.string.contacts_str));

						CTSavingMediaModel.getInstance().updateMediaSavingProgress(VZTransferConstants.CONTACTS_STR, perc,count,activity.getString(R.string.contacts_str));
							publishProgress(ctSavingMediaVO);
						//}
					}
				} while (ret > 0);
				CTSavingMediaModel.getInstance().updateMediaSavingProgress(VZTransferConstants.CONTACTS_STR, 100,total,activity.getString(R.string.contacts_str));
				//NotificationUtil.complete(VZTransferConstants.CONTACT_NOTIFICATION_INDEX, "Contacts import completed");
				db.close();

				synchronized (syncMonitor) {
					mAction = Action.IDLE;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private int getVcardCount(){
		long ret = 0;
		int count=0;
		final String fileName = VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CLIENT_CONTACTS_FILE;
		BufferedReader vcfBuffer = null;
		try {
			vcfBuffer = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Contact parseContact = new Contact(null,null,null);
		do {
			try {
				ret = parseContact.vcardCount(vcfBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (ret >= 0) {
				count++;
			}
		} while (ret > 0);
		return count;
	}

}
