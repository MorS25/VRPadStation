package com.laser.ui.fragments.preferences;

import java.util.ArrayList;
import java.util.Set;

import com.laser.VrPadStation.R;
import com.laser.connections.BTConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class MAVLinkPrefsFragment extends PreferenceFragment {

	
	private ListPreference btPairedDevice;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mavlink_prefs);
		
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		
     	btPairedDevice = (ListPreference) findPreference("pref_bluetooth_paired_device");
     	
     	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) 
		{
        	setEmptyList();
		} 
		else 
		{
			Set<BluetoothDevice> devicesPaired = bluetoothAdapter.getBondedDevices();	        
	        if (devicesPaired.size() > 0) 
	        {
	        	for (BluetoothDevice device : devicesPaired) 
	            {
	        		ParcelUuid[] pp = device.getUuids();
	                if (pp != null) 
	                {
		                for (ParcelUuid id : pp) 
		                {
		                    if (id.toString().equalsIgnoreCase(BTConnection.UUID_SPP_DEVICE)) 
		                    {
		                        devices.add(device);
		                    }
		                }
	                }
	            }
	        	if (devices.size() > 0)
	        	{
	        		String[] entriesArray = new String[devices.size()]; 
	        		String[] entryValuesArray = new String[devices.size()]; 
	        		for (int i = 0; i < devices.size(); i++) {
	        			BluetoothDevice device = devices.get(i);
	        			entriesArray[i] = device.getName();
	        			entryValuesArray[i] = device.getAddress();
	        		}
	        		btPairedDevice.setEntries(entriesArray);
	        		btPairedDevice.setEntryValues(entryValuesArray);
	        	}
	        	else
	        	{
	        		setEmptyList();
	        	}
	        } 
	        else
	        {
	        	setEmptyList();
	        }
		}
	}
	
	private void setEmptyList() {
		String[] entries = new String[1];
		String[] entryValues = new String[1];
		entries[0] = "";
		entryValues[0] = "";
		btPairedDevice.setEntries(entries);
		btPairedDevice.setEntryValues(entryValues);
	}
	
}