package com.verizon.contenttransfer.model;

import android.graphics.drawable.Drawable;
import java.io.File;

/**
 * Created by yempasu on 4/7/2017.
 */
public class CTReceiverAppsListVO {
    private String appName = null;
    private Drawable appIcon;
    private File path;
    boolean installed;
    boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public boolean isInstalled() {return installed;}

    public void setInstalled(boolean installed) {this.installed = installed;}

}
