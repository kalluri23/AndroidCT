package com.verizon.contenttransfer.adapter;

import java.net.Socket;

public interface ConnectionListener {

	public void clientAccepted(Socket clientSocket, String clientIp, boolean isCross);
	public void clientCommAccepted(Socket clientSocket);

	public void clientConnected(Socket clientSocket, String host, String connType, boolean isCross);
	public void clientCommConnected(Socket clientSocket);

	public void clientDisconnected(Socket clientSocket, String clientIp, String clientMessage);
}
