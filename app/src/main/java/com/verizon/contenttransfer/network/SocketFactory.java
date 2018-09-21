package com.verizon.contenttransfer.network;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;

/**
 * Created by kommisu on 6/29/2016.
 */
public class SocketFactory {

    private static final String TAG = SocketFactory.class.getName();

    private SocketManager socketManager = null;

    public final SocketManager createClientSockets( boolean secure) {
        /*String type = "";
        CTGlobal ctGlobal = CTGlobal.getInstance();
        final String state = ctGlobal.getPhoneSelection();
        LogUtil.d(TAG, "Is this device a : " + state );

        if ( state.equalsIgnoreCase( VZTransferConstants.OLD_PHONE ) ) {
            type = VZTransferConstants.SENDER;
        } else {
            type = VZTransferConstants.RECEIVER;
        }*/

        if (secure) {
            //create SSL sockets
            //SslSocketManager sslSocketManager = new SslSocketManager();
        } else {
            //create regular sockets
            socketManager = new SocketManager( VZTransferConstants.SENDER );
            //socketManager.initializeSockets();
        }

        return socketManager;

    }

    public final SocketManager createServerSockets( boolean secure) {
        /*String type = "";
        CTGlobal ctGlobal = CTGlobal.getInstance();
        final String state = ctGlobal.getPhoneSelection();
        LogUtil.d(TAG, "Is this device a : " + state );

        if ( state.equalsIgnoreCase( VZTransferConstants.OLD_PHONE ) ) {
            type = VZTransferConstants.SENDER;
        } else {
            type = VZTransferConstants.RECEIVER;
        }*/

        if (secure) {
            //create SSL sockets
            //SslSocketManager sslSocketManager = new SslSocketManager();
        } else {
            //create regular sockets
            socketManager = new SocketManager( VZTransferConstants.RECEIVER );
            //socketManager.initializeSockets();
        }

        return socketManager;

    }
}
