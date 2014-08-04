package com.laser.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public class MAVLinkClient
{
	public static final int MSG_RECEIVED_DATA = 0;
	public static final int MSG_SELF_DESTROY_SERVICE = 1;
	public static final int MSG_RECEIVED_SERIAL_DATA = 2;

	private Context context;
	private Messenger mService = null;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean isServiceBound;

	private OnMavlinkClientListener listener;
	public interface OnMavlinkClientListener 
	{
		public void mavlinkConnected();
		public void mavlinkDisconnected();
		public void mavlinkReceivedData(MAVLinkMessage m);
		public void serialReceivedData(byte[] readData);
	}
	public MAVLinkClient(Context context, OnMavlinkClientListener listener) {
		this.context = context;
		this.listener = listener;
	}

	public void initMavLink() {
		context.bindService(new Intent(context, MAVLinkService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		isServiceBound = true;
	}

	public void closeMavLink() 
	{
		if (isConnected()) 
		{
			if (mService != null) 
			{
				try {
					Message msg = Message.obtain(null, MAVLinkService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				// Unbind the service.
				context.unbindService(serviceConnection);
				onDisconnectedService();
			}
		}
	}


	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {		// Handler of incoming messages from service.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Received data from...chiunque
			case MSG_RECEIVED_DATA:
				Bundle b = msg.getData();
				MAVLinkMessage m = (MAVLinkMessage) b.getSerializable("msg");
				listener.mavlinkReceivedData(m);
				break;
			case MSG_SELF_DESTROY_SERVICE:
				closeMavLink();
				break;
			case MSG_RECEIVED_SERIAL_DATA:
				Bundle b2 = msg.getData();
				byte[] readData = (byte[]) b2.getSerializable("serial_msg");
				listener.serialReceivedData(readData);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, MAVLinkService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
				onConnectedService();
			} catch (RemoteException e) {}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			onDisconnectedService();
		}
	};
	
	public void sendMavPacket(MAVLinkPacket pack) 
	{
		Message msg = Message.obtain(null, MAVLinkService.MSG_SEND_DATA);
		Bundle data = new Bundle();
		data.putSerializable("msg", pack);
		msg.setData(data);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}
	
	public void sendSerialData(byte[] data) 
	{
		Message msg = Message.obtain(null, MAVLinkService.MSG_SEND_SERIAL_DATA);
		Bundle bundle = new Bundle();
		bundle.putSerializable("serial_msg", data);
		msg.setData(bundle);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	public void queryConnectionState() {
		if (isServiceBound) 
			listener.mavlinkConnected();
		else 
			listener.mavlinkDisconnected();
	}

	private void onConnectedService() {
		listener.mavlinkConnected();
	}

	private void onDisconnectedService() {
		isServiceBound = false;
		listener.mavlinkDisconnected();
	}
	
	public boolean isConnected() {
		return isServiceBound;
	}
}
