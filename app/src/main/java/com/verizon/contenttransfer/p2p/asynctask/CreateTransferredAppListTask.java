package com.verizon.contenttransfer.p2p.asynctask;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverAppsListVO;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;


public class CreateTransferredAppListTask extends AsyncTask<Void, CTReceiverAppsListVO, String> {

    private static final String TAG = CreateTransferredAppListTask.class.getName();
    private Activity activity;
    private boolean shouldExecuteOnResume;
    public CreateTransferredAppListTask(Activity activity, boolean shouldExecuteOnResume) {
        this.activity = activity;
        this.shouldExecuteOnResume = shouldExecuteOnResume;
    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Starting create app list....."+shouldExecuteOnResume);
        Utils.getAllApps(CTGlobal.getInstance().getContentTransferContext());
        publishProgress(new CTReceiverAppsListVO());
        if(!shouldExecuteOnResume) {
            String path = VZTransferConstants.tempAppsStoragePath ;
            LogUtil.d(TAG, "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            LogUtil.d(TAG, "Size: " + files.length);
            PackageManager pm = activity.getPackageManager();
            for (int i = 0; i < files.length; i++)
            {
                CTReceiverAppsListVO ctReceiverAppsListVO = new CTReceiverAppsListVO();
                String APKFilePath = files[i].getPath();
                LogUtil.d(TAG, "Apk file path :"+APKFilePath);
                PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);
                if(null != pi){
                    pi.applicationInfo.sourceDir       = APKFilePath;
                    pi.applicationInfo.publicSourceDir = APKFilePath;
                    Drawable apkIcon = pi.applicationInfo.loadIcon(pm);
                    String appName = pi.applicationInfo.loadLabel(pm).toString();

                    ctReceiverAppsListVO.setAppIcon(apkIcon);
                    ctReceiverAppsListVO.setAppName(appName);
                    File apkURL = new File(files[i].getAbsolutePath());
                    ctReceiverAppsListVO.setPath(apkURL);
                    publishProgress(ctReceiverAppsListVO);
                }
            }
        }

        LogUtil.d(TAG, "getAvailable Apps completed.");
        return null;
    }

    @Override
    protected void onProgressUpdate(CTReceiverAppsListVO... values) {
        super.onProgressUpdate(values);
        if(values[0].getAppName() != null){
            LogUtil.d(TAG, "onProgressUpdate :" + values[0].getAppName());
            CTAppUtil.getInstance().addReceivedApps(values[0]);
        }else{
            if(!shouldExecuteOnResume) {
                CTAppUtil.getInstance().clearReceivedApps();
            }
        }
        CTAppUtil.getInstance().updateInstalledFlag();
        sort();
        CTReceiverAppsListView.getInstance().callNotifyDataSetChanged();
    }
    public void sort() {
        Collections.sort(CTAppUtil.getInstance().getReceivedApps(), new Comparator<CTReceiverAppsListVO>() {
            @Override
            public int compare(CTReceiverAppsListVO item1, CTReceiverAppsListVO item2) {
                if(item1.isInstalled()){
                    return 0;
                }else {
                    return -1;
                }
            }
        });
    }
    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG, "onPostExecute");
        CustomDialogs.dismissDefaultProgressDialog();
    }

    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG,"On PreExecute");
        CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.preparing_apps_wait),activity,false);
    }

}
