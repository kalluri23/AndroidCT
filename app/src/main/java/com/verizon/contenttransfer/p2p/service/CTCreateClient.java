package com.verizon.contenttransfer.p2p.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.adapter.ClientAdapter;
import com.verizon.contenttransfer.adapter.ConnectionListener;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.HandShakeUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by rahiahm on 6/21/2016.
 */
public class CTCreateClient extends AsyncTask<Void, Void, String> {
    private static final String TAG = CTCreateClient.class.getName();

    //initialized to android for wifi direct

    public static final String iosConnectMessagePrefix = "VZCONTENTTRANSFERSECURITYKEYFROMSENAND#";		//--TODO add "AND" to represent android platform
    private static String serverIp = "";
    private static String altServerIp = "";
    ConnectionListener connectionListener = null;
    public CTCreateClient( String serverIp, String altServerIp) {
        this.altServerIp = altServerIp;
        this.serverIp = serverIp;
        this.connectionListener = new ClientAdapter();
    }

    protected String doInBackground(Void...params) {

            Socket clientSocket = null;
            try {
                clientSocket = SocketUtil.connectToServer(serverIp);
                if (clientSocket != null && clientSocket.isConnected()) {
                    LogUtil.e(TAG, "connection verified " + serverIp);
                    HandShakeUtil.clientSideHandShake(connectionListener, clientSocket, false);
                } else {
                    if(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.PHONE_WIFI_CONNECTION) && altServerIp != null){
                        try{
                            serverIp = altServerIp;
                            LogUtil.e(TAG, "Dummy");
                            clientSocket = SocketUtil.connectToServer(serverIp);
                            if (clientSocket != null && clientSocket.isConnected()) {
                                LogUtil.e(TAG, "connection verified " + serverIp);
                                HandShakeUtil.clientSideHandShake(connectionListener, clientSocket, false);
                            } else {
                                throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Socket not connected");
                            }
                        }catch (IOException e){
                            String errMsg = "";
                            errMsg = e.getMessage();
                            LogUtil.e(TAG, errMsg);
                            CustomDialogs.dismissDefaultProgressDialog();
                            CustomDialogs.customProgressDialog = null;
                            //get the key from sender
                            Intent keyintent1 = new Intent(VZTransferConstants.CLIENT_KEY_UPDATE_BROADCAST);
                            keyintent1.putExtra("message", VZTransferConstants.CLIENT_KEY_UPDATE_BROADCAST);
                            keyintent1.putExtra("errorMessage", errMsg);
                            // add data
                            LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(keyintent1);
                        }

                    }else {
                        throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Socket not connected");
                    }
                }
            } catch (IOException e) {
                LogUtil.e(TAG, "IO exception.. :"+e.getMessage());
                if(clientSocket != null){
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        LogUtil.e(TAG, "IOException.... :" + e1.getMessage());
                    }
                }
            }catch (Exception e) {
                LogUtil.e(TAG, "exception.. :"+e.getMessage());
            } finally {

            }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LogUtil.d(TAG, "CT create client onPostExecute.");
        if(CTGlobal.getInstance().getConnectionType().equals(VZTransferConstants.WIFI_DIRECT_CONNECTION)){
            CustomDialogs.dismissDefaultProgressDialog();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        LogUtil.d(TAG, "CT create client onPreExecute... Connection type from CTGlobal :" + CTGlobal.getInstance().getConnectionType());
    }
}
