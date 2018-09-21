package com.verizon.contenttransfer.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class ContentPreference {

	public static final String PREFS_NAME = "CTDATA";
	public static final String UUIDDATA = "uuiddata";
	public static final String PILOT_STATUS = "pilotstatus";
	public static final String CT_MDN="ctmdn";
	public static final String CTLAUNCH="ctlaunch";
	public static final String KEEP_APPS="ctkeepapps";
	public static final String TAG = "ContentPreference";

	public static String getStringValue(Context ctx, String key,String defaultValue) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		return settings.getString(key, defaultValue);
	}
	public static boolean getBooleanValue(Context ctx, String key,boolean defaultValue) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		return settings.getBoolean(key, defaultValue);
	}
	public static void putStringValue(Context ctx, String key,String value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			editor.apply();
		} else {
			editor.commit();
		}
	}
	public static void putBooleanValue(Context ctx, String key,boolean value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			editor.apply();
		} else {
			editor.commit();
		}
	}
	public static void setCtLaunchStatus(Context ctx, boolean value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(CTLAUNCH, value);
		editor.commit();
	}

	public static boolean getCtLaunchStatus(Context ctx) {
		SharedPreferences tcdata = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		boolean value = tcdata.getBoolean(CTLAUNCH, false);
		return value;
	}

/*
	*//**
	 * asynchronous save operation on api lvl 9+
	 *
	 * @param key
	 * @param value
	 * @param overwrite
	 * @return always returns true
	 *//*
	public static boolean saveSetting(String key, String value, boolean overwrite, Context ctx) {
		//SharedPreferences sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			sharedPreferences.edit().putString(key, value).apply();
		} else {
			sharedPreferences.edit().putString(key, value).commit();
		}
		return true;
	}

	public static void setSharedPreferences(SharedPreferences sPreferences){
		sharedPreferences = sPreferences;
	}
	public static boolean isSettingExist(Context ctx, String key) {
		//SharedPreferences sharedPreferences = P2PStartupActivity.contentTransferContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.contains(key);
	}
	public static String getSettingString(Context ctx, String key) {
		//SharedPreferences sharedPreferences = P2PStartupActivity.contentTransferContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, null);
	}
	public static boolean removeSetting(Context ctx, String key) {
		//SharedPreferences sharedPreferences = P2PStartupActivity.contentTransferContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		UtilLog.d(TAG, "Removing KEY >>>> " + key);
		if (key == null || key.equals("") || !isSettingExist(ctx, key))
			return false;
		boolean isRemoved = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			sharedPreferences.edit().remove(key).apply();
		} else {
			isRemoved = sharedPreferences.edit().remove(key).commit();
		}
		UtilLog.d(TAG, "isRemoved >>>>>>>> " + isRemoved);
		return isRemoved;
	}*/
}
