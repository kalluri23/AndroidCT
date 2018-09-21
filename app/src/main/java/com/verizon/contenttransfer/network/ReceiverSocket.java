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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kommisu on 6/10/2016.
 */
public class ReceiverSocket implements ICTSocket {

    private static final String TAG = ReceiverSocket.class.getName();

    private boolean socketInitialized = false;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private OutputStream out = null;
    private InputStream in = null;

    private BufferedWriter dataWriter = null;
    private BufferedReader dataReader = null;
    private PrintWriter dataPrintWriter = null;

    private static Map<String, ICTSocket> socketMap = new HashMap<String, ICTSocket>();

    public Map<String, ICTSocket> getSocketMap() {
        return socketMap;
    }

    public boolean isInitialized() {
        if ( (null == this.socket || null == this.serverSocket) && !socketInitialized ) {
            return false;
        }

        return true;
    }

    public final ICTSocket getSocket(final int port ) throws ContentTransferFatalException {
        ICTSocket lclSocket = socketMap.get( Integer.toString(port) );
        if ( null == lclSocket ) {
            initSocket( port );
            lclSocket = receiverSocket;
        }
        return lclSocket;
    }
    private ReceiverSocket receiverSocket = null;

    public final void initSocket(final String host, final int port ) throws ContentTransferFatalException {
        initSocket( port );
    }

    @Override
    public boolean isConnected() {
        //isBound status does change after status is closed, so to determine connect status
        //check to make sure socket is not closed
        if ( socket.isBound() && ! socket.isClosed() ) {
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

    public final void initSocket( final int port ) throws ContentTransferFatalException {
        receiverSocket = new ReceiverSocket();
        receiverSocket.initializeSocket( port, null );
        socketMap.put( Integer.toString(port), receiverSocket );
    }

    private InetAddress inetAddress = null;
    public final void initHotspotSocket( final int port ) throws ContentTransferFatalException {
        receiverSocket = new ReceiverSocket();
        try {
            inetAddress = InetAddress.getByName("192.168.49.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        receiverSocket.initializeSocket( port, inetAddress );
        socketMap.put( Integer.toString(port), receiverSocket );
    }

    private final void initializeSocket( int port, InetAddress host ) {

        try {
            //Initialize Server socket
            socket = new Socket();

            if ( null != host ) {
                //Using default value for "0" for backlog queue
                //The backlog argument must be a positive value greater than 0.
                //If the value passed if equal or less than 0, then the default value will be assumed.
                serverSocket = new ServerSocket( port, 0, host );
            } else {
                serverSocket = new ServerSocket( port );
            }
            serverSocket.setSoTimeout( VZTransferConstants.SOCKET_TIMEOUT );
            //This property will allows us to reconnect if the socket is in connection
            //timeout state
            serverSocket.setReuseAddress( true );
            socket.setTcpNoDelay(true);

            //This a blocking call, until the connection is made
            socket = serverSocket.accept();

            out = socket.getOutputStream();
            in = socket.getInputStream();

            dataReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //dataWriter = new PrintWriter(socket.getOutputStream(), true);
            dataWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));

            //Set initialized to true so that we can start read write
            socketInitialized = true;
        } catch (SocketException se ) {
            LogUtil.e(TAG, "Failed to initialize Server Socket -- " + se.getMessage() );
            se.printStackTrace();
        } catch (IOException ioe ) {
            LogUtil.e(TAG, "Failed to initialize Server Socket -- " + ioe.getMessage() );
            ioe.printStackTrace();
        }
    }

    public OutputStream getServerOutputStream() throws SocketInitializationException {
        if ( isInitialized() ) {
            return out;
        } else {
            throw new SocketInitializationException( "Socket not initialized yet.");
        }
    }

    public InputStream getServerInputStream() throws SocketInitializationException {
        if ( isInitialized() ) {
            return in;
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
        ReceiverSocket destSocket = (ReceiverSocket)socketMap.get( Integer.toString( port) );
        destSocket.close();
    }

    private final void close() {
        try {
            LogUtil.d(TAG, "Receiving socket is still connected, closing");
            if ( null != socket ) {
                this.socket.close();
                this.socket = null;
            }
            if ( null != serverSocket ) {
                this.serverSocket.close();
                this.serverSocket = null;
            }
        } catch (IOException ioe ) {
            LogUtil.e(TAG, "Failed to close socket : " + ioe.getMessage() );
            ioe.printStackTrace();
        }
    }

    public void writeMessageToSocket(String msg) throws IOException {
        LogUtil.d(TAG, "Writing message to socket : " + msg);
        if(null != dataWriter) {
            dataWriter.write(msg);
            dataWriter.write(VZTransferConstants.CRLF);
            dataWriter.flush();
        }
    }

    public String readMessageFromSocket() {
        LogUtil.d(TAG, "Waiting to read message from socket....");
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
