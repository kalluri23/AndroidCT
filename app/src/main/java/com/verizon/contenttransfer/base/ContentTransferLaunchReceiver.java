package com.verizon.contenttransfer.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by duggipr on 2/8/2016.
 */
public class ContentTransferLaunchReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        String mdn = intent.getStringExtra("mdn");

        LogUtil.d("ContentTransferLaunchReceiver", "macId =" + mdn);

        launchApplicationActivity(context,mdn);

    }


    public void launchApplicationActivity (Context context,String mdn){

        //set the launch status, that can be restored later

        if(!ContentPreference.getCtLaunchStatus(context)){

            ContentPreference.setCtLaunchStatus(context,true);
        }

        Utils.resetExitAppFlags(false);
        Intent i = new Intent(context, P2PStartupActivity.class);
        i.putExtra("mdn", mdn);
        i.putExtra("appType",VZTransferConstants.MVM);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }
}
