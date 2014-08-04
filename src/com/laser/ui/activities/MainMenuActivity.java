package com.laser.ui.activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.google.android.gms.common.ConnectionResult;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.app.VrPadStationApp.ConnectionStateListener;
import com.laser.helpers.CustomLocationManager;
import com.laser.helpers.CustomLocationManager.CustomLocationManagerListener;
import com.laser.utils.LaserConstants;
import com.laser.utils.Utils;

public class MainMenuActivity extends Activity  implements OnClickListener,
														   ConnectionStateListener,
														   CustomLocationManagerListener {
	
	public VrPadStationApp app;

	private ImageButton btnConnect;
	private ImageButton btnSettings;
	private ImageButton btnGcs;
	private ImageButton btnRadio;
	private ImageButton btnGimbal;
	private ImageButton btnAbout;
	private ImageButton btnExit;
	
	private boolean paused = false;
	
	private static String EXPIRATION_DATE = "31/12/2014";	 
    private boolean expired = false;
        
	
	@Override
	protected void onResume() {
		paused = false;
		if (!expired)
			Utils.hideSystemBar(getWindow());
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		Calendar currentCal = Calendar.getInstance();
		currentCal.setTimeInMillis(System.currentTimeMillis());
		Calendar maxCal = Calendar.getInstance();
		maxCal.setTimeInMillis(System.currentTimeMillis());
		maxCal.clear();
		maxCal.set(2014,Calendar.DECEMBER,31);
		
		if (currentCal.after(maxCal))
		{
			setContentView(R.layout.expired);
			expired = true;
		}
		else
		{		
			Utils.hideSystemBar(getWindow());				
			Utils.CheckScreenSize(getWindowManager(), getResources());		
			muteAudioStream(true);			
			setContentView(R.layout.activity_main);		

			app = (VrPadStationApp) getApplication();
			app.setConnectionStateListener(this);
			app.locationManager.setCustomLocationManagerListener(this);

			btnConnect = (ImageButton)findViewById(R.id.btnConnect);
			btnGcs = (ImageButton)findViewById(R.id.btnGcs);
			btnRadio = (ImageButton)findViewById(R.id.btnRadio);
			btnGimbal = (ImageButton)findViewById(R.id.btnGimbal);
			btnSettings = (ImageButton)findViewById(R.id.btnSettings);
			btnAbout = (ImageButton)findViewById(R.id.btnAbout);
			btnExit = (ImageButton)findViewById(R.id.btnExit);
			
			btnConnect.setOnClickListener(this);
			btnGcs.setOnClickListener(this);
			btnRadio.setOnClickListener(this);
			btnGimbal.setOnClickListener(this);
			btnSettings.setOnClickListener(this);
			btnAbout.setOnClickListener(this);
			btnExit.setOnClickListener(this);
			
			if (app.isMavlinkConnected())
				btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.disconnect));
			else
				btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.connect));
			
			enableButtons(true);
		    
		    if (app.locationManager != null)
		    	app.locationManager.connect();
		}
	}
	
	@Override
	protected void onDestroy() {
		if (app.mLibVLC != null)
			app.mLibVLC.stop();

//	    if (app.tts != null) 
//	    	app.tts.destroyTTS();
	    
	    if (app.locationManager != null)
	    	app.locationManager.destroy();
	    
		super.onDestroy();
		
		app.removeConnectionStateListener(this);		
		app.MAVClient.closeMavLink();		
		app.parameterMananger.clearParametersList();
		app.locationManager.removeCustomLocationManagerListener(this);
		
		muteAudioStream(false);
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btnConnect:
			app.toggleConnectionState();
			break;
		case R.id.btnGcs:
			Intent iGcs = new Intent(this, GcsActivity.class);
			try {
				startActivity(iGcs);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnRadio:	
			Intent iRadio = new Intent(this, RadioActivity.class);
			try {
				startActivity(iRadio);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnGimbal:	
			showToast("Not yet implemented!");
			break;
		case R.id.btnSettings:
			Intent iSettings = new Intent(this, SettingsActivity.class);
			try {
				startActivity(iSettings);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnAbout:	
			showDialogAppVersion();
			break;
		case R.id.btnExit:
			finish(); 
			break;
		}
	}
	
	public void showToast(String text)
	{
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
//		if ((requestCode == LaserConstants.SETTINGS_CODE) && (resultCode == Activity.RESULT_OK))
//		{
//			app.settings = (LaserSettings) data.getSerializableExtra("SETTINGS2");
//		}
	}
	
	private void showDialogAppVersion()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setCanceledOnTouchOutside(true);
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			String versionCode = String.valueOf(pInfo.versionCode);
			alertDialog.setTitle(getResources().getString(R.string.app_name) + " " + version + " build " + versionCode);
			alertDialog.setMessage("Author: Laser Navigation s.r.l.\n\nEmail: info@virtualrobotix.com\n\nExpiration date: "+EXPIRATION_DATE+"\n\nDisclaimer: Laser Navigation shall not be liable for any damage caused through use of this application, be it indirect, special, incidental or consequential damages.");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();			
			}
		});
		alertDialog.show();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
			Utils.hideSystemBar(getWindow());
	}
	
	private void enableButtons(boolean enabled)
	{
		btnGcs.setEnabled(enabled);
		btnRadio.setEnabled(enabled);
		btnGimbal.setEnabled(enabled);
	}

	@Override
	public void mavlinkConnected() {
		app.waypointMananger.getWaypoints();
		btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.disconnect));
	}

	@Override
	public void mavlinkDisconnected() {	
		btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.connect));
		//enableButtons(false);
	}

	@Override
	public void mavlinkReceivedData(MAVLinkMessage msg) {}
	
	private void muteAudioStream(boolean mute)
	{
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamMute(AudioManager.STREAM_ALARM, mute);
		am.setStreamMute(AudioManager.STREAM_DTMF, mute);
		am.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute);
		am.setStreamMute(AudioManager.STREAM_RING, mute);
		am.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
		am.setStreamMute(AudioManager.STREAM_VOICE_CALL, mute);	
	}

	@Override
	public void serialReceivedData(byte[] readData) {}
	

	@Override
	public void onLocationChanged(Location location) {
		if (location != null && app.MAVClient != null && app.MAVClient.isConnected()) {
//			String gpsData = "Lat: " + location.getLatitude() + " " +
//							 "Lon: " + location.getLongitude() + " " +
//							 "Alt: " + location.getAltitude() + " " +
//							 "Spd: " + location.getSpeed() + " " +
//							 "Acc: " + location.getAccuracy() + " " +
//							 "Yaw: " + location.getBearing() + " " + 
//							 "Sat: " + app.locationManager.getSatellitesNumber();
			
//			msg_gps_pad_raw_int msg = new msg_gps_pad_raw_int();
//			msg.alt = (int) (location.getAltitude() * 1000);
//			msg.lat = (int) (location.getLatitude() * Math.pow(10, 7));
//			msg.lon = (int) (location.getLongitude() * Math.pow(10, 7));
//			msg.vel = (short) (location.getSpeed() * 100);
//			msg.cog = (short) (location.getBearing() * 100);
//			msg.satellites_visible = (byte) app.locationManager.getSatellitesNumber();			
//			app.MAVClient.sendMavPacket(msg.pack());
			
			//TODO: loggo le mie posizioni, con un messagggio
			// di tipo msg_gps_raw_int ma con sysid definito in LaserConstants.PAD_SYSID 
			msg_gps_raw_int msg = new msg_gps_raw_int();
			msg.sysid = LaserConstants.PAD_SYSID;
			msg.compid = LaserConstants.PAD_SYSID;
			msg.alt = (int) (location.getAltitude() * 1000);
			msg.lat = (int) (location.getLatitude() * Math.pow(10, 7));
			msg.lon = (int) (location.getLongitude() * Math.pow(10, 7));
			msg.vel = (short) (location.getSpeed() * 100);
			msg.cog = (short) (location.getBearing() * 100);
			msg.satellites_visible = (byte) app.locationManager.getSatellitesNumber();	
			MAVLinkPacket pack = msg.pack(false);
			app.MAVClient.sendMavPacket(pack);
		}
	}

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
