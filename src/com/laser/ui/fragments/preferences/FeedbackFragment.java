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

public class FeedbackFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

//	private CheckBoxPreference vibration;	
//	private CheckBoxPreference yawVibro;	
//	private CheckBoxPreference rollVibro;	
	private CheckBoxPreference throttleAudio;
	
	private EditTextPreference timer;
	private CheckBoxPreference setTimer;

	private VrPadStationApp app;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
		
		addPreferencesFromResource(R.xml.feedback_prefs);
		
//     	vibration = (CheckBoxPreference) findPreference("vibration");
//     	yawVibro = (CheckBoxPreference) findPreference("yawVibro");
//     	rollVibro = (CheckBoxPreference) findPreference("rollVibro");
     	throttleAudio = (CheckBoxPreference) findPreference("throttleAudio");
     	
     	timer = (EditTextPreference) findPreference("timer");
     	setTimer = (CheckBoxPreference) findPreference("setTimer");
     	
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
//			vibration.setChecked(app.settings.VIBRATION);
//			yawVibro.setChecked(app.settings.YAW_VIBRO);
//			rollVibro.setChecked(app.settings.ROLL_VIBRO); 
//			if (app.settings.VIBRATION)
//			{
//				rollVibro.setEnabled(true);
//				yawVibro.setEnabled(true);
//			}
//			else
//			{
//				rollVibro.setEnabled(false);
//				yawVibro.setEnabled(false);
//			}
			throttleAudio.setChecked(app.settings.AUDIO_FEEDBACK);  
//			if (yawVibro.isChecked())
//				rollVibro.setChecked(false);
//			else if (rollVibro.isChecked())
//				yawVibro.setChecked(false);			

			timer.setText(String.valueOf(app.settings.TIMER));
			setTimer.setChecked(app.settings.SET_TIMER);
		}
  	}
	
	private void SetSummary()
  	{
		if (app.settings != null)
		{
//			vibration.setSummary(String.valueOf(app.settings.VIBRATION));
//			yawVibro.setSummary(String.valueOf(app.settings.YAW_VIBRO));
//			rollVibro.setSummary(String.valueOf(app.settings.ROLL_VIBRO));
			throttleAudio.setSummary(String.valueOf(app.settings.AUDIO_FEEDBACK));

			timer.setSummary(String.valueOf(app.settings.TIMER));
			setTimer.setSummary(String.valueOf(app.settings.SET_TIMER));
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
	    	    
//				if (key.equals(vibration.getKey()))
//				{
//					app.settings.VIBRATION = sharedPreferences.getBoolean(key, app.settings.VIBRATION);
//
//		    	    editor.putBoolean("VIBRATION", app.settings.VIBRATION);
//		    	    editor.commit();
//		    	    
//					if (app.settings.VIBRATION)
//					{
//						rollVibro.setEnabled(true);
//						yawVibro.setEnabled(true);
//					}
//					else
//					{
//						rollVibro.setEnabled(false);
//						yawVibro.setEnabled(false);
//					}
//				}
//				else if (key.equals(yawVibro.getKey()))
//				{
//					app.settings.YAW_VIBRO = sharedPreferences.getBoolean(key, app.settings.YAW_VIBRO);
//					
//		    	    editor.putBoolean("YAW_VIBRO", app.settings.YAW_VIBRO);
//		    	    editor.commit();
//		    	    
//					if (app.settings.YAW_VIBRO)
//						rollVibro.setChecked(false);
//				}
//		  		else if (key.equals(rollVibro.getKey()))
//		  		{
//		  			app.settings.ROLL_VIBRO = sharedPreferences.getBoolean(key, app.settings.ROLL_VIBRO);
//		  	    	
//		    	    editor.putBoolean("ROLL_VIBRO", app.settings.ROLL_VIBRO);
//		    	    editor.commit();
//		    	    
//		  	    	if (app.settings.ROLL_VIBRO)
//		  	    		yawVibro.setChecked(false);
//		  		}else
		  		if (key.equals(throttleAudio.getKey()))
		  		{
		  			app.settings.AUDIO_FEEDBACK = sharedPreferences.getBoolean(key, app.settings.AUDIO_FEEDBACK);
		  	    	
		    	    editor.putBoolean("AUDIO_FEEDBACK", app.settings.AUDIO_FEEDBACK);
		    	    editor.commit();
		  		}
		  		else if (key.equals(timer.getKey()))
		  		{
		  			String val = sharedPreferences.getString(key, String.valueOf(app.settings.TIMER));
		  			app.settings.TIMER = Integer.parseInt(val);
		  	    	
		    	    editor.putInt("TIMER", app.settings.TIMER);
		    	    editor.commit();
		  		}
		  		else if (key.equals(setTimer.getKey()))
		  		{
		  			app.settings.SET_TIMER = sharedPreferences.getBoolean(key, app.settings.SET_TIMER);
		  	    	
		    	    editor.putBoolean("SET_TIMER", app.settings.SET_TIMER);
		    	    editor.commit();
		  		}
	  		}catch (Exception ex){}
		}
  	}
}