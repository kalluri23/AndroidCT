package com.verizon.contenttransfer.adapter;


import android.util.Log;

import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.ClientConnectionObject;
import com.verizon.contenttransfer.utils.HandShakeUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientAdapter implements ConnectionListener {
	private static String TAG = ClientAdapter.class.getName();

	@Override
	public void clientConnected ( Socket clientSocket, String host, String connType, boolean isCross) {
		LogUtil.d(TAG, "Client adapter - Connection established for... clientIp :"+host);
		LogUtil.d(TAG, "Client adapter - local IP :"+ ConnectionManager.getLocalIpAddress());

		try {
			Utils.updateConnectedDeviceInfo(clientSocket, host, isCross, VZTransferConstants.CLIENT_CONNECTED);
			LogUtil.d(TAG, "Top activity name :" + CTBatteryLevelReceiver.getTopActivityName());
			HandShakeUtil.handshakeSuccessCallback(host, false);
			ClientConnectionObject.getInstance().createCommSocket();
		} catch (IOException e) {
			LogUtil.e(TAG, "Exception on creating new client object :" + e.getMessage());
		}
	}

	@Override
	public void clientCommConnected(Socket commClientSocket) {
		LogUtil.d(TAG, "client Comm Connected");
		PrintWriter commWriter = null;
		BufferedReader commReader = null;

		try {
			String clientIp = commClientSocket.getInetAddress().getHostAddress();
			commWriter = new PrintWriter(commClientSocket.getOutputStream(), true);
			commReader = new BufferedReader(new InputStreamReader(commClientSocket.getInputStream()));

			ClientConnectionObject.getInstance().setCommClientSocket(commClientSocket);
			ClientConnectionObject.getInstance().setCommOut(commWriter);
			ClientConnectionObject.getInstance().setCommIn(commReader);

			Utils.commTaskToReadWriteMsg(commClientSocket);
			Utils.writeToCommSocketThread(Utils.getDeviceInfo(), clientIp);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void clientDisconnected(Socket clientSocket, String clientIp, String clientMessage) {
		Log.d(TAG, "Client disconnected..");
	}
	@Override
	public void clientAccepted(Socket clientSocket, String clientIp, boolean isCross) {

	}

	@Override
	public void clientCommAccepted(Socket clientSocket) {

	}
}
