package com.verizon.contenttransfer.p2p.asynctask;

import android.os.AsyncTask;

import com.verizon.contenttransfer.BuildConfig;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.P2PFinishUtil;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;


public class AppAnalyticsAysncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = AppAnalyticsAysncTask.class.getName();

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Start uploading data file in background task.....");

        if(BuildConfig.ENABLE_ANALYTICS)   {
            int count = 0;
            try {
                while(count < 3) {
                    if(ConnectionManager.isConnectedToInternet()) {
                        P2PFinishUtil.getInstance().uploadAppAnalyticFile();
                        break;
                    }else {
                        count ++;
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onPreExecute() {
    }
}
