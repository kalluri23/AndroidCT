package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.verizon.contenttransfer.base.VZTransferConstants;

import java.util.Timer;
import java.util.TimerTask;


public class DataSpeedAnalyzer {

	private static final String TAG = "DataSpeedAnalyzer";

	public static Dialog progressDialog = null;



	public static long TotalSize = 0L;
	static double speed = 0;
	static double speedinMbps = 0;
	static long startTime = System.currentTimeMillis();
	static long tempTime = 0;
	static boolean isDuplicateFile = false;
	public static Timer elapsedTimer = null, speedTimer = null,calcTimer=null, processCancelTimer = null;
	static String progressMessage = "";
	private static long fileCounter = 0;

	public static long transferEndTime;
	public static long transferStartTime;
	public static String currentFileName = "";
	public static String currentProgressStatus = "";
	public static void setCurrentProgressStatus(String currentProgressStatus) {
		DataSpeedAnalyzer.currentProgressStatus = currentProgressStatus;
	}


	public static void setCurrentMediaType(String currentMediaType) {
		/*if(currentMediaType.equals(VZTransferConstants.SMS_STR)) {
			DataSpeedAnalyzer.currentMediaType = currentMediaType;
		}else{*/
		LogUtil.d(TAG,"currentMediaType before :"+currentMediaType);
			DataSpeedAnalyzer.currentMediaType = currentMediaType.toLowerCase();
		LogUtil.d(TAG,"currentMediaType after:"+currentMediaType.toLowerCase());

		//}

	}

	public static String currentMediaType = "";
	private static final String INFO = "Content Transfer";
	public static boolean isTimerStopped = false,isCalcTimerStopped=false, isprocessCancelTimerStopped = false;
	public DataSpeedAnalyzer() {

	}

	public static void setDuplicateFile(boolean flag) {
		isDuplicateFile = flag;
	}
	public static void setTotalsize(long data)
	{
		TotalSize = data;
	}
	public static long getTotalSize() {
		return TotalSize;
	}
	public static long getFileCounter() {
		return fileCounter;
	}

	public static void setFileCounter(long fileCounter) {
		DataSpeedAnalyzer.fileCounter = fileCounter;
	}
	public static void setStartTime(long data)
	{
		startTime = data;
	}

	public static void updateProgressMessage(String msg)
	{
		progressMessage = msg;
	}
	public static String getProgressMessage(){
		return progressMessage;
	}
	public static void setCurrentFileName(String fileName) {
		currentFileName = fileName;
	}

	public static String getCurrentFileName(){
		return currentFileName;
	}
	public static void stopTimer() {

		if(elapsedTimer != null){
			elapsedTimer.cancel();
		}
		if(calcTimer != null){
			calcTimer.cancel();
		}
		if(processCancelTimer != null){
			processCancelTimer.cancel();
		}
		if(speedTimer != null){
			speedTimer.cancel();
		}
		isTimerStopped = true;
		isCalcTimerStopped = true;
		isprocessCancelTimerStopped = true;
		elapsedTimer = null;
		speedTimer = null;
		calcTimer = null;
		LogUtil.d(TAG,"stop Timer");
	}

	public static void resetValues() {
		LogUtil.d(TAG, "Reset DataspeedAnalyzer variables..");
		TotalSize = 0L;
		fileCounter = 0;
		speed = 0;
		speedinMbps = 0;
		startTime = 0;
		tempTime = 0;
		isDuplicateFile = false;
		elapsedTimer = null;
		speedTimer = null;
		calcTimer = null;
		processCancelTimer = null;
		transferEndTime=0;
		transferStartTime=0;
		currentMediaType = "";
		currentProgressStatus = "";
	}


	public static void displaySpeedDialog(final Activity activity) {
		if (elapsedTimer == null) {
			elapsedTimer = new Timer();
			isTimerStopped = false;
			elapsedTimer.scheduleAtFixedRate(
					new TimerTask() {

						@Override
						public void run() {
							activity.runOnUiThread(new Runnable() {

										@Override
										public void run() {
								if(isTimerStopped){
									return;
								}
								CustomDialogs.updateMFTimeElapsed();
								}
							});
						}
					}, 500, 1000);// This timer update transfer time, so interval always need to be 1000.
		}
		
		if (speedTimer == null) {
			speedTimer = new Timer();
			speedTimer.scheduleAtFixedRate(
					new TimerTask() {

						@Override
						public void run() {
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if(isTimerStopped){
										return;
									}
									tempTime = System.currentTimeMillis();
									if(SocketUtil.getCtAnalyticUtil(null)!=null){
										CustomDialogs.updateMFTransferRate(
												TotalSize,
												SocketUtil.getCtAnalyticUtil(null).getTransferredBytes(),
												tempTime,
												startTime,
												isDuplicateFile,
												currentMediaType,
												currentProgressStatus);

										SocketUtil.getCtAnalyticUtil(null).setTransferDuration(tempTime - startTime);
									}else{
										LogUtil.e(TAG,"CTAnalytic util is null");
									}

								}
							});
						}
					}, 0, 100);
		}
	}

	public static void startCalcProgressDialogTimer(final Activity activity) {
		LogUtil.d(TAG,"cal timer :"+calcTimer);
		if (calcTimer == null) {
			calcTimer = new Timer();
			isCalcTimerStopped = false;
			calcTimer.scheduleAtFixedRate(
					new TimerTask() {

						@Override
						public void run() {
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (isCalcTimerStopped) {
										LogUtil.d(TAG,"Stop running cal progress dialog task.");
										return;
									}
									CustomDialogs.br_loader_specs_text.setVisibility(View.GONE);
									CustomDialogs.br_round_bar.setVisibility(View.VISIBLE);
									CustomDialogs.updateProgressMessage(progressMessage);
									CustomDialogs.bar.setVisibility(View.GONE);
									// Hide cancel button
									CustomDialogs.progressNegButton.setVisibility(View.GONE);
								}
							});
						}
					}, 10, 1000);
		}

	}

	public static void showProgressDialog(String message, Context activity) {
		if(null != activity){
			if (progressDialog != null) {
				if (progressDialog.isShowing()) {
					progressDialog.cancel();
				}
			}
			updateProgressMessage(message);
			LogUtil.d(TAG,"create progress dialog.");
			progressDialog = CustomDialogs.createProgressDialog(INFO, message, activity);
			progressDialog.setCancelable(true);

			if (!progressDialog.isShowing())
				progressDialog.show();
		}

	}

	public static String convertBytesToMegString(long totDataDownloaded) {
		String totalTransferred = "0";

		double convDownloadObj = Utils.bytesToMeg(totDataDownloaded);
		double convSizeObj = Utils.bytesToMeg(DataSpeedAnalyzer.getTotalSize());
		if( convDownloadObj>0 || convSizeObj>0)
		{
			totalTransferred = String.valueOf(convDownloadObj);
		}
		return totalTransferred;
	}


	public static void setProgressbarProperties(String media, String fileName) {
		DataSpeedAnalyzer.setCurrentMediaType(media);
		DataSpeedAnalyzer.setDuplicateFile(false);
		DataSpeedAnalyzer.setCurrentFileName(fileName);
		DataSpeedAnalyzer.setCurrentProgressStatus("1/1");
		/*if(media.equals(VZTransferConstants.SMS_STR)) {
			DataSpeedAnalyzer.updateProgressMessage(VZTransferConstants.RECEIVING_FILE_MSG
					+ media);
		}else {*/
			DataSpeedAnalyzer.updateProgressMessage(VZTransferConstants.RECEIVING_FILE_MSG
					+ media.toLowerCase());
		}
}
