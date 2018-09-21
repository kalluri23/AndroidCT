package com.verizon.contenttransfer.p2p.model;

import com.verizon.contenttransfer.utils.CTAnalyticUtil;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by c0bissh on 3/27/2017.
 */
public class AcceptedClientInfo {

    private Socket clientSocket;
    private OutputStream out;
    private BufferedReader in;
    private String status = "";
    private String clientIp = "";
    private String deviceName = "";
    private Socket commClientSocket;
    private PrintWriter commOut;
    private BufferedReader commIn;

    private CTAnalyticUtil ctAnalyticUtil = new CTAnalyticUtil();

    public CTAnalyticUtil getCtAnalyticUtil() {
        return ctAnalyticUtil;
    }

    public void setCtAnalyticUtil(CTAnalyticUtil ctAnalyticUtil) {
        this.ctAnalyticUtil = ctAnalyticUtil;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public AcceptedClientInfo(){

    }
    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    }

    public PrintWriter getCommOut() {
        return commOut;
    }

    public void setCommOut(PrintWriter commOut) {
        this.commOut = commOut;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
