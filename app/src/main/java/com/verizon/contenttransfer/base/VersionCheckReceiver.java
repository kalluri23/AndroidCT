package com.verizon.contenttransfer.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.VersionManager;

/**
 * Created by rahiahm on 7/13/2016.
 */
public class VersionCheckReceiver extends BroadcastReceiver {
    private final Activity activity;
    private final String screen;

    public static final String TAG = VersionCheckReceiver.class.getName();
    private Dialog versionMismatchDialog;

    public VersionCheckReceiver(Activity activity, String screen){
        this.activity = activity;
        this.screen = screen;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int result = intent.getIntExtra("resultCode", 0);
        String myVer = CTGlobal.getInstance().isCross()? VersionManager.getMinSupportedVersion() + activity.getString(R.string.higher): CTGlobal.getInstance().getBuildVersion() + ".";
        String otherVer = CTGlobal.getInstance().isCross()?intent.getStringExtra("minSupported") + activity.getString(R.string.higher):intent.getStringExtra("version") + ".";
        LogUtil.d(TAG, " Version Check Receiver - result : " + result);
        if(result == -1) {

            Utils.resetWiFiDirect();
            //The other device needs to upgrade
            versionMismatchDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.version_mismatch_string), activity.getString(R.string.version_update_message1) + myVer, false, null, false, null, null, true,activity.getString(R.string.cancel_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    versionMismatchDialog.dismiss();
                    ExitHandler.performExitTasks(activity, "Pin Screen");
                }
            });
        } else if( result == 1) {
            Utils.resetWiFiDirect();
            //This device needs to upgrade
            versionMismatchDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.version_mismatch_string), activity.getString(R.string.version_update_message2) + otherVer, false, null, true,activity.getString(R.string.upgrade), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.verizon.contenttransfer")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/com.verizon.contenttransfer")));
                    }
                }
            }, true, activity.getString(R.string.cancel_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    versionMismatchDialog.dismiss();
                    ExitHandler.performExitTasks(activity, "Pin Screen");
                }
            });

        }
    }
}
