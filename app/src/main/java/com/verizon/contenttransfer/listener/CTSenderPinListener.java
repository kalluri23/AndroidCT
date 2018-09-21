package com.verizon.contenttransfer.listener;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adobe.CTSiteCatConstants;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.exceptions.SiteCatLogException;
import com.verizon.contenttransfer.model.CTSenderPinModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.PinManager;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.VerificationCodeEditText;
import com.verizon.contenttransfer.view.CTSenderPinView;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.util.HashMap;

/**
 * Created by duggipr on 9/6/2016.
 */
public class CTSenderPinListener implements View.OnClickListener{

    private static String TAG = CTSenderPinListener.class.getName();
    private static final String INFO = "Content Transfer";
    private Activity activity;
    CTSenderPinModel p2pSenderModel;
    private CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();


    public CTSenderPinListener(Activity act) {
        this.activity = act;
        p2pSenderModel = new CTSenderPinModel(act);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == (R.id.search_icon)
                || v.getId() == (R.id.ct_toolbar_hamburger_menuIV)
                || v.getId() == (R.id.ct_toolbar_backIV)){
            ExitContentTransferDialog.showExitDialog(activity, "PinScreen");
        }

        if(v.getId() == R.id.ct_enter_pin_cancel_button_tv){
            activity.finish();
        }

        if(v.getId() == R.id.ct_enter_pin_next_button_tv){

            String et1 = String.valueOf(((VerificationCodeEditText)activity.findViewById(R.id.et1)).getText());
            String et2 = String.valueOf(((VerificationCodeEditText)activity.findViewById(R.id.et2)).getText());
            String et3 = String.valueOf(((VerificationCodeEditText)activity.findViewById(R.id.et3)).getText());
            String et4 = String.valueOf(((VerificationCodeEditText)activity.findViewById(R.id.et4)).getText());

            String enteredPin=et1+et2+et3+et4;
            if (enteredPin.trim().length() > 0) {
                if(enteredPin.trim().length() < 4){
                    CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                            activity.getString(R.string.invalid_pin),
                            activity,
                            activity.getString(R.string.msg_ok), -1).show();
                } else {
                    CTSenderPinView.getInstance().enableConnect(false);
                    PinManager pinManager = new PinManager();
                    String decodedPin = pinManager.decodePin(enteredPin.trim());
                    LogUtil.d(TAG, "Decoded Pin Val : " + decodedPin);

                    //p2pSenderModel.showConnectDialog();
                    CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.configuring_device),activity,false);
                    String localip = ConnectionManager.getLocalIpAddress();
                    String[] parts = localip.split("\\.");
                    String remoteip = "";
                    String alternateIp = ""; 

                    remoteip = parts[0] + "." + parts[1] + "." + parts[2] + "." + decodedPin;

                    if(CTGlobal.getInstance().getHotspotName().equals("Verizon Guest Wi-Fi")){
                        if(parts[2].equals("98")){
                            alternateIp = parts[0] + "." + parts[1] + ".99." + decodedPin;
                        }
                        else if (parts[2].equals("99")){
                            alternateIp = parts[0] + "." + parts[1] + ".98." + decodedPin;
                        }
                    }

                    remoteip = remoteip.trim();
                    LogUtil.d(TAG, "Local Ip " + localip);
                    LogUtil.d(TAG, "Remote Ip " + remoteip);
/*                    if(VZTransferConstants.SUPPORT_ONE_TO_MANY){*/
/*                        Intent intent = new Intent(activity, CTCreateClient.class);
                        intent.setAction(VZTransferConstants.ACTION_HAND_SHAKE);
                        intent.putExtra(VZTransferConstants.PAIRING_CONNECTION_TYPE, VZTransferConstants.PHONE_WIFI_CONNECTION);
                        intent.putExtra(VZTransferConstants.EXTRAS_GROUP_OWNER_ADDRESS, remoteip);
                        intent.putExtra(VZTransferConstants.EXTRAS_ALT_GROUP_OWNER_ADDRESS, alternateIp);

                        activity.startService(intent);*/

                        Utils.CreateClientConnectionObject(VZTransferConstants.PHONE_WIFI_CONNECTION, remoteip, alternateIp, activity);

/*                    }else {
                        Intent intent = new Intent(activity, P2PClient.class);
                        intent.setAction(VZTransferConstants.ACTION_HAND_SHAKE);
                        intent.putExtra(VZTransferConstants.EXTRAS_GROUP_OWNER_ADDRESS, remoteip);
                        intent.putExtra(VZTransferConstants.EXTRAS_ALT_GROUP_OWNER_ADDRESS, alternateIp);

                        activity.startService(intent);
                    }*/
                }
            } else {
                CustomDialogs.MultiLineAlertDialogWithDismissBtn(INFO,
                        activity.getString( R.string.enter_valid_pin),
                        activity,
                        activity.getString(R.string.msg_ok), -1).show();
            }

            HashMap<String, Object> phoneSelectMap = new HashMap();
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_LINKNAME, CTSiteCatConstants.SITECAT_VALUE_CONNECT_USING_PIN);
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_PAGELINK, CTSiteCatConstants.SITECAT_VALUE_PHONE_PIN
                    + CTSiteCatConstants.SITECAT_VALUE_DELIMITER
                    + CTSiteCatConstants.SITECAT_VALUE_CONNECT_USING_PIN);
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_FLOW_INITIATED, CTSiteCatConstants.SITECAT_VALUE_FLOW_INITIATED);
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_FLOWNAME, CTSiteCatConstants.SITECAT_VALUE_PAIRING_OF_SENDER_RECEIVER);
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_TRANSACTIONID, CTGlobal.getInstance().getDeviceUUID());
            phoneSelectMap.put(CTSiteCatConstants.SITECAT_KEY_SENDERRECEIVER, CTSiteCatConstants.SITECAT_VALUE_SENDER);

            try {
                iCTSiteCat.getInstance().trackAction(CTSiteCatConstants.SITECAT_VALUE_CONNECT_USING_PIN, phoneSelectMap);
            } catch (SiteCatLogException e) {
                LogUtil.e(TAG, e.getMessage());
            }
        }
    }
}
