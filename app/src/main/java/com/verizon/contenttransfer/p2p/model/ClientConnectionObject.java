package com.verizon.contenttransfer.p2p.model;

import android.os.Build;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.CTCreateClient;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Created by c0bissh on 3/27/2017.
 */
public class ClientConnectionObject {
    private static String TAG = ClientConnectionObject.class.getName();
    private static ClientConnectionObject instance;
    private Socket clientSocket = null;
    private String status = "";
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    private Socket commClientSocket;
    private PrintWriter commOut;
    private BufferedReader commIn;
    private String host;
    private CTAnalyticUtil ctAnalyticUtil = new CTAnalyticUtil();

    public ClientConnectionObject(){

    }

    public CTAnalyticUtil getCtAnalyticUtil() {
        return ctAnalyticUtil;
    }

    public void setCtAnalyticUtil(CTAnalyticUtil ctAnalyticUtil) {
        this.ctAnalyticUtil = ctAnalyticUtil;
    }

    public static ClientConnectionObject getInstance() {
        if (instance == null) {
            instance = new ClientConnectionObject();
        }
        return instance;
    }

    public void destroySocket(){
        isConnected = false;
        closeClientSocket();
        instance = null;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }


    public boolean isConnected() {
        return isConnected;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        LogUtil.d(TAG,"set new client socket : clientSocket = "+clientSocket);

        this.clientSocket = clientSocket;
        this.isConnected = true;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public Socket getCommClientSocket() {
        return commClientSocket;
    }

    public void setCommClientSocket(Socket commClientSocket) {
        this.commClientSocket = commClientSocket;
    }

    public BufferedReader getCommIn() {
        return commIn;
    }

    public void setCommIn(BufferedReader commIn) {
        this.commIn = commIn;
        LogUtil.d(TAG,"Set comm input stream.");
    }

    public PrintWriter getCommOut() {
        return commOut;
    }

    public void setCommOut(PrintWriter commOut) {
        this.commOut = commOut;
    }
    public void closeClientSocket() {
        if(clientSocket != null){
            try {
                clientSocket.close();
                in.close();
                out.close();
                clientSocket = null;
                LogUtil.d(TAG, "Client socket - closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(commClientSocket != null){
            try {
                commClientSocket.close();
                commIn.close();
                commOut.close();
                commClientSocket = null;
                LogUtil.d(TAG, "Client comm socket - closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createClient( String serverIp, String altServerIp){
        CTCreateClient ctCreateClient = new CTCreateClient(serverIp,altServerIp );
        LogUtil.d(TAG, "Start CTCreateClient async task.");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ctCreateClient.executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            ctCreateClient.execute();
        }
    }

    public void createCommSocket(){
        LogUtil.d(TAG, "Comm socket opening now...");
        Utils.openCommSockets(VZTransferConstants.CLIENT_COMM_SOCKET, getHost());
    }
}
