package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.p2p.model.ClientConnectionObject;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by c0bissh on 5/1/2017.
 */
public class SocketUtil {
    private static String TAG = SocketUtil.class.getName();
    public static final int SOCKET_TIMEOUT = 5000;
    public static boolean isClient(){
        return ClientConnectionObject.getInstance().isConnected();
    }

    public static PrintWriter getCommWriter(String hostIp){

        if(ClientConnectionObject.getInstance().isConnected()) {
            LogUtil.d(TAG,"its a client device : hostIP="+hostIp);
            return ClientConnectionObject.getInstance().getCommOut();
        }else {
            LogUtil.d(TAG,"its a Server device : hostIP="+hostIp);
            AcceptedClientInfo acceptedClientInfo = ServerConnectionObject.getInstance().getAcceptedClientByIp(hostIp);
            if (acceptedClientInfo != null) {
                return acceptedClientInfo.getCommOut();
            }else {
                LogUtil.e(TAG, "Client not found . host ip :"+hostIp);
            }
        }
        return null;
    }
    public static ConnectionObject getConnectionObject(String hostIp){
        ConnectionObject connectionObject = null;
        if(ClientConnectionObject.getInstance().isConnected()) {
            LogUtil.d(TAG,"its a client device : return Connection Object for hostIP="+hostIp);
            connectionObject = new ConnectionObject();
            connectionObject.setServer(false);
            connectionObject.setClientConnectionObject(ClientConnectionObject.getInstance());
        }else {
            LogUtil.d(TAG,"its a Server device : return Connection Object for hostIP="+hostIp);
            AcceptedClientInfo acceptedClientInfo = ServerConnectionObject.getInstance().getAcceptedClientByIp(hostIp);
            if (acceptedClientInfo != null) {
                connectionObject = new ConnectionObject();
                connectionObject.setServer(true);
                connectionObject.setAcceptedClientInfo(acceptedClientInfo);
            }else {
                LogUtil.e(TAG, "Client not found . host ip :"+hostIp);
            }
        }
        return connectionObject;
    }
    public static Socket getClientSocket(String hostIp){

        if(ClientConnectionObject.getInstance().isConnected()) {
            LogUtil.d(TAG,"its a client device : hostIP="+hostIp);
            return ClientConnectionObject.getInstance().getClientSocket();
        }else {
            LogUtil.d(TAG,"its a Server device : hostIP="+hostIp);
            AcceptedClientInfo acceptedClientInfo = ServerConnectionObject.getInstance().getAcceptedClientByIp(hostIp);
            if (acceptedClientInfo != null) {
                return acceptedClientInfo.getClientSocket();
            }else {
                LogUtil.e(TAG, "Client not found . host ip :"+hostIp);
            }
        }
        return null;
    }
    public static CTAnalyticUtil getCtAnalyticUtil(String hostIp){
        if(ClientConnectionObject.getInstance().isConnected()) {
            return ClientConnectionObject.getInstance().getCtAnalyticUtil();
        }else {
            AcceptedClientInfo acceptedClientInfo = ServerConnectionObject.getInstance().getAcceptedClientByIp(hostIp);
            if (acceptedClientInfo != null) {
                return acceptedClientInfo.getCtAnalyticUtil();
            }else {
                LogUtil.e(TAG, "Client not found . host ip.. :"+hostIp);
            }
        }
        return null;
    }
    public static List<Socket> getConnectedClients(){
        List<Socket> socketList = new ArrayList<Socket>();
        if(ClientConnectionObject.getInstance().isConnected()) {
            socketList.add(ClientConnectionObject.getInstance().getClientSocket());
        }else {
            LogUtil.d(TAG, "Total accepted clients size :" + ServerConnectionObject.getInstance().getAcceptedClients().size());
            if(ServerConnectionObject.getInstance().getAcceptedClients() != null && ServerConnectionObject.getInstance().getAcceptedClients().size()>0){
                Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = ServerConnectionObject.getInstance().getAcceptedClients().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
                    LogUtil.d(TAG, "...Client ip : " + entry.getKey() + " ...Status :" + entry.getValue().getStatus());
                    socketList.add(entry.getValue().getClientSocket());
                }
            }
        }
        return socketList;
    }

    public static ServerSocket getServerSocket(){
        if(ClientConnectionObject.getInstance().isConnected()) {
            return null;
        }else {
            return ServerConnectionObject.getInstance().getServerSocket();
        }
    }

    public static void destroyAllSocket() {
        ClientConnectionObject.getInstance().destroySocket();
        ServerConnectionObject.getInstance().destroySocket();
    }

    public static void disconnectAllSocket() {
        if(ClientConnectionObject.getInstance().isConnected()){
            ClientConnectionObject.getInstance().closeClientSocket();
            LogUtil.d(TAG, "Client dis connected.");
        }else {
            ServerConnectionObject.getInstance().closeServerSocket();
            LogUtil.d(TAG, "server dis connected.");
        }
    }
    public static void disconnectSocketByIp (String clientIp) {
        if(ClientConnectionObject.getInstance().isConnected()){
            ClientConnectionObject.getInstance().closeClientSocket();
            LogUtil.d(TAG, "Client side client socket disconnected with ip :"+clientIp);
        }else {
            ServerConnectionObject.getInstance().closeServerSocketByIp(clientIp);
            LogUtil.d(TAG, "server side client socket disconnected with ip :"+clientIp);
        }
    }
    public static Socket connectToServer(String serverIp) {
        LogUtil.d(TAG, "Opening client socket to - " + serverIp);
        Socket clientSocket = null;
        boolean flag = true;
        int count =0;
        do{
            try {
                clientSocket = new Socket();
                Thread.sleep(5000);
                clientSocket.bind(null);
                clientSocket.setReuseAddress(true);
                clientSocket.setTcpNoDelay(true);
                LogUtil.e(TAG, "connecting to Server ip :" + serverIp+"  - count ="+count);
                clientSocket.connect((new InetSocketAddress(serverIp, VZTransferConstants.DISCOVERY_PORT)), SOCKET_TIMEOUT);
                if (clientSocket.isConnected()) {
                    LogUtil.e(TAG, "connection success to " + serverIp+"  - count ="+count);
                    flag = false;
                    break;
                }
            }catch (InterruptedException e) {
                count++;
                e.printStackTrace();
            }catch (IOException e){
                count++;
                LogUtil.d(TAG, "Exception ***: " + e.getMessage());
                if(clientSocket != null) {
                    try {
                        clientSocket.close();
                        clientSocket = null;
                    } catch (Exception e1) {
                        LogUtil.d(TAG, "Closing socket b4 retry :" + e1.getMessage());
                    }
                }
                LogUtil.e(TAG, "Failed.Trying again - count :" + count);
            }
            LogUtil.e(TAG, "Try to connect to host :" + count);
        }while(flag && count <3);
        return clientSocket;
    }
}
