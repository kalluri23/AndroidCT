package com.verizon.contenttransfer.utils;

import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.p2p.model.ClientConnectionObject;

/**
 * Created by c0bissh on 9/25/2017.
 */

public class ConnectionObject {

    private boolean isServer;
    private AcceptedClientInfo acceptedClientInfo;
    private ClientConnectionObject clientConnectionObject;


    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public AcceptedClientInfo getAcceptedClientInfo() {
        return acceptedClientInfo;
    }

    public void setAcceptedClientInfo(AcceptedClientInfo acceptedClientInfo) {
        this.acceptedClientInfo = acceptedClientInfo;
    }

    public ClientConnectionObject getClientConnectionObject() {
        return clientConnectionObject;
    }

    public void setClientConnectionObject(ClientConnectionObject clientConnectionObject) {
        this.clientConnectionObject = clientConnectionObject;
    }



}
