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

public class PPMSUMFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private EditTextPreference transmissionRate;
	private EditTextPreference minPulseWidth;
	private EditTextPreference maxPulseWidth;	
	private CheckBoxPreference checkAudioSignal;	
	private CheckBoxPreference checkSignalReverse;	
	private CheckBoxPreference switchChannel;
	private VrPadStationApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
		
		addPreferencesFromResource(R.xml.ppmsum_prefs);
     	minPulseWidth = (EditTextPreference) findPreference("minPulseWidth");
     	maxPulseWidth = (EditTextPreference) findPreference("maxPulseWidth");
     	transmissionRate = (EditTextPreference) findPreference("transmissionRate");
     	checkAudioSignal = (CheckBoxPreference) findPreference("checkAudioSignal");
     	checkSignalReverse = (CheckBoxPreference) findPreference("checkSignalReverse");
     	switchChannel = (CheckBoxPreference) findPreference("switchChannel");
     	
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
			transmissionRate.setText(String.valueOf(app.settings.TRANSMISSION_RATE));
			minPulseWidth.setText(String.valueOf(app.settings.MIN_PULSE_WIDTH));
			maxPulseWidth.setText(String.valueOf(app.settings.MAX_PULSE_WIDTH));
			checkAudioSignal.setChecked(app.settings.AUDIO_SIGNAL);
			checkSignalReverse.setChecked(app.settings.ReverseSignal);
			switchChannel.setChecked(app.settings.SWITCH_PPMSUM_CHANNEL);
		}
  	}
	
	private void SetSummary()
  	{
		if (app.settings != null)
		{
			transmissionRate.setSummary(String.valueOf(app.settings.TRANSMISSION_RATE));
			minPulseWidth.setSummary(String.valueOf(app.settings.MIN_PULSE_WIDTH));
			maxPulseWidth.setSummary(String.valueOf(app.settings.MAX_PULSE_WIDTH));
			checkAudioSignal.setSummary(String.valueOf(app.settings.AUDIO_SIGNAL));
			checkSignalReverse.setSummary(String.valueOf(app.settings.ReverseSignal));
			switchChannel.setSummary(String.valueOf(app.settings.SWITCH_PPMSUM_CHANNEL));
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
	    	    
				if (key.equals(transmissionRate.getKey()))
				{
					String val = sharedPreferences.getString(key, String.valueOf(app.settings.TRANSMISSION_RATE));
					app.settings.TRANSMISSION_RATE = Integer.parseInt(val);
					
		    	    editor.putInt("TRANSMISSION_RATE", app.settings.TRANSMISSION_RATE);
		    	    editor.commit();
				}
				else if (key.equals(minPulseWidth.getKey()))
		  		{
		  			String val = sharedPreferences.getString(key, String.valueOf(app.settings.MIN_PULSE_WIDTH));
		  			app.settings.MIN_PULSE_WIDTH = Integer.parseInt(val);
		  	    	
		    	    editor.putInt("MIN_PULSE_WIDTH", app.settings.MIN_PULSE_WIDTH);
		    	    editor.commit();
		  		}
		  		else if (key.equals(maxPulseWidth.getKey()))
		  		{
		  			String val = sharedPreferences.getString(key, String.valueOf(app.settings.MAX_PULSE_WIDTH));
		  			app.settings.MAX_PULSE_WIDTH = Integer.parseInt(val);
		  	    	
		    	    editor.putInt("MAX_PULSE_WIDTH", app.settings.MAX_PULSE_WIDTH);
		    	    editor.commit();
		  		}
		  		else if (key.equals(checkAudioSignal.getKey()))
		  		{
		  			app.settings.AUDIO_SIGNAL = sharedPreferences.getBoolean(key, app.settings.AUDIO_SIGNAL);
		  	    	
		    	    editor.putBoolean("AUDIO_SIGNAL", app.settings.AUDIO_SIGNAL);
		    	    editor.commit();
	    	    }
		  		else if (key.equals(checkSignalReverse.getKey()))
		  		{
		  			app.settings.ReverseSignal = sharedPreferences.getBoolean(key, app.settings.ReverseSignal);
		  	    	
		    	    editor.putBoolean("ReverseSignal", app.settings.ReverseSignal);
		    	    editor.commit();
		  		}
		  		else if (key.equals(switchChannel.getKey()))
		  		{
		  			app.settings.SWITCH_PPMSUM_CHANNEL = sharedPreferences.getBoolean(key, app.settings.SWITCH_PPMSUM_CHANNEL);
		  	    	
		    	    editor.putBoolean("SWITCH_PPMSUM_CHANNEL", app.settings.SWITCH_PPMSUM_CHANNEL);
		    	    editor.commit();
		  		}
	  		}catch (Exception ex){}
		}
  	}
}