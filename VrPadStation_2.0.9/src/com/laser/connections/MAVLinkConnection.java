package com.laser.connections;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.laser.app.VrPadStationApp;
import com.laser.helpers.file.FileStream;
import com.laser.utils.LaserConstants;

public abstract class MAVLinkConnection extends Thread {

	protected abstract void openConnection()		  throws UnknownHostException, IOException;
	protected abstract void closeConnection() 		  throws IOException;
	protected abstract void readDataBlock()			  throws IOException;
	protected abstract void sendBuffer(byte[] buffer) throws IOException;
	protected abstract void getPreferences(SharedPreferences prefs);
	
	protected void setSocketNull() {} //clear variables on disconnect errors

	public interface MavLinkConnectionListener
	{
		public void onReceiveMessage(MAVLinkMessage msg);
		public void onDisconnect();
		public void onReceiveSerialData(byte[] readData);
		public void onError(String error);
	}
	
	protected Context context;
	private MavLinkConnectionListener listner;

	protected MAVLinkPacket receivedPacket;
	protected Parser parser = new Parser();
	protected byte[] readData = new byte[4096];
	protected int bytesRead, byteIndex;
	protected boolean isConnected = true;

	private boolean logEnabled;
	private ByteBuffer logBuffer;	
	private BufferedOutputStream logWriter;  
	
	public MAVLinkConnection(Context context) {
		this.context = context;
		this.listner = (MavLinkConnectionListener) context;
		updatePreferences();
	}
	
	private void updatePreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		logEnabled = prefs.getBoolean("pref_mavlink_log_enabled", true);
		getPreferences(prefs);		
	}

	@Override
	public void run() {
		super.run();
		try {			
			updatePreferences();
			openConnection();
			
			if (logEnabled) 
			{
				logWriter = FileStream.getTLogFileStream();
				logBuffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
				logBuffer.order(ByteOrder.BIG_ENDIAN);
			}
			
			while (isConnected)
			{
				readDataBlock();
				if (LaserConstants.USB_SERIAL_CONNECTION)
					listner.onReceiveSerialData(readData);
				else
					handleData();
			}	
			closeConnection();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setSocketNull();
		listner.onDisconnect();
	}

	//Your activity will respond to this action String
	public static final String RECEIVE_NOTIFICATION = VrPadStationApp.class.getName();//"com.laser.app.VrPadStationApp";
	public void sendBroadcastIntent(String text) 
	{
		Intent intent = new Intent(RECEIVE_NOTIFICATION);
		intent.putExtra("MAVLinkConnection", text);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	
	private void handleData() throws IOException {
		if (bytesRead < 1) 
			return;
		
		for (byteIndex = 0; byteIndex < bytesRead; byteIndex++) 
		{
			receivedPacket = parser.mavlink_parse_char(readData[byteIndex] & 0x00ff);
			if (receivedPacket != null) 
			{
				saveToLog(receivedPacket);
				MAVLinkMessage msg = receivedPacket.unpack();
				listner.onReceiveMessage(msg);
			}
		}
	}
	
	private void saveToLog(MAVLinkPacket receivedPacket) throws IOException 
	{
		if (logEnabled) {
			try {
				logBuffer.clear();
				long time = System.currentTimeMillis() * 1000;
				logBuffer.putLong(time);
				logWriter.write(logBuffer.array());
				logWriter.write(receivedPacket.encodePacket());
			} catch (Exception e) {
				e.printStackTrace(); //TODO:
			}
		}
	}

	public void sendMavPacket(MAVLinkPacket packet) 
	{
		byte[] buffer = packet.encodePacket();
		try {
			if (packet.sysid == LaserConstants.PAD_SYSID) {
				saveToLog(packet);
			} else {
				sendBuffer(buffer);
				saveToLog(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSerialData(byte[] data) {
		if (LaserConstants.USB_SERIAL_CONNECTION) {
			try {
				sendBuffer(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void disconnect()
	{
		isConnected = false;
	}
	
	public void onError(String string) 
	{
		listner.onError(string);
	}




}
