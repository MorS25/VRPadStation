package com.laser.ui.activities;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.cellbots.PulseGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.laser.VrPadStation.R;
import com.laser.airports.Airport;
import com.laser.app.VrPadStationApp;
import com.laser.app.VrPadStationApp.OnAirportsLoadedListener;
import com.laser.app.VrPadStationApp.OnWaypointReceivedListener;
import com.laser.helpers.CustomLocationManager;
import com.laser.helpers.CustomLocationManager.CustomLocationManagerListener;
import com.laser.ui.fragments.CustomMapFragment;
import com.laser.ui.layers.FlyInterfaceLayer;
import com.laser.ui.layers.FlyInterfaceLayer.FlyInterfaceListener;
import com.laser.ui.layers.JoystickLayer;
import com.laser.ui.layers.JoystickLayer.JoystickListener;
import com.laser.ui.layers.PlayerLayer;
import com.laser.ui.layers.PlayerLayer.PlayerLayerListener;
import com.laser.utils.LaserConstants;

public class RadioActivity extends ParentActivity implements PlayerLayerListener,
													   		 JoystickListener,
													   		 FlyInterfaceListener,
													   		 OnWaypointReceivedListener,
													   		 OnAirportsLoadedListener,
													   		 CustomLocationManagerListener {

    private final String TAG = RadioActivity.class.getSimpleName();    
	public VrPadStationApp app;

	public static boolean KEEP_RUNNING = false;

	private FrameLayout baseLayout;	
	private CustomMapFragment mapFragment;
	private PlayerLayer playerLayer;								
	private JoystickLayer joystickLayer;						
	private FlyInterfaceLayer flyInterfaceLayer;		
	
	// PulseGenerator
    private PulseGenerator noise;
    private Thread noiseThread;
	private boolean bInitPulseGenerator = false;
	
	private boolean paused = false;
        
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
				
		app = (VrPadStationApp) getApplication();
		app.setWaypointReceivedListener(this);
		app.setAirportsLoadedListener(this);
		app.locationManager.setCustomLocationManagerListener(this);
		
		setContentView(R.layout.activity_radio);
		baseLayout = (FrameLayout)findViewById(R.id.baseLayout);
		mapFragment = ((CustomMapFragment)getFragmentManager().findFragmentById(R.id.flightMapFragment));
		if (mapFragment.getMap() == null)
			mapFragment = null;
		
//		if (mapFragment != null)
//			mapFragment.forcePan(true);
		
		playerLayer = new PlayerLayer(this, app.settings);
		joystickLayer = new JoystickLayer(app);	
		flyInterfaceLayer = new FlyInterfaceLayer(this, app);	
		
		baseLayout.addView(playerLayer.getPlayerLayer());	
		baseLayout.addView(joystickLayer);
		baseLayout.addView(flyInterfaceLayer.getFlyUserInterface());
						
		// set listeners
		playerLayer.setListener(this);
		joystickLayer.setListener(this);
		flyInterfaceLayer.setListener(this);

		// create PulseGenerator
	    noise = new PulseGenerator(app);
	    noiseThread = new Thread(noise);	    

	    // create transmission thread
	    mHandlerTransmission = new Handler();
	    
		// battery receiver
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	
//		if (mapFragment != null)
//		{			
//			mapFragment.updateMapPreferences();
//			mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
//			mapFragment.updateHomeToMap(app.drone);	
//			mapFragment.update(app.drone, null, app.getAirportsList());
//			mapFragment.getMap().setBuildingsEnabled(true);
//			mapFragment.getMap().setIndoorEnabled(true);
//			onZoom();
//		}

		app.channelManager.updateChannels(app.settings, -1);
		if (flyInterfaceLayer != null)
			flyInterfaceLayer.updateFlyInterface();
		
		lockMap(app.isRcOverrided());
    }	
	
	private void lockMap(boolean lock) {	
//		lock = true;
		if (mapFragment != null) {
			if (lock) {
				joystickLayer.setVisibility(View.VISIBLE);
				mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
				mapFragment.getMap().getUiSettings().setTiltGesturesEnabled(false);
			} else {
				joystickLayer.setVisibility(View.GONE);
				mapFragment.getMap().getUiSettings().setZoomControlsEnabled(true);
				mapFragment.getMap().getUiSettings().setTiltGesturesEnabled(true);
			}
		}
	}

	public void onZoom() {
		if (mapFragment != null)
			mapFragment.zoomToLastKnowPosition();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if ((requestCode == LaserConstants.SETTINGS_CODE) && (resultCode == Activity.RESULT_OK))
		{
			boolean bForceRestart = data.getBooleanExtra("bForceRestart", false);
			String prevCameraAddress = app.settings.CAMERA_ADDRESS;
			
			app.channelManager.updateChannels(app.settings, -1);			
//			noise.updateSettings(app.settings);
			
			if (bForceRestart)
			{
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						try {
							RadioActivity.this.finalize();
						} catch (Throwable e) {
							e.printStackTrace();
						}
						RadioActivity.this.finish();
						System.exit(0);					
						}
				});
			}

			flyInterfaceLayer.updateFlyInterface();				
			playerLayer.onActivityResult(prevCameraAddress, app.settings);

			joystickLayer.updateJoystickLayer();
			joystickLayer.invalidate();
		}
	}
	    
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context arg0, Intent intent) {
	    	int level = intent.getIntExtra("level", 0);
	    	int plugged = intent.getIntExtra("plugged", 0);	
	    	
	    	if (plugged == BatteryManager.BATTERY_PLUGGED_AC ||
	    		plugged ==  BatteryManager.BATTERY_PLUGGED_USB)
	    	{
	    		flyInterfaceLayer.updateBattery(level, true);
	    	} 
	    	else
	    	{
	    		flyInterfaceLayer.updateBattery(level, false);	    		
	    	}
    	}
	};
    
	@Override
	public boolean onSearchRequested() {
		return false;
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
		{
			return true;
		}
		else
			return super.onKeyLongPress(keyCode, event);
	}	
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		app.removeWaypointReceivedListener(this);
		app.removeAirportsLoadedListener(this);
		app.locationManager.removeCustomLocationManagerListener(this);
		
		if (noise != null)
			noise.stop();

		KEEP_RUNNING = false;
		
		try{
			unregisterReceiver(mBatInfoReceiver);
		}catch (IllegalArgumentException ex){}
		
		joystickLayer.destroyRes();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		paused = false;
		
//		mapFragment.updateMapPreferences();
		if (mapFragment != null)
		{			
			mapFragment.updateMapPreferences();
			mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
			mapFragment.updateHomeToMap(app.drone);	
			mapFragment.update(app.drone, null, app.getAirportsList());
			mapFragment.getMap().setBuildingsEnabled(true);
			mapFragment.getMap().setIndoorEnabled(true);
			onZoom();
		}
		
		unregisterReceiver(mBatInfoReceiver);
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if (!app.MAVClient.isConnected() && app.isMavlinkConnected())
			app.MAVClient.initMavLink();

		joystickLayer.updateChannelsAndTrims();
		
		flyInterfaceLayer.hideTrims();
		flyInterfaceLayer.hidePotentiometers();
		flyInterfaceLayer.hideTimer();

		KEEP_RUNNING = true;
        mHandlerTransmission.postDelayed(transmit, 1000/app.settings.TRANSMISSION_RATE);

        playerLayer.onResume();

		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		paused = true;
		playerLayer.onPause();
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		if (!noiseThread.isAlive())
			noiseThread.start();
	    super.onStart();
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		playerLayer.onConfigurationChanged();
        super.onConfigurationChanged(newConfig);
    }
	
	public void switchToCam()
	{
		if (mapFragment != null)
			mapFragment.getView().setVisibility(View.GONE);
		playerLayer.setVisibility(View.VISIBLE);
		flyInterfaceLayer.changeToCam();
	}
	public void switchToMap()
	{
		if (mapFragment != null)
			mapFragment.getView().setVisibility(View.VISIBLE);
		playerLayer.setVisibility(View.GONE);
		flyInterfaceLayer.changeToMap();
	}	

	private int backPressCounter = 0;
	public void onBackPressed() 
	{
		if (app.isRcOverrided())
		{
			Toast.makeText(this, "Disable RC override!", Toast.LENGTH_SHORT).show();
		}
		else
		{		
			backPressCounter++;
			if (backPressCounter == 1)
			{
				Toast.makeText(this, "Press again to exit.", Toast.LENGTH_LONG).show();
				Handler exitHandler = new Handler();
				exitHandler.postDelayed(new Runnable() {				
					@Override
					public void run() {
						backPressCounter = 0;
					}
				}, 3500);
			}
			else if (backPressCounter == 2)
				finish();
		}
	};
	
	@Override
	public void mavlinkConnected() 
	{
		super.mavlinkConnected();
		app.waypointMananger.getWaypoints();
	}
	
	@Override
	public void mavlinkDisconnected() 
	{
		super.mavlinkDisconnected();		
	}
	
	@Override
	public void mavlinkReceivedData(MAVLinkMessage m) {
		super.mavlinkReceivedData(m);
		
		if (mapFragment != null)
			mapFragment.receiveData(m, app.drone);		
		
		if (flyInterfaceLayer != null && !app.areBaseParamsReceived())
			flyInterfaceLayer.updateFlyInterface();
				
		switch (m.msgid) 
		{
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			msg_rc_channels_raw m_rc = (msg_rc_channels_raw) m;
			final String rc = "ch1: " + m_rc.chan1_raw 
							+ "\nch2: " + m_rc.chan2_raw
							+ "\nch3: " + m_rc.chan3_raw 
							+ "\nch4: " + m_rc.chan4_raw 
							+ "\nch5: " + m_rc.chan5_raw 
							+ "\nch6: " + m_rc.chan6_raw 
							+ "\nch7: " + m_rc.chan7_raw 
							+ "\nch8: " + m_rc.chan8_raw;
			runOnUiThread(new Runnable() {					
				@Override
				public void run() {
					flyInterfaceLayer.setRcText(rc);
				}
			});
			if (LaserConstants.DEBUG)
				Log.d("MAVLINK_RC", m_rc.toString());
			break;
		}
	}
	
	@Override
	public void OnSetVolumeControlStream(int stream) {
        setVolumeControlStream(stream);
	}

	@Override
	public Window GetWindow() {
		return getWindow();
	}
	
	// Thread di trasmissione
	private Handler mHandlerTransmission;
	private long lastToastShowed = 0;
	private Runnable transmit = new Runnable() {		
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();			
			if (flyInterfaceLayer.getTimer().isTimerStarted())
			{
				if (!flyInterfaceLayer.getTimer().isSetTimerOn())
				{
					long time = (app.settings.TIMER) - ((System.currentTimeMillis() - flyInterfaceLayer.getTimer().getStartTime()) / 1000);
					if (time >= 0)
						flyInterfaceLayer.getTimer().setVal((int)time);
					else
						joystickLayer.setTimerExpired(true);
				}
				else
				{
					long time = ((System.currentTimeMillis() - flyInterfaceLayer.getTimer().getStartTime()) / 1000);
					flyInterfaceLayer.getTimer().setVal((int)time);
					joystickLayer.setTimerExpired(false);
				}
			}
			else
				joystickLayer.setTimerExpired(false);
											
			if (app.settings.MAVLINK && app.isMavlinkConnected())
			{
				// To MAVLink
				// Lo faccio in un altro thread
				//sendRcOverrideMsg();
				flyInterfaceLayer.setTxtRcVisibility(View.VISIBLE);
			}
			else
			{
				flyInterfaceLayer.setTxtRcVisibility(View.GONE);
			}
			
			if (app.settings.PPMSUM)
			{
				app.channelManager.updateChannelsArray();				

				flyInterfaceLayer.setTxtRcVisibility(View.GONE);
				// Initialize PulseGenerator
				if (!bInitPulseGenerator){
					noise.init(app.channelManager.getChannelsArray());
					bInitPulseGenerator = true;
				}
				
				// To audio out
				if (noise != null)
					noise.setPulseValues(app.channelManager.getChannelsArray());
			}
			
			if (!app.settings.MAVLINK && !app.settings.PPMSUM && !paused)
			{
				if (System.currentTimeMillis() - lastToastShowed > 7000)
				{
					showToast("No transmission protocol selected!\nGo to: Settings->Output signal->Transmission types");
					lastToastShowed = System.currentTimeMillis();
				}
			}				
			
			// To serial
			/*String msgToSend = "$:";
			for (int i = 0; i < channelValuesArray.length; i++)
			{
				msgToSend +=channelValuesArray[i]+",";
			}
			msgToSend = (String) msgToSend.subSequence(0, msgToSend.length()-1);
			msgToSend += "\n\r";			
			msgToSend = "$:"+channelValuesArray[0]+","
							+channelValuesArray[1]+","
							+channelValuesArray[2]+","
							+channelValuesArray[3]+","
							+channelValuesArray[4]+","
							+channelValuesArray[5]+","
							+channelValuesArray[6]+","
							+channelValuesArray[7]+"\n\r";
			Log.d("MSGTOSEND", msgToSend);
			/*byte[] data = msgToSend.getBytes();
			if (mSerialIoManager != null)
			{
				mSerialIoManager.writeAsync(data);	
			}	*/
			
			long delay = System.currentTimeMillis() - startTime;  
						
			if (KEEP_RUNNING)
				mHandlerTransmission.postDelayed(transmit, (long)(1000.0f/(float)app.settings.TRANSMISSION_RATE) - delay);
		}
	};
	

	@Override
	public void onRollChanged() {
		flyInterfaceLayer.onRollChanged();
	}

	@Override
	public void onPitchChanged() {
		flyInterfaceLayer.onPitchChanged();
	}

	@Override
	public void onYawChanged() {
		flyInterfaceLayer.onYawChanged();
	}

	@Override
	public void onThrottleChanged() {
		flyInterfaceLayer.onThrottleChanged();
	}

//	@Override
//	public void onToggleConnectionState() {
//		app.toggleConnectionState();
//	}
//
//	@Override
//	public void startRadioSettings() {
//		Intent intent = new Intent(RadioActivity.this, SettingsActivity.class);
//		try
//		{
//			startActivityForResult(intent, LaserConstants.SETTINGS_CODE);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public void onSwitchToMap() {
		switchToMap();
	}

	@Override
	public void onSwitchToCam() {
		switchToCam();
	}
	
	@Override
	public void onStartStream() {
		playerLayer.startPlayer();
	}

	@Override
	public void onTrimClick() {
		joystickLayer.onTrimClick();
	}

	@Override
	public void rcOverrideStateChanged() {
		lockMap(app.isRcOverrided());
		//app.channelManager.updateChannels(this.app.settings, -1);	
	}

	@Override
	public void onWaypointsReceived(List<waypoint> waypoints) {
		onWaypointsReceived();		
	}

	private void onWaypointsReceived() {
		if (mapFragment != null)
		{
			mapFragment.update(app.drone, null, app.getAirportsList());
			mapFragment.updateHomeToMap(app.drone);
			mapFragment.zoomToExtents(app.drone.getAllCoordinates());
		}
	}

	@Override
	public void onAirportsLoaded(final List<Airport> airports) {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RadioActivity.this);
				boolean showAirports = prefs.getBoolean("pref_show_airports", false);
				if (mapFragment != null && showAirports)
					mapFragment.update(app.drone, null, airports);
			}
		});
	}


	@Override
	public void onLocationChanged(Location location) {}

	@Override
	public void onLocationManagerConnectionFailed(ConnectionResult connectionResult) {
		if (!paused) {
			showToast("Location service - connection failed - " + connectionResult.getErrorCode());
			/*
	         * Google Play services can resolve some errors it detects.
	         * If the error has a resolution, try sending an Intent to
	         * start a Google Play services activity that can resolve error.
	         */
	        if (connectionResult.hasResolution()) {
	            try {
	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(this, CustomLocationManager.CONNECTION_FAILURE_RESOLUTION_REQUEST);
	                // Thrown if Google Play services canceled the original PendingIntent
	            } catch (IntentSender.SendIntentException e) {
	                // Log the error
	                e.printStackTrace();
	            }
	        } else {
	            // If no resolution is available, display a dialog to the user with the error./
	            //showErrorDialog(connectionResult.getErrorCode());
	        }
		}
	}

	@Override
	public void onLocationManagerConnected() {
		if (!paused)
			showToast("Location service connected");
	}

	@Override
	public void onLocationManagerDisconnected() {
		if (!paused)
			showToast("Location service disconnected");
	}
}
