package com.verizon.contenttransfer.p2p.service;

/**
 * Created by c0bissh on 3/29/2017.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.adapter.ConnectionListener;
import com.verizon.contenttransfer.adapter.ServerAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.HandShakeUtil;
import com.verizon.contenttransfer.utils.LogUtil;

import java.net.ServerSocket;
import java.net.Socket;

public class CTServer extends AsyncTask<Void, Void, String> {
	private static final String TAG = CTServer.class.getName();

	public CTServer() {
	}

	protected String doInBackground(Void...params) {
		ServerSocket serverSocket = ServerConnectionObject.getInstance().getServerSocket();
		ConnectionListener connectionListener = new ServerAdapter();
		do {
			try {
				CTGlobal.getInstance().setReadyToConnect(true);
				Intent intent = new Intent(VZTransferConstants.READY_TO_ACCEPT_CONNECTION);
				LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);

				LogUtil.d(TAG, "Server Socket -  is waiting to accept connection.");
				Socket clientSocket = serverSocket.accept();
				CTGlobal.getInstance().setReadyToConnect(false);
				clientSocket.setTcpNoDelay(true);
				if(!CTGlobal.getInstance().getExitApp()){
					if(CTGlobal.getInstance().isWaitForNewDevice()
							|| !CTGlobal.getInstance().isDoingOneToMany()) {
						LogUtil.d(TAG,"Start server side handshake.");
						HandShakeUtil.serverSideHandShake(connectionListener, clientSocket, true);
					}
				}
			} catch (Exception e) {
				LogUtil.d(TAG, "Server Socket -  exception: " + e.getMessage());
				break;
			}
		} while (CTGlobal.getInstance().isWaitForNewDevice() && !CTGlobal.getInstance().getExitApp());
		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		LogUtil.d(TAG, "onPreExecute - launch CTserver.");
	}

	@Override
	protected void onPostExecute(String s) {
		super.onPostExecute(s);

		LogUtil.d(TAG, "onPostExecute - We exited CT Server Async task");
	}
}
