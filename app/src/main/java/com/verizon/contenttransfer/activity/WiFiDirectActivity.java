package com.verizon.contenttransfer.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.ExitContentTransferDialog;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.base.VersionCheckReceiver;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.fonts.CTButtonTextView;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.model.CTWifiDirectModel;
import com.verizon.contenttransfer.p2p.accesspoint.WifiAccessPoint;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.service.SensorService;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifidirect.WiFiDirectBroadcastReceiver;
import com.verizon.contenttransfer.wifidirect.WifiDirectCustomListener;
import com.verizon.contenttransfer.wifip2p.WifiManagerControl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WiFiDirectActivity extends BaseActivity {

	public static final String TAG = "WiFiDirectActivity";
	private static WifiP2pManager manager;
	public static CustomDialogs WifiDirectCustomDialogs;
	private static Channel channel;
	private BroadcastReceiver receiver = null;
	private BroadcastReceiver versionReceiver = null;
	public static Activity activity = null;
	public static int selectedListitemPosition = -1;
	private OnClickListener cancelListener;
	static Dialog connectionDialog=null;
	private TextView manualBtn;
	private Dialog manualSetupDialog;
	private boolean selectHotspot;
	private static List<BroadcastReceiver> broadcastReceiversList = new ArrayList<BroadcastReceiver>();


	//private Dialog qrCodeValidatingDialog = null;
	ProgressDialog pDialog = null;

	@Override
	public void onBackPressed() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		activity = this;
		CTErrorReporter.getInstance().Init(this);
		CTWifiDirectModel.getInstance().initModel(activity);



		if(!QRCodeUtil.getInstance().isReturnedFromQRActivity()) {
			QRCodeUtil.getInstance().setScannedQRCode(null);
		}
		selectHotspot = false;

		if(CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE))
		{
			if(QRCodeUtil.getInstance().isUsingQRCode()){
				activity.setContentView(R.layout.ct_wifi_direct_pairing_reciver_layout_qr);
			}else {

				activity.setContentView(R.layout.ct_wifi_direct_pairing_reciver_layout);
			}
			if(!Utils.isWifiDirectSupported()){
				((TextView)activity.findViewById(R.id.ct_device_combo_hd)).setText(activity.getString(R.string.doesnt_support_widi));
				((TextView)activity.findViewById(R.id.ct_w_pairing_desc_tv)).setText(R.string.ct_error_desc1);
				activity.findViewById(R.id.ct_old_phone_chk_view).setVisibility(View.GONE);
			}

			if(Utils.isStandAloneBuild()){
				activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
				activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
				activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
			}

			((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_pairing);

			View.OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					ExitContentTransferDialog.showExitDialog(WiFiDirectActivity.this, "DiscoveringScreen");
				}
			};

			activity.findViewById(R.id.search_icon).setOnClickListener(listener);
			activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(listener);
			activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(listener);

			this.findViewById(R.id.ct_W_pairing_i_dont_see_it_txt).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					launchHotspot();
				}
			});
		}else {
			setContentView(R.layout.ct_main);
		}

		Handler handler1 = new Handler();
		handler1.postDelayed(new Runnable() {
			@Override
			public void run() {
				WifiDirectCustomDialogs.hideProgressBar();
			}
		}, 3000);

		manualBtn = (CTButtonTextView) findViewById(R.id.ct_manual_btn);
		if(manualBtn != null){
			manualBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {



					manualSetupDialog = CustomDialogs.createDialog(activity, activity.getString(R.string.dialog_title), activity.getString(R.string.manual_setup_dialog_text), true, null,
							true,activity.getString(R.string.msg_ok), new View.OnClickListener() {
								@Override
								public void onClick(View v) {

									Utils.manualSetupExit(activity, manualSetupDialog);

								}
							},
							true, activity.getString(R.string.cancel_button), new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									manualSetupDialog.dismiss();
								}
							});
				}
			});
		}

		WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		selectedListitemPosition = -1;

		LogUtil.d(TAG, "Backup WifiConfiguration list on WiFiDirectActivity create.");
		if(!HotSpotInfo.isDeviceHotspot()) {
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}
		}

		if(Utils.isWifiDirectSupported()) {

			versionReceiver = new VersionCheckReceiver(this, WiFiDirectActivity.class.getName());

			registerWifiDirectManager();
			registerBroadcastReceiver();
			cancelListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					WifiDirectCustomDialogs.hideProgressBar();
					LogUtil.d(TAG, " WifiDirectActivity cancel click");
					Handler handler3 = new Handler();
					handler3.postDelayed(new Runnable() {

						@Override
						public void run() {
							StopPeerDiscovery();
						}
					}, 300);
				}

			};
			WifiDirectCustomDialogs.showProgressBar(WiFiDirectActivity.this, activity.getString(R.string.toolbar_heading_discovering),activity.getString(R.string.please_wait), true, true, true, null, false, null, null, true, activity.getString(R.string.msg_ok), cancelListener);
		}
	}

	public void registerWifiDirectManager() {
		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		initializeP2P();

		channel = manager.initialize(this, getMainLooper(), null);

		Utils.setListener(new WifiDirectCustomListener(WiFiDirectActivity.this, manager, channel));

	}

	public void registerWifiDirectBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);

	}
	private static void onInitiateDiscovery(final Activity activity) {

		OnClickListener cancelListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiDirectCustomDialogs.hideProgressBar();
				LogUtil.d(TAG, " WifiDirectActivity cancel click");
				Handler handler3 = new Handler();
				handler3.postDelayed(new Runnable() {

					@Override
					public void run() {

						StopPeerDiscovery();
					}
				}, 300);
			}
		};

		if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.OLD_PHONE)) {
			CustomDialogs.showProgressBar(activity, activity.getString(R.string.toolbar_heading_discovering),activity.getString(R.string.please_wait), true, true, true, null, false, null, null, true,activity.getString(R.string.msg_ok), cancelListener);
			if (QRCodeUtil.getInstance().isUsingQRCode()) {
				QRCodeUtil.getInstance().setReturnedFromQRActivity(false);
				LogUtil.d(TAG, "set returned from qr act ..4:" + QRCodeUtil.getInstance().isReturnedFromQRActivity());
				Handler handler3 = new Handler();
				handler3.postDelayed(new Runnable() {

					@Override
					public void run() {
						CustomDialogs.hideProgressBar();
						LogUtil.d(TAG, "Start thread to launch QR code activity.");

						QRCodeUtil.getInstance().launchQRCodeActivity(activity);
					}
				}, 3000);
			}
		}

	}

	public static void startWiDiDiscovery(final Activity activity) {

		LogUtil.d(TAG,"Start wifi direct discovery...");

		new Handler().postDelayed(new Runnable() {
			public void run() {
				WiFiDirectActivity.startDiscovery(activity);
			}
		}, 1000);
	}

	public static void updateThisDevice(WifiP2pDevice device) {

		TextView phonename = (TextView) activity.findViewById(R.id.ct_old_phone_hd_desc);
		if(phonename != null){
			phonename.setText(device.deviceName);

			boolean displayQrCode = false;
			if(QRCodeUtil.getInstance().isUsingQRCode()) {
				if (CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE)) {
					displayQrCode = true;
				} else if (CTGlobal.getInstance().isCross() && HotSpotInfo.isDeviceHotspot()) {
					displayQrCode = true;
				}
			}

			QRCodeUtil.getInstance().generateQRCode(activity, VZTransferConstants.WIFI_DIRECT_CONNECTION, device.deviceName, null, displayQrCode);
		}
	}

	private void launchHotspot(){
		//rahiahm - Need to disable the WifiDirect listeners to prevent conflict with Hotspot code
		Utils.setListener(null);

		selectHotspot = true;
		if(Utils.isWifiDirectSupported()) { //sang
			WiFiDirectActivity.StopPeerDiscovery();
		} //sang end

		LogUtil.d(TAG, "Broadcasting message");
		//DeviceIterator.p2ptype = true;
		CTGlobal.getInstance().setIsWifiDirecct(false);
		Intent sensorIntent = new Intent(SensorService.STOP_SENSOR_SERVICE);

		this.sendBroadcast(sensorIntent);
		LogUtil.d(TAG, "SensorService stoped");

		if(Utils.isThisServer()){
			WifiAccessPoint.getInstance().init(activity);
			WifiAccessPoint.getInstance().Start();

			CustomDialogs.createDefaultProgressDialog(activity.getString(R.string.settingup_wifi_hotspot),activity, false);
		}else {
			CTDeviceIteratorModel.getInstance().iDontSeeItFunction(activity);
		}
	}

	private BroadcastReceiver mVersionCheckSuccess = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String hostReceived = intent.getStringExtra("ipAddressOfServer");

			if(Utils.isReceiverDevice()){
				LogUtil.d(TAG, "Version check success .. new phone. go to getting ready page.");
				Intent getReadyIntent = new Intent(CTGlobal.getInstance().getContentTransferContext(), CTGettingReadyReceiverActivity.class);
				getReadyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Bundle bundle = new Bundle();
				bundle.putBoolean("isServer", true);
				bundle.putString("ipAddressOfServer", hostReceived);
				getReadyIntent.putExtras(bundle);
				CTGlobal.getInstance().getContentTransferContext().startActivity(getReadyIntent);
			}else {
				LogUtil.d(TAG, "Version check success .. old phone. go to select item page.");
				Utils.startSelectContentActivity();

			}
		}
	};
	private BroadcastReceiver mHotspotReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			CustomDialogs.dismissDefaultProgressDialog();

			// If app crash, we don't need to do any action on broadcast receiver.
			if(P2PStartupActivity.contentTransferContext == null){
				return;
			}

			//rahiahm - only process the broadcast if it was from clicking "I don't see it"
			if(selectHotspot) {
				Intent intent1 = new Intent(activity, CTWifiSetupActivity.class);
				boolean isServer = CTGlobal.getInstance().getPhoneSelection().equals(VZTransferConstants.NEW_PHONE);
				intent1.putExtra("isServer", isServer);
				intent1.putExtra("enableWifi", true);
				startActivity(intent1);
			}
		}
	};

	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "restore-wifi-connection" is broadcasted.
	private BroadcastReceiver mMessageRestoreWifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// If app crash, we don't need to do any action on broadcast receiver.
			if(P2PStartupActivity.contentTransferContext == null){
				return;
			}
			// Get extra data included in the Intent
			String message = intent.getStringExtra("message");

			LogUtil.d( TAG, "Got message: " + message);
			if(null != message){
				if(message.equals("restorewifi")){
					CTGlobal.getInstance().setIsWifiDirecct(false);
					WifiManagerControl.closePendingAsyncTask(true, false);
				}
				else if(message.equals("validate_wifi_on_wifidirect_connection")){
					WifiManagerControl.configuringWifiConnection(activity);
				}
				else if(message.equals("restart-widi-discovery")){
					LogUtil.d(TAG, "received restart-widi-discovery");
					deletePersistentGroups();
					LogUtil.d(TAG, "deletePersistentGroups..");
					if(Utils.isReceiverDevice()) {
						startDiscoveryInBackground(activity);
						LogUtil.d(TAG, "started discovery in background");
					}

				}

				else if(message.equals("show_connecting_dialog")){

					connectionDialog = CustomDialogs.showProgressBar(WiFiDirectActivity.activity,activity.getString(R.string.connecting), activity.getString(R.string.please_wait),true, true, true, null, false, null, null, true,activity.getString(R.string.cancel_button), null);
				}
			}

		}
	};

	private BroadcastReceiver mMessageStopWiDiBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// If app crash, we don't need to do any action on broadcast receiver.
			if(P2PStartupActivity.contentTransferContext == null){
				return;
			}
			// Get extra data included in the Intent
			String message = intent.getStringExtra("message");

			LogUtil.d( TAG, "mMessageStopWiDiBroadcastReceiver - Got message: " + message);
			if(null != message){
				if(message.equals("stopwidireceiver")){
					unregisterWifiDirectReceivers();
				}

			}

		}
	};


	private void initializeP2P() {
		LogUtil.d( TAG,"WifiDirectActivity - Oncreate initializeP2P");
		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(),
				new ChannelListener() {
					public void onChannelDisconnected() {
						initializeP2P();
					}
				});
		deletePersistentGroups();
	}
	private void deletePersistentGroups(){
		try {
			Method[] methods = WifiP2pManager.class.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals("deletePersistentGroup")) {
					// Delete any persistent group
					for (int netid = 0; netid < 32; netid++) {
						methods[i].invoke(manager, channel, netid, null);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onResume - WiFiDirectActivity");
		if(CTGlobal.getInstance().getExitApp()){
			finish();
			return;
		}
		if(Utils.isWifiDirectSupported()) {
			registerConnectionBroadcastReceiver();
		}
		if(QRCodeUtil.getInstance().getScannedQRCode() == null) {
				startWiDiDiscovery(activity);
		}
		LocalBroadcastManager.getInstance(this).registerReceiver(mHotspotReceiver, new IntentFilter(VZTransferConstants.ACCESS_POINT_UPDATE));
		try {
			Utils.getListener().disconnect();
		} catch (Exception e) {
			LogUtil.e(TAG, "Exception while disconecting : " + e.getMessage());
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		LogUtil.e(TAG, "Wifi Direct paused");
		if(CTGlobal.getInstance().getExitApp()){
			return;
		}
		if(mHotspotReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mHotspotReceiver);
		}
		unregisterConnectionBroadcastReceiver();

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(VZTransferConstants.ACTIVITY_TAG, "onDestroy - WiFiDirectActivity");
		unregisterWifiDirectReceivers();
	}

	private void registerBroadcastReceiver() {
		if(activity != null) {
			registerWifiDirectBroadcastReceiver();
			broadcastReceiversList.add(receiver);
			LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageRestoreWifiReceiver, new IntentFilter("restore-wifi-connection"));
			broadcastReceiversList.add(mMessageRestoreWifiReceiver);
			LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageStopWiDiBroadcastReceiver, new IntentFilter("stop-widi-broadcast-receiver"));
			broadcastReceiversList.add(mMessageStopWiDiBroadcastReceiver);
		}
	}

	private void registerConnectionBroadcastReceiver() {
		LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).registerReceiver(versionReceiver, new IntentFilter(VZTransferConstants.VERSION_CHECK_FAILED));
		broadcastReceiversList.add(versionReceiver);
		LocalBroadcastManager.getInstance(activity).registerReceiver(mVersionCheckSuccess, new IntentFilter(VZTransferConstants.VERSION_CHECK_SUCCESS));
		broadcastReceiversList.add(mVersionCheckSuccess);
	}

	private void unregisterWifiDirectReceivers() {
		if(null != receiver && isReceiverRegistered(receiver)) {
			unregisterReceiver(receiver);
			broadcastReceiversList.remove(receiver);
		}

		if(mMessageRestoreWifiReceiver != null && isReceiverRegistered(mMessageRestoreWifiReceiver)) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageRestoreWifiReceiver);
			broadcastReceiversList.remove(mMessageRestoreWifiReceiver);
		}
		if( mMessageStopWiDiBroadcastReceiver != null && isReceiverRegistered(mMessageStopWiDiBroadcastReceiver)) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver( mMessageStopWiDiBroadcastReceiver );
			broadcastReceiversList.remove(mMessageStopWiDiBroadcastReceiver);
		}
		unregisterConnectionBroadcastReceiver();

	}

	private void unregisterConnectionBroadcastReceiver() {
		if(versionReceiver != null && isReceiverRegistered(versionReceiver)) {
			LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).unregisterReceiver(versionReceiver);
			broadcastReceiversList.remove(versionReceiver);
		}
		if(mVersionCheckSuccess != null && isReceiverRegistered(mVersionCheckSuccess)) {
			LocalBroadcastManager.getInstance(activity).unregisterReceiver(mVersionCheckSuccess);
			broadcastReceiversList.remove(mVersionCheckSuccess);
		}
	}
	public boolean isReceiverRegistered(BroadcastReceiver receiver){
		boolean registered = broadcastReceiversList.contains(receiver);
		LogUtil.i(getClass().getSimpleName(), "is receiver " + receiver + " registered? " + registered);
		return registered;
	}



	public static void cancelP2P(){
		StopPeerDiscovery();
		if(null != manager) {
			manager.cancelConnect(channel, new ActionListener() {

				@Override
				public void onSuccess() {
					LogUtil.d(TAG, "cancelled WifiDirect connection");
				}

				@Override
				public void onFailure(int reason) {
					LogUtil.d(TAG, "Could not cancel WifiDirect connection. Reason code: " + reason);
				}
			});
		}
	}
	public static boolean startDiscovery(final Activity activity) {
		LogUtil.d(TAG, "Line 496");
		if (Utils.isWifiDirectSupported() && !CTGlobal.getInstance().isWidiDiscovering()) {
			CTGlobal.getInstance().setIsWidiDiscovering(true);
			LogUtil.d(TAG, "Line 499");
			onInitiateDiscovery(activity);
			manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

				@Override
				public void onSuccess() {
					LogUtil.d(TAG, "Discovery Initiated");
					CTGlobal.getInstance().setIsWidiDiscovering(false);
				}

				@Override
				public void onFailure(int reasonCode) {
					LogUtil.d(TAG, "Discovery Failed : ");
					CTGlobal.getInstance().setIsWidiDiscovering(false);
					switch (reasonCode) {

						case WifiP2pManager.ERROR:

							LogUtil.d(TAG, "Getting error while peers discover");
							break;

						case WifiP2pManager.P2P_UNSUPPORTED:
							LogUtil.d(TAG, "Device is not supported");
							break;


						case WifiP2pManager.BUSY:
							LogUtil.d(TAG, "Device is busy");
							break;

					}
				}
			});
		}
		return true;
	}
	public static boolean startDiscoveryInBackground(final Context context) {
		if (Utils.isWifiDirectSupported() && !CTGlobal.getInstance().isWidiDiscovering()) {
			CTGlobal.getInstance().setIsWidiDiscovering(true);
			manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

				@Override
				public void onSuccess() {
					LogUtil.d(TAG, "Discovery Initiated");
					CTGlobal.getInstance().setIsWidiDiscovering(false);
				}

				@Override
				public void onFailure(int reasonCode) {
					LogUtil.d(TAG, "Discovery Failed : reasonCode=" + reasonCode);
					CTGlobal.getInstance().setIsWidiDiscovering(false);
					switch (reasonCode) {

						case WifiP2pManager.ERROR:

							LogUtil.d(TAG, "Getting error while peers discover");
							break;

						case WifiP2pManager.P2P_UNSUPPORTED:
							LogUtil.d(TAG, "Device is not supported");
							break;


						case WifiP2pManager.BUSY:
							LogUtil.d(TAG, "Device is busy");
							new Handler().postDelayed(new Runnable() {
								public void run() {
									startDiscoveryInBackground(activity);
								}
							}, 3000);
							break;
					}
				}
			});
		}
		return true;
	}
	public static void StopPeerDiscovery(){
		LogUtil.d(TAG, "STOP PEER DISCOVERY.");

		if (Utils.isWifiDirectSupported()) {
			if (manager != null) {
				manager.stopPeerDiscovery(channel, new ActionListener() {
					@Override
					public void onSuccess() {
						LogUtil.d(WiFiDirectActivity.TAG, "Stopped Peer discovery");
					}

					@Override
					public void onFailure(int i) {
						LogUtil.d(WiFiDirectActivity.TAG, "Not Stopped Peer discovery");
					}
				});

			}
		}
	}
}
