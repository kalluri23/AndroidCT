package com.verizon.contenttransfer.p2p.receiver;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileNameGenerator;
import com.verizon.contenttransfer.utils.SocketUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

//import android.util.Log;

public class PhotoReceiver {
	private static final String TAG = "PhotoReceiver";

	private static String FILE_NAME = VZTransferConstants.CLIENT_PHOTOS_LIST_FILE;

	public static void startProcessingPhotoList(Socket iosServerSocket) throws IOException {
		CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(iosServerSocket.getInetAddress().getHostAddress());
		File client_photo_file = new File( VZTransferConstants.VZTRANSFER_DIR, FILE_NAME );
		MediaFetchingService mediaService = new MediaFetchingService();
		//String photoFilePath = "";
		try {
			if ( LogUtil.DEBUG ) {
				BufferedReader photoReader = new BufferedReader(new FileReader(client_photo_file));
				String photoFileName = null;
				while ((photoFileName = photoReader.readLine()) != null) {
					LogUtil.d(TAG, "Photos to be received : " + photoFileName);

					//Send request to receive
				}
			}

			JSONParser parser = new JSONParser();
			
			LogUtil.d(TAG, "Parsing Photo List....");
			JSONArray photoListArray = (JSONArray) parser.parse( new FileReader( client_photo_file ) );

			for (Object photo : photoListArray) {
				JSONObject jsonObject  = (JSONObject) photo;

				String photoFileSize = (String) jsonObject .get("Size");
				LogUtil.d( TAG, "Size of the File : " +  photoFileSize );
				mediaService.fetchMedia(iosServerSocket, VZTransferConstants.PHOTOS, jsonObject, photoFileSize, MediaFileNameGenerator.isDuplicate(VZTransferConstants.PHOTOS, jsonObject) );
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException se){
			LogUtil.d(TAG,"Socket Exception.. "+se.getMessage());
			throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Process photo list:"+se.getMessage());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
