package com.laser.helpers.file;

import android.os.Environment;


public class DirectoryPath {

	public static String settingsDirectory = Environment.getExternalStorageDirectory() + "/VrPadStation/";
	
	public static String getVrPadStationPath() { return settingsDirectory; }
	public static String getParametersPath() { return getVrPadStationPath() + "Parameters/"; }
	public static String getWaypointsPath() { return getVrPadStationPath() + "Waypoints/"; }
	public static String getGCPPath() { return getVrPadStationPath() + "GCP/"; }
	public static String getTLogPath() { return getVrPadStationPath() + "Logs/"; }
	public static String getMapsPath() { return getVrPadStationPath() + "Maps/"; }
	public static String getErrorsPath() { return getVrPadStationPath() + "Errors/"; }
	
}
