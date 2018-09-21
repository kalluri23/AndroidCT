package com.verizon.contenttransfer.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahiahm on 9/2/2016.
 */
public class CTPermissonCheck {

    public static final int MY_PERMISSIONS_REQUEST = 100;
    public static final String TAG = CTPermissonCheck.class.getName();

    @TargetApi(Build.VERSION_CODES.M)
    public static void checkPermission(Activity activity){
        // Here, thisActivity is the current activity
        final List<String> permissions = new ArrayList<String>();

        if (Utils.checkPermissionStatus(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_CONTACTS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CONTACTS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_SMS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_MMS);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_CALL_LOG);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_CALENDAR);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CALENDAR);
        }
        if(Utils.checkPermissionStatus(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if( !permissions.isEmpty() ) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[permissions.size()]), MY_PERMISSIONS_REQUEST);
        }
    }
}
