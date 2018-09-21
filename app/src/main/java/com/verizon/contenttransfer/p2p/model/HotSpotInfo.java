package com.verizon.contenttransfer.p2p.model;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;

/**
 * Created by rahiahm on 3/18/2016.
 */
public class HotSpotInfo {

    private static boolean IS_DEVICE_HOTSPOT = false;
    private static String hotspotSSID = "";
    private static String hotspotPass = "";
    //private static WifiConfiguration netConfig;

    public static String getHotspotSSID() { return hotspotSSID; }

    public static void setHotspotSSID(String hotspotSSID) {
        HotSpotInfo.hotspotSSID = hotspotSSID;
    }

    public static void resetHotspotInfo()
    {
        IS_DEVICE_HOTSPOT = false;
        hotspotPass = "";
        hotspotSSID = "";
    }

    public static String getHotspotPass() {return hotspotPass;}

    public static void setHotspotPass(String hotspotPass) {HotSpotInfo.hotspotPass = hotspotPass;}

    public static boolean isDeviceHotspot() {
        return IS_DEVICE_HOTSPOT;
    }

    public static void setIsDeviceHotspot(boolean isDeviceHotspot) {
        IS_DEVICE_HOTSPOT = isDeviceHotspot;
        if(IS_DEVICE_HOTSPOT) {
            CTGlobal.getInstance().setConnectionType(VZTransferConstants.HOTSPOT_WIFI_CONNECTION);
        }else {
            CTGlobal.getInstance().setConnectionType(VZTransferConstants.PHONE_WIFI_CONNECTION);
        }
    }
}
