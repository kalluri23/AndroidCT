package com.verizon.contenttransfer.network;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.exceptions.ContentTransferFatalException;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.Map;

/**
 * Created by kommisu on 6/20/2016.
 */
public class SocketManager {

    private static final String TAG = SocketManager.class.getName();

    //private static ReceiverSocket receiverSocket = null;
    //private static SenderSocket senderSocket = null;

    private ICTSocket activeSocket = null;
    private boolean multiSocket = false;
    private String state = null;

    private SocketManager() {
        //Cannot initialize without specifying type
    }

    public SocketManager( final String type ) {
        init( type );
    }

    public final void init( final String type ) {
        if ( null != type && type.length() > 0) {
            if ( type.equalsIgnoreCase( VZTransferConstants.SENDER ) ) {
                state = VZTransferConstants.SENDER;
                activeSocket = new SenderSocket();
            } else if ( type.equalsIgnoreCase( VZTransferConstants.RECEIVER ) ) {
                state = VZTransferConstants.RECEIVER;
                activeSocket = new ReceiverSocket();
            }
        }
    }


    public final Map<String, ICTSocket> initializeSockets(String host) {
    //public final ICTSocket initializeSockets( String host) {
        if ( multiSocket ) {
            //For each port number create a socket
            //This feature is not supported yet
        } else {
            try {
                for ( VZTransferConstants.CTPorts port : VZTransferConstants.CTPorts.values() ) {
                    LogUtil.d(TAG, "Opening socket on port : " + port.value());
                    if (state.equalsIgnoreCase(VZTransferConstants.SENDER)) {
                        if ( null == host ) {
                            throw new ContentTransferFatalException("Host is NULL");
                        }
                        activeSocket.initSocket( host, port.value() );
                    } else if (state.equalsIgnoreCase(VZTransferConstants.RECEIVER)) {
                        activeSocket.initSocket( port.value() );
                    }
                }
            } catch (ContentTransferFatalException ctfe ) {
                LogUtil.e(TAG, "Failed to initialize sock : " + ctfe.getMessage() );
                ctfe.printStackTrace();
            }
        }

        return activeSocket.getSocketMap();
        //return activeSocket;
    }

    public final void closeSockets( ) {
        try {
            for (VZTransferConstants.CTPorts port : VZTransferConstants.CTPorts.values()) {
                LogUtil.d(TAG, "Opening socket on port : " + port.value());
                ICTSocket rSocket = null;
                if (state.equalsIgnoreCase(VZTransferConstants.SENDER)) {
                    rSocket = ((SenderSocket)activeSocket).getSocket(port.value());
                } else if (state.equalsIgnoreCase(VZTransferConstants.RECEIVER)) {
                    rSocket = ((ReceiverSocket)activeSocket).getSocket(port.value());
                }
                rSocket.closeSocket(port.value());
            }
        } catch (ContentTransferFatalException ctfe ) {
            LogUtil.e(TAG, "Failed to close sock : " + ctfe.getMessage() );
            ctfe.printStackTrace();
        }
    }

}

