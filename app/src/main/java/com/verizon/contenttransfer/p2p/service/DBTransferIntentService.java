package com.verizon.contenttransfer.p2p.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;


/**
 * Created by c0bissh on 8/14/2017.
 */

public class DBTransferIntentService extends Service {

    private static final String TAG = DBTransferIntentService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG,"service is created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG,"service  onStartCommand - password file is ready to read.");
        CTGlobal.getInstance().setPasswordManagerDBCopied(true);
        stopSelf();
        LogUtil.d(TAG,"Stop self is called.");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG,"service  onDestroy.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG,"service  onBind.");
        return null;
    }
}