package com.verizon.contenttransfer.p2p.receiver;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ContactsReceiver {

	private static final String TAG = ContactsReceiver.class.getName();


	public static void receiveContacts(Socket iosServerSocket) {
		String mediaType =  "";
		try {

			//Send the header to Sender
			String hostIp = iosServerSocket.getInetAddress().getHostAddress();
			OutputStream clientOutputStream = iosServerSocket.getOutputStream();
			clientOutputStream.write(VZTransferConstants.VCARD_REQUEST_HEADER.getBytes());
			//If Android send Line Break
			if (!CTGlobal.getInstance().isCross()) {
				clientOutputStream.write( VZTransferConstants.CRLF.getBytes() );
			}
			clientOutputStream.flush();

			InputStream socketStream = iosServerSocket.getInputStream();
			BufferedInputStream bis = null;
			bis = new BufferedInputStream(  socketStream );
			LogUtil.d(TAG, "Created input stream to read data..");
			LogUtil.d(TAG, "Server: connection done");

			String FILE_NAME = VZTransferConstants.CLIENT_CONTACTS_FILE;		//"contacts";
			FileOutputStream fileOutputStream = null; // Stream to write to destination

			File transferFolder = new File( VZTransferConstants.VZTRANSFER_DIR );
			transferFolder.mkdirs();

			File tmp_file = new File( VZTransferConstants.VZTRANSFER_DIR, FILE_NAME );

			try {
				fileOutputStream = new FileOutputStream( tmp_file ); // Create output stream
				LogUtil.d(TAG, "Waiting to read data...");
				LogUtil.d(TAG, "NUmber of bytes available to read : " + bis.available() );

				//byte[] headerArray = new byte[48];
				byte[] headerArray = new byte[37];

				socketStream.read(headerArray);

				String headerStr = new String (ByteBuffer.wrap( headerArray ).array() );
				LogUtil.d(TAG, "Header : " + headerStr );

				mediaType = headerStr.substring(17, 22);
				LogUtil.d(TAG, "Media Type : " + mediaType );

				String size = headerStr.substring(27, 37);
				int imageSize = 0;

				if ( !CTGlobal.getInstance().isCross()) {
					//2 additional bytes are CRLF bytes
					imageSize = Integer.parseInt(size) + 2;
				} else {
					imageSize = Integer.parseInt(size);
				}

				LogUtil.d(TAG, "Size to be received : " + imageSize );

				byte[] imageAr = new byte[ 2048 ];		// To hold file contents
				int dataRead = 0;						// How many bytes in buffer

				int readSize = 0;
				DataSpeedAnalyzer.setProgressbarProperties(CTGlobal.getInstance().getContentTransferContext().getString(R.string.contacts_str), VZTransferConstants.CLIENT_CONTACTS_FILE);
				if ( imageSize > 0 ) {
					CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(hostIp);
					ctAnalyticUtil.addFileTransferredCount(1);
					while ( (dataRead = socketStream.read(imageAr)) != -1 ) {

						fileOutputStream.write(imageAr, 0, dataRead);
						fileOutputStream.flush();

						readSize += dataRead;
						LogUtil.d(TAG, "Reading ..... --dataRead: " + dataRead + "  --readSize: " + readSize);
						Utils.updateTransferredBytes(ctAnalyticUtil, dataRead);
						if ( imageSize == readSize ) {
							LogUtil.d(TAG, "Last Data Receved : " + new String(imageAr) );
							break;
						}
						imageAr = new byte[ 2048 ];
					}

					LogUtil.d(TAG, "Reading complete...");
				} else {
					LogUtil.d(TAG, "There are no contacts to be received." );
				}
			} catch (IOException e) {
				LogUtil.d(TAG, e.getMessage());
			}
			// Always close the streams, even if exceptions were thrown
			finally {
				if ( fileOutputStream != null) {
					try {
						fileOutputStream.close();
						LogUtil.d(TAG, "Contacts File closed...");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			LogUtil.d(TAG, "NUmber of bytes available to read : " + bis.available() );

		} catch (Exception e) {
			LogUtil.d(TAG, e.getMessage());
		}
	}
}
