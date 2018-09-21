package com.verizon.contenttransfer.p2p.sender;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



public class SMSSender {

	private static final String TAG = SMSSender.class.getName();

	public static void generateSMSMMSFileList() {
		LogUtil.d(TAG, "Starting SMS file list generation...");

		MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
		// Write SMS / MMS to file.
		//Create SMS File.
		MediaFileListGenerator.appendToFile(null, VZTransferConstants.SMS, false,false);
		// new message code.
		fileListGenerator.getMessageFileList(CTGlobal.getInstance().getContentTransferContext());

	}

	public static void sendSMS(Socket iosClientSocket) {
		try {
			String hostIp = iosClientSocket.getInetAddress().getHostAddress();
			String CTS_HEADER = VZTransferConstants.SMS_TRANSFER_REQUEST_HEADER;
			OutputStream out = iosClientSocket.getOutputStream();
			File smsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.SMS_FILE );
			if ( smsFile.exists() ) {
				//DataSpeedAnalyzer.setProgressbarProperties(VZTransferConstants.SMS_STR,VZTransferConstants.SMS_FILE);
				DataSpeedAnalyzer.setProgressbarProperties(CTGlobal.getInstance().getContentTransferContext().getString(R.string.messages_str),VZTransferConstants.SMS_FILE);
				InputStream smsStream = new FileInputStream( smsFile );   //getContentResolver().openInputStream( selectedFileUri );

				long smsFileSize= smsFile.length();

				String filesize = Long.toString( smsFileSize );
				LogUtil.d(TAG, "File size : " + filesize );

				int numDigits = filesize.length();
				LogUtil.d(TAG, "Numb of digists : " +  numDigits);

				if ( numDigits < 10 ) {
					for (int k=0; k < (10-numDigits); k++ ) {
						filesize ="0"+filesize;
					}
				}

				LogUtil.d(TAG, "File size : " + filesize );
				String smsHeader = CTS_HEADER+filesize;

				out.write( smsHeader.getBytes() );
				if(!CTGlobal.getInstance().isCross()){
					out.write(VZTransferConstants.CRLF.getBytes());
				}
				out.flush();

				byte[] buffer = new byte[1024];
				int len = smsStream.read(buffer);

				CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(hostIp);
				ctAnalyticUtil.addFileTransferredCount(1);
				while (len != -1) {
					out.write(buffer, 0, len);
					Utils.updateTransferredBytes(ctAnalyticUtil, len);
					len = smsStream.read(buffer);
				}
				out.flush();
				LogUtil.d(TAG, "SMS File transfer complete");
			} else {
				LogUtil.e(TAG, "There is no SMS file to transfer..");
				String smsHeader = CTS_HEADER+"0000000000";
				out.write( smsHeader.getBytes() );
				out.flush();
			}
		} catch (FileNotFoundException e) {
			LogUtil.e(TAG, "sms file not found" + e.getMessage());
		} catch (IOException e) {
			LogUtil.e(TAG, "Failed to send sms file" + e.getMessage());
		}
	}
	public static int fetchSMSFromDevice(Context ctxt, String msgId , int totMsgCount, int currentMsgCount) {
		// reading all data in descending order according to DATE
		LogUtil.d(TAG, "==============================Scan SMS() for id :"+msgId+"==============================");

		Uri uri = Uri.parse("content://sms");
		String[] projection = {"*"};
		String selection = "_id = "+msgId;
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
				//MessageUtil.displayCursorProperties(cursor,"test sms properties :");
				JSONObject smsmsg = new JSONObject();
				String id = cursor.getString(cursor.getColumnIndex("_id"));
				String thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
				String address = cursor.getString(cursor.getColumnIndex("address"));
				String person = cursor.getString(cursor.getColumnIndex("person"));
				String date = cursor.getString(cursor.getColumnIndex("date"));
				String body = MessageUtil.stripHtml(cursor.getString(cursor.getColumnIndex("body")));
				String type = cursor.getString(cursor.getColumnIndex("type"));
				String read = cursor.getString(cursor.getColumnIndex("read"));

				try {
					smsmsg.put("id", id);
					smsmsg.put("thread_id", thread_id);
					smsmsg.put("address", address);

					if (person == null) {
						person = "";
					}
					smsmsg.put("person", person);
					smsmsg.put("date", date);
					smsmsg.put("body", body);
					smsmsg.put("type", type);
					smsmsg.put("read", read);

					boolean appendComma = (currentMsgCount < totMsgCount);

					MediaFileListGenerator.appendToFile(smsmsg, VZTransferConstants.SMS, false,appendComma);

				} catch (Exception e) {
					e.printStackTrace();
				}
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
}
