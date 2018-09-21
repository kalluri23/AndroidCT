package com.verizon.contenttransfer.p2p.asynctask;

import android.os.AsyncTask;

import com.verizon.contenttransfer.adapter.ClientAdapter;
import com.verizon.contenttransfer.adapter.ConnectionListener;
import com.verizon.contenttransfer.adapter.ServerAdapter;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.ClientConnectionObject;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.LogUtil;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kommisu on 2/22/2016.
 */
public class OpenCommSocketAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = OpenCommSocketAsyncTask.class.getName();
    private String socketName = "";
    private static String host;

    public ServerSocket commServerSocket = null;
    public Socket commClientSocket = null;

    public OpenCommSocketAsyncTask(String name, String hostName) {
        socketName = name;
        host = hostName;
    }

    @Override
    protected String doInBackground(Void... params) {
        LogUtil.d(TAG, "Opening comm socket.");
        if(socketName.equals(VZTransferConstants.SERVER_COMM_SOCKET)){
            LogUtil.d(TAG, "Creating Server COMM port....");
            openServerCommSocket();
        }else if(socketName.equals(VZTransferConstants.CLIENT_COMM_SOCKET)){
            LogUtil.d(TAG, "Creating Client COMM port....");
            openClientCommSocket(host);
        }
        return null;
    }

    private void openClientCommSocket(String host) {
        LogUtil.d(TAG, "Connecting client COMM host...."+host+"  created socket - client :"+ClientConnectionObject.getInstance().getClientSocket());
        boolean flag = false;
        int count = 0;
        if(host == null ){
            LogUtil.e(TAG, "comm socket host is null.");
            return;
        }
        while(!flag && count <5){
            try {
                LogUtil.d(TAG, "commSocket ---  =" + commClientSocket + "  count=" + count);
                if (commClientSocket != null) {
                    try {
                        LogUtil.d(TAG, "commSocket.isConnected() =" + commClientSocket.isConnected());
                        commClientSocket.close();
                        commClientSocket = null;
                    } catch (IOException e) {
                        e.getStackTrace();
                    }
                }
                commClientSocket = new Socket();
                commClientSocket.bind(null);

                Thread.sleep(4000);
                LogUtil.d(TAG, "Opening communication socket - port : :" + VZTransferConstants.COMM_PORT);
                commClientSocket.connect((new InetSocketAddress(host, VZTransferConstants.COMM_PORT)), VZTransferConstants.SOCKET_TIMEOUT);
                commClientSocket.setTcpNoDelay(true);
                if(commClientSocket != null && commClientSocket.isConnected()){
                    flag = true;
                    ConnectionListener connectionListener = new ClientAdapter();
                    connectionListener.clientCommConnected(commClientSocket);
                    LogUtil.d(TAG,"Client comm socket connected........");
                }
            }catch(IOException e){
                LogUtil.d(TAG, "Failed creating Client COMM socket IOException...."+e.getMessage());
                if(e.getMessage().contains("ENETUNREACH")){
                    flag = true;
                }
                e.printStackTrace();
            }catch(Exception e){
                LogUtil.d(TAG, "Failed creating Client COMM socket Exception...."+e.getMessage());

                e.printStackTrace();
            }finally{
                count ++;
            }
        }

    }

    private void openServerCommSocket() {
        try {
            if(ServerConnectionObject.getInstance().getServerCommSocket() != null){
                LogUtil.e(TAG, "Comm Server already created. Pls verify why trying to create again.");
                return;
            }

            boolean flag = true;
            int count = 0;
            while (flag && ++count < 5) {
                try {
                    LogUtil.d(TAG, "Creating Router server socket.");
                    commServerSocket = new ServerSocket( VZTransferConstants.COMM_PORT );

                    LogUtil.d(TAG, "set Reuse Address.");
                    commServerSocket.setReuseAddress(true);

                    LogUtil.d(TAG, "comm socket address: " + commServerSocket.getInetAddress().getHostAddress() + ", port: " + commServerSocket.getLocalPort());

                    if(commServerSocket == null){
                        Thread.sleep(3000);
                    }else {
                        flag = false;
                    }


                } catch (BindException be) {
                    LogUtil.e(TAG, "BindException - " + be.getMessage());
                } catch (Exception e) {
                    LogUtil.e(TAG, "Exception - " + e.getMessage());
                }
            }

            if(commServerSocket == null){
                LogUtil.e(TAG,"Server socket still null.");
                return;
            }else {
                LogUtil.d(TAG, "server socket - Created.");
                LogUtil.d(TAG, "Server: local ip : " + commServerSocket.getInetAddress().getHostName());

                ServerConnectionObject.getInstance().setServerCommSocket(commServerSocket);
            }


            while (true) {
                try {
                    LogUtil.d(TAG, "Server comm Socket -  is waiting to accept connection.");
                    commClientSocket = commServerSocket.accept();
                    commClientSocket.setTcpNoDelay(true);

                    ConnectionListener connectionListener = new ServerAdapter();
                    connectionListener.clientCommAccepted(commClientSocket);
                }catch (IOException ie) {
                    LogUtil.e(TAG, "Server Socket -  IOException: " + ie.getMessage());
                    ie.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "Failed to create COMM port...."+e.getMessage());
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG,"client comm message async task - onPostExecute.");

    }

    @Override
    protected void onPreExecute() {

    }
}
