package com.verizon.contenttransfer.wifidirect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.widget.Toast;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adobe.CTSiteCatImpl;
import com.verizon.contenttransfer.adobe.CTSiteCatInterface;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifidirect.DeviceIterator.DeviceActionListener;

import java.net.InetAddress;

//import android.util.Log;

public class WifiDirectCustomListener implements ChannelListener, 
DeviceActionListener, ConnectionInfoListener {
	
	private static final String TAG = "WifiDirectCustomListener";
	
	private WifiP2pManager manager;
	private Activity activity;
	private Channel channel;
	private boolean retryChannel = false;
	//protected String TAG = "WifiDirectCustomListener";
	private static InetAddress host = null;
	private static String serverIp = null;
	private static ProgressDialog pDialog = null;
	CTSiteCatInterface iCTSiteCat = new CTSiteCatImpl();

	public WifiDirectCustomListener(Activity activity, WifiP2pManager manager, Channel channel){
		this.activity = activity;
		this.manager = manager;
		this.channel = channel;
	}
	
	@Override
	public void cancelDisconnect() {

		if (manager != null) {
			//final DeviceIterator fragment = (DeviceIterator) activity.getFragmentManager()
			//		.findFragmentById(R.id.frag_list);
			if (CTDeviceIteratorModel.getInstance().getDevice() == null
					|| CTDeviceIteratorModel.getInstance().getDevice().status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (CTDeviceIteratorModel.getInstance().getDevice().status == WifiP2pDevice.AVAILABLE
					|| CTDeviceIteratorModel.getInstance().getDevice().status == WifiP2pDevice.INVITED) {

				manager.cancelConnect(channel, new ActionListener() {

					@Override
					public void onSuccess() {
						Toast.makeText(activity.getApplicationContext(),
								activity.getString(R.string.aborting_connection), Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(
								activity.getApplicationContext(),
								"Reason code :" + reasonCode, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}

	}

	@Override
	public void showDetails(WifiP2pDevice device) {
		//Blank for now, but can be used to see device details in future.
	}

	@Override
	public void connect(WifiP2pConfig config) {

		manager.connect(channel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				LogUtil.d(TAG,"WifiDirect Custom listener - OnSuccess");

			}

			@Override
			public void onFailure(int reason) {
				
				LogUtil.d(TAG,"WifiDirect Custom listener - onFailure reason="+reason);
				//SensorService.bumpdetected=false;
				switch (reason) {

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
				//if(DeviceIterator.connectionDialog != null) DeviceIterator.connectionDiaLogUtil.dismiss();
				Toast.makeText(activity.getApplicationContext(),
						activity.getString(R.string.connect_failed_retry), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void disconnect() {		
		if (manager != null) {
			LogUtil.d(TAG, "WifiDirect Custom listener - Disconnect");

			try {
				manager.removeGroup(channel, new ActionListener() {

					@Override
					public void onFailure(int reasonCode) {
						LogUtil.d(TAG, "Disconnect failed. Reason :" + reasonCode);
					}

					@Override
					public void onSuccess() {
						LogUtil.d(TAG, "Disconnected");
					}

				});

			}catch(Exception e){

				LogUtil.d(TAG,e.getMessage());
			}
		}

	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (manager != null && !retryChannel) {
			Toast.makeText(activity.getApplicationContext(), "Channel lost. Trying again",
					Toast.LENGTH_LONG).show();
			resetData();
			retryChannel = true;
			manager.initialize(activity.getApplicationContext(), activity.getApplicationContext().getMainLooper(), 
					this);
		} else {
			Toast.makeText(
					activity.getApplicationContext(),
					"channel lost",
					Toast.LENGTH_LONG).show();
		}
	}
	
	public void resetData() {
		try {
			DeviceIterator fragmentList = (DeviceIterator) activity.getFragmentManager()
					.findFragmentById(R.id.frag_list);
			if (fragmentList != null) {
				fragmentList.clearPeers();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		LogUtil.d(TAG, "OnConnectionInfoAvailable...");
		if(MediaFetchingService.isP2PFinishActivityLaunched){
			return;
		}
		
		// After the group negotiation, we assign the group owner as the file
		// server. The file server is single threaded, single connection server
		if (info.groupFormed) {
			//String clientIp = getIpAddress();

			LogUtil.d(TAG, "isWiFiDirecteConnEstablished = " + CTGlobal.getInstance().isWifiDirecct());

			LogUtil.d(TAG, "Is Group Owner : --info.groupFormed--" + info.groupFormed + " --info.isGroupOwner--" + info.isGroupOwner);
			LogUtil.d(TAG, "isSendingDevice : " + info.groupOwnerAddress.getHostAddress().toString());
			CTGlobal.getInstance().setConnectionType(VZTransferConstants.WIFI_DIRECT_CONNECTION);
			if(!CTGlobal.getInstance().isWifiDirecct()){
				CTGlobal.getInstance().setIsWifiDirecct(true);
				// If we're the server
				if (info.isGroupOwner ) {

					host = info.groupOwnerAddress;
					Utils.createServerConnectionObject(host);

					LogUtil.d(TAG, "start CTCreate Server - Launching CT Create Server for wifi direct conn");

				}
				// If we're the client
				else {


					serverIp = info.groupOwnerAddress.getHostAddress();

/*					Intent intent = new Intent(activity, CTCreateClient.class);
					intent.setAction(VZTransferConstants.ACTION_HAND_SHAKE);
					intent.putExtra(VZTransferConstants.PAIRING_CONNECTION_TYPE, VZTransferConstants.WIFI_DIRECT_CONNECTION);
					intent.putExtra(VZTransferConstants.SERVER_IP, serverIp);

					activity.startService(intent);*/

					Utils.CreateClientConnectionObject( VZTransferConstants.WIFI_DIRECT_CONNECTION, serverIp, null,activity);

/*					ClientAsync Task client = new ClientAsync Task(activity,serverIp);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						client.executeOnExecutor(Executors.newSingleThreadExecutor());
					} else {
						client.execute();
					}*/

				}
			}
		}
	}
}
