package com.laser.ui.fragments.preferences;

import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.activities.SettingsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class CameraSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private CheckBoxPreference gopro_enabled;
	private CheckBoxPreference gopro_startup;
	private EditTextPreference gopro_ssid;
	private EditTextPreference gopro_password;
	private EditTextPreference cameraAddress;
	private VrPadStationApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
		
		addPreferencesFromResource(R.xml.camera_prefs);

     	gopro_enabled = (CheckBoxPreference) findPreference("gopro_enabled");
     	gopro_startup = (CheckBoxPreference) findPreference("gopro_startup");
		gopro_ssid = (EditTextPreference) findPreference("gopro_ssid");
		gopro_password = (EditTextPreference) findPreference("gopro_password");
		cameraAddress = (EditTextPreference) findPreference("cameraAddress");
     	
     	MostraDati();
     	SetSummary();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
			.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void MostraDati()
  	{
		if (app.settings != null)
		{
			gopro_enabled.setChecked(app.settings.GOPRO_ENABLED);
			if (gopro_enabled.isChecked())
			{
				cameraAddress.setEnabled(false);
				gopro_ssid.setEnabled(true);
				gopro_password.setEnabled(true);
				gopro_startup.setEnabled(true);
			}
			else
			{
				cameraAddress.setEnabled(true);
				gopro_ssid.setEnabled(false);
				gopro_password.setEnabled(false);
				gopro_startup.setEnabled(false);
				app.settings.GOPRO_STARTUP = false;
			}
			gopro_startup.setChecked(app.settings.GOPRO_STARTUP);
			gopro_ssid.setText(String.valueOf(app.settings.GOPRO_SSID));
			gopro_password.setText(String.valueOf(app.settings.GOPRO_PASSWORD));
			
			cameraAddress.setText(String.valueOf(app.settings.CAMERA_ADDRESS));
		}
  	}
	
	private void SetSummary()
  	{
		if (app.settings != null)
		{
			gopro_enabled.setSummary(String.valueOf(app.settings.GOPRO_ENABLED));
			gopro_startup.setSummary(String.valueOf(app.settings.GOPRO_STARTUP));
			gopro_ssid.setSummary(String.valueOf(app.settings.GOPRO_SSID));
			//gopro_password.setSummary(String.valueOf(app.settings.GOPRO_PASSWORD));
			cameraAddress.setSummary(String.valueOf(app.settings.CAMERA_ADDRESS));
		}
  	}
		
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SettingsActivity.bEdited = true;
	    AggiornaInterfaccia(sharedPreferences, key);
	    SetSummary();
	}

	public void AggiornaInterfaccia(SharedPreferences sharedPreferences, String key)
  	{
		if (app.settings != null)
		{
			try
			{
				SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
	    	    SharedPreferences.Editor editor = prefs.edit();
	    	    
				if (key.equals(gopro_enabled.getKey()))
		  		{
		  			app.settings.GOPRO_ENABLED = sharedPreferences.getBoolean(key, app.settings.GOPRO_ENABLED);
		  	    	
		    	    editor.putBoolean("GOPRO_ENABLED", app.settings.GOPRO_ENABLED);
		    	    editor.commit();
		    	    
		  	    	if (app.settings.GOPRO_ENABLED)
		  	    	{
						cameraAddress.setEnabled(false);
						gopro_ssid.setEnabled(true);
						gopro_password.setEnabled(true);
						gopro_startup.setEnabled(true);
		  	    	}
					else
					{	
						cameraAddress.setEnabled(true);
						gopro_ssid.setEnabled(false);
						gopro_password.setEnabled(false);
						app.settings.GOPRO_STARTUP = false;
						gopro_startup.setEnabled(false);
						gopro_startup.setChecked(false);
					}
		  		}
		  		else if (key.equals(gopro_startup.getKey()))
		  		{
		  			app.settings.GOPRO_STARTUP = sharedPreferences.getBoolean(key, app.settings.GOPRO_STARTUP);
		  	    	
		    	    editor.putBoolean("GOPRO_STARTUP", app.settings.GOPRO_STARTUP);
		    	    editor.commit();
		  		}
		  		else if (key.equals(gopro_ssid.getKey()))
		  		{
		  			app.settings.GOPRO_SSID = sharedPreferences.getString(key, app.settings.GOPRO_SSID);
		  	    	
		    	    editor.putString("GOPRO_SSID", app.settings.GOPRO_SSID);
		    	    editor.commit();
		  		}
		  		else if (key.equals(gopro_password.getKey()))
		  		{
		  			app.settings.GOPRO_PASSWORD = sharedPreferences.getString(key, app.settings.GOPRO_PASSWORD);

		    	    editor.putString("GOPRO_PASSWORD", app.settings.GOPRO_PASSWORD);
		    	    editor.commit();
		  		}
		  		else if (key.equals(cameraAddress.getKey()))
		  		{
		  			app.settings.CAMERA_ADDRESS = sharedPreferences.getString(key, app.settings.CAMERA_ADDRESS);
		  	    	
		    	    editor.putString("CAMERA_ADDRESS", app.settings.CAMERA_ADDRESS);
		    	    editor.commit();
		  		}
	  		}catch (Exception ex){}
		}
  	}
}