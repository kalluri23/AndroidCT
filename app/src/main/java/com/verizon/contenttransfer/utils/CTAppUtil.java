package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.verizon.contenttransfer.model.CTReceiverAppsListVO;
import com.verizon.contenttransfer.p2p.asynctask.CreateTransferredAppListTask;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by yempasu on 4/18/2017.
 */
public class CTAppUtil {

    private static CTAppUtil instance;
    private static String TAG = CTAppUtil.class.getName();
    private  Map<String, String> mapPath = new HashMap<String, String>();
    private List<String> installedAppNames;

    private ArrayList<CTReceiverAppsListVO> receivedApps = new ArrayList<CTReceiverAppsListVO>();

    public static CTAppUtil getInstance() {
        if (instance == null) {
            instance = new CTAppUtil();
        }
        return instance;
    }
    public ArrayList<CTReceiverAppsListVO> getReceivedApps() {
        return receivedApps;
    }

    public void setReceivedApps(ArrayList<CTReceiverAppsListVO> receivedApps) {
        this.receivedApps = receivedApps;
    }
    public void addReceivedApps(CTReceiverAppsListVO receivedApp) {
        this.receivedApps.add(receivedApp);
    }
    public List<String> getInstalledAppNames() {
        return installedAppNames;
    }

    public void setInstalledAppNames(List<String> installedAppNames) {
        this.installedAppNames = installedAppNames;
    }
    public void updateCheckedFlag(int index,boolean checked) {
        if(receivedApps != null) {
            if(receivedApps.size()>index){
                CTReceiverAppsListVO appData = receivedApps.get(index);
                appData.setChecked(checked);
            }
        }else {
            LogUtil.d(TAG,"Received apps list is null");
        }
    }

    public void updateInstalledFlag() {
        if(receivedApps != null) {
            //record that we have installed the app
            LogUtil.d(TAG, "installedAppNames list length :" + installedAppNames.size());
            for (CTReceiverAppsListVO appData : receivedApps) {

                if (installedAppNames.contains(appData.getAppName())) {
                    appData.setInstalled(true);
                    LogUtil.d(TAG, "installedAppNames Already installed app  =" + appData.getAppName());
                }else {
                    LogUtil.d(TAG, "installedAppNames not installed app  =" + appData.getAppName());
                }
            }
        }else {
            LogUtil.d(TAG,"Received apps list is null");
        }
    }

    public Boolean isAnyItemSelected() {
        if(receivedApps != null) {
            for (CTReceiverAppsListVO appData : receivedApps) {
                if (appData.isChecked()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addIconPath(String apkpath, String iconpath){
        mapPath.put(apkpath,iconpath);
    }

    public String getIconPath(String apkpath){
        return mapPath.get(apkpath);
    }

    public void reset(){
        mapPath.clear();
    }
    public void clearReceivedApps(){
        receivedApps.clear();
    }
    public boolean installComplete(){
        boolean done = true;
        for(CTReceiverAppsListVO appData: receivedApps){
            if(appData.isInstalled() == false){
                done = false;
            }
        }
        return done;
    }

    public void updateReceivedAppList(Activity activity, boolean shouldExecuteOnResume) {
        CreateTransferredAppListTask createTransferedAppList = new CreateTransferredAppListTask(activity,shouldExecuteOnResume);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            createTransferedAppList.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            createTransferedAppList.execute();
        }
    }

    public void selectAllChange(){
        boolean flag=false;
        for(CTReceiverAppsListVO apps:receivedApps){
            if(!apps.isInstalled()){
                if(apps.isChecked()==false){
                    flag=true;
                    break;
                }
            }
        }
        if(flag){
            CTReceiverAppsListView.getInstance().selectAllTextChange(false);
        }else{
            CTReceiverAppsListView.getInstance().selectAllTextChange(true);
        }
    }
    public boolean appInstalledOrNot(String uri, Activity activity) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            LogUtil.d(TAG,"app "+uri+" is installed");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public boolean showSelectAll(){
        boolean flag=false;
        for(CTReceiverAppsListVO apps: receivedApps) {
            if (!apps.isInstalled()) {
                LogUtil.d(TAG,"Show selectAll");
                flag =true;
                break;
            }
        }
        LogUtil.d(TAG,"selectAll is disabled");
        return flag;
    }

}

