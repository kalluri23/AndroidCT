package com.verizon.contenttransfer.p2p.sender;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.P2PSetupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class ContactsSender { 	//extends AsyncTask<Void, Void, String> {

	private static final String TAG = ContactsSender.class.getName();

	
	 /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    public interface ContactsQuery {

        // An identifier for the loader
        final static int QUERY_ID = 1;

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = Contacts.CONTENT_URI;

        // The search/filter query Uri
        final static Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

        // The selection clause for the CursorLoader query. The search criteria defined here
        // restrict results to contacts that have a display name and are linked to visible groups.
        // Notice that the search on the string provided by the user is implemented by appending
        // the search string to CONTENT_FILTER_URI.
        @SuppressLint("InlinedApi")
        final static String SELECTION =
                (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME) +
                "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";

        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        final static String SORT_ORDER =
                Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;

        // The projection for the CursorLoader query. This is a list of columns that the Contacts
        // Provider should return in the Cursor.
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                // The contact's row id
                Contacts._ID,

                // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
                // a "permanent" contact URI.
                Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
                // some other useful identifier such as an email address. This column isn't
                // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
                // instead.
                Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
                // you generate the pointer from the contact's ID value and constants defined in
                // android.provider.ContactsContract.Contacts.
                Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID,

                // The sort order column for the returned Cursor, used by the AlphabetIndexer
                SORT_ORDER,
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int PHOTO_THUMBNAIL_DATA = 3;
        final static int SORT_KEY = 4;
    }
	

	public static void sendContacts(Socket iosClientSocket,CTAnalyticUtil ctAnalyticUtil) {
		try {
			String hostIp = iosClientSocket.getInetAddress().getHostAddress();
			String CTS_HEADER = VZTransferConstants.VCARD_TRANSFER_REQUEST_HEADER;
			OutputStream out = iosClientSocket.getOutputStream();


			File contactsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CONTACTS_FILE );
			if ( contactsFile.exists() ) {
				DataSpeedAnalyzer.setProgressbarProperties(CTGlobal.getInstance().getContentTransferContext().getString(R.string.contacts_str),VZTransferConstants.CONTACTS_FILE);
				InputStream contactsStream = new FileInputStream( contactsFile );   //getContentResolver().openInputStream( selectedFileUri );
				
				long contactsFileSize= contactsFile.length();

				//send header now


				String filesize = Long.toString( contactsFileSize );
				LogUtil.d(TAG, "File size : " + filesize );

				int numDigits = filesize.length();
				LogUtil.d(TAG, "Numb of digists : " +  numDigits);

				if ( numDigits < 10 ) {
					for (int k=0; k < (10-numDigits); k++ ) {
						filesize ="0"+filesize;
					}
				}

				LogUtil.d(TAG, "File size : " + filesize );
				String contactsHeader = CTS_HEADER+filesize;

				out.write( contactsHeader.getBytes() );
				//if(!CTGlobal.getInstance().isCross()){
				if(!ctAnalyticUtil.isCross()){
					out.write(VZTransferConstants.CRLF.getBytes());
				}
				out.flush();

				byte[] buffer = new byte[1024];
				int len = contactsStream.read(buffer);
				long totRead = 0;
				ctAnalyticUtil.addFileTransferredCount(1);
				while (len != -1) {
					out.write(buffer, 0, len);
					totRead += len;
					LogUtil.d(TAG, "Contacts file transfer in progress... totRead " + totRead);
					Utils.updateTransferredBytes(ctAnalyticUtil, len);
					len = contactsStream.read(buffer);
				}
				out.flush();
				LogUtil.d(TAG, "Contacts File transfer complete");
			} else {
				LogUtil.e(TAG, "There is no contacts file to transfer..");
				String contactsHeader = CTS_HEADER+"0000000000";
				out.write( contactsHeader.getBytes() );
				out.flush();
			}

		} catch (FileNotFoundException e) {
			LogUtil.e(TAG, "contacts file not found" + e.getMessage());
		} catch (IOException e) {
			LogUtil.e(TAG, "Failed to send contact file" + e.getMessage());
		}
	}

	
	private static Cursor cursor;

	private static StringBuilder vCard ;
	
	public static final String vfile = "contacts0.vcf";
	public static final String storagePath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + "vztransfer" + File.separator ;

	//The collection Async task is started from P2PSetupActivty
	private static ContentResolver contentResolver = P2PSetupActivity.activity.getContentResolver();

	public static void resetVariables(){
		cursor = null;
		vCard = null;
		contentResolver = null;
	}
	public static String getRawContactType(String contactId) 	{
		//LogUtil.d(TAG, "Contacts ID : " + contactId);
		String[] projection=new String[]{ContactsContract.RawContacts._ID,ContactsContract.RawContacts.ACCOUNT_TYPE,ContactsContract.RawContacts.ACCOUNT_NAME};
		String selection=ContactsContract.RawContacts.CONTACT_ID+"=?";
		String[] selectionArgs=new String[]{contactId};
		Cursor c=contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);
		String rawContactType = "";

		if ( null != c ) {

			int count = c.getCount();
			//LogUtil.d(TAG, "Raw contact count ="+count);
			if (c != null && count > 0) {
				if (c.moveToFirst()) {
					rawContactType = c.getString(c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
					//Log.d(TAG, "rawContactId: " +c.getInt(c.getColumnIndex(ContactsContract.RawContacts._ID)));
					//Log.d(TAG, "ACCOUNT_TYPE: " +c.getString(c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)));
					//Log.d(TAG, "ACCOUNT_NAME: " +c.getString(c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)) );
				}
			}

			//close the cursor before leaving
			c.close();
		} else {
			LogUtil.d(TAG, "Get Contacts Cursor is NULL");
		}
		c.close();
		return rawContactType;
	}

	public static void exportContacts() {

			try{
				LogUtil.d( TAG, "Exporting Contacts in Your Phone");
				vCard = new StringBuilder();

				cursor = contentResolver.query(ContactsQuery.CONTENT_URI, ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null, ContactsQuery.SORT_ORDER);
				MediaFileListGenerator.TOT_CONTACTS= 0;
				MediaFileListGenerator.TOT_CLOUD_CONTACTS= 0;
				LogUtil.d(TAG, "Creating exportContacts start");
				int contactCount = cursor.getCount();
				if( cursor != null && contactCount > 0 ) {
					cursor.moveToFirst();

					for(int i =0;i<contactCount;i++) {

						try{
							String contactId = String.valueOf(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
							String rawContactType = getRawContactType(contactId);

							if(rawContactType != null){
								rawContactType = rawContactType.toLowerCase();
							}
							if(i==0) {
								LogUtil.d(TAG, "raw contact type " + rawContactType);
							}
							if((rawContactType == null
									|| CTGlobal.getInstance().isCross()//Supporting cloud contacts for cross platform
									|| rawContactType.contains(VZTransferConstants.ACCOUNT_TYPE_PHONE)
									|| rawContactType.equals("com.motorola.contacts.preloaded" )
									|| rawContactType.equals("com.htc.android.pcsc" )
									|| rawContactType.equals("com.sonyericsson.localcontacts")
									|| rawContactType.equals("com.motorola.android.buacontactadapter" )
									|| rawContactType.equals("com.att.aab")
							)){
								boolean isVcardCreated = getVcard(cursor);
								if(isVcardCreated){
									MediaFileListGenerator.TOT_CONTACTS++;
								}else {
									LogUtil.d(TAG, "Vcard not found..");
								}
							}else {
								MediaFileListGenerator.TOT_CLOUD_CONTACTS++;
							}
						}catch (Exception e){
							e.getStackTrace();
						}
						cursor.moveToNext();
						if(!Utils.shouldCollect(VZTransferConstants.CONTACTS_STR)){
							break;
						}
					}
				} else {
					LogUtil.d( TAG, "No Contacts in Your Phone");
				}
				LogUtil.d(TAG, "Creating exportContacts end");
			}catch (Exception e1){
				e1.getStackTrace();
			}

			FileOutputStream mFileOutputStream = null;
			try {

				mFileOutputStream = new FileOutputStream( storagePath + vfile, false );
				mFileOutputStream.write( vCard.toString().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}finally{

				if(mFileOutputStream !=null)
				{
					try {
						mFileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						LogUtil.d( TAG, "No Contacts in Your Phone");
					}
				}

			}
	}

	private static boolean getVcard(Cursor cursor) {
		boolean isVcardCreated = false;
		String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
		AssetFileDescriptor fd;
		FileInputStream fis = null;		//fd.createInputStream();
		try {
			fd = contentResolver.openAssetFileDescriptor(uri, "r");

			fis = fd.createInputStream();

			int length = (int) fd.getDeclaredLength();
			if(length < 0){
				isVcardCreated = false;
			}else {
				byte[] buf = new byte[length];
				fis.read(buf);
				String vcardString = new String(buf);

				vCard.append(vcardString);
				isVcardCreated = true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if ( fis != null ) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isVcardCreated;
	}

}
