package com.laser.service;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.laser.connections.BTConnection;
import com.laser.connections.MAVLinkConnection;
import com.laser.connections.MAVLinkConnection.MavLinkConnectionListener;
import com.laser.connections.TcpConnection;
import com.laser.connections.UdpConnection;
import com.laser.connections.UsbConnection;

public class MAVLinkService extends Service implements MavLinkConnectionListener {
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_DATA = 3;
	public static final int MSG_SEND_SERIAL_DATA = 4;
	
	private WakeLock wakeLock;
	private MAVLinkConnection mavConnection;
	private Messenger msgCenter = null;
	private final Messenger messenger = new Messenger(new IncomingHandler());
	private boolean destroyConnection = false;

	
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				msgCenter = msg.replyTo;
				if (destroyConnection)
					selfDestroyService();
				break;
			case MSG_UNREGISTER_CLIENT:
				msgCenter = null;
				break;
			case MSG_SEND_SERIAL_DATA:
				Bundle b2 = msg.getData();
				byte[] data = (byte[]) b2.getSerializable("serial_msg");
				if (mavConnection != null) 
					mavConnection.sendSerialData(data);
				break;
			case MSG_SEND_DATA:
				Bundle b = msg.getData();
				MAVLinkPacket packet = (MAVLinkPacket) b.getSerializable("msg");
				if (mavConnection != null) 
					mavConnection.sendMavPacket(packet);
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) {
		notifyNewMessage(msg);
	}
	

	@Override
	public void onReceiveSerialData(byte[] readData) {
		notifyNewSerialData(readData);
	}
	
	private void notifyNewMessage(MAVLinkMessage m) 
	{
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null, MAVLinkClient.MSG_RECEIVED_DATA);
				Bundle data = new Bundle();
				data.putSerializable("msg", m);
				msg.setData(data);
				if (msgCenter != null)	// meglio controllare anche qui
					msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void notifyNewSerialData(byte[] readData) 
	{
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null, MAVLinkClient.MSG_RECEIVED_SERIAL_DATA);
				Bundle data = new Bundle();
				data.putSerializable("serial_msg", readData);
				msg.setData(data);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnect() {
		destroyConnection  = true;
		selfDestroyService();
	}

	private void selfDestroyService() {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null, MAVLinkClient.MSG_SELF_DESTROY_SERVICE);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		connectMAVconnection();
		showNotification();
		aquireWakelock();
		updateNotification("Connected");
	}

	@Override
	public void onDestroy() {
		disconnectMAVConnection();
		dismissNotification();
		releaseWakelock();
		super.onDestroy();
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
	private void connectMAVconnection() {
		String connectionType = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_mavlink_connection_type", "");
		if (connectionType.equals("USB")) {
			mavConnection = new UsbConnection(this);
		} else if (connectionType.equals("BT")) {
			mavConnection = new BTConnection(this);
		} else if (connectionType.equals("TCP")) {
			mavConnection = new TcpConnection(this);
		} else if (connectionType.equals("UDP")) {
			mavConnection = new UdpConnection(this);
		} else {
			mavConnection = new UsbConnection(this);
		}		
		mavConnection.start();
	}

	private void disconnectMAVConnection() {
		if (mavConnection != null) {
			mavConnection.disconnect();
			mavConnection = null;
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	static final int StatusBarNotification = 1;

	private void showNotification() {
		updateNotification("Disconnected");
	}
	
	private void updateNotification(String text) {
		mavConnection.sendBroadcastIntent(text);
	}

	private void dismissNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}

	protected void aquireWakelock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU");
			wakeLock.acquire();
		}
	}
	
	protected void releaseWakelock() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	@Override
	public void onError(final String error) {
		Handler h = new Handler(getMainLooper());
	    h.post(new Runnable() {
	        @Override
	        public void run() {
	    		Toast.makeText(MAVLinkService.this, error, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

}
