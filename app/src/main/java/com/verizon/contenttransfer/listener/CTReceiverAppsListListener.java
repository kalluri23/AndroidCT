package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTReceiverAppsListModel;
import com.verizon.contenttransfer.model.CTReceiverAppsListVO;
import com.verizon.contenttransfer.model.CTReceiverModel;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;

import java.util.ArrayList;

/**
 * Created by rahiahm on 4/21/2017.
 */
public class CTReceiverAppsListListener implements OnClickListener{

    private Activity activity;
    ArrayList<CTReceiverAppsListVO> receivedApps= CTAppUtil.getInstance().getReceivedApps();

    public CTReceiverAppsListListener(Activity act) {this.activity = act;}

    @Override
    public void onClick(View v) {
        if(v == activity.findViewById(R.id.search_icon)
                || v == activity.findViewById(R.id.ct_toolbar_hamburger_menuIV)
                || v == activity.findViewById(R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "ReceiveDataScreen");
        }

        if(v == activity.findViewById(R.id.ct_select_all_apps)) {
            TextView textView = (TextView) activity.findViewById(R.id.ct_select_all_apps);
            if (textView != null){
                if (textView.getText().equals(activity.getString(R.string.select_all))) {
                    CTReceiverAppsListView.getInstance().selectAllTextChange(true);
                    for (CTReceiverAppsListVO x : receivedApps) {
                        if (!x.isInstalled()) {
                            x.setChecked(true);
                            CTAppUtil.getInstance().updateCheckedFlag(0, true);
                        }
                    }
                } else {
                    CTReceiverAppsListView.getInstance().selectAllTextChange(false);
                    for (CTReceiverAppsListVO x : receivedApps) {
                        if (!x.isInstalled()) {
                            x.setChecked(false);
                            CTAppUtil.getInstance().updateCheckedFlag(0, false);
                        }
                    }

                }
            }
            CTReceiverAppsListView.getInstance().callNotifyDataSetChanged();
            CTReceiverAppsListView.getInstance().enableInstallButton(CTAppUtil.getInstance().isAnyItemSelected());
        }

        if(v == activity.findViewById(R.id.ct_btn_done)){
            if(CTAppUtil.getInstance().installComplete()){
                ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, false);
                finishInstall();
            }
            else{
                CustomDialogs.createDialog(activity, activity.getString(R.string.app_install_incomplete),
                        activity.getString(R.string.app_install_incomplete_message),
                        false, null,
                        true, activity.getString(R.string.btnYes), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, true);
                                finishInstall();
                            }
                        },
                        true,activity.getString(R.string.btnNo),  new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, false);
                                finishInstall();
                            }
                        }
                );
            }
        }

        if(v == activity.findViewById(R.id.install_all)) {
            for (CTReceiverAppsListVO z:receivedApps) {
                if (z.isChecked() && !z.isInstalled()) {
                    CTReceiverAppsListModel.getInstance().onInstallClicked(0, z.getPath());
                    CTReceiverAppsListView.getInstance().callNotifyDataSetChanged();
                }
                z.setChecked(false);
                CTReceiverAppsListView.getInstance().enableInstallButton(CTAppUtil.getInstance().isAnyItemSelected());
                CTReceiverAppsListView.getInstance().selectAllTextChange(false);
            }
        }

    }

    private void finishInstall(){
        String msg = CTReceiverAppsListView.getInstance().getIntentMessage(VZTransferConstants.MESSAGE_KEY);
        if(msg != null &&
                msg.equals(VZTransferConstants.TRANSFER_FINISH_HEADER)){
            CTReceiverModel.getInstance().launchFinishActivity();
        }else {
            activity.finish();
        }
    }
}

