package com.verizon.contenttransfer.wifidirect;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTErrorMsgActivity;
import com.verizon.contenttransfer.activity.P2PStartupActivity;
import com.verizon.contenttransfer.activity.WiFiDirectActivity;
import com.verizon.contenttransfer.adapter.WiFiPeerListAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTDeviceIteratorView;


@TargetApi(14)
public class DeviceIterator extends ListFragment implements PeerListListener {
    //

    //private static boolean isServer;

    public static final String TAG = DeviceIterator.class.getName();

    ProgressDialog progressDialog = null;
    private static View mContentView = null;

    public static boolean goToMVMHome = false;
    private WiFiPeerListAdapter listadaptor = null;
    //public static WifiP2pConfig config = new WifiP2pConfig();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CTErrorReporter.getInstance().Init(getActivity());
        CTDeviceIteratorModel.getInstance().setDevice(null);
        WifiP2pDevice noDevice = new WifiP2pDevice();

        noDevice.deviceName = getActivity().getString(R.string.ct_dont_see_desc_tv);
        noDevice.deviceAddress = null;
        CTDeviceIteratorModel.getInstance().getPeers().add(noDevice);

        listadaptor = new WiFiPeerListAdapter(getActivity(), R.layout.ct_wifi_direct_device_cell, CTDeviceIteratorModel.getInstance().getPeers());

        this.setListAdapter(listadaptor);
        loadParameters();
        loadEvents();

        if (!Utils.isWifiDirectSupported()) {
            getSearchAgainInGrayColor();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(QRCodeUtil.getInstance().isUsingQRCode()){
            mContentView = inflater.inflate(R.layout.ct_wifi_direct_pairing_sender_layout_qr, null);
        }else {
            mContentView = inflater.inflate(R.layout.ct_wifi_direct_pairing_sender_layout, null);
        }

        CTDeviceIteratorView.getInstance().initView(getActivity(), mContentView);



        if (null == P2PStartupActivity.contentTransferContext) {
            return null;
        } else {
            return mContentView;
        }

    }




    private static String getDeviceStatus(int deviceStatus) {
        LogUtil.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        LogUtil.d("DiscoveredDeviceList", "position =" + position);
    }


    private void showConnectingDialog() {
        Intent canceledIntent = new Intent("restore-wifi-connection");
        canceledIntent.putExtra("message", "show_connecting_dialog");
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(canceledIntent);
    }

    public void peerDiscoverStateChanged(WifiP2pDevice device) {

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (CTDeviceIteratorModel.getInstance().getPeers() != null && CTDeviceIteratorModel.getInstance().getPeers().size() == 0 && !Utils.isThisServer()) {
                    Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            WiFiDirectActivity.WifiDirectCustomDialogs.hideProgressBar();
                        }
                    }, 3000);

                } else {
                    WiFiDirectActivity.WifiDirectCustomDialogs.hideProgressBar();

                }
            }
        }, 5000);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (CTGlobal.getInstance().getExitApp() || null == getActivity()) {
            return;
        }
        CTDeviceIteratorModel.getInstance().getPeers().clear();
        WifiP2pDevice noDevice = new WifiP2pDevice();
        CTDeviceIteratorModel.getInstance().getPeers().addAll(peerList.getDeviceList());
        LogUtil.d(TAG,"onPeersAvailable peer size :"+CTDeviceIteratorModel.getInstance().getPeers().size());
        noDevice.deviceName = getActivity().getString(R.string.ct_dont_see_desc_tv);
        noDevice.deviceAddress = null;
        CTDeviceIteratorModel.getInstance().getPeers().add(peerList.getDeviceList().size(), noDevice);

        refreshListView();

        if (CTDeviceIteratorModel.getInstance().getPeers().size() == 1 && !Utils.isThisServer()) {
            LogUtil.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }
    }

    public void clearPeers() {
        LogUtil.d(TAG,"Clear peers..current peer size="+CTDeviceIteratorModel.getInstance().getPeers().size());
        CTDeviceIteratorModel.getInstance().getPeers().clear();
        refreshListView();
    }

    public void refreshListView() {

        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }


    private void loadParameters() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            //isServer = bundle.getInt(VZTransferConstants.SERVER_EXTRA_KEY) == 1 ? true : false;

            if (!Utils.isWifiDirectSupported()) {
                Intent intent = new Intent(getActivity(), CTErrorMsgActivity.class);
                intent.putExtra("screen", VZTransferConstants.WIDI_ERROR);
            }
        }
    }


    private void loadEvents() {
        mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_next_wifi_direct_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                CTDeviceIteratorModel.getInstance().executeNextEvent();
            }
        });

        mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_search_again_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WiFiDirectActivity.startDiscovery(getActivity());
            }
        });

    }

    private void getSearchAgainInGrayColor() {
        (mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_search_again_btn)).setBackgroundResource(R.mipmap.ic_ct_grey_solid_button);
        ((TextView) mContentView.findViewById(R.id.ct_wifi_direct_pairing_sender_search_again_btn)).setTextColor(mContentView.getResources().getColor(R.color.ct_mf_light_grey_color));
    }

    @Override
    public void onPause() {
        super.onPause();
        CTDeviceIteratorModel.getInstance().onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CTGlobal.getInstance().getExitApp()) {
            return;
        }
        CTDeviceIteratorModel.getInstance().onResume();
    }






    @Override
    public void onStop() {
        super.onStop();
        LogUtil.d(TAG, "device iterator on stop.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "device iterator on destroy.");
    }
}
