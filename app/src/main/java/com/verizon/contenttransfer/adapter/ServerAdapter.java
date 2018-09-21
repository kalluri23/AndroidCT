package com.verizon.contenttransfer.adapter;


import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.HandShakeUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.wifip2p.ConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerAdapter implements ConnectionListener {
	private static String TAG = ServerAdapter.class.getName();

	@Override
	public void clientAccepted(Socket clientSocket, String clientIp, boolean isCross) {

		LogUtil.d(TAG, "Server adapter - Connection established for... clientIp :"+clientIp);
		LogUtil.d(TAG, "Server adapter - local IP :"+ ConnectionManager.getLocalIpAddress());
		try {
			AcceptedClientInfo clientInfo = Utils.getAccetpedDeviceInfo(clientSocket, clientIp, isCross, VZTransferConstants.CLIENT_CONNECTED);
			ServerConnectionObject.getInstance().addNewAcceptedClient(clientIp, clientInfo);
			LogUtil.d(TAG, "Total accepted client size =" + ServerConnectionObject.getInstance().getAcceptedClients().size());
			LogUtil.d(TAG, "Top activity name :" + CTBatteryLevelReceiver.getTopActivityName());
			ServerConnectionObject.getInstance().createCommSocket();
			HandShakeUtil.handshakeSuccessCallback(clientIp, true);
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.e(TAG, "Exception on creating new client object :" + e.getMessage());
		}

		//Temporary code testing only one connection. On one to many we need to set it after all connection done.
		//CTGlobal.getInstance().setWaitForNewDevice(false);
	}


	@Override
	public void clientCommAccepted(Socket commClientSocket) {
		LogUtil.d(TAG, "client Comm Accepted");
		PrintWriter commWriter = null;
		BufferedReader commReader = null;
		String clientIp = "";
		try {
			clientIp = commClientSocket.getInetAddress().getHostAddress();
			commWriter = new PrintWriter(commClientSocket.getOutputStream(), true);
			commReader = new BufferedReader(new InputStreamReader(commClientSocket.getInputStream()));

			ServerConnectionObject.getInstance().updateAcceptedClientCommSocket(clientIp, commClientSocket, commWriter, commReader);

			Utils.commTaskToReadWriteMsg(commClientSocket);
			Utils.writeToCommSocketThread(Utils.getDeviceInfo(), clientIp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clientConnected(Socket clientSocket, String host, String connType, boolean isCross) {

	}

	@Override
	public void clientCommConnected(Socket clientSocket) {

	}

	@Override
	public void clientDisconnected(Socket clientSocket, String clientIp, String clientMessage) {

	}
}
