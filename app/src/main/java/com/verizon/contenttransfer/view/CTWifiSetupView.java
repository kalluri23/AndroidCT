package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTWifiSetupListener;
import com.verizon.contenttransfer.model.CTWifiSetupModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTWifiSetupView {
    private static String TAG = CTWifiSetupView.class.getName();
    private TextView networkHeading;
    private TextView wifiSSID;
    private TextView p2pWifipass;
    private TextView p2pWifikey;
    private TextView wifiMessageTV;
    private TextView totConnected;
    private TextView networkNameTV;
    private TextView oneToManyNextBtn;
   // private ImageView wifiPassLockIV; //removed with brand refresh
    private ImageView qrcodeIV;
    //private RelativeLayout passwordDivider;
    private View passwordDivider;
    private View hotspotPswLayout;
    private View noInternetAlertLayout;
    //private LinearLayout wifiSettingBtn;
    //private LinearLayout nextBtn;
    private TextView wifiSettingBtn, manualBtn, maxConnectionMsg;
    private TextView tryAnotherWayBtn;
    private TextView nextBtn;
    //private boolean isNew;

    private static CTWifiSetupView instance;
    private Activity activity;

    public void initView(Activity activity) {
        this.activity = activity;
        initialize(activity);

    }

    public static CTWifiSetupView getInstance() {
        if (instance == null) {
            instance = new CTWifiSetupView();
        }
        return instance;
    }

    public static void killInstance() {
        instance = null;
    }

    private void initialize(final Activity activity) {

        if (QRCodeUtil.getInstance().isUsingQRCode()) {
            activity.setContentView(R.layout.ct_wifi_network_layout_qr);
        } else {
            activity.setContentView(R.layout.ct_wifi_network_layout);
        }
        networkHeading = (TextView) activity.findViewById(R.id.ct_disc_hotspot_heading);
        wifiMessageTV = (TextView) activity.findViewById(R.id.ct_disc_hotspot_info);
        networkNameTV = (TextView) activity.findViewById(R.id.ct_hotspot_disc_name_tv);
        wifiSSID = (TextView) activity.findViewById(R.id.ct_hotspot_disc_conn_status);


        passwordDivider = (View) activity.findViewById(R.id.ct_hotspot_password_divider);

       // wifiPassLockIV = (ImageView) activity.findViewById(R.id.ct_hotspot_disc_iv_pwd);
        p2pWifipass = (TextView) activity.findViewById(R.id.ct_hotspot_tv_pwd_txt);
        p2pWifikey = (TextView) activity.findViewById(R.id.ct_hotspot_tv_pwd);

        hotspotPswLayout = activity.findViewById(R.id.ct_hotspot_disc_pwd_container);
        wifiSettingBtn = (TextView) activity.findViewById(R.id.ct_same_wifi_network_btn_wifi_settings);
        nextBtn = (TextView) activity.findViewById(R.id.ct_same_wifi_network_btn_next);
        totConnected = (TextView) activity.findViewById(R.id.ct_total_conn_count);
        oneToManyNextBtn = (TextView) activity.findViewById(R.id.ct_one_to_many_next_btn);

        manualBtn = (TextView) activity.findViewById(R.id.ct_manual_btn);
        tryAnotherWayBtn = (TextView) activity.findViewById(R.id.ct_try_another_way_btn);
        qrcodeIV = (ImageView) activity.findViewById(R.id.qrcodePreview_iv);
        noInternetAlertLayout =  activity.findViewById(R.id.ct_hotspot_no_internet_alert_msg_tv);
        Typeface tf = Typeface.createFromAsset(activity.getAssets(), "VeraMono.ttf");
        p2pWifikey.setTypeface(tf);

        if (Utils.isStandAloneBuild()) {
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }

        ((TextView) activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_pairing);

        View.OnClickListener listener = new CTWifiSetupListener(activity);

        wifiSettingBtn.setOnClickListener(listener);
        oneToManyNextBtn.setOnClickListener(listener);

        if (QRCodeUtil.getInstance().isUsingQRCode()) {
            manualBtn.setOnClickListener(listener);
            tryAnotherWayBtn.setOnClickListener(listener);
        } else {
            nextBtn.setOnClickListener(listener);
        }

        if(CTGlobal.getInstance().isDoingOneToMany() && !Utils.isReceiverDevice()){
            totConnected.setVisibility(View.VISIBLE);
            maxConnectionMsg = (TextView) activity.findViewById(R.id.ct_one_to_many_max_device_connection);
            if(maxConnectionMsg != null) {
                maxConnectionMsg.setVisibility(View.VISIBLE);
            }
        }
        activity.findViewById(R.id.search_icon).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
    }

    public void enableOneToManyNext(int count){
        totConnected.setText(activity.getString(R.string.total_connected_devices) + count);
        if (count > 0) {
            activity.findViewById(R.id.setting_btn_group).setVisibility(View.GONE);
            activity.findViewById(R.id.next_btn_group).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.setting_btn_group).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.next_btn_group).setVisibility(View.GONE);
        }

    }
    public void toggleConnectionCounterView(boolean flag){
        if(flag) {
            totConnected.setText(activity.getString(R.string.total_connected_devices) + "0");
            totConnected.setVisibility(View.VISIBLE);
        }else {
            totConnected.setVisibility(View.GONE);
        }
    }
    public void wifiUIReset() {
        if (QRCodeUtil.getInstance().isUsingQRCode()) {
            networkNameTV.setText(R.string.qr_network_name_connected);
        }
        else {
            networkNameTV.setText(R.string.heading_network_name_ctsame_wifi_network);
        }

        if (passwordDivider != null)
            passwordDivider.setVisibility(View.GONE);
        if (hotspotPswLayout != null)
            hotspotPswLayout.setVisibility(View.INVISIBLE);
        p2pWifipass.setVisibility(View.INVISIBLE);
        p2pWifikey.setVisibility(View.INVISIBLE);
        wifiSSID.setText(R.string.wifi_not_connected);
    }

    public void updateConnectionUI(String ssid, boolean displayQrCode) {
        if (!displayQrCode
                && CTGlobal.getInstance().isCross()
                && CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.OLD_PHONE)) {
            if (null != tryAnotherWayBtn) {
                LogUtil.d(TAG, "Display try another way btn.");
                tryAnotherWayBtn.setVisibility(View.VISIBLE);
            }
        }

        String ssidConnected = "";
        if (QRCodeUtil.getInstance().isUsingQRCode()) {
            LogUtil.d(TAG,"hide qr code img.???????????");
            qrcodeIV.setVisibility(View.GONE);
        } else {
            LogUtil.d(TAG,"show wifiSettingBtn.");
            wifiSettingBtn.setVisibility(View.VISIBLE);
        }
        if(maxConnectionMsg != null) {
            String myFormattedString = "";

            if(HotSpotInfo.isDeviceHotspot() || ssid.startsWith(VZTransferConstants.DIRECT_WIFI)){
                myFormattedString = Utils.getMaxConnectionHeader(activity,true);
            }else {
                myFormattedString = Utils.getMaxConnectionHeader(activity,false);
            }

            maxConnectionMsg.setText(myFormattedString);
        }

        if (!HotSpotInfo.isDeviceHotspot()) {
            LogUtil.e(TAG, "Stopped hotspot server.");
            WifiAccessPoint.getInstance().Stop();

            ssidConnected = CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_hotspot_find_conn_status);
            LogUtil.d(TAG, "Update connection UI check is Open camera.");
            if (Utils.isThisServer()) {
                if (QRCodeUtil.getInstance().isUsingQRCode()) {
                    networkHeading.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_qr_code_scan_page_header_receiver));
                } else {
                    networkHeading.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.heading_ctsame_wifi_network));
                }
                if (wifiMessageTV != null)
                    wifiMessageTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.text_ctsame_wifi_network));
            } else {
                networkHeading.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.heading_ctsame_wifi_network));
                if (wifiMessageTV != null)
                    wifiMessageTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.connect_this_phn_tv));
            }

            if (Utils.isConnectedViaWifi()) {
                ssidConnected = ssid;
                String routerPsw = "";
                LogUtil.d(TAG,"Wifi connected : ssid :"+ssidConnected);
                if (QRCodeUtil.getInstance().isUsingQRCode()) {
                    QRCodeUtil.getInstance().generateQRCode(activity, VZTransferConstants.PHONE_WIFI_CONNECTION, ssid, routerPsw, displayQrCode);
                    tryAnotherWayBtn.setVisibility(View.GONE);
                } else {
                    enableNextButton(true);
                }
            } else {
                if (!QRCodeUtil.getInstance().isUsingQRCode()) {
                    enableNextButton(false);
                }
            }
            if (QRCodeUtil.getInstance().isUsingQRCode()) {
                networkNameTV.setText(R.string.qr_network_name_connected);
                LogUtil.d(TAG,"is using qr code....");
/*
                if (!Utils.isReceiverDevice()) {
                    tryAnotherWayBtn.setVisibility(View.GONE);
                    wifiSettingBtn.setVisibility(View.VISIBLE);
                }
                */
                if (!CTGlobal.getInstance().isCross()) {
                    tryAnotherWayBtn.setVisibility(View.GONE);
                    wifiSettingBtn.setVisibility(View.VISIBLE);
                }else {
                    tryAnotherWayBtn.setVisibility(View.VISIBLE);
                    wifiSettingBtn.setVisibility(View.GONE);
                }
            } else {
                networkNameTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.heading_network_name_ctsame_wifi_network));
                nextBtn.setVisibility(View.VISIBLE);
                CTWifiSetupView.getInstance().toggleConnectionCounterView(false);
            }
            if (passwordDivider != null)
                passwordDivider.setVisibility(View.GONE);
            if (hotspotPswLayout != null)
                hotspotPswLayout.setVisibility(View.INVISIBLE);
           /* if(wifiPassLockIV !=null)
                wifiPassLockIV.setVisibility(View.GONE);*/
            p2pWifipass.setVisibility(View.INVISIBLE);
            p2pWifikey.setVisibility(View.INVISIBLE);
            wifiSSID.setText(ssidConnected);

            CTGlobal.getInstance().setHotspotName(ssidConnected);

        } else {

            if (!QRCodeUtil.getInstance().isUsingQRCode()) {
                networkHeading.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.this_is_your_hotspot_tv));
                nextBtn.setVisibility(View.GONE);
                if (passwordDivider != null)
                    passwordDivider.setVisibility(View.VISIBLE);
                if (hotspotPswLayout != null)
                    hotspotPswLayout.setVisibility(View.VISIBLE);
               /* if(wifiPassLockIV !=null)
                    wifiPassLockIV.setVisibility(View.VISIBLE);*/
            }
            else {
                QRCodeUtil.getInstance().generateQRCode(activity, VZTransferConstants.HOTSPOT_WIFI_CONNECTION, HotSpotInfo.getHotspotSSID(), HotSpotInfo.getHotspotPass(), displayQrCode);
            }

            if (wifiMessageTV != null)
                wifiMessageTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.connect_your_other_phn_tv));

            networkNameTV.setText(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_hotspot_disc_name_tv));
            wifiSSID.setText(HotSpotInfo.getHotspotSSID());
            p2pWifipass.setVisibility(View.VISIBLE);
            p2pWifikey.setVisibility(View.VISIBLE);

            p2pWifikey.setText(HotSpotInfo.getHotspotPass());
            CTGlobal.getInstance().setHotspotName(HotSpotInfo.getHotspotSSID());
        }
    }

    private void enableNextButton(Boolean enable) {
        if (enable) {
            (activity.findViewById(R.id.ct_same_wifi_network_btn_next)).setBackgroundResource(R.drawable.ct_button_solid_black_bg);
            ((TextView) activity.findViewById(R.id.ct_same_wifi_network_btn_next)).setTextColor(activity.getResources().getColor(R.color.ct_mf_white_color));
        } else {
            (activity.findViewById(R.id.ct_same_wifi_network_btn_next)).setBackgroundResource(R.drawable.vz_gray_solid_round_button);
            ((TextView) activity.findViewById(R.id.ct_same_wifi_network_btn_next)).setTextColor(activity.getResources().getColor(R.color.ct_mf_light_grey_color));
        }
        nextBtn.setEnabled(enable);
    }

    public void setupQRRouterUI() {
        tryAnotherWayBtn.setVisibility(View.GONE);
        wifiSettingBtn.setVisibility(View.VISIBLE);
        qrcodeIV.setVisibility(View.GONE);
    }

    public void updateNoInternetAlert(boolean isVisible) {
        if(noInternetAlertLayout != null){
            if(isVisible) {
                noInternetAlertLayout.setVisibility(View.VISIBLE);
                int startSpannableLength=activity.getResources().getString(R.string.ct_no_internet_alert_msg_show_notification).indexOf(
                        activity.getResources().getString(R.string.here_text));
                int endSpannableLength=startSpannableLength+activity.getResources().getString(R.string.here_text).length();

                SpannableString ss = new SpannableString(activity.getResources().getString(R.string.ct_no_internet_alert_msg_show_notification));
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        CTWifiSetupModel.getInstance().openNotificationTray();
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.linkColor = activity.getResources().getColor(R.color.hyper_link);
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                ss.setSpan(clickableSpan, startSpannableLength, endSpannableLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView termsText = (TextView) activity.findViewById(R.id.ct_hotspot_no_internet_alert_msg_link);
                termsText.setText(ss);
                termsText.setMovementMethod(LinkMovementMethod.getInstance());
                termsText.setHighlightColor(Color.TRANSPARENT);
            }else {
                noInternetAlertLayout.setVisibility(View.GONE);
            }
        }
    }
}
