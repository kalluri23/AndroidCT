package com.verizon.contenttransfer.feedback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.activity.ErrorReportActivity;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTErrorReporter implements java.lang.Thread.UncaughtExceptionHandler {
  public static final String TAG = CTErrorReporter.class.getName();
  private String mVersionName;
  private String mPackageName;
  private String mFilePath;
  private String mPhoneModel;
  private String mAndroidVersion;
  private String mBoard;
  private String mBrand;
  // String CPU_ABI;
  private String mDevice;
  private String mDisplay;
  private String mFingerPrint;
  private String mHost;
  private String ID;
  private String mModel;
  private String mProduct;
  private String mTags;
  private long mTime;
  private String mType;
  private String mUser;
  private Thread.UncaughtExceptionHandler mPreviousHandler;
  private static CTErrorReporter mInstance;
  private Activity mContext;
    public static final ArrayList<String> CTPackageListForCrashDialog = new ArrayList<String>() {{
        add("com.verizon.contenttransfer");
    }};
  public CTErrorReporter() {

    LogUtil.d(TAG,"Initializing CTErrorReporter");
  }
  public static CTErrorReporter getInstance() {
    if (mInstance == null) {
      mInstance = new CTErrorReporter();
    }
    return mInstance;
  }
    public void Init(Activity context){
        if(VZTransferConstants.HANDLE_RUNTIME_EXCEPTIONS) {
            mContext = context;
            RecoltInformations(context);
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }
  public void uncaughtException(Thread t, Throwable e) {

    try{
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(new Intent(VZTransferConstants.CONTENT_TRANSFER_STOPPED));
        ContentPreference.putBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_WIFI_CONNECTED, (SetupModel.getInstance().getBackupSSID()!=null && SetupModel.getInstance().getBackupSSID().length()>0?true:false));
        LogUtil.d(TAG, "reading from shared preference :" + ContentPreference.getBooleanValue(CTGlobal.getInstance().getContentTransferContext(), VZTransferConstants.IS_WIFI_CONNECTED, false));
/*        try{
            LogUtil.d(TAG,"reset wifi connection...");
            WifiManagerControl.closePendingAsyncTask();
        }catch (Exception e1){
            LogUtil.d(TAG,"reset wifi connection on app crash :"+e1.getMessage());
        }*/

      StringBuilder report = new StringBuilder();
      Date curDate = new Date();
            report.append("Error Report collected on : ").append(curDate.toString()).append("\n\n")
                    .append("Informations :").append("\n")
                    .append("==============").append("\n\n")
                    .append(createInformationString()).append("\n\n")
                    .append("StackTrace:").append("\n")
                    .append("======= \n");
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      e.printStackTrace(printWriter);
      report.append(result.toString())
              .append("\n\n").append("Cause:").append("\n======= \n");



      // If the exception was thrown in a background thread inside
      // AsyncTask, then the actual exception can be found with getCause
      Throwable cause = e.getCause();
      while (cause != null) {
        cause.printStackTrace(printWriter);
        report.append(result.toString());
        cause = cause.getCause();
      }
      printWriter.close();
      report.append("\n").append("****  End of current Report ***");
      try {
        LogUtil.d(TAG,"log crash report to MVM Server");
        logErrorCrashToServer(e);
      } catch (Exception exp) {
        LogUtil.d(TAG, "mainExcepiton :"+exp.getMessage());
      }

      LogUtil.d(TAG, "handleException calling with msg =" + report.toString());

      //if(VZTransferConstants.CRASH_LOGGRING) {
      //SaveAsFile(report.toString());
      //appendErrorsToFile(report.toString());
      //}

      handleException(e, report.toString());
      LogUtil.d(TAG, "handleException finished");

    }catch (Exception exc){
      LogUtil.d(TAG, "mainExcepiton");
      logErrorCrashToServer(e);
      System.exit(0);
    }finally {
      //we can't put System.exit here because than handleException won't be able to complete.
    }

  }
  private void handleException(final Throwable e, final String message) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        //Looper.prepare();
        try {
          LogUtil.d(TAG,"HandleException.."+e.getMessage());
          if(displayCustomCrashDialog(e)){
            LogUtil.d(TAG,"HandleException..1");
            Intent dialogIntent = new Intent(mContext, ErrorReportActivity.class);
            dialogIntent.putExtra("ERROR_DATA", message);
            LogUtil.d(TAG, "handleException dialog shown ");
            mContext.startActivity(dialogIntent);
            LogUtil.d(TAG, "HandleException..activity launched");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
          }else{
            LogUtil.d(TAG,"HandleException..2");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
          }

        }catch(Exception e){
          LogUtil.d(TAG, "handleException dialog shown exception");
          logErrorCrashToServer(e);
          System.exit(10);
        }finally {
          //we can't put System.exit here because if we disply a custom dialog, it won't last....
        }
      }
    };
    new Thread(runnable).start();
  }
  private boolean displayCustomCrashDialog(Throwable e){

    LogUtil.d(TAG, "displayCustomCrashDialog");
    try{
      StackTraceElement[] stackTraceElementArray = e.getStackTrace();

      for(int i = 0;i<stackTraceElementArray.length;i++) {
        for (String packg : CTPackageListForCrashDialog){
          LogUtil.d(TAG, stackTraceElementArray[i].getClassName());
          if(stackTraceElementArray[i].getClassName().contains(packg)) {
            LogUtil.d(TAG, stackTraceElementArray[i].getClassName());
            LogUtil.d(TAG, packg);
            return true;
          }
        }

      }
    }catch (Exception ex){
      LogUtil.d(TAG, "displayCustomCrashDialog exception ");
      return false;
    }finally {
      LogUtil.d(TAG, "displayCustomCrashDialog finally ");
    }
    return false;
  }
  void RecoltInformations(Context context) {
    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo pi;
      // Version
      pi = pm.getPackageInfo(context.getPackageName(), 0);
      mVersionName = pi.versionName;
      // Package name
      mPackageName = pi.packageName;
      // Files dir for storing the stack traces
      mFilePath = context.getFilesDir().getAbsolutePath();
      // mDevice model
      mPhoneModel = android.os.Build.MODEL;
      // Android version
      mAndroidVersion = android.os.Build.VERSION.RELEASE;

      mBoard = android.os.Build.BOARD;
      mBrand = android.os.Build.BRAND;
      //CPU_ABI = android.os.Build.;
      mDevice = android.os.Build.DEVICE;
      mDisplay = android.os.Build.DISPLAY;
      mFingerPrint = android.os.Build.FINGERPRINT;
      mHost = android.os.Build.HOST;
      ID = android.os.Build.ID;
      //mManufacturer = android.os.Build.;
      mModel = android.os.Build.MODEL;
      mProduct = android.os.Build.PRODUCT;
      mTags = android.os.Build.TAGS;
      mTime = android.os.Build.TIME;
      mType = android.os.Build.TYPE;
      mUser = android.os.Build.USER;

    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }
  public String createInformationString() {
    StringBuilder returnVal = new StringBuilder();
    String ReturnVal = "";
    returnVal.append("current_app_version : ").append(mVersionName).append("\n")
            .append("Package : ").append(mPackageName).append("\n")
            .append("FilePath : ").append(mFilePath).append("\n")
            .append("Phone Model : ").append(mPhoneModel).append("\n")
            .append("Android Version : ").append(mAndroidVersion).append("\n")
            .append("Board : ").append(mBoard).append("\n")
            .append("mBrand : ").append(mBrand).append("\n")
            .append("mDevice : ").append(mDevice).append("\n")
            .append("mDisplay : ").append(mDisplay).append("\n")
            .append("Finger Print : ").append(mFingerPrint).append("\n")
            .append("mHost : ").append(mHost).append("\n")
            .append("ID : ").append(ID).append("\n")
            .append("mModel : ").append(mModel).append("\n")
            .append("mProduct : ").append(mProduct).append("\n")
            .append("mTags : ").append(mTags).append("\n")
            .append("mTime : ").append(mTime).append("\n")
            .append("mType : ").append(mType).append("\n")
            .append("mUser : ").append(mUser).append("\n")
            .append("Total Internal memory : ").append(getTotalInternalMemorySize()).append("\n")
            .append("Available Internal memory : ").append(getAvailableInternalMemorySize()).append("\n");
    return returnVal.toString();
  }
  public long getAvailableInternalMemorySize() {
    File path = Environment.getDataDirectory();
    StatFs stat = new StatFs(path.getPath());
    long blockSize = stat.getBlockSize();
    long availableBlocks = stat.getAvailableBlocks();
    return availableBlocks * blockSize;
  }

  public long getTotalInternalMemorySize() {
    File path = Environment.getDataDirectory();
    StatFs stat = new StatFs(path.getPath());
    long blockSize = stat.getBlockSize();
    long totalBlocks = stat.getBlockCount();
    return totalBlocks * blockSize;
  }

  private void appendErrorsToFile(String ErrorContent) {
    BufferedWriter writer = null;
    try {
      //String errorReportLogFile = VZTransferConstants.VZTRANSFER_DIR+VZTransferConstants.ERROR_REPORT_FILE;
        File transferDir = new File( VZTransferConstants.VZTRANSFER_DIR);

        if ( transferDir.exists() ) {
            LogUtil.d(TAG, "Transfer Directory already exists..");
            if ( ! transferDir.isDirectory() ) {
                transferDir.delete();
                transferDir.mkdirs();
            }
        } else {
            LogUtil.d(TAG, "Transfer directory is not there, creating ");
            //If directory does not exists then crete it
            transferDir.mkdirs();
        }

      File dir = new File(VZTransferConstants.VZTRANSFER_DIR.substring(0, VZTransferConstants.VZTRANSFER_DIR.length() - 1));
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(System.currentTimeMillis()));
      LogUtil.d(TAG,"CT_Logs dir exist ="+dir.exists());
      if (dir.exists() == false)
        dir.mkdir();
      File f = new File(dir, VZTransferConstants.ERROR_REPORT_FILE);
      if (f.exists() == false)
        f.createNewFile();
        if(f.length()>0){
            ErrorContent = VZTransferConstants.CRASH_LOG_DELIMITER + ErrorContent;
        }
      FileWriter file = new FileWriter(f, true);
      writer = new BufferedWriter(file);
      writer.append(ErrorContent.toString());
      writer.append("\n");
      LogUtil.d(TAG, "CT_Logs file wrote successfully.");
      //mHandler.sendEmptyMessage(0);
    } catch (IOException exception) {

    } finally {
      if (writer != null)
        try {
          writer.close();
        } catch (IOException e) {
          Log.e(TAG, e.getMessage(), e);
        }
    }
  }

    private void SaveAsFile(String ErrorContent) {

        BufferedWriter writer = null;
        try {
            File dir = new File(new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .append(File.separator).append("MVM_Logs").toString());
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(System.currentTimeMillis()));
            if (dir.exists() == false)
                dir.mkdir();
            File f = new File(dir, new StringBuilder("mvm_").append("response_").append(timeStamp).append(".stacktrace").toString());
            if (f.exists() == false)
                f.createNewFile();
            FileWriter file = new FileWriter(f, true);
            writer = new BufferedWriter(file);
            writer.append(ErrorContent.toString());
            writer.append("\n");
        } catch (IOException exception) {

        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e("LogUtil", e.getMessage(), e);
                }
        }
    }
  private void logErrorCrashToServer(Throwable e){

       CTCrashReportBean crashReportBean = new CTCrashReportBean();
        crashReportBean.setExceptionReason(e.getMessage());
        //crashReportBean.setMdn(AppContext.getAppContext().getSignedInMDN());
        crashReportBean.setMdn("000-000-0000");
        //crashReportBean.setSessionCookie(ErrorReporterUtils.getJsessionId());
        crashReportBean.setSessionCookie("000000000000000000000000000:000000000");
        crashReportBean.setCrashStack(e.getStackTrace());
        crashReportBean.setDeviceModel(mModel);
        crashReportBean.setOsVersion(mAndroidVersion);
        crashReportBean.setSourceid("ctrc");

        crashReportBean.setTimestamp(Utils.getUTCDate(new Date()));
        crashReportBean.setAppVersion(BuildConfig.VERSION_NAME);
        crashReportBean.setDeviceName(Utils.getDeviceName());
        //log crash as jsonError First
      try{
          String jsonString = getJsonString(crashReportBean);
          appendErrorsToFile(jsonString);
      }catch(Exception ex){
          //
      }
  }

    public static String buildCrashReportJson(List<CTCrashReportBean> myList){
        String jsonString = "";
        CTRequestParameterBean ctRequestParameterBean = new CTRequestParameterBean();
        ctRequestParameterBean.setModel(Build.MODEL);
        ctRequestParameterBean.setApptype("Android");
        ctRequestParameterBean.setRequestParameters("logCrashReport");
        ctRequestParameterBean.setSimOperatorCode("000000");
        ctRequestParameterBean.setSupportlocationservices("true");
        ctRequestParameterBean.setTimeZone("EDT");
        ctRequestParameterBean.setType("android2");
        ctRequestParameterBean.setAppName("contenttransfer");
        ctRequestParameterBean.setCurrentAppVersion(BuildConfig.VERSION_NAME);
        ctRequestParameterBean.setDeviceName(Utils.getDeviceName());
        ctRequestParameterBean.setStaticCacheVersion("1.0");
        ctRequestParameterBean.setDeviceMdn("0000000000");
        ctRequestParameterBean.setFormfactor("handset");
        ctRequestParameterBean.setApplicationId("contenttransfer");
        ctRequestParameterBean.setRequestedPageType("logCrashReport");
        ctRequestParameterBean.setSourceID("ctrc");
        ctRequestParameterBean.setFwVersion("6.0.1");
        ctRequestParameterBean.setNoSimPresent("false");
        ctRequestParameterBean.setOsVersion("6.0.1");
        ctRequestParameterBean.setStaticCacheTimestamp("");
        ctRequestParameterBean.setUpgradeCheckFlag("localDB");
        ctRequestParameterBean.setNetworkOperatorCode("311480");
        ctRequestParameterBean.setOsName("Android");
        ctRequestParameterBean.setApiLevel(String.valueOf(Build.VERSION.SDK_INT));
        ctRequestParameterBean.setBrand("Verizon Wireless");
        ctRequestParameterBean.setDeviceMode("4G");

/*        for(int i=0;i<myList.size();i++){
            LogUtil.d(TAG,"myList["+i+"]="+myList.get(i));

            JSONParser parser = new JSONParser();
            Map<String,List<CTCrashReportBean>> result1 = parser.parse(List.class, String.valueOf(myList.get(i)));
            for (Entry<String, List<Example>> entry : result1.entrySet()) {
                for (Example example : entry.getValue()) {
                    System.out.println("VALUE :->"+ example.getFoo());
                }
            }
        }*/
        //ctRequestParameterBean.setCrashLogsList(myList);

        JSONArray jsonArrayFromList = createJsonArrayFromList(myList);
        ctRequestParameterBean.setCrashLogsList(jsonArrayFromList);
        //log crash as jsonError First
        try{
            //JSONObject mGson = new JSONObject();
            //jsonString = mGson.(ctRequestParameterBean, CTRequestParameterBean.class);
            //CTRequestParameterBean test = new CTRequestParameterBean();
            Map<String, Object> map = ConvertObjectToMap(ctRequestParameterBean);
            JSONObject jsonObject = new JSONObject(map);
            jsonString = jsonObject.toString();
            LogUtil.d(TAG,"Final crash json :"+jsonString);
            LogUtil.d(TAG, "jsonString............2=" + jsonString);
            //appendErrorsToFile(jsonString);
        }catch(Exception ex){
            //
        }

        return jsonString;
    }
    public static JSONArray createJsonArrayFromList(List<CTCrashReportBean> list) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (CTCrashReportBean cTCrashReportBean : list) {
                Map<String, Object> map = ConvertObjectToMap(cTCrashReportBean);
                JSONObject jsonObject = new JSONObject(map);
                jsonArray.add(jsonObject);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static Map<String, Object> ConvertObjectToMap(Object obj) throws
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException {
        Class<?> pomclass = obj.getClass();
        pomclass = obj.getClass();
        Method[] methods = obj.getClass().getMethods();


        Map<String, Object> map = new HashMap<String, Object>();
        for (Method m : methods) {
            if (m.getName().startsWith("get") && !m.getName().startsWith("getClass")) {
                Object value = (Object) m.invoke(obj);
                map.put(m.getName().substring(3), (Object) value);
            }
        }
        return map;
    }
    private String getJsonString(CTCrashReportBean crashReportBean) {
        String jsonString = "";
        try {
            //Gson mGson = new Gson();

            //jsonString = mGson.toJson(crashReportBean, CTCrashReportBean.class);
            Map<String, Object> map = ConvertObjectToMap(crashReportBean);
            JSONObject jsonObject = new JSONObject(map);
            jsonString = jsonObject.toString();
            LogUtil.d(TAG, "jsonString............1=" + jsonString);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonString;
    }
}
