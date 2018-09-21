package com.verizon.contenttransfer.network;

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.exceptions.ContentTransferFatalException;
import com.verizon.contenttransfer.exceptions.SocketInitializationException;
import com.verizon.contenttransfer.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kommisu on 6/10/2016.
 */
public class SenderSocket implements ICTSocket {

    private static final String TAG = SenderSocket.class.getName();

    private boolean socketInitialized = false;
    private Socket socket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;

    private BufferedWriter dataWriter = null;
    private BufferedReader dataReader = null;
    private PrintWriter dataPrintWriter = null;
    private Map<String, ICTSocket> senderSocketMap = new HashMap<String, ICTSocket>();
    private final int RETRY_COUNT = 6;

    public Map<String, ICTSocket> getSocketMap() {
        return senderSocketMap;
    }

    public boolean isInitialized() {
        if ( null == this.socket && !socketInitialized ) {
            return false;
        }

        return true;
    }

    public final ICTSocket getSocket(final int port ) throws ContentTransferFatalException {
        ICTSocket localSocket = senderSocketMap.get( Integer.toString(port) );
        if ( null == localSocket ) {
            throw new ContentTransferFatalException( "Connection cannot be established *****?");
        }
        return localSocket;
    }

    public final void initSocket( final int port ) throws ContentTransferFatalException {
        throw new ContentTransferFatalException( "What are you trying to initialize *****?");
    }

    private SenderSocket senderSocket = null;
    public final void initSocket( final String host, final int port) {
        senderSocket = new SenderSocket();
        senderSocket.initializeSocket( host, port );
        senderSocketMap.put( Integer.toString(port), senderSocket );
    }

    @Override
    public boolean isConnected() {
        //Socket isConnect status does not change after closing socket,
        //so check isClosed to determine connect status
        if ( socket.isConnected() && ! socket.isClosed() ) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isClosed() {
        if ( socket.isClosed() ) {
            return true;
        }
        return false;
    }

    private final void initializeSocket(String host, int port ) {
        int count = 0;

        try {
            socket = new Socket();
            socket.bind(null);
            socket.setReuseAddress(true);
            socket.setTcpNoDelay(true);

            //This is a infinite loop be careful before changing the logic
            while ( true ) {
                socket.connect((new InetSocketAddress(host, port)), VZTransferConstants.SOCKET_TIMEOUT);
                boolean boundStatus = (socket.isBound() && !socket.isClosed() );
                LogUtil.d(TAG, "Bound status of the socket on port : " + port );
                boolean connected = ( socket.isConnected() && !socket.isClosed() );
                LogUtil.d(TAG, "Connect status of the socket on port : " + port );

                if ( connected ) {
                    //Get out of the loop if connected
                    break;
                }
                count++;
                if ( count < RETRY_COUNT ) {
                    LogUtil.e(TAG, "Failed to connect, retry in 5 secs on port : " + port);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LogUtil.e(TAG, "Failed to init connection on port : " + port );
                        //e.printStackTrace();
                    }
                } else {
                    //If we crossed five attempts throw exception
                    throw new ContentTransferFatalException("Failed to connect on : " + port );
                }
            }

            outStream = socket.getOutputStream();
            inStream = socket.getInputStream();

            dataReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //dataWriter = new PrintWriter(socket.getOutputStream(), true);
            dataWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));

            //set initialized status so we can read and write
            socketInitialized = true;
        } catch (SocketException se ) {
            LogUtil.e(TAG, "Failed to initialize Server Socket -- " + se.getMessage() );
            se.printStackTrace();
        } catch (IOException ioe ) {
            LogUtil.e(TAG, "Failed to initialize Server Socket -- " + ioe.getMessage() );
            ioe.printStackTrace();
        } catch (ContentTransferFatalException ctfe ) {
            LogUtil.e(TAG, "Failed to initialize Server Socket -- " + ctfe.getMessage() );
            ctfe.printStackTrace();
        }

    }

    public OutputStream getServerOutputStream() throws SocketInitializationException {
        if ( isInitialized() ) {
            return outStream;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public InputStream getServerInputStream() throws SocketInitializationException {
        if ( isInitialized() ) {
            return inStream;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public BufferedReader getDataReader() throws SocketInitializationException {
        if ( isInitialized() ) {
            return dataReader;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public BufferedWriter getDataWriter() throws SocketInitializationException {
        if ( isInitialized() ) {
            return dataWriter;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public PrintWriter getDataPrintWriter() throws SocketInitializationException {
        if ( isInitialized() ) {
            return dataPrintWriter;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public void closeSocket( int port ) {
        SenderSocket destSocket = (SenderSocket)senderSocketMap.get( Integer.toString( port) );
        destSocket.close();
    }

    private void close() {
        if ( socket != null ) {
            LogUtil.d(TAG, "Sending socket is still connected, closing");
            try {
                this.socket.close();
                this.socket = null;
            } catch (IOException ioe) {
                LogUtil.e(TAG, "Failed closing Sending socket : " + ioe.getMessage() );
                ioe.printStackTrace();
            }
        }
    }

    public void writeMessageToSocket(String msg) throws IOException {
        LogUtil.d(TAG, "Sender writing message to socket : " + msg);
        if( null != dataWriter ) {
            dataWriter.write(msg);
            dataWriter.write(VZTransferConstants.CRLF);
            dataWriter.flush();
        }
    }

    public String readMessageFromSocket() {
        LogUtil.d(TAG, "Sender waiting to read message from socket....");
        try {
            String msg = dataReader.readLine();
            return msg;
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to read message from socket : " + e.getMessage() );
            e.printStackTrace();
        }
        return null;
    }
}
