package com.verizon.contenttransfer.utils;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.verizon.contenttransfer.adapter.ConnectionListener;
import com.verizon.contenttransfer.base.VZTransferConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by c0bissh on 3/28/2017.
 */
public class HandShakeUtil {
    private static String TAG = HandShakeUtil.class.getName();

    public static void serverSideHandShake(ConnectionListener connectionListener, Socket clientSocket, boolean isServer) {
        OutputStream out = null;
        BufferedReader in = null;
        try{
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            LogUtil.d(TAG, "Server: connection done, Client IP : [" + clientIp + "] connection type : " + CTGlobal.getInstance().getConnectionType());

            out = clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            LogUtil.d(TAG, "Waiting to read from client.");
            String inputMessage = in.readLine();

            LogUtil.e(TAG, "Received from Client 1:" + inputMessage);
            VersionCheckProperty versionCheckProperty = new VersionCheckProperty(inputMessage);

            String reply = VZTransferConstants.CONNECTION_MESSAGE_PREFIX
                    + VersionManager.getVersion()
                    + "#" + VersionManager.getMinSupportedVersion()
                    + "#" + VZTransferConstants.NEW_DEVICE_AVAILABLE_SPACE+ Utils.bytesAvailable()
                    + VZTransferConstants.CRLF;
            out.write(reply.getBytes());
            out.flush();
            LogUtil.d(TAG, "Sent reply " + reply);

            LogUtil.d(TAG, "Doing version check ");
            int result = VersionManager.compareVersion(versionCheckProperty.getBuildVersion(),
                    versionCheckProperty.getMinSupported(),
                    versionCheckProperty.getOsFlag());
            LogUtil.d(TAG,"Version check result.......... :"+result);
            if(result == 0) {
                LogUtil.d(TAG,"Version check success.. client accepted... osFlag="+versionCheckProperty.getOsFlag());
                boolean isCross = (versionCheckProperty.getOsFlag().equalsIgnoreCase(VZTransferConstants.ANDROID)?false:true);
                connectionListener.clientAccepted(clientSocket, clientIp,isCross);
            }
            else{
                // result is : lower 1, higher -1, equal 0.
                LogUtil.d(TAG, "Serverside Version check failed.. Show upgrade prompt. - result=" + result);

                handshakeFailedCallback(versionCheckProperty.getBuildVersion(), versionCheckProperty.getMinSupported(), result);
                LogUtil.d(TAG, "Client side Version check failed.. client not accepted. - result=" + result);
            }
        }catch (Exception e){
            LogUtil.e(TAG, "Server side Version check exception :" + e.getMessage());
        }finally {
        }
    }
    public static void clientSideHandShake(ConnectionListener connectionListener, Socket clientSocket, boolean isServer) {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            LogUtil.d(TAG, "Start version check flow.");
            String serverIp = clientSocket.getInetAddress().getHostAddress();
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            LogUtil.d(TAG, "Start version check flow. serverIp="+serverIp);
            if(clientSocket != null){
                String secretCode = VZTransferConstants.CONNECTION_MESSAGE_PREFIX
                        + VersionManager.getVersion()
                        + "#" + VersionManager.getMinSupportedVersion()
                        + "#" + VZTransferConstants.NEW_DEVICE_AVAILABLE_SPACE+ Utils.bytesAvailable()
                        + VZTransferConstants.CRLF;
                String fromServer = null;
                int count = 0;

                while (fromServer == null && ++count < 5) {
                    out.println(secretCode);
                    LogUtil.d(TAG, "Sending Message to Server : " + secretCode);
                    out.flush();

                    LogUtil.d(TAG, "Waiting to receive ACK from server:");
                    fromServer = in.readLine();
                    Thread.sleep(3000);
                }

                if (fromServer == null) {
                    LogUtil.d(TAG, "ACK received  is null");

                }else {
                    LogUtil.d(TAG, "ACK received " + fromServer);
                    VersionCheckProperty versionCheckProperty = new VersionCheckProperty(fromServer);

                    int result = VersionManager.compareVersion(versionCheckProperty.getBuildVersion(),
                            versionCheckProperty.getMinSupported(),
                            versionCheckProperty.getOsFlag());
                    LogUtil.d(TAG, "Version check result. : " + result);
                    if(result == 0){
                        LogUtil.e(TAG, "Sending broadcasts with clientIp= " + serverIp+" osFlag="+versionCheckProperty.getOsFlag());
                        boolean isCross = (versionCheckProperty.getOsFlag().equals(VZTransferConstants.ANDROID)?false:true);
                        connectionListener.clientConnected(clientSocket, serverIp, CTGlobal.getInstance().getConnectionType(), isCross);
                    }
                    else{
                        LogUtil.e(TAG, "Sending version mismatch intent - result :" + result);
                        handshakeFailedCallback(versionCheckProperty.getBuildVersion(), versionCheckProperty.getMinSupported(), result);
                    }
                }
            }else {
                LogUtil.d(TAG, "Client socket is null");
            }

        }catch (Exception e ) {
            e.printStackTrace();
        } finally {
        }
    }

    private static void handshakeFailedCallback(String buildVersion, String minSupported, int result) {
        LogUtil.d(TAG,"minSupported ="+minSupported);
        if(minSupported == null || minSupported.length() == 0 || minSupported.contains("space")){
            result = -1;
        }
        Intent versionIntent = new Intent(VZTransferConstants.VERSION_CHECK_FAILED);
        versionIntent.putExtra("resultCode", result);
        versionIntent.putExtra("version", buildVersion);
        versionIntent.putExtra("minSupported", minSupported);

        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(versionIntent);


    }

    public static void handshakeSuccessCallback(final String hostIp, boolean isServer) {
        if(!Utils.isReceiverDevice()) {
            Utils.startP2PClientIos(hostIp);
        }else {
            Utils.startP2PServerIos(hostIp);
        }
        Intent intent = new Intent(VZTransferConstants.VERSION_CHECK_SUCCESS);
        LogUtil.e(TAG, "callBack VersionCheckSuccess - sending version check success callback.  host :" + hostIp);
        intent.putExtra("ipAddressOfServer", hostIp);
        LocalBroadcastManager.getInstance(CTGlobal.getInstance().getContentTransferContext()).sendBroadcast(intent);

    }
}
