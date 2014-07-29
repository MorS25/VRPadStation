package com.laser.ui.activities;


import com.laser.VrPadStation.R;
import com.laser.ui.fragments.preferences.CameraAnglesFragment;
import com.laser.ui.fragments.preferences.CameraPanFragment;
import com.laser.ui.fragments.preferences.CameraRollFragment;
import com.laser.ui.fragments.preferences.CameraShutterFragment;
import com.laser.ui.fragments.preferences.CameraTiltFragment;
import com.laser.ui.fragments.preferences.MAVLinkPrefsFragment;
import com.laser.ui.fragments.SettingsListFragmentGimbal;

import android.app.Activity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

public class SettingsActivityGimbal extends ParentActivity implements SettingsListFragmentGimbal.OnFlyListFragmentItemClick {

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getActionBar().hide();
		
//		app = (VrPadStationApp) getApplication();
        
        // Manca il layout portrait. Lo lascio bloccato in landscape visto che l'applicazione si usa così.
        setContentView(R.layout.activity_settings_gimbal);
        setResult(Activity.RESULT_CANCELED);
    }
        
	@Override
	public void onBackPressed() {
		if (SettingsActivity.bEdited)
		{
			Intent intent = new Intent();
			intent.putExtra("bForceRestart", SettingsActivity.bForceRestart);
			setResult(Activity.RESULT_OK, intent);
		}
		else
			setResult(Activity.RESULT_CANCELED);			
		finish();
	}

    @Override
    public void onClick(int item) {
    	
            switch (item)
            {
            case 0:		// MAVLink
	            {
	            	View detailView = findViewById(R.id.detailContainer);
	                if(detailView==null) {
	                	// Activity per portrait
	                }
	                else {
	                	MAVLinkPrefsFragment mavlinkPrefsFragment = new MAVLinkPrefsFragment();
	                	FragmentManager fragmentManager = this.getFragmentManager();
	                    fragmentManager.beginTransaction()
	                    .replace(R.id.detailContainer, mavlinkPrefsFragment)
	                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
	                    .commit();
	                }
	            }
            break;
            case 1:		// Gimbal Roll
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraRollFragment channelsSettingsFragment = new CameraRollFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 2:		// Gimbal Tilt
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraTiltFragment channelsSettingsFragment = new CameraTiltFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 3:		// Gimbal Pan
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraPanFragment channelsSettingsFragment = new CameraPanFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 4:		// Gimbal Shutter
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraShutterFragment channelsSettingsFragment = new CameraShutterFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 5:		// Gimbal Angles
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraAnglesFragment channelsSettingsFragment = new CameraAnglesFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
        }
        /*
        // Recupero la vista detailContainer
        View detailView = findViewById(R.id.detailContainer);
        if(detailView==null) {
                // Non esiste spazio per la visualizzazione del dattagli, quindi ho necessità di lanciare una nuova activity.
                // Carico gli arguments nell'intent di chiamata.
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtras(arguments);
                startActivity(intent);
        		finish();
        }
        else {
                // Esiste lo spazio, procedo con la creazione del Fragment!
                MyDetailFragment myDetailFragment = new MyDetailFragment();
                // Imposto gli argument del fragment.
                myDetailFragment.setArguments(arguments);
               
                // Procedo con la sostituzione del fragment visualizzato.
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                .replace(R.id.detailContainer, myDetailFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        }*/
    }
}
