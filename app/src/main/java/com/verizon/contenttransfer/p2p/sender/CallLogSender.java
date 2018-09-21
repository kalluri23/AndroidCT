package com.verizon.contenttransfer.p2p.sender;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

//import android.util.Log;

public class CallLogSender {

	private static final String TAG = CallLogSender.class.getName();

	public static void generateCallLogsFile() {
		
		LogUtil.d(TAG, "Starting CallLog file list generation...");

				MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
				//export photos file list
				Map<String, String> musicsList = fileListGenerator.getCallLogsFileList(CTGlobal.getInstance().getContentTransferContext());
				LogUtil.d(TAG, "CallLog file created ...");
	}

	public static void sendCalllogs(Socket iosClientSocket) {
			try {
				String hostIp = iosClientSocket.getInetAddress().getHostAddress();
				String CTS_HEADER = VZTransferConstants.CALLLOG_TRANSFER_REQUEST_HEADER;
				OutputStream out = iosClientSocket.getOutputStream();

				File calllogsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CALLLOGS_FILE );
				if ( calllogsFile.exists() ) {
					DataSpeedAnalyzer.setProgressbarProperties(CTGlobal.getInstance().getContentTransferContext().getString(R.string.callLogs_str), VZTransferConstants.CALLLOGS_FILE);
					InputStream calllogsStream = new FileInputStream( calllogsFile );   //getContentResolver().openInputStream( selectedFileUri );

					long calllogsFileSize= calllogsFile.length();

					String filesize = Long.toString( calllogsFileSize );
					LogUtil.d(TAG, "Call log File size : " + filesize );

					int numDigits = filesize.length();
					LogUtil.d(TAG, "Numb of digists : " +  numDigits);

					if ( numDigits < 10 ) {
						for (int k=0; k < (10-numDigits); k++ ) {
							filesize ="0"+filesize;
						}
					}

					LogUtil.d(TAG, "File size : " + filesize );
					String calllogsHeader = CTS_HEADER+filesize;
					LogUtil.d(TAG,"calllogsHeader ="+calllogsHeader);
					out.write( calllogsHeader.getBytes() );
					if(!CTGlobal.getInstance().isCross()){
						out.write(VZTransferConstants.CRLF.getBytes());
					}
					out.flush();

					byte[] buffer = new byte[1024];
					int len = calllogsStream.read(buffer);
					long totRead = 0;
					CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(hostIp);
					ctAnalyticUtil.addFileTransferredCount(1);
					while (len != -1) {
						out.write(buffer, 0, len);
						totRead += len;
						Utils.updateTransferredBytes(ctAnalyticUtil, len);
						len = calllogsStream.read(buffer);
					}
					out.flush();
					LogUtil.d(TAG, "Calllogs File transfer complete");
				} else {
					LogUtil.e(TAG, "There is no Calllogs file to transfer..");
					String calllogsHeader = CTS_HEADER+"0000000000";
					out.write( calllogsHeader.getBytes() );
					out.flush();
				}

			} catch (FileNotFoundException e) {
				LogUtil.e(TAG, "calllogs file not found" + e.getMessage());
			} catch (IOException e) {
				LogUtil.e(TAG, "Failed to send calllogs file" + e.getMessage());
			}
	}
}
