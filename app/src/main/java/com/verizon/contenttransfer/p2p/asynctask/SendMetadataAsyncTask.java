package com.verizon.contenttransfer.p2p.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.verizon.contenttransfer.p2p.sender.SendMetadata;
import com.verizon.contenttransfer.utils.LogUtil;

/**
 * Created by c0bissh on 8/8/2016.
 */
public class SendMetadataAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = SendMetadataAsyncTask.class.getName();
    Context ctxt;

    public SendMetadataAsyncTask(Context context) {
        ctxt = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Checking status on data collection.....");
        SendMetadata.sendMetadata();
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {

    }
}
