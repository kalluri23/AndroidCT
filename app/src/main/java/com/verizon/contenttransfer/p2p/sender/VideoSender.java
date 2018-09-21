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

public class VideoSender {

	private static final String TAG = VideoSender.class.getName();
	private static final String videoLogHeader = "VZCONTENTTRANSFERVIDEOLOGAND";

	public static void generateVideoListFile() {

		LogUtil.d(TAG, "Starting Video file list generation...");
		MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
		//export photos file list
		Map<String, String> videoList = fileListGenerator.getVideosFileList( CTGlobal.getInstance().getContentTransferContext() );
		LogUtil.d(TAG, "Video filelist file created ...");
	}

	public static void sendVideosFileList(Socket iosClientSocket) {
		
		try {
			File videosFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.VIDEOS_FILE );
			InputStream videoStream = new FileInputStream( videosFile );   //getContentResolver().openInputStream( selectedFileUri );
			OutputStream out = iosClientSocket.getOutputStream();

			long videosFileSize= videosFile.length();
			PrintWriter writer = new PrintWriter( out ,true );
			//send header now
			out.flush();

			//LogUtil.d(TAG, "Image Header sent");

			String filesize = Long.toString( videosFileSize );
			LogUtil.d(TAG, "File size : " + filesize );

			int numDigits = filesize.length();
			LogUtil.d(TAG, "Numb of digists : " +  numDigits);

			if ( numDigits < 10 ) {
				for (int k=0; k < (10-numDigits); k++ ) {
					filesize ="0"+filesize;
				}
			}

			LogUtil.d(TAG, "File size : " + filesize );

			String videoHeader = videoLogHeader + filesize;
			out.write( videoHeader.getBytes() );
			out.flush();
			LogUtil.d(TAG, "Video Log Header sent.");

			byte[] buffer = new byte[4096];
			int len = videoStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				LogUtil.d(TAG, "Videos file List transfer in progress... size " +  len );
				len = videoStream.read(buffer);
			}

			out.flush();

			LogUtil.d(TAG, "Videos File list transfer complete");

		} catch (FileNotFoundException e) {
			LogUtil.d(TAG, "Videos file not found" + e.getMessage());
		} catch (IOException e) {
			LogUtil.d(TAG, "Failed to send Videos file" + e.getMessage());
		}
	}
}
