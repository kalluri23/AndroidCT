package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTReceiverAppsListActivity;
import com.verizon.contenttransfer.activity.P2PSetupActivity;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTLandingModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

/**
 * Created by duggipr on 9/14/2016.
 */
public class CTLandingListener implements View.OnClickListener {

    private Activity activity;
    private static final String INFO = "Content Transfer";
    private Dialog mobileHotspotDialog, appDialog, savingDailog;

    private static final String TAG = CTLandingListener.class.getName();


    public CTLandingListener(Activity act) {
        this.activity = act;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == (R.id.search_icon)
                || view.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || view.getId() == (R.id.ct_toolbar_backIV)) {
            ExitContentTransferDialog.showExitDialog(activity, "LandingScreen");
        }

        if (view.getId() == R.id.ct_get_started_button_tv) {
            Utils.resetExitAppFlags(false);
            LogUtil.d(TAG, "some media not saved :" + CTLandingModel.getInstance().isSomeMediaNotSaved());

            if (WifiManagerControl.isPersonalHotsoptOn()) {
                // show dialog.
                showHotspotDialog();
            } else if (CTLandingModel.getInstance().isSomeMediaNotSaved()) {
                //relaunch saving media page
               showMediaSavingPage();
            } else if (ContentPreference.getBooleanValue(activity, ContentPreference.KEEP_APPS, false)) {
                //check whether apps folder is not empty.
                if (Utils.isDirectoryHasFileWithExtension(VZTransferConstants.tempAppsStoragePath,".apk")) {
                    showAppsInstall();
                } else {
                    continueGetStarted();
                }
            } else {
                continueGetStarted();
            }
        }
        if (view.getId() == R.id.aboutTV) {

            PackageInfo pInfo;
            String space = "        ";
            String aboutInfo = "";

            aboutInfo += activity.getString(R.string.build_version) + CTGlobal.getInstance().getBuildVersion();
            aboutInfo += "\n";
            aboutInfo += activity.getString(R.string.build_date) + CTGlobal.getInstance().getBuildDate();

            CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                    aboutInfo,
                    activity,
                    activity.getString(R.string.msg_ok), -1).show();
        }

        if (view.getId() == R.id.privacyTV) {
            String privacyLink = "http://www.verizon.com/privacy";
            Intent linkIntent = new Intent(Intent.ACTION_VIEW);
            linkIntent.setData(Uri.parse(privacyLink));
            activity.startActivity(linkIntent);
        }

    }

    private void showHotspotDialog() {
        mobileHotspotDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.personal_hotspot_on_msg), true, null,
                true, activity.getString(R.string.go_to_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mobileHotspotDialog.dismiss();
                        activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                },
                true, activity.getString(R.string.exit), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mobileHotspotDialog.dismiss();
                        CTGlobal.getInstance().setExitApp(true);
                        activity.finish();
                    }
                });
    }

    private void showAppsInstall() {

        appDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.app_install_header),
                activity.getString(R.string.app_insatll_dialog_message),
                false, null,
                true, activity.getString(R.string.btnYes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appDialog.dismiss();
                        Intent intent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTReceiverAppsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CTGlobal.getInstance().getContentTransferContext().startActivity(intent);
                    }
                },
                true, activity.getString(R.string.btnNo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, false);
                        appDialog.dismiss();
                    }
                }
        );
    }

    private void continueGetStarted() {
        if (Utils.isStandAloneBuild()) {
            if (!ContentPreference.getCtLaunchStatus(activity)) {
                ContentPreference.setCtLaunchStatus(activity, true);
                //CTMdnCaptureDialog.showMDNDialog(activity);
                startP2PSetupActivity();
            } else {
                startP2PSetupActivity();
            }

        } else {
            startP2PSetupActivity();
        }
    }

    private void startP2PSetupActivity() {
        Intent setupIntent = new Intent(activity, P2PSetupActivity.class);
        activity.startActivity(setupIntent);
    }

    private void showMediaSavingPage() {

        savingDailog = CustomDialogs.createDialog(activity, activity.getString(R.string.save_media_header),
                activity.getString(R.string.save_media_dialog_message),
                false, null,
                true,activity.getString(R.string.btnYes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        savingDailog.dismiss();
                        if(Utils.isDirectoryHasFileWithExtension(VZTransferConstants.tempAppsStoragePath,".apk")) {
                            ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, true);
                        }
                        CTLandingModel.getInstance().launchSavingMediaPage();
                    }
                },
                true, activity.getString(R.string.btnNo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Utils.isDirectoryHasFileWithExtension(VZTransferConstants.tempAppsStoragePath,".apk")) {
                            showAppsInstall();
                            ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, true);
                        }
                        Utils.clearAllPreferencesForSavingMedia();
                        savingDailog.dismiss();
                    }
                }
        );
    }



}
