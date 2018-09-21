package com.verizon.contenttransfer.feedback;

import android.os.AsyncTask;
import android.util.Log;

import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CrashReportUploadAysncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = CrashReportUploadAysncTask.class.getName();
    public static boolean isCrashReportAysncTaskRunning = false;
    private String uploadLog = "";
    private String HTTP_URL = "";
    //

    public CrashReportUploadAysncTask(String fileContent) {
        LogUtil.d(TAG, "requestContentFromServer>>>>>>>>>>>");
        uploadLog=fileContent;
        LogUtil.d(TAG, "Error Report sending to server.. finalPayload:" + uploadLog);
       // pageType = pageType.trim().replace(" ", "%20");

        if(BuildConfig.ENABLE_CRASHLOG) {
            if(BuildConfig.DEBUG_LOG) {
                HTTP_URL = VZTransferConstants.CRASH_REPORT_DEV_URL;
            }else {
                HTTP_URL = VZTransferConstants.CRASH_REPORT_PROD_URL;
            }
        }
        //HTTP_URL = VZTransferConstants.CRASH_REPORT_URL;
        LogUtil.d(TAG, "url for request:" + HTTP_URL);

    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Start uploading data file in background task.....");

        if (VZTransferConstants.CRASH_LOGGING) {

            String errorFileContent = Utils.readFileContent(new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.ERROR_REPORT_FILE));
            if(errorFileContent.length()>0){
                //CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();

                List<String> myList = new ArrayList<String>(Arrays.asList(errorFileContent.split(VZTransferConstants.CRASH_LOG_DELIMITER)));
                List<CTCrashReportBean> ctCrashReportBeanList = new ArrayList<CTCrashReportBean>();
                for(int i=0;i<myList.size();i++){
                    try {
                        JSONObject errorLog = new JSONObject(myList.get(i));
                        CTCrashReportBean ctCrashReportBean = new CTCrashReportBean();
                        ctCrashReportBean.setAppVersion(errorLog.getString("AppVersion"));
                        ctCrashReportBean.setDeviceModel(errorLog.getString("DeviceModel"));
                        ctCrashReportBean.setExceptionReason(errorLog.getString("ExceptionReason"));
                        ctCrashReportBean.setMdn(errorLog.getString("Mdn"));
                        ctCrashReportBean.setOsVersion(errorLog.getString("OsVersion"));
                        ctCrashReportBean.setSessionCookie(errorLog.getString("SessionCookie"));
                        ctCrashReportBean.setSourceid(errorLog.getString("Sourceid"));
                        //SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
/*                        try {
                            //iCTSiteCat.getInstance().trackStateGlobal(CTSiteCatConstants.SITECAT_VALUE_TRANSFER_CRASHED, null);
                            HashMap<String, Object> transferCrashedMap = new HashMap();
                            String excReason  = errorLog.getString("ExceptionReason");
                            if(excReason != null){
                                if(excReason.length()>250){
                                    excReason = excReason.substring(0,250);
                                }
                            }
                            transferCrashedMap.put(CTSiteCatConstants.SITECAT_KEY_ERROR_MESSAGE, excReason);

                            iCTSiteCat.getInstance().trackAction(CTSiteCatConstants.SITECAT_VALUE_ACTION_APPCRASHED, transferCrashedMap);
                        } catch (SiteCatLogException e) {
                            e.printStackTrace();
                        }*/
                        try {
                            ctCrashReportBean.setTimestamp(Utils.StringToUTCDate(errorLog.getString("Timestamp")));
                        } catch (Exception e) {
                            LogUtil.d(TAG, "Set time stamp exception :" + e.getMessage());
                            //e.printStackTrace();
                        }
                        List<String> crashlist = new ArrayList<String>();
                        JSONArray jArray  = (JSONArray)errorLog.getJSONArray("CrashStack");
                        if (jArray != null) {
                            for (int j=0;j<jArray.length();j++){
                                crashlist.add(jArray.get(j).toString());
                            }
                        }
                        ctCrashReportBean.setCrashStack(crashlist);

                        ctCrashReportBeanList.add(ctCrashReportBean);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                makePostRequest(HTTP_URL, CTErrorReporter.buildCrashReportJson(ctCrashReportBeanList));
            }
        }
        return null;
    }

    private void makePostRequest(String strUrl, String payload) {
        LogUtil.d(TAG,"Url="+strUrl);
        LogUtil.d(TAG,"Payload ="+payload);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
            HttpResponse response;
            LogUtil.d(TAG,"test 1 ");

            try {
                HttpPost post = new HttpPost(strUrl);
                LogUtil.d(TAG,"test 2 ");
                StringEntity se = new StringEntity(payload);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                LogUtil.d(TAG,"test 3");
                    /*Checking response */
                if(response!=null){
                    String result = EntityUtils.toString(response.getEntity());

                    Log.i(TAG, "response :"+result);
                    isCrashReportAysncTaskRunning = false;
                    if(result!=null)//Do your validation for result
                    {
                        try{
                            //JSONArray jArray = new JSONArray(result); // here if the result is null an exeption will occur
                            JSONObject json_data = new JSONObject(result);
                            JSONObject json_ErrInfo = json_data.getJSONObject("ErrInfo");
                            if(json_ErrInfo.getString("errMsg").equals("Success")){
                                LogUtil.d(TAG,"Successfully uploaded Crash File...Now deleting it.");
                                File errorFile = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.ERROR_REPORT_FILE);
                                if (errorFile != null && errorFile.exists()) {
                                    errorFile.delete();
                                    LogUtil.d(TAG,"Crash File deleted successfully.");
                                }
                            }
                        }
                        catch(JSONException e){
                            Log.e(TAG, "parsing  error " + e.toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, "Cannot Estabilish Connection");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    protected void onPostExecute(String result) {

        isCrashReportAysncTaskRunning = false;
/*        if(result!=null)//Do your validation for result
        {
            try{
                //JSONArray jArray = new JSONArray(result); // here if the result is null an exeption will occur
                JSONObject json_data = new JSONObject(result);
                JSONObject json_ErrInfo = json_data.getJSONObject("ErrInfo");
                if(json_ErrInfo.getString("errMsg").equals("Success")){
                    LogUtil.d(TAG,"Successfully uploaded Crash File...Now deleting it.");
                    File errorFile = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.ERROR_REPORT_FILE);
                    if (errorFile != null && errorFile.exists()) {
                        errorFile.delete();
                        LogUtil.d(TAG,"Crash File deleted successfully.");
                    }
                }
            }
            catch(JSONException e){
                Log.e(TAG, "parsing  error " + e.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/

    }

    @Override
    protected void onPreExecute() {
        isCrashReportAysncTaskRunning = true;
    }
}
