package com.verizon.contenttransfer.p2p.sender;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.UtilsFromApacheLib.CTIOUtils;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

//import android.util.Log;

public class SendMetadata {

	private static final String TAG = SendMetadata.class.getName();

	public static void sendMetadata() {
		try {
			String photosList = "[]";
			ContentSelectionVO cdata = Utils.getContentSelection(VZTransferConstants.PHOTOS_STR);
			if(cdata.getContentflag()){
				File photosFile =  new File( VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.PHOTOS_FILE );
				FileInputStream photosInputStream = new FileInputStream( photosFile );
				photosList = CTIOUtils.toString( photosInputStream, "UTF-8");
				photosInputStream.close();
			}

			String videosList = "[]";
			cdata = Utils.getContentSelection(VZTransferConstants.VIDEOS_STR);
			if(cdata.getContentflag()) {
				File videosFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.VIDEOS_FILE);
				FileInputStream videosInputStream = new FileInputStream(videosFile);
				videosList = CTIOUtils.toString(videosInputStream, "UTF-8");
				videosInputStream.close();
			}

			String calendarList = "[]";
			cdata = Utils.getContentSelection(VZTransferConstants.CALENDAR_STR);
			if(cdata.getContentflag()) {
				File calendarFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CALENDAR_FILE);
				FileInputStream calendarInputStream = new FileInputStream(calendarFile);
				calendarList = CTIOUtils.toString(calendarInputStream, "UTF-8");
				calendarInputStream.close();
			}

			String appsList = "[]";
			if(VZTransferConstants.SUPPORT_APPS) {
				cdata = Utils.getContentSelection(VZTransferConstants.APPS_STR);
				if (cdata.getContentflag()) {
					File apkFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.APPS_FILE);
					FileInputStream apkInputStream = new FileInputStream(apkFile);
					appsList = CTIOUtils.toString(apkInputStream, "UTF-8");
					apkInputStream.close();
				}
			}
			String musicsList = "[]";
			String documentsList = "[]";

			if(Utils.isMediaSupported(VZTransferConstants.AUDIO_STR)) {
				cdata = Utils.getContentSelection(VZTransferConstants.AUDIO_STR);
				if (cdata.getContentflag()) {
					File musicsFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.MUSICS_FILE);
					FileInputStream musicsInputStream = new FileInputStream(musicsFile);
					musicsList = CTIOUtils.toString(musicsInputStream, "UTF-8");
					musicsInputStream.close();
				}
			}

				if(VZTransferConstants.SUPPORT_DOCS && Utils.isMediaSupported(VZTransferConstants.AUDIO_STR)) {
					cdata = Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR);
					if (cdata.getContentflag()) {
						File documentsFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.DOCUMENTS_FILE);
						FileInputStream documentsInputStream = new FileInputStream(documentsFile);
						documentsList = CTIOUtils.toString(documentsInputStream, "UTF-8");
						documentsInputStream.close();
					}
				}

			StringBuilder mediaMetaData = new StringBuilder();

			mediaMetaData.append("{");
			if(CTGlobal.getInstance().isDoingOneToMany()) {
				mediaMetaData.append("\"deviceCount\":");
				mediaMetaData.append(SocketUtil.getConnectedClients().size());
				mediaMetaData.append(",");
			}

			mediaMetaData.append("\"itemList\":{");

			cdata = Utils.getContentSelection(VZTransferConstants.CONTACTS_STR);

			if(cdata.getContentflag() && cdata.getContentsize() >0)
			{
				mediaMetaData.append("\"contacts\":{");
				mediaMetaData.append( "\"status\" : \"true\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalContactsBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(MediaFileListGenerator.TOT_CONTACTS);
				mediaMetaData.append("}");
			}
			else
			{
				mediaMetaData.append("\"contacts\":{");
				mediaMetaData.append( "\"status\" : \"false\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalContactsBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(MediaFileListGenerator.TOT_CONTACTS);
				mediaMetaData.append("}");
			}

			cdata = Utils.getContentSelection(VZTransferConstants.PHOTOS_STR);

			if(cdata.getContentflag() && cdata.getContentsize() >0)
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"photos\":{");
				mediaMetaData.append( "\"status\" : \"true\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalPhotosBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(photosList));
				mediaMetaData.append("}");
			}
			else
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"photos\":{");
				mediaMetaData.append( "\"status\" : \"false\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalPhotosBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(photosList));
				mediaMetaData.append("}");
			}
			if(VZTransferConstants.SUPPORT_APPS) {
				cdata = Utils.getContentSelection(VZTransferConstants.APPS_STR);
				if (cdata.getContentflag() && cdata.getContentsize() > 0) {
					mediaMetaData.append(",");
					mediaMetaData.append("\"apps\":{");
					mediaMetaData.append("\"status\" : \"true\", ");
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append(!CTGlobal.getInstance().isCross()? (CTSelectContentModel.getInstance().totalAppsBytes):(CTSelectContentModel.getInstance().totalAppIconsBytes));
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(getMediaCount(appsList));
					mediaMetaData.append("}");
				} else {
					mediaMetaData.append(",");
					mediaMetaData.append("\"apps\":{");
					mediaMetaData.append("\"status\" : \"false\", ");
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append(!CTGlobal.getInstance().isCross()? (CTSelectContentModel.getInstance().totalAppsBytes):(CTSelectContentModel.getInstance().totalAppIconsBytes));
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(getMediaCount(appsList));
					mediaMetaData.append("}");
				}
			}
			cdata = Utils.getContentSelection(VZTransferConstants.VIDEOS_STR);

			if(cdata.getContentflag() && cdata.getContentsize() >0)
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"videos\":{");
				mediaMetaData.append( "\"status\" : \"true\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalVideosBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(videosList));
				mediaMetaData.append("}");
			}
			else
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"videos\":{");
				mediaMetaData.append( "\"status\" : \"false\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalVideosBytes );
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(videosList));
				mediaMetaData.append("}");
			}

			cdata = Utils.getContentSelection(VZTransferConstants.CALENDAR_STR);

			if(cdata.getContentflag() && cdata.getContentsize() > 0)
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"calendar\":{");
				mediaMetaData.append( "\"status\" : \"true\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalCalendarBytes);
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(calendarList));
				mediaMetaData.append("}");
			}
			else
			{
				mediaMetaData.append(",");
				mediaMetaData.append("\"calendar\":{");
				mediaMetaData.append( "\"status\" : \"false\", " );
				mediaMetaData.append("\"totalSize\":");
				mediaMetaData.append( CTSelectContentModel.getInstance().totalCalendarBytes);
				mediaMetaData.append(",");
				mediaMetaData.append("\"totalCount\":");
				mediaMetaData.append(getMediaCount(calendarList));
				mediaMetaData.append("}");
			}

			if(!CTGlobal.getInstance().isCross())
			{
				cdata = Utils.getContentSelection(VZTransferConstants.AUDIO_STR);
				if(cdata.getContentflag() && cdata.getContentsize() >0)
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"musics\":{");
					mediaMetaData.append( "\"status\" : \"true\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append( CTSelectContentModel.getInstance().totalMusicBytes );
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(getMediaCount(musicsList));
					mediaMetaData.append("}");
				}
				else
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"musics\":{");
					mediaMetaData.append( "\"status\" : \"false\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append( CTSelectContentModel.getInstance().totalMusicBytes );
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(getMediaCount(musicsList));
					mediaMetaData.append("}");
				}

				cdata = Utils.getContentSelection(VZTransferConstants.CALLLOG_STR);
				if(cdata.getContentflag() && cdata.getContentsize() > 0)
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"calllogs\":{");
					mediaMetaData.append( "\"status\" : \"true\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append( CTSelectContentModel.getInstance().totalCalllogsBytes);
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(MediaFileListGenerator.TOT_CALLLOGS);
					mediaMetaData.append("}");
				}
				else
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"calllogs\":{");
					mediaMetaData.append( "\"status\" : \"false\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append(CTSelectContentModel.getInstance().totalCalllogsBytes);
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append(MediaFileListGenerator.TOT_CALLLOGS);
					mediaMetaData.append("}");
				}

				cdata = Utils.getContentSelection(VZTransferConstants.SMS_STR);
				if(cdata.getContentflag() && cdata.getContentsize() > 0)
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"sms\":{");
					mediaMetaData.append( "\"status\" : \"true\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append( CTSelectContentModel.getInstance().totalSMSBytes);
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append( MediaFileListGenerator.TOT_MESSAGES);
					mediaMetaData.append("}");
				}
				else
				{
					mediaMetaData.append(",");
					mediaMetaData.append("\"sms\":{");
					mediaMetaData.append( "\"status\" : \"false\", " );
					mediaMetaData.append("\"totalSize\":");
					mediaMetaData.append( CTSelectContentModel.getInstance().totalSMSBytes);
					mediaMetaData.append(",");
					mediaMetaData.append("\"totalCount\":");
					mediaMetaData.append( MediaFileListGenerator.TOT_MESSAGES);
					mediaMetaData.append("}");
				}
				if(VZTransferConstants.SUPPORT_DOCS) {
					cdata = Utils.getContentSelection(VZTransferConstants.DOCUMENTS_STR);

					if (cdata.getContentflag() && cdata.getContentsize() > 0) {
						mediaMetaData.append(",");
						mediaMetaData.append("\"documents\":{");
						mediaMetaData.append("\"status\" : \"true\", ");
						mediaMetaData.append("\"totalSize\":");
						mediaMetaData.append(CTSelectContentModel.getInstance().totalDocumentsBytes);
						mediaMetaData.append(",");
						mediaMetaData.append("\"totalCount\":");
						mediaMetaData.append(MediaFileListGenerator.TOT_DOCS);
						mediaMetaData.append("}");

					} else {
						mediaMetaData.append(",");
						mediaMetaData.append("\"documents\":{");
						mediaMetaData.append("\"status\" : \"false\", ");
						mediaMetaData.append("\"totalSize\":");
						mediaMetaData.append(CTSelectContentModel.getInstance().totalDocumentsBytes);
						mediaMetaData.append(",");
						mediaMetaData.append("\"totalCount\":");
						mediaMetaData.append(MediaFileListGenerator.TOT_DOCS);
						mediaMetaData.append("}");
					}
				}
			}

			mediaMetaData.append("}");

			mediaMetaData.append(",");
			mediaMetaData.append("\"photoFileList\":");
			mediaMetaData.append( photosList );

			mediaMetaData.append(",");
			mediaMetaData.append("\"videoFileList\":");
			mediaMetaData.append( videosList );

			mediaMetaData.append(",");
			mediaMetaData.append("\"calendarFileList\":");
			mediaMetaData.append(calendarList);

			if(VZTransferConstants.SUPPORT_APPS) {
				mediaMetaData.append(",");
				mediaMetaData.append("\"appsFileList\":");
				mediaMetaData.append(appsList);
			}
			if(Utils.isSupportMusic()){
				mediaMetaData.append(",");
				mediaMetaData.append("\"musicFileList\":");
				mediaMetaData.append( musicsList );
			}
			if(VZTransferConstants.SUPPORT_DOCS && Utils.isMediaSupported(VZTransferConstants.DOCUMENTS_STR)) {
				mediaMetaData.append(",");
				mediaMetaData.append("\"documentFileList\":");
				mediaMetaData.append(documentsList);
			}

			if (!CTGlobal.getInstance().isCross()) { // add any data need to share between device after connection established.
				mediaMetaData.append(",");
				mediaMetaData.append("\"data\":");
				mediaMetaData.append(additionalMetaData());
			}
			mediaMetaData.append("}");

			LogUtil.d(TAG, "Metadata String : " + mediaMetaData.toString() );

			long payloadSize = mediaMetaData.toString().length();

			LogUtil.d(TAG, "payloadSize : " + payloadSize);

			String dataSize = Long.toString( payloadSize );
			LogUtil.d(TAG, "dataSize : " + dataSize);

			for ( int i=0; dataSize.length() < 10 ; i++) {
				dataSize = "0" + dataSize;
			}

			LogUtil.d(TAG, "Size of payload : " + dataSize );
			String requestString = VZTransferConstants.METADATA_TRANSFER_REQUEST_HEADER + dataSize + mediaMetaData.toString();
			LogUtil.d(TAG, "Request String : " + requestString );
			CTGlobal.getInstance().setVztransferStarted(true);

			LogUtil.d(TAG, "Preparing to send Meta data... Total Client count :"+SocketUtil.getConnectedClients().size());

			int counter = 0;
			for(int clientCount = 0; clientCount< SocketUtil.getConnectedClients().size();clientCount++){
				OutputStream out = null;

				while ( (SocketUtil.getConnectedClients().get(clientCount) != null && ! SocketUtil.getConnectedClients().get(clientCount).isConnected()) && counter < 20 ){
					LogUtil.d(TAG, "Waiting for client socket to make connection....Ip :" + SocketUtil.getConnectedClients().get(clientCount).getInetAddress().getHostAddress());
					counter ++;
					try {
						Thread.sleep(2000);
					}catch (Exception e){
						LogUtil.e(TAG,"P2PClientIos.iosClientSocket.isConnected() ="+e.getMessage());
					}
				}
				if(SocketUtil.getConnectedClients().get(clientCount).isConnected()) {
					LogUtil.d(TAG, "Finally was able to make client socket connection....");
				}else {
					LogUtil.d(TAG, "socket connection not successful ....SocketUtil.get Clients().get("+clientCount+").isConnected() ="+SocketUtil.getConnectedClients().get(clientCount).isConnected());
				}

				out = SocketUtil.getConnectedClients().get(clientCount).getOutputStream();
				out.write( requestString.getBytes() );
				out.flush();
				LogUtil.d(TAG, "Meta data send successfully...SocketUtil.get Clients().get("+clientCount+").isConnected() ="+SocketUtil.getConnectedClients().get(clientCount).isConnected());
			}
			LogUtil.d(TAG, "All Meta data send successfully.");

			DataSpeedAnalyzer.setStartTime(System.currentTimeMillis());


		} catch (IOException e) {
			LogUtil.d(TAG,e.getMessage());
			e.printStackTrace();
		}catch (NullPointerException e){
			LogUtil.d(TAG,e.getMessage());
			e.printStackTrace();
		}
	}

	private static String additionalMetaData(){
		StringBuilder additionalData = new StringBuilder();
		additionalData.append("{");
		// adding connection timed out flag.
		additionalData.append("\"");
		additionalData.append(VZTransferConstants.CONNECTION_TIMED_OUT_DURING_TRANSFER);
		additionalData.append("\"");
		additionalData.append(":");
		additionalData.append("\"");
		additionalData.append(Utils.isConnectionTimedOutRecorded());
		additionalData.append("\"");
		// end of adding connection timed out flag.
		additionalData.append("}");

		return additionalData.toString();
	}
	private static String getMediaCount(String dataJsonStr) {
		JSONParser parser = new JSONParser();

		try {
			if(dataJsonStr.length()>0) {
				JSONArray metaData = (JSONArray) parser.parse(dataJsonStr);
				return String.valueOf(metaData.size());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "0";
	}
}
