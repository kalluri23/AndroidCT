package com.verizon.contenttransfer.p2p.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.json.simple.JSONObject;

public class MediaTransferStateVO implements Parcelable {

	private static final String TAG = MediaTransferStateVO.class.getName();

	private String contactsState;
	private String photosState;
	private String appsState;
	private String videosState;
	private String musicState;
	private String callLogsState;
	private String smsState;
	private String calendarState;
	private String calendarSize;
	private String wifiSettingState;
	private String contactsSize;
	private String photosSize;
	private String appsSize;
	private String videosSize;
	private String musicSize;
	private String callLogsSize;
	private String smsSize;
	private String wifiSettingSize;
	private String documentsState;
	private String passwordsState;
	private String documentsSize;
	private String passwordsSize;
	private String contactsCount;
	private String photosCount;
	private String appsCount;
	private String videosCount;
	private String musicCount;
	private String documentsCount;
	private String passwordsCount;
	private String calllogsCount;
	private String smsCount;
	private String calendarCount;

	public MediaTransferStateVO(
			String _contactsState, String _contactsSize, String _contactsCount,
			String _photosState, String _photosSize, String _photoCount,
			String _appsState, String _appsSize, String _appCount,
			String _videosState, String _videosSize, String _videoCount,
			String _musicState, String _musicSize, String _musicCount,
			String _callLogsState, String _callLogsSize, String _calllogCount,
			String _smsState, String _smsSize, String _smsCount,
			String _wifiSettingState, String _wifiSettingSize,
			String _calendarState,String _calendarSize, String _calendarCount,
			String _documentsState, String _documentsSize, String _documentCount,
			String _passwordsState, String _passwordsSize, String _passwordsCount) {

		contactsState = _contactsState;
		contactsSize = _contactsSize;
		photosState = _photosState;
		photosSize = _photosSize;
		appsState=_appsState;
		appsSize=_appsSize;
		videosState = _videosState;
		videosSize= _videosSize;
		musicState = _musicState;
		musicSize = _musicSize;
		callLogsState = _callLogsState;
		callLogsSize = _callLogsSize;
		smsState= _smsState;
		smsSize= _smsSize;
		wifiSettingState = _wifiSettingState;
		wifiSettingSize = _wifiSettingSize;
		calendarState = _calendarState;
		calendarSize = _calendarSize;
		documentsState = _documentsState;
		documentsSize = _documentsSize;

		passwordsState = _passwordsState;
		passwordsSize = _passwordsSize;
		contactsCount = _contactsCount;
		photosCount = _photoCount;
		appsCount =_appCount;
		videosCount = _videoCount;
		musicCount = _musicCount;
		documentsCount = _documentCount;
		passwordsCount = _passwordsCount;
		calllogsCount = _calllogCount;
		smsCount = _smsCount;
		calendarCount = _calendarCount;
	}

	public MediaTransferStateVO(JSONObject mediaState) {
		init ( mediaState );
	}
	private JSONObject localJsonObj = null;
	public void init( JSONObject mediaState ) {
		LogUtil.d(TAG, "Media State = " + mediaState);

		localJsonObj = (JSONObject) mediaState.get( VZTransferConstants.CONTACTS.toLowerCase() );
		LogUtil.d(TAG,"localJsonObj ="+localJsonObj);
		if(null != localJsonObj) {
			contactsState = localJsonObj.get(VZTransferConstants.STATUS).toString();
			contactsSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
			contactsCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
		}

		localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.PHOTOS.toLowerCase());
		if(null != localJsonObj) {
			photosState = localJsonObj.get(VZTransferConstants.STATUS).toString();
			photosSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
			photosCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
			LogUtil.d(TAG, "photosSize =" + photosSize);
		}
		LogUtil.d(TAG,"photosState ="+photosState);//{"status":"true","totalSize":465273501}

		if(VZTransferConstants.SUPPORT_APPS) {
			localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.APPS.toLowerCase());
			if (null != localJsonObj) {
				appsState = localJsonObj.get(VZTransferConstants.STATUS).toString();
				appsSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
				appsCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
				LogUtil.d(TAG, "appsSize =" + appsSize+"  app count :"+appsCount+"  apps state :"+appsState);
			}
		}


		localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.VIDEOS.toLowerCase());
		if(null != localJsonObj) {
			videosState = localJsonObj.get(VZTransferConstants.STATUS).toString();
			videosSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
			videosCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
			LogUtil.d(TAG, "videosSize =" + videosSize);
		}

		localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.CALENDAR.toLowerCase());
		if(null != localJsonObj) {
			calendarState = localJsonObj.get(VZTransferConstants.STATUS).toString();
			calendarSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
			calendarCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
			LogUtil.d(TAG, "calendarSize =" + calendarSize);
		}

		LogUtil.d(TAG, "calendarState =" + calendarState);

		if(Utils.isSupportMusic()){
			localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.MUSICS.toLowerCase());
			if(null != localJsonObj) {
				musicState = localJsonObj.get(VZTransferConstants.STATUS).toString();
				musicSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
				musicCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
				LogUtil.d(TAG, "musicSize =" + musicSize);//contactsState ={"status":"true","totalSize":0}
			}
			LogUtil.d(TAG, "musicState =" + musicState);
		}

		if( !CTGlobal.getInstance().isCross() ) {
				localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.CALLLOG.toLowerCase());
				if (null != localJsonObj) {
					callLogsState = localJsonObj.get(VZTransferConstants.STATUS).toString();
					callLogsSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
					calllogsCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
					LogUtil.d(TAG, "callLogsSize =" + callLogsSize);//contactsState ={"status":"true","totalSize":0}
				}
				LogUtil.d(TAG, "callLogsState =" + callLogsState);
			localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.SMS.toLowerCase());
			if (null != localJsonObj) {
				smsState = localJsonObj.get(VZTransferConstants.STATUS).toString();
				smsSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
				smsCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
				LogUtil.d(TAG, "smsSize =" + smsSize);//contactsState ={"status":"true","totalSize":0}
			}
			LogUtil.d(TAG, "smsState =" + smsState);
			if(VZTransferConstants.SUPPORT_DOCS) {
				localJsonObj = (JSONObject) mediaState.get(VZTransferConstants.DOCUMENTS.toLowerCase());
				if (null != localJsonObj) {
					documentsState = localJsonObj.get(VZTransferConstants.STATUS).toString();
					documentsSize = localJsonObj.get(VZTransferConstants.TOTAL_SIZE).toString();
					documentsCount = localJsonObj.get(VZTransferConstants.TOTAL_COUNT).toString();
					LogUtil.d(TAG, "documentsSize =" + documentsSize);
				}
				LogUtil.d(TAG, "documentsState =" + documentsState);
			}
		}
	}

	public String getContactsState() {
		return (null ==contactsState ? "false" : contactsState);
	}
	public void setContactsState(String contactsState) { this.contactsState = contactsState; }
	public String getContactsSize() {
		return contactsSize;
	}
	public void setContactsSize(String contactsSize) {
		this.contactsSize = contactsSize;
	}

	public String getPhotosState() { return (null ==photosState ? "false" : photosState); }
	public void setPhotosState(String photosState) {
		this.photosState = photosState;
	}
	public String getPhotosSize() {
		return photosSize;
	}
	public void setPhotosSize(String photosSize) {
		this.photosSize = photosSize;
	}
	public String getAppsState() { return (null ==appsState ? "false" : appsState); }
	public void setAppsState(String appsState) {
		this.appsState = appsState;
	}
	public String getAppsSize() {
		return appsSize;
	}
	public void setAppsSize(String appsSize) {
		this.appsSize = appsSize;
	}


	public String getVideosState() {
		return (null ==videosState ? "false" : videosState);
	}
	public void setVideosState(String videosState) {
		this.videosState = videosState;
	}
	public String getVideosSize() {
		return videosSize;
	}
	public void setVideosSize(String videosSize) {
		this.videosSize = videosSize;
	}

	public String getMusicsState() {
		return (null ==musicState ? "false" : musicState);
	}
	public void setMusicsState(String musicsState) {
		this.musicState = musicsState;
	}
	public String getMusicsSize() {
		return musicSize;
	}
	public void setMusicsSize(String musicsSize) {
		this.musicSize = musicsSize;
	}

	public String getCallLogsState() {
		return (null ==callLogsState ? "false" : callLogsState);
	}
	public void setCallLogsState(String callLogsState) {
		this.callLogsState = callLogsState;
	}
	public String getCallLogsSize() {
		return callLogsSize;
	}
	public void setCallLogsSize(String callLogsSize) {
		this.callLogsSize = callLogsSize;
	}

	public String getWifiSettingState() {
		return (null ==wifiSettingState ? "false" : wifiSettingState);
	}
	public void setWifiSettingState(String wifiSetting) {
		this.wifiSettingState = wifiSetting;
	}
	public String getWifiSettingSize() {
		return wifiSettingSize;
	}
	public void setWifiSettingSize(String wifiSetting) {
		this.wifiSettingSize = wifiSetting;
	}

	public String getSmsState() {
		return (null ==smsState ? "false" : smsState);
	}
	public String getSmsSize() {
		return smsSize;
	}
	public void setSmsSize(String smsSize) {
		this.smsSize = smsSize;
	}

	public String getCalendarState() { return (null ==calendarState ? "false" : calendarState);}
	public void setCalendarState(String calendarState) {
		this.calendarState = calendarState;
	}
	public String getCalendarSize() {
		return calendarSize;
	}
	public void setCalendarSize(String calendarSize) {
		this.calendarSize = calendarSize;
	}

	public String getDocumentsState() { return (null ==documentsState ? "false" : documentsState); }
	public void setDocumentsState(String documentsState) { this.documentsState = documentsState; }
	public String getDocumentsSize() { return documentsSize; }
	public void setDocumentsSize(String documentsSizee) { this.documentsSize = documentsSize; }

	public String getPasswordsState() { return (null ==passwordsState ? "false" : passwordsState); }
	public void setPasswordsState(String passwordsState) { this.passwordsState = passwordsState; }
	public String getPasswordsSize() { return passwordsSize; }
	public void setPasswordsSize(String passwordsSizee) { this.passwordsSize = passwordsSize; }
	public String getContactsCount() {
		return contactsCount;
	}

	public void setContactsCount(String contactsCount) {
		this.contactsCount = contactsCount;
	}

	public String getPhotosCount() {
		return photosCount;
	}

	public void setPhotosCount(String photosCount) {
		this.photosCount = photosCount;
	}
	public String getAppsCount() {
		return appsCount;
	}

	public void setAppsCount(String appsCount) {
		this.appsCount = appsCount;
	}

	public String getVideosCount() {
		return videosCount;
	}

	public void setVideosCount(String videosCount) {
		this.videosCount = videosCount;
	}

	public String getMusicCount() {
		return musicCount;
	}

	public void setMusicCount(String musicCount) {
		this.musicCount = musicCount;
	}

	public String getDocumentsCount() {
		return documentsCount;
	}

	public void setDocumentsCount(String documentsCount) {
		this.documentsCount = documentsCount;
	}

	public String getPasswordsCount() {
		return passwordsCount;
	}

	public void setPasswordsCount(String passwordsCount) {
		this.passwordsCount = passwordsCount;
	}

	public String getCalllogsCount() {
		return calllogsCount;
	}

	public void setCalllogsCount(String calllogsCount) {
		this.calllogsCount = calllogsCount;
	}

	public String getSmsCount() {
		return smsCount;
	}

	public void setSmsCount(String smsCount) {
		this.smsCount = smsCount;
	}

	public String getCalendarCount() {
		return calendarCount;
	}

	public void setCalendarCount(String calendarCount) {
		this.calendarCount = calendarCount;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append( "\"");
		sb.append( VZTransferConstants.CONTACTS.toLowerCase() );
		sb.append( "\":\"" + getContactsState() );
		sb.append( "\",\"");
		sb.append( VZTransferConstants.PHOTOS.toLowerCase() );
		sb.append( "\":\"" + getPhotosState() );
		if(VZTransferConstants.SUPPORT_APPS) {
			sb.append("\",\"");
			sb.append(VZTransferConstants.APPS.toLowerCase());
			sb.append("\":\"" + getAppsState());
		}
		if(Utils.isSupportMusic()) {
			sb.append("\",\"");
			sb.append(VZTransferConstants.MUSICS.toLowerCase());
			sb.append("\":\"" + getMusicsState());
		}
		sb.append( "\",\"");
		sb.append( VZTransferConstants.VIDEOS.toLowerCase() );
		sb.append( "\":\"" + getVideosState() );
		sb.append( "\",\"");
		sb.append( VZTransferConstants.CALENDAR.toLowerCase() );
		sb.append( "\":\"" + getCalendarState() );
		if(VZTransferConstants.SUPPORT_DOCS) {
			sb.append("\",\"");
			sb.append(VZTransferConstants.DOCUMENTS.toLowerCase());
			sb.append("\":\"" + getDocumentsState());
		}

		sb.append( "\"");
		return sb.toString();
	}

	public String toJson() {

		StringBuilder sb = new StringBuilder();

		sb.append( "{");
		sb.append(  toString() );
		sb.append( "}");

		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		// create a bundle for the key value pairs
		Bundle bundle = new Bundle();

		// insert the key value pairs to the bundle
		bundle.putString ( "contactsState", getContactsState());
		bundle.putString ( "contactsSize", getContactsSize());
		bundle.putString ( "contactsCount", getContactsCount());

		bundle.putString( "photosState", getPhotosState() );
		bundle.putString( "photosSize", getPhotosSize() );
		bundle.putString( "photosCount", getPhotosCount() );
		if(VZTransferConstants.SUPPORT_APPS) {
			bundle.putString("appsState", getAppsState());
			bundle.putString("appsSize", getAppsSize());
			bundle.putString("appsCount", getAppsCount());
		}
		bundle.putString( "videosState" , getVideosState() );
		bundle.putString( "videosSize" , getVideosSize() );
		bundle.putString( "videosCount", getVideosCount() );

		bundle.putString( "musicState" , getMusicsState() );
		bundle.putString( "musicSize" , getMusicsSize() );
		bundle.putString( "musicCount" , getMusicCount() );

		bundle.putString( "callLogsState" , getCallLogsState() );
		bundle.putString( "callLogsSize" , getCallLogsSize() );
		bundle.putString( "callLogsCount" , getCalllogsCount() );

		bundle.putString( "smsState" , getSmsState() );
		bundle.putString( "smsSize" , getSmsSize() );
		bundle.putString( "smsCount" , getSmsCount() );

		bundle.putString( "wifiSettingState" , getWifiSettingState() );
		bundle.putString( "wifiSettingSize" , getWifiSettingSize() );

		bundle.putString( "calendarState" , getCalendarState() );
		bundle.putString( "calendarSize" , getCalendarSize() );
		bundle.putString( "calendarCount" , getCalendarCount() );

		if(VZTransferConstants.SUPPORT_DOCS) {
			bundle.putString("documentState", getDocumentsState());
			bundle.putString("documentSize", getDocumentsSize());
			bundle.putString("documentCount", getDocumentsCount());
		}

		if(VZTransferConstants.SUPPORT_PASSWORDS_DATABASE) {
			bundle.putString("passwordsState", getPasswordsState());
			bundle.putString("passwordsSize", getPasswordsSize());
			bundle.putString("passwordsCount", getPasswordsCount());
		}
		// write the key value pairs to the parcel
		dest.writeBundle(bundle);
	}

	/**
	 * Creator required for class implementing the parcelable interface.
	 */
	public static final Parcelable.Creator<MediaTransferStateVO> CREATOR = new Creator<MediaTransferStateVO>() {

		@Override
		public MediaTransferStateVO createFromParcel(Parcel source) {
			// read the bundle containing key value pairs from the parcel
			Bundle bundle = source.readBundle();

			return new MediaTransferStateVO (
					bundle.getString("contactsState"),bundle.getString("contactsSize"),bundle.getString("contactsCount"),
					bundle.getString("photosState"),bundle.getString("photosSize"),bundle.getString("photosCount"),
					bundle.getString("appsState"),bundle.getString("appsSize"),bundle.getString("appsCount"),
					bundle.getString("videosState"),bundle.getString("videosSize"),bundle.getString("videosCount"),
					bundle.getString("musicState"),	bundle.getString("musicSize"),bundle.getString("musicCount"),
					bundle.getString("callLogsState"),	bundle.getString("callLogsSize"),bundle.getString("callLogsCount"),
					bundle.getString("smsState"),	bundle.getString("smsSize"),bundle.getString("smsCount"),
					bundle.getString("wifiSettingState"),bundle.getString("wifiSettingSize"),
					bundle.getString("calendarState"),bundle.getString("calendarSize"),bundle.getString("calendarCount"),
					bundle.getString("documentState"),bundle.getString("documentSize"),bundle.getString("documentCount"),
					bundle.getString("passwordsState"),bundle.getString("passwordsSize"),bundle.getString("passwordsCount")
			);
		}

		@Override
		public MediaTransferStateVO[] newArray(int size) {
			return new MediaTransferStateVO[size];
		}

	};
}
