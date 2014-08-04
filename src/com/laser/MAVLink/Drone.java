package com.laser.MAVLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_raw_imu;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.laser.helpers.TTS;
import com.google.android.gms.maps.model.LatLng;

public class Drone {
	
	public waypoint home;
	public Double defaultAlt;
	public List<waypoint> waypoints;

	private double roll = 0, pitch = 0, yaw = 0, altitude = 0, disttowp = 0,
			verticalSpeed = 0, groundSpeed = 0, airSpeed = 0, targetSpeed = 0,
			targetAltitude = 0, battVolt = -1, battRemain = -1,
			battCurrent = -1;
	private int wpno = -1, satCount = -1, fixType = -1,
			type = MAV_TYPE.MAV_TYPE_FIXED_WING;
	private short hdop = -1;
	private boolean failsafe = false, armed = false;
	private ApmModes mode = ApmModes.UNKNOWN;
	private LatLng position;
	private double gpsAltitude = 0;
	private msg_rc_channels_raw rcChannels;
	private msg_servo_output_raw rcOutput;	
	private float climbRate = 0;

	private ArrayList<HudUpdatedListner> hudListenerList = new ArrayList<HudUpdatedListner>();
	private MapUpdatedListner mapListner;
	private DroneListener droneListener;
	private TTS tts;
	
//	private boolean newListener = false;
		
	public msg_raw_imu m_last_imu_raw = null;
	
	private MAVLink_Sensors sensors_present;
	private MAVLink_Sensors sensors_enabled;
	private MAVLink_Sensors sensors_health;
	private String sensors_error_message = "";
	private int sysId = -1;
	private int compId = -1;
	
	

	public interface HudUpdatedListner {
		public void onDroneUpdate();
	}

	public interface MapUpdatedListner {
		public void onDronePositionUpdate();
	}

	public interface DroneListener {
//		Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//		public void onDroneTypeChanged();
		public void onDroneArmedUpdate();
		public void onDroneModeChanged();
	}
	

	public Drone(TTS tts) {
		super();
		this.tts = tts;
		this.home = new waypoint(0.0, 0.0, 0.0, MAV_CMD.MAV_CMD_NAV_WAYPOINT); //TODO: sarà giusto MAV_CMD_NAV_WAYPOINT per la home?
		this.defaultAlt = 100.0;
		this.waypoints = new ArrayList<waypoint>();
	}

	public void setHudListner(HudUpdatedListner listner) {
		//hudListner = listner;
		hudListenerList.add(listner);
	}

	public void setMapListner(MapUpdatedListner listner) {
		mapListner = listner;
	}

	public void setDroneActivityListener(DroneListener listener) {
		droneListener = listener;
//		newListener = true;
	}
//	
//	public void setDroneUpdateListner(DroneUpdateListner listner) {
//		activityListner = listner;
//	}

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
		notifyHudUpdate();
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed) {
		this.altitude = altitude;
		this.groundSpeed = groundSpeed;
		this.airSpeed = airSpeed;
		notifyHudUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		this.disttowp = disttowp;
		targetAltitude = alt_error + altitude;
		targetSpeed = aspd_error + airSpeed;
		notifyHudUpdate();
	}

	public void setBatteryState(double battVolt, double battRemain,
			double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			tts.batteryDischargeNotification(battRemain);
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			notifyHudUpdate();
//			notifyBatteryUpdate();
		}
	}

	public void setArmedAndFailsafe(boolean armed, boolean failsafe) {
		if (this.armed != armed | this.failsafe != failsafe) {
			if (this.armed != armed) {
				tts.speakArmedState(armed);					
			}
			this.armed = armed;
			this.failsafe = failsafe;
			notifyHudUpdate();
			notifyArmedUpdate();
		}
	}

	public void setGpsState(int fix, int satellites_visible, short eph) {
		if (satCount != satellites_visible | fixType != fix | hdop != eph) {
			if (fixType != fix) {
				tts.speakGpsMode(fix);
			}
			this.fixType = fix;
			this.satCount = satellites_visible;
			this.hdop = eph;
			notifyHudUpdate();
		}
	}


	public void setId(int sys, int comp) {
		if (sys > 0 && sys <= 255 && comp > 0 && comp <= 255) {
			if (sysId > 0)
				return;
			this.sysId = sys;
			this.compId = comp;
		}
	}
	
	public void invalidateId() {
		this.sysId = -1;
		this.compId = -1;
	}
	
	public byte getsysId() {
		return (byte) this.sysId;
	}
	

	public byte getcompId() {
		return (byte) this.compId;
	}

	public void setType(int type) {
		if (this.type != type ) { //|| newListener) {
			this.type = type;
//			newListener = false;
//			Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//			notifyTypeChanged();
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			tts.speakMode(mode);
			notifyHudUpdate();
			notifyModeChanged();
		}
	}

	public void setWpno(int wpno) {
		if (this.wpno != wpno) {
			this.wpno = wpno;
			tts.speak("Going for waypoint "+wpno);
			notifyHudUpdate();
		}
	}

	public void setPosition(LatLng position) {
		if (this.position != position) {
			this.position = position;
			notifyPositionChange();
		}
	}

	private void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDronePositionUpdate();
		}
	}

//	Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//	private void notifyTypeChanged() {
//		if (droneListener != null) {
//			droneListener.onDroneTypeChanged();
//		}
//	}
	
	private void notifyModeChanged() {
		if (droneListener != null) {
			droneListener.onDroneModeChanged();
		}
	}

	private void notifyHudUpdate() {
		for (int i = 0; i < hudListenerList.size(); i++)
		{
			if (hudListenerList.get(i) != null)
				hudListenerList.get(i).onDroneUpdate();
		}
	}
	
//	private void notifyBatteryUpdate()
//	{
//		if (activityListner != null)
//			activityListner.onDroneBatteryUpdate();
//	}
	
	private void notifyArmedUpdate() {
		if (droneListener != null)
			droneListener.onDroneArmedUpdate();
	}

	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getDisttowp() {
		return disttowp;
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public double getBattVolt() {
		return battVolt;
	}

	public double getBattRemain() {
		return battRemain;
	}

	public double getBattCurrent() {
		return battCurrent;
	}

	public int getWpno() {
		return wpno;
	}

	public int getSatCount() {
		return satCount;
	}
	
	public double getHDOP() {
		return ((double)hdop)/100.0;
	}

	public int getFixType() {
		return fixType;
	}

	public int getType() {
		return type;
	}

	public ApmModes getMode() {
		return mode;
	}

	public LatLng getPosition() {
		return position;
	}

	public boolean isFailsafe() {
		return failsafe;
	}

	public boolean isArmed() {
		return armed;
	}

	public void setWaypoints(List<waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public void addWaypoints(List<waypoint> points) {
		waypoints.addAll(points);
	}

	public void addWaypoint(Double Lat, Double Lng, Double h, int cmd) {
		waypoints.add(new waypoint(Lat, Lng, h, cmd));
	}

	public void addWaypoint(LatLng coord, Double h, int cmd) {
		waypoints.add(new waypoint(coord, h, cmd));
	}

	public void addWaypoint(LatLng coord, int cmd) {
		addWaypoint(coord, getDefaultAlt(), cmd);
	}

	public void clearWaypoints() {
		waypoints.clear();
	}

	public String getWaypointData() {
		String waypointData = String.format(Locale.ENGLISH, "Home\t%2.0f\n",
				home.Height);
		waypointData += String.format("Def:\t%2.0f\n", getDefaultAlt());

		int i = 1;
		for (waypoint point : waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.Height);
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(Double defaultAlt) {
		this.defaultAlt = defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public void setHome(waypoint home) {
		this.home = home;
	}

	public void setHome(LatLng home) {
		this.home.coord = home;
	}

	public void moveWaypoint(LatLng coord, int number) {
		waypoints.get(number).coord = coord;
	}

	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}

	public void setClimbRate(float climb) {
		climbRate = climb;
	}
	
	public float getClimbRate() {
		return climbRate;
	}

	public void setRcChannelsRaw(msg_rc_channels_raw m_rc) {
		this.rcChannels = m_rc;
	}
	
	public msg_rc_channels_raw getRcChannelsRaw()
	{
		return this.rcChannels;
	}

	public void setRcOutputRaw(msg_servo_output_raw m_rc) {
		this.rcOutput = m_rc;
	}
	
	public msg_servo_output_raw getRcOutputRaw()
	{
		return this.rcOutput;
	}

	public void setSensorsHealth(int onboard_control_sensors_present,
								 int onboard_control_sensors_enabled,
								 int onboard_control_sensors_health) 
	{
		this.sensors_present = new MAVLink_Sensors(onboard_control_sensors_present);
		this.sensors_enabled = new MAVLink_Sensors(onboard_control_sensors_enabled);
		this.sensors_health = new MAVLink_Sensors(onboard_control_sensors_health);		
		
        if (sensors_health.getGps() != sensors_enabled.getGps())
        	sensors_error_message = "Bad GPS Health";
        else if (sensors_health.getGyro() != sensors_enabled.getGyro())
        	sensors_error_message = "Bad Gyro Health";
        else if (sensors_health.getAccelerometer() != sensors_enabled.getAccelerometer())
        	sensors_error_message = "Bad Accel Health";
        else if (sensors_health.getCompass() != sensors_enabled.getCompass())
        	sensors_error_message = "Bad Compass Health";
        else if (sensors_health.getBarometer() != sensors_enabled.getBarometer())
        	sensors_error_message = "Bad Baro Health";
        else if (sensors_health.getOpticalFlow() != sensors_enabled.getOpticalFlow())
        	sensors_error_message = "Bad OptFlow Health";
        else
    		sensors_error_message = "";
	}
	
	public String getSensorsHealth() {
		return sensors_error_message;
	}

	public void setGpsAltitude(double gpsAltitude) {
		this.gpsAltitude = gpsAltitude;
	}
	public double getGpsAltitude() {
		return gpsAltitude;
	}


}
