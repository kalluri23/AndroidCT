package com.verizon.contenttransfer.model;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;

import java.io.File;
import java.util.List;

/**
 * Created by yempasu on 4/7/2017.
 */
public class CTReceiverAppsListModel {

    private static CTReceiverAppsListModel instance;
    private static final String TAG = CTReceiverAppsListModel.class.getName();
    private Activity activity;

    public static CTReceiverAppsListModel getInstance(){
        if(instance == null){
            instance = new CTReceiverAppsListModel();
        }
        return instance;
    }


    public void initModel(Activity activity) {
        this.activity=activity;
        CTReceiverAppsListView.getInstance().enableInstallButton(CTAppUtil.getInstance().isAnyItemSelected());
    }

    public void onInstallClicked(int position,File apkURL){
        Intent promptInstall = null;
        Uri fileURI = null;

        String authority;
        if(!Utils.isStandAloneBuild()) {
            authority = VZTransferConstants.MY_VERIZON_PKG;
        }else{
            authority = "com.verizon.contenttransfer_standalone";//BuildConfig.APPLICATION_ID;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            fileURI = FileProvider.getUriForFile(activity, authority, apkURL);
            promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(fileURI,
                    "application/vnd.android.package-archive");

            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(promptInstall, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, fileURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else {
            fileURI = Uri.fromFile(apkURL);
            promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(fileURI, "application/vnd.android.package-archive");
        }

        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(promptInstall);
    }
}
