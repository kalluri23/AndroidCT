package com.verizon.contenttransfer.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.QRCodeVO;

/**
 * Created by duggipr on 9/6/2016.
 */
public class CTRouterConnectionQRUtil {

    private static String TAG = CTRouterConnectionQRUtil.class.getName();
    private static final String INFO = "Content Transfer";
    private BroadcastReceiver versionCheckReciever = null;
    private static CTRouterConnectionQRUtil instance = null;

    public CTRouterConnectionQRUtil() {
    }
    public static CTRouterConnectionQRUtil getInstance() {
        if (instance == null) {
            instance = new CTRouterConnectionQRUtil();
        }
        return instance;
    }

    public void connectRouter(Activity activity, QRCodeVO qrCodeVO) {
        LogUtil.d(TAG,"Connect to router.");
        //if(CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.OLD_PHONE)) {
        //if(Utils.isCameraOpened(VZTransferConstants.p2PWifiSetupActivity)) {
        if(!Utils.isThisServer()) {

            //String[] remoteParts = qrCodeVO.getIpaddress().split("\\.");
            String localip = getLocalIpAddress(activity);
            LogUtil.d(TAG, "Local ip :" + localip + "  remote id :" + qrCodeVO.getIpaddress());
            String[] parts = qrCodeVO.getIpaddress().split("\\.");//localip.split("\\.");
            String remoteip = "";
            String alternateIp = "";

            remoteip = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];

            if (CTGlobal.getInstance().getStoreMacId() != null
                    && CTGlobal.getInstance().getHotspotName().equals(VZTransferConstants.VERIZON_GUEST_WIFI)) {
                if(parts[2].equals("98")){
                    alternateIp = parts[0] + "." + parts[1] + ".99." + parts[3];
                }
                else if (parts[2].equals("99")){
                    alternateIp = parts[0] + "." + parts[1] + ".98." + parts[3];
                }
            }

            remoteip = remoteip.trim();
            LogUtil.d(TAG, "Local Ip " + getLocalIpAddress(activity));
            LogUtil.d(TAG, "Remote Ip " + remoteip);

/*            if(VZTransferConstants.SUPPORT_ONE_TO_MANY){*/
                Utils.CreateClientConnectionObject(VZTransferConstants.PHONE_WIFI_CONNECTION, remoteip, alternateIp,activity);

/*            }else {
                Intent intent = new Intent(activity, P2PClient.class);
                intent.setAction(VZTransferConstants.ACTION_HAND_SHAKE);
                intent.putExtra(VZTransferConstants.EXTRAS_GROUP_OWNER_ADDRESS, remoteip);
                intent.putExtra(VZTransferConstants.EXTRAS_ALT_GROUP_OWNER_ADDRESS, alternateIp);
                LogUtil.d(TAG, "Lanching P2P Client");

                activity.startService(intent);
            }*/

        }
    }

    public String getLocalIpAddress(Activity activity) {
        WifiManager wifiMan = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();

        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }
}
