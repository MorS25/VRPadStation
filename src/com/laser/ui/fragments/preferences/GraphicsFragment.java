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

public class GraphicsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private CheckBoxPreference showFps;
	private EditTextPreference anaBaseRadius;
	private EditTextPreference anaStickRadius;
	private CheckBoxPreference lockAnaSticks;
	private CheckBoxPreference showTrims;
	private CheckBoxPreference showPots;
	private CheckBoxPreference showTimer;
	private VrPadStationApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
		
		addPreferencesFromResource(R.xml.graphics_prefs);
    	showFps = (CheckBoxPreference) findPreference("showFps");
    	anaBaseRadius = (EditTextPreference) findPreference("anaBaseRadius");
    	anaStickRadius = (EditTextPreference) findPreference("anaStickRadius");
    	lockAnaSticks = (CheckBoxPreference) findPreference("lockAnaSticks");
    	showTrims = (CheckBoxPreference) findPreference("showTrims");
    	showPots = (CheckBoxPreference) findPreference("showPots");
    	showTimer = (CheckBoxPreference) findPreference("showTimer");
     	
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
	  		showFps.setChecked(app.settings.SHOW_FPS);
	  		anaBaseRadius.setText(String.valueOf(app.settings.ANALOG_BASE_RADIUS));
	  		anaStickRadius.setText(String.valueOf(app.settings.ANALOG_STICK_RADIUS));
	  		lockAnaSticks.setChecked(app.settings.LOCK_ANALOG_STICKS);
	  		showPots.setChecked(app.settings.SHOW_POTS);
	  		showTimer.setChecked(app.settings.SHOW_TIMER);
	  		showTrims.setChecked(app.settings.SHOW_TRIMS);
		}
  	}
	
	private void SetSummary()
  	{
		if (app.settings != null)
		{
			showFps.setSummary(String.valueOf(app.settings.SHOW_FPS));
			anaBaseRadius.setSummary(String.valueOf(app.settings.ANALOG_BASE_RADIUS));
			anaStickRadius.setSummary(String.valueOf(app.settings.ANALOG_STICK_RADIUS));
			lockAnaSticks.setSummary(String.valueOf(app.settings.LOCK_ANALOG_STICKS));
			showPots.setSummary(String.valueOf(app.settings.SHOW_POTS));
			showTimer.setSummary(String.valueOf(app.settings.SHOW_TIMER));
			showTrims.setSummary(String.valueOf(app.settings.SHOW_TRIMS));
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
	    	    
				if (key.equals(showFps.getKey()))
		  		{
		  			app.settings.SHOW_FPS = sharedPreferences.getBoolean(key, app.settings.SHOW_FPS);
		  	    	
		    	    editor.putBoolean("SHOW_FPS", app.settings.SHOW_FPS);
		    	    editor.commit();
		  		}
				else if (key.equals(anaBaseRadius.getKey()))
		  		{
		  			String val = sharedPreferences.getString(key, String.valueOf(app.settings.ANALOG_BASE_RADIUS));
		  			app.settings.ANALOG_BASE_RADIUS = Integer.parseInt(val);
		  	    	
		    	    editor.putInt("ANALOG_BASE_RADIUS", app.settings.ANALOG_BASE_RADIUS);
		    	    editor.commit();
		  		}
				else if (key.equals(anaStickRadius.getKey()))
		  		{
		  			String val = sharedPreferences.getString(key, String.valueOf(app.settings.ANALOG_STICK_RADIUS));
		  			app.settings.ANALOG_STICK_RADIUS = Integer.parseInt(val);
		  	    	
		    	    editor.putInt("ANALOG_STICK_RADIUS", app.settings.ANALOG_STICK_RADIUS);
		    	    editor.commit();
		  		}
				else if (key.equals(lockAnaSticks.getKey()))
		  		{
		  			app.settings.LOCK_ANALOG_STICKS = sharedPreferences.getBoolean(key, app.settings.LOCK_ANALOG_STICKS);
		  	    	
		    	    editor.putBoolean("LOCK_ANALOG_STICKS", app.settings.LOCK_ANALOG_STICKS);
		    	    editor.commit();
		  		}
				else if (key.equals(showPots.getKey()))
		  		{
		  			app.settings.SHOW_POTS = sharedPreferences.getBoolean(key, app.settings.SHOW_POTS);
		  	    	
		    	    editor.putBoolean("SHOW_POTS", app.settings.SHOW_POTS);
		    	    editor.commit();
		  		}
				else if (key.equals(showTimer.getKey()))
		  		{
		  			app.settings.SHOW_TIMER = sharedPreferences.getBoolean(key, app.settings.SHOW_TIMER);
		  	    	
		    	    editor.putBoolean("SHOW_TIMER", app.settings.SHOW_TIMER);
		    	    editor.commit();
		  		}
				else if (key.equals(showTrims.getKey()))
		  		{
		  			app.settings.SHOW_TRIMS = sharedPreferences.getBoolean(key, app.settings.SHOW_TRIMS);
		  	    	
		    	    editor.putBoolean("SHOW_TRIMS", app.settings.SHOW_TRIMS);
		    	    editor.commit();
		  		}
	  		}catch (Exception ex){}
		}
  	}
}