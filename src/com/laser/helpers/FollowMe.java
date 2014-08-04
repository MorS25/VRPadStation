package com.laser.helpers;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.laser.MAVLink.Drone;
import com.laser.service.MAVLinkClient;

public class FollowMe {
	
	private MAVLinkClient MAV;
	private Context context;
	private Drone drone;
	private boolean followMeEnabled = false;
//	private LocationManager locationManager;
//	private static final long MIN_TIME_MS = 1000;
//	private static final float MIN_DISTANCE_M = 0;
	
	public FollowMe(MAVLinkClient MAVClient,Context context, Drone drone) {
		this.MAV = MAVClient;
		this.context = context;
		this.drone = drone;
//		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void enableFollowMe() {
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();				
//		// Register the listener with the Location Manager to receive location updates
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, this);		
		followMeEnabled = true;		
	}
	
	public void disableFollowMe() {
		Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
//		locationManager.removeUpdates(this);
		followMeEnabled = false;
	}
	
	public boolean isEnabled() {
		return followMeEnabled;
	}

	public void onLocationChanged(Location location) {
		if (followMeEnabled) {
			Log.d("GPS", "Location:"+location.getProvider()+" lat "+location.getLatitude()+" :lng "+location.getLongitude()+" :alt "+location.getAltitude()+" :acu "+location.getAccuracy());
			waypoint guidedWP = new waypoint(location.getLatitude(), location.getLongitude(), drone.defaultAlt, MAV_CMD.MAV_CMD_NAV_WAYPOINT);
			setGuidedMode(guidedWP);
		}
	}

//	@Override
//	public void onProviderDisabled(String provider) {		
//	}
//
//	@Override
//	public void onProviderEnabled(String provider) {
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {		
//	}

	
	private void setGuidedMode(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2;	// use guided mode enum
		msg.frame = 0; 
		msg.command = 16; 
		msg.param1 = 0; 
		msg.param2 = 0; 
		msg.param3 = 0;
		msg.param4 = 0; 
		msg.x = (float) wp.coord.latitude;
		msg.y = (float) wp.coord.longitude;
		msg.z = wp.Height.floatValue();
		msg.autocontinue = 1; 
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAV.sendMavPacket(msg.pack());
	}
}
