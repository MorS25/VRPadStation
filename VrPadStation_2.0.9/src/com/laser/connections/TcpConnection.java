package com.laser.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;

public class TcpConnection extends MAVLinkConnection {
	
	private Socket socket;
	
	private BufferedOutputStream mavOutStream;
	private BufferedInputStream mavInStream;

	private String serverIP;
	private int serverPort;

	public TcpConnection(Context context) {
		super(context);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException 
	{
		getTCPStream();		
	}
	
	private void getTCPStream() throws UnknownHostException, IOException {
		InetAddress serverAddress = InetAddress.getByName(serverIP);
		socket = new Socket(serverAddress, serverPort);
		mavOutStream = new BufferedOutputStream((socket.getOutputStream()));
		mavInStream = new BufferedInputStream(socket.getInputStream());
	}


	@Override
	protected void readDataBlock() throws IOException
	{
		bytesRead = mavInStream.read(readData);		
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (mavOutStream!=null) 
		{
			mavOutStream.write(buffer);
			mavOutStream.flush();
		}
	}
	
	@Override
	protected void closeConnection() throws IOException {
		socket.close();		
	}
	
	@Override
	protected void getPreferences(SharedPreferences prefs) {
		serverIP = prefs.getString("pref_server_ip", "");
		serverPort = Integer.parseInt(prefs.getString("pref_server_port", "0"));
	}


}
