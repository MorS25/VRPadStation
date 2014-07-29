package com.laser.ui.fragments.preferences;


import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.activities.SettingsActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RcModesFragment extends Fragment implements OnClickListener {


	private TextView txtMode1;
	private TextView txtMode2;
	private TextView txtMode3;
	private TextView txtMode4;
	
	private ImageView imgMode1;
	private ImageView imgMode2;
	private ImageView imgMode3;
	private ImageView imgMode4;
		
	private int prev_RC_MODE;
	private VrPadStationApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.app = (VrPadStationApp)getActivity().getApplication();
		prev_RC_MODE = app.settings.RC_MODE;
	}
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.rc_modes_layout, container, false);
		
		if (app.settings != null)
		{
			txtMode1 = (TextView)v.findViewById(R.id.txtMode1);
			txtMode2 = (TextView)v.findViewById(R.id.txtMode2);
			txtMode3 = (TextView)v.findViewById(R.id.txtMode3);
			txtMode4 = (TextView)v.findViewById(R.id.txtMode4);
			
			imgMode1 = (ImageView)v.findViewById(R.id.imgMode1);
			imgMode2 = (ImageView)v.findViewById(R.id.imgMode2);
			imgMode3 = (ImageView)v.findViewById(R.id.imgMode3);
			imgMode4 = (ImageView)v.findViewById(R.id.imgMode4);
			imgMode1.setOnClickListener(this);
			imgMode2.setOnClickListener(this);
			imgMode3.setOnClickListener(this);
			imgMode4.setOnClickListener(this);
			
			setActiveMode();
			
		}
		
		return v;
	}
	
	private void setActiveMode()
	{
		switch (app.settings.RC_MODE)
		{
		case 1:
			txtMode1.setTextColor(Color.GREEN);
			txtMode2.setTextColor(Color.WHITE);
			txtMode3.setTextColor(Color.WHITE);
			txtMode4.setTextColor(Color.WHITE);
			imgMode1.setClickable(false);
			imgMode2.setClickable(true);
			imgMode3.setClickable(true);
			imgMode4.setClickable(true);
			break;
		case 2:
			txtMode1.setTextColor(Color.WHITE);
			txtMode2.setTextColor(Color.GREEN);
			txtMode3.setTextColor(Color.WHITE);
			txtMode4.setTextColor(Color.WHITE);
			imgMode1.setClickable(true);
			imgMode2.setClickable(false);
			imgMode3.setClickable(true);
			imgMode4.setClickable(true);
			break;
		case 3:
			txtMode1.setTextColor(Color.WHITE);
			txtMode2.setTextColor(Color.WHITE);
			txtMode3.setTextColor(Color.GREEN);
			txtMode4.setTextColor(Color.WHITE);
			imgMode1.setClickable(true);
			imgMode2.setClickable(true);
			imgMode3.setClickable(false);
			imgMode4.setClickable(true);
			break;
		case 4:
			txtMode1.setTextColor(Color.WHITE);
			txtMode2.setTextColor(Color.WHITE);
			txtMode3.setTextColor(Color.WHITE);
			txtMode4.setTextColor(Color.GREEN);
			imgMode1.setClickable(true);
			imgMode2.setClickable(true);
			imgMode3.setClickable(true);
			imgMode4.setClickable(false);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (app.settings != null)
		{
			SettingsActivity.bEdited = true;
			
			switch (v.getId())
			{
			case R.id.imgMode1:
				app.settings.RC_MODE = 1;
				break;
			case R.id.imgMode2:
				app.settings.RC_MODE = 2;
				break;
			case R.id.imgMode3:
				app.settings.RC_MODE = 3;
				break;
			case R.id.imgMode4:
				app.settings.RC_MODE = 4;
				break;
			}

			SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
    	    SharedPreferences.Editor editor = prefs.edit();
    	    editor.putInt("RC_MODE", app.settings.RC_MODE);
    	    editor.commit();
    	    
			setActiveMode();
			
			if (prev_RC_MODE != app.settings.RC_MODE)
				SettingsActivity.bForceRestart = true;
			else
				SettingsActivity.bForceRestart = false;	
		}
	}
}