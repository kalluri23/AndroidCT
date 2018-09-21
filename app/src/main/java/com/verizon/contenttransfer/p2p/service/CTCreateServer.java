package com.verizon.contenttransfer.p2p.service;

/**
 * Created by c0bissh on 3/29/2017.
 */

import android.os.AsyncTask;
import android.os.Build;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;

import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class CTCreateServer extends AsyncTask<Void, Void, String> {
    private static final String TAG = CTCreateServer.class.getName();

    private InetAddress host = null;

    public CTCreateServer(InetAddress host) {
        this.host = host;
    }

    protected String doInBackground(Void...params) {
        ServerSocket serverSocket = null;
        try {
            if(ServerConnectionObject.getInstance().getAcceptedClients() != null && ServerConnectionObject.getInstance().getAcceptedClients().size()>0){
                LogUtil.e(TAG, "Server already created. Pls verify why trying to create again.");
                return null;
            }

            boolean flag = true;
            int count = 0;
            while (flag && ++count < 5) {
                try {

                    if (serverSocket != null) {
                        flag = false;
                    } else {
                        Thread.sleep(5000);

                        if (host != null) {
                            LogUtil.d(TAG, "Trying to Connect - ServerSocket :" + count + " , server Ip =" + host.getHostAddress() + "  serverSocket=" + serverSocket);
                            serverSocket = new ServerSocket(VZTransferConstants.DISCOVERY_PORT, 10, host);
                        }else {
                            LogUtil.d(TAG, "Creating Router server socket.");
                            serverSocket = new ServerSocket(VZTransferConstants.DISCOVERY_PORT);
                        }
                        LogUtil.d(TAG, "set Reuse Address.");
                        serverSocket.setReuseAddress(true);

                        LogUtil.d(TAG, "address: " + serverSocket.getInetAddress().getHostAddress() + ", port: " + serverSocket.getLocalPort());

                    }
                } catch (BindException be) {
                    LogUtil.e(TAG, "BindException - " + be.getMessage());
                } catch (Exception e) {
                    LogUtil.e(TAG, "Exception - " + e.getMessage());
                }
            }

            if(serverSocket == null){
                LogUtil.e(TAG,"Server socket still null.");
                return null;
            }else {
                LogUtil.d(TAG, "server socket - Created.");
                LogUtil.d(TAG, "Server: local ip : " + serverSocket.getInetAddress().getHostName());

                ServerConnectionObject.getInstance().setServerSocket(serverSocket);
            }



        }catch (Exception e ) {
            LogUtil.d(TAG,"Socket testing - Is socket closed - exception: "+e.getMessage());
            e.printStackTrace();
        } finally {
            LogUtil.d(TAG,"Socket testing - Is socket closed : on finally");
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CTGlobal.getInstance().setReadyToConnect(false);
        LogUtil.d(TAG, "onPreExecute - launch CTCreate server.");

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LogUtil.e(TAG, "onPostExecute - We exited CTCreate Server Async task");
        if(ServerConnectionObject.getInstance().getServerSocket()!= null){

            CTServer server = new CTServer();

            LogUtil.d(TAG, "start CTCreate Server - Launching CT Server");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                server.executeOnExecutor(Executors.newSingleThreadExecutor());
            } else {
                server.execute();
            }

        }
    }
}
