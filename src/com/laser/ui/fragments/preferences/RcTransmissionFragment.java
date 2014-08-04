package com.laser.ui.fragments.preferences;

import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.activities.SettingsActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

public class RcTransmissionFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private VrPadStationApp app;
	private CheckBoxPreference mavlink;	
	private CheckBoxPreference ppmsum;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
		
		addPreferencesFromResource(R.xml.rc_transmission_prefs);
     	mavlink = (CheckBoxPreference) findPreference("mavlink");
     	ppmsum = (CheckBoxPreference) findPreference("ppmsum");
     	
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
			mavlink.setChecked(app.settings.MAVLINK);
			ppmsum.setChecked(app.settings.PPMSUM);   
			if (mavlink.isChecked())
				ppmsum.setChecked(false);
			else if (ppmsum.isChecked())
				mavlink.setChecked(false);
		}
  	}
	
	private void SetSummary()
  	{
		if (app.settings != null)
		{
			mavlink.setSummary(String.valueOf(app.settings.MAVLINK));
			ppmsum.setSummary(String.valueOf(app.settings.PPMSUM));
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
	    	    
		  		if (key.equals(mavlink.getKey()))
				{
					app.settings.MAVLINK = sharedPreferences.getBoolean(key, app.settings.MAVLINK);
					
		    	    editor.putBoolean("MAVLINK", app.settings.MAVLINK);
		    	    editor.commit();
		    	    
					if (app.settings.MAVLINK)
						ppmsum.setChecked(false);
				}
		  		else if (key.equals(ppmsum.getKey()))
		  		{
		  			app.settings.PPMSUM = sharedPreferences.getBoolean(key, app.settings.PPMSUM);
		  	    	
		    	    editor.putBoolean("PPMSUM", app.settings.PPMSUM);
		    	    editor.commit();
		    	    
		  	    	if (app.settings.PPMSUM)
		  	    		mavlink.setChecked(false);
		  		}
	  		}catch (Exception ex){}
		}
  	}
}