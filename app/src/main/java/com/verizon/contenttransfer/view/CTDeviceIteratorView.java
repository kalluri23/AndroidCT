package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.listener.DeviceIteratorListener;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;

/**
 * Created by rahiahm on 9/6/2016.
 */
public class CTDeviceIteratorView {
    private static String TAG = CTDeviceIteratorView.class.getName();

    private static CTDeviceIteratorView instance;
    private Activity activity;
    private View mContentView;
    public void initView(Activity activity, View mContentView) {
        this.activity = activity;
        this.mContentView = mContentView;
        initialize(activity,mContentView);
        CTDeviceIteratorModel.getInstance().initModel(activity);
    }

    public static CTDeviceIteratorView getInstance() {
        if (instance == null) {
            instance = new CTDeviceIteratorView();
        }
        return instance;
    }

    public static void killInstance() {
        instance = null;
    }

    private void initialize(final Activity activity, final View mContentView) {


        ((TextView) mContentView.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_pairing);

        if (Utils.isStandAloneBuild()) {
            mContentView.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            mContentView.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }
        View.OnClickListener listener = new DeviceIteratorListener( activity );

        mContentView.findViewById(R.id.search_icon).setOnClickListener(listener);
        mContentView.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
        mContentView.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);
        showInvitationLayout(false);

    }

    public void showInvitationLayout(boolean show) {
        int inviteVisible = show == true ? View.VISIBLE : View.GONE;
        int listVisible = show == true ? View.GONE : View.VISIBLE;
        if(QRCodeUtil.getInstance().isUsingQRCode()){
            listVisible = View.GONE;
        }
        mContentView.findViewById(R.id.ct_invitation_layout).setVisibility(inviteVisible);
        if(null != mContentView.findViewById(R.id.ct_w_direct_layout_qr) && (show)) {
            mContentView.findViewById(R.id.ct_w_direct_layout_qr).setVisibility(listVisible);
        }
        mContentView.findViewById(R.id.ct_w_direct_layout).setVisibility(listVisible);
        mContentView.findViewById(R.id.ct_wifi_pairing_sender_btn_group).setVisibility(listVisible);
    }


    public void handleListItemClickEvent(int position, WifiP2pDevice wifiP2pDevice,  View v) {
        WiFiDirectActivity.selectedListitemPosition = position;
        CTDeviceIteratorModel.getInstance().setDevice(wifiP2pDevice);
        if(CTDeviceIteratorModel.getInstance().getConfig() != null && wifiP2pDevice !=null) {
            CTDeviceIteratorModel.getInstance().getConfig().deviceAddress = wifiP2pDevice.deviceAddress;
            CTDeviceIteratorModel.getInstance().getConfig().wps.setup = WpsInfo.PBC;
            CTDeviceIteratorModel.getInstance().getConfig().groupOwnerIntent = 0;
        }
        LogUtil.d(TAG, "Handle list item click event :" + v);

        if (v != null) {
            ListView lv = (ListView) mContentView.findViewById(android.R.id.list);
            for (int i = 0; i < CTDeviceIteratorModel.getInstance().getPeers().size(); i++) {
                if (null != lv.getChildAt(i)) {
                    ((ImageView) lv.getChildAt(i).findViewById(R.id.ct_w_phn_name_chk)).setImageResource(R.mipmap.icon_ct_check_grey);
                }
            }

            ((ImageView) v.findViewById(R.id.ct_w_phn_name_chk)).setImageResource(R.mipmap.radio_checked_new);
            getNextInRedColor();
        }
    }
    private void getNextInRedColor() {
        (mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_next_wifi_direct_btn)).setBackgroundResource(R.drawable.ct_button_solid_black_bg);
        ((TextView) mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_next_wifi_direct_btn)).setTextColor(mContentView.getResources().getColor(R.color.ct_mf_white_color));
    }
}
