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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

//import android.util.Log;

public class VideoReceiver {
	private static final String TAG = VideoReceiver.class.getName();

	private static String FILE_NAME = VZTransferConstants.CLIENT_VIDEOS_LIST_FILE;		//"Photos";

	public static void startProcessingVideoList(Socket iosServerSocket) throws IOException {
		CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(iosServerSocket.getInetAddress().getHostAddress());
		File client_video_file = new File( VZTransferConstants.VZTRANSFER_DIR, FILE_NAME );
		MediaFetchingService mediaService = new MediaFetchingService();

		try {

			JSONParser parser = new JSONParser();

			JSONArray videoListArray = (JSONArray) parser.parse( new FileReader( client_video_file ) );

			for (Object photo : videoListArray) {
				JSONObject jsonObject  = (JSONObject) photo;

				String videoFileSize = (String) jsonObject .get("Size");
				LogUtil.d( TAG, "Size of the File : " +  videoFileSize );


				long longVideoFileSize = Long.parseLong(videoFileSize);
				LogUtil.d(TAG, "video size = "+longVideoFileSize);
				mediaService.fetchMedia(iosServerSocket, VZTransferConstants.VIDEOS, jsonObject, videoFileSize , MediaFileNameGenerator.isDuplicate(VZTransferConstants.VIDEOS, jsonObject));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException se){
			LogUtil.d(TAG,"Socket Exception.. "+se.getMessage());
			throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Process video list:"+se.getMessage());
		}catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
