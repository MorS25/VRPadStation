package com.laser.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;

public class BTConnection extends MAVLinkConnection {
	
	private static final String BT = "BT";	
	public static final String UUID_SPP_DEVICE = "00001101-0000-1000-8000-00805f9b34fb";
	
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothSocket bluetoothSocket = null;
	
	private OutputStream outStream = null;
	private InputStream inStream = null;

	public BTConnection(Context context) {
		super(context);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) 
			Log.d(BT, "BluetoothAdapter NULL");
	}

    private BluetoothDevice findSerialBluetoothBoard() throws UnknownHostException {
        Set<BluetoothDevice> devicesPaired = bluetoothAdapter.getBondedDevices();
        
        if (devicesPaired.size() > 0) 					 // If there are paired devices  
        {						         
        	// controllo se ho quello di default            
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    		String defDevice = prefs.getString("pref_bluetooth_paired_device", "");
    		if (!defDevice.equals("")) 
    		{
	            for (BluetoothDevice device : devicesPaired) // Loop through paired devices
	            {  
	            	if (device.getAddress().equals(defDevice))
	            	{
                        Log.d(BT, ">> Default selected: " + device.getName());
	            		return device;
	            	}
	            }
    		}
        	
            for (BluetoothDevice device : devicesPaired) // Loop through paired devices
            {  
                // Add the name and address to an array adapter to show in a ListView
                Log.d(BT, device.getName() + " #" + device.getAddress() + "#");
                
                boolean fetch = device.fetchUuidsWithSdp();
                ParcelUuid[] pp = device.getUuids();
                if (pp != null) 
                {
	                for (ParcelUuid id : pp) 
	                {
	                    Log.d(BT, "id:" + id.toString());
	                    if (id.toString().equalsIgnoreCase(UUID_SPP_DEVICE)) 
	                    {
	                        Log.d(BT, ">> Selected: " + device.getName() + " Using: " + id.toString());
	                        return device;
	                    }
	                }
                }
            }
        }
        super.onError("No Bluetooth device found");
        throw new UnknownHostException("No Bluetooth device found");
    }
    

    @Override
    protected void openConnection() throws IOException {
        Log.d(BT, "Connect");
        resetConnection();
        
        BluetoothDevice device = findSerialBluetoothBoard();
        Log.d(BT, "Trying to connect to device with address " + device.getAddress());
		Log.d(BT, "BT Create Socket Call...");
//		bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_SPP_DEVICE));
	
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		BluetoothSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_SPP_DEVICE));
            Log.d(BT,"createRfcommSocketToServiceRecord");
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(BT,"BOOM 1 ****" + e.getLocalizedMessage()); 
        }
        bluetoothSocket = tmp;

		Log.d(BT, "BT Cancel Discovery Call...");		
		bluetoothAdapter.cancelDiscovery();		
        
		Log.d(BT, "BT Connect Call...");	
		bluetoothSocket.connect(); //Here the IOException will rise on BT protocol/handshake error.

		Log.d(BT, "## BT Connected ##");			
		outStream = bluetoothSocket.getOutputStream();
		inStream = bluetoothSocket.getInputStream();
	}

	@Override
	protected void readDataBlock() throws IOException 
	{
		bytesRead = inStream.read(readData);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException 
	{
		if (outStream != null)
		{
			outStream.write(buffer);
		}
	}

	@Override
	protected void setSocketNull()
	{
		bluetoothSocket = null;
		inStream = null;
		outStream = null;
	}

	private void resetConnection() throws IOException 
	{
        if (inStream != null) 
        {
            inStream.close(); 
            inStream = null;
        }

        if (outStream != null) 
        {
            outStream.close();
            outStream = null;
        }

        if (bluetoothSocket != null) 
        {
            bluetoothSocket.close();
            bluetoothSocket = null;
        }
	}
	

	@Override
	protected void closeConnection() throws IOException {
		resetConnection();
		Log.d(BT, "## BT Connection Closed ##");
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {}
	
}
