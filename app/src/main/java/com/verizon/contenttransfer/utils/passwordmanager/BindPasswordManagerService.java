package com.verizon.contenttransfer.utils.passwordmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.Messenger;

import com.verizon.contenttransfer.utils.LogUtil;

import java.util.List;

/**
 * Created by c0bissh on 8/14/2017.
 */

public class BindPasswordManagerService {
    private static final String TAG = BindPasswordManagerService.class.getName();
    private Messenger myService = null;
    private boolean isBound;

    public BindPasswordManagerService(Context context){
        Intent serviceIntent = new Intent("com.verizon.contenttransfer.password.requrest");
        Intent intent  = createExplicitFromImplicitIntent(context, serviceIntent);

        if(intent != null ){
            context.startService(intent);
            context.bindService(intent, myConnection, Context.BIND_DEBUG_UNBIND);
        }
    }


    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            myService = new Messenger(service);
            isBound = true;
            LogUtil.d(TAG,"Service is connected.");
        }

        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            isBound = false;
            LogUtil.d(TAG,"Service is disconnected.");
        }
    };
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
