package com.laser.helpers.file;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public class FileManager {

	public static String getVrPadStationPath() 
	{
		String root = Environment.getExternalStorageDirectory().toString();
		return (root + "/VrPadStation/");
	}

	public static String getWaypointsPath() { return getVrPadStationPath() + "/Waypoints/";	}
	public static String getGCPPath() {	return getVrPadStationPath() + "/GCP/"; }
	private static String getTLogPath() { return getVrPadStationPath() + "/Logs/"; }
	public static String getMapsPath() { return getVrPadStationPath() + "/Maps/"; }

	public static String[] loadWaypointFileList() 
	{
		File mPath = new File(getWaypointsPath());
		try {
			mPath.mkdirs();
		} catch (SecurityException ex) {
			return null;
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.contains(".txt");
				}
			};
			return mPath.list(filter);
		} else {
			return null;
		}
	}

	public static String[] loadKMZFileList() 
	{
		File mPath = new File(getGCPPath());
		try {
			mPath.mkdirs();
		} catch (SecurityException ex) {
			return new String[0];
		}
		if (mPath.exists())
		{
			FilenameFilter filter = new FilenameFilter() 
			{
				public boolean accept(File dir, String filename) 
				{
					return filename.contains(".kml") || filename.contains(".kmz");
				}
			};
			return mPath.list(filter);
		} else {
			return new String[0];
		}
	}
	
	public static String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",
				Locale.US);
		String timeStamp = sdf.format(new Date());
		return timeStamp;
	}
	
	public static boolean isExternalStorageAvaliable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) 
			return true;
		
		return false;
	}

}
