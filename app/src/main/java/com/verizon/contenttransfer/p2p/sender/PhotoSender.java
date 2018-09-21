package com.verizon.contenttransfer.p2p.sender;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

//import android.util.Log;

public class PhotoSender { //extends AsyncTask<Void, Void, String> {

	private static final String TAG = PhotoSender.class.getName();
	private static final String photoLogHeader = "VZCONTENTTRANSFERPHOTOLOGAND";

	public static void generatePhotosListFile() {
		
		LogUtil.d(TAG, "Starting Photos file list generation...");
		MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
		//export photos file list
		Map<String, String> photosList = fileListGenerator.getPhotosFileList( CTGlobal.getInstance().getContentTransferContext() );
		LogUtil.d(TAG, "Photos file created ...");

	}

	public static void sendPhotosFileList(Socket iosClientSocket) {
		try {
			File photosFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.PHOTOS_FILE);
			InputStream photosStream = new FileInputStream( photosFile );   //getContentResolver().openInputStream( selectedFileUri );
			OutputStream out = iosClientSocket.getOutputStream();

			long photosFileSize= photosFile.length();
			//send header now
			PrintWriter writer = new PrintWriter( out ,true );

			//If there is anything from Contacts Transfer flush it
			out.flush();

			String filesize = Long.toString( photosFileSize );
			LogUtil.d(TAG, "File size : " + filesize );

			int numDigits = filesize.length();
			LogUtil.d(TAG, "Numb of digists : " +  numDigits);

			if ( numDigits < 10 ) {
				for (int k=0; k < (10-numDigits); k++ ) {
					filesize ="0"+filesize;
				}
			}

			LogUtil.d(TAG, "File size : " + filesize );
			writer.print( photoLogHeader + filesize );
			writer.flush();
			LogUtil.d(TAG, "Photo Header sent");

			byte[] buffer = new byte[4096];
			int len = photosStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				LogUtil.d(TAG, "Photos file List transfer in progress... size " +  len );
				len = photosStream.read(buffer);
			}

			out.flush();

			LogUtil.d(TAG, "Photos File List transfer complete");

		} catch (FileNotFoundException e) {
			LogUtil.d(TAG, "Photos" + e.getMessage());
		} catch (IOException e) {
			LogUtil.d(TAG, "Failed to send photos file" + e.getMessage());
		}
	}
}
