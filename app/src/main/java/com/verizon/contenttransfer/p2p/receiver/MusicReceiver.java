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

public class MusicReceiver {
	private static final String TAG = MusicReceiver.class.getName();

	private static String FILE_NAME = VZTransferConstants.CLIENT_MUSIC_LIST_FILE;		//"Music";


	public static void startProcessingMusicList(Socket iosServerSocket) throws IOException {
		CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(iosServerSocket.getInetAddress().getHostAddress());
		File client_music_file = new File( VZTransferConstants.VZTRANSFER_DIR, FILE_NAME );
		MediaFetchingService mediaService = new MediaFetchingService();

		try {
			BufferedReader musicReader = new BufferedReader (new FileReader( client_music_file ) );
			String musicFileName = null;
			while ( (musicFileName =  musicReader.readLine() ) != null ) {
				LogUtil.d(TAG, "Music to be received : " + musicFileName);

				//Send request to receive
			}

			JSONParser parser = new JSONParser();

			JSONArray musicListArray = (JSONArray) parser.parse( new FileReader( client_music_file ) );

			for (Object music : musicListArray) {
				JSONObject jsonObject  = (JSONObject) music;
				String musicFileSize = (String) jsonObject .get("Size");
				LogUtil.d(TAG, "before fetchMedia - File name :"+client_music_file+", Size of the File : " + musicFileSize);
				mediaService.fetchMedia(iosServerSocket, VZTransferConstants.MUSICS, jsonObject, musicFileSize, MediaFileNameGenerator.isDuplicate(VZTransferConstants.MUSICS, jsonObject));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
