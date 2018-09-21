/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verizon.contenttransfer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTSelectContentActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.adapter.P2PContentListAdapter;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CrashReportUploadAysncTask;
import com.verizon.contenttransfer.model.CTDeviceComboModel;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.asynctask.AppAnalyticsAysncTask;
import com.verizon.contenttransfer.p2p.asynctask.CommMessageAsyncTask;
import com.verizon.contenttransfer.p2p.asynctask.OpenCommSocketAsyncTask;
import com.verizon.contenttransfer.p2p.asynctask.VZcloudBannerAnalyticsAsyncTask;
import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.p2p.model.ClientConnectionObject;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.p2p.service.P2PClientIos;
import com.verizon.contenttransfer.p2p.service.P2PServerIos;
import com.verizon.contenttransfer.sms.ComposeSmsActivity;
import com.verizon.contenttransfer.sms.MmsReceiver;
import com.verizon.contenttransfer.utils.UtilsFromApacheLib.CTFileUtils;
import com.verizon.contenttransfer.wifidirect.WifiDirectCustomListener;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;


/**
 * This class contains static utility methods.
 */
public class Utils {

	private static WifiDirectCustomListener listener;
	private static boolean isWifiP2pEnabled = false;

	private static final long MEGABYTE = 1024L * 1024L;
	public static boolean SECURITY_CHECK_PASSED = false;
	public static String defaultSmsApp = null;
	public final static String TAG = "Utils";

	public static boolean isSendingDevice = false;

	public static final Uri CALL_LOGS_CONTENT_URI = CallLog.Calls.CONTENT_URI; // ##

	// Prevents instantiation.
	private Utils() {
	}

	public static WifiDirectCustomListener getListener() {
		return listener;
	}

	public static void setListener(WifiDirectCustomListener listener) {
		Utils.listener = listener;
	}

	public static void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		Utils.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	/**
	 * Uses static final constants to detect if the device's platform version is
	 * Gingerbread or later.
	 */
	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	/**
	 * Uses static final constants to detect if the device's platform version is
	 * Honeycomb or later.
	 */
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private final static String p2pInt = "p2p-p2p0";


	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	byte[] buffer = new byte[1024];

	public static long bytesAvailable() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		long bytesAvailable = 0;
		if (Build.VERSION.SDK_INT <= 18) {
			bytesAvailable = (long) stat.getBlockSize()
					* (long) stat.getAvailableBlocks();
		} else if (Build.VERSION.SDK_INT > 18) {
			bytesAvailable = (long) stat.getBlockSizeLong()
					* (long) stat.getAvailableBlocksLong();
		}

		return bytesAvailable;
	}

	/**
	 * This method returns the available memory size in MB
	 *
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static float megabytesAvailable() {

		return bytesAvailable() / (1024.f * 1024.f);
	}


	public static BytesConversion bytesToMegDisplay(long bytes) {
		BytesConversion conversion = new BytesConversion();
		try {
			if (0 != bytes && bytes < MEGABYTE) {
				//conversion.setStringValue(CTGlobal.getInstance().getContentTransferContext().getString(R.string.less_than_1));
				conversion.setStringValue(CTGlobal.getInstance().getContentTransferContext().getString(R.string.less_than_1));
			} else {
				//Tenth decimal place update
				//DecimalFormat df = new DecimalFormat("#0.00");
				//double bytesToMb = Double.parseDouble(df.format((double)bytes / MEGABYTE));
				double bytesToMb = bytesToMeg(bytes);

				conversion.setNumericValue((float) bytesToMb);
			}
			return conversion;

		} catch (Exception e) {
			LogUtil.d(TAG, "Exception bytesToMegDisplay : " + e);
			return conversion;
		}
	}
	public static double bytesToMeg(long bytes) {
		try {
			double thresholdValue = 0.1;
			LogUtil.d(TAG,"bytes to meg :"+bytes);


			Double BMI = ((double)bytes / MEGABYTE);
			DecimalFormat df1 = new DecimalFormat("0.00");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			df1.setDecimalFormatSymbols(dfs);
			String sBMI = df1.format(BMI);
			double bytesToMb = Double.parseDouble(sBMI);
			LogUtil.d(TAG,"bytes to meg- sBMI :"+sBMI);

			if(bytesToMb<thresholdValue){
				bytesToMb = thresholdValue;
			}
			LogUtil.d(TAG,"bytes to meg bytesToMb 111:"+bytesToMb);
			return bytesToMb;
		} catch (Exception e) {
			LogUtil.d(TAG, "Error bytesToMeg converting bytes to mega bytes : " + e.toString());
			return 0;
		}
	}

	/**
	 * @param
	 * @return
	 */
	public static float bytesToGigabytes(float megaBytes) {
		return (float) (megaBytes / 1024L);
	}

	public static String getFirmware() {
		return Build.VERSION.RELEASE;
	}


	public static String getAndroidVersion() {
		String release = Build.VERSION.RELEASE;
		int sdkVersion = Build.VERSION.SDK_INT;
		return "Android SDK: " + sdkVersion + " (" + release + ")";
	}

	public static boolean isConnectedViaWifi() {
		ConnectivityManager connectivityManager = (ConnectivityManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

/*	public static void getWifiConnectionInfo() {
		try {
			WifiManager wifiManager = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiManager != null) {
				LogUtil.d(TAG, "getLinkSpeed : " + wifiManager.getConnectionInfo().getLinkSpeed());
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
					LogUtil.d(TAG, "getFrequency : " + wifiManager.getConnectionInfo().getFrequency());
				}

				LogUtil.d(TAG, "getRssi : " + wifiManager.getConnectionInfo().getRssi());
				LogUtil.d(TAG, "getConnectionInfo toString : " + wifiManager.getConnectionInfo().toString());
				LogUtil.d(TAG, "getSupplicantState : " + wifiManager.getConnectionInfo().getSupplicantState().toString());
				//wifiManager.is5GHzBandSupported()
			} else {
				LogUtil.d(TAG, "Wifi Manager is null");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static String getWifiLinkSpeed() {
		int linkSpeed = 0;
		try {

			WifiManager wifi = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
			linkSpeed = wifi.getConnectionInfo().getLinkSpeed();
			LogUtil.d(TAG, "WiFi linkSpeed = " + linkSpeed);
		} catch (Exception e) {
			LogUtil.d(TAG, e.getMessage());
		}
		return String.valueOf(linkSpeed);
	}

	public static int getWiFiFrequeny(){
		WifiManager wifi = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);


		int currentapiVersion = Build.VERSION.SDK_INT;
		if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP){
			WifiInfo info = wifi.getConnectionInfo();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				LogUtil.d(TAG,"getFrequency : " + info.getFrequency());
			}
		}
		return 0;
	}

	public static String getWifiSignalStrength(){
		int signalStrength = 0;
		try {
			int MIN_RSSI = -100;
			int MAX_RSSI = -55;
			int levels = 101;
			WifiManager wifi = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			int rssi = info.getRssi();

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				signalStrength = WifiManager.calculateSignalLevel(info.getRssi(), levels);
			} else {
				// this is the code since 4.0.1
				if (rssi <= MIN_RSSI) {
					signalStrength = 0;
				} else if (rssi >= MAX_RSSI) {
					signalStrength = levels - 1;
				} else {
					float inputRange = (MAX_RSSI - MIN_RSSI);
					float outputRange = (levels - 1);
					signalStrength = (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
				}
			}

		}catch (Exception e){
			LogUtil.d(TAG,e.getMessage());
		}
		LogUtil.d(TAG, "WiFi Signal Strength = " + signalStrength);

		return String.valueOf(signalStrength);
	}*/

	public static boolean isWifiDirectSupported() {
		boolean flag = true;

		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
				|| isMotoHtcSupportWifiDirect()) {
			flag = false;
		}
		return flag;
	}
	public static boolean isHotspotSupported() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	private static boolean isMotoHtcSupportWifiDirect() {
		boolean flag = false;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			if (Build.BRAND.equalsIgnoreCase(VZTransferConstants.MOTOROLA_BRAND)) {
				flag = true;
			} else if (Build.BRAND.equalsIgnoreCase(VZTransferConstants.HTC_BRAND)
					|| Build.MODEL.toUpperCase().startsWith("HTC")) {
				flag = true;
			}
		}

		return flag;


	}
	public static boolean isSamsung()
	{
		String manufacturer = android.os.Build.MANUFACTURER;
		if (manufacturer.toLowerCase(Locale.ENGLISH).contains(VZTransferConstants.SAMSUNG_BRAND))
			return true;
		else
			return false;
	}
	public static long getTimeInMillis() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		return calendar.getTimeInMillis();
	}

	public static boolean isAlphaNumeric(String s) {
		//String pattern = "^[a-zA-Z0-9]*$";
		String pattern = "^[a-zA-Z0-9~@#$^*()_+={}|\\,.?: -]*$";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	public static void uploadAppAnalyticsFile() {
		boolean isCtFlowStarted = ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);
		LogUtil.d(TAG, "isCtFlowStarted =" + isCtFlowStarted);
		if(isCtFlowStarted){
			// check if not middle of transfer.
			return;
		}
		if (!CTGlobal.getInstance().isAppAnalyticsAysncTaskRunning()) {
			AppAnalyticsAysncTask appAnalyticsAysncTask = new AppAnalyticsAysncTask();
			LogUtil.d(TAG, "Launching AppAnalytics async task....");
			if (Build.VERSION.SDK_INT >= 11) {
				LogUtil.d(TAG, "Launching single thread async task...");
				appAnalyticsAysncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
			} else {
				LogUtil.d(TAG, "Launching single thread async task...");
				appAnalyticsAysncTask.execute();
			}
		}
	}

	public static void uploadVzcloudBannerJSON(){
		boolean isCtFlowStarted = ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);
		LogUtil.d(TAG, "isCtFlowStarted =" + isCtFlowStarted);
		if(isCtFlowStarted){
			// check if not middle of transfer.
			return;
		}
		if (!CTGlobal.getInstance().isVzcloudClickAnalyticsRunning()) {
			VZcloudBannerAnalyticsAsyncTask vzcloudBannerAnalyticsAsyncTask = new VZcloudBannerAnalyticsAsyncTask();
			LogUtil.d(TAG, "Launching vzcloud banner async task....");
			if (Build.VERSION.SDK_INT >= 11) {
				LogUtil.d(TAG, "Launching single thread async task...");
				vzcloudBannerAnalyticsAsyncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
			} else {
				LogUtil.d(TAG, "Launching single thread async task...");
				vzcloudBannerAnalyticsAsyncTask.execute();
			}
		}
	}

	public static void uploadCrashErrorReportFile() {
		boolean isCtFlowStarted = ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_CT_FLOW_STARTED, false);
		LogUtil.d(TAG, "isCtFlowStarted ="+isCtFlowStarted);
		if(isCtFlowStarted){
			return;
		}
		if (!CrashReportUploadAysncTask.isCrashReportAysncTaskRunning) {

			try {
				String errorFileContent = Utils.readFileContent(new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.ERROR_REPORT_FILE));
				if (errorFileContent.length() > 0) {
					CrashReportUploadAysncTask crashReportUploadAysncTask = new CrashReportUploadAysncTask(errorFileContent);
					LogUtil.d(TAG, "Launching uploadCrashErrorReportFile async task....");
					if (Build.VERSION.SDK_INT >= 11) {
						LogUtil.d(TAG, "Launching single thread async task...");
						crashReportUploadAysncTask.executeOnExecutor(Executors.newSingleThreadExecutor());
					} else {
						LogUtil.d(TAG, "Launching single thread async task...");
						crashReportUploadAysncTask.execute();
					}
				}
				//}
			} catch (Exception e) {
				LogUtil.d(TAG, "Exception in reading error report file." + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static String readFileContent(File errorFile) {
		String errorFileContent = "";
		try {
			if (errorFile != null && errorFile.exists()) {
				errorFileContent = CTFileUtils.readFileToString(errorFile, "UTF-8");
				LogUtil.d(TAG, "errorFileContent =" + errorFileContent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorFileContent;
	}

	public static void logEvent(View view, Map<String, Object> eventExtraInfo, String name) {

		if (VZTransferConstants.MVM_ANALYTICS) {
			//LogAnalytics.getInstance().logEvent(view, eventExtraInfo, name, LogAnalytics.EventType.CLICK, VZTransferConstants.APP_NAME, false);
		}
	}

	public static void logEvent(Activity activity) {
		if (VZTransferConstants.MVM_ANALYTICS) {
			//LogAnalytics.getInstance().initialize(activity, VZTransferConstants.ANALYTICS_URL, true);
		}
	}


	public static Date StringToUTCDate(String dateStr)
	{
		LogUtil.d(TAG,"Date str ="+dateStr);
		Date date = new Date();
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			date = formatter.parse(dateStr.trim());

		}
		catch (Exception e)
		{
			LogUtil.d(TAG,"Formatting date from string:"+e.getMessage());
		}
		return date;
	}

	public static Date getUTCDate(Date date) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.setTime(date);
		Date time = calendar.getTime();
		SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateAsString = outputFmt.format(time);

		return StringToUTCDate(dateAsString);
	}

	public static String getEncodedString(String reqStr){

		String str = "";
		byte[] data = new byte[0];
		try {
			data = reqStr.getBytes("UTF-8");
			str = Base64.encodeToString(data, Base64.NO_WRAP);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static String getDecodedString(String base64){
		String str = "";
		LogUtil.d(TAG, "Before decode file path :" + base64);
		try {
			byte[] data = Base64.decode(base64, Base64.NO_WRAP);
			str = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (IllegalArgumentException e){
			str=VZTransferConstants.INVALID_QR_CODE;
			e.printStackTrace();
		}
		LogUtil.d(TAG, "after decode file path :" + str);
		return str;
		//return base64;
	}

	public static String getAppsFileDetails(String path, String name, String size) {

		StringBuilder jsonString = new StringBuilder();
		jsonString.append("{\"Path\":\"");
		jsonString.append(Utils.getEncodedString(path));
		jsonString.append("\",");
		jsonString.append("\"Size\":\"");
		jsonString.append(size);
		jsonString.append("\",");
		jsonString.append("\"name\":\"");
		jsonString.append(Utils.getEncodedString(name));
		jsonString.append("\"},");

		return jsonString.toString();
	}  // actual used JSON format

	public static String getRequestedFileDetails(String docPath, String docSize, String albumName) {
		//String jsonString = "";
		StringBuilder jsonString = new StringBuilder();
		jsonString.append("{\"Path\":\"");
		jsonString.append(Utils.getEncodedString(docPath));
		jsonString.append("\",");
		jsonString.append("\"Size\":\"");
		jsonString.append(docSize);
		jsonString.append("\",");
		jsonString.append("\"AlbumName\":[\"");
		jsonString.append(Utils.getEncodedString(albumName));
		jsonString.append("\"]},");

		return jsonString.toString();
	}
	public static String initCapitalize(final String line) {
		String formatedStr = "";
		if(line != null && line.length()>0){
			formatedStr = String.valueOf(line.charAt(0)).toUpperCase();
			if(line.length()>1) {
				formatedStr += line.substring(1).toLowerCase();
			}
		}
		return formatedStr;
	}
	public static boolean validateMDN(String mdn){

		mdn.replace("-", "");
		if(mdn.matches("\\d{10}")){
			return true;
		}
		return false;
	}

	public static String getFileName(String absolutePath) {
		String returnStr = "";
		String addStars = "......";
		String mFileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1);
		try{
			if (mFileName.length()> 26) {
				returnStr = mFileName.substring(0,10);
				returnStr += addStars;
				returnStr += mFileName.substring((mFileName.length()-10),mFileName.length());
				return returnStr;
			}
		}catch (Exception e){
			LogUtil.d(TAG, "Exception on getting formatted file name" + e.getMessage());
		}
		return mFileName;
	}

	public static String getDeviceInfo() {
		String sendingDeviceDetails = "";

		StringBuilder deviceInfo = new StringBuilder();
		deviceInfo.append("{\"");

		deviceInfo.append(VZTransferConstants.DB_PAIRING_DEVICE_ID);
		deviceInfo.append("\"");
		deviceInfo.append(":\"");
		deviceInfo.append(CTGlobal.getInstance().getDeviceUUID());
		deviceInfo.append("\"");

		deviceInfo.append(",");
		deviceInfo.append("\"");
		deviceInfo.append(VZTransferConstants.DB_PAIRING_MODEL);
		deviceInfo.append("\"");
		deviceInfo.append(":\"");
		deviceInfo.append(Build.MODEL);
		deviceInfo.append("\"");

		deviceInfo.append(",");
		deviceInfo.append("\"");
		deviceInfo.append(VZTransferConstants.DB_PAIRING_OS_VERSION);
		deviceInfo.append("\"");
		deviceInfo.append(":\"");
		deviceInfo.append(Utils.getAndroidVersion());
		deviceInfo.append("\"");

		deviceInfo.append(",");
		deviceInfo.append("\"");
		deviceInfo.append(VZTransferConstants.DB_PAIRING_DEVICE_TYPE);
		deviceInfo.append("\"");
		deviceInfo.append(":\"");
		deviceInfo.append(Build.MANUFACTURER);
		deviceInfo.append("\"");

		deviceInfo.append("}");


		LogUtil.d(TAG, "Sending device Details :" + deviceInfo.toString());

		sendingDeviceDetails = deviceInfo.toString();
		return sendingDeviceDetails;
	}

	public static void writeToCommSocketThread(final String msg, final String hostIp) {
		final PrintWriter commWriter = SocketUtil.getCommWriter(hostIp);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				LogUtil.d(TAG, "Writing to COMM port....commWriter=" + commWriter);
				try {
					if (null != commWriter) {
						commWriter.write(msg);
						commWriter.write(VZTransferConstants.CRLF);
						commWriter.flush();
						LogUtil.d(TAG, "Comm msg wrote successfully-- msg=" + msg);
					}
				}catch (Exception e){}
			}
		});
		thread.start();
	}

	public static void writeToAllCommSocketThread(String msg){
		CTAnalyticUtil ctAnalyticUtil;
		if(ClientConnectionObject.getInstance().isConnected()) {
			writeToCommSocketThread(msg, null);
			ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
			if(ctAnalyticUtil != null) {
				ctAnalyticUtil.setVztransferCancelled(true);
			}
		}else {
			LogUtil.d(TAG, "Total accepted clients size :" + ServerConnectionObject.getInstance().getAcceptedClients().size());
			if(ServerConnectionObject.getInstance().getAcceptedClients() != null && ServerConnectionObject.getInstance().getAcceptedClients().size()>0){
				Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = ServerConnectionObject.getInstance().getAcceptedClients().entrySet().iterator();

				while (iterator.hasNext()) {
					Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
					LogUtil.d(TAG, "...Client ip : " + entry.getKey() + " ...Status :" + entry.getValue().getStatus());
					writeToCommSocketThread(msg, entry.getValue().getClientIp());
					ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(entry.getValue().getClientIp());
					if(ctAnalyticUtil != null) {
						ctAnalyticUtil.setVztransferCancelled(true);
					}
				}
			}
		}
	}

	public static ContentSelectionVO getContentSelection(String mediatype) {
		ContentSelectionVO ctselection= new ContentSelectionVO();

		if(P2PContentListAdapter.contentselctionlist != null) {
			for (int i = 0; i < P2PContentListAdapter.contentselctionlist.size(); i++) {
				if (P2PContentListAdapter.contentselctionlist.get(i).getContentType().equals(mediatype)) {
					ctselection = P2PContentListAdapter.contentselctionlist.get(i);
					break;
				}
			}
		}
		return ctselection;
	}

	public static void setAvailableSpace(String availableSpace) {
		if (null != availableSpace)
		{
			if (availableSpace.startsWith(VZTransferConstants.NEW_DEVICE_AVAILABLE_SPACE)) {
				String parsingSpace = availableSpace.substring(VZTransferConstants.NEW_DEVICE_AVAILABLE_SPACE.length());
				LogUtil.d(TAG, "Parsed space =" + parsingSpace);
				long avaSpace = Long.parseLong(parsingSpace);

				CTGlobal.getInstance().setAvailableSpaceAtReceiverEnd(avaSpace);
			}
		}
	}

	public static void resetExitAppFlags(boolean flag) {
		CTGlobal.getInstance().setExitApp(flag);
		CTGlobal.getInstance().setForceStop(flag);
	}

	public static void manualSetupExit(Activity activity, Dialog manualSetupDialog){
		CTGlobal.getInstance().setManualSetup(true);
		HotSpotInfo.resetHotspotInfo();
		manualSetupDialog.dismiss();
		activity.finish();
		CTGlobal.getInstance().setExitApp(true);
	}
	public static void openCommSockets(String commSocketName, String port) {
		//NOTE: comm socket reset on server/client socket create.
		OpenCommSocketAsyncTask openCommPort = new OpenCommSocketAsyncTask(commSocketName, port);
		LogUtil.d(TAG, "Launching Open Comm port async task.");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			openCommPort.executeOnExecutor(Executors.newSingleThreadExecutor());
		} else {
			openCommPort.execute();
		}
	}

	public static void commTaskToReadWriteMsg(Socket commClientSocket) {
		if(commClientSocket != null) {
			CommMessageAsyncTask commMessage = new CommMessageAsyncTask(commClientSocket);
			LogUtil.d(TAG, "Launching commTask To ReadServerMsg async task.");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				commMessage.executeOnExecutor(Executors.newSingleThreadExecutor());
			} else {
				commMessage.execute();
			}
		}else {
			LogUtil.d(TAG, "Client socket is null.");
		}
	}
	public static void startP2PServerTask(Activity activity) {
		Utils.createServerConnectionObject(null);
	}

	public static void createServerConnectionObject(InetAddress host) {
		LogUtil.d(TAG, "server socket - creating.");
		ServerConnectionObject.getInstance().destroySocket();
		ServerConnectionObject.getInstance().createServer(host);
	}

	public static void CreateClientConnectionObject(String connType, String serverIp, String altServerIp,Activity activity) {
		LogUtil.d(TAG, "Client socket - creating for connType :" + connType);
		if(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)) {
			CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.msg_connection_other_device),activity, false);
		}
		ClientConnectionObject.getInstance().destroySocket();
		ClientConnectionObject.getInstance().createClient(serverIp, altServerIp);

	}

	public static void updateConnectedDeviceInfo(Socket clientSocket, String host, boolean isCross, String status) throws IOException {
		if (clientSocket != null) {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			ClientConnectionObject.getInstance().setClientSocket(clientSocket);
			ClientConnectionObject.getInstance().setStatus(status);
			ClientConnectionObject.getInstance().setIn(in);
			ClientConnectionObject.getInstance().setOut(out);
			ClientConnectionObject.getInstance().setHost(host);
			ClientConnectionObject.getInstance().getCtAnalyticUtil().setIsCross(isCross);
			LogUtil.d(TAG, "update Connected Device Info success.");
		}else {
			LogUtil.e(TAG, "update Connected Device Info failed - client socket is null.");
		}
	}

	public static AcceptedClientInfo getAccetpedDeviceInfo(Socket clientSocket, String clientIp, boolean isCross, String status) throws IOException {

		AcceptedClientInfo clientInfo = new AcceptedClientInfo();
		if(clientSocket != null) {
			OutputStream out = clientSocket.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			clientInfo.setClientIp(clientIp);
			clientInfo.setClientSocket(clientSocket);
			clientInfo.setStatus(status);
			clientInfo.setIn(in);
			clientInfo.setOut(out);
			clientInfo.getCtAnalyticUtil().setIsCross(isCross);

		}
		return clientInfo;
	}

	public static String getServerName () {
		if(!Utils.isWifiDirectSupported() ||
				CTGlobal.getInstance().isCross() ||
				CTGlobal.getInstance().isDoingOneToMany()){
			if(isHotspotSupported()){
				return VZTransferConstants.HOTSPOT_WIFI_CONNECTION;
			}else {
				return VZTransferConstants.PHONE_WIFI_CONNECTION;
			}
		}else {
			return VZTransferConstants.WIFI_DIRECT_CONNECTION;
		}
	}
	public static boolean isThisServer() {
		boolean isServerFlag = false;
		if(CTDeviceComboModel.getInstance().isCross()){
			if(CTGlobal.getInstance().isTryAnotherWay() && !Utils.isReceiverDevice()){
				isServerFlag = false;
			}else {
				isServerFlag = true;
			}
		}else if(CTGlobal.getInstance().isDoingOneToMany() &&
				!Utils.isReceiverDevice()){
			isServerFlag = true;
		}else if(!CTGlobal.getInstance().isDoingOneToMany() &&
				Utils.isReceiverDevice()){
			isServerFlag = true;
		}
		LogUtil.d(TAG, "Is This Server :" + isServerFlag);

		return isServerFlag;

	}

	public static boolean isReceiverDevice() {
		if(CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)){
			return true;
		}else {
			return false;
		}
	}

	public static String resetUUID(){
		return UUID.randomUUID().toString().replace("-","").toLowerCase();
	}

	public static void UniqueID(){
		String uniqueID= UUID.randomUUID().toString().replace("-","").toLowerCase();
		LogUtil.d(TAG,"Utils getUniqueID :"+ uniqueID);
		ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.GLOBAL_UUID, uniqueID);
	}


	public static void resetWiFiDirect() {

		LogUtil.d(TAG, " Version Check Receiver - reset wifi direct : ");

		Intent intent = new Intent("stop-widi-broadcast-receiver");
		// You can also include some extra data.
		intent.putExtra("message", "stopwidireceiver");
		LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
		LogUtil.d(TAG, "Broadcasting message sent");

		if(Utils.isWifiDirectSupported()) {
			WiFiDirectActivity.StopPeerDiscovery();
		}
		Utils.setListener(null);
	}

	public static boolean shouldCollect(String media) {
		if (CTGlobal.getInstance().getExitApp()){
			return false;
		}
		else if (CTSelectContentModel.getInstance().getSelectedMediaList() != null) {
			if (!CTSelectContentModel.getInstance().getSelectedMediaList().contains(media)) {
				return false;
			}
		}
		return true;
	}

	public static void startP2PClientIos(String hostReceived) {
		LogUtil.d(TAG, "Launching P2PClientIos..hostReceived=" + hostReceived);
/*		Intent intent = new Intent(CTGlobal.getInstance().getContentTransferContext(), P2PClientIos.class);
		intent.setAction(VZTransferConstants.ACTION_HAND_SHAKE);
		intent.putExtra(VZTransferConstants.EXTRAS_GROUP_OWNER_ADDRESS, hostReceived);
		CTGlobal.getInstance().getContentTransferContext().startService(intent);*/

		P2PClientIos iosClient = new P2PClientIos(hostReceived);
		LogUtil.d(TAG, "Launching P2PClientIos..");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			iosClient.executeOnExecutor(Executors.newSingleThreadExecutor());
		} else {
			iosClient.execute();
		}
	}
	public static void oneToManyNextButton(Activity activity) {
		if(ServerConnectionObject.getInstance().isConnectionAvailable()) {
			LogUtil.d(TAG, "******** one to many next is clicked... Start Select content activity.");
			CTGlobal.getInstance().setWaitForNewDevice(false);
			Utils.startSelectContentActivity();
		}else {
			LogUtil.d(TAG,"******** one to many next is clicked...No client connected.");
			CustomDialogs.createInfoDialog(activity, activity.getString(R.string.no_device_connected));
		}
	}
	public static void startSelectContentActivity(){
		if(!CTGlobal.getInstance().isWaitForNewDevice()) {
			Intent intent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTSelectContentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
		}else {
			if(CTGlobal.getInstance().isDoingOneToMany()) {
				LogUtil.d(TAG,"doing one to many - send broadcast..UPDATE_TOTAL_CONNECTION_COUNT");
				Intent intent = new Intent(VZTransferConstants.UPDATE_TOTAL_CONNECTION_COUNT);
				LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
			}
		}
	}

	public static void startP2PServerIos(String hostIp) {
		P2PServerIos iosServer = new P2PServerIos(hostIp);
		LogUtil.d(TAG, "Launching P2PServerIOS..");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			iosServer.executeOnExecutor(Executors.newSingleThreadExecutor());
		} else {
			iosServer.execute();
		}
	}
	public static List<ApplicationInfo> getInstalledApps(Context context) {
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		List<ApplicationInfo> trimList = new ArrayList<>();
		for (ApplicationInfo appInfo : apps) {
			if (!Utils.isSystemPackage(appInfo) &&
					!appInfo.packageName.equals(VZTransferConstants.CONTENT_TRANSFER_PKG)
					&& !appInfo.packageName.equals(VZTransferConstants.MY_VERIZON_PKG)
					&& packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
				trimList.add(appInfo);
			}
		}

		return trimList;
	}


	/*
    Some devices have Facebook, Instagram, etc... as system apps. When receiving device tries to install these
    apps from the sender, there is an error. Use this method to generate an "installed" list that contains
    these apps also.
     */
	public static List<ApplicationInfo> getAllApps(Context context) {
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		ArrayList<String> appNames = new ArrayList<String>();
		for (ApplicationInfo appInfo : apps) {
			appNames.add(appInfo.loadLabel(packageManager).toString());
		}
		CTAppUtil.getInstance().setInstalledAppNames(appNames);
		return apps;
	}


	public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
		return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}
	public static void cleanupSenderMediaFilesFromVZTransferDir() {
		deleteTempFile(VZTransferConstants.PHOTOS_FILE);
		deleteTempFile(VZTransferConstants.APPS_FILE);
		deleteTempFile(VZTransferConstants.SMS_FILE);
		deleteTempFile(VZTransferConstants.VIDEOS_FILE);
		deleteTempFile(VZTransferConstants.MUSICS_FILE);
		deleteTempFile(VZTransferConstants.CALLLOGS_FILE);
		deleteTempFile(VZTransferConstants.CALENDAR_FILE);
		deleteTempFile(VZTransferConstants.DOCUMENTS_FILE);

	}

	public static void cleanUpReceiverMediaFilesFromVZTransfer() {

		File transferDir = new File(VZTransferConstants.VZTRANSFER_DIR);

		if (transferDir.exists()) {
			LogUtil.d(TAG, "Transfer Directory already exists..");
			if (transferDir.isDirectory()) {
				deleteTempFile(VZTransferConstants.CLIENT_CONTACTS_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_PHOTOS_LIST_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_VIDEOS_LIST_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_MUSIC_LIST_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_CALLLOG_LIST_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_SMS_LIST_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_CALENDAR_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_DOCUMENTS_FILE);
				deleteTempFile(VZTransferConstants.CLIENT_PAYLOAD);

				//rahiahm - be mindful before deleting these if user needs to keep the app list
				if(!ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), ContentPreference.KEEP_APPS, false)) {
					deleteTempFile(VZTransferConstants.CLIENT_APPS_LIST_FILE);
					deleteTempAppFolder();
				}
			}
		}
	}

	private static void deleteTempFile(String fileName) {
		File file = new File(VZTransferConstants.VZTRANSFER_DIR, fileName);
		try {
			if (null != file && file.exists()) {
				LogUtil.d(TAG, "Deleting file :" + file.getName());
				file.delete();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	public static void deleteTempAppFolder() {
		File dir = new File(VZTransferConstants.tempAppsStoragePath);
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				new File(dir, children[i]).delete();
			}
		}
	}


	public static String findContentStorage(int totCount, double totBytes,String media ) {
		String contentStorage = "";
		if (totCount == 0) {
			contentStorage = "0"+VZTransferConstants.MB;
		}else if (totBytes <= 0.1 && totCount > 0) {
			contentStorage = CTGlobal.getInstance().getContentTransferContext().getString(R.string.less_than_1) + VZTransferConstants.MB;
		}else if(media.equalsIgnoreCase(VZTransferConstants.APPS_STR) && CTGlobal.getInstance().isCross()){
			contentStorage="";
		}else{
			contentStorage = String.valueOf(totBytes)+VZTransferConstants.MB;
		}
		return contentStorage;
	}

	public static void resetConnectionOnTransferFinish(boolean forceWifiReset) {
		if(HotSpotInfo.isDeviceHotspot()){
			WifiAccessPoint.getInstance().Stop();
			LogUtil.d(TAG, "Stopped hotspot server.");
		}
		WifiManagerControl.resetWifiConnectionOnFinish(forceWifiReset);
	}


	public static void saveImage(Drawable d, String name) {

		Bitmap finalBitmap = drawableToBitmap(d);
		File myDir = new File(VZTransferConstants.tempAppsIconsStoragePath);
		myDir.mkdirs();
		String fname = name + ".png";
		File file = new File (myDir, fname);
		if (file.exists()) file.delete ();
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Bitmap drawableToBitmap(Drawable d){
		Bitmap bitmap = null;

		if (d instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		if(d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		d.draw(canvas);
		return bitmap;
	}
	public static boolean isStandAloneBuild(){
		return com.verizon.contenttransfer.BuildConfig.STANDALONE_BUILD;
	}

	public static void cleanUpOnTransferFinish() {

		Utils.writeToAllCommSocketThread(VZTransferConstants.CLOSE_COMM_ON_FINISH_TRANSFER);

		MediaFetchingService.isP2PFinishActivityLaunched = true;

		resetVariables();
		LogUtil.d(TAG, "Transfer Complete!!!");
		if(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)) {
			WifiManagerControl.resetWifiConnectionOnFinish(false);
			LogUtil.d(TAG, "Wifi reset completed." );
		}
		Utils.resetConnectionOnTransferFinish(false);
	}
	public static void resetVariables() {
		MediaFileListGenerator.TOT_DOCS=0;
		MediaFileListGenerator.TOT_CONTACTS = 0;
		MediaFileListGenerator.TOT_CLOUD_CONTACTS = 0;
		MediaFileListGenerator.TOT_CALLLOGS = 0;
		MediaFileListGenerator.TOT_MESSAGES = 0;
		MediaFileListGenerator.TOT_CALENDAR =0 ;
	}
	public static void setCtAnalyticUtil(String activityName,String eventName){
		if(eventName.equals(VZTransferConstants.MF_MVM_EXIT)) {
			CTAnalyticUtil ctAnalyticUtil;
			if (ClientConnectionObject.getInstance().isConnected()) {
				ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(null);
				ctAnalyticUtil.setDataTransferStatusMsg(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER);
				ctAnalyticUtil.setDescription(ctAnalyticUtil.getDescription()+" > "+VZTransferConstants.MF_MVM_EXIT + ">" + activityName);
			} else {
				if (ServerConnectionObject.getInstance().getAcceptedClients() != null && ServerConnectionObject.getInstance().getAcceptedClients().size() > 0) {
					Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = ServerConnectionObject.getInstance().getAcceptedClients().entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
						Log.d(TAG, "Client ip : " + entry.getKey() + " Status :" + entry.getValue().getStatus());
						ctAnalyticUtil = entry.getValue().getCtAnalyticUtil();
						ctAnalyticUtil.setDataTransferStatusMsg(VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER);
						ctAnalyticUtil.setDescription(ctAnalyticUtil.getDescription()+" > "+VZTransferConstants.MF_MVM_EXIT + ">" + activityName);
					}
				}
			}
		}
	}

	public static boolean isCTTransferSuccess(){
		if (ClientConnectionObject.getInstance().isConnected()) {
			if(SocketUtil.getCtAnalyticUtil(null).getDataTransferStatusMsg().equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
				return true;
			}
		} else {
			if (ServerConnectionObject.getInstance().getAcceptedClients() != null && ServerConnectionObject.getInstance().getAcceptedClients().size() > 0) {
				Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = ServerConnectionObject.getInstance().getAcceptedClients().entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
					Log.d(TAG, "Client ip : " + entry.getKey() + " Status :" + entry.getValue().getStatus());
					if(entry.getValue().getCtAnalyticUtil().getDataTransferStatusMsg().equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void updateTransferredBytes(final CTAnalyticUtil ctAnalyticUtil, final long dataRead) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				ctAnalyticUtil.setTransferredBytes(ctAnalyticUtil.getTransferredBytes() + dataRead);
				//if(ctAnalyticUtil.getProgressUpdate()%2 == 0) {
				Intent intent = new Intent(VZTransferConstants.UPDATE_ONE_TO_MANY_PROGRESS);
				LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);
				//}
				//LogUtil.d(TAG, "Dismissing exit dialog");
			}
		});
		thread.start();
	}
	public static void updateTransferredDuplicateBytes(CTAnalyticUtil ctAnalyticUtil, long dataRead) {
		ctAnalyticUtil.setTotDuplicateBytesTransferred(ctAnalyticUtil.getTotDuplicateBytesTransferred() + dataRead);
	}
	public static void updateTransferredFailedBytes(CTAnalyticUtil ctAnalyticUtil, long dataRead) {
		ctAnalyticUtil.setTotFailedBytesTransferred(ctAnalyticUtil.getTotFailedBytesTransferred() + dataRead);
	}
	public static boolean isSupportMusic(){
		if(CTGlobal.getInstance().isCross()){
			if(VZTransferConstants.SUPPORT_MUSIC_ON_IOS)
				return true;
			else
				return false;
		}else {
			return true;
		}
	}


	public static boolean isGooglePhone() {
		LogUtil.d(TAG,"Build.MANUFACTURER :"+Build.MANUFACTURER+" Build.BRAND :"+Build.BRAND);
		if(Build.BRAND.toLowerCase().equals(VZTransferConstants.GOOGLE)
				&& Build.MANUFACTURER.toLowerCase().equals(VZTransferConstants.GOOGLE)){
			return true;
		}
		return false;
	}

	public static String getPassCopiedToClipboardMsg(Activity activity) {
		StringBuilder sb = new StringBuilder();
		// Password  string : "
		sb.append ( "Select \"" + activity.getString(R.string.button_wifi_settings_text)+"\" > ");
		sb.append (activity.getString(R.string.wifi_connected_to) +"<b>" + QRCodeUtil.getInstance().getQrCodeVO().getSsid() + "</b>." );
		sb.append("<br>"+activity.getString(R.string.password_copied)+"<b>" + activity.getString(R.string.password_paste_msg) + "</b>");
		return sb.toString();
	}

	public static String getMaxConnectionHeader(Activity activity, boolean isHotspot) {
		String myFormattedString = "";
		if(isHotspot){
			myFormattedString = activity.getString(R.string.ct_one_to_many_max_device_connection);
		}else {
			myFormattedString = activity.getString(R.string.ct_one_to_many_max_device_connection_use_pin);
		}
		myFormattedString = String.format(myFormattedString, getMaxConnectionCount(isHotspot));

		return myFormattedString;
	}

	public static int getMaxConnectionCount(boolean isHotspot){
		int connCount = 0;
		if(isHotspot){
			if(Utils.isSamsung()){
				connCount = VZTransferConstants.MAX_SAMSUNG_CONNECTION_COUNT;
			}else {
				connCount = VZTransferConstants.MAX_HOTSPOT_CONNECTION_COUNT;
			}
		}else {
			connCount = VZTransferConstants.MAX_ROUTER_CONNECTION_COUNT;
		}
		return connCount;
	}

	public static boolean isValidFile(File file){

		if(file != null && file.isFile()){
			String filePath = file.getAbsolutePath();
			if( !filePath.contains(VZTransferConstants.VZTRANSFER_DIR)
					&& !filePath.contains(VZTransferConstants.SYSTEM_HIDDEN_FILE)
					&& !filePath.startsWith(VZTransferConstants.DOCUMENT_LOG_FILE)
					&& checkFileCanRead(file)){
				return true;
			}
		}
		return false;
	}

	public static boolean checkFileCanRead(File file){
		if (!file.exists()) {
			return false;
		}
		if (!file.canRead()) {
			return false;
		}
		try {
			InputStream mediaStream = new FileInputStream( file );
			mediaStream.read();
			mediaStream.close();
		} catch (Exception e) {
			LogUtil.e(TAG,"Exception when checked file can read with message:"+e.getMessage());
			return false;
		}
		return true;
	}

	public static int checkPermissionStatus(String permissionName){
		return ContextCompat.checkSelfPermission(CTGlobal.getInstance().getContentTransferContext(), permissionName);
	}

	public static void addNonTransferredFileStatus(CTAnalyticUtil ctAnalyticUtil, String media, TransferSummaryStatus transferSummaryStatus) {
		if ( media.equalsIgnoreCase(VZTransferConstants.PHOTOS) ) {
			ctAnalyticUtil.notTransferredPhotoList.add(transferSummaryStatus);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.VIDEOS) ) {
			ctAnalyticUtil.notTransferredVideoList.add(transferSummaryStatus);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.MUSICS ) ) {
			ctAnalyticUtil.notTransferredMusicList.add(transferSummaryStatus);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.DOCUMENTS ) ) {
			ctAnalyticUtil.notTransferredDocumentList.add(transferSummaryStatus);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.APPS ) ) {
			ctAnalyticUtil.notTransferredAppsList.add(transferSummaryStatus);
		}
	}

	public static void addFailedMediaCount(CTAnalyticUtil ctAnalyticUtil, String media) {
		if ( media.equalsIgnoreCase(VZTransferConstants.PHOTOS) ) {
			ctAnalyticUtil.addFailedPhotoCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.VIDEOS) ) {
			ctAnalyticUtil.addFailedVideoCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.MUSICS ) ) {
			ctAnalyticUtil.addFailedMusicCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.DOCUMENTS ) ) {
			ctAnalyticUtil.addFailedDocCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.APPS ) ) {
			ctAnalyticUtil.addFailedAppCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.CALENDAR) ) {
			ctAnalyticUtil.addFailedCalendarCount(1);
		}
	}
	public static void addTransferredMediaCount(String media, CTAnalyticUtil ctAnalyticUtil, boolean isDuplicate) {
		if ( media.equalsIgnoreCase(VZTransferConstants.PHOTOS) ) {
			if(isDuplicate){
				ctAnalyticUtil.addDuplicatePhotoCount(1);
			}else {
				ctAnalyticUtil.addPhotoCount(1);
			}
		} else if ( media.equalsIgnoreCase( VZTransferConstants.VIDEOS) ) {
			if(isDuplicate){
				ctAnalyticUtil.addDuplicateVideoCount(1);
			}else {
				ctAnalyticUtil.addVideoCount(1);
			}
		}else if ( media.equalsIgnoreCase( VZTransferConstants.MUSICS ) ) {
			if(isDuplicate){
				ctAnalyticUtil.addDuplicateMusicCount(1);
			}else {
				ctAnalyticUtil.addMusicCount(1);
			}
		} else if ( media.equalsIgnoreCase( VZTransferConstants.DOCUMENTS ) ) {
			if(isDuplicate) {
				ctAnalyticUtil.addDuplicateDocCount(1);
			}else {
				ctAnalyticUtil.addDocCount(1);
			}
		} else if ( media.equalsIgnoreCase( VZTransferConstants.APPS) ) {
			ctAnalyticUtil.addAppCount(1);
		} else if ( media.equalsIgnoreCase( VZTransferConstants.CALENDAR) ) {
			ctAnalyticUtil.addCalendarCount(1);
		} else {
			LogUtil.e(TAG, "UNKNOWN Media type is being requested, quitting....");
		}
	}

	public static String stringWithNoSpecialCharacter(String name) {
		return name.replaceAll("[^A-Za-z0-9]+", " ");
	}

	public static boolean shouldShowSmsPrompt(Activity activity){
		String mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(activity);
		boolean flag;
		boolean smsPrompt=!CTDeviceComboModel.getInstance().isCross() && !activity.getPackageName().equals(mDefaultSmsApp) && CTGlobal.getInstance().isSmsPermitted();
		if(!CTGlobal.getInstance().isDoingOneToMany()){
			flag=smsPrompt;
		}else{
			if(CTGlobal.getInstance().isDoingOneToManyComb()){
				flag=false;
			}else{
				flag=smsPrompt;
			}
		}
		LogUtil.d(TAG,"SMS prompt: "+flag);
		return flag;
	}

	public static String getIMEI(Context context){
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice= tm.getDeviceId();
		LogUtil.d(TAG,"tmDevice ID: "+tmDevice);
	    return tmDevice;
	}

	public static boolean isAPILevelAndroidO(){
		LogUtil.d(TAG, "Android API level : "+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= 26) {
			// only for O and newer versions
			WifiManager wifiManagerObj = (WifiManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(Context.WIFI_SERVICE);
			List<WifiConfiguration> list = wifiManagerObj.getConfiguredNetworks();
			LogUtil.d(TAG,"saved network size :"+list.size());
			if(list.size()>0) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}

	public static void deleteGeneratedPasswordManagerDbFile() {
		File tmp_file = new File(VZTransferConstants.VZTRANSFER_PSWDB_DIR, VZTransferConstants.DEFAULT_DATABASE_FILE);
		if(tmp_file.exists()){
			tmp_file.delete();
			LogUtil.d(TAG, "On transfer complete delete generated PSW Manager DB File from external storage.");
		}
	}

	public static boolean isMediaSupported(String media) {
		for(String item : CTGlobal.getInstance().getMediaTypeArray()){
			if(item.trim().equalsIgnoreCase(media)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isGoToSavingMediaPage() {
		if(ReceiveMetadata.mediaStateObject == null) {
			LogUtil.d(TAG,"not Going to saving media page ");
			return false;
		}else if((ReceiveMetadata.mediaStateObject.getSmsState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isSmsPermitted())||
				(ReceiveMetadata.mediaStateObject.getCalendarState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isCalendarPermitted())||
				(ReceiveMetadata.mediaStateObject.getCallLogsState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isCalllogsPermitted())||
				(ReceiveMetadata.mediaStateObject.getContactsState().toLowerCase().trim().equalsIgnoreCase("true") && CTGlobal.getInstance().isContactsPermitted())){

			LogUtil.d(TAG,"Going to saving media page ");
			return true;
		}
		LogUtil.d(TAG,"not Going to saving media page ");
		return false;
	}

	public static String getDate(long milliSeconds, String dateFormat) {
		// Create a DateFormatter object for displaying date in specified format.
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

		// Create a calendar object that will convert the date and time value in milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	public static void deleteTempCalendarFiles() throws IOException {
		//delete calendarTemp
		File removeDir = new File(VZTransferConstants.tempCalendarStoragePath);
		CTFileUtils.deleteDirectory(removeDir);
	}

    public static String  createUniqueUID(Cursor cur, String event_id, String event_dtstart, String event_end, String event_title) {
        //String UNIQUE_UID = "fba3928e0db04b35b131afada86ea";
		int maxLength = 22;
        String UNIQUE_UID = "";
        if(null != event_id){
        	UNIQUE_UID += event_id;
		}

		if(null != event_dtstart && null != event_end){
			UNIQUE_UID += (Long.parseLong(event_end) - Long.parseLong(event_dtstart));
		}else if(null != event_dtstart){
			UNIQUE_UID +=event_dtstart;
		}else if(null != event_end){
			UNIQUE_UID +=event_end;
		}

		if(null != event_title){
			UNIQUE_UID += event_title;
		}

		if(UNIQUE_UID.length()>maxLength){
			// take up to 32 characters.
			UNIQUE_UID.substring(0, maxLength);
		}else {
			// make 32 characters.
			for (int k=0; UNIQUE_UID.length() < maxLength; k++ ) {
				UNIQUE_UID ="0"+UNIQUE_UID;
			}
		}
        //String eventPropertyId = getString(cur, CalendarContractWrapper.Events._ID);
		LogUtil.d(TAG,"UNIQUE_UID created b4 encoding:"+UNIQUE_UID);
		UNIQUE_UID = Utils.getEncodedString(UNIQUE_UID).toLowerCase();
        LogUtil.d(TAG,"UNIQUE_UID created :"+UNIQUE_UID);
        return UNIQUE_UID;
    }

	public static boolean isDirectoryHasFileWithExtension(String filename, String extension) {
		List<String> apkFiles = new ArrayList<String>();
		File dir = new File(filename);

		if(!dir.exists()){
			return false;
		}else{
			LogUtil.d(TAG,"Apps directory exists");
		}

		for (File file : dir.listFiles()) {
			if (file.getName().endsWith((extension))) {
				apkFiles.add(file.getName());
			}
		}

		if (apkFiles.size() > 0) {
			LogUtil.d(TAG,"no of extension files :"+apkFiles.size());
			return true;
		} else {
			LogUtil.d(TAG,"no of extension files found.");
			return false;
		}
	}

	public static boolean isConnectionTimedOutRecorded(){
		boolean status = ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.CONNECTION_TIMED_OUT_DURING_TRANSFER, VZTransferConstants.FALSE );
		return status;
	}

	public static void setConnectionTimedOutRecorded(){
		ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.CONNECTION_TIMED_OUT_DURING_TRANSFER, true);
	}

	public static void enableDefaultSmsApp(Activity activity){
		ComponentName component=new ComponentName(activity ,MmsReceiver.class);
		activity.getPackageManager().setComponentEnabledSetting(component,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

		ComponentName component1=new ComponentName(activity ,ComposeSmsActivity.class);
		activity.getPackageManager().setComponentEnabledSetting(component1,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	public static void clearAllPreferencesForSavingMedia(){

		ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CALENDAR_STR,"false");
		ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CONTACTS_STR,"false");
		ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.SMS_STR,"false");
		ContentPreference.putStringValue(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CALLLOG_STR,"false");

		/*ContentPreference.removeSetting(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CALENDAR_STR);
		ContentPreference.removeSetting(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CONTACTS_STR);
		ContentPreference.removeSetting(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.SMS_STR);
		ContentPreference.removeSetting(CTGlobal.getInstance().getContentTransferContext(),VZTransferConstants.CALLLOG_STR);*/
	}
}
