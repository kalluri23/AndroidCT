package com.verizon.contenttransfer.p2p.sender;

import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;

import java.util.Map;

//import android.util.Log;


public class MusicSender { //extends AsyncTask<Void, Void, String> {

	private static final String TAG = MusicSender.class.getName();

	public static void generateMusicsListFile() {
		
		LogUtil.d(TAG, "Starting Photos file list generation...");

		MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
		//export music file list
		Map<String, String> musicsList = fileListGenerator.getMusicsFileList(CTGlobal.getInstance().getContentTransferContext());
		LogUtil.d(TAG, "Music file created ...");
	}

/*	public static void sendMusicsFileList() { // unused method
		try {

			//String photoHeader = "VZCONTENTTRANSFERPHOTOSTART";

			//generatePhotosListFile();

			//File photosFile = new File (Constants.vzTransferDirectory + VZTransferConstants.PHOTOS_FILE);
			File musicsFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.MUSICS_FILE);
			InputStream photosStream = new FileInputStream( musicsFile );   //getContentResolver().openInputStream( selectedFileUri );
			out = P2PClientIos.iosClientSocket.getOutputStream();

			long photosFileSize= musicsFile.length();
			//send header now
			//PrintWriter writer = new PrintWriter(P2PClientIos.clientSocket.getOutputStream(),true);
			writer = new PrintWriter( out ,true );
			//writer.print( photoHeader );
			//LogUtil.d(TAG, "Writer error status : " + writer.checkError() );
			//writer.flush();

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

			//startProcessingPhotoList();

		} catch (FileNotFoundException e) {
			LogUtil.d(TAG, "Photos" + e.getMessage());
		} catch (IOException e) {
			LogUtil.d(TAG, "Failed to send photos file" + e.getMessage());
		}
	}


	private static void startProcessingMusicsList() {

		File music_file = new File( VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.MUSICS_FILE );
		MediaTransferService mediaService = new MediaTransferService();

		try {
			BufferedReader photoReader = new BufferedReader (new FileReader( music_file ) );
			String photoFileName = null;
			while ( (photoFileName = photoReader.readLine() ) != null ) {
				LogUtil.d(TAG, "Music to be sent : " + photoFileName );
			}

			JSONParser parser = new JSONParser();

			JSONArray musicListArray = (JSONArray) parser.parse( new FileReader( music_file ) );

			for (Object music : musicListArray) {
				JSONObject jsonObject  = (JSONObject) music;

				String photoFilePath = (String) jsonObject .get("Path");
				LogUtil.d( TAG, "File to be processed : " + photoFilePath );

				String photoFileSize = (String) jsonObject .get("Size");
				LogUtil.d( TAG, "Size of the File : " +  photoFileSize );

				//mediaService.transferMedia( "PHOTO", photoFilePath, photoFileSize );
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}*/

	/*
	@Override
	protected String doInBackground(Void... params) {
		LogUtil.d(TAG, "Generating Photos file ...");

		MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
		//export photos file list
		Map<String, String> photosList = fileListGenerator.getPhotosFileList( IOSSenderActivity.ctxt );
		LogUtil.d(TAG, "Photos file created ...");

		return null;
	}


	@Override
	protected void onPostExecute(String result) {
		try {
			//File photosFile = new File (Constants.vzTransferDirectory + VZTransferConstants.PHOTOS_FILE);
			File photosFile = new File ( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.PHOTOS_FILE);
			InputStream photosStream = new FileInputStream( photosFile );   //getContentResolver().openInputStream( selectedFileUri );
			out = P2PClientIos.iosClientSocket.getOutputStream();

			long photosFileSize= photosFile.length();
			//send header now
			//PrintWriter writer = new PrintWriter(P2PClientIos.clientSocket.getOutputStream(),true);
			writer = new PrintWriter( out ,true );
			writer.print( photoHeader );
			LogUtil.d(TAG, "Writer error status : " + writer.checkError() );
			writer.flush();

			out.flush();

			LogUtil.d(TAG, "Image Header sent");

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
			writer.print( filesize );
			writer.flush();

			byte[] buffer = new byte[4096];
			int len = photosStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				LogUtil.d(TAG, "Photos file transfer in progress... size " +  len );
				len = photosStream.read(buffer);
			}

			out.flush();

			LogUtil.d(TAG, "Photos File transfer complete");

			//startProcessingPhotoList();

		} catch (FileNotFoundException e) {
			LogUtil.d(TAG, "Photos" + e.getMessage());
		} catch (IOException e) {
			LogUtil.d(TAG, "Failed to send photos file" + e.getMessage());
		}
	}


	@Override
	protected void onPreExecute() {

	}*/

}
