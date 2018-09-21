package com.verizon.contenttransfer.p2p.model;

import android.os.Build;
import android.util.Log;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.CTCreateServer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by c0bissh on 3/27/2017.
 */
public class ServerConnectionObject {
    private static String TAG = ServerConnectionObject.class.getName();
    private static ServerConnectionObject instance;
    private ServerSocket serverSocket;
    private ServerSocket serverCommSocket;
    private Map<String, AcceptedClientInfo> clients = new HashMap<String, AcceptedClientInfo>();


    public ServerConnectionObject(){

    }

    public static ServerConnectionObject getInstance() {
        if (instance == null) {
            instance = new ServerConnectionObject();
        }
        return instance;
    }

    public void createServer(InetAddress host){
        CTCreateServer server = new CTCreateServer(host);
        LogUtil.d(TAG, "start CTCreate Server - Launching CT Create Server.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            server.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            server.execute();
        }
    }

    public void createCommSocket(){
        LogUtil.d(TAG, "Comm socket opening now...");
        Utils.openCommSockets(VZTransferConstants.SERVER_COMM_SOCKET, null);
    }

    public void destroySocket(){

        closeServerSocket();
        if(clients != null ) {
            this.clients.clear();
            clients = new HashMap<String, AcceptedClientInfo>();
        }
        instance = null;
    }

    public ArrayList<AcceptedClientInfo> getClients(){
        ArrayList<AcceptedClientInfo> acceptedClients = new ArrayList<AcceptedClientInfo>(clients.values());
        return acceptedClients;
    }

    public void closeServerSocket() {
        if(serverSocket != null){
            try {
                closeAllAcceptedClientConnection();
                serverSocket.close();
                serverSocket = null;
                LogUtil.d(TAG, "server socket - closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(serverCommSocket != null){
            try {
                serverCommSocket.close();
                serverCommSocket = null;
                LogUtil.d(TAG, "server comm socket - closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerCommSocket() {
        return serverCommSocket;
    }

    public void setServerCommSocket(ServerSocket serverCommSocket) {
        this.serverCommSocket = serverCommSocket;
    }
    public AcceptedClientInfo getAcceptedClientByIp(String clientIp) {
        if(clients != null && clients.size() > 0 ){
            if( clientIp != null) {
                return clients.get(clientIp);
            }else {
                //Return first item from the HashMap
                Map.Entry<String, AcceptedClientInfo> entry=clients.entrySet().iterator().next();
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<String, AcceptedClientInfo> getAcceptedClients() {
        return clients;
    }
    public void addNewAcceptedClient(String clientIp, AcceptedClientInfo clientInfo) {
        if(clients != null) {
            if(clients.get(clientIp)!=null){
                clients.remove(clientIp);
            }
            clients.put(clientIp, clientInfo);
            LogUtil.d(TAG,"New client added to clients list. - tot clients :"+clients.size());
        }
    }

    public void updateAcceptedClientStatus(String clientIp, String status) {

        Log.d(TAG, "Client status updated..");
        if(clients != null && clientIp != null) {
            if(clients.get(clientIp) != null){
                clients.get(clientIp).setStatus(status);
            }else {
                LogUtil.d(TAG, "Client is not found with IP :" + clientIp);
            }
        }
    }
    public void updateAcceptedDeviceName(String clientIp, String name) {

        Log.d(TAG, "Client status updated..");
        if(clients != null && clientIp != null) {
            if(clients.get(clientIp) != null){
                clients.get(clientIp).setDeviceName(name);
            }else {
                LogUtil.d(TAG, "Client is not found with IP :" + clientIp);
            }
        }
    }

    public void deleteClientOnDisconnectBeforeTransfer(String clientIp) {

        Log.d(TAG, "delete Client On Disconnect Before Transfer..");
        if(clients != null && clientIp != null) {
            if(clients.get(clientIp) != null){
                clients.remove(clientIp);
                LogUtil.d(TAG, "Removing client from the clients list  :" + clientIp);
            }else {
                LogUtil.d(TAG, "Client is not found with IP :"+clientIp);
            }
        }
    }
    public void updateAcceptedClientCommSocket(String clientIp,
                                               Socket commClientSocket,
                                               PrintWriter commOut,
                                               BufferedReader commIn) {

        Log.d(TAG, "Client comm socket updated..");
        if(clients != null && clientIp != null) {
            if(clients.get(clientIp) != null){
                clients.get(clientIp).setCommClientSocket(commClientSocket);
                clients.get(clientIp).setCommOut(commOut);
                clients.get(clientIp).setCommIn(commIn);
            }else {
                LogUtil.d(TAG, "Client is not found with IP :"+clientIp);
            }
        }else {
            LogUtil.e(TAG, "client socket is null for client ip :"+clientIp);
            displayAllAcceptedClients();
        }
    }
    public void displayAllAcceptedClients() {
        if (clients != null) {
            Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
                Log.d(TAG, "Key : " + entry.getKey() + "\n Value :" + entry.getValue().getClientIp() + "\n Status :" + entry.getValue().getStatus());
            }

        }else {
            Log.d(TAG, "Clients map is empty.");
        }
    }
    public void closeServerSocketByIp(String clientIp) {
        if (clients != null) {
            Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = clients.entrySet().iterator();
            try {
                displayAllAcceptedClients();
                while (iterator.hasNext()) {
                    Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
                    AcceptedClientInfo acceptedClientInfo = entry.getValue();
                    if(acceptedClientInfo != null){
                        if(acceptedClientInfo.getClientIp().equals(clientIp)){
                            acceptedClientInfo.getIn().close();
                            acceptedClientInfo.getOut().close();
                            acceptedClientInfo.getClientSocket().close();

                            acceptedClientInfo.getCommIn().close();
                            acceptedClientInfo.getCommOut().close();
                            acceptedClientInfo.getCommClientSocket().close();
                            LogUtil.d(TAG, "Closing connection with IP.... :" + clientIp);
                            updateAcceptedClientStatus(clientIp, VZTransferConstants.DATA_TRANSFER_INTERRUPTED_BY_USER);
                            break;
                        }
                        LogUtil.d(TAG, "Closed Client IP:" + entry.getValue().getClientIp());
                    }
                }
                displayAllAcceptedClients();
            } catch (Exception e) {
                LogUtil.e(TAG,"exception on closing sockets :"+e.getMessage());
            }
        }else {
            Log.d(TAG, "Clients map is empty.");
        }
    }
    public void closeAllAcceptedClientConnection() {
        if (clients != null) {
            Iterator<Map.Entry<String, AcceptedClientInfo>> iterator = clients.entrySet().iterator();
            try {
                displayAllAcceptedClients();
                while (iterator.hasNext()) {
                    Map.Entry<String, AcceptedClientInfo> entry = (Map.Entry<String, AcceptedClientInfo>) iterator.next();
                    AcceptedClientInfo acceptedClientInfo = entry.getValue();
                    if(acceptedClientInfo != null){
                        acceptedClientInfo.getIn().close();
                        acceptedClientInfo.getOut().close();
                        acceptedClientInfo.getClientSocket().close();

                        acceptedClientInfo.getCommIn().close();
                        acceptedClientInfo.getCommOut().close();
                        acceptedClientInfo.getCommClientSocket().close();
                        LogUtil.d(TAG, "Closed Client IP:" + entry.getValue().getClientIp());
                    }
                }
            } catch (Exception e) {
                LogUtil.e(TAG,"exception on closing sockets :"+e.getMessage());
            }
        }else {
            Log.d(TAG, "Clients map is empty.");
        }
    }
    public boolean isConnectionAvailable()
    {
        if(clients != null && clients.size()>0){
            return true;
        }
        return false;
    }
}
