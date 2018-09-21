package com.verizon.contenttransfer.network;

import com.verizon.contenttransfer.exceptions.ContentTransferFatalException;
import com.verizon.contenttransfer.exceptions.SocketInitializationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by kommisu on 6/11/2016.
 */
public interface ICTSocket {

    public boolean isConnected();
    public boolean isClosed();

    //applicable for Sender only
    public void initSocket( String host, int port) throws ContentTransferFatalException;
    //applicable for Receiver only
    public void initSocket( int port ) throws ContentTransferFatalException;
    public OutputStream getServerOutputStream() throws SocketInitializationException;
    public InputStream getServerInputStream() throws SocketInitializationException;
    public BufferedReader getDataReader() throws SocketInitializationException;
    public BufferedWriter getDataWriter() throws SocketInitializationException;
    //public PrintWriter getPrintWriter() throws SocketInitializationException;

    public Map<String, ICTSocket> getSocketMap();
    public void closeSocket( int port );

}
