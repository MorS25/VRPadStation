package com.laser.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_STATE;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.laser.MAVLink.MAVLinkArm;
import com.laser.ui.dialogs.OpenMissionDialog;
import com.laser.helpers.CustomLocationManager.CustomLocationManagerListener;
import com.laser.helpers.CustomLocationManager;
import com.laser.helpers.Polygon;
import com.laser.waypoints.MissionReader;
import com.laser.waypoints.MissionWriter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.laser.VrPadStation.R;
import com.laser.airports.Airport;
import com.laser.app.VrPadStationApp;
import com.laser.app.VrPadStationApp.OnAirportsLoadedListener;
import com.laser.app.VrPadStationApp.OnDroneUpdateListener;
import com.laser.app.VrPadStationApp.OnWaypointReceivedListener;
import com.laser.ui.dialogs.PolygonGenerationDialog;
import com.laser.ui.layers.MapLayer;
import com.laser.ui.layers.MapLayer.MapLayerListener;
import com.laser.ui.widgets.SeekBarExtended;
import com.laser.ui.widgets.SpinnerAuto;
import com.laser.ui.widgets.SpinnerAuto.OnSpinnerItemSelectedListener;
import com.laser.utils.LaserConstants;
import com.laser.utils.LaserConstants.MapClickModes;
import com.laser.utils.LaserConstants.MissionModes;

public class GcsActivity extends ParentActivity implements OnWaypointReceivedListener,
													 	   OnDroneUpdateListener,
													 	   MapLayerListener,
													   	   OnAirportsLoadedListener,
													   	   CustomLocationManagerListener {

    
	public VrPadStationApp app;
	private Polygon polygon;
	private double MIN_ALTITUDE = 5.0;
	private double targetAltitude = MIN_ALTITUDE;
	private LatLng guidedPoint;
	private boolean takeOffPreparationCompleted = false;
	private boolean takeOffPreparationStarted = false;
	private boolean takeoffStarted = false;

	private MapLayer mapLayer;
	
	private boolean paused = false;
	
	private Handler handlerDisableRcOverride = new Handler();
	private Runnable runnableDisableRcOverride = new Runnable() {		
		@Override
		public void run() {
			if (!paused) 
			{			
				if ((takeoffStarted || takeOffPreparationStarted || takeOffPreparationCompleted) &&
					app.drone != null && 
					(app.drone.getMode() == ApmModes.ROTOR_GUIDED ||
					app.drone.getMode() == ApmModes.FIXED_WING_GUIDED))
				{
					// se sono in takeoff e sono in guided allora non faccio nulla
				} else {
					// altrimenti disabilito sempre l'override
					app.disableRcOverride();
				}
			}
			handlerDisableRcOverride.postDelayed(runnableDisableRcOverride, 1000);
		}
	};
	
	
        
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
		
		app = (VrPadStationApp) getApplication();
		app.setWaypointReceivedListener(this);
		app.setDroneUpdateListener(this);
		app.setAirportsLoadedListener(this);
		app.locationManager.setCustomLocationManagerListener(this);
		polygon = new Polygon();
		
		setContentView(R.layout.activity_gcs);
		mapLayer = new MapLayer(this, app.drone, polygon, false);
						
		// set listeners
		mapLayer.setListener(this);
		
		// battery receiver
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		checkIntent();	
		mapLayer.init();

		// timer che controlla se ho l'RC override attivo, e lo disabilita
		handlerDisableRcOverride.postDelayed(runnableDisableRcOverride, 1000);
    }	
		    
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context arg0, Intent intent) {
	    	int level = intent.getIntExtra("level", 0);
	    	int plugged = intent.getIntExtra("plugged", 0);		  
	    	
	    	if (plugged == BatteryManager.BATTERY_PLUGGED_AC ||
	    		plugged ==  BatteryManager.BATTERY_PLUGGED_USB)
	    	{
	    		mapLayer.updateBattery(level, true);
	    	} 
	    	else
	    	{
	    		mapLayer.updateBattery(level, false);	    		
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
		app.removeWaypointReceivedListener(this);
		app.removeDroneUpdateListener(this);
		app.removeAirportsLoadedListener(this);
		app.locationManager.removeCustomLocationManagerListener(this);
		
		try{
			unregisterReceiver(mBatInfoReceiver);
		}catch (IllegalArgumentException ex){}
		
		mapLayer.onDestroy();
		super.onDestroy();		
	}
	
	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		paused = false;
		
		if (mapLayer != null) {
			mapLayer.updateMapPreferences();
			mapLayer.update();
			mapLayer.updateAltitude(targetAltitude);
		}
		
		unregisterReceiver(mBatInfoReceiver);
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if (!app.MAVClient.isConnected() && app.isMavlinkConnected())
			app.MAVClient.initMavLink();

		super.onResume();
	}
	
	private void checkIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_VIEW.equals(action) && type != null) {
			Toast.makeText(this, intent.getData().getPath(), Toast.LENGTH_LONG).show();
			openMission(intent.getData().getPath());
			mapLayer.checkIntent();
		}
	}

	private void openMission(String path) {
		MissionReader missionReader = new MissionReader();
		if(missionReader.openMission(path)){
			app.drone.home = missionReader.getHome();
			app.drone.waypoints = missionReader.getWaypoints();
		}		
	}
	
	private void clearWaypointsAndUpdate() {
		app.drone.clearWaypoints();
		mapLayer.clearWaypointsAndUpdate();
	}
	
	private void openMissionFile() {
		OpenMissionDialog missionDialog = new OpenMissionDialog(app.drone) {
			@Override
			public void waypointFileLoaded() {
				mapLayer.waypointFileLoaded();
			}
		};
		missionDialog.launchDialog(this);
	}
	
	private void menuSaveFile() {
		if (writeMission()) {
			Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, R.string.error_when_saving, Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	private boolean writeMission() {
		MissionWriter missionWriter = new MissionWriter(app.drone.home, app.drone.waypoints);
		return missionWriter.saveWaypoints();
	}
	
	public void openPolygonGenerateDialog() {
		double defaultHatchAngle = mapLayer.openPolygonGenerateDialog();
		PolygonGenerationDialog polygonDialog = new PolygonGenerationDialog() {
			@Override
			public void onPolygonGenerated(List<waypoint> list) {
				app.drone.addWaypoints(list);
				mapLayer.update();
			}
		};
		polygonDialog.generatePolygon(defaultHatchAngle, 50.0, polygon, app.drone.getLastWaypoint().coord, app.drone.getDefaultAlt(), this);	
	}

	private boolean wpExists = false;
	private waypoint wpToEdit = null;
	private int wpCommand = MAV_CMD.MAV_CMD_NAV_WAYPOINT; // lo metto come comando di default
	@Override
	public void editWaypoint(final LatLng point) 
	{
		// queste le devo sempre azzerare
		wpExists = false;
		wpToEdit = null;
		wpCommand = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		
		for (waypoint wp : app.drone.getWaypoints()) {
			LatLng p = wp.coord;
			if (p.equals(point)) {
				wpToEdit = wp;
				wpExists = true;
				break;
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Waypoint");
		builder.setCancelable(false);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		final SeekBarExtended altitudeBar = new SeekBarExtended(this);
		altitudeBar.setMinMaxInc(0, 200, 1);
		altitudeBar.setText("Altitude:", true);
		altitudeBar.setUnit("m");
		if (wpExists)
			altitudeBar.setValue(wpToEdit.Height);
		else
			altitudeBar.setValue(app.drone.getDefaultAlt());
		
		final SpinnerAuto spinnerCommand = new SpinnerAuto(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
																android.R.layout.simple_spinner_item, 
																waypoint.getCommandList());
		adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinnerCommand.setAdapter(adapter);
		spinnerCommand.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {			
			@Override
			public void onSpinnerItemSelected(Spinner spinner, int position, String text) {	
				String strCommand = (String) spinnerCommand.getSelectedItem();
				wpCommand = waypoint.parseString(strCommand);
			}
		});		
		
		layout.addView(altitudeBar);
		layout.addView(spinnerCommand);
		builder.setView(layout);
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int id) {}
		});
		
		if (wpExists)
		{
			builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {			
				public void onClick(DialogInterface dialog, int id) {
					app.drone.getWaypoints().remove(wpToEdit);
					mapLayer.onRemoveWaypoint();
				}
			});
		}
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id)
			{
				app.drone.setDefaultAlt((double) altitudeBar.getValue());
				if (!wpExists)
					app.drone.addWaypoint(point, wpCommand);
				else
				{
					wpToEdit.Height = app.drone.getDefaultAlt();
					wpToEdit.cmd = wpCommand;
				}				
				mapLayer.update();
			}
		});
		
		builder.create().show();
	}
		
//	private void changeDefaultAlt2(final LatLng point) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle("Default Altitude");
//		builder.setCancelable(false);
//
//		final NumberPicker numb3rs = new NumberPicker(this);
//		numb3rs.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//		numb3rs.setMaxValue(200);
//		numb3rs.setMinValue(0);
//		numb3rs.setValue((app.drone.getDefaultAlt().intValue()));
//		builder.setView(numb3rs);
//
//		builder.setNegativeButton("Cancel",
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//					}
//				});
//		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				app.drone.setDefaultAlt((double) numb3rs.getValue());
//				//app.drone.addWaypoint(point);
//				boolean exists = false;
//				for (waypoint wp : app.drone.getWaypoints())
//				{
//					LatLng p = wp.coord;
//					if (p.equals(point))
//					{
//						wp.Height = app.drone.getDefaultAlt();
//						exists = true;
//						break;
//					}
//				}
//				if (!exists)
//					app.drone.addWaypoint(point, MAV_CMD.MAV_CMD_NAV_WAYPOINT); // Lo forzo a 16
//
//				mapLayer.update();
//			}
//		});
//		builder.create().show();
//	}
	
	private int backPressCounter = 0;
	public void onBackPressed() 
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
	};
	
	@Override
	public void onWaypointsReceived(List<waypoint> waypoints) {
		mapLayer.onWaypointsReceived();		
	}
	
	@Override
	public void mavlinkConnected() {
		super.mavlinkConnected();
		app.waypointMananger.getWaypoints();
		if (mapLayer != null)
			mapLayer.updateOverviewFragment();
	}
	
	@Override
	public void mavlinkDisconnected() {
		super.mavlinkDisconnected();		
	}
		
	@Override
	public void mavlinkReceivedData(MAVLinkMessage m) {
		super.mavlinkReceivedData(m);
		mapLayer.receiveData(m);
		mapLayer.updateFlightModesFragment(m);
		
		// se ho iniziato la procedura di takeoff
		if (app.drone != null && app.drone.isArmed() && takeOffPreparationCompleted &&
			(app.drone.getMode() == ApmModes.ROTOR_GUIDED || app.drone.getMode() == ApmModes.FIXED_WING_GUIDED)) 
		{
			// Setto il waypoint
			LatLng dronePos = app.drone.getPosition();
			waypoint guidedWP = new waypoint(dronePos.latitude, dronePos.longitude, targetAltitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT);
			app.setGuidedMode(guidedWP);	
			
			takeOffPreparationCompleted = false;
			takeoffStarted = true;
		}
		
		// se sono in take off
		// e l'altitudine del drone raggiunge o supera l'altitudine da me impostata
		// esco dal take off e disabilito l'RC override
		if (takeoffStarted)
		{
			if (app.drone.getAltitude() >= (targetAltitude * 90 / 100))
			{
				takeoffStarted = false;	    		
	    		app.disableRcOverride();		
			}
		}

		if (m.msgid == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT)
		{
			msg_heartbeat msg_heart = (msg_heartbeat) m;
			if (LaserConstants.DEBUG)
				Log.d("MAV_STATE", msg_heart.system_status + "");
			if (msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_STANDBY)
			{
				if (LaserConstants.DEBUG)
					Log.d("MAV_STATE_STANDBY", "MAV_STATE_STANDBY");
				{
					if (app.drone.getRcChannelsRaw() != null && 
						app.drone.getRcChannelsRaw().chan3_raw > app.settings.RC2_MIN)
						app.disableRcOverride();
				}
			}
		}
	}
	
	@Override
	public void onDroneModeChanged() {
		if (mapLayer != null) {
			mapLayer.onDroneModeChanged(app.drone);
		}
	}
	
//	Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//	@Override
//	public void onDroneTypeChanged() {
//		if (mapLayer != null)
//			mapLayer.onDroneTypeChanged();
//	}

	@Override
	public void onDroneArmedUpdate() {
		if (mapLayer != null)
			mapLayer.updateOverviewFragment();
	}

	@Override
	public void onSetCurrentWaypoint(short position) {
		app.waypointMananger.setCurrentWaypoint((short) position);
	}

	@Override
	public void onChangeFlightMode(String mode) {
		ApmModes apmMode = ApmModes.getMode(mode,app.drone.getType());
		if (apmMode != ApmModes.UNKNOWN) {
			app.changeFlightMode(apmMode);
		}
	}

	@Override
	public void writeWaypointsToApm() {
		List<waypoint> data = new ArrayList<waypoint>();
		data.add(app.drone.getHome());
		data.addAll(app.drone.getWaypoints());
		app.waypointMananger.writeWaypoints(data);
	}

	@Override
	public void writeWaypointsToFile() {
		menuSaveFile();
	}

	@Override
	public void loadWaypointsFromApm() {
		app.waypointMananger.getWaypoints();
	}

	@Override
	public void loadWaypointsFromFile() {
		openMissionFile();
	}

	@Override
	public void clearWaypoints() {
		clearWaypointsAndUpdate();
	}

	@Override
	public void enablePolygonMode(boolean isEnabled) {
		if (isEnabled)
			setModeToPolygon();
		else
		{
			setModeToMission();
			mapLayer.update();
		}
	}
	
	private void setModeToMission() {
		mapLayer.setMissionMode(MissionModes.MISSION);
		Toast.makeText(this, R.string.exiting_polygon_mode, Toast.LENGTH_SHORT)
				.show();       
	}
	
	private void setModeToPolygon() {
		mapLayer.setMissionMode(MissionModes.POLYGON);
		Toast.makeText(this, R.string.entering_polygon_mode, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void generatePolygon() {
		openPolygonGenerateDialog();
	}
	
	@Override
	public void clearPolygon() {
		polygon.clearPolygon();
		mapLayer.update();
	}
	
	@Override
	public void toggleArmed() {
		if (app.isMavlinkConnected())
			createToggleArmConfirmDialog();
		else
			showToast("MAVLink not connected");
	}
	
	private void createToggleArmConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (app.drone.isArmed())
			builder.setMessage("Confirm DISARM command?");
		else
			builder.setMessage("Confirm ARM command?");
		builder.setNegativeButton("Cancel", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Ok", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MAVLinkArm.sendArmMessage(app.MAVClient, !app.drone.isArmed(), app.drone.isArmed(), app.drone);
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

	@Override
	public void clearTrack() {
		mapLayer.clearFlightPath();
	}

	@Override
	public void zoom() {
		mapLayer.onZoom();
	}

	@Override
	public void setModeAuto() {
		switch (app.drone.getType())
		{
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			app.changeFlightMode(ApmModes.ROTOR_AUTO);
			break;
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
			app.changeFlightMode(ApmModes.FIXED_WING_AUTO);
			break;
		default:
			app.changeFlightMode(ApmModes.UNKNOWN);
			break;
		}
	}

	@Override
	public void takeOff()
	{
		createTakeoffConfirmDialog();
	}
	
	private void createTakeoffConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Confirm TAKEOFF command?");
		builder.setNegativeButton("Cancel", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Ok", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (app.isMavlinkConnected())
				{
					takeOffPreparationStarted = true;
					
					// Armo
					if (!app.drone.isArmed())
						MAVLinkArm.sendArmMessage(app.MAVClient, true, app.drone.isArmed(), app.drone);		
					// Setto il modo in guided
					switch (app.drone.getType())
					{
					case MAV_TYPE.MAV_TYPE_TRICOPTER:
					case MAV_TYPE.MAV_TYPE_QUADROTOR:
					case MAV_TYPE.MAV_TYPE_HEXAROTOR:
					case MAV_TYPE.MAV_TYPE_OCTOROTOR:
					case MAV_TYPE.MAV_TYPE_HELICOPTER:
						app.changeFlightMode(ApmModes.ROTOR_GUIDED);
						break;
					case MAV_TYPE.MAV_TYPE_FIXED_WING:
						app.changeFlightMode(ApmModes.FIXED_WING_GUIDED);
						break;
					default:
						app.changeFlightMode(ApmModes.UNKNOWN);
						break;
					}	
					// Aumento il throttle
					app.sendTakeOffMsg();	
					// takeOffPreparationStarted la uso per evitare che il timer dell'RC mi disabiliti l'RC nel caso passi in questo istante					
					takeOffPreparationStarted = false;
					takeOffPreparationCompleted = true;
				}
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

	@Override
	public void setAltitude(double offset) {
		targetAltitude += offset;
		if (targetAltitude < MIN_ALTITUDE)
		{
			targetAltitude = MIN_ALTITUDE;
			showToast("Cannot set altitude under " + MIN_ALTITUDE + " meters");
		}
		else
		{
			if ((app.drone.getMode() == ApmModes.ROTOR_GUIDED || app.drone.getMode() == ApmModes.FIXED_WING_GUIDED) && 
				LaserConstants.MAP_CLICK_MODE == MapClickModes.SET_GUIDED_POINT)
			{
				app.drone.setDefaultAlt(targetAltitude);
				if (guidedPoint != null)
					app.setGuidedMode(new waypoint(guidedPoint, targetAltitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
			}
			else if ((app.drone.getMode() == ApmModes.ROTOR_GUIDED || app.drone.getMode() == ApmModes.FIXED_WING_GUIDED) && 
					LaserConstants.MAP_CLICK_MODE == MapClickModes.FOLLOW_ME)
			{
				app.drone.setDefaultAlt(targetAltitude);
			}
		}
		mapLayer.updateAltitude(targetAltitude);
	}


	@Override
	public void onSetGuidedMode(LatLng point) {
		guidedPoint = point;
		app.drone.setDefaultAlt(targetAltitude);
		app.setGuidedMode(new waypoint(point, targetAltitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
	}

	@Override
	public void modeGuided() {
		app.drone.setDefaultAlt(targetAltitude);
		if (LaserConstants.MAP_CLICK_MODE == MapClickModes.SET_GUIDED_POINT)
			showToast("Long click on map for setting a waypoint");
		if (app.followMe.isEnabled())
			app.followMe.disableFollowMe();

		// gli mando le coordinate attuali del drone come waypoint così son sicuro che resta lì
		app.setGuidedMode(new waypoint(app.drone.getPosition(), targetAltitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
		
		// lo setto in guided
		switch (app.drone.getType())
		{
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			app.changeFlightMode(ApmModes.ROTOR_GUIDED);
			break;
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
			app.changeFlightMode(ApmModes.FIXED_WING_GUIDED);
			break;
		default:
			showToast("Error reading drone type.");
			break;
		}
	}

	@Override
	public void modeFollowMe() {
		app.drone.setDefaultAlt(targetAltitude);
		
		// gli mando le coordinate attuali del drone come waypoint così son sicuro che resta lì
		app.setGuidedMode(new waypoint(app.drone.getPosition(), targetAltitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
		
		// lo setto in guided
		switch (app.drone.getType())
		{
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			app.changeFlightMode(ApmModes.ROTOR_GUIDED);
			break;
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
			app.changeFlightMode(ApmModes.FIXED_WING_GUIDED);
			break;
		default:
			showToast("Error reading drone type.");
			break;
		}
		
		if (LaserConstants.MAP_CLICK_MODE == MapClickModes.FOLLOW_ME)
			app.followMe.enableFollowMe();
		else
			app.followMe.disableFollowMe();
	}

	@Override
	public void addWaypointMode() {
		if (app.followMe.isEnabled())
			app.followMe.disableFollowMe();
	}

	@Override
	public void onAirportsLoaded(final List<Airport> airports) {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GcsActivity.this);
				boolean showAirports = prefs.getBoolean("pref_show_airports", false);
				if (mapLayer != null && showAirports)
					mapLayer.onAirportsLoaded(airports);	
			}
		});	
	}

	@Override
	public void onLocationChanged(Location location) {
		app.followMe.onLocationChanged(location);
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
