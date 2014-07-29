package com.laser.ui.fragments.preferences;

import com.laser.VrPadStation.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MapPrefsFragment extends PreferenceFragment {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.map_prefs);
	}
	
}