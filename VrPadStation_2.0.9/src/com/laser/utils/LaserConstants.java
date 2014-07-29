package com.laser.utils;

public class LaserConstants {
	
	public static int PAD_SYSID = 249;
	
	public static boolean USB_SERIAL_CONNECTION = false;
	
	public static final int MAVLINK_TIMEOUT = 8;

	public static float TEXT_SIZE_SMALL = 16;
	public static float TEXT_SIZE_MEDIUM = 20;
	public static float TEXT_SIZE_LARGE = 24;
	
	public static boolean DEBUG = false;	//TODO:
	public static boolean DEBUG_LAYOUT = false;
	
	public static int SETTINGS_CODE = 819;
		
	public static int SCREEN_SIZE = -1;
	
	public static int DRONE_BATTERY_WIDTH = 120;

	public static UnderlayModes UNDERLAY_MODE;
	
	public static MapClickModes MAP_CLICK_MODE = MapClickModes.NONE;
	
	public enum ConnectionState {
		CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING;
	}

	public enum MapClickModes {
		NONE, ADD_WAYPOINTS, SET_GUIDED_POINT, FOLLOW_ME;
	}

	public enum UnderlayModes {
		MAP, CAM;
	}	

	public enum MissionModes {
		MISSION, POLYGON;
	}
}
