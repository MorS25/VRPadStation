package com.laser.ui.activities;


import com.laser.VrPadStation.R;
import com.laser.ui.fragments.preferences.CameraSettingsFragment;
import com.laser.ui.fragments.preferences.FeedbackFragment;
import com.laser.ui.fragments.preferences.GraphicsFragment;
import com.laser.ui.fragments.preferences.MAVLinkPrefsFragment;
import com.laser.ui.fragments.preferences.MapPrefsFragment;
import com.laser.ui.fragments.preferences.PPMSUMFragment;
import com.laser.ui.fragments.preferences.PitchSettingsFragment;
import com.laser.ui.fragments.preferences.QuickModesSettingsFragment;
import com.laser.ui.fragments.preferences.RcModesFragment;
import com.laser.ui.fragments.preferences.RcTransmissionFragment;
import com.laser.ui.fragments.preferences.RollSettingsFragment;
import com.laser.ui.fragments.preferences.ThrottleSettingsFragment;
import com.laser.ui.fragments.preferences.YawSettingsFragment;
import com.laser.ui.fragments.SettingsListFragmentRadio;

import android.app.Activity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;

public class SettingsActivityRadio extends ParentActivity implements SettingsListFragmentRadio.OnFlyListFragmentItemClick {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getActionBar().hide();
		
//		app = (VrPadStationApp) getApplication();
        
        // Manca il layout portrait. Lo lascio bloccato in landscape visto che l'applicazione si usa così.
        setContentView(R.layout.activity_settings_radio);
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
            case 0:	// Map
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	MapPrefsFragment mapPrefsFragment = new MapPrefsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, mapPrefsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 1:		// MAVLink
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
            case 2:		// PPMSUM
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	PPMSUMFragment ppmSumFragment = new PPMSUMFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, ppmSumFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
            break;
            case 3:		// RC Transmission
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	RcTransmissionFragment outputSignalFragment = new RcTransmissionFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, outputSignalFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
    		break;
            case 4:		// RC Mode
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	RcModesFragment radioControlModesFragment = new RcModesFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, radioControlModesFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
    		break;
            case 5:		// Flight Modes
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	QuickModesSettingsFragment modesFragment = new QuickModesSettingsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, modesFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 6:	// Graphics
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	GraphicsFragment graphicsFragment = new GraphicsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, graphicsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 7:	// Feedback
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	FeedbackFragment feedbackFragment = new FeedbackFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, feedbackFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 8:	// Camera
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	CameraSettingsFragment cameraSettingsFragment = new CameraSettingsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, cameraSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
            break;
            case 9:		// Drone Roll
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	RollSettingsFragment channelsSettingsFragment = new RollSettingsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 10:		// Drone Pitch
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	PitchSettingsFragment channelsSettingsFragment = new PitchSettingsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 11:		// Drone Throttle
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	ThrottleSettingsFragment channelsSettingsFragment = new ThrottleSettingsFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, channelsSettingsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
        	break;
            case 12:		// Drone Yaw
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	YawSettingsFragment channelsSettingsFragment = new YawSettingsFragment();
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
